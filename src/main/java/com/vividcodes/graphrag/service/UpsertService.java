package com.vividcodes.graphrag.service;

import java.util.List;
import java.util.Map;
import com.vividcodes.graphrag.model.dto.NodeComparisonResult;
import com.vividcodes.graphrag.model.dto.UpsertResult;
import com.vividcodes.graphrag.model.graph.AnnotationNode;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.FieldNode;
import com.vividcodes.graphrag.model.graph.MethodNode;
import com.vividcodes.graphrag.model.graph.PackageNode;
import com.vividcodes.graphrag.model.graph.RepositoryNode;
import com.vividcodes.graphrag.model.graph.SubProjectNode;
import com.vividcodes.graphrag.service.NodeIdentifierService.NodeType;

/**
 * Service interface for performing upsert (update or insert) operations on graph nodes.
 * Provides intelligent create-or-update functionality with change detection and audit trail.
 * 
 * @author Graph RAG System
 * @version 1.0
 * @since 2024
 */
public interface UpsertService {

    // Core Upsert Operations

    /**
     * Upsert a class node - create if doesn't exist, update if changed.
     * 
     * @param classNode The class node to upsert
     * @return UpsertResult with operation details and statistics
     */
    UpsertResult upsertClass(ClassNode classNode);

    /**
     * Upsert a method node - create if doesn't exist, update if changed.
     * 
     * @param methodNode The method node to upsert
     * @return UpsertResult with operation details and statistics
     */
    UpsertResult upsertMethod(MethodNode methodNode);

    /**
     * Upsert a field node - create if doesn't exist, update if changed.
     * 
     * @param fieldNode The field node to upsert
     * @return UpsertResult with operation details and statistics
     */
    UpsertResult upsertField(FieldNode fieldNode);

    /**
     * Upsert a package node - create if doesn't exist, update if changed.
     * 
     * @param packageNode The package node to upsert
     * @return UpsertResult with operation details and statistics
     */
    UpsertResult upsertPackage(PackageNode packageNode);

    /**
     * Upsert a repository node - create if doesn't exist, update if changed.
     * 
     * @param repositoryNode The repository node to upsert
     * @return UpsertResult with operation details and statistics
     */
    UpsertResult upsertRepository(RepositoryNode repositoryNode);

    /**
     * Upsert a sub-project node - create if doesn't exist, update if changed.
     * 
     * @param subProjectNode The sub-project node to upsert
     * @return UpsertResult with operation details and statistics
     */
    UpsertResult upsertSubProject(SubProjectNode subProjectNode);

    /**
     * Upsert an annotation node - create if doesn't exist, update if changed.
     * 
     * @param annotationNode The annotation node to upsert
     * @return UpsertResult with operation details and statistics
     */
    UpsertResult upsertAnnotation(AnnotationNode annotationNode);

    // Batch Operations

    /**
     * Upsert multiple nodes in a single transaction for better performance.
     * 
     * @param nodes List of nodes to upsert (mixed types allowed)
     * @return List of UpsertResult for each node in the same order
     */
    List<UpsertResult> upsertBatch(List<Object> nodes);

    // Node Existence and Lookup

    /**
     * Check if a node exists in the database using its unique ID.
     * 
     * @param nodeId The unique node identifier
     * @param nodeType The type of node to check
     * @return true if the node exists, false otherwise
     */
    boolean nodeExists(String nodeId, NodeType nodeType);

    /**
     * Find and return an existing node by its unique ID.
     * 
     * @param nodeId The unique node identifier
     * @param nodeType The type of node to find
     * @return The existing node, or null if not found
     */
    <T> T findExistingNode(String nodeId, NodeType nodeType);

    /**
     * Check existence of multiple nodes efficiently in a batch operation.
     * 
     * @param nodeIds Map of node IDs to their types
     * @return Map of node IDs to their existence status
     */
    Map<String, Boolean> checkNodesExist(Map<String, NodeType> nodeIds);

    // Node Comparison and Analysis

    /**
     * Compare an existing node with an incoming node to detect changes.
     * 
     * @param existingNode The node currently in the database
     * @param incomingNode The new node to compare against
     * @return NodeComparisonResult with detailed change analysis
     */
    NodeComparisonResult compareNodes(Object existingNode, Object incomingNode);

    /**
     * Update properties of an existing node with values from the incoming node.
     * 
     * @param existingNode The node to update
     * @param incomingNode The source of new property values
     * @param comparisonResult The result of comparing the nodes
     * @return Updated node with new property values
     */
    <T> T updateNodeProperties(T existingNode, T incomingNode, NodeComparisonResult comparisonResult);

    // Transaction and Operation Management

    /**
     * Execute multiple upsert operations within a single database transaction.
     * 
     * @param operations List of operations to execute atomically
     * @return List of UpsertResult for each operation
     */
    List<UpsertResult> executeInTransaction(List<UpsertOperation> operations);

    /**
     * Set the current operation ID for audit trail purposes.
     * All subsequent upserts will use this operation ID until changed.
     * 
     * @param operationId Unique identifier for the current batch of operations
     */
    void setOperationId(String operationId);

    /**
     * Get the current operation ID being used for audit trails.
     * 
     * @return Current operation ID, or null if not set
     */
    String getOperationId();

    // Statistics and Monitoring

    /**
     * Get statistics about upsert operations performed.
     * 
     * @return Map containing operation counts and performance metrics
     */
    Map<String, Object> getUpsertStatistics();

    /**
     * Reset operation statistics counters.
     */
    void resetStatistics();

    /**
     * Functional interface for defining upsert operations in transactions.
     */
    @FunctionalInterface
    interface UpsertOperation {
        UpsertResult execute();
    }
}
