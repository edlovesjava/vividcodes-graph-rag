package com.vividcodes.graphrag.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.vividcodes.graphrag.model.dto.UpsertResult;
import com.vividcodes.graphrag.model.graph.AnnotationNode;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.FieldNode;
import com.vividcodes.graphrag.model.graph.MethodNode;
import com.vividcodes.graphrag.model.graph.PackageNode;
import com.vividcodes.graphrag.model.graph.RepositoryNode;
import com.vividcodes.graphrag.model.graph.SubProjectNode;

/**
 * Unit tests for GraphService SubProject operations using simple mock implementation
 */
class GraphServiceSubProjectTest {
    
    private TestMockGraphService graphService;
    private SubProjectNode testSubProject;
    
    @BeforeEach
    void setUp() {
        graphService = new TestMockGraphService();
        
        // Create test SubProject
        testSubProject = new SubProjectNode("test-id", "test-project", "/path/to/project", "maven");
        testSubProject.setBuildFile("pom.xml");
        testSubProject.setDescription("Test project");
        testSubProject.setVersion("1.0.0");
        testSubProject.setRepositoryId("repo-123");
        testSubProject.setSourceDirectories(Arrays.asList("src/main/java"));
        testSubProject.setTestDirectories(Arrays.asList("src/test/java"));
        testSubProject.setDependencies(Arrays.asList("junit", "mockito"));
    }
    
    @Test
    void testSaveSubProject() {
        // Execute - should not throw exception
        assertDoesNotThrow(() -> graphService.saveSubProject(testSubProject));
        
        // Verify it was saved to our mock
        assertTrue(graphService.subProjectSaved);
        assertEquals(testSubProject.getId(), graphService.lastSavedSubProject.getId());
        assertEquals(testSubProject.getName(), graphService.lastSavedSubProject.getName());
    }
    
    @Test
    void testSaveSubProjectWithNullValues() {
        // Create SubProject with minimal data
        SubProjectNode minimalProject = new SubProjectNode("min-id", "minimal", "/min/path", "custom");
        
        // Should not throw exception with null optional fields
        assertDoesNotThrow(() -> graphService.saveSubProject(minimalProject));
        
        assertTrue(graphService.subProjectSaved);
        assertEquals("min-id", graphService.lastSavedSubProject.getId());
    }
    
    @Test
    void testFindSubProjectById() {
        String testId = "test-id";
        
        // Set up mock to return our test project
        graphService.subProjectsToReturn.add(testSubProject);
        
        SubProjectNode found = graphService.findSubProjectById(testId);
        
        // Verify
        assertNotNull(found);
        assertEquals("test-id", found.getId());
        assertEquals("test-project", found.getName());
        assertEquals("/path/to/project", found.getPath());
        assertEquals("maven", found.getType());
        assertTrue(graphService.findByIdCalled);
    }
    
    @Test
    void testFindSubProjectByIdNotFound() {
        String testId = "non-existent-id";
        
        // Don't add any projects to return list
        SubProjectNode found = graphService.findSubProjectById(testId);
        
        assertNull(found);
        assertTrue(graphService.findByIdCalled);
    }
    
    @Test
    void testFindSubProjectsByRepositoryId() {
        String repositoryId = "repo-123";
        
        // Set up mock to return multiple projects
        SubProjectNode project1 = new SubProjectNode("project-1", "Project 1", "/path/1", "maven");
        SubProjectNode project2 = new SubProjectNode("project-2", "Project 2", "/path/2", "gradle");
        graphService.subProjectsToReturn.add(project1);
        graphService.subProjectsToReturn.add(project2);
        
        List<SubProjectNode> subProjects = graphService.findSubProjectsByRepositoryId(repositoryId);
        
        assertNotNull(subProjects);
        assertEquals(2, subProjects.size());
        assertEquals("project-1", subProjects.get(0).getId());
        assertEquals("project-2", subProjects.get(1).getId());
        assertTrue(graphService.findByRepositoryIdCalled);
    }
    
    @Test
    void testFindSubProjectsByRepositoryIdEmpty() {
        String repositoryId = "empty-repo";
        
        // Don't add any projects to return list
        List<SubProjectNode> subProjects = graphService.findSubProjectsByRepositoryId(repositoryId);
        
        assertNotNull(subProjects);
        assertTrue(subProjects.isEmpty());
        assertTrue(graphService.findByRepositoryIdCalled);
    }
    
    @Test
    void testDeleteSubProject() {
        String testId = "test-id";
        
        assertDoesNotThrow(() -> graphService.deleteSubProject(testId));
        
        assertTrue(graphService.deleteSubProjectCalled);
        assertEquals(testId, graphService.lastDeletedSubProjectId);
    }
    
