package com.vividcodes.graphrag.model.graph;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Class")
public class ClassNode {
    
    @Id
    private String id;
    
    @Property("name")
    private String name;
    
    @Property("visibility")
    private String visibility;
    
    @Property("modifiers")
    private List<String> modifiers;
    
    @Property("is_interface")
    private Boolean isInterface;
    
    @Property("is_enum")
    private Boolean isEnum;
    
    @Property("is_annotation")
    private Boolean isAnnotation;
    
    @Property("file_path")
    private String filePath;
    
    @Property("line_start")
    private Integer lineStart;
    
    @Property("line_end")
    private Integer lineEnd;
    
    @Property("package_name")
    private String packageName;
    
    @Property("repository_id")
    private String repositoryId;
    
    @Property("repository_name")
    private String repositoryName;
    
    @Property("repository_url")
    private String repositoryUrl;
    
    @Property("branch")
    private String branch;
    
    @Property("commit_hash")
    private String commitHash;
    
    @Property("commit_date")
    private LocalDateTime commitDate;
    
    @Property("file_relative_path")
    private String fileRelativePath;
    
    @Property("created_at")
    private LocalDateTime createdAt;
    
    @Property("updated_at")
    private LocalDateTime updatedAt;
    
    public ClassNode() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public ClassNode(String name, String packageName, String filePath) {
        this();
        this.name = name;
        this.packageName = packageName;
        this.filePath = filePath;
        this.id = generateId(name, packageName);
    }
    
    private String generateId(String name, String packageName) {
        return "class:" + packageName + ":" + name;
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
    
    public Boolean getIsInterface() {
        return isInterface;
    }
    
    public void setIsInterface(Boolean isInterface) {
        this.isInterface = isInterface;
    }
    
    public Boolean getIsEnum() {
        return isEnum;
    }
    
    public void setIsEnum(Boolean isEnum) {
        this.isEnum = isEnum;
    }
    
    public Boolean getIsAnnotation() {
        return isAnnotation;
    }
    
    public void setIsAnnotation(Boolean isAnnotation) {
        this.isAnnotation = isAnnotation;
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
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public String getRepositoryId() {
        return repositoryId;
    }
    
    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }
    
    public String getRepositoryName() {
        return repositoryName;
    }
    
    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }
    
    public String getRepositoryUrl() {
        return repositoryUrl;
    }
    
    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }
    
    public String getBranch() {
        return branch;
    }
    
    public void setBranch(String branch) {
        this.branch = branch;
    }
    
    public String getCommitHash() {
        return commitHash;
    }
    
    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }
    
    public LocalDateTime getCommitDate() {
        return commitDate;
    }
    
    public void setCommitDate(LocalDateTime commitDate) {
        this.commitDate = commitDate;
    }
    
    public String getFileRelativePath() {
        return fileRelativePath;
    }
    
    public void setFileRelativePath(String fileRelativePath) {
        this.fileRelativePath = fileRelativePath;
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