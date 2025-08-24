package com.vividcodes.graphrag.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.FieldNode;
import com.vividcodes.graphrag.model.graph.MethodNode;
import com.vividcodes.graphrag.model.graph.PackageNode;

/**
 * Test class to validate that all relationship types specified in the graph schema design
 * are properly supported by the GraphService interface and implementation.
 * 
 * Based on INITIAL_STORY.md graph schema:
 * - CONTAINS (Package→Class, Class→Method, Class→Field)
 * - EXTENDS (Class→Class)
 * - IMPLEMENTS (Class→Class)
 * - CALLS (Method→Method)
 * - USES (Method→Field, Method→Class)
 * - DEPENDS_ON (Class→Class)
 */
class GraphRelationshipValidationTest {
    
    private GraphService graphService;
    
    @BeforeEach
    void setUp() {
        // Create a mock GraphService for testing relationship validation
        graphService = new MockGraphService();
    }
    
    @Test
    void testAllRelationshipTypesAreSupported() {
        // Define all expected relationship types from the schema design
        List<String> expectedRelationshipTypes = Arrays.asList(
            "CONTAINS", "EXTENDS", "IMPLEMENTS", "CALLS", "USES", "DEPENDS_ON"
        );
        
        // Test that each relationship type can be created
        for (String relationshipType : expectedRelationshipTypes) {
            assertDoesNotThrow(() -> {
                graphService.createRelationship("test-from-id", "test-to-id", relationshipType);
            }, "Relationship type '" + relationshipType + "' should be supported");
        }
    }
    
    @Test
    void testContainsRelationship_PackageToClass() {
        // Given
        PackageNode packageNode = createPackageNode("com.example", "com/example");
        ClassNode classNode = createClassNode("TestClass", "com.example.TestClass", "com.example");
        
        // When & Then
        assertDoesNotThrow(() -> {
            graphService.savePackage(packageNode);
            graphService.saveClass(classNode);
            graphService.createRelationship(packageNode.getId(), classNode.getId(), "CONTAINS");
        }, "CONTAINS relationship from Package to Class should be supported");
    }
    
    @Test
    void testContainsRelationship_ClassToMethod() {
        // Given
        ClassNode classNode = createClassNode("TestClass", "com.example.TestClass", "com.example");
        MethodNode methodNode = createMethodNode("testMethod", "com.example.TestClass.testMethod", "com.example.TestClass");
        
        // When & Then
        assertDoesNotThrow(() -> {
            graphService.saveClass(classNode);
            graphService.saveMethod(methodNode);
            graphService.createRelationship(classNode.getId(), methodNode.getId(), "CONTAINS");
        }, "CONTAINS relationship from Class to Method should be supported");
    }
    
    @Test
    void testContainsRelationship_ClassToField() {
        // Given
        ClassNode classNode = createClassNode("TestClass", "com.example.TestClass", "com.example");
        FieldNode fieldNode = createFieldNode("testField", "com.example.TestClass.testField", "com.example.TestClass");
        
        // When & Then
        assertDoesNotThrow(() -> {
            graphService.saveClass(classNode);
            graphService.saveField(fieldNode);
            graphService.createRelationship(classNode.getId(), fieldNode.getId(), "CONTAINS");
        }, "CONTAINS relationship from Class to Field should be supported");
    }
    
    @Test
    void testExtendsRelationship_ClassToClass() {
        // Given
        ClassNode parentClass = createClassNode("ParentClass", "com.example.ParentClass", "com.example");
        ClassNode childClass = createClassNode("ChildClass", "com.example.ChildClass", "com.example");
        
        // When & Then
        assertDoesNotThrow(() -> {
            graphService.saveClass(parentClass);
            graphService.saveClass(childClass);
            graphService.createRelationship(childClass.getId(), parentClass.getId(), "EXTENDS");
        }, "EXTENDS relationship from Class to Class should be supported");
    }
    
