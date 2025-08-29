package com.vividcodes.graphrag.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.vividcodes.graphrag.model.dto.SubProjectMetadata;

/**
 * Unit tests for SubProjectDetector
 */
class SubProjectDetectorTest {
    
    private SubProjectDetector detector;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        detector = new SubProjectDetector();
    }
    
    @Test
    void testDetectSubProjects_EmptyRepository() {
        // Test with empty repository
        List<SubProjectMetadata> projects = detector.detectSubProjects(tempDir.toString(), "test-repo");
        
        assertNotNull(projects);
        assertTrue(projects.isEmpty());
    }
    
    @Test
    void testDetectSubProjects_NonExistentPath() {
        // Test with non-existent path
        String nonExistentPath = tempDir.resolve("non-existent").toString();
        List<SubProjectMetadata> projects = detector.detectSubProjects(nonExistentPath, "test-repo");
        
        assertNotNull(projects);
        assertTrue(projects.isEmpty());
    }
    
    @Test
    void testDetectMavenProjects_RootPom() throws IOException {
        // Create root pom.xml
        Files.createFile(tempDir.resolve("pom.xml"));
        
        // Create Maven directory structure
        Path srcMainJava = tempDir.resolve("src/main/java");
        Path srcTestJava = tempDir.resolve("src/test/java");
        Files.createDirectories(srcMainJava);
        Files.createDirectories(srcTestJava);
        
        List<SubProjectMetadata> projects = detector.detectSubProjects(tempDir.toString(), "test-repo");
        
        assertEquals(1, projects.size());
        
        SubProjectMetadata project = projects.get(0);
        assertEquals("root", project.getName());
        assertEquals("", project.getPath()); // Root project has empty path
        assertEquals("maven", project.getType());
        assertEquals("pom.xml", project.getBuildFile());
        assertEquals("test-repo", project.getRepositoryId());
        assertNotNull(project.getId());
        assertNotNull(project.getDetectedAt());
        
        // Check source directories
        assertTrue(project.getSourceDirectories().contains("src/main/java"));
        assertTrue(project.getTestDirectories().contains("src/test/java"));
    }
    
    @Test
    void testDetectMavenProjects_MultiModule() throws IOException {
        // Create root pom.xml
        Files.createFile(tempDir.resolve("pom.xml"));
        
        // Create module directories with pom.xml files
        Path module1 = tempDir.resolve("module1");
        Path module2 = tempDir.resolve("module2");
        Files.createDirectories(module1);
        Files.createDirectories(module2);
        Files.createFile(module1.resolve("pom.xml"));
        Files.createFile(module2.resolve("pom.xml"));
        
        // Create source directories for modules
        Files.createDirectories(module1.resolve("src/main/java"));
        Files.createDirectories(module2.resolve("src/test/kotlin"));
        
        List<SubProjectMetadata> projects = detector.detectSubProjects(tempDir.toString(), "test-repo");
        
        assertEquals(3, projects.size()); // Root + 2 modules
        
        // Verify we have projects with expected names
        List<String> projectNames = projects.stream().map(SubProjectMetadata::getName).toList();
        assertTrue(projectNames.contains("root"));
        assertTrue(projectNames.contains("module1"));
        assertTrue(projectNames.contains("module2"));
        
        // Find module1 and verify its properties
        SubProjectMetadata module1Project = projects.stream()
            .filter(p -> "module1".equals(p.getName()))
            .findFirst()
            .orElseThrow();
        
        assertEquals("module1", module1Project.getPath());
        assertEquals("maven", module1Project.getType());
        assertTrue(module1Project.getSourceDirectories().contains("src/main/java"));
    }
    
    @Test
    void testDetectGradleProjects_RootBuildGradle() throws IOException {
        // Create root build.gradle
        Files.createFile(tempDir.resolve("build.gradle"));
        
        // Create Gradle directory structure
        Path srcMainJava = tempDir.resolve("src/main/java");
        Files.createDirectories(srcMainJava);
        
        List<SubProjectMetadata> projects = detector.detectSubProjects(tempDir.toString(), "test-repo");
        
        assertEquals(1, projects.size());
        
        SubProjectMetadata project = projects.get(0);
        assertEquals("root", project.getName());
        assertEquals("", project.getPath());
        assertEquals("gradle", project.getType());
        assertEquals("build.gradle", project.getBuildFile());
        assertEquals("test-repo", project.getRepositoryId());
    }
    
    @Test
    void testDetectGradleProjects_KotlinDSL() throws IOException {
        // Create root build.gradle.kts (Kotlin DSL)
        Files.createFile(tempDir.resolve("build.gradle.kts"));
        
        List<SubProjectMetadata> projects = detector.detectSubProjects(tempDir.toString(), "test-repo");
        
        assertEquals(1, projects.size());
        
        SubProjectMetadata project = projects.get(0);
        assertEquals("gradle", project.getType());
        assertEquals("build.gradle.kts", project.getBuildFile());
    }
    
    @Test
    void testDetectGradleProjects_MultiProject() throws IOException {
        // Create root build.gradle and settings.gradle
        Files.createFile(tempDir.resolve("build.gradle"));
        Files.createFile(tempDir.resolve("settings.gradle"));
        
        // Create sub-project directories with build.gradle files
        Path app = tempDir.resolve("app");
        Path lib = tempDir.resolve("lib");
        Files.createDirectories(app);
        Files.createDirectories(lib);
        Files.createFile(app.resolve("build.gradle"));
        Files.createFile(lib.resolve("build.gradle.kts"));
        
        // Create source directories
        Files.createDirectories(app.resolve("src/main/kotlin"));
        Files.createDirectories(lib.resolve("src/main/groovy"));
        
        List<SubProjectMetadata> projects = detector.detectSubProjects(tempDir.toString(), "test-repo");
        
        assertEquals(3, projects.size()); // Root + app + lib
        
        // Verify project types and build files
        List<String> buildFiles = projects.stream().map(SubProjectMetadata::getBuildFile).toList();
        assertTrue(buildFiles.contains("build.gradle"));
        assertTrue(buildFiles.contains("build.gradle.kts"));
    }
    
    @Test
    void testDetectNpmProjects_RootPackageJson() throws IOException {
        // Create root package.json
        Files.createFile(tempDir.resolve("package.json"));
        
        // Create NPM directory structure
        Path srcDir = tempDir.resolve("src");
        Path testDir = tempDir.resolve("test");
        Files.createDirectories(srcDir);
        Files.createDirectories(testDir);
        
        List<SubProjectMetadata> projects = detector.detectSubProjects(tempDir.toString(), "test-repo");
        
        assertEquals(1, projects.size());
        
        SubProjectMetadata project = projects.get(0);
        assertEquals("root", project.getName());
        assertEquals("npm", project.getType());
        assertEquals("package.json", project.getBuildFile());
        assertTrue(project.getSourceDirectories().contains("src"));
        assertTrue(project.getTestDirectories().contains("test"));
    }
    
    @Test
    void testDetectNpmProjects_IgnoreNodeModules() throws IOException {
        // Create root package.json
        Files.createFile(tempDir.resolve("package.json"));
        
        // Create node_modules with package.json (should be ignored)
        Path nodeModules = tempDir.resolve("node_modules/some-package");
        Files.createDirectories(nodeModules);
        Files.createFile(nodeModules.resolve("package.json"));
        
        List<SubProjectMetadata> projects = detector.detectSubProjects(tempDir.toString(), "test-repo");
        
        assertEquals(1, projects.size()); // Should only find root, not node_modules
        assertEquals("root", projects.get(0).getName());
    }
    
    @Test
    void testDetectMixedProjects() throws IOException {
        // Create a repository with both Maven and Gradle projects
        Files.createFile(tempDir.resolve("pom.xml")); // Root Maven
        
        Path gradleModule = tempDir.resolve("gradle-module");
        Files.createDirectories(gradleModule);
        Files.createFile(gradleModule.resolve("build.gradle"));
        
        Path npmModule = tempDir.resolve("frontend");
        Files.createDirectories(npmModule);
        Files.createFile(npmModule.resolve("package.json"));
        
        List<SubProjectMetadata> projects = detector.detectSubProjects(tempDir.toString(), "test-repo");
        
        assertEquals(3, projects.size());
        
        List<String> types = projects.stream().map(SubProjectMetadata::getType).toList();
        assertTrue(types.contains("maven"));
        assertTrue(types.contains("gradle"));
        assertTrue(types.contains("npm"));
    }
    
    @Test
    void testIsProjectType() throws IOException {
        // Create test directories with different project types
        Path mavenDir = tempDir.resolve("maven-project");
        Path gradleDir = tempDir.resolve("gradle-project");
        Path npmDir = tempDir.resolve("npm-project");
        Path emptyDir = tempDir.resolve("empty-project");
        
        Files.createDirectories(mavenDir);
        Files.createDirectories(gradleDir);
        Files.createDirectories(npmDir);
        Files.createDirectories(emptyDir);
        
        Files.createFile(mavenDir.resolve("pom.xml"));
        Files.createFile(gradleDir.resolve("build.gradle"));
        Files.createFile(npmDir.resolve("package.json"));
        
        // Test Maven detection
        assertTrue(detector.isProjectType(mavenDir, "maven"));
        assertFalse(detector.isProjectType(gradleDir, "maven"));
        assertFalse(detector.isProjectType(npmDir, "maven"));
        assertFalse(detector.isProjectType(emptyDir, "maven"));
        
        // Test Gradle detection
        assertTrue(detector.isProjectType(gradleDir, "gradle"));
        assertFalse(detector.isProjectType(mavenDir, "gradle"));
        assertFalse(detector.isProjectType(npmDir, "gradle"));
        
        // Test NPM detection
        assertTrue(detector.isProjectType(npmDir, "npm"));
        assertFalse(detector.isProjectType(mavenDir, "npm"));
        assertFalse(detector.isProjectType(gradleDir, "npm"));
        
        // Test non-existent directory
        assertFalse(detector.isProjectType(tempDir.resolve("non-existent"), "maven"));
    }
    
    @Test
    void testGetBuildFileName() {
        assertEquals("pom.xml", detector.getBuildFileName("maven"));
        assertEquals("pom.xml", detector.getBuildFileName("MAVEN")); // Case insensitive
        
        assertEquals("build.gradle", detector.getBuildFileName("gradle"));
        assertEquals("build.gradle", detector.getBuildFileName("GRADLE"));
        
        assertEquals("package.json", detector.getBuildFileName("npm"));
        assertEquals("package.json", detector.getBuildFileName("NPM"));
        
        assertEquals(null, detector.getBuildFileName("unknown"));
        assertEquals(null, detector.getBuildFileName(null));
    }
    
    @Test
    void testSubProjectIdGeneration() throws IOException {
        // Create nested project structure to test ID generation
        Files.createFile(tempDir.resolve("pom.xml"));
        
        Path deepModule = tempDir.resolve("modules/deep/nested");
        Files.createDirectories(deepModule);
        Files.createFile(deepModule.resolve("pom.xml"));
        
        List<SubProjectMetadata> projects = detector.detectSubProjects(tempDir.toString(), "test-repo-123");
        
        assertEquals(2, projects.size());
        
        // Verify IDs are unique and properly formatted
        List<String> ids = projects.stream().map(SubProjectMetadata::getId).toList();
        
        assertTrue(ids.stream().allMatch(id -> id.startsWith("test-repo-123_maven_")));
        assertTrue(ids.stream().anyMatch(id -> id.endsWith("_root")));
        assertTrue(ids.stream().anyMatch(id -> id.contains("modules_deep_nested")));
        
        // Ensure all IDs are unique
        assertEquals(2, ids.stream().distinct().count());
    }
}
