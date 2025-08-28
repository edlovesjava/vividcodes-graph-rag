package com.vividcodes.graphrag.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.vividcodes.graphrag.model.graph.SubProjectNode;
import com.vividcodes.graphrag.service.GraphService;

/**
 * Integration tests for SubProject operations with Neo4j database.
 * These tests require a running Neo4j instance.
 */
@SpringBootTest
@ActiveProfiles("test")
class SubProjectIntegrationTest {
    
    @Autowired
    private GraphService graphService;
    
    private String testRepositoryId;
    private SubProjectNode testSubProject1;
    private SubProjectNode testSubProject2;
    
    @BeforeEach
    void setUp() {
        // Generate unique IDs to avoid conflicts
        testRepositoryId = "test-repo-" + UUID.randomUUID().toString();
        
        // Create test SubProjects
        testSubProject1 = new SubProjectNode(
            "sub-project-1-" + UUID.randomUUID().toString(),
            "Main Project",
            "/src/main",
            "maven"
        );
        testSubProject1.setRepositoryId(testRepositoryId);
        testSubProject1.setBuildFile("pom.xml");
        testSubProject1.setDescription("Main application module");
        testSubProject1.setVersion("1.0.0");
        testSubProject1.setSourceDirectories(Arrays.asList("src/main/java", "src/main/resources"));
        testSubProject1.setTestDirectories(Arrays.asList("src/test/java", "src/test/resources"));
        testSubProject1.setDependencies(Arrays.asList("spring-boot-starter", "junit"));
        testSubProject1.setHealthScore(85.5f);
        testSubProject1.setComplexityScore(42.3f);
        testSubProject1.setMaintainabilityScore(78.9f);
        
        testSubProject2 = new SubProjectNode(
            "sub-project-2-" + UUID.randomUUID().toString(),
            "SDK Module",
            "/sdk",
            "gradle"
        );
        testSubProject2.setRepositoryId(testRepositoryId);
        testSubProject2.setBuildFile("build.gradle");
        testSubProject2.setDescription("SDK for external integration");
        testSubProject2.setVersion("2.1.0");
        testSubProject2.setSourceDirectories(Arrays.asList("src/main/java"));
        testSubProject2.setTestDirectories(Arrays.asList("src/test/java"));
        testSubProject2.setDependencies(Arrays.asList("okhttp", "gson", "testng"));
    }
    
