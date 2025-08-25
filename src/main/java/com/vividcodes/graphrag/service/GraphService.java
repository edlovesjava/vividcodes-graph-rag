package com.vividcodes.graphrag.service;

import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.FieldNode;
import com.vividcodes.graphrag.model.graph.MethodNode;
import com.vividcodes.graphrag.model.graph.PackageNode;
import com.vividcodes.graphrag.model.graph.RepositoryNode;

public interface GraphService {
    
    void savePackage(PackageNode packageNode);
    
    void saveClass(ClassNode classNode);
    
    void saveMethod(MethodNode methodNode);
    
    void saveField(FieldNode fieldNode);
    
    void saveRepository(RepositoryNode repositoryNode);
    
    void createRelationship(String fromId, String toId, String relationshipType);
    
    void createRelationship(String fromId, String toId, String relationshipType, java.util.Map<String, Object> properties);
    
    /**
     * Clear all data from the graph database
     */
    void clearAllData();
    
    /**
     * Get statistics about the current data in the graph database
     */
    java.util.Map<String, Object> getDataStatistics();
} 