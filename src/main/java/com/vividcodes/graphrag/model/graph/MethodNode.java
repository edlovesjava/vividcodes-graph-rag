package com.vividcodes.graphrag.model.graph;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Method")
public class MethodNode {
    
    @Id
    private String id;
    
    @Property("name")
    private String name;
    
    @Property("visibility")
    private String visibility;
    
    @Property("modifiers")
    private List<String> modifiers;
    
    @Property("return_type")
    private String returnType;
    
    @Property("parameters")
    private List<String> parameters;
    
    @Property("parameter_names")
    private List<String> parameterNames;
    
    @Property("file_path")
    private String filePath;
    
    @Property("line_start")
    private Integer lineStart;
    
    @Property("line_end")
    private Integer lineEnd;
    
    @Property("class_name")
    private String className;
    
    @Property("package_name")
    private String packageName;
    
    @Property("created_at")
    private LocalDateTime createdAt;
    
    @Property("updated_at")
    private LocalDateTime updatedAt;
    
    public MethodNode() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public MethodNode(String name, String className, String packageName, String filePath) {
        this();
        this.name = name;
        this.className = className;
        this.packageName = packageName;
        this.filePath = filePath;
        this.id = generateId(name, className, packageName);
    }
    
    private String generateId(String name, String className, String packageName) {
        return "method:" + packageName + ":" + className + ":" + name;
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
        return modifiers != null ? Collections.unmodifiableList(modifiers) : Collections.emptyList();
    }
    
    public void setModifiers(List<String> modifiers) {
        this.modifiers = modifiers != null ? new ArrayList<>(modifiers) : new ArrayList<>();
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
    
    public List<String> getParameters() {
        return parameters != null ? Collections.unmodifiableList(parameters) : Collections.emptyList();
    }
    
    public void setParameters(List<String> parameters) {
        this.parameters = parameters != null ? new ArrayList<>(parameters) : new ArrayList<>();
    }
    
    public List<String> getParameterNames() {
        return parameterNames != null ? Collections.unmodifiableList(parameterNames) : Collections.emptyList();
    }
    
    public void setParameterNames(List<String> parameterNames) {
        this.parameterNames = parameterNames != null ? new ArrayList<>(parameterNames) : new ArrayList<>();
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public Integer getLineStart() {
        return lineStart;
    }
    
    public void setLineStart(Integer lineStart) {
        this.lineStart = lineStart;
    }
    
    public Integer getLineEnd() {
        return lineEnd;
    }
    
    public void setLineEnd(Integer lineEnd) {
        this.lineEnd = lineEnd;
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