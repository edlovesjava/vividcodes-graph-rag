package com.vividcodes.graphrag.service;

import java.util.List;

/**
 * Service interface for generating consistent, unique identifiers for all node types.
 * This service centralizes ID generation logic and ensures consistency across the application.
 * 
 * @author Graph RAG System
 * @version 1.0
 * @since 2024
 */
public interface NodeIdentifierService {

    /**
     * Generate unique ID for a class node.
     * Format: "class:{packageName}:{className}"
     * 
     * @param packageName The package containing the class (can be empty for default package)
     * @param className The simple class name
     * @return Unique class identifier
     * @throws IllegalArgumentException if className is null or empty
     */
    String generateClassId(String packageName, String className);

    /**
     * Generate unique ID for a method node.
     * Format: "method:{packageName}:{className}:{methodName}:{paramHash}"
     * 
     * @param classId The unique identifier of the containing class
     * @param methodName The method name
     * @param parameterTypes List of parameter type names for method signature uniqueness
     * @return Unique method identifier
     * @throws IllegalArgumentException if classId or methodName is null or empty
     */
    String generateMethodId(String classId, String methodName, List<String> parameterTypes);

    /**
     * Generate unique ID for a field node.
     * Format: "field:{packageName}:{className}:{fieldName}:{fieldType}"
     * 
     * @param classId The unique identifier of the containing class
     * @param fieldName The field name
     * @param fieldType The field type (for overloading support if needed)
     * @return Unique field identifier
     * @throws IllegalArgumentException if classId or fieldName is null or empty
     */
    String generateFieldId(String classId, String fieldName, String fieldType);

    /**
     * Generate unique ID for a package node.
     * Format: "package:{normalizedPath}:{packageName}"
     * 
     * @param packageName The package name (e.g., "com.example.service")
     * @return Unique package identifier
     * @throws IllegalArgumentException if packageName is null
     */
    String generatePackageId(String packageName);

    /**
     * Generate unique ID for a repository node.
     * Format: "repo:{repositoryName}:{pathHash}"
     * 
     * @param repositoryPath The local path to the repository
     * @param repositoryName The repository name
     * @return Unique repository identifier
     * @throws IllegalArgumentException if repositoryName or repositoryPath is null or empty
     */
    String generateRepositoryId(String repositoryPath, String repositoryName);

    /**
     * Generate unique ID for a sub-project node.
     * Format: "subproject:{repositoryId}:{normalizedPath}"
     * 
     * @param repositoryId The unique identifier of the parent repository
     * @param subProjectPath The relative path of the sub-project within the repository
     * @return Unique sub-project identifier
     * @throws IllegalArgumentException if repositoryId or subProjectPath is null or empty
     */
    String generateSubProjectId(String repositoryId, String subProjectPath);

    /**
     * Generate unique ID for an annotation node.
     * Format: "annotation:{fullyQualifiedName}" or "annotation:{name}" if FQN not available
     * 
     * @param packageName The package containing the annotation
     * @param annotationName The simple annotation name
     * @return Unique annotation identifier
     * @throws IllegalArgumentException if annotationName is null or empty
     */
    String generateAnnotationId(String packageName, String annotationName);

    /**
     * Generate unique ID for an upsert audit node.
     * Format: "upsert_audit:{operationId}:{nodeType}:{nodeId}"
     * 
     * @param operationId The unique operation identifier
     * @param nodeType The type of node being audited
     * @param nodeId The ID of the node being audited
     * @return Unique upsert audit identifier
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    String generateUpsertAuditId(String operationId, String nodeType, String nodeId);

    // ID Validation Methods

    /**
     * Validate that a node ID conforms to the expected format for its type.
     * 
     * @param nodeId The node ID to validate
     * @param nodeType The expected node type
     * @return true if the ID is valid for the given node type
     */
    boolean validateNodeId(String nodeId, NodeType nodeType);

    /**
     * Check if two nodes have consistent IDs (same node, same ID).
     * 
     * @param existingNode The existing node
     * @param newNode The new node to compare
     * @return true if the nodes have consistent identifiers
     */
    boolean isConsistentId(Object existingNode, Object newNode);

    // ID Normalization Methods

    /**
     * Normalize a method signature for consistent ID generation.
     * Handles parameter type normalization, ordering, etc.
     * 
     * @param methodName The method name
     * @param parameterTypes List of parameter types
     * @return Normalized method signature string
     */
    String normalizeMethodSignature(String methodName, List<String> parameterTypes);

    /**
     * Normalize a package name for consistent ID generation.
     * Handles case sensitivity, special characters, etc.
     * 
     * @param packageName The package name to normalize
     * @return Normalized package name
     */
    String normalizePackageName(String packageName);

    /**
     * Normalize a file path for consistent ID generation.
     * Handles path separators, relative paths, etc.
     * 
     * @param filePath The file path to normalize
     * @return Normalized file path
     */
    String normalizeFilePath(String filePath);
    
    /**
     * Extract the node type from a node ID based on its format.
     * 
     * @param nodeId The node ID to analyze
     * @return The node type, or null if the format is not recognized
     */
    NodeType extractNodeType(String nodeId);

    /**
     * Enum defining the types of nodes for validation purposes.
     */
    enum NodeType {
        CLASS,
        METHOD,
        FIELD,
        PACKAGE,
        REPOSITORY,
        SUBPROJECT,
        ANNOTATION,
        UPSERT_AUDIT
    }
}
