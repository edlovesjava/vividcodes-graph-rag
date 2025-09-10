package com.vividcodes.graphrag.util;

import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vividcodes.graphrag.service.NodeIdentifierService.NodeType;

/**
 * Utility class for validating node identifiers according to their expected formats.
 * Provides additional validation beyond the basic format checking in NodeIdentifierService.
 * 
 * @author Graph RAG System
 * @version 1.0
 * @since 2024
 */
public final class NodeIdValidator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeIdValidator.class);
    
    // Regex patterns for different node ID formats
    private static final Pattern CLASS_PATTERN = Pattern.compile("^class:[^:]*:[^:]+$");
    private static final Pattern METHOD_PATTERN = Pattern.compile("^method:[^:]*:[^:]+:[^:]+:[a-f0-9]+$");
    private static final Pattern FIELD_PATTERN = Pattern.compile("^field:[^:]*:[^:]+:[^:]+:[^:]*$");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^package:[^:]*$");
    private static final Pattern REPOSITORY_PATTERN = Pattern.compile("^repo:[^:]+:[a-f0-9]+$");
    private static final Pattern SUBPROJECT_PATTERN = Pattern.compile("^subproject:repo:[^:]+:[a-f0-9]+:[^:]*$");
    private static final Pattern ANNOTATION_PATTERN = Pattern.compile("^annotation:[^:]+$");
    private static final Pattern UPSERT_AUDIT_PATTERN = Pattern.compile("^upsert_audit:[^:]+:[^:]+:[^:]+$");
    
    private NodeIdValidator() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Validate a node ID using strict format validation.
     * 
     * @param nodeId The node ID to validate
     * @param nodeType The expected node type
     * @return ValidationResult containing validation status and details
     */
    public static ValidationResult validateStrict(String nodeId, NodeType nodeType) {
        if (nodeId == null || nodeId.trim().isEmpty()) {
            return ValidationResult.invalid("Node ID cannot be null or empty");
        }
        
        Pattern pattern = getPatternForNodeType(nodeType);
        if (pattern == null) {
            return ValidationResult.invalid("Unknown node type: " + nodeType);
        }
        
        boolean matches = pattern.matcher(nodeId).matches();
        
        if (matches) {
            LOGGER.debug("Valid node ID: {} for type: {}", nodeId, nodeType);
            return ValidationResult.valid();
        } else {
            String message = String.format("Invalid format for %s node ID: %s", nodeType, nodeId);
            LOGGER.debug(message);
            return ValidationResult.invalid(message);
        }
    }
    
    /**
     * Check for potential ID collisions by analyzing ID components.
     * 
     * @param nodeId The node ID to analyze
     * @param nodeType The node type
     * @return CollisionAnalysis containing collision risk assessment
     */
    public static CollisionAnalysis analyzeCollisionRisk(String nodeId, NodeType nodeType) {
        ValidationResult validation = validateStrict(nodeId, nodeType);
        if (!validation.isValid()) {
            return CollisionAnalysis.invalid("Cannot analyze invalid ID");
        }
        
        switch (nodeType) {
            case CLASS:
                return analyzeClassCollisionRisk(nodeId);
            case METHOD:
                return analyzeMethodCollisionRisk(nodeId);
            case FIELD:
                return analyzeFieldCollisionRisk(nodeId);
            case PACKAGE:
                return analyzePackageCollisionRisk(nodeId);
            case REPOSITORY:
                return analyzeRepositoryCollisionRisk(nodeId);
            case SUBPROJECT:
                return analyzeSubProjectCollisionRisk(nodeId);
            case ANNOTATION:
                return analyzeAnnotationCollisionRisk(nodeId);
            case UPSERT_AUDIT:
                return CollisionAnalysis.low("Audit IDs include timestamp and operation ID");
            default:
                return CollisionAnalysis.unknown("Unknown node type");
        }
    }
    
    /**
     * Extract node type from node ID prefix.
     * 
     * @param nodeId The node ID to analyze
     * @return NodeType if recognized, null otherwise
     */
    public static NodeType extractNodeType(String nodeId) {
        if (nodeId == null || nodeId.trim().isEmpty()) {
            return null;
        }
        
        if (nodeId.startsWith("class:")) return NodeType.CLASS;
        if (nodeId.startsWith("method:")) return NodeType.METHOD;
        if (nodeId.startsWith("field:")) return NodeType.FIELD;
        if (nodeId.startsWith("package:")) return NodeType.PACKAGE;
        if (nodeId.startsWith("repo:")) return NodeType.REPOSITORY;
        if (nodeId.startsWith("subproject:")) return NodeType.SUBPROJECT;
        if (nodeId.startsWith("annotation:")) return NodeType.ANNOTATION;
        if (nodeId.startsWith("upsert_audit:")) return NodeType.UPSERT_AUDIT;
        
        return null;
    }
    
    // Private utility methods
    
    private static Pattern getPatternForNodeType(NodeType nodeType) {
        switch (nodeType) {
            case CLASS: return CLASS_PATTERN;
            case METHOD: return METHOD_PATTERN;
            case FIELD: return FIELD_PATTERN;
            case PACKAGE: return PACKAGE_PATTERN;
            case REPOSITORY: return REPOSITORY_PATTERN;
            case SUBPROJECT: return SUBPROJECT_PATTERN;
            case ANNOTATION: return ANNOTATION_PATTERN;
            case UPSERT_AUDIT: return UPSERT_AUDIT_PATTERN;
            default: return null;
        }
    }
    
    private static CollisionAnalysis analyzeClassCollisionRisk(String nodeId) {
        String[] parts = nodeId.split(":", 3);
        if (parts.length < 3) {
            return CollisionAnalysis.high("Malformed class ID");
        }
        
        String packageName = parts[1];
        String className = parts[2];
        
        if (packageName.isEmpty() && className.length() < 3) {
            return CollisionAnalysis.medium("Short class name in default package");
        } else if (className.length() < 2) {
            return CollisionAnalysis.medium("Very short class name");
        } else {
            return CollisionAnalysis.low("Well-formed package and class name");
        }
    }
    
    private static CollisionAnalysis analyzeMethodCollisionRisk(String nodeId) {
        String[] parts = nodeId.split(":", 5);
        if (parts.length < 5) {
            return CollisionAnalysis.high("Malformed method ID");
        }
        
        String methodName = parts[3];
        String paramHash = parts[4];
        
        if (methodName.length() < 2) {
            return CollisionAnalysis.medium("Very short method name");
        } else if (paramHash.length() < 8) {
            return CollisionAnalysis.medium("Short parameter hash");
        } else {
            return CollisionAnalysis.low("Well-formed method signature with hash");
        }
    }
    
    private static CollisionAnalysis analyzeFieldCollisionRisk(String nodeId) {
        String[] parts = nodeId.split(":", 5);
        if (parts.length < 5) {
            return CollisionAnalysis.high("Malformed field ID");
        }
        
        String fieldName = parts[3];
        
        if (fieldName.length() < 2) {
            return CollisionAnalysis.medium("Very short field name");
        } else {
            return CollisionAnalysis.low("Well-formed field identifier");
        }
    }
    
    private static CollisionAnalysis analyzePackageCollisionRisk(String nodeId) {
        String packageName = nodeId.substring("package:".length());
        
        if (packageName.isEmpty()) {
            return CollisionAnalysis.high("Default package has high collision risk");
        } else if (packageName.split("\\.").length < 2) {
            return CollisionAnalysis.medium("Single-level package name");
        } else {
            return CollisionAnalysis.low("Multi-level package name");
        }
    }
    
    private static CollisionAnalysis analyzeRepositoryCollisionRisk(String nodeId) {
        String[] parts = nodeId.split(":", 3);
        if (parts.length < 3) {
            return CollisionAnalysis.high("Malformed repository ID");
        }
        
        String repoName = parts[1];
        String pathHash = parts[2];
        
        if (repoName.length() < 3) {
            return CollisionAnalysis.medium("Short repository name");
        } else if (pathHash.length() < 8) {
            return CollisionAnalysis.medium("Short path hash");
        } else {
            return CollisionAnalysis.low("Well-formed repository identifier with path hash");
        }
    }
    
    private static CollisionAnalysis analyzeSubProjectCollisionRisk(String nodeId) {
        if (!nodeId.startsWith("subproject:repo:")) {
            return CollisionAnalysis.high("Malformed subproject ID");
        }
        
        String remaining = nodeId.substring("subproject:".length());
        String[] parts = remaining.split(":", 4);
        
        if (parts.length < 4) {
            return CollisionAnalysis.medium("Short subproject path");
        } else {
            return CollisionAnalysis.low("Well-formed subproject identifier");
        }
    }
    
    private static CollisionAnalysis analyzeAnnotationCollisionRisk(String nodeId) {
        String annotationName = nodeId.substring("annotation:".length());
        
        if (!annotationName.contains(".")) {
            return CollisionAnalysis.medium("Annotation without package qualifier");
        } else if (annotationName.length() < 5) {
            return CollisionAnalysis.medium("Very short annotation name");
        } else {
            return CollisionAnalysis.low("Well-formed fully qualified annotation name");
        }
    }
    
    /**
     * Result of node ID validation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * Analysis of collision risk for a node ID.
     */
    public static class CollisionAnalysis {
        public enum RiskLevel {
            LOW, MEDIUM, HIGH, UNKNOWN
        }
        
        private final RiskLevel riskLevel;
        private final String analysis;
        
        private CollisionAnalysis(RiskLevel riskLevel, String analysis) {
            this.riskLevel = riskLevel;
            this.analysis = analysis;
        }
        
        public static CollisionAnalysis low(String analysis) {
            return new CollisionAnalysis(RiskLevel.LOW, analysis);
        }
        
        public static CollisionAnalysis medium(String analysis) {
            return new CollisionAnalysis(RiskLevel.MEDIUM, analysis);
        }
        
        public static CollisionAnalysis high(String analysis) {
            return new CollisionAnalysis(RiskLevel.HIGH, analysis);
        }
        
        public static CollisionAnalysis unknown(String analysis) {
            return new CollisionAnalysis(RiskLevel.UNKNOWN, analysis);
        }
        
        public static CollisionAnalysis invalid(String analysis) {
            return new CollisionAnalysis(RiskLevel.HIGH, analysis);
        }
        
        public RiskLevel getRiskLevel() {
            return riskLevel;
        }
        
        public String getAnalysis() {
            return analysis;
        }
    }
}
