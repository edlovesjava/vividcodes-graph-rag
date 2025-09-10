package com.vividcodes.graphrag.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.vividcodes.graphrag.model.dto.NodeComparisonResult;
import com.vividcodes.graphrag.model.dto.UpsertResult;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.MethodNode;

/**
 * Comprehensive unit tests for UpsertService implementation.
 * Tests all core upsert operations, comparison logic, and transaction handling.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest
@ActiveProfiles("test")
public class UpsertServiceTest {

    @Mock
    private Driver mockNeo4jDriver;
    
    @Mock
    private Session mockSession;
    
    @Mock
    private Transaction mockTransaction;
    
    @Mock
    private Result mockResult;
    
    @Mock
    private Record mockRecord;
    
    @Mock
    private org.neo4j.driver.Value mockValue;
    
    @Mock
    private NodeIdentifierService mockNodeIdentifierService;

    private UpsertService upsertService;

    @BeforeEach
    void setUp() {
        upsertService = new UpsertServiceImpl(mockNeo4jDriver, mockNodeIdentifierService);
        
        // Setup basic mock behaviors
        when(mockNeo4jDriver.session()).thenReturn(mockSession);
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        when(mockSession.writeTransaction(any())).thenAnswer(invocation -> {
            // Return null for now since we're mocking the session behavior at a higher level
            return null;
        });
        
        // Mock transaction.run() to return empty results (no existing nodes) - use lenient for overrides
        lenient().when(mockTransaction.run(anyString(), any(java.util.Map.class))).thenReturn(mockResult);
        lenient().when(mockTransaction.run(anyString())).thenReturn(mockResult);
        lenient().when(mockResult.hasNext()).thenReturn(false); // Default: no existing nodes
        
        // Mock node identifier service - use correct method name
        when(mockNodeIdentifierService.validateNodeId(anyString(), any())).thenReturn(true);
    }

    // Node Comparison Tests

    @Test
    void testCompareNodes_IdenticalNodes() {
        ClassNode node1 = new ClassNode("TestClass", "com.example", "TestClass.java");
        ClassNode node2 = new ClassNode("TestClass", "com.example", "TestClass.java");
        
        NodeComparisonResult result = upsertService.compareNodes(node1, node2);
        
        assertTrue(result.isIdentical());
        assertFalse(result.hasChanges());
        assertEquals(0, result.getChangeCount());
    }

    @Test
    void testCompareNodes_DifferentNodes() {
        ClassNode node1 = new ClassNode("TestClass1", "com.example", "TestClass1.java");
        ClassNode node2 = new ClassNode("TestClass2", "com.example", "TestClass2.java");
        
        NodeComparisonResult result = upsertService.compareNodes(node1, node2);
        
        assertFalse(result.isIdentical());
        assertTrue(result.hasChanges());
        assertTrue(result.getChangeCount() > 0);
    }

    @Test
    void testCompareNodes_DifferentTypes() {
        ClassNode classNode = new ClassNode("TestClass", "com.example", "TestClass.java");
        MethodNode methodNode = new MethodNode("testMethod", "TestClass", "com.example", "TestClass.java");
        
        NodeComparisonResult result = upsertService.compareNodes(classNode, methodNode);
        
        assertTrue(result.hasConflict());
        assertNotNull(result.getConflictReason());
    }

    @Test
    void testUpdateNodeProperties_WithChanges() {
        ClassNode existingNode = new ClassNode("TestClass", "com.example", "TestClass.java");
        existingNode.setVisibility("public");
        
        ClassNode incomingNode = new ClassNode("TestClass", "com.example", "TestClass.java");
        incomingNode.setVisibility("protected");
        
        NodeComparisonResult comparison = upsertService.compareNodes(existingNode, incomingNode);
        
        ClassNode updatedNode = upsertService.updateNodeProperties(existingNode, incomingNode, comparison);
        
        assertEquals("protected", updatedNode.getVisibility());
    }

    @Test
    void testUpdateNodeProperties_NoChanges() {
        ClassNode existingNode = new ClassNode("TestClass", "com.example", "TestClass.java");
        ClassNode incomingNode = new ClassNode("TestClass", "com.example", "TestClass.java");
        
        NodeComparisonResult comparison = upsertService.compareNodes(existingNode, incomingNode);
        
        ClassNode updatedNode = upsertService.updateNodeProperties(existingNode, incomingNode, comparison);
        
        assertEquals(existingNode, updatedNode);
    }

    // Batch Operations Tests

    @Test
    void testUpsertBatch_MultipleNodes() {
        // Mock the session and transaction behavior for batch operations
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        
        ClassNode class1 = new ClassNode("Class1", "com.example", "Class1.java");
        ClassNode class2 = new ClassNode("Class2", "com.example", "Class2.java");
        
        List<Object> nodes = Arrays.asList(class1, class2);
        
        // Mock the transaction run method to simulate node not existing (insert scenario)
        when(mockTransaction.run(anyString(), any(java.util.Map.class))).thenReturn(mockResult);
        when(mockResult.hasNext()).thenReturn(false);
        
        List<UpsertResult> results = upsertService.upsertBatch(nodes);
        
        assertEquals(2, results.size());
        // Note: Actual assertions would depend on the mocked behavior setup
    }

    // Operation ID Management Tests

    @Test
    void testOperationIdManagement() {
        String originalId = upsertService.getOperationId();
        assertNotNull(originalId);
        
        String customId = "custom_operation_123";
        upsertService.setOperationId(customId);
        
        assertEquals(customId, upsertService.getOperationId());
    }

    // Statistics Tests

    @Test
    void testStatisticsTracking() {
        Map<String, Object> initialStats = upsertService.getUpsertStatistics();
        
        assertEquals(0L, initialStats.get("insertCount"));
        assertEquals(0L, initialStats.get("updateCount"));
        assertEquals(0L, initialStats.get("skipCount"));
        assertEquals(0L, initialStats.get("errorCount"));
        assertEquals(0L, initialStats.get("totalOperations"));
    }

    @Test
    void testResetStatistics() {
        upsertService.resetStatistics();
        
        Map<String, Object> stats = upsertService.getUpsertStatistics();
        
        assertEquals(0L, stats.get("insertCount"));
        assertEquals(0L, stats.get("updateCount"));
        assertEquals(0L, stats.get("skipCount"));
        assertEquals(0L, stats.get("errorCount"));
    }

    // Transaction Tests

    @Test
    void testExecuteInTransaction_Success() {
        List<UpsertService.UpsertOperation> operations = Arrays.asList(
            () -> UpsertResult.success("node1", "Class", UpsertResult.OperationType.INSERT, new HashMap<>(), 10, "op1"),
            () -> UpsertResult.success("node2", "Method", UpsertResult.OperationType.UPDATE, new HashMap<>(), 15, "op1")
        );
        
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        
        List<UpsertResult> results = upsertService.executeInTransaction(operations);
        
        assertEquals(2, results.size());
        assertTrue(results.get(0).isSuccess());
        assertTrue(results.get(1).isSuccess());
    }

    // Node Type Specific Tests

    // Note: testUpsertClass_NewNode and testUpsertMethod_NewNode removed 
    // - These are now covered by integration tests in UpsertServiceIntegrationTest

    // Error Handling Tests

    @Test
    void testUpsertClass_NullId() {
        ClassNode classNode = new ClassNode("TestClass", "com.example", "TestClass.java");
        // Force ID to be null by creating a node without proper ID generation
        try {
            java.lang.reflect.Field idField = ClassNode.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(classNode, null);
        } catch (Exception e) {
            // Ignore reflection errors for this test
        }
        
        UpsertResult result = upsertService.upsertClass(classNode);
        
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
    }

    // Integration-style Tests (using actual node operations)

    @Test 
    void testNodeComparisonWithRealNodes() {
        ClassNode node1 = new ClassNode("TestClass", "com.example", "TestClass.java");
        node1.setVisibility("public");
        
        ClassNode node2 = new ClassNode("TestClass", "com.example", "TestClass.java");
        node2.setVisibility("private");
        
        NodeComparisonResult result = upsertService.compareNodes(node1, node2);
        
        assertFalse(result.isIdentical());
        assertTrue(result.hasChanges());
        assertTrue(result.getPropertyChanges().containsKey("visibility"));
    }

    @Test
    void testPropertyChangeDetection() {
        ClassNode original = new ClassNode("TestClass", "com.example", "TestClass.java");
        original.setVisibility("public");
        
        ClassNode modified = new ClassNode("TestClass", "com.example", "TestClass.java");
        modified.setVisibility("protected");
        modified.setIsInterface(true); // Additional change
        
        NodeComparisonResult result = upsertService.compareNodes(original, modified);
        
        assertEquals(2, result.getChangeCount()); // visibility and isInterface changed
        assertTrue(result.getPropertyChanges().containsKey("visibility"));
        assertTrue(result.getPropertyChanges().containsKey("isInterface"));
    }

    // Performance and Edge Case Tests

    @Test
    void testLargePropertyMap() {
        ClassNode node1 = new ClassNode("TestClass", "com.example", "TestClass.java");
        ClassNode node2 = new ClassNode("TestClass", "com.example", "TestClass.java");
        
        // Add large lists to test performance with complex objects
        node1.setModifiers(Arrays.asList("public", "static", "final"));
        node2.setModifiers(Arrays.asList("protected", "static"));
        
        NodeComparisonResult result = upsertService.compareNodes(node1, node2);
        
        assertFalse(result.isIdentical());
        assertTrue(result.hasChanges());
    }

    @Test
    void testNullPropertyHandling() {
        ClassNode node1 = new ClassNode("TestClass", "com.example", "TestClass.java");
        node1.setVisibility(null);
        
        ClassNode node2 = new ClassNode("TestClass", "com.example", "TestClass.java");
        node2.setVisibility("public");
        
        NodeComparisonResult result = upsertService.compareNodes(node1, node2);
        
        assertTrue(result.hasChanges());
        assertTrue(result.getPropertyChanges().containsKey("visibility"));
        assertEquals(NodeComparisonResult.PropertyChange.ChangeType.ADDED,
                result.getPropertyChanges().get("visibility").getChangeType());
    }
}
