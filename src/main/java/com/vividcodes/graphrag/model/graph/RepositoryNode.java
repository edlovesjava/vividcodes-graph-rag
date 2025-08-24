package com.vividcodes.graphrag.model.graph;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Repository")
public class RepositoryNode {
    
    @Id
    private String id;
    
    @Property("name")
    private String name;
    
    @Property("organization")
    private String organization;
    
    @Property("url")
    private String url;
    
    @Property("clone_url")
    private String cloneUrl;
    
    @Property("default_branch")
    private String defaultBranch;
    
    @Property("created_at")
    private LocalDateTime createdAt;
    
    @Property("updated_at")
    private LocalDateTime updatedAt;
    
    @Property("last_commit_hash")
    private String lastCommitHash;
    
    @Property("last_commit_date")
    private LocalDateTime lastCommitDate;
    
    @Property("total_files")
    private Integer totalFiles;
    
    @Property("language_stats")
    private Map<String, Object> languageStats;
    
    @Property("local_path")
    private String localPath;
    
    public RepositoryNode() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public RepositoryNode(String name, String localPath) {
        this();
        this.name = name;
        this.localPath = localPath;
        this.id = generateId(name, localPath);
    }
    
    private String generateId(String name, String localPath) {
        return "repo:" + name + ":" + localPath.hashCode();
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
    
    public String getOrganization() {
        return organization;
    }
    
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getCloneUrl() {
        return cloneUrl;
    }
    
    public void setCloneUrl(String cloneUrl) {
        this.cloneUrl = cloneUrl;
    }
    
    public String getDefaultBranch() {
        return defaultBranch;
    }
    
    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
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
    
    public String getLastCommitHash() {
        return lastCommitHash;
    }
    
    public void setLastCommitHash(String lastCommitHash) {
        this.lastCommitHash = lastCommitHash;
    }
    
    public LocalDateTime getLastCommitDate() {
        return lastCommitDate;
    }
    
    public void setLastCommitDate(LocalDateTime lastCommitDate) {
        this.lastCommitDate = lastCommitDate;
    }
    
    public Integer getTotalFiles() {
        return totalFiles;
    }
    
    public void setTotalFiles(Integer totalFiles) {
        this.totalFiles = totalFiles;
    }
    
    public Map<String, Object> getLanguageStats() {
        return languageStats;
    }
    
    public void setLanguageStats(Map<String, Object> languageStats) {
        this.languageStats = languageStats;
    }
    
    public String getLocalPath() {
        return localPath;
    }
    
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
}