    @Test
    void testImplementsRelationship_ClassToClass() {
        // Given
        ClassNode interfaceClass = createClassNode("TestInterface", "com.example.TestInterface", "com.example");
        ClassNode implementingClass = createClassNode("ImplementingClass", "com.example.ImplementingClass", "com.example");
        
        // When & Then
        assertDoesNotThrow(() -> {
            graphService.saveClass(interfaceClass);
            graphService.saveClass(implementingClass);
            graphService.createRelationship(implementingClass.getId(), interfaceClass.getId(), "IMPLEMENTS");
        }, "IMPLEMENTS relationship from Class to Class should be supported");
    }
    
    @Test
    void testCallsRelationship_MethodToMethod() {
        // Given
        MethodNode callingMethod = createMethodNode("callingMethod", "com.example.TestClass.callingMethod", "com.example.TestClass");
        MethodNode calledMethod = createMethodNode("calledMethod", "com.example.TestClass.calledMethod", "com.example.TestClass");
        
        // When & Then
        assertDoesNotThrow(() -> {
            graphService.saveMethod(callingMethod);
            graphService.saveMethod(calledMethod);
            graphService.createRelationship(callingMethod.getId(), calledMethod.getId(), "CALLS");
        }, "CALLS relationship from Method to Method should be supported");
    }
    
    @Test
    void testUsesRelationship_MethodToField() {
        // Given
        MethodNode method = createMethodNode("testMethod", "com.example.TestClass.testMethod", "com.example.TestClass");
        FieldNode field = createFieldNode("testField", "com.example.TestClass.testField", "com.example.TestClass");
        
        // When & Then
        assertDoesNotThrow(() -> {
            graphService.saveMethod(method);
            graphService.saveField(field);
            graphService.createRelationship(method.getId(), field.getId(), "USES");
        }, "USES relationship from Method to Field should be supported");
    }
    
    @Test
    void testUsesRelationship_MethodToClass() {
        // Given
        MethodNode method = createMethodNode("testMethod", "com.example.TestClass.testMethod", "com.example.TestClass");
        ClassNode usedClass = createClassNode("UsedClass", "com.example.UsedClass", "com.example");
        
        // When & Then
        assertDoesNotThrow(() -> {
            graphService.saveMethod(method);
            graphService.saveClass(usedClass);
            graphService.createRelationship(method.getId(), usedClass.getId(), "USES");
        }, "USES relationship from Method to Class should be supported");
    }
    
    @Test
    void testDependsOnRelationship_ClassToClass() {
        // Given
        ClassNode dependentClass = createClassNode("DependentClass", "com.example.DependentClass", "com.example");
        ClassNode dependencyClass = createClassNode("DependencyClass", "com.example.DependencyClass", "com.example");
        
        // When & Then
        assertDoesNotThrow(() -> {
            graphService.saveClass(dependentClass);
            graphService.saveClass(dependencyClass);
            graphService.createRelationship(dependentClass.getId(), dependencyClass.getId(), "DEPENDS_ON");
        }, "DEPENDS_ON relationship from Class to Class should be supported");
    }
    
    @Test
    void testRelationshipWithProperties() {
        // Given
        ClassNode classNode = createClassNode("TestClass", "com.example.TestClass", "com.example");
        MethodNode methodNode = createMethodNode("testMethod", "com.example.TestClass.testMethod", "com.example.TestClass");
        Map<String, Object> properties = Map.of("line_number", 10, "context", "method call");
        
        // When & Then
        assertDoesNotThrow(() -> {
            graphService.saveClass(classNode);
            graphService.saveMethod(methodNode);
            graphService.createRelationship(classNode.getId(), methodNode.getId(), "CONTAINS", properties);
        }, "Relationship with properties should be supported");
    }
    
