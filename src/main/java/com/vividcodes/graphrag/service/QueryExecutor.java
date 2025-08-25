package com.vividcodes.graphrag.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.vividcodes.graphrag.exception.QueryExecutionException;
import com.vividcodes.graphrag.model.dto.CypherQueryRequest;
import com.vividcodes.graphrag.model.dto.QueryResult;

@Component
public class QueryExecutor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryExecutor.class);
    
    private final Driver neo4jDriver;
    private final QueryCache queryCache;
    
    @Autowired
    public QueryExecutor(Driver neo4jDriver, QueryCache queryCache) {
        this.neo4jDriver = neo4jDriver;
        this.queryCache = queryCache;
    }
    
    public QueryResult execute(String cypherQuery, Map<String, Object> parameters, 
                              CypherQueryRequest.QueryOptions options) {
        long startTime = System.currentTimeMillis();
        
        try (Session session = neo4jDriver.session()) {
            // Check cache first
            String cacheKey = generateCacheKey(cypherQuery, parameters != null ? parameters : new HashMap<>());
            QueryResult cachedResult = queryCache.get(cacheKey);
            if (cachedResult != null) {
                LOGGER.debug("Returning cached result for query: {}", cypherQuery);
                return cachedResult;
            }
            
            // Execute query with timeout
            CompletableFuture<Result> future = CompletableFuture.supplyAsync(() -> {
                if (parameters != null && !parameters.isEmpty()) {
                    return session.run(cypherQuery, parameters);
                } else {
                    return session.run(cypherQuery);
                }
            });
            
            Result result = future.get(options.getTimeout(), TimeUnit.SECONDS);
            
            // Process results
            List<Map<String, Object>> results = new ArrayList<>();
            int resultCount = 0;
            
            while (result.hasNext() && resultCount < options.getMaxResults()) {
                Record record = result.next();
                results.add(record.asMap());
                resultCount++;
            }
            
            // Get statistics
            QueryResult.QueryStatistics statistics = extractStatistics(result);
            
            // Create result object
            QueryResult queryResult = new QueryResult(
                cypherQuery,
                System.currentTimeMillis() - startTime,
                resultCount,
                results,
                statistics
            );
            
            // Cache result if it's not too large
            if (resultCount <= 100) { // Only cache smaller results
                queryCache.put(cacheKey, queryResult);
            }
            
            LOGGER.info("Query executed successfully in {}ms with {} results", 
                       queryResult.getExecutionTime(), resultCount);
            
            return queryResult;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            LOGGER.error("Query execution failed after {}ms: {}", executionTime, e.getMessage(), e);
            throw new QueryExecutionException("Failed to execute query: " + e.getMessage(), e);
        }
    }
    
    private String generateCacheKey(String cypherQuery, Map<String, Object> parameters) {
        StringBuilder key = new StringBuilder(cypherQuery);
        if (parameters != null) {
            key.append(":").append(parameters.toString());
        }
        return key.toString().hashCode() + "";
    }
    
    private QueryResult.QueryStatistics extractStatistics(Result result) {
        // Neo4j doesn't provide detailed statistics in the Result object
        // We'll create a basic statistics object
        return new QueryResult.QueryStatistics(
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        );
    }
}
