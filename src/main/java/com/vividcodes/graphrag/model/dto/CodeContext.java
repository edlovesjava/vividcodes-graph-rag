package com.vividcodes.graphrag.model.dto;

import java.util.List;

public class CodeContext {
    
    private String filePath;
    private Integer lineStart;
    private Integer lineEnd;
    private String code;
    private String nodeId;
    private String nodeType;
    private List<String> relatedNodes;
    
    public CodeContext() {}
    
    public CodeContext(String filePath, Integer lineStart, Integer lineEnd, String code) {
        this.filePath = filePath;
        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
        this.code = code;
    }
    
    public CodeContext(String filePath, Integer lineStart, Integer lineEnd, String code, 
                      String nodeId, String nodeType) {
        this(filePath, lineStart, lineEnd, code);
        this.nodeId = nodeId;
        this.nodeType = nodeType;
    }
    
    // Getters and Setters
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
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getNodeId() {
        return nodeId;
    }
    
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    
    public String getNodeType() {
        return nodeType;
    }
    
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }
    
    public List<String> getRelatedNodes() {
        return relatedNodes;
    }
    
    public void setRelatedNodes(List<String> relatedNodes) {
        this.relatedNodes = relatedNodes;
    }
} 