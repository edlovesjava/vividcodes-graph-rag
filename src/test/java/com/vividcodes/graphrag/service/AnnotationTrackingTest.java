package com.vividcodes.graphrag.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.vividcodes.graphrag.model.dto.UpsertResult;
import com.vividcodes.graphrag.model.graph.AnnotationNode;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.FieldNode;
import com.vividcodes.graphrag.model.graph.MethodNode;
import com.vividcodes.graphrag.model.graph.PackageNode;
import com.vividcodes.graphrag.model.graph.RepositoryNode;
import com.vividcodes.graphrag.model.graph.SubProjectNode;

/**
 * Dedicated test for annotation tracking functionality.
 * This test validates that annotations are properly parsed, processed, and relationships created.
 */
class AnnotationTrackingTest {

    private DependencyAnalyzer dependencyAnalyzer;
    private TrackingGraphService trackingGraphService;
    private RelationshipManager relationshipManager;
    private TypeResolver typeResolver;
    private NodeFactory nodeFactory;

    @TempDir
    java.nio.file.Path tempDir;

    @BeforeEach
    void setUp() {
        trackingGraphService = new TrackingGraphService();
        typeResolver = new TypeResolver();
        nodeFactory = new NodeFactory(typeResolver);
        relationshipManager = new RelationshipManager(trackingGraphService);
        dependencyAnalyzer = new DependencyAnalyzer(
            typeResolver, nodeFactory, relationshipManager, trackingGraphService);
    }

    @Test
    void shouldProcessMarkerAnnotation() {
        // Arrange
        String sourceCode = """
            package com.example;
            
            public class TestClass {
                @Override
                public void toString() {
                }
            }
            """;

        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> parseResult = parser.parse(sourceCode);
        assertTrue(parseResult.isSuccessful());
        
        CompilationUnit cu = parseResult.getResult().get();
        MarkerAnnotationExpr annotation = cu.findFirst(MarkerAnnotationExpr.class).get();
        
        MethodNode method = new MethodNode("toString", "TestClass", "com.example", "TestClass.java");
        Map<String, String> imports = new HashMap<>();

        // Act
        dependencyAnalyzer.processAnnotation(annotation, method, "method", imports);

        // Assert
        assertEquals(1, trackingGraphService.getAnnotationSaveCount());
        assertEquals(1, trackingGraphService.getRelationshipCount());
    }

    @Test
    void shouldProcessSingleMemberAnnotation() {
        // Arrange
        String sourceCode = """
            package com.example;
            
            public class TestClass {
                @SuppressWarnings("unchecked")
                private List list;
            }
            """;

        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> parseResult = parser.parse(sourceCode);
        assertTrue(parseResult.isSuccessful());
        
        CompilationUnit cu = parseResult.getResult().get();
        SingleMemberAnnotationExpr annotation = cu.findFirst(SingleMemberAnnotationExpr.class).get();
        
        FieldNode field = new FieldNode("list", "TestClass", "com.example", "TestClass.java");
        Map<String, String> imports = new HashMap<>();

        // Act
        dependencyAnalyzer.processAnnotation(annotation, field, "field", imports);

        // Assert
        assertEquals(1, trackingGraphService.getAnnotationSaveCount());
        assertEquals(1, trackingGraphService.getRelationshipCount());
    }

    @Test
    void shouldExtractAnnotationNameCorrectly() {
        // Arrange
        String sourceCode = """
            package com.example;
            import org.springframework.stereotype.Service;
            
            @Service
            public class TestService {
            }
            """;

        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> parseResult = parser.parse(sourceCode);
        assertTrue(parseResult.isSuccessful());
        
        CompilationUnit cu = parseResult.getResult().get();
        MarkerAnnotationExpr annotation = cu.findFirst(MarkerAnnotationExpr.class).get();
        
        ClassNode classNode = new ClassNode("TestService", "com.example", "TestService.java");
        Map<String, String> imports = Map.of("Service", "org.springframework.stereotype.Service");

        // Act
        dependencyAnalyzer.processAnnotation(annotation, classNode, "class", imports);

        // Assert
        AnnotationNode savedAnnotation = trackingGraphService.getLastSavedAnnotation();
        assertNotNull(savedAnnotation);
        assertEquals("Service", savedAnnotation.getName());
        assertEquals("org.springframework.stereotype.Service", savedAnnotation.getFullyQualifiedName());
        assertEquals("class", savedAnnotation.getTargetType());
        assertTrue(savedAnnotation.getFrameworkType() != null);
        assertEquals("Spring", savedAnnotation.getFrameworkType());
    }

    /**
     * Mock GraphService implementation for tracking calls during testing.
     */
    private static class TrackingGraphService implements GraphService {
        
        private final AtomicInteger annotationSaveCount = new AtomicInteger(0);
        private final AtomicInteger relationshipCount = new AtomicInteger(0);
        private AnnotationNode lastSavedAnnotation;
        
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
            annotationSaveCount.incrementAndGet();
            lastSavedAnnotation = annotationNode;
            return UpsertResult.inserted(annotationNode.getId(), "Annotation", 1L, "test-op");
        }
        
        @Override
        public UpsertResult saveRepository(RepositoryNode repositoryNode) {
            return UpsertResult.inserted(repositoryNode.getId(), "Repository", 1L, "test-op");
        }
        
        @Override
        public UpsertResult saveSubProject(SubProjectNode subProjectNode) {
            return UpsertResult.inserted(subProjectNode.getId(), "SubProject", 1L, "test-op");
        }
        
        @Override
        public boolean createRelationship(String fromId, String toId, String relationshipType) {
            relationshipCount.incrementAndGet();
            return true;
        }
        
        @Override
        public boolean createRelationship(String fromId, String toId, String relationshipType, 
                                     Map<String, Object> properties) {
            relationshipCount.incrementAndGet();
            return true;
        }
        
        @Override
        public List<UpsertResult> saveBatch(List<Object> nodes) {
            return nodes.stream()
                .map(node -> UpsertResult.inserted("test-id", "Test", 1L, "test-op"))
                .collect(java.util.stream.Collectors.toList());
        }
        
        @Override
        public void clearAllData() {
            // Mock implementation
        }
        
        @Override
        public Map<String, Object> getDataStatistics() {
            return new HashMap<>();
        }
        
        @Override
        public SubProjectNode findSubProjectById(String id) {
            return null;
        }
        
        @Override
        public java.util.List<SubProjectNode> findSubProjectsByRepositoryId(String repositoryId) {
            return new java.util.ArrayList<>();
        }
        
        @Override
        public void deleteSubProject(String id) {
            // Mock implementation
        }
        
        // Test helper methods
        public int getAnnotationSaveCount() {
            return annotationSaveCount.get();
        }
        
        public int getRelationshipCount() {
            return relationshipCount.get();
        }
        
        public AnnotationNode getLastSavedAnnotation() {
            return lastSavedAnnotation;
        }
    }
}
