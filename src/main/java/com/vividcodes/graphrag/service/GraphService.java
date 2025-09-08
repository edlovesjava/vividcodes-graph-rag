package com.vividcodes.graphrag.service;

import java.util.List;
import com.vividcodes.graphrag.model.dto.UpsertResult;
import com.vividcodes.graphrag.model.graph.AnnotationNode;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.FieldNode;
import com.vividcodes.graphrag.model.graph.MethodNode;
import com.vividcodes.graphrag.model.graph.PackageNode;
import com.vividcodes.graphrag.model.graph.RepositoryNode;
import com.vividcodes.graphrag.model.graph.SubProjectNode;

public interface GraphService {
    
    UpsertResult savePackage(PackageNode packageNode);
    
    UpsertResult saveClass(ClassNode classNode);
    
    UpsertResult saveMethod(MethodNode methodNode);
    
    UpsertResult saveField(FieldNode fieldNode);
    
    UpsertResult saveAnnotation(AnnotationNode annotationNode);
    
    UpsertResult saveRepository(RepositoryNode repositoryNode);
    
    UpsertResult saveSubProject(SubProjectNode subProjectNode);
    
    SubProjectNode findSubProjectById(String id);
    
    java.util.List<SubProjectNode> findSubProjectsByRepositoryId(String repositoryId);
    
    void deleteSubProject(String id);
    
    boolean createRelationship(String fromId, String toId, String relationshipType);
    
    boolean createRelationship(String fromId, String toId, String relationshipType, java.util.Map<String, Object> properties);
    
    /**
     * Clear all data from the graph database
     */
    void clearAllData();
    
    /**
     * Get statistics about the current data in the graph database
     */
    java.util.Map<String, Object> getDataStatistics();
    
    /**
     * Perform batch upsert operations for multiple nodes in a single transaction.
     * 
     * @param nodes List of nodes to upsert (mixed types allowed)
     * @return List of UpsertResult for each node in the same order
     */
    List<UpsertResult> saveBatch(List<Object> nodes);
    
} 