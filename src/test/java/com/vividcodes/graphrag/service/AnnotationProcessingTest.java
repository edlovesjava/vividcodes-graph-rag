package com.vividcodes.graphrag.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.vividcodes.graphrag.model.graph.AnnotationNode;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.FieldNode;
import com.vividcodes.graphrag.model.graph.MethodNode;
import com.vividcodes.graphrag.model.graph.PackageNode;
import com.vividcodes.graphrag.model.graph.RepositoryNode;
import com.vividcodes.graphrag.model.graph.SubProjectNode;

/**
 * Unit test for annotation processing functionality.
 * Tests the annotation tracking logic in isolation.
 */
class AnnotationProcessingTest {

    private DependencyAnalyzer dependencyAnalyzer;
    private TrackingGraphService trackingGraphService;
    private RelationshipManager relationshipManager;
    private NodeFactory nodeFactory;

    @BeforeEach
    void setUp() {
        trackingGraphService = new TrackingGraphService();
        TypeResolver typeResolver = new TypeResolver();
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
                public String toString() {
                    return "test";
                }
            }
            """;

        CompilationUnit cu = parseCode(sourceCode);
        AnnotationExpr annotation = cu.findAll(AnnotationExpr.class).get(0);
        MethodNode method = createMockMethod("toString");
        Map<String, String> imports = new HashMap<>();

        // Act
        dependencyAnalyzer.processAnnotation(annotation, method, "method", imports);

        // Assert
        assertEquals(1, trackingGraphService.getAnnotationSaveCount());
        assertEquals(1, trackingGraphService.getRelationshipCount());
        
        AnnotationNode savedAnnotation = trackingGraphService.getSavedAnnotations().get(0);
        assertEquals("Override", savedAnnotation.getName());
        assertEquals("java.lang.Override", savedAnnotation.getFullyQualifiedName());
        assertEquals("method", savedAnnotation.getTargetType());
        assertTrue(savedAnnotation.getIsFramework());
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

        CompilationUnit cu = parseCode(sourceCode);
        AnnotationExpr annotation = cu.findAll(AnnotationExpr.class).get(0);
        FieldNode field = createMockField("list");
        Map<String, String> imports = new HashMap<>();

        // Act
        dependencyAnalyzer.processAnnotation(annotation, field, "field", imports);

        // Assert
        assertEquals(1, trackingGraphService.getAnnotationSaveCount());
        assertEquals(1, trackingGraphService.getRelationshipCount());
        
        AnnotationNode savedAnnotation = trackingGraphService.getSavedAnnotations().get(0);
        assertEquals("SuppressWarnings", savedAnnotation.getName());
        assertEquals("java.lang.SuppressWarnings", savedAnnotation.getFullyQualifiedName());
        assertEquals("field", savedAnnotation.getTargetType());
        assertTrue(savedAnnotation.getAttributes().containsKey("value"));
        assertEquals("unchecked", savedAnnotation.getAttributes().get("value"));
    }

    @Test
    void shouldProcessSpringServiceAnnotation() {
        // Arrange
        String sourceCode = """
            package com.example;
            
            import org.springframework.stereotype.Service;
            
            @Service
            public class UserService {
            }
            """;

        CompilationUnit cu = parseCode(sourceCode);
        AnnotationExpr annotation = cu.findAll(AnnotationExpr.class).get(0);
        ClassNode clazz = createMockClass("UserService");
        Map<String, String> imports = Map.of("Service", "org.springframework.stereotype.Service");

        // Act
        dependencyAnalyzer.processAnnotation(annotation, clazz, "class", imports);

        // Assert
        assertEquals(1, trackingGraphService.getAnnotationSaveCount());
        assertEquals(1, trackingGraphService.getRelationshipCount());
        
        AnnotationNode savedAnnotation = trackingGraphService.getSavedAnnotations().get(0);
        assertEquals("Service", savedAnnotation.getName());
        assertEquals("org.springframework.stereotype.Service", savedAnnotation.getFullyQualifiedName());
        assertEquals("class", savedAnnotation.getTargetType());
        assertTrue(savedAnnotation.getIsFramework());
        assertEquals("spring", savedAnnotation.getFrameworkType());
    }

    @Test
    void shouldProcessJUnitTestAnnotation() {
        // Arrange
        String sourceCode = """
            package com.example;
            
