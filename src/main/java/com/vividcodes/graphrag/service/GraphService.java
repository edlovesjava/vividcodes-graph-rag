package com.vividcodes.graphrag.service;

import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.FieldNode;
import com.vividcodes.graphrag.model.graph.MethodNode;
import com.vividcodes.graphrag.model.graph.PackageNode;

public interface GraphService {
    
    void savePackage(PackageNode packageNode);
    
    void saveClass(ClassNode classNode);
    
    void saveMethod(MethodNode methodNode);
    
    void saveField(FieldNode fieldNode);
    
    void createRelationship(String fromId, String toId, String relationshipType);
    
    void createRelationship(String fromId, String toId, String relationshipType, java.util.Map<String, Object> properties);
} 