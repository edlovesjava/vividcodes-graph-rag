package com.vividcodes.graphrag.model.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for sub-project metadata.
 * Contains information about a sub-project within a repository,
 * including build configuration, directory structure, and dependencies.
 */
public class SubProjectMetadata {
    
    private String id;
    private String name;
    private String path;
    private String type; // "maven", "gradle", "npm", "custom"
    private String buildFile; // pom.xml, build.gradle, package.json
    private List<String> sourceDirectories;
    private List<String> testDirectories;
    private List<String> dependencies;
    private String description;
    private String version;
    private String repositoryId;
    private LocalDateTime detectedAt;
    
    /**
     * Default constructor
     */
    public SubProjectMetadata() {
        this.detectedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with required fields
     */
    public SubProjectMetadata(String name, String path, String type) {
        this();
        this.name = name;
        this.path = path;
        this.type = type;
        this.id = generateId(path, type);
    }
    
    /**
     * Constructor with all basic fields
     */
    public SubProjectMetadata(String name, String path, String type, String repositoryId) {
        this(name, path, type);
        this.repositoryId = repositoryId;
    }
    
    /**
     * Generate a unique ID for the sub-project based on path and type
     */
    private String generateId(String path, String type) {
        if (path == null || type == null) {
            return null;
        }
        // Create a unique ID by combining path and type
        String normalizedPath = path.replace("/", "_").replace("\\", "_");
        return String.format("%s_%s_%d", normalizedPath, type, System.currentTimeMillis());
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
        // Regenerate ID if path changes
        if (this.type != null) {
            this.id = generateId(path, this.type);
        }
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
        // Regenerate ID if type changes
        if (this.path != null) {
            this.id = generateId(this.path, type);
        }
    }
    
    public String getBuildFile() {
        return buildFile;
    }
    
    public void setBuildFile(String buildFile) {
        this.buildFile = buildFile;
    }
    
    public List<String> getSourceDirectories() {
        return sourceDirectories;
    }
    
    public void setSourceDirectories(List<String> sourceDirectories) {
        this.sourceDirectories = sourceDirectories;
    }
    
    public List<String> getTestDirectories() {
        return testDirectories;
    }
    
    public void setTestDirectories(List<String> testDirectories) {
        this.testDirectories = testDirectories;
    }
    
    public List<String> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getRepositoryId() {
        return repositoryId;
    }
    
    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }
    
    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }
    
    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }
    
    /**
     * Check if this sub-project has valid basic information
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               path != null && !path.trim().isEmpty() &&
               type != null && !type.trim().isEmpty();
    }
    
    /**
     * Check if this is a Maven project
     */
    public boolean isMavenProject() {
        return "maven".equalsIgnoreCase(type) || 
               (buildFile != null && buildFile.endsWith("pom.xml"));
    }
    
    /**
     * Check if this is a Gradle project
     */
    public boolean isGradleProject() {
        return "gradle".equalsIgnoreCase(type) ||
               (buildFile != null && (buildFile.endsWith("build.gradle") || buildFile.endsWith("build.gradle.kts")));
    }
    
    /**
     * Check if this is an NPM project
     */
    public boolean isNpmProject() {
        return "npm".equalsIgnoreCase(type) ||
               (buildFile != null && buildFile.endsWith("package.json"));
    }
    
    /**
     * Get the relative path within the repository
     */
    public String getRelativePath() {
        if (path == null) {
            return null;
        }
        // Remove leading slashes and normalize
        String normalized = path.replaceAll("^[/\\\\]+", "").replace("\\", "/");
        // Replace multiple consecutive slashes with single slash
        normalized = normalized.replaceAll("/+", "/");
        return normalized;
    }
    
    @Override
    public String toString() {
        return "SubProjectMetadata{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", type='" + type + '\'' +
                ", buildFile='" + buildFile + '\'' +
                ", version='" + version + '\'' +
                ", repositoryId='" + repositoryId + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SubProjectMetadata that = (SubProjectMetadata) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
