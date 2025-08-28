package com.vividcodes.graphrag.model.dto;

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
 * Unit tests for SubProjectMetadata
 */
class SubProjectMetadataTest {
    
    private SubProjectMetadata metadata;
    
    @BeforeEach
    void setUp() {
        metadata = new SubProjectMetadata();
    }
    
    @Test
    void testDefaultConstructor() {
        SubProjectMetadata meta = new SubProjectMetadata();
        
        assertNotNull(meta.getDetectedAt());
        assertTrue(meta.getDetectedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
    
    @Test
    void testConstructorWithRequiredFields() {
        String name = "test-project";
        String path = "/path/to/project";
        String type = "maven";
        
        SubProjectMetadata meta = new SubProjectMetadata(name, path, type);
        
        assertEquals(name, meta.getName());
        assertEquals(path, meta.getPath());
        assertEquals(type, meta.getType());
        assertNotNull(meta.getId());
        assertNotNull(meta.getDetectedAt());
    }
    
    @Test
    void testConstructorWithAllBasicFields() {
        String name = "test-project";
        String path = "/path/to/project";
        String type = "gradle";
        String repositoryId = "repo-123";
        
        SubProjectMetadata meta = new SubProjectMetadata(name, path, type, repositoryId);
        
        assertEquals(name, meta.getName());
        assertEquals(path, meta.getPath());
        assertEquals(type, meta.getType());
        assertEquals(repositoryId, meta.getRepositoryId());
        assertNotNull(meta.getId());
    }
    
    @Test
    void testIdGeneration() {
        String path = "/src/main/java/project";
        String type = "maven";
        
        SubProjectMetadata meta = new SubProjectMetadata("test", path, type);
        String id = meta.getId();
        
        assertNotNull(id);
        assertTrue(id.contains("maven"));
        assertTrue(id.contains("_src_main_java_project_")); // Path should be normalized
    }
    
    @Test
    void testIdRegenerationOnPathChange() {
        metadata.setName("test");
        metadata.setPath("/old/path");
        metadata.setType("maven");
        
        String oldId = metadata.getId();
        
        metadata.setPath("/new/path");
        String newId = metadata.getId();
        
        assertNotEquals(oldId, newId);
        assertTrue(newId.contains("new_path"));
    }
    
    @Test
    void testIdRegenerationOnTypeChange() {
        metadata.setName("test");
        metadata.setPath("/path/to/project");
        metadata.setType("maven");
        
        String oldId = metadata.getId();
        
        metadata.setType("gradle");
        String newId = metadata.getId();
        
        assertNotEquals(oldId, newId);
        assertTrue(newId.contains("gradle"));
    }
    
    @Test
    void testBasicProperties() {
        String name = "My Project";
        String path = "/src/main/java/project";
        String type = "npm";
        String buildFile = "package.json";
        String description = "A test project";
        String version = "2.1.0";
        String repositoryId = "repo-456";
        
        metadata.setName(name);
        metadata.setPath(path);
        metadata.setType(type);
        metadata.setBuildFile(buildFile);
        metadata.setDescription(description);
        metadata.setVersion(version);
        metadata.setRepositoryId(repositoryId);
        
        assertEquals(name, metadata.getName());
        assertEquals(path, metadata.getPath());
        assertEquals(type, metadata.getType());
        assertEquals(buildFile, metadata.getBuildFile());
        assertEquals(description, metadata.getDescription());
        assertEquals(version, metadata.getVersion());
        assertEquals(repositoryId, metadata.getRepositoryId());
    }
    
    @Test
    void testListProperties() {
        List<String> sourceDirectories = Arrays.asList("src", "lib");
        List<String> testDirectories = Arrays.asList("test", "spec");
        List<String> dependencies = Arrays.asList("lodash", "express", "jest");
        
        metadata.setSourceDirectories(sourceDirectories);
        metadata.setTestDirectories(testDirectories);
        metadata.setDependencies(dependencies);
        
        assertEquals(sourceDirectories, metadata.getSourceDirectories());
        assertEquals(testDirectories, metadata.getTestDirectories());
        assertEquals(dependencies, metadata.getDependencies());
    }
    
    @Test
    void testIsValid() {
        // Invalid cases
        assertFalse(metadata.isValid()); // all null
        
        metadata.setName("test");
        assertFalse(metadata.isValid()); // missing path and type
        
        metadata.setPath("/path");
        assertFalse(metadata.isValid()); // missing type
        
        metadata.setType("maven");
        assertTrue(metadata.isValid()); // all required fields present
        
        // Test with empty strings
        metadata.setName("");
        assertFalse(metadata.isValid());
        
        metadata.setName("  ");
        assertFalse(metadata.isValid());
        
        metadata.setName("valid");
        metadata.setPath("");
        assertFalse(metadata.isValid());
        
        metadata.setPath("/valid/path");
        metadata.setType("");
        assertFalse(metadata.isValid());
    }
    
    @Test
    void testProjectTypeCheckers() {
        // Test Maven project
        metadata.setType("maven");
        assertTrue(metadata.isMavenProject());
        assertFalse(metadata.isGradleProject());
        assertFalse(metadata.isNpmProject());
        
        metadata.setType("Maven"); // case insensitive
        assertTrue(metadata.isMavenProject());
        
        metadata.setBuildFile("pom.xml");
        metadata.setType("custom");
        assertTrue(metadata.isMavenProject()); // detected by build file
        
        // Test Gradle project
        metadata.setType("gradle");
        metadata.setBuildFile(null);
        assertTrue(metadata.isGradleProject());
        assertFalse(metadata.isMavenProject());
        assertFalse(metadata.isNpmProject());
        
        metadata.setType("Gradle"); // case insensitive
        assertTrue(metadata.isGradleProject());
        
        metadata.setBuildFile("build.gradle");
        metadata.setType("custom");
        assertTrue(metadata.isGradleProject()); // detected by build file
        
        metadata.setBuildFile("build.gradle.kts");
        assertTrue(metadata.isGradleProject()); // Kotlin DSL
        
        // Test NPM project
        metadata.setType("npm");
        metadata.setBuildFile(null);
        assertTrue(metadata.isNpmProject());
        assertFalse(metadata.isMavenProject());
        assertFalse(metadata.isGradleProject());
        
        metadata.setType("NPM"); // case insensitive
        assertTrue(metadata.isNpmProject());
        
        metadata.setBuildFile("package.json");
        metadata.setType("custom");
        assertTrue(metadata.isNpmProject()); // detected by build file
    }
    
    @Test
    void testGetRelativePath() {
        // Test null path
        assertNull(metadata.getRelativePath());
        
        // Test regular path
        metadata.setPath("src/main/java");
        assertEquals("src/main/java", metadata.getRelativePath());
        
        // Test path with leading slashes
        metadata.setPath("/src/main/java");
        assertEquals("src/main/java", metadata.getRelativePath());
        
        metadata.setPath("\\src\\main\\java");
        assertEquals("src/main/java", metadata.getRelativePath());
        
        metadata.setPath("//src//main//java");
        assertEquals("src/main/java", metadata.getRelativePath());
        
        // Test path normalization
        metadata.setPath("/src\\main/java");
        assertEquals("src/main/java", metadata.getRelativePath());
    }
    
    @Test
    void testToString() {
        metadata.setName("test-project");
        metadata.setPath("/test/path");
        metadata.setType("maven");
        metadata.setBuildFile("pom.xml");
        metadata.setVersion("1.0.0");
        metadata.setRepositoryId("repo-123");
        
        String toString = metadata.toString();
        
        assertTrue(toString.contains("test-project"));
        assertTrue(toString.contains("/test/path"));
        assertTrue(toString.contains("maven"));
        assertTrue(toString.contains("pom.xml"));
        assertTrue(toString.contains("1.0.0"));
        assertTrue(toString.contains("repo-123"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        String name = "test";
        String path = "/path";
        String type = "maven";
        
        SubProjectMetadata meta1 = new SubProjectMetadata(name, path, type);
        SubProjectMetadata meta2 = new SubProjectMetadata(name, path, type);
        SubProjectMetadata meta3 = new SubProjectMetadata("different", path, type);
        
        SubProjectMetadata metaWithNullId = new SubProjectMetadata();
        
        // IDs should be different due to timestamp
        assertNotEquals(meta1.getId(), meta2.getId());
        
        // Set same ID for equality test
        String commonId = "test-id";
        meta1.setId(commonId);
        meta2.setId(commonId);
        
        // Test equals
        assertTrue(meta1.equals(meta2));
        assertFalse(meta1.equals(meta3));
        assertFalse(meta1.equals(metaWithNullId));
        assertTrue(meta1.equals(meta1)); // self-equality
        assertFalse(meta1.equals(null));
        assertFalse(meta1.equals("not a SubProjectMetadata"));
        
        // Test hashCode consistency
        assertEquals(meta1.hashCode(), meta2.hashCode());
    }
    
    @Test
    void testTimestampProperties() {
        LocalDateTime detectedAt = LocalDateTime.now().minusMinutes(5);
        
        metadata.setDetectedAt(detectedAt);
        assertEquals(detectedAt, metadata.getDetectedAt());
    }
    
    @Test
    void testNullSafety() {
        // Test that null values don't cause issues
        metadata.setName(null);
        metadata.setPath(null);
        metadata.setType(null);
        metadata.setBuildFile(null);
        metadata.setDescription(null);
        metadata.setVersion(null);
        metadata.setRepositoryId(null);
        metadata.setSourceDirectories(null);
        metadata.setTestDirectories(null);
        metadata.setDependencies(null);
        
        assertFalse(metadata.isValid());
        assertFalse(metadata.isMavenProject());
        assertFalse(metadata.isGradleProject());
        assertFalse(metadata.isNpmProject());
        assertNull(metadata.getRelativePath());
        
        // toString should still work
        assertDoesNotThrow(() -> metadata.toString());
    }
}
