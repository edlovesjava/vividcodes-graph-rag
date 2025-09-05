package com.vividcodes.graphrag.model.graph;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

/**
 * Graph node representing a Java annotation.
 * Captures annotation metadata including name, attributes, target type, and framework classification.
 */
@Node("Annotation")
public class AnnotationNode {
    
    @Id
    private String id;
    
    @Property("name")
    private String name; // Simple name (e.g., "Service")
    
    @Property("fullyQualifiedName")
    private String fullyQualifiedName; // Complete name (e.g., "org.springframework.stereotype.Service")
    
    @Property("attributes")
    private String attributes = "{}"; // JSON string representation of attributes
    
    @Property("targetType")
    private String targetType; // "class|method|field|parameter"
    
    @Property("isFramework")
    private Boolean isFramework; // true for Spring, JUnit, etc.
    
    @Property("frameworkType")
    private String frameworkType; // "spring|junit|validation|etc."
    
    @Property("created_at")
    private LocalDateTime createdAt;
    
    @Property("updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor.
     */
    public AnnotationNode() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.attributes = "{}";
        this.isFramework = false;
    }
    
    /**
     * Constructor with basic required fields.
     * 
     * @param name The simple annotation name
     * @param fullyQualifiedName The fully qualified annotation name
     */
    public AnnotationNode(final String name, final String fullyQualifiedName) {
        this();
        this.name = name;
        this.fullyQualifiedName = fullyQualifiedName;
        this.id = generateId(name, fullyQualifiedName);
        
        // Auto-detect framework type
        this.frameworkType = detectFrameworkType(fullyQualifiedName);
        this.isFramework = this.frameworkType != null;
    }
    
    /**
     * Generate unique ID for the annotation node.
     * 
     * @param name The annotation name
     * @param fullyQualifiedName The fully qualified name
     * @return Unique annotation ID
     */
    private String generateId(final String name, final String fullyQualifiedName) {
        return "annotation:" + (fullyQualifiedName != null ? fullyQualifiedName : name);
    }
    
    /**
     * Detect framework type based on annotation package.
     * 
     * @param fullyQualifiedName The fully qualified annotation name
     * @return Framework type or null if not a known framework
     */
    private String detectFrameworkType(final String fullyQualifiedName) {
        if (fullyQualifiedName == null) {
            return null;
        }
        
        if (fullyQualifiedName.startsWith("org.springframework.")) {
            return "spring";
        } else if (fullyQualifiedName.startsWith("org.junit.") || 
                   fullyQualifiedName.startsWith("junit.")) {
            return "junit";
        } else if (fullyQualifiedName.startsWith("javax.validation.") ||
                   fullyQualifiedName.startsWith("jakarta.validation.")) {
            return "validation";
        } else if (fullyQualifiedName.startsWith("com.fasterxml.jackson.")) {
            return "jackson";
        } else if (fullyQualifiedName.startsWith("javax.persistence.") ||
                   fullyQualifiedName.startsWith("jakarta.persistence.")) {
            return "jpa";
        }
        
        return null;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }
    
    public void setFullyQualifiedName(final String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }
    
    /**
     * Get annotation attributes as a Map parsed from JSON.
     * 
     * @return Map of attribute name-value pairs
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getAttributes() {
        if (attributes == null || attributes.isEmpty() || "{}".equals(attributes)) {
            return new HashMap<String, String>();
        }
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(attributes, Map.class);
        } catch (Exception e) {
            // Fallback to empty map if JSON parsing fails
            return new HashMap<String, String>();
        }
    }
    
    /**
     * Get annotation attributes as raw JSON string for database storage.
     * 
     * @return JSON string representation of attributes
     */
    public String getAttributesAsJson() {
        return attributes == null ? "{}" : attributes;
    }
    
    /**
     * Set annotation attributes from a Map (serialized to JSON).
     * 
     * @param attributes Map of attribute name-value pairs
     */
    public void setAttributes(final Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            this.attributes = "{}";
            return;
        }
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            this.attributes = mapper.writeValueAsString(attributes);
        } catch (Exception e) {
            // Fallback to empty JSON
            this.attributes = "{}";
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Add an attribute to the annotation.
     * 
     * @param key Attribute name
     * @param value Attribute value
     */
    public void addAttribute(final String key, final String value) {
        try {
            // Parse existing JSON, add new attribute, serialize back
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, String> attrMap = mapper.readValue(this.attributes, Map.class);
            attrMap.put(key, value);
            this.attributes = mapper.writeValueAsString(attrMap);
        } catch (Exception e) {
            // Fallback: create simple JSON manually
            if ("{}".equals(this.attributes) || this.attributes == null) {
                this.attributes = "{\"" + key + "\":\"" + value.replace("\"", "\\\"")
                    .replace("\\", "\\\\") + "\"}";
            } else {
                // Simple append - not perfect JSON but functional for single attributes
                String escapedKey = key.replace("\"", "\\\"").replace("\\", "\\\\");
                String escapedValue = value.replace("\"", "\\\"").replace("\\", "\\\\");
                this.attributes = this.attributes.replace("}", ",\"" + escapedKey + "\":\"" + escapedValue + "\"}");
            }
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public void setTargetType(final String targetType) {
        this.targetType = targetType;
    }
    
    public Boolean getIsFramework() {
        return isFramework;
    }
    
    public void setIsFramework(final Boolean isFramework) {
        this.isFramework = isFramework;
    }
    
    public String getFrameworkType() {
        return frameworkType;
    }
    
    public void setFrameworkType(final String frameworkType) {
        this.frameworkType = frameworkType;
        this.isFramework = frameworkType != null;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "AnnotationNode{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", fullyQualifiedName='" + fullyQualifiedName + '\'' +
                ", targetType='" + targetType + '\'' +
                ", frameworkType='" + frameworkType + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
