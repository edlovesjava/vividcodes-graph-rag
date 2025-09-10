package com.vividcodes.graphrag.service;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vividcodes.graphrag.model.dto.RepositoryMetadata;
import com.vividcodes.graphrag.model.dto.SubProjectMetadata;
import com.vividcodes.graphrag.model.graph.RepositoryNode;
import com.vividcodes.graphrag.model.graph.SubProjectNode;

@Service
public class RepositoryService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryService.class);
    
    private final GitService gitService;
    private final GraphService graphService;
    private final SubProjectDetector subProjectDetector;
    
    // Cache for repository nodes to avoid duplicate creation
    private final Map<String, RepositoryNode> repositoryCache = new ConcurrentHashMap<>();
    
    @Autowired
    public RepositoryService(GitService gitService, GraphService graphService, SubProjectDetector subProjectDetector) {
        this.gitService = gitService;
        this.graphService = graphService;
        this.subProjectDetector = subProjectDetector;
    }
    
    /**
     * Detect repository information from a file path
     */
    public RepositoryMetadata detectRepositoryMetadata(Path filePath) {
        return gitService.createRepositoryMetadata(filePath);
    }
    
    /**
     * Create or update repository node
     */
    public RepositoryNode createOrUpdateRepository(RepositoryMetadata metadata) {
        if (metadata == null) {
            LOGGER.debug("No repository metadata provided, skipping repository creation");
            return null;
        }
        
        String cacheKey = metadata.getRepositoryName() + ":" + metadata.getLocalPath();
        
        // Check cache first
        if (repositoryCache.containsKey(cacheKey)) {
            LOGGER.debug("Using cached repository node for: {}", cacheKey);
            return repositoryCache.get(cacheKey);
        }
        
        // Create new repository node
        RepositoryNode repositoryNode = new RepositoryNode(metadata.getRepositoryName(), metadata.getLocalPath());
        repositoryNode.setOrganization(metadata.getOrganization());
        repositoryNode.setUrl(metadata.getRepositoryUrl());
        repositoryNode.setCloneUrl(metadata.getRepositoryUrl());
        repositoryNode.setDefaultBranch(metadata.getBranch());
        repositoryNode.setLastCommitHash(metadata.getCommitHash());
        repositoryNode.setLastCommitDate(metadata.getCommitDate());
        
        // Save to graph database
        var repositoryResult = graphService.saveRepository(repositoryNode);
        
        // Cache the repository node
        repositoryCache.put(cacheKey, repositoryNode);
        
        LOGGER.info("Repository node {}: {} (org: {}, branch: {}) - {}", 
                   repositoryResult.getOperationType().name().toLowerCase(),
                   repositoryNode.getName(), repositoryNode.getOrganization(), repositoryNode.getDefaultBranch(),
                   repositoryResult.isSuccess() ? "SUCCESS" : "FAILED");
        
        return repositoryNode;
    }
    
    /**
     * Detect and create sub-projects within a repository
     */
    public List<SubProjectNode> detectAndCreateSubProjects(RepositoryNode repository) {
        if (repository == null || repository.getLocalPath() == null) {
            LOGGER.debug("No repository or local path provided, skipping sub-project detection");
            return List.of();
        }
        
        LOGGER.info("Detecting sub-projects in repository: {}", repository.getName());
        
        // Detect sub-projects using the detector service
        List<SubProjectMetadata> detectedProjects = subProjectDetector.detectSubProjects(
            repository.getLocalPath(), 
            repository.getId()
        );
        
        // Convert metadata to nodes and save them
        List<SubProjectNode> subProjectNodes = detectedProjects.stream()
            .map(this::createSubProjectNode)
            .toList();
        
        // Save sub-projects to database and create relationships
        for (SubProjectNode subProject : subProjectNodes) {
            var subProjectResult = graphService.saveSubProject(subProject);
            
            // Create CONTAINS relationship from repository to sub-project
            boolean relationshipCreated = graphService.createRelationship(repository.getId(), subProject.getId(), "CONTAINS");
            
            // Defensive logging to handle potential null operationType
            String operationType = "UNKNOWN";
            if (subProjectResult != null && subProjectResult.getOperationType() != null) {
                operationType = subProjectResult.getOperationType().name().toLowerCase();
            } else {
                LOGGER.warn("UpsertResult has null operationType for subProject: {} - Result: {}", 
                           subProject.getName(), subProjectResult);
            }
            
            LOGGER.debug("Sub-project node {}: {} of type {} in repository {} - {} (relationship: {})", 
                        operationType,
                        subProject.getName(), subProject.getType(), repository.getName(),
                        subProjectResult != null && subProjectResult.isSuccess() ? "SUCCESS" : "FAILED",
                        relationshipCreated ? "CREATED" : "EXISTS");
        }
        
        LOGGER.info("Created {} sub-projects for repository: {}", subProjectNodes.size(), repository.getName());
        return subProjectNodes;
    }
    
    /**
     * Convert SubProjectMetadata to SubProjectNode
     */
    private SubProjectNode createSubProjectNode(SubProjectMetadata metadata) {
        SubProjectNode node = new SubProjectNode();
        node.setId(metadata.getId());
        node.setName(metadata.getName());
        node.setPath(metadata.getPath());
        node.setType(metadata.getType());
        node.setBuildFile(metadata.getBuildFile());
        node.setSourceDirectories(metadata.getSourceDirectories());
        node.setTestDirectories(metadata.getTestDirectories());
        node.setDependencies(metadata.getDependencies());
        node.setDescription(metadata.getDescription());
        node.setVersion(metadata.getVersion());
        node.setRepositoryId(metadata.getRepositoryId());
        
        // Set initial scores (can be calculated later)
        node.setHealthScore(null);
        node.setComplexityScore(null);
        node.setMaintainabilityScore(null);
        
        return node;
    }
    
    /**
     * Find sub-projects by repository ID
     */
    public List<SubProjectNode> findSubProjectsByRepository(String repositoryId) {
        return graphService.findSubProjectsByRepositoryId(repositoryId);
    }
    
    /**
     * Link source file nodes to repository
     */
    public void linkNodesToRepository(List<Object> nodes, RepositoryNode repository) {
        if (repository == null || nodes == null || nodes.isEmpty()) {
            return;
        }
        
        for (Object node : nodes) {
            if (node instanceof com.vividcodes.graphrag.model.graph.ClassNode) {
                com.vividcodes.graphrag.model.graph.ClassNode classNode = (com.vividcodes.graphrag.model.graph.ClassNode) node;
                graphService.createRelationship(repository.getId(), classNode.getId(), "CONTAINS");
                LOGGER.debug("Linked repository {} to class {}", repository.getName(), classNode.getName());
            } else if (node instanceof com.vividcodes.graphrag.model.graph.MethodNode) {
                com.vividcodes.graphrag.model.graph.MethodNode methodNode = (com.vividcodes.graphrag.model.graph.MethodNode) node;
                graphService.createRelationship(repository.getId(), methodNode.getId(), "CONTAINS");
                LOGGER.debug("Linked repository {} to method {}", repository.getName(), methodNode.getName());
            } else if (node instanceof com.vividcodes.graphrag.model.graph.FieldNode) {
                com.vividcodes.graphrag.model.graph.FieldNode fieldNode = (com.vividcodes.graphrag.model.graph.FieldNode) node;
                graphService.createRelationship(repository.getId(), fieldNode.getId(), "CONTAINS");
                LOGGER.debug("Linked repository {} to field {}", repository.getName(), fieldNode.getName());
            } else if (node instanceof com.vividcodes.graphrag.model.graph.PackageNode) {
                com.vividcodes.graphrag.model.graph.PackageNode packageNode = (com.vividcodes.graphrag.model.graph.PackageNode) node;
                graphService.createRelationship(repository.getId(), packageNode.getId(), "CONTAINS");
                LOGGER.debug("Linked repository {} to package {}", repository.getName(), packageNode.getName());
            }
        }
    }
    
    /**
     * Clear repository cache
     */
    public void clearCache() {
        repositoryCache.clear();
        LOGGER.debug("Cleared repository cache");
    }
    
    /**
     * Get repository statistics
     */
    public Map<String, Object> getRepositoryStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cachedRepositories", repositoryCache.size());
        stats.put("cacheKeys", repositoryCache.keySet());
        return stats;
    }
}
