package com.vividcodes.graphrag.model.graph;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Id;

import java.time.LocalDateTime;

@Node("Package")
public class PackageNode {
    
    @Id
    private String id;
    
    @Property("name")
    private String name;
    
    @Property("path")
    private String path;
    
    @Property("file_path")
    private String filePath;
    
    @Property("line_start")
    private Integer lineStart;
    
    @Property("line_end")
    private Integer lineEnd;
    
    @Property("created_at")
    private LocalDateTime createdAt;
    
    @Property("updated_at")
    private LocalDateTime updatedAt;
    
    public PackageNode() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public PackageNode(String name, String path, String filePath) {
        this();
        this.name = name;
        this.path = path;
        this.filePath = filePath;
        this.id = generateId(name, path);
    }
    
    private String generateId(String name, String path) {
        return "package:" + path + ":" + name;
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
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
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