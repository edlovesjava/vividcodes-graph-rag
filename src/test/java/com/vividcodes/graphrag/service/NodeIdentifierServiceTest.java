package com.vividcodes.graphrag.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.service.NodeIdentifierService.NodeType;

/**
 * Comprehensive unit tests for NodeIdentifierService implementation.
 * Tests ID generation, validation, normalization, and consistency checks.
 */
public class NodeIdentifierServiceTest {

    private NodeIdentifierService nodeIdentifierService;

    @BeforeEach
    void setUp() {
        nodeIdentifierService = new NodeIdentifierServiceImpl();
    }

    // Class ID Generation Tests

    @Test
    void testGenerateClassId_ValidPackageAndClass() {
        String id = nodeIdentifierService.generateClassId("com.example.service", "UserService");
        assertEquals("class:com.example.service:UserService", id);
    }

    @Test
    void testGenerateClassId_EmptyPackage() {
        String id = nodeIdentifierService.generateClassId("", "UserService");
        assertEquals("class::UserService", id);
    }

    @Test
    void testGenerateClassId_NullPackage() {
        String id = nodeIdentifierService.generateClassId(null, "UserService");
        assertEquals("class::UserService", id);
    }

    @Test
    void testGenerateClassId_NullClassName() {
        assertThrows(IllegalArgumentException.class, 
            () -> nodeIdentifierService.generateClassId("com.example", null));
    }

    @Test
    void testGenerateClassId_EmptyClassName() {
        assertThrows(IllegalArgumentException.class, 
            () -> nodeIdentifierService.generateClassId("com.example", ""));
    }

    // Method ID Generation Tests

    @Test
    void testGenerateMethodId_WithParameters() {
        String classId = "class:com.example.service:UserService";
        List<String> params = Arrays.asList("String", "Integer", "boolean");
        
        String methodId = nodeIdentifierService.generateMethodId(classId, "updateUser", params);
        
        assertTrue(methodId.startsWith("method:com.example.service:UserService:updateUser:"));
        assertTrue(methodId.contains(":"));
        assertNotEquals("method:com.example.service:UserService:updateUser:", methodId);
    }

    @Test
    void testGenerateMethodId_NoParameters() {
        String classId = "class:com.example.service:UserService";
        
        String methodId = nodeIdentifierService.generateMethodId(classId, "getUsers", Collections.emptyList());
        
        assertTrue(methodId.startsWith("method:com.example.service:UserService:getUsers:"));
    }

    @Test
    void testGenerateMethodId_NullParameters() {
        String classId = "class:com.example.service:UserService";
        
        String methodId = nodeIdentifierService.generateMethodId(classId, "getUsers", null);
        
        assertTrue(methodId.startsWith("method:com.example.service:UserService:getUsers:"));
    }

    @Test
    void testGenerateMethodId_InvalidClassId() {
        assertThrows(IllegalArgumentException.class,
            () -> nodeIdentifierService.generateMethodId("invalid", "method", null));
    }

    @Test
    void testGenerateMethodId_NullMethodName() {
        String classId = "class:com.example.service:UserService";
        assertThrows(IllegalArgumentException.class,
            () -> nodeIdentifierService.generateMethodId(classId, null, null));
    }

    // Field ID Generation Tests

    @Test
    void testGenerateFieldId_ValidField() {
        String classId = "class:com.example.model:User";
        
        String fieldId = nodeIdentifierService.generateFieldId(classId, "username", "String");
        
        assertEquals("field:com.example.model:User:username:String", fieldId);
    }

    @Test
    void testGenerateFieldId_NullFieldType() {
        String classId = "class:com.example.model:User";
        
        String fieldId = nodeIdentifierService.generateFieldId(classId, "username", null);
        
        assertEquals("field:com.example.model:User:username:Object", fieldId);
    }

    @Test
    void testGenerateFieldId_InvalidClassId() {
        assertThrows(IllegalArgumentException.class,
            () -> nodeIdentifierService.generateFieldId("invalid", "field", "String"));
    }

    // Package ID Generation Tests

    @Test
    void testGeneratePackageId_ValidPackage() {
        String id = nodeIdentifierService.generatePackageId("com.example.service");
        assertEquals("package:com.example.service", id);
    }

    @Test
    void testGeneratePackageId_EmptyPackage() {
        String id = nodeIdentifierService.generatePackageId("");
        assertEquals("package:", id);
    }

    @Test
    void testGeneratePackageId_NullPackage() {
        String id = nodeIdentifierService.generatePackageId(null);
        assertEquals("package:", id);
    }

    // Repository ID Generation Tests

    @Test
    void testGenerateRepositoryId_ValidRepository() {
        String id = nodeIdentifierService.generateRepositoryId("/path/to/repo", "myproject");
        
        assertTrue(id.startsWith("repo:myproject:"));
        assertNotEquals("repo:myproject:", id);
    }

