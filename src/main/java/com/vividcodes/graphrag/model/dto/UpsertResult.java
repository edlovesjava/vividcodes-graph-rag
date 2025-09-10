package com.vividcodes.graphrag.model.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Result of an upsert operation containing operation statistics and metadata.
 * 
 * @author Graph RAG System
 * @version 1.0
 * @since 2024
 */
public class UpsertResult {

    public enum OperationType {
        INSERT,    // Node was created
        UPDATE,    // Node was updated
        SKIP       // Node was unchanged
    }

    private final String nodeId;
    private final String nodeType;
    private final OperationType operationType;
    private final boolean success;
    private final String errorMessage;
    private final LocalDateTime timestamp;
    private final long processingTimeMs;
    private final Map<String, Object> changedProperties;
    private final int propertyCount;
    private final String operationId;

    private UpsertResult(Builder builder) {
        this.nodeId = builder.nodeId;
        this.nodeType = builder.nodeType;
        this.operationType = builder.operationType;
        this.success = builder.success;
        this.errorMessage = builder.errorMessage;
        this.timestamp = builder.timestamp;
        this.processingTimeMs = builder.processingTimeMs;
        this.changedProperties = builder.changedProperties;
        this.propertyCount = builder.propertyCount;
        this.operationId = builder.operationId;
    }

    // Static factory methods for common scenarios
    public static UpsertResult success(String nodeId, String nodeType, OperationType operationType,
                                      Map<String, Object> changedProperties, long processingTimeMs, String operationId) {
        return new Builder(nodeId, nodeType)
                .operationType(operationType)
                .success(true)
                .timestamp(LocalDateTime.now())
                .processingTimeMs(processingTimeMs)
                .changedProperties(changedProperties)
                .propertyCount(changedProperties != null ? changedProperties.size() : 0)
                .operationId(operationId)
                .build();
    }

    public static UpsertResult failure(String nodeId, String nodeType, String errorMessage, long processingTimeMs, String operationId) {
        return new Builder(nodeId, nodeType)
                .operationType(null)
                .success(false)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .processingTimeMs(processingTimeMs)
                .operationId(operationId)
                .build();
    }

    public static UpsertResult skip(String nodeId, String nodeType, long processingTimeMs, String operationId) {
        return new Builder(nodeId, nodeType)
                .operationType(OperationType.SKIP)
                .success(true)
                .timestamp(LocalDateTime.now())
                .processingTimeMs(processingTimeMs)
                .propertyCount(0)
                .operationId(operationId)
                .build();
    }

    // Convenience factory methods for tests
    public static UpsertResult inserted(String nodeId, String nodeType, long processingTimeMs, String operationId) {
        return new Builder(nodeId, nodeType)
                .operationType(OperationType.INSERT)
                .success(true)
                .timestamp(LocalDateTime.now())
                .processingTimeMs(processingTimeMs)
                .operationId(operationId)
                .build();
    }

    public static UpsertResult updated(String nodeId, String nodeType, long processingTimeMs, String operationId, java.util.List<com.vividcodes.graphrag.model.dto.NodeComparisonResult.PropertyChange> changes) {
        return new Builder(nodeId, nodeType)
                .operationType(OperationType.UPDATE)
                .success(true)
                .timestamp(LocalDateTime.now())
                .processingTimeMs(processingTimeMs)
                .propertyCount(changes != null ? changes.size() : 0)
                .operationId(operationId)
                .build();
    }

    public static UpsertResult skipped(String nodeId, String nodeType, long processingTimeMs, String operationId) {
        return skip(nodeId, nodeType, processingTimeMs, operationId);
    }

    // Getters
    public String getNodeId() {
        return nodeId;
    }

    public String getNodeType() {
        return nodeType;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public Map<String, Object> getChangedProperties() {
        return changedProperties;
    }

    public int getPropertyCount() {
        return propertyCount;
    }

    public String getOperationId() {
        return operationId;
    }

    // Helper methods
    public boolean isInsert() {
        return operationType == OperationType.INSERT;
    }

    public boolean isUpdate() {
        return operationType == OperationType.UPDATE;
    }

    public boolean isSkip() {
        return operationType == OperationType.SKIP;
    }

    @Override
    public String toString() {
        return String.format("UpsertResult{nodeId='%s', nodeType='%s', operation=%s, success=%s, processingTime=%dms, propertyCount=%d}",
                nodeId, nodeType, operationType, success, processingTimeMs, propertyCount);
    }

    // Builder pattern for flexible construction
    public static class Builder {
        private final String nodeId;
        private final String nodeType;
        private OperationType operationType;
        private boolean success;
        private String errorMessage;
        private LocalDateTime timestamp;
        private long processingTimeMs;
        private Map<String, Object> changedProperties;
        private int propertyCount;
        private String operationId;

        public Builder(String nodeId, String nodeType) {
            this.nodeId = nodeId;
            this.nodeType = nodeType;
        }

        public Builder operationType(OperationType operationType) {
            this.operationType = operationType;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder processingTimeMs(long processingTimeMs) {
            this.processingTimeMs = processingTimeMs;
            return this;
        }

        public Builder changedProperties(Map<String, Object> changedProperties) {
            this.changedProperties = changedProperties;
            return this;
        }

        public Builder propertyCount(int propertyCount) {
            this.propertyCount = propertyCount;
            return this;
        }

        public Builder operationId(String operationId) {
            this.operationId = operationId;
            return this;
        }

        public UpsertResult build() {
            return new UpsertResult(this);
        }
    }
}
