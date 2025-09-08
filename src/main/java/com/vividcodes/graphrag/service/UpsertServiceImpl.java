package com.vividcodes.graphrag.service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vividcodes.graphrag.model.dto.NodeComparisonResult;
import com.vividcodes.graphrag.model.dto.NodeComparisonResult.PropertyChange;
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
 * Implementation of UpsertService providing intelligent create-or-update operations
 * for all graph node types with change detection, audit trails, and transaction management.
 * 
 * @author Graph RAG System
 * @version 1.0
 * @since 2024
 */
@Service
public class UpsertServiceImpl implements UpsertService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpsertServiceImpl.class);
    
    private final Driver neo4jDriver;
    private final NodeIdentifierService nodeIdentifierService;
    
    // Operation tracking
    private String currentOperationId;
    
    // Statistics tracking
    private final AtomicLong insertCount = new AtomicLong(0);
    private final AtomicLong updateCount = new AtomicLong(0);
    private final AtomicLong skipCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);

    @Autowired
    public UpsertServiceImpl(Driver neo4jDriver, NodeIdentifierService nodeIdentifierService) {
        this.neo4jDriver = neo4jDriver;
        this.nodeIdentifierService = nodeIdentifierService;
        this.currentOperationId = generateOperationId();
    }

    @Override
    public UpsertResult upsertClass(ClassNode classNode) {
        return performUpsert(classNode, NodeType.CLASS, "Class");
    }

    @Override
    public UpsertResult upsertMethod(MethodNode methodNode) {
        return performUpsert(methodNode, NodeType.METHOD, "Method");
    }

    @Override
    public UpsertResult upsertField(FieldNode fieldNode) {
        return performUpsert(fieldNode, NodeType.FIELD, "Field");
    }

    @Override
    public UpsertResult upsertPackage(PackageNode packageNode) {
        return performUpsert(packageNode, NodeType.PACKAGE, "Package");
    }

    @Override
    public UpsertResult upsertRepository(RepositoryNode repositoryNode) {
        return performUpsert(repositoryNode, NodeType.REPOSITORY, "Repository");
    }

    @Override
    public UpsertResult upsertSubProject(SubProjectNode subProjectNode) {
        return performUpsert(subProjectNode, NodeType.SUBPROJECT, "SubProject");
    }

    @Override
    public UpsertResult upsertAnnotation(AnnotationNode annotationNode) {
        return performUpsert(annotationNode, NodeType.ANNOTATION, "Annotation");
    }

    @Override
    public List<UpsertResult> upsertBatch(List<Object> nodes) {
        List<UpsertResult> results = new ArrayList<>();
        
        try (Session session = neo4jDriver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                for (Object node : nodes) {
                    UpsertResult result = performUpsertInTransaction(node, tx);
                    results.add(result);
                }
                tx.commit();
                
                LOGGER.info("Batch upsert completed successfully: {} nodes", nodes.size());
                
            } catch (Exception e) {
                LOGGER.error("Batch upsert failed, transaction rolled back", e);
                // Add failure results for any remaining nodes
                for (int i = results.size(); i < nodes.size(); i++) {
                    String nodeId = extractNodeId(nodes.get(i));
                    results.add(UpsertResult.failure(nodeId, nodes.get(i).getClass().getSimpleName(),
                            "Transaction failed: " + e.getMessage(), 0, currentOperationId));
                }
            }
        }
        
        return results;
    }

    @Override
    public boolean nodeExists(String nodeId, NodeType nodeType) {
        String nodeLabel = getNodeLabelForType(nodeType);
        String query = String.format("MATCH (n:%s {id: $nodeId}) RETURN count(n) > 0 as exists", nodeLabel);
        
        try (Session session = neo4jDriver.session()) {
            Result result = session.run(query, Values.parameters("nodeId", nodeId));
            return result.single().get("exists").asBoolean();
        } catch (Exception e) {
            LOGGER.error("Error checking node existence for ID: {}, type: {}", nodeId, nodeType, e);
            return false;
        }
    }

    @Override
    public <T> T findExistingNode(String nodeId, NodeType nodeType) {
        String nodeLabel = getNodeLabelForType(nodeType);
        String query = String.format("MATCH (n:%s {id: $nodeId}) RETURN n", nodeLabel);
        
        try (Session session = neo4jDriver.session()) {
            Result result = session.run(query, Values.parameters("nodeId", nodeId));
            
            if (result.hasNext()) {
                Record record = result.next();
                @SuppressWarnings("unchecked")
                T node = (T) mapRecordToNode(record.get("n").asNode(), nodeType);
                return node;
            }
            
            return null;
        } catch (Exception e) {
            LOGGER.error("Error finding existing node for ID: {}, type: {}", nodeId, nodeType, e);
            return null;
        }
    }

    @Override
    public Map<String, Boolean> checkNodesExist(Map<String, NodeType> nodeIds) {
        Map<String, Boolean> existenceMap = new HashMap<>();
        
        // Group by node type for efficient querying
        Map<NodeType, List<String>> nodesByType = new HashMap<>();
        for (Map.Entry<String, NodeType> entry : nodeIds.entrySet()) {
            nodesByType.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }
        
        try (Session session = neo4jDriver.session()) {
            for (Map.Entry<NodeType, List<String>> entry : nodesByType.entrySet()) {
                NodeType nodeType = entry.getKey();
                List<String> ids = entry.getValue();
                
                String nodeLabel = getNodeLabelForType(nodeType);
                String query = String.format("MATCH (n:%s) WHERE n.id IN $nodeIds RETURN n.id as id", nodeLabel);
                
                Result result = session.run(query, Values.parameters("nodeIds", ids));
                
                // Mark found nodes as existing
                while (result.hasNext()) {
                    String foundId = result.next().get("id").asString();
                    existenceMap.put(foundId, true);
                }
                
                // Mark remaining nodes as not existing
                for (String id : ids) {
                    existenceMap.putIfAbsent(id, false);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error checking batch node existence", e);
            // Mark all as false on error
            for (String nodeId : nodeIds.keySet()) {
                existenceMap.put(nodeId, false);
            }
        }
        
        return existenceMap;
    }

    @Override
    public NodeComparisonResult compareNodes(Object existingNode, Object incomingNode) {
        if (existingNode == null || incomingNode == null) {
            throw new IllegalArgumentException("Both nodes must be non-null for comparison");
        }
        
        if (!existingNode.getClass().equals(incomingNode.getClass())) {
            return NodeComparisonResult.conflict("Node types do not match: " + 
                    existingNode.getClass().getSimpleName() + " vs " + incomingNode.getClass().getSimpleName());
        }
        
        Map<String, PropertyChange> changes = new HashMap<>();
        
        try {
            Field[] fields = existingNode.getClass().getDeclaredFields();
            
            for (Field field : fields) {
                // Skip static and synthetic fields
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                
                field.setAccessible(true);
                
                Object existingValue = field.get(existingNode);
                Object incomingValue = field.get(incomingNode);
                
                PropertyChange change = compareFieldValues(field.getName(), existingValue, incomingValue, field.getType());
                
                if (change != null) {
                    changes.put(field.getName(), change);
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Error comparing nodes", e);
            return NodeComparisonResult.conflict("Comparison failed: " + e.getMessage());
        }
        
        // Filter out insignificant changes
        Map<String, PropertyChange> significantChanges = changes.entrySet().stream()
                .filter(entry -> {
                    PropertyChange change = entry.getValue();
                    return change.getChangeType() != PropertyChange.ChangeType.MODIFIED || change.isSignificant();
                })
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
        
        if (significantChanges.isEmpty()) {
            return NodeComparisonResult.identical();
        } else {
            return NodeComparisonResult.propertiesChanged(significantChanges);
        }
    }

    @Override
    public <T> T updateNodeProperties(T existingNode, T incomingNode, NodeComparisonResult comparisonResult) {
        if (!comparisonResult.hasChanges()) {
            return existingNode;
        }
        
        try {
            for (Map.Entry<String, PropertyChange> entry : comparisonResult.getPropertyChanges().entrySet()) {
                String propertyName = entry.getKey();
                PropertyChange change = entry.getValue();
                
                if (change.getChangeType() != PropertyChange.ChangeType.REMOVED) {
                    Field field = existingNode.getClass().getDeclaredField(propertyName);
                    field.setAccessible(true);
                    field.set(existingNode, change.getNewValue());
                }
            }
            
            // Update the updated_at timestamp if the field exists
            try {
                Field updatedAtField = existingNode.getClass().getDeclaredField("updatedAt");
                updatedAtField.setAccessible(true);
                updatedAtField.set(existingNode, LocalDateTime.now());
            } catch (NoSuchFieldException e) {
                // Field doesn't exist, ignore
            }
            
        } catch (Exception e) {
            LOGGER.error("Error updating node properties", e);
            throw new RuntimeException("Failed to update node properties", e);
        }
        
        return existingNode;
    }

    @Override
    public List<UpsertResult> executeInTransaction(List<UpsertOperation> operations) {
        List<UpsertResult> results = new ArrayList<>();
        
        try (Session session = neo4jDriver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                for (UpsertOperation operation : operations) {
                    UpsertResult result = operation.execute();
                    results.add(result);
                }
                tx.commit();
                
                LOGGER.info("Transaction completed successfully with {} operations", operations.size());
                
            } catch (Exception e) {
                LOGGER.error("Transaction failed, rolling back", e);
                throw new RuntimeException("Transaction execution failed", e);
            }
        }
        
        return results;
    }

    @Override
    public void setOperationId(String operationId) {
        this.currentOperationId = operationId;
    }

    @Override
    public String getOperationId() {
        return currentOperationId;
    }

    @Override
    public Map<String, Object> getUpsertStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("insertCount", insertCount.get());
        stats.put("updateCount", updateCount.get());
        stats.put("skipCount", skipCount.get());
        stats.put("errorCount", errorCount.get());
        stats.put("totalOperations", insertCount.get() + updateCount.get() + skipCount.get() + errorCount.get());
        stats.put("totalProcessingTimeMs", totalProcessingTime.get());
        
        long totalOps = insertCount.get() + updateCount.get() + skipCount.get() + errorCount.get();
        if (totalOps > 0) {
            stats.put("averageProcessingTimeMs", totalProcessingTime.get() / totalOps);
        }
        
        return stats;
    }

    @Override
    public void resetStatistics() {
        insertCount.set(0);
        updateCount.set(0);
        skipCount.set(0);
        errorCount.set(0);
        totalProcessingTime.set(0);
        LOGGER.info("Upsert statistics reset");
    }

    // Private helper methods

    private <T> UpsertResult performUpsert(T node, NodeType nodeType, String nodeLabel) {
        long startTime = System.currentTimeMillis();
        
        try {
            String nodeId = extractNodeId(node);
            
            if (nodeId == null || nodeId.trim().isEmpty()) {
                errorCount.incrementAndGet();
                long processingTime = System.currentTimeMillis() - startTime;
                totalProcessingTime.addAndGet(processingTime);
                return UpsertResult.failure(nodeId, nodeLabel, "Node ID is null or empty", processingTime, currentOperationId);
            }
            
            // Validate node ID format using NodeIdentifierService
            if (!nodeIdentifierService.validateNodeId(nodeId, nodeType)) {
                errorCount.incrementAndGet();
                long processingTime = System.currentTimeMillis() - startTime;
                totalProcessingTime.addAndGet(processingTime);
                return UpsertResult.failure(nodeId, nodeLabel, "Invalid node ID format for type " + nodeType, processingTime, currentOperationId);
            }
            
            LOGGER.debug("Starting upsert for {} node: {}", nodeLabel, nodeId);
            
            try (Session session = neo4jDriver.session()) {
                try (Transaction tx = session.beginTransaction()) {
                    UpsertResult result = performUpsertInTransaction(node, tx);
                    tx.commit();
                    return result;
                } catch (Exception e) {
                    LOGGER.error("Transaction failed for {} node", nodeLabel, e);
                    throw e;
                }
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
            long processingTime = System.currentTimeMillis() - startTime;
            totalProcessingTime.addAndGet(processingTime);
            LOGGER.error("Upsert failed for {} node", nodeLabel, e);
            return UpsertResult.failure(extractNodeId(node), nodeLabel, e.getMessage(), processingTime, currentOperationId);
        }
    }

    private <T> UpsertResult performUpsertInTransaction(T node, Transaction tx) {
        long startTime = System.currentTimeMillis();
        
        try {
            String nodeId = extractNodeId(node);
            NodeType nodeType = getNodeTypeFromClass(node.getClass());
            String nodeLabel = getNodeLabelForType(nodeType);
            
            // Check if node exists
            String existenceQuery = String.format("MATCH (n:%s {id: $nodeId}) RETURN n", nodeLabel);
            Result existenceResult = tx.run(existenceQuery, Values.parameters("nodeId", nodeId));
            
            if (existenceResult == null) {
                LOGGER.error("Query execution returned null result for query: {} with nodeId: {}", existenceQuery, nodeId);
                throw new RuntimeException("Database query returned null result");
            }
            
            if (existenceResult.hasNext()) {
                // Node exists - compare and update if needed
                Record record = existenceResult.next();
                @SuppressWarnings("unchecked")
                T existingNode = (T) mapRecordToNode(record.get("n").asNode(), nodeType);
                
                NodeComparisonResult comparison = compareNodes(existingNode, node);
                
                if (comparison.isIdentical()) {
                    skipCount.incrementAndGet();
                    long processingTime = System.currentTimeMillis() - startTime;
                    totalProcessingTime.addAndGet(processingTime);
                    LOGGER.debug("Node unchanged, skipping: {}", nodeId);
                    return UpsertResult.skip(nodeId, nodeLabel, processingTime, currentOperationId);
                } else if (comparison.hasChanges()) {
                    // Update existing node
                    T updatedNode = updateNodeProperties(existingNode, node, comparison);
                    updateNodeInDatabase(tx, updatedNode, nodeType);
                    
                    updateCount.incrementAndGet();
                    long processingTime = System.currentTimeMillis() - startTime;
                    totalProcessingTime.addAndGet(processingTime);
                    
                    LOGGER.debug("Node updated: {}, changes: {}", nodeId, comparison.getChangeCount());
                    
                    Map<String, Object> changedProps = new HashMap<>();
                    for (Map.Entry<String, PropertyChange> entry : comparison.getPropertyChanges().entrySet()) {
                        changedProps.put(entry.getKey(), entry.getValue().getNewValue());
                    }
                    
                    return UpsertResult.success(nodeId, nodeLabel, UpsertResult.OperationType.UPDATE, 
                            changedProps, processingTime, currentOperationId);
                } else {
                    // Conflict
                    long processingTime = System.currentTimeMillis() - startTime;
                    totalProcessingTime.addAndGet(processingTime);
                    return UpsertResult.failure(nodeId, nodeLabel, "Conflict: " + comparison.getConflictReason(),
                            processingTime, currentOperationId);
                }
            } else {
                // Node doesn't exist - create it
                createNodeInDatabase(tx, node, nodeType);
                
                insertCount.incrementAndGet();
                long processingTime = System.currentTimeMillis() - startTime;
                totalProcessingTime.addAndGet(processingTime);
                
                LOGGER.debug("Node created: {}", nodeId);
                
                return UpsertResult.success(nodeId, nodeLabel, UpsertResult.OperationType.INSERT, 
                        new HashMap<>(), processingTime, currentOperationId);
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
            long processingTime = System.currentTimeMillis() - startTime;
            totalProcessingTime.addAndGet(processingTime);
            throw new RuntimeException("Upsert transaction failed", e);
        }
    }

    private PropertyChange compareFieldValues(String fieldName, Object existingValue, Object incomingValue, Class<?> fieldType) {
        // Handle null values
        if (existingValue == null && incomingValue == null) {
            return null; // No change
        }
        
        if (existingValue == null) {
            return PropertyChange.added(incomingValue, fieldType.getSimpleName());
        }
        
        if (incomingValue == null) {
            return PropertyChange.removed(existingValue, fieldType.getSimpleName());
        }
        
        // Check if types are different
        if (!existingValue.getClass().equals(incomingValue.getClass())) {
            return PropertyChange.typeChanged(existingValue, incomingValue, fieldType.getSimpleName());
        }
        
        // Compare values
        if (!Objects.equals(existingValue, incomingValue)) {
            boolean isSignificant = isSignificantChange(fieldName, existingValue, incomingValue);
            return PropertyChange.modified(existingValue, incomingValue, fieldType.getSimpleName(), isSignificant);
        }
        
        return null; // No change
    }

    private boolean isSignificantChange(String fieldName, Object oldValue, Object newValue) {
        // Treat timestamp updates as non-significant (they're expected to change)
        if (fieldName.contains("updated") || fieldName.contains("timestamp") || fieldName.contains("createdAt")) {
            return false;
        }
        
        // All other changes are significant
        return true;
    }

    private void createNodeInDatabase(Transaction tx, Object node, NodeType nodeType) {
        String nodeLabel = getNodeLabelForType(nodeType);
        Map<String, Object> properties = extractNodeProperties(node);
        
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("CREATE (n:").append(nodeLabel).append(" {");
        
        boolean first = true;
        for (String key : properties.keySet()) {
            if (!first) queryBuilder.append(", ");
            queryBuilder.append(key).append(": $").append(key);
            first = false;
        }
        
        queryBuilder.append("})");
        
        tx.run(queryBuilder.toString(), properties);
    }

    private void updateNodeInDatabase(Transaction tx, Object node, NodeType nodeType) {
        String nodeLabel = getNodeLabelForType(nodeType);
        String nodeId = extractNodeId(node);
        Map<String, Object> properties = extractNodeProperties(node);
        
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("MATCH (n:").append(nodeLabel).append(" {id: $id}) SET ");
        
        boolean first = true;
        for (String key : properties.keySet()) {
            if ("id".equals(key)) continue; // Don't update the ID
            if (!first) queryBuilder.append(", ");
            queryBuilder.append("n.").append(key).append(" = $").append(key);
            first = false;
        }
        
        properties.put("id", nodeId);
        tx.run(queryBuilder.toString(), properties);
    }

    private String extractNodeId(Object node) {
        try {
            Method getIdMethod = node.getClass().getMethod("getId");
            return (String) getIdMethod.invoke(node);
        } catch (Exception e) {
            LOGGER.error("Error extracting node ID from {}", node.getClass().getSimpleName(), e);
            return null;
        }
    }

    private Map<String, Object> extractNodeProperties(Object node) {
        Map<String, Object> properties = new HashMap<>();
        
        try {
            Field[] fields = node.getClass().getDeclaredFields();
            
            for (Field field : fields) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                
                field.setAccessible(true);
                Object value = field.get(node);
                
                if (value != null) {
                    properties.put(field.getName(), value);
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Error extracting properties from node", e);
        }
        
        return properties;
    }

    private Object mapRecordToNode(org.neo4j.driver.types.Node dbNode, NodeType nodeType) {
        // This is a simplified mapping - in a real implementation, you'd want a proper object mapper
        Map<String, Object> properties = dbNode.asMap();
        
        try {
            Class<?> nodeClass = getClassForNodeType(nodeType);
            Object node = nodeClass.getDeclaredConstructor().newInstance();
            
            Field[] fields = nodeClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = properties.get(field.getName());
                if (value != null) {
                    field.set(node, value);
                }
            }
            
            return node;
            
        } catch (Exception e) {
            LOGGER.error("Error mapping database node to object", e);
            return null;
        }
    }

    private String getNodeLabelForType(NodeType nodeType) {
        switch (nodeType) {
            case CLASS: return "Class";
            case METHOD: return "Method";
            case FIELD: return "Field";
            case PACKAGE: return "Package";
            case REPOSITORY: return "Repository";
            case SUBPROJECT: return "SubProject";
            case ANNOTATION: return "Annotation";
            case UPSERT_AUDIT: return "UpsertAudit";
            default: throw new IllegalArgumentException("Unknown node type: " + nodeType);
        }
    }

    private Class<?> getClassForNodeType(NodeType nodeType) {
        switch (nodeType) {
            case CLASS: return ClassNode.class;
            case METHOD: return MethodNode.class;
            case FIELD: return FieldNode.class;
            case PACKAGE: return PackageNode.class;
            case REPOSITORY: return RepositoryNode.class;
            case SUBPROJECT: return SubProjectNode.class;
            case ANNOTATION: return AnnotationNode.class;
            default: throw new IllegalArgumentException("Unknown node type: " + nodeType);
        }
    }

    private NodeType getNodeTypeFromClass(Class<?> clazz) {
        if (clazz.equals(ClassNode.class)) return NodeType.CLASS;
        if (clazz.equals(MethodNode.class)) return NodeType.METHOD;
        if (clazz.equals(FieldNode.class)) return NodeType.FIELD;
        if (clazz.equals(PackageNode.class)) return NodeType.PACKAGE;
        if (clazz.equals(RepositoryNode.class)) return NodeType.REPOSITORY;
        if (clazz.equals(SubProjectNode.class)) return NodeType.SUBPROJECT;
        if (clazz.equals(AnnotationNode.class)) return NodeType.ANNOTATION;
        
        throw new IllegalArgumentException("Unknown node class: " + clazz.getSimpleName());
    }

    private String generateOperationId() {
        return "upsert_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }
}