    @Test
    void testGenerateRepositoryId_SameName_DifferentPaths() {
        String id1 = nodeIdentifierService.generateRepositoryId("/path1/to/repo", "project");
        String id2 = nodeIdentifierService.generateRepositoryId("/path2/to/repo", "project");
        
        assertNotEquals(id1, id2, "Different paths should produce different IDs");
    }

    @Test
    void testGenerateRepositoryId_NullName() {
        assertThrows(IllegalArgumentException.class,
            () -> nodeIdentifierService.generateRepositoryId("/path", null));
    }

    @Test
    void testGenerateRepositoryId_NullPath() {
        assertThrows(IllegalArgumentException.class,
            () -> nodeIdentifierService.generateRepositoryId(null, "project"));
    }

    // SubProject ID Generation Tests

    @Test
    void testGenerateSubProjectId_ValidSubProject() {
        String repoId = "repo:myproject:12345678";
        
        String id = nodeIdentifierService.generateSubProjectId(repoId, "submodule/core");
        
        assertEquals("subproject:repo:myproject:12345678:submodule/core", id);
    }

    @Test
    void testGenerateSubProjectId_NullRepositoryId() {
        assertThrows(IllegalArgumentException.class,
            () -> nodeIdentifierService.generateSubProjectId(null, "path"));
    }

    @Test
    void testGenerateSubProjectId_NullPath() {
        String repoId = "repo:myproject:12345678";
        assertThrows(IllegalArgumentException.class,
            () -> nodeIdentifierService.generateSubProjectId(repoId, null));
    }

    // Annotation ID Generation Tests

    @Test
    void testGenerateAnnotationId_WithPackage() {
        String id = nodeIdentifierService.generateAnnotationId("org.springframework.stereotype", "Service");
        assertEquals("annotation:org.springframework.stereotype.Service", id);
    }

    @Test
    void testGenerateAnnotationId_EmptyPackage() {
        String id = nodeIdentifierService.generateAnnotationId("", "Override");
        assertEquals("annotation:Override", id);
    }

    @Test
    void testGenerateAnnotationId_NullPackage() {
        String id = nodeIdentifierService.generateAnnotationId(null, "Override");
        assertEquals("annotation:Override", id);
    }

    @Test
    void testGenerateAnnotationId_NullAnnotationName() {
        assertThrows(IllegalArgumentException.class,
            () -> nodeIdentifierService.generateAnnotationId("package", null));
    }

    // Upsert Audit ID Generation Tests

