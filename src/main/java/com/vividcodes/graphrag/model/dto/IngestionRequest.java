package com.vividcodes.graphrag.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class IngestionRequest {
    
    @NotBlank(message = "Source path is required")
    private String sourcePath;
    
    private IngestionFilters filters;
    
    public IngestionRequest() {}
    
    public IngestionRequest(String sourcePath, IngestionFilters filters) {
        this.sourcePath = sourcePath;
        this.filters = filters;
    }
    
    public String getSourcePath() {
        return sourcePath;
    }
    
    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }
    
    public IngestionFilters getFilters() {
        return filters;
    }
    
    public void setFilters(IngestionFilters filters) {
        this.filters = filters;
    }
    
    public static class IngestionFilters {
        private boolean includePrivate = false;
        private boolean includeTests = false;
        private List<String> filePatterns = List.of("*.java");
        
        public IngestionFilters() {}
        
        public IngestionFilters(boolean includePrivate, boolean includeTests, List<String> filePatterns) {
            this.includePrivate = includePrivate;
            this.includeTests = includeTests;
            this.filePatterns = filePatterns;
        }
        
        public boolean isIncludePrivate() {
            return includePrivate;
        }
        
        public void setIncludePrivate(boolean includePrivate) {
            this.includePrivate = includePrivate;
        }
        
        public boolean isIncludeTests() {
            return includeTests;
        }
        
        public void setIncludeTests(boolean includeTests) {
            this.includeTests = includeTests;
        }
        
        public List<String> getFilePatterns() {
            return filePatterns;
        }
        
        public void setFilePatterns(List<String> filePatterns) {
            this.filePatterns = filePatterns;
        }
    }
} 