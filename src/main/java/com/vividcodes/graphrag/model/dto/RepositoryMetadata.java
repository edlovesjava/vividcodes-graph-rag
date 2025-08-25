package com.vividcodes.graphrag.model.dto;

import java.time.LocalDateTime;

public class RepositoryMetadata {
    private String repositoryId;
    private String repositoryName;
    private String repositoryUrl;
    private String organization;
    private String branch;
    private String commitHash;
    private LocalDateTime commitDate;
    private String fileRelativePath;
    private String localPath;
    
    public RepositoryMetadata() {
    }
    
    public RepositoryMetadata(String repositoryName, String localPath) {
        this.repositoryName = repositoryName;
        this.localPath = localPath;
    }
    
    // Getters and Setters
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
    
    public String getOrganization() {
        return organization;
    }
    
    public void setOrganization(String organization) {
        this.organization = organization;
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
    
    public String getLocalPath() {
        return localPath;
    }
    
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
}