    @Test
    void testGenerateUpsertAuditId_ValidParameters() {
        String id = nodeIdentifierService.generateUpsertAuditId("op123", "Class", "class:com.example:Test");
        assertEquals("upsert_audit:op123:Class:class:com.example:Test", id);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testGenerateUpsertAuditId_InvalidOperationId(String operationId) {
        assertThrows(IllegalArgumentException.class,
            () -> nodeIdentifierService.generateUpsertAuditId(operationId, "Class", "nodeId"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testGenerateUpsertAuditId_InvalidNodeType(String nodeType) {
        assertThrows(IllegalArgumentException.class,
            () -> nodeIdentifierService.generateUpsertAuditId("op123", nodeType, "nodeId"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testGenerateUpsertAuditId_InvalidNodeId(String nodeId) {
        assertThrows(IllegalArgumentException.class,
            () -> nodeIdentifierService.generateUpsertAuditId("op123", "Class", nodeId));
    }

    // Node ID Validation Tests

    @Test
    void testValidateNodeId_ValidClassId() {
        String classId = "class:com.example:TestClass";
        assertTrue(nodeIdentifierService.validateNodeId(classId, NodeType.CLASS));
    }

    @Test
    void testValidateNodeId_InvalidPrefix() {
        String invalidId = "method:com.example:TestClass";
        assertFalse(nodeIdentifierService.validateNodeId(invalidId, NodeType.CLASS));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testValidateNodeId_InvalidId(String nodeId) {
        assertFalse(nodeIdentifierService.validateNodeId(nodeId, NodeType.CLASS));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "class:com.example:TestClass",
        "method:com.example:TestClass:testMethod:12345678",
        "field:com.example:TestClass:testField:String",
        "package:com.example",
        "repo:testproject:87654321",
        "annotation:com.example.TestAnnotation",
        "upsert_audit:op123:Class:class:com.example:Test"
    })
    void testValidateNodeId_ValidFormats(String nodeId) {
        NodeType nodeType = determineNodeType(nodeId);
        assertTrue(nodeIdentifierService.validateNodeId(nodeId, nodeType));
    }

    // Consistency Check Tests

    @Test
    void testIsConsistentId_SameClassNodes() {
        ClassNode node1 = new ClassNode("TestClass", "com.example", "TestClass.java");
        ClassNode node2 = new ClassNode("TestClass", "com.example", "TestClass.java");
        
        assertTrue(nodeIdentifierService.isConsistentId(node1, node2));
    }

    @Test
    void testIsConsistentId_DifferentClassNodes() {
        ClassNode node1 = new ClassNode("TestClass1", "com.example", "TestClass1.java");
        ClassNode node2 = new ClassNode("TestClass2", "com.example", "TestClass2.java");
        
        assertFalse(nodeIdentifierService.isConsistentId(node1, node2));
    }

    @Test
    void testIsConsistentId_NullNodes() {
        ClassNode node1 = new ClassNode("TestClass", "com.example", "TestClass.java");
        
        assertFalse(nodeIdentifierService.isConsistentId(node1, null));
        assertFalse(nodeIdentifierService.isConsistentId(null, node1));
        assertFalse(nodeIdentifierService.isConsistentId(null, null));
    }

    // Normalization Tests

    @Test
    void testNormalizeMethodSignature_WithParameters() {
        List<String> params = Arrays.asList("String", "int", "List<String>");
        String signature = nodeIdentifierService.normalizeMethodSignature("testMethod", params);
        
        assertTrue(signature.startsWith("testMethod("));
        assertTrue(signature.endsWith(")"));
    }

    @Test
    void testNormalizeMethodSignature_NoParameters() {
        String signature = nodeIdentifierService.normalizeMethodSignature("testMethod", Collections.emptyList());
        assertEquals("testMethod()", signature);
    }

    @Test
    void testNormalizeMethodSignature_NullParameters() {
        String signature = nodeIdentifierService.normalizeMethodSignature("testMethod", null);
        assertEquals("testMethod()", signature);
    }

    @Test
    void testNormalizePackageName_ValidPackage() {
        String normalized = nodeIdentifierService.normalizePackageName("Com.Example.Service");
        assertEquals("com.example.service", normalized);
    }

    @Test
    void testNormalizePackageName_WithSpaces() {
        String normalized = nodeIdentifierService.normalizePackageName("com example service");
        assertEquals("com.example.service", normalized);
    }

    @Test
    void testNormalizePackageName_WithBackslashes() {
        String normalized = nodeIdentifierService.normalizePackageName("com\\example\\service");
        assertEquals("com.example.service", normalized);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void testNormalizePackageName_NullOrEmpty(String packageName) {
        String normalized = nodeIdentifierService.normalizePackageName(packageName);
        assertEquals("", normalized);
    }

    @Test
    void testNormalizeFilePath_ValidPath() {
        String normalized = nodeIdentifierService.normalizeFilePath("/path/to/file.java");
        assertEquals("path/to/file.java", normalized);
    }

    @Test
    void testNormalizeFilePath_WindowsPath() {
        String normalized = nodeIdentifierService.normalizeFilePath("C:\\path\\to\\file.java");
        assertEquals("C:/path/to/file.java", normalized);
    }

    @Test
    void testNormalizeFilePath_MultipleSlashes() {
        String normalized = nodeIdentifierService.normalizeFilePath("/path//to///file.java");
        assertEquals("path/to/file.java", normalized);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void testNormalizeFilePath_NullOrEmpty(String filePath) {
        String normalized = nodeIdentifierService.normalizeFilePath(filePath);
        assertEquals("", normalized);
    }

    // Integration Tests

    @Test
    void testIdGenerationConsistency_SameInputsSameOutput() {
        String id1 = nodeIdentifierService.generateClassId("com.example", "TestClass");
        String id2 = nodeIdentifierService.generateClassId("com.example", "TestClass");
        
        assertEquals(id1, id2, "Same inputs should produce same outputs");
    }

    @Test
    void testIdGenerationUniqueness_DifferentInputsDifferentOutput() {
        String id1 = nodeIdentifierService.generateClassId("com.example1", "TestClass");
        String id2 = nodeIdentifierService.generateClassId("com.example2", "TestClass");
        
        assertNotEquals(id1, id2, "Different inputs should produce different outputs");
    }

    @Test
    void testMethodIdUniqueness_DifferentParametersSameMethod() {
        String classId = "class:com.example:TestClass";
        
        String id1 = nodeIdentifierService.generateMethodId(classId, "test", Arrays.asList("String"));
        String id2 = nodeIdentifierService.generateMethodId(classId, "test", Arrays.asList("Integer"));
        
        assertNotEquals(id1, id2, "Methods with different parameters should have different IDs");
    }

    // Helper methods

    private NodeType determineNodeType(String nodeId) {
        if (nodeId.startsWith("class:")) return NodeType.CLASS;
        if (nodeId.startsWith("method:")) return NodeType.METHOD;
        if (nodeId.startsWith("field:")) return NodeType.FIELD;
        if (nodeId.startsWith("package:")) return NodeType.PACKAGE;
        if (nodeId.startsWith("repo:")) return NodeType.REPOSITORY;
        if (nodeId.startsWith("subproject:")) return NodeType.SUBPROJECT;
        if (nodeId.startsWith("annotation:")) return NodeType.ANNOTATION;
        if (nodeId.startsWith("upsert_audit:")) return NodeType.UPSERT_AUDIT;
        
        throw new IllegalArgumentException("Unknown node type for ID: " + nodeId);
    }
}
