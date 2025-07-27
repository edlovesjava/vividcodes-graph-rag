package com.vividcodes.graphrag.model.graph;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Id;

import java.time.LocalDateTime;
import java.util.List;

@Node("Field")
public class FieldNode {
    
    @Id
    private String id;
    
    @Property("name")
    private String name;
    
    @Property("visibility")
    private String visibility;
    
    @Property("modifiers")
    private List<String> modifiers;
    
    @Property("type")
    private String type;
    
    @Property("file_path")
    private String filePath;
    
    @Property("line_number")
    private Integer lineNumber;
    
    @Property("class_name")
    private String className;
    
    @Property("package_name")
    private String packageName;
    
    @Property("created_at")
    private LocalDateTime createdAt;
    
    @Property("updated_at")
    private LocalDateTime updatedAt;
    
    public FieldNode() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public FieldNode(String name, String className, String packageName, String filePath) {
        this();
        this.name = name;
        this.className = className;
        this.packageName = packageName;
        this.filePath = filePath;
        this.id = generateId(name, className, packageName);
    }
    
    private String generateId(String name, String className, String packageName) {
        return "field:" + packageName + ":" + className + ":" + name;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVisibility() {
        return visibility;
    }
    
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
    
    public List<String> getModifiers() {
        return modifiers;
    }
    
    public void setModifiers(List<String> modifiers) {
        this.modifiers = modifiers;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public Integer getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 