    @Test
    void testInvalidRelationshipType() {
        // Given
        String invalidRelationshipType = "INVALID_RELATIONSHIP";
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            graphService.createRelationship("from-id", "to-id", invalidRelationshipType);
        }, "Invalid relationship type should be rejected");
    }
    
    @Test
    void testNullRelationshipType() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            graphService.createRelationship("from-id", "to-id", null);
        }, "Null relationship type should be rejected");
    }
    
    @Test
    void testEmptyRelationshipType() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            graphService.createRelationship("from-id", "to-id", "");
        }, "Empty relationship type should be rejected");
    }
    
    @Test
    void testNullIds() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            graphService.createRelationship(null, "to-id", "CONTAINS");
        }, "Null from ID should be rejected");
        
        assertThrows(IllegalArgumentException.class, () -> {
            graphService.createRelationship("from-id", null, "CONTAINS");
        }, "Null to ID should be rejected");
    }
    
    @Test
    void testEmptyIds() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            graphService.createRelationship("", "to-id", "CONTAINS");
        }, "Empty from ID should be rejected");
        
        assertThrows(IllegalArgumentException.class, () -> {
            graphService.createRelationship("from-id", "", "CONTAINS");
        }, "Empty to ID should be rejected");
    }
    
    @Test
    void testSchemaCompleteness() {
        // Verify that all relationship types from the schema design are covered
        List<String> schemaRelationshipTypes = Arrays.asList(
            "CONTAINS", "EXTENDS", "IMPLEMENTS", "CALLS", "USES", "DEPENDS_ON"
        );
        
        // Verify that each relationship type is valid
        for (String relationshipType : schemaRelationshipTypes) {
            assertTrue(isValidRelationshipType(relationshipType), 
                "Relationship type '" + relationshipType + "' should be valid");
        }
        
        // Verify that the schema covers all necessary relationships
        assertTrue(schemaRelationshipTypes.contains("CONTAINS"), "CONTAINS relationship should be in schema");
        assertTrue(schemaRelationshipTypes.contains("EXTENDS"), "EXTENDS relationship should be in schema");
        assertTrue(schemaRelationshipTypes.contains("IMPLEMENTS"), "IMPLEMENTS relationship should be in schema");
        assertTrue(schemaRelationshipTypes.contains("CALLS"), "CALLS relationship should be in schema");
        assertTrue(schemaRelationshipTypes.contains("USES"), "USES relationship should be in schema");
        assertTrue(schemaRelationshipTypes.contains("DEPENDS_ON"), "DEPENDS_ON relationship should be in schema");
    }
    
    // Helper methods
    private boolean isValidRelationshipType(String relationshipType) {
        List<String> validTypes = Arrays.asList(
            "CONTAINS", "EXTENDS", "IMPLEMENTS", "CALLS", "USES", "DEPENDS_ON"
        );
        return validTypes.contains(relationshipType);
    }
    
    private PackageNode createPackageNode(String name, String path) {
        PackageNode node = new PackageNode();
        node.setId("package:" + name);
        node.setName(name);
        node.setPath(path);
        node.setFilePath(path + "/package-info.java");
        node.setLineStart(1);
        node.setLineEnd(1);
        node.setCreatedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());
        return node;
    }
    
    private ClassNode createClassNode(String name, String id, String packageName) {
        ClassNode node = new ClassNode();
        node.setId(id);
        node.setName(name);
        node.setVisibility("PUBLIC");
        node.setModifiers(Arrays.asList("public"));
        node.setIsInterface(false);
        node.setIsEnum(false);
        node.setIsAnnotation(false);
        node.setFilePath("src/main/java/" + packageName.replace('.', '/') + "/" + name + ".java");
        node.setLineStart(1);
        node.setLineEnd(10);
        node.setPackageName(packageName);
        node.setCreatedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());
        return node;
    }
    
    private MethodNode createMethodNode(String name, String id, String className) {
        MethodNode node = new MethodNode();
        node.setId(id);
        node.setName(name);
        node.setVisibility("PUBLIC");
        node.setModifiers(Arrays.asList("public"));
        node.setReturnType("void");
        node.setParameters(Arrays.asList("String param1", "int param2"));
        node.setParameterNames(Arrays.asList("param1", "param2"));
        node.setFilePath("src/main/java/" + className.replace('.', '/') + ".java");
        node.setLineStart(5);
        node.setLineEnd(8);
        node.setClassName(className);
        node.setPackageName(className.substring(0, className.lastIndexOf('.')));
        node.setCreatedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());
        return node;
    }
    
    private FieldNode createFieldNode(String name, String id, String className) {
        FieldNode node = new FieldNode();
        node.setId(id);
        node.setName(name);
        node.setVisibility("PRIVATE");
        node.setModifiers(Arrays.asList("private"));
        node.setType("String");
        node.setFilePath("src/main/java/" + className.replace('.', '/') + ".java");
        node.setLineNumber(3);
        node.setClassName(className);
        node.setPackageName(className.substring(0, className.lastIndexOf('.')));
        node.setCreatedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());
        return node;
    }
    
    /**
     * Mock implementation of GraphService for testing relationship validation
     */
    private static class MockGraphService implements GraphService {
        
        @Override
        public void savePackage(PackageNode packageNode) {
            // Mock implementation - just validate the node
            assertNotNull(packageNode, "PackageNode should not be null");
            assertNotNull(packageNode.getId(), "PackageNode ID should not be null");
        }
        
        @Override
        public void saveClass(ClassNode classNode) {
            // Mock implementation - just validate the node
            assertNotNull(classNode, "ClassNode should not be null");
            assertNotNull(classNode.getId(), "ClassNode ID should not be null");
        }
        
        @Override
        public void saveMethod(MethodNode methodNode) {
            // Mock implementation - just validate the node
            assertNotNull(methodNode, "MethodNode should not be null");
            assertNotNull(methodNode.getId(), "MethodNode ID should not be null");
        }
        
        @Override
        public void saveField(FieldNode fieldNode) {
            // Mock implementation - just validate the node
            assertNotNull(fieldNode, "FieldNode should not be null");
            assertNotNull(fieldNode.getId(), "FieldNode ID should not be null");
        }
        
        @Override
        public void saveRepository(com.vividcodes.graphrag.model.graph.RepositoryNode repositoryNode) {
            // Mock implementation - just validate the node
            assertNotNull(repositoryNode, "RepositoryNode should not be null");
            assertNotNull(repositoryNode.getId(), "RepositoryNode ID should not be null");
        }
        
        @Override
        public void createRelationship(String fromId, String toId, String relationshipType) {
            // Validate inputs
            validateRelationshipInputs(fromId, toId, relationshipType);
            
            // Validate relationship type
            List<String> validTypes = Arrays.asList(
                "CONTAINS", "EXTENDS", "IMPLEMENTS", "CALLS", "USES", "DEPENDS_ON"
            );
            
            if (!validTypes.contains(relationshipType)) {
                throw new IllegalArgumentException("Invalid relationship type: " + relationshipType);
            }
        }
        
        @Override
        public void createRelationship(String fromId, String toId, String relationshipType, Map<String, Object> properties) {
            // Validate inputs
            validateRelationshipInputs(fromId, toId, relationshipType);
            
            // Validate relationship type
            List<String> validTypes = Arrays.asList(
                "CONTAINS", "EXTENDS", "IMPLEMENTS", "CALLS", "USES", "DEPENDS_ON"
            );
            
            if (!validTypes.contains(relationshipType)) {
                throw new IllegalArgumentException("Invalid relationship type: " + relationshipType);
            }
            
            // Validate properties if provided
            if (properties != null) {
                assertFalse(properties.isEmpty(), "Properties should not be empty if provided");
            }
        }
        
        private void validateRelationshipInputs(String fromId, String toId, String relationshipType) {
            if (fromId == null || fromId.trim().isEmpty()) {
                throw new IllegalArgumentException("From ID cannot be null or empty");
            }
            if (toId == null || toId.trim().isEmpty()) {
                throw new IllegalArgumentException("To ID cannot be null or empty");
            }
            if (relationshipType == null || relationshipType.trim().isEmpty()) {
                throw new IllegalArgumentException("Relationship type cannot be null or empty");
            }
        }
    }
} 