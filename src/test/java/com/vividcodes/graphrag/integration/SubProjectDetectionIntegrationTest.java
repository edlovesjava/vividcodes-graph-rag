package com.vividcodes.graphrag.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.vividcodes.graphrag.model.dto.RepositoryMetadata;
import com.vividcodes.graphrag.model.graph.RepositoryNode;
import com.vividcodes.graphrag.model.graph.SubProjectNode;
import com.vividcodes.graphrag.service.RepositoryService;
import com.vividcodes.graphrag.service.SubProjectDetector;

/**
 * Integration test for end-to-end sub-project detection workflow
 */
@SpringBootTest
@ActiveProfiles("test")
class SubProjectDetectionIntegrationTest {
    
    @Autowired
    private SubProjectDetector subProjectDetector;
    
    @Autowired
    private RepositoryService repositoryService;
    
    @TempDir
    Path tempRepositoryDir;
    
    @Test
    void testEndToEndSubProjectDetection() throws IOException {
        // Setup: Create a multi-project repository structure
        setupMultiProjectRepository();
        
        // Step 1: Create repository metadata
        RepositoryMetadata repoMetadata = createTestRepositoryMetadata();
        
        // Step 2: Create repository node
        RepositoryNode repository = repositoryService.createOrUpdateRepository(repoMetadata);
        assertNotNull(repository);
        assertEquals("test-multi-project-repo", repository.getName());
        
        // Step 3: Detect and create sub-projects
        List<SubProjectNode> subProjects = repositoryService.detectAndCreateSubProjects(repository);
        
        // Verify results
        assertNotNull(subProjects);
        assertEquals(4, subProjects.size()); // root + 3 modules
        
        // Verify project types
        List<String> projectTypes = subProjects.stream().map(SubProjectNode::getType).toList();
        assertTrue(projectTypes.contains("maven"));
        assertTrue(projectTypes.contains("gradle"));
        assertTrue(projectTypes.contains("npm"));
        
        // Verify specific projects exist
        List<String> projectNames = subProjects.stream().map(SubProjectNode::getName).toList();
        assertTrue(projectNames.contains("root")); // Root Maven project
        assertTrue(projectNames.contains("backend")); // Maven module
        assertTrue(projectNames.contains("android-app")); // Gradle module
        assertTrue(projectNames.contains("frontend")); // NPM module
        
        // Verify repository relationships
        for (SubProjectNode subProject : subProjects) {
            assertEquals(repository.getId(), subProject.getRepositoryId());
            assertNotNull(subProject.getId());
            assertNotNull(subProject.getCreatedAt());
        }
        
        // Step 4: Test retrieval of sub-projects by repository ID
        List<SubProjectNode> retrievedSubProjects = repositoryService.findSubProjectsByRepository(repository.getId());
        assertEquals(subProjects.size(), retrievedSubProjects.size());
    }
    
    @Test
    void testMavenMultiModuleDetection() throws IOException {
        // Create Maven multi-module project
        setupMavenMultiModuleRepository();
        
        RepositoryMetadata repoMetadata = createTestRepositoryMetadata();
        RepositoryNode repository = repositoryService.createOrUpdateRepository(repoMetadata);
        
        List<SubProjectNode> subProjects = repositoryService.detectAndCreateSubProjects(repository);
        
        // Should detect: root + core + api + web modules
        assertEquals(4, subProjects.size());
        
        // All should be Maven projects
        assertTrue(subProjects.stream().allMatch(sp -> "maven".equals(sp.getType())));
        
        // Verify source directories are detected
        SubProjectNode coreModule = subProjects.stream()
            .filter(sp -> "core".equals(sp.getName()))
            .findFirst()
            .orElseThrow();
        
        assertTrue(coreModule.getSourceDirectories().contains("src/main/java"));
        assertTrue(coreModule.getTestDirectories().contains("src/test/java"));
    }
    
    @Test
    void testGradleMultiProjectDetection() throws IOException {
        // Create Gradle multi-project
        setupGradleMultiProjectRepository();
        
        RepositoryMetadata repoMetadata = createTestRepositoryMetadata();
        RepositoryNode repository = repositoryService.createOrUpdateRepository(repoMetadata);
        
        List<SubProjectNode> subProjects = repositoryService.detectAndCreateSubProjects(repository);
        
        // Should detect: root + app + lib
        assertEquals(3, subProjects.size());
        
        // All should be Gradle projects
        assertTrue(subProjects.stream().allMatch(sp -> "gradle".equals(sp.getType())));
        
        // Verify build files are correctly identified
        List<String> buildFiles = subProjects.stream().map(SubProjectNode::getBuildFile).toList();
        assertTrue(buildFiles.contains("build.gradle"));
        assertTrue(buildFiles.contains("build.gradle.kts"));
    }
    