            import org.junit.jupiter.api.Test;
            
            public class UserServiceTest {
                @Test
                void shouldCreateUser() {
                    // test code
                }
            }
            """;

        CompilationUnit cu = parseCode(sourceCode);
        AnnotationExpr annotation = cu.findAll(AnnotationExpr.class).get(0);
        MethodNode method = createMockMethod("shouldCreateUser");
        Map<String, String> imports = Map.of("Test", "org.junit.jupiter.api.Test");

        // Act
        dependencyAnalyzer.processAnnotation(annotation, method, "method", imports);

        // Assert
        assertEquals(1, trackingGraphService.getAnnotationSaveCount());
        assertEquals(1, trackingGraphService.getRelationshipCount());
        
        AnnotationNode savedAnnotation = trackingGraphService.getSavedAnnotations().get(0);
        assertEquals("Test", savedAnnotation.getName());
        assertEquals("org.junit.jupiter.api.Test", savedAnnotation.getFullyQualifiedName());
        assertEquals("method", savedAnnotation.getTargetType());
        assertTrue(savedAnnotation.getIsFramework());
        assertEquals("junit", savedAnnotation.getFrameworkType());
    }

    private CompilationUnit parseCode(String sourceCode) {
        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> result = parser.parse(sourceCode);
        assertTrue(result.isSuccessful());
        return result.getResult().get();
    }

    private ClassNode createMockClass(String name) {
        ClassNode clazz = new ClassNode();
        clazz.setName(name);
        clazz.setId("class-" + name.toLowerCase());
        clazz.setPackageName("com.example");
        return clazz;
    }

    private MethodNode createMockMethod(String name) {
        MethodNode method = new MethodNode();
        method.setName(name);
        method.setId("method-" + name.toLowerCase());
        method.setClassName("TestClass");
        return method;
    }

    private FieldNode createMockField(String name) {
        FieldNode field = new FieldNode();
        field.setName(name);
        field.setId("field-" + name.toLowerCase());
        field.setClassName("TestClass");
        return field;
    }

    /**
     * Tracking GraphService implementation for testing.
     */
    private static class TrackingGraphService implements GraphService {
        
        private final AtomicInteger annotationSaveCount = new AtomicInteger(0);
        private final AtomicInteger relationshipCount = new AtomicInteger(0);
        private final java.util.List<AnnotationNode> savedAnnotations = new java.util.ArrayList<>();
        
        @Override
        public void savePackage(PackageNode packageNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void saveClass(ClassNode classNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void saveMethod(MethodNode methodNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void saveField(FieldNode fieldNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void saveAnnotation(AnnotationNode annotationNode) {
            annotationSaveCount.incrementAndGet();
            savedAnnotations.add(annotationNode);
        }
        
        @Override
        public void saveRepository(RepositoryNode repositoryNode) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void saveSubProject(SubProjectNode subProjectNode) {
            // Mock implementation - do nothing
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
            // Mock implementation - do nothing
        }
        
        @Override
        public void createRelationship(String fromId, String toId, String relationshipType) {
            relationshipCount.incrementAndGet();
        }
        
        @Override
        public void createRelationship(String fromId, String toId, String relationshipType, 
                                      Map<String, Object> properties) {
            relationshipCount.incrementAndGet();
        }
        
        @Override
        public void clearAllData() {
            // Mock implementation - do nothing
        }
        
        @Override
        public Map<String, Object> getDataStatistics() {
            return Map.of();
        }
        
        public int getAnnotationSaveCount() {
            return annotationSaveCount.get();
        }
        
        public int getRelationshipCount() {
            return relationshipCount.get();
        }
        
        public java.util.List<AnnotationNode> getSavedAnnotations() {
            return new java.util.ArrayList<>(savedAnnotations);
        }
    }
}
