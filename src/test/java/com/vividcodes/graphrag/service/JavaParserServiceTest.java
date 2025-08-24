package com.vividcodes.graphrag.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.vividcodes.graphrag.config.ParserConfig;

class JavaParserServiceTest {
    
    private GraphService graphService;
    private RepositoryService repositoryService;
    private ParserConfig parserConfig;
    private JavaParserService javaParserService;
    
    @BeforeEach
    void setUp() {
        // Use a simple mock implementation instead of Mockito
        graphService = new SimpleMockGraphService();
        repositoryService = new SimpleMockRepositoryService();
        
        parserConfig = new ParserConfig();
        ReflectionTestUtils.setField(parserConfig, "includePrivate", false);
        ReflectionTestUtils.setField(parserConfig, "includeTests", false);
        ReflectionTestUtils.setField(parserConfig, "maxFileSize", 10 * 1024 * 1024L);
        
        javaParserService = new JavaParserService(parserConfig, graphService, repositoryService);
    }
    
    @Test
    void testParseDirectory_ValidPath() {
        // Get the test resources directory
        Path testResourcesPath = Paths.get("src", "test", "resources", "test-data");
        
        // This test will verify that the service can handle a valid directory
        assertDoesNotThrow(() -> {
            javaParserService.parseDirectory(testResourcesPath.toString());
        });
    }
    
    @Test
    void testParseDirectory_InvalidPath() {
        String invalidPath = "/non/existent/path";
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            javaParserService.parseDirectory(invalidPath);
        });
        
        assertTrue(exception.getMessage().contains("Source path does not exist") || 
                  exception.getCause().getMessage().contains("Source path does not exist"));
    }
    
    @Test
    void testParserConfig() {
        assertFalse(parserConfig.isIncludePrivate());
        assertFalse(parserConfig.isIncludeTests());
        assertEquals(10 * 1024 * 1024L, parserConfig.getMaxFileSize());
        assertEquals("java", parserConfig.getSupportedExtensions());
    }
    
    @Test
    void testShouldIncludeFile_NullPath() {
        // Test that null path is handled gracefully
        assertFalse(javaParserService.shouldIncludeFile(null));
    }
    
    @Test
    void testShouldIncludeFile_PathWithNullFileName() {
        // Test with a real path that might have null fileName
        // This is a more realistic test that doesn't require mocking
        Path testPath = Paths.get("src", "test", "resources", "test-data");
        // The test passes if the method handles the path without throwing NPE
        assertDoesNotThrow(() -> {
            javaParserService.shouldIncludeFile(testPath);
        });
    }
    
    /**
     * Simple mock implementation of GraphService for testing.
     * This avoids the Mockito ByteBuddy issues with Java 17.
     */
    private static class SimpleMockGraphService implements GraphService {
        
        @Override
        public void savePackage(com.vividcodes.graphrag.model.graph.PackageNode packageNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void saveClass(com.vividcodes.graphrag.model.graph.ClassNode classNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void saveMethod(com.vividcodes.graphrag.model.graph.MethodNode methodNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void saveField(com.vividcodes.graphrag.model.graph.FieldNode fieldNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void saveRepository(com.vividcodes.graphrag.model.graph.RepositoryNode repositoryNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void createRelationship(String fromId, String toId, String relationshipType) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void createRelationship(String fromId, String toId, String relationshipType, 
                                    java.util.Map<String, Object> properties) {
            // Mock implementation - do nothing
        }
    }
    
    /**
     * Simple mock implementation of RepositoryService for testing.
     */
    private static class SimpleMockRepositoryService extends RepositoryService {
        
        public SimpleMockRepositoryService() {
            super(null, null); // Pass null dependencies for testing
        }
        
        @Override
        public com.vividcodes.graphrag.model.dto.RepositoryMetadata detectRepositoryMetadata(java.nio.file.Path filePath) {
            // Mock implementation - return null
            return null;
        }
        
        @Override
        public com.vividcodes.graphrag.model.graph.RepositoryNode createOrUpdateRepository(com.vividcodes.graphrag.model.dto.RepositoryMetadata metadata) {
            // Mock implementation - return null
            return null;
        }
        
        @Override
        public void linkNodesToRepository(java.util.List<Object> nodes, com.vividcodes.graphrag.model.graph.RepositoryNode repository) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void clearCache() {
            // Mock implementation - do nothing
        }
        
        @Override
        public java.util.Map<String, Object> getRepositoryStats() {
            // Mock implementation - return empty map
            return new java.util.HashMap<>();
        }
    }
} 