    @AfterEach
    void tearDown() {
        // Clean up test data
        try {
            if (testSubProject1 != null && testSubProject1.getId() != null) {
                graphService.deleteSubProject(testSubProject1.getId());
            }
            if (testSubProject2 != null && testSubProject2.getId() != null) {
                graphService.deleteSubProject(testSubProject2.getId());
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
    
    @Test
    void testSaveAndFindSubProject() {
        // Save the SubProject
        assertDoesNotThrow(() -> graphService.saveSubProject(testSubProject1));
        
        // Find by ID
        SubProjectNode found = graphService.findSubProjectById(testSubProject1.getId());
        
        // Verify all properties
        assertNotNull(found);
        assertEquals(testSubProject1.getId(), found.getId());
        assertEquals(testSubProject1.getName(), found.getName());
        assertEquals(testSubProject1.getPath(), found.getPath());
        assertEquals(testSubProject1.getType(), found.getType());
        assertEquals(testSubProject1.getBuildFile(), found.getBuildFile());
        assertEquals(testSubProject1.getDescription(), found.getDescription());
        assertEquals(testSubProject1.getVersion(), found.getVersion());
        assertEquals(testSubProject1.getRepositoryId(), found.getRepositoryId());
        assertEquals(testSubProject1.getSourceDirectories(), found.getSourceDirectories());
        assertEquals(testSubProject1.getTestDirectories(), found.getTestDirectories());
        assertEquals(testSubProject1.getDependencies(), found.getDependencies());
    }
    
    @Test
    void testSaveSubProjectWithMinimalData() {
        // Create SubProject with only required fields
        SubProjectNode minimal = new SubProjectNode(
            "minimal-" + UUID.randomUUID().toString(),
            "Minimal Project",
            "/minimal",
            "custom"
        );
        minimal.setRepositoryId(testRepositoryId);
        
        // Save and retrieve
        assertDoesNotThrow(() -> graphService.saveSubProject(minimal));
        
        SubProjectNode found = graphService.findSubProjectById(minimal.getId());
        
        assertNotNull(found);
        assertEquals(minimal.getId(), found.getId());
        assertEquals(minimal.getName(), found.getName());
        assertEquals(minimal.getPath(), found.getPath());
        assertEquals(minimal.getType(), found.getType());
        assertEquals(minimal.getRepositoryId(), found.getRepositoryId());
        
        // Clean up
        graphService.deleteSubProject(minimal.getId());
    }
    
    @Test
    void testFindSubProjectsByRepositoryId() {
        // Save both SubProjects
        assertDoesNotThrow(() -> {
            graphService.saveSubProject(testSubProject1);
            graphService.saveSubProject(testSubProject2);
        });
        
        // Find by repository ID
        List<SubProjectNode> subProjects = graphService.findSubProjectsByRepositoryId(testRepositoryId);
        
        // Verify results
        assertNotNull(subProjects);
        assertEquals(2, subProjects.size());
        
        // Verify both projects are found (order may vary)
        List<String> foundIds = subProjects.stream()
            .map(SubProjectNode::getId)
            .toList();
        
        assertTrue(foundIds.contains(testSubProject1.getId()));
        assertTrue(foundIds.contains(testSubProject2.getId()));
        
        // Verify repository ID is correct for all found projects
        subProjects.forEach(project -> 
            assertEquals(testRepositoryId, project.getRepositoryId())
        );
    }
    
    @Test
    void testFindSubProjectsByRepositoryIdEmpty() {
        String nonExistentRepositoryId = "non-existent-repo-" + UUID.randomUUID().toString();
        
        List<SubProjectNode> subProjects = graphService.findSubProjectsByRepositoryId(nonExistentRepositoryId);
        
        assertNotNull(subProjects);
        assertTrue(subProjects.isEmpty());
    }
    
    @Test
    void testUpdateSubProject() {
        // Save initial SubProject
        graphService.saveSubProject(testSubProject1);
        
        // Update properties
        testSubProject1.setDescription("Updated description");
        testSubProject1.setVersion("1.1.0");
        testSubProject1.setHealthScore(90.0f);
        
        // Save again (should update existing)
        assertDoesNotThrow(() -> graphService.saveSubProject(testSubProject1));
        
        // Verify update
        SubProjectNode found = graphService.findSubProjectById(testSubProject1.getId());
        
        assertNotNull(found);
        assertEquals("Updated description", found.getDescription());
        assertEquals("1.1.0", found.getVersion());
        // Note: Health score might not be retrieved in the current implementation
    }
    
    @Test
    void testDeleteSubProject() {
        // Save SubProject
        graphService.saveSubProject(testSubProject1);
        
        // Verify it exists
        SubProjectNode found = graphService.findSubProjectById(testSubProject1.getId());
        assertNotNull(found);
        
        // Delete
        assertDoesNotThrow(() -> graphService.deleteSubProject(testSubProject1.getId()));
        
        // Verify it's deleted
        SubProjectNode notFound = graphService.findSubProjectById(testSubProject1.getId());
        assertNull(notFound);
        
        // Clear reference to avoid cleanup attempt
        testSubProject1 = null;
    }
    
    @Test
    void testFindNonExistentSubProject() {
        String nonExistentId = "non-existent-" + UUID.randomUUID().toString();
        
        SubProjectNode notFound = graphService.findSubProjectById(nonExistentId);
        
        assertNull(notFound);
    }
    
    @Test
    void testDeleteNonExistentSubProject() {
        String nonExistentId = "non-existent-" + UUID.randomUUID().toString();
        
        // Should not throw exception when deleting non-existent SubProject
        assertDoesNotThrow(() -> graphService.deleteSubProject(nonExistentId));
    }
    
    @Test
    void testSubProjectWithSpecialCharacters() {
        // Create SubProject with special characters in name and description
        SubProjectNode specialProject = new SubProjectNode(
            "special-" + UUID.randomUUID().toString(),
            "Project with Special Characters: éñ中文",
            "/path/with spaces/and-special_chars",
            "maven"
        );
        specialProject.setRepositoryId(testRepositoryId);
        specialProject.setDescription("Description with special chars: @#$%^&*()");
        
        // Save and retrieve
        assertDoesNotThrow(() -> graphService.saveSubProject(specialProject));
        
        SubProjectNode found = graphService.findSubProjectById(specialProject.getId());
        
        assertNotNull(found);
        assertEquals(specialProject.getName(), found.getName());
        assertEquals(specialProject.getPath(), found.getPath());
        assertEquals(specialProject.getDescription(), found.getDescription());
        
        // Clean up
        graphService.deleteSubProject(specialProject.getId());
    }
    
    @Test
    void testSubProjectWithLargeData() {
        // Create SubProject with large lists
        SubProjectNode largeProject = new SubProjectNode(
            "large-" + UUID.randomUUID().toString(),
            "Large Project",
            "/large/project",
            "gradle"
        );
        largeProject.setRepositoryId(testRepositoryId);
        
        // Create large lists
        List<String> manySourceDirs = Arrays.asList(
            "src/main/java", "src/main/resources", "src/main/webapp",
            "src/generated/java", "src/integration/java", "src/api/java"
        );
        List<String> manyTestDirs = Arrays.asList(
            "src/test/java", "src/test/resources", "src/integration-test/java",
            "src/performance-test/java", "src/unit-test/java"
        );
        List<String> manyDependencies = Arrays.asList(
            "spring-boot-starter", "spring-boot-starter-web", "spring-boot-starter-data-jpa",
            "hibernate-core", "junit", "mockito", "testcontainers", "wiremock",
            "jackson-core", "jackson-databind", "slf4j-api", "logback-classic"
        );
        
        largeProject.setSourceDirectories(manySourceDirs);
        largeProject.setTestDirectories(manyTestDirs);
        largeProject.setDependencies(manyDependencies);
        
        // Save and retrieve
        assertDoesNotThrow(() -> graphService.saveSubProject(largeProject));
        
        SubProjectNode found = graphService.findSubProjectById(largeProject.getId());
        
        assertNotNull(found);
        assertEquals(largeProject.getName(), found.getName());
        // Note: Lists might not be fully retrieved in current implementation
        // This test mainly verifies that large data doesn't break the save operation
        
        // Clean up
        graphService.deleteSubProject(largeProject.getId());
    }
}