    @Test
    void testEmptyRepositoryDetection() throws IOException {
        // Test with empty repository (no build files)
        RepositoryMetadata repoMetadata = createTestRepositoryMetadata();
        RepositoryNode repository = repositoryService.createOrUpdateRepository(repoMetadata);
        
        List<SubProjectNode> subProjects = repositoryService.detectAndCreateSubProjects(repository);
        
        // Should detect no sub-projects
        assertTrue(subProjects.isEmpty());
    }
    
    /**
     * Setup a complex multi-project repository with different project types
     */
    private void setupMultiProjectRepository() throws IOException {
        // Root Maven project
        Files.createFile(tempRepositoryDir.resolve("pom.xml"));
        Files.createDirectories(tempRepositoryDir.resolve("src/main/java"));
        Files.createDirectories(tempRepositoryDir.resolve("src/test/java"));
        
        // Maven module: backend
        Path backendModule = tempRepositoryDir.resolve("backend");
        Files.createDirectories(backendModule);
        Files.createFile(backendModule.resolve("pom.xml"));
        Files.createDirectories(backendModule.resolve("src/main/java"));
        Files.createDirectories(backendModule.resolve("src/test/java"));
        
        // Gradle module: android-app
        Path androidModule = tempRepositoryDir.resolve("android-app");
        Files.createDirectories(androidModule);
        Files.createFile(androidModule.resolve("build.gradle"));
        Files.createDirectories(androidModule.resolve("src/main/kotlin"));
        
        // NPM module: frontend
        Path frontendModule = tempRepositoryDir.resolve("frontend");
        Files.createDirectories(frontendModule);
        Files.createFile(frontendModule.resolve("package.json"));
        Files.createDirectories(frontendModule.resolve("src"));
        Files.createDirectories(frontendModule.resolve("test"));
    }
    
    /**
     * Setup a Maven multi-module repository
     */
    private void setupMavenMultiModuleRepository() throws IOException {
        // Root pom.xml
        Files.createFile(tempRepositoryDir.resolve("pom.xml"));
        
        // Core module
        Path coreModule = tempRepositoryDir.resolve("core");
        Files.createDirectories(coreModule);
        Files.createFile(coreModule.resolve("pom.xml"));
        Files.createDirectories(coreModule.resolve("src/main/java"));
        Files.createDirectories(coreModule.resolve("src/test/java"));
        
        // API module
        Path apiModule = tempRepositoryDir.resolve("api");
        Files.createDirectories(apiModule);
        Files.createFile(apiModule.resolve("pom.xml"));
        Files.createDirectories(apiModule.resolve("src/main/java"));
        
        // Web module
        Path webModule = tempRepositoryDir.resolve("web");
        Files.createDirectories(webModule);
        Files.createFile(webModule.resolve("pom.xml"));
        Files.createDirectories(webModule.resolve("src/main/java"));
        Files.createDirectories(webModule.resolve("src/main/resources"));
    }
    
    /**
     * Setup a Gradle multi-project repository
     */
    private void setupGradleMultiProjectRepository() throws IOException {
        // Root build.gradle and settings.gradle
        Files.createFile(tempRepositoryDir.resolve("build.gradle"));
        Files.createFile(tempRepositoryDir.resolve("settings.gradle"));
        
        // App module with Kotlin DSL
        Path appModule = tempRepositoryDir.resolve("app");
        Files.createDirectories(appModule);
        Files.createFile(appModule.resolve("build.gradle.kts"));
        Files.createDirectories(appModule.resolve("src/main/kotlin"));
        
        // Lib module
        Path libModule = tempRepositoryDir.resolve("lib");
        Files.createDirectories(libModule);
        Files.createFile(libModule.resolve("build.gradle"));
        Files.createDirectories(libModule.resolve("src/main/groovy"));
        Files.createDirectories(libModule.resolve("src/test/groovy"));
    }
    
    /**
     * Create test repository metadata
     */
    private RepositoryMetadata createTestRepositoryMetadata() {
        RepositoryMetadata metadata = new RepositoryMetadata();
        metadata.setRepositoryName("test-multi-project-repo");
        metadata.setOrganization("test-org");
        metadata.setRepositoryUrl("https://github.com/test-org/test-multi-project-repo");
        metadata.setBranch("main");
        metadata.setCommitHash("abc123");
        metadata.setCommitDate(LocalDateTime.now());
        metadata.setLocalPath(tempRepositoryDir.toString());
        return metadata;
    }
}
