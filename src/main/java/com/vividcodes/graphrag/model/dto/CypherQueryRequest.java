package com.vividcodes.graphrag.model.dto;

import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CypherQueryRequest {
    
    @NotBlank(message = "Query is required")
    private String query;
    
    private Map<String, Object> parameters;
    
    @NotNull(message = "Options are required")
    private QueryOptions options;
    
    public CypherQueryRequest() {
        this.options = new QueryOptions(); // Default options
    }
    
    public CypherQueryRequest(String query, Map<String, Object> parameters, QueryOptions options) {
        this.query = query;
        this.parameters = parameters;
        this.options = options != null ? options : new QueryOptions();
    }
    
    // Getters and Setters
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public QueryOptions getOptions() {
        return options;
    }
    
    public void setOptions(QueryOptions options) {
        this.options = options;
    }
    
    public static class QueryOptions {
        private int timeout = 30; // Default 30 seconds
        private boolean includeStats = false;
        private String format = "json";
        private int maxResults = 1000; // Default max results
        
        public QueryOptions() {
        }
        
        public QueryOptions(int timeout, boolean includeStats, String format, int maxResults) {
            this.timeout = timeout;
            this.includeStats = includeStats;
            this.format = format;
            this.maxResults = maxResults;
        }
        
        // Getters and Setters
        public int getTimeout() {
            return timeout;
        }
        
        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
        
        public boolean isIncludeStats() {
            return includeStats;
        }
        
        public void setIncludeStats(boolean includeStats) {
            this.includeStats = includeStats;
        }
        
        public String getFormat() {
            return format;
        }
        
        public void setFormat(String format) {
            this.format = format;
        }
        
        public int getMaxResults() {
            return maxResults;
        }
        
        public void setMaxResults(int maxResults) {
            this.maxResults = maxResults;
        }
    }
}
