package com.vividcodes.graphrag.model.dto;

import java.util.List;
import java.util.Map;

public class QueryResult {
    
    private String query;
    private long executionTime;
    private int resultCount;
    private List<Map<String, Object>> results;
    private QueryStatistics statistics;
    
    public QueryResult() {
    }
    
    public QueryResult(String query, long executionTime, int resultCount, 
                      List<Map<String, Object>> results, QueryStatistics statistics) {
        this.query = query;
        this.executionTime = executionTime;
        this.resultCount = resultCount;
        this.results = results;
        this.statistics = statistics;
    }
    
    // Getters and Setters
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
    
    public int getResultCount() {
        return resultCount;
    }
    
    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }
    
    public List<Map<String, Object>> getResults() {
        return results;
    }
    
    public void setResults(List<Map<String, Object>> results) {
        this.results = results;
    }
    
    public QueryStatistics getStatistics() {
        return statistics;
    }
    
    public void setStatistics(QueryStatistics statistics) {
        this.statistics = statistics;
    }
    
    public static class QueryStatistics {
        private int nodesCreated;
        private int nodesDeleted;
        private int relationshipsCreated;
        private int relationshipsDeleted;
        private int propertiesSet;
        private int labelsAdded;
        private int labelsRemoved;
        private int indexesAdded;
        private int indexesRemoved;
        private int constraintsAdded;
        private int constraintsRemoved;
        
        public QueryStatistics() {
        }
        
        public QueryStatistics(int nodesCreated, int nodesDeleted, int relationshipsCreated, 
                              int relationshipsDeleted, int propertiesSet, int labelsAdded, 
                              int labelsRemoved, int indexesAdded, int indexesRemoved, 
                              int constraintsAdded, int constraintsRemoved) {
            this.nodesCreated = nodesCreated;
            this.nodesDeleted = nodesDeleted;
            this.relationshipsCreated = relationshipsCreated;
            this.relationshipsDeleted = relationshipsDeleted;
            this.propertiesSet = propertiesSet;
            this.labelsAdded = labelsAdded;
            this.labelsRemoved = labelsRemoved;
            this.indexesAdded = indexesAdded;
            this.indexesRemoved = indexesRemoved;
            this.constraintsAdded = constraintsAdded;
            this.constraintsRemoved = constraintsRemoved;
        }
        
        // Getters and Setters
        public int getNodesCreated() {
            return nodesCreated;
        }
        
        public void setNodesCreated(int nodesCreated) {
            this.nodesCreated = nodesCreated;
        }
        
        public int getNodesDeleted() {
            return nodesDeleted;
        }
        
        public void setNodesDeleted(int nodesDeleted) {
            this.nodesDeleted = nodesDeleted;
        }
        
        public int getRelationshipsCreated() {
            return relationshipsCreated;
        }
        
        public void setRelationshipsCreated(int relationshipsCreated) {
            this.relationshipsCreated = relationshipsCreated;
        }
        
        public int getRelationshipsDeleted() {
            return relationshipsDeleted;
        }
        
        public void setRelationshipsDeleted(int relationshipsDeleted) {
            this.relationshipsDeleted = relationshipsDeleted;
        }
        
        public int getPropertiesSet() {
            return propertiesSet;
        }
        
        public void setPropertiesSet(int propertiesSet) {
            this.propertiesSet = propertiesSet;
        }
        
        public int getLabelsAdded() {
            return labelsAdded;
        }
        
        public void setLabelsAdded(int labelsAdded) {
            this.labelsAdded = labelsAdded;
        }
        
        public int getLabelsRemoved() {
            return labelsRemoved;
        }
        
        public void setLabelsRemoved(int labelsRemoved) {
            this.labelsRemoved = labelsRemoved;
        }
        
        public int getIndexesAdded() {
            return indexesAdded;
        }
        
        public void setIndexesAdded(int indexesAdded) {
            this.indexesAdded = indexesAdded;
        }
        
        public int getIndexesRemoved() {
            return indexesRemoved;
        }
        
        public void setIndexesRemoved(int indexesRemoved) {
            this.indexesRemoved = indexesRemoved;
        }
        
        public int getConstraintsAdded() {
            return constraintsAdded;
        }
        
        public void setConstraintsAdded(int constraintsAdded) {
            this.constraintsAdded = constraintsAdded;
        }
        
        public int getConstraintsRemoved() {
            return constraintsRemoved;
        }
        
        public void setConstraintsRemoved(int constraintsRemoved) {
            this.constraintsRemoved = constraintsRemoved;
        }
    }
}
