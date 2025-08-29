package com.vividcodes.graphrag.model.graph;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for SubProjectNode
 */
class SubProjectNodeTest {
    
    private SubProjectNode subProjectNode;
    
    @BeforeEach
    void setUp() {
        subProjectNode = new SubProjectNode();
    }
    
    @Test
    void testDefaultConstructor() {
        SubProjectNode node = new SubProjectNode();
        
        assertNotNull(node.getCreatedAt());
        assertNotNull(node.getUpdatedAt());
        assertTrue(node.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(node.getUpdatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
    
    @Test
    void testConstructorWithBasicFields() {
        String id = "test-id";
        String name = "test-project";
        String path = "/path/to/project";
        String type = "maven";
        
        SubProjectNode node = new SubProjectNode(id, name, path, type);
        
        assertEquals(id, node.getId());
        assertEquals(name, node.getName());
        assertEquals(path, node.getPath());
        assertEquals(type, node.getType());
        assertNotNull(node.getCreatedAt());
        assertNotNull(node.getUpdatedAt());
    }
    
    @Test
    void testSettersUpdateTimestamp() {
        LocalDateTime initialUpdatedAt = subProjectNode.getUpdatedAt();
        
        // Wait a small amount to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        subProjectNode.setName("new-name");
        assertTrue(subProjectNode.getUpdatedAt().isAfter(initialUpdatedAt));
        
        subProjectNode.setPath("/new/path");
        assertTrue(subProjectNode.getUpdatedAt().isAfter(initialUpdatedAt));
        
        subProjectNode.setType("gradle");
        assertTrue(subProjectNode.getUpdatedAt().isAfter(initialUpdatedAt));
    }
    
    @Test
    void testBasicProperties() {
        String id = "project-123";
        String name = "My Project";
        String path = "/src/main/java/project";
        String type = "maven";
        String buildFile = "pom.xml";
        String description = "A test project";
        String version = "1.0.0";
        String repositoryId = "repo-456";
        
        subProjectNode.setId(id);
        subProjectNode.setName(name);
        subProjectNode.setPath(path);
        subProjectNode.setType(type);
        subProjectNode.setBuildFile(buildFile);
        subProjectNode.setDescription(description);
        subProjectNode.setVersion(version);
        subProjectNode.setRepositoryId(repositoryId);
        
        assertEquals(id, subProjectNode.getId());
        assertEquals(name, subProjectNode.getName());
        assertEquals(path, subProjectNode.getPath());
        assertEquals(type, subProjectNode.getType());
        assertEquals(buildFile, subProjectNode.getBuildFile());
        assertEquals(description, subProjectNode.getDescription());
        assertEquals(version, subProjectNode.getVersion());
        assertEquals(repositoryId, subProjectNode.getRepositoryId());
    }
    
    @Test
    void testListProperties() {
        List<String> sourceDirectories = Arrays.asList("src/main/java", "src/main/resources");
        List<String> testDirectories = Arrays.asList("src/test/java", "src/test/resources");
        List<String> dependencies = Arrays.asList("junit", "mockito", "spring-boot");
        
        subProjectNode.setSourceDirectories(sourceDirectories);
        subProjectNode.setTestDirectories(testDirectories);
        subProjectNode.setDependencies(dependencies);
        
        assertEquals(sourceDirectories, subProjectNode.getSourceDirectories());
        assertEquals(testDirectories, subProjectNode.getTestDirectories());
        assertEquals(dependencies, subProjectNode.getDependencies());
    }
    
    @Test
    void testScoreProperties() {
        Float healthScore = 85.5f;
        Float complexityScore = 42.3f;
        Float maintainabilityScore = 78.9f;
        
        subProjectNode.setHealthScore(healthScore);
        subProjectNode.setComplexityScore(complexityScore);
        subProjectNode.setMaintainabilityScore(maintainabilityScore);
        
        assertEquals(healthScore, subProjectNode.getHealthScore());
        assertEquals(complexityScore, subProjectNode.getComplexityScore());
        assertEquals(maintainabilityScore, subProjectNode.getMaintainabilityScore());
    }
    
    @Test
    void testTimestampProperties() {
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        
        subProjectNode.setCreatedAt(createdAt);
        subProjectNode.setUpdatedAt(updatedAt);
        
        assertEquals(createdAt, subProjectNode.getCreatedAt());
        assertEquals(updatedAt, subProjectNode.getUpdatedAt());
    }
    
    @Test
    void testToString() {
        subProjectNode.setId("test-id");
        subProjectNode.setName("test-name");
        subProjectNode.setPath("/test/path");
        subProjectNode.setType("maven");
        subProjectNode.setBuildFile("pom.xml");
        subProjectNode.setVersion("1.0.0");
        subProjectNode.setRepositoryId("repo-123");
        
        String toString = subProjectNode.toString();
        
        assertTrue(toString.contains("test-id"));
        assertTrue(toString.contains("test-name"));
        assertTrue(toString.contains("/test/path"));
        assertTrue(toString.contains("maven"));
        assertTrue(toString.contains("pom.xml"));
        assertTrue(toString.contains("1.0.0"));
        assertTrue(toString.contains("repo-123"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        String id = "test-id";
        
        SubProjectNode node1 = new SubProjectNode();
        node1.setId(id);
        
        SubProjectNode node2 = new SubProjectNode();
        node2.setId(id);
        
        SubProjectNode node3 = new SubProjectNode();
        node3.setId("different-id");
        
        SubProjectNode nodeWithNullId = new SubProjectNode();
        
        // Test equals
        assertTrue(node1.equals(node2));
        assertFalse(node1.equals(node3));
        assertFalse(node1.equals(nodeWithNullId));
        assertFalse(nodeWithNullId.equals(node1));
        assertTrue(node1.equals(node1)); // self-equality
        assertFalse(node1.equals(null));
        assertFalse(node1.equals("not a SubProjectNode"));
        
        // Test hashCode consistency
        assertEquals(node1.hashCode(), node2.hashCode());
        assertNotEquals(node1.hashCode(), node3.hashCode());
    }
    
    @Test
    void testEqualsWithNullIds() {
        SubProjectNode node1 = new SubProjectNode();
        SubProjectNode node2 = new SubProjectNode();
        
        // Both have null IDs
        assertFalse(node1.equals(node2));
        assertEquals(0, node1.hashCode());
        assertEquals(0, node2.hashCode());
    }
    
    @Test
    void testNullSafetyForOptionalFields() {
        // Test that setting null values doesn't cause issues
        subProjectNode.setBuildFile(null);
        subProjectNode.setDescription(null);
        subProjectNode.setVersion(null);
        subProjectNode.setRepositoryId(null);
        subProjectNode.setSourceDirectories(null);
        subProjectNode.setTestDirectories(null);
        subProjectNode.setDependencies(null);
        subProjectNode.setHealthScore(null);
        subProjectNode.setComplexityScore(null);
        subProjectNode.setMaintainabilityScore(null);
        
        assertNull(subProjectNode.getBuildFile());
        assertNull(subProjectNode.getDescription());
        assertNull(subProjectNode.getVersion());
        assertNull(subProjectNode.getRepositoryId());
        assertNull(subProjectNode.getSourceDirectories());
        assertNull(subProjectNode.getTestDirectories());
        assertNull(subProjectNode.getDependencies());
        assertNull(subProjectNode.getHealthScore());
        assertNull(subProjectNode.getComplexityScore());
        assertNull(subProjectNode.getMaintainabilityScore());
        
        // toString should still work
        assertDoesNotThrow(() -> subProjectNode.toString());
    }
}
