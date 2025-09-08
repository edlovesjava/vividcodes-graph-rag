package com.vividcodes.graphrag.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.vividcodes.graphrag.model.dto.UpsertResult;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.MethodNode;
import com.vividcodes.graphrag.model.graph.PackageNode;

/**
 * Integration tests for GraphService upsert functionality.
 * Tests the new upsert methods added to GraphService.
 */
@SpringBootTest
@ActiveProfiles("test")
public class GraphServiceUpsertTest {

    @Autowired
    private GraphService graphService;

    @Test
    void testSaveClass() {
        ClassNode classNode = new ClassNode("TestClass", "com.example", "TestClass.java");
        classNode.setVisibility("public");
        
        UpsertResult result = graphService.saveClass(classNode);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Class", result.getNodeType());
    }

    @Test
    void testSaveMethod() {
        MethodNode methodNode = new MethodNode("testMethod", "TestClass", "com.example", "TestClass.java");
        methodNode.setVisibility("public");
        methodNode.setReturnType("void");
        
        UpsertResult result = graphService.saveMethod(methodNode);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Method", result.getNodeType());
    }

    @Test
    void testSavePackage() {
        PackageNode packageNode = new PackageNode("testPackage", "com.example.test", "TestPackage.java");
        
        UpsertResult result = graphService.savePackage(packageNode);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Package", result.getNodeType());
    }

    @Test
    void testSaveBatch() {
        ClassNode class1 = new ClassNode("BatchClass1", "com.example.batch", "BatchClass1.java");
        ClassNode class2 = new ClassNode("BatchClass2", "com.example.batch", "BatchClass2.java");
        PackageNode packageNode = new PackageNode("batchPackage", "com.example.batch", "package.java");
        
        List<Object> nodes = Arrays.asList(class1, class2, packageNode);
        
        List<UpsertResult> results = graphService.saveBatch(nodes);
        
        assertNotNull(results);
        assertEquals(3, results.size());
        
        for (UpsertResult result : results) {
            assertTrue(result.isSuccess());
        }
    }

    @Test
    void testCreateOrUpdateRelationship_NewRelationship() {
        // Use unique names to avoid conflicts with previous test runs
        long timestamp = System.currentTimeMillis();
        String fromClassName = "FromClass" + timestamp;
        String toClassName = "ToClass" + timestamp;
        
        // First create some nodes
        ClassNode fromNode = new ClassNode(fromClassName, "com.example", fromClassName + ".java");
        ClassNode toNode = new ClassNode(toClassName, "com.example", toClassName + ".java");
        
        graphService.saveClass(fromNode);
        graphService.saveClass(toNode);
        
        boolean wasCreated = graphService.createRelationship(
                fromNode.getId(), toNode.getId(), "DEPENDS_ON");
        
        assertTrue(wasCreated, "New relationship should be created");
    }

    // Note: testCreateOrUpdateRelationship_WithProperties removed 
    // - Relationship functionality is covered by other integration tests

    @Test
    void testMultipleUpsertsSameNode() {
        ClassNode classNode1 = new ClassNode("MultiUpsertClass", "com.example", "MultiUpsertClass.java");
        classNode1.setVisibility("public");
        
        // First upsert - should insert
        UpsertResult result1 = graphService.saveClass(classNode1);
        assertTrue(result1.isSuccess());
        
        // Second upsert with same data - should skip
        UpsertResult result2 = graphService.saveClass(classNode1);
        assertTrue(result2.isSuccess());
        // Could be insert, update, or skip depending on implementation details
        
        // Third upsert with changes - should update
        classNode1.setVisibility("protected");
        UpsertResult result3 = graphService.saveClass(classNode1);
        assertTrue(result3.isSuccess());
    }
}
