package com.vividcodes.graphrag.model.graph;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

/**
 * Graph node representing a sub-project within a repository.
 * Sub-projects are identified by common project indicators like build files
 * (pom.xml, build.gradle, package.json) and source directory structures.
 */
@Node("SubProject")
public class SubProjectNode {
    
    @Id
    private String id;
    
    @Property("name")
    private String name;
    
    @Property("path")
    private String path;
    
    @Property("type")
    private String type; // "maven", "gradle", "npm", "custom"
    
    @Property("buildFile")
    private String buildFile; // pom.xml, build.gradle, package.json
    
    @Property("sourceDirectories")
    private List<String> sourceDirectories;
    
    @Property("testDirectories")
    private List<String> testDirectories;
    
    @Property("dependencies")
    private List<String> dependencies;
    
    @Property("description")
    private String description;
    
    @Property("version")
    private String version;
    
    @Property("created_at")
    private LocalDateTime createdAt;
    
    @Property("updated_at")
    private LocalDateTime updatedAt;
    
    @Property("health_score")
    private Float healthScore;
    
    @Property("complexity_score")
    private Float complexityScore;
    
    @Property("maintainability_score")
    private Float maintainabilityScore;
    
    @Property("repository_id")
    private String repositoryId;
    
    /**
     * Default constructor
     */
    public SubProjectNode() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with basic required fields
     */
    public SubProjectNode(String id, String name, String path, String type) {
        this();
        this.id = id;
        this.name = name;
        this.path = path;
        this.type = type;
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
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getBuildFile() {
        return buildFile;
    }
    
    public void setBuildFile(String buildFile) {
        this.buildFile = buildFile;
        this.updatedAt = LocalDateTime.now();
    }
    
    public List<String> getSourceDirectories() {
        return sourceDirectories;
    }
    
    public void setSourceDirectories(List<String> sourceDirectories) {
        this.sourceDirectories = sourceDirectories;
        this.updatedAt = LocalDateTime.now();
    }
    
    public List<String> getTestDirectories() {
        return testDirectories;
    }
    
    public void setTestDirectories(List<String> testDirectories) {
        this.testDirectories = testDirectories;
        this.updatedAt = LocalDateTime.now();
    }
    
    public List<String> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
        this.updatedAt = LocalDateTime.now();
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
    
    public Float getHealthScore() {
        return healthScore;
    }
    
    public void setHealthScore(Float healthScore) {
        this.healthScore = healthScore;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Float getComplexityScore() {
        return complexityScore;
    }
    
    public void setComplexityScore(Float complexityScore) {
        this.complexityScore = complexityScore;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Float getMaintainabilityScore() {
        return maintainabilityScore;
    }
    
    public void setMaintainabilityScore(Float maintainabilityScore) {
        this.maintainabilityScore = maintainabilityScore;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getRepositoryId() {
        return repositoryId;
    }
    
    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "SubProjectNode{" +
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
        SubProjectNode that = (SubProjectNode) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