    @Test
    void testSubProjectNodeCreation() {
        // Test that SubProjectNode can be created with all required fields
        SubProjectNode node = new SubProjectNode("id", "name", "/path", "type");
        
        assertNotNull(node);
        assertEquals("id", node.getId());
        assertEquals("name", node.getName());
        assertEquals("/path", node.getPath());
        assertEquals("type", node.getType());
    }
    
    @Test
    void testSubProjectNodeWithAllFields() {
        // Test SubProjectNode with all fields set
        SubProjectNode node = new SubProjectNode("full-id", "full-project", "/full/path", "maven");
        node.setBuildFile("pom.xml");
        node.setDescription("Full description");
        node.setVersion("2.0.0");
        node.setRepositoryId("repo-456");
        node.setSourceDirectories(Arrays.asList("src/main/java", "src/main/resources"));
        node.setTestDirectories(Arrays.asList("src/test/java", "src/test/resources"));
        node.setDependencies(Arrays.asList("spring-boot", "junit"));
        
        assertEquals("pom.xml", node.getBuildFile());
        assertEquals("Full description", node.getDescription());
        assertEquals("2.0.0", node.getVersion());
        assertEquals("repo-456", node.getRepositoryId());
        assertEquals(2, node.getSourceDirectories().size());
        assertEquals(2, node.getTestDirectories().size());
        assertEquals(2, node.getDependencies().size());
    }
    
    /**
     * Simple mock implementation of GraphService for testing SubProject operations
     */
    private static class TestMockGraphService implements GraphService {
        // Track method calls
        boolean subProjectSaved = false;
        boolean findByIdCalled = false;
        boolean findByRepositoryIdCalled = false;
        boolean deleteSubProjectCalled = false;
        
        // Store test data
        SubProjectNode lastSavedSubProject;
        String lastDeletedSubProjectId;
        List<SubProjectNode> subProjectsToReturn = new ArrayList<>();
        
        @Override
        public UpsertResult saveSubProject(SubProjectNode subProjectNode) {
            subProjectSaved = true;
            lastSavedSubProject = subProjectNode;
            return UpsertResult.inserted(subProjectNode.getId(), "SubProject", 1L, "test-op");
        }
        
        @Override
        public SubProjectNode findSubProjectById(String id) {
            findByIdCalled = true;
            return subProjectsToReturn.stream()
                .filter(sp -> sp.getId().equals(id))
                .findFirst()
                .orElse(null);
        }
        
        @Override
        public List<SubProjectNode> findSubProjectsByRepositoryId(String repositoryId) {
            findByRepositoryIdCalled = true;
            return new ArrayList<>(subProjectsToReturn);
        }
        
        @Override
        public void deleteSubProject(String id) {
            deleteSubProjectCalled = true;
            lastDeletedSubProjectId = id;
        }
        
        // Empty implementations for other GraphService methods
        @Override
        public UpsertResult savePackage(PackageNode packageNode) {
            return UpsertResult.inserted(packageNode.getId(), "Package", 1L, "test-op");
        }
        
        @Override
        public UpsertResult saveClass(ClassNode classNode) {
            return UpsertResult.inserted(classNode.getId(), "Class", 1L, "test-op");
        }
        
        @Override
        public UpsertResult saveMethod(MethodNode methodNode) {
            return UpsertResult.inserted(methodNode.getId(), "Method", 1L, "test-op");
        }
        
        @Override
        public UpsertResult saveField(FieldNode fieldNode) {
            return UpsertResult.inserted(fieldNode.getId(), "Field", 1L, "test-op");
        }
        
        @Override
        public UpsertResult saveAnnotation(AnnotationNode annotationNode) {
            return UpsertResult.inserted(annotationNode.getId(), "Annotation", 1L, "test-op");
        }
        
        @Override
        public UpsertResult saveRepository(RepositoryNode repositoryNode) {
            return UpsertResult.inserted(repositoryNode.getId(), "Repository", 1L, "test-op");
        }
        
        @Override
        public boolean createRelationship(String fromId, String toId, String relationshipType) {
            return true;
        }
        
        @Override
        public void clearAllData() {}
        
        @Override
        public java.util.Map<String, Object> getDataStatistics() {
            return new java.util.HashMap<>();
        }
        
        @Override
        public boolean createRelationship(String fromId, String toId, String relationshipType, java.util.Map<String, Object> properties) {
            return true;
        }
        
        @Override
        public List<UpsertResult> saveBatch(List<Object> nodes) {
            return nodes.stream()
                .map(node -> UpsertResult.inserted("test-id", "Test", 1L, "test-op"))
                .collect(java.util.stream.Collectors.toList());
        }
    }
}
