package com.vividcodes.graphrag.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vividcodes.graphrag.model.graph.AnnotationNode;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.FieldNode;
import com.vividcodes.graphrag.model.graph.MethodNode;
import com.vividcodes.graphrag.model.graph.PackageNode;
import com.vividcodes.graphrag.model.graph.RepositoryNode;
import com.vividcodes.graphrag.model.graph.SubProjectNode;
import com.vividcodes.graphrag.model.graph.UpsertAuditNode;

/**
 * Implementation of NodeIdentifierService that provides consistent,
 * unique identifier generation for all node types in the graph database.
 * 
 * This service centralizes ID generation logic, handles normalization,
 * validation, and ensures cross-repository uniqueness.
 * 
 * @author Graph RAG System
 * @version 1.0
 * @since 2024
 */
@Service
public class NodeIdentifierServiceImpl implements NodeIdentifierService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeIdentifierServiceImpl.class);
    
    // ID prefixes for different node types
    private static final String CLASS_PREFIX = "class:";
    private static final String METHOD_PREFIX = "method:";
    private static final String FIELD_PREFIX = "field:";
    private static final String PACKAGE_PREFIX = "package:";
    private static final String REPOSITORY_PREFIX = "repo:";
    private static final String SUBPROJECT_PREFIX = "subproject:";
    private static final String ANNOTATION_PREFIX = "annotation:";
    private static final String UPSERT_AUDIT_PREFIX = "upsert_audit:";
    
    @Override
    public String generateClassId(String packageName, String className) {
        validateNotNullOrEmpty(className, "className");
        
        String normalizedPackage = normalizePackageName(packageName);
        String normalizedClass = normalizeSimpleName(className);
        
        String id = CLASS_PREFIX + normalizedPackage + ":" + normalizedClass;
        
        LOGGER.debug("Generated class ID: {} for package: {}, class: {}", id, packageName, className);
        return id;
    }
    
    @Override
    public String generateMethodId(String classId, String methodName, List<String> parameterTypes) {
        validateNotNullOrEmpty(classId, "classId");
        validateNotNullOrEmpty(methodName, "methodName");
        
        String normalizedMethodName = normalizeSimpleName(methodName);
        String parameterHash = generateParameterHash(parameterTypes);
        
        String id = METHOD_PREFIX + extractPackageFromClassId(classId) + ":" + 
                   extractClassNameFromClassId(classId) + ":" + normalizedMethodName + ":" + parameterHash;
        
        LOGGER.debug("Generated method ID: {} for class: {}, method: {}, parameters: {}", 
                    id, classId, methodName, parameterTypes);
        return id;
    }
    
    @Override
    public String generateFieldId(String classId, String fieldName, String fieldType) {
        validateNotNullOrEmpty(classId, "classId");
        validateNotNullOrEmpty(fieldName, "fieldName");
        
        String normalizedFieldName = normalizeSimpleName(fieldName);
        String normalizedFieldType = normalizeTypeName(fieldType);
        
        String id = FIELD_PREFIX + extractPackageFromClassId(classId) + ":" + 
                   extractClassNameFromClassId(classId) + ":" + normalizedFieldName + ":" + normalizedFieldType;
        
        LOGGER.debug("Generated field ID: {} for class: {}, field: {}, type: {}", 
                    id, classId, fieldName, fieldType);
        return id;
    }
    
    @Override
    public String generatePackageId(String packageName) {
        String normalizedPackage = normalizePackageName(packageName);
        String id = PACKAGE_PREFIX + normalizedPackage;
        
        LOGGER.debug("Generated package ID: {} for package: {}", id, packageName);
        return id;
    }
    
    @Override
    public String generateRepositoryId(String repositoryPath, String repositoryName) {
        validateNotNullOrEmpty(repositoryName, "repositoryName");
        validateNotNullOrEmpty(repositoryPath, "repositoryPath");
        
        String normalizedName = normalizeSimpleName(repositoryName);
        String pathHash = generatePathHash(repositoryPath);
        
        String id = REPOSITORY_PREFIX + normalizedName + ":" + pathHash;
        
        LOGGER.debug("Generated repository ID: {} for name: {}, path: {}", id, repositoryName, repositoryPath);
        return id;
    }
    
    @Override
    public String generateSubProjectId(String repositoryId, String subProjectPath) {
        validateNotNullOrEmpty(repositoryId, "repositoryId");
        validateNotNullOrEmpty(subProjectPath, "subProjectPath");
        
        String normalizedPath = normalizeFilePath(subProjectPath);
        String id = SUBPROJECT_PREFIX + repositoryId + ":" + normalizedPath;
        
        LOGGER.debug("Generated subproject ID: {} for repository: {}, path: {}", 
                    id, repositoryId, subProjectPath);
        return id;
    }
    
    @Override
    public String generateAnnotationId(String packageName, String annotationName) {
        validateNotNullOrEmpty(annotationName, "annotationName");
        
        String normalizedPackage = normalizePackageName(packageName);
        String normalizedAnnotation = normalizeSimpleName(annotationName);
        
        String fullyQualifiedName = normalizedPackage.isEmpty() ? 
                                   normalizedAnnotation : 
                                   normalizedPackage + "." + normalizedAnnotation;
        
        String id = ANNOTATION_PREFIX + fullyQualifiedName;
        
        LOGGER.debug("Generated annotation ID: {} for package: {}, annotation: {}", 
                    id, packageName, annotationName);
        return id;
    }
    
    @Override
    public String generateUpsertAuditId(String operationId, String nodeType, String nodeId) {
        validateNotNullOrEmpty(operationId, "operationId");
        validateNotNullOrEmpty(nodeType, "nodeType");
        validateNotNullOrEmpty(nodeId, "nodeId");
        
        String id = UPSERT_AUDIT_PREFIX + operationId + ":" + nodeType + ":" + nodeId;
        
        LOGGER.debug("Generated upsert audit ID: {} for operation: {}, nodeType: {}, nodeId: {}", 
                    id, operationId, nodeType, nodeId);
        return id;
    }
    
    @Override
    public boolean validateNodeId(String nodeId, NodeType nodeType) {
        if (nodeId == null || nodeId.trim().isEmpty()) {
            return false;
        }
        
        String expectedPrefix = getPrefixForNodeType(nodeType);
        boolean isValid = nodeId.startsWith(expectedPrefix);
        
        if (!isValid) {
            LOGGER.debug("Invalid node ID: {} for type: {}. Expected prefix: {}", 
                        nodeId, nodeType, expectedPrefix);
        }
        
        return isValid;
    }
    
    @Override
    public boolean isConsistentId(Object existingNode, Object newNode) {
        if (existingNode == null || newNode == null) {
            return false;
        }
        
        String existingId = extractIdFromNode(existingNode);
        String newId = extractIdFromNode(newNode);
        
        boolean isConsistent = Objects.equals(existingId, newId);
        
        if (!isConsistent) {
            LOGGER.debug("Inconsistent node IDs: existing={}, new={}", existingId, newId);
        }
        
        return isConsistent;
    }
    
    @Override
    public String normalizeMethodSignature(String methodName, List<String> parameterTypes) {
        if (methodName == null) {
            methodName = "";
        }
        
        if (parameterTypes == null || parameterTypes.isEmpty()) {
            return normalizeSimpleName(methodName) + "()";
        }
        
        String normalizedParams = parameterTypes.stream()
                .map(this::normalizeTypeName)
                .collect(Collectors.joining(","));
        
        return normalizeSimpleName(methodName) + "(" + normalizedParams + ")";
    }
    
    @Override
    public String normalizePackageName(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return "";
        }
        
        return packageName.trim().toLowerCase()
                         .replaceAll("[\\s\\\\]+", ".")
                         .replaceAll("\\.+", ".")
                         .replaceAll("^\\.|\\.$", "");
    }
    
    @Override
    public String normalizeFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return "";
        }
        
        return filePath.trim()
                      .replace('\\', '/')
                      .replaceAll("/+", "/")
                      .replaceAll("^/|/$", "");
    }
    
    // Private utility methods
    
    private void validateNotNullOrEmpty(String value, String paramName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + " cannot be null or empty");
        }
    }
    
    private String normalizeSimpleName(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().replaceAll("\\s+", "_");
    }
    
    private String normalizeTypeName(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            return "Object";
        }
        
        // Normalize generic types, arrays, etc.
        return typeName.trim()
                      .replaceAll("\\s+", "")
                      .replaceAll("<[^>]*>", "<T>") // Simplify generics
                      .replaceAll("\\[\\]", "[]");   // Normalize array syntax
    }
    
    private String generateParameterHash(List<String> parameterTypes) {
        if (parameterTypes == null || parameterTypes.isEmpty()) {
            return "void";
        }
        
        String paramString = parameterTypes.stream()
                .map(this::normalizeTypeName)
                .collect(Collectors.joining(","));
        
        // For readability, return short hash instead of full parameter list
        return generateShortHash(paramString);
    }
    
    private String generatePathHash(String path) {
        return generateShortHash(normalizeFilePath(path));
    }
    
    private String generateShortHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // Convert to short hex string (first 8 characters)
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(4, hashBytes.length); i++) {
                sb.append(String.format("%02x", hashBytes[i]));
            }
            return sb.toString();
            
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("MD5 not available, using hashCode fallback", e);
            return String.format("%08x", Math.abs(input.hashCode()));
        }
    }
    
    private String extractPackageFromClassId(String classId) {
        if (!classId.startsWith(CLASS_PREFIX)) {
            throw new IllegalArgumentException("Invalid class ID format: " + classId);
        }
        
        String[] parts = classId.substring(CLASS_PREFIX.length()).split(":", 2);
        return parts.length >= 1 ? parts[0] : "";
    }
    
    private String extractClassNameFromClassId(String classId) {
        if (!classId.startsWith(CLASS_PREFIX)) {
            throw new IllegalArgumentException("Invalid class ID format: " + classId);
        }
        
        String[] parts = classId.substring(CLASS_PREFIX.length()).split(":", 2);
        return parts.length >= 2 ? parts[1] : "";
    }
    
    private String getPrefixForNodeType(NodeType nodeType) {
        switch (nodeType) {
            case CLASS: return CLASS_PREFIX;
            case METHOD: return METHOD_PREFIX;
            case FIELD: return FIELD_PREFIX;
            case PACKAGE: return PACKAGE_PREFIX;
            case REPOSITORY: return REPOSITORY_PREFIX;
            case SUBPROJECT: return SUBPROJECT_PREFIX;
            case ANNOTATION: return ANNOTATION_PREFIX;
            case UPSERT_AUDIT: return UPSERT_AUDIT_PREFIX;
            default: throw new IllegalArgumentException("Unknown node type: " + nodeType);
        }
    }
    
    private String extractIdFromNode(Object node) {
        if (node instanceof ClassNode) {
            return ((ClassNode) node).getId();
        } else if (node instanceof MethodNode) {
            return ((MethodNode) node).getId();
        } else if (node instanceof FieldNode) {
            return ((FieldNode) node).getId();
        } else if (node instanceof PackageNode) {
            return ((PackageNode) node).getId();
        } else if (node instanceof RepositoryNode) {
            return ((RepositoryNode) node).getId();
        } else if (node instanceof SubProjectNode) {
            return ((SubProjectNode) node).getId();
        } else if (node instanceof AnnotationNode) {
            return ((AnnotationNode) node).getId();
        } else if (node instanceof UpsertAuditNode) {
            return ((UpsertAuditNode) node).getId();
        } else {
            LOGGER.warn("Unknown node type for ID extraction: {}", node.getClass().getSimpleName());
            return null;
        }
    }
    
    @Override
    public NodeType extractNodeType(String nodeId) {
        if (nodeId == null || nodeId.trim().isEmpty()) {
            return null;
        }
        
        // Extract node type based on ID prefix
        if (nodeId.startsWith("repository:")) {
            return NodeType.REPOSITORY;
        } else if (nodeId.startsWith("subproject:")) {
            return NodeType.SUBPROJECT;
        } else if (nodeId.startsWith("package:")) {
            return NodeType.PACKAGE;
        } else if (nodeId.startsWith("class:")) {
            return NodeType.CLASS;
        } else if (nodeId.startsWith("method:")) {
            return NodeType.METHOD;
        } else if (nodeId.startsWith("field:")) {
            return NodeType.FIELD;
        } else if (nodeId.startsWith("annotation:")) {
            return NodeType.ANNOTATION;
        } else if (nodeId.startsWith("upsert_audit:")) {
            return NodeType.UPSERT_AUDIT;
        } else {
            LOGGER.warn("Unknown node ID format: {}", nodeId);
            return null;
        }
    }
}
