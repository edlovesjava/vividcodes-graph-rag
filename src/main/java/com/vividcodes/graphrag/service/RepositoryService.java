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
import com.vividcodes.graphrag.model.graph.RepositoryNode;

@Service
public class RepositoryService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryService.class);
    
    private final GitService gitService;
    private final GraphService graphService;
    
    // Cache for repository nodes to avoid duplicate creation
    private final Map<String, RepositoryNode> repositoryCache = new ConcurrentHashMap<>();
    
    @Autowired
    public RepositoryService(GitService gitService, GraphService graphService) {
        this.gitService = gitService;
        this.graphService = graphService;
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
        graphService.saveRepository(repositoryNode);
        
        // Cache the repository node
        repositoryCache.put(cacheKey, repositoryNode);
        
        LOGGER.info("Created repository node: {} (org: {}, branch: {})", 
                   repositoryNode.getName(), repositoryNode.getOrganization(), repositoryNode.getDefaultBranch());
        
        return repositoryNode;
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
