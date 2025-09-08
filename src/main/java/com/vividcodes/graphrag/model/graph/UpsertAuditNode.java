package com.vividcodes.graphrag.model.graph;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

/**
 * Node representing audit trail information for upsert operations.
 * Tracks all changes made during node upsert operations for debugging,
 * rollback, and change analysis purposes.
 */
@Node("UpsertAudit")
public class UpsertAuditNode {
    
    @Id
    private String id;
    
    @Property("operationId")
    private String operationId;
    
    @Property("operationType")
    private String operationType; // "insert", "update", "skip"
    
    @Property("nodeType") 
    private String nodeType; // "Class", "Method", "Field", "Package", etc.
    
    @Property("nodeId")
    private String nodeId;
    
    @Property("oldValues")
    private Map<String, Object> oldValues; // Previous values (for updates)
    
    @Property("newValues")
    private Map<String, Object> newValues; // New values
    
    @Property("timestamp")
    private LocalDateTime timestamp;
    
    @Property("source")
    private String source; // Import source identifier
    
    @Property("conflictResolution")
    private String conflictResolution; // How conflicts were resolved
    
    @Property("executionTimeMs")
    private Long executionTimeMs; // Time taken for the operation
    
    @Property("created_at")
    private LocalDateTime createdAt;
    
    public UpsertAuditNode() {
        this.timestamp = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }
    
    public UpsertAuditNode(String operationId, String operationType, String nodeType, String nodeId) {
        this();
        this.operationId = operationId;
        this.operationType = operationType;
        this.nodeType = nodeType;
        this.nodeId = nodeId;
        this.id = generateId(operationId, nodeType, nodeId);
    }
    
    /**
     * Generate unique ID for the audit node.
     * 
     * @param operationId The operation identifier
     * @param nodeType The type of node being audited
     * @param nodeId The ID of the node being audited
     * @return Unique audit ID
     */
    private String generateId(String operationId, String nodeType, String nodeId) {
        return "upsert_audit:" + operationId + ":" + nodeType + ":" + nodeId;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getOperationId() {
        return operationId;
    }
    
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
    
    public String getOperationType() {
        return operationType;
    }
    
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
    
    public String getNodeType() {
        return nodeType;
    }
    
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }
    
    public String getNodeId() {
        return nodeId;
    }
    
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    
    public Map<String, Object> getOldValues() {
        return oldValues;
    }
    
    public void setOldValues(Map<String, Object> oldValues) {
        this.oldValues = oldValues;
    }
    
    public Map<String, Object> getNewValues() {
        return newValues;
    }
    
    public void setNewValues(Map<String, Object> newValues) {
        this.newValues = newValues;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getConflictResolution() {
        return conflictResolution;
    }
    
    public void setConflictResolution(String conflictResolution) {
        this.conflictResolution = conflictResolution;
    }
    
    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "UpsertAuditNode{" +
                "id='" + id + '\'' +
                ", operationId='" + operationId + '\'' +
                ", operationType='" + operationType + '\'' +
                ", nodeType='" + nodeType + '\'' +
                ", nodeId='" + nodeId + '\'' +
                ", timestamp=" + timestamp +
                ", source='" + source + '\'' +
                '}';
    }
}
