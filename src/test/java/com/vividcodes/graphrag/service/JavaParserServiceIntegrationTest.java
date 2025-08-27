package com.vividcodes.graphrag.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import com.vividcodes.graphrag.config.ParserConfig;

class JavaParserServiceIntegrationTest {
    
    private GraphService graphService;
    private ParserConfig parserConfig;
    private JavaParserService javaParserService;
    
    @BeforeEach
    void setUp() {
        // Use a tracking mock implementation instead of Mockito
        graphService = new TrackingMockGraphService();
        
        parserConfig = new ParserConfig();
        ReflectionTestUtils.setField(parserConfig, "includePrivate", false);
        ReflectionTestUtils.setField(parserConfig, "includeTests", false);
        ReflectionTestUtils.setField(parserConfig, "maxFileSize", 10 * 1024 * 1024L);
        
        javaParserService = new JavaParserService(parserConfig, graphService, new SimpleMockRepositoryService());
    }
    
    @Test
    void testParseSimpleJavaFile(@TempDir Path tempDir) throws Exception {
        // Create a simple Java file for testing
        Path testFile = tempDir.resolve("SimpleClass.java");
        String javaCode = """
            package test;
            
            public class SimpleClass {
                public void testMethod() {
                    System.out.println("Hello World");
                }
            }
            """;
        
        System.out.println("Creating file at: " + testFile.toAbsolutePath());
        Files.write(testFile, javaCode.getBytes());
        
        // Verify file was created
        assertTrue(Files.exists(testFile), "Test file should exist");
        System.out.println("File exists: " + Files.exists(testFile));
        System.out.println("File size: " + Files.size(testFile));
        
        // Parse the file
        javaParserService.parseDirectory(tempDir.toString());
        
        // Verify that the GraphService was called using our tracking mock
        TrackingMockGraphService trackingService = (TrackingMockGraphService) graphService;
        assertTrue(trackingService.getSaveClassCount() > 0, "GraphService.saveClass should have been called");
    }
    
    /**
     * Tracking mock implementation of GraphService for integration testing.
     * This avoids the Mockito ByteBuddy issues with Java 17.
     */
    private static class TrackingMockGraphService implements GraphService {
        
        private final AtomicInteger saveClassCount = new AtomicInteger(0);
        private final AtomicInteger saveMethodCount = new AtomicInteger(0);
        private final AtomicInteger saveFieldCount = new AtomicInteger(0);
        private final AtomicInteger savePackageCount = new AtomicInteger(0);
        private final AtomicInteger createRelationshipCount = new AtomicInteger(0);
        
        public int getSaveClassCount() {
            return saveClassCount.get();
        }
        
        public int getSaveMethodCount() {
            return saveMethodCount.get();
        }
        
        public int getSaveFieldCount() {
            return saveFieldCount.get();
        }
        
        public int getSavePackageCount() {
            return savePackageCount.get();
        }
        
        public int getCreateRelationshipCount() {
            return createRelationshipCount.get();
        }
        
        @Override
        public void savePackage(com.vividcodes.graphrag.model.graph.PackageNode packageNode) {
            savePackageCount.incrementAndGet();
        }
        
        @Override
        public void saveClass(com.vividcodes.graphrag.model.graph.ClassNode classNode) {
            saveClassCount.incrementAndGet();
        }
        
        @Override
        public void saveMethod(com.vividcodes.graphrag.model.graph.MethodNode methodNode) {
            saveMethodCount.incrementAndGet();
        }
        
        @Override
        public void saveField(com.vividcodes.graphrag.model.graph.FieldNode fieldNode) {
            saveFieldCount.incrementAndGet();
        }
        
        @Override
        public void createRelationship(String fromId, String toId, String relationshipType) {
            createRelationshipCount.incrementAndGet();
        }
        
        @Override
        public void createRelationship(String fromId, String toId, String relationshipType, 
                                    java.util.Map<String, Object> properties) {
            createRelationshipCount.incrementAndGet();
        }
        
        @Override
        public void saveRepository(com.vividcodes.graphrag.model.graph.RepositoryNode repositoryNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void clearAllData() {
            // Mock implementation - do nothing
        }
        
        @Override
        public java.util.Map<String, Object> getDataStatistics() {
            // Mock implementation - return empty map
            return new java.util.HashMap<>();
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