package com.vividcodes.graphrag.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vividcodes.graphrag.exception.InvalidQueryException;
import com.vividcodes.graphrag.exception.QueryExecutionException;
import com.vividcodes.graphrag.model.dto.CypherQueryRequest;
import com.vividcodes.graphrag.model.dto.QueryResult;
import com.vividcodes.graphrag.service.CypherQueryService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class CypherQueryController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CypherQueryController.class);
    
    private final CypherQueryService cypherQueryService;
    
    @Autowired
    public CypherQueryController(CypherQueryService cypherQueryService) {
        this.cypherQueryService = cypherQueryService;
    }
    
    @PostMapping("/cypher")
    public ResponseEntity<Map<String, Object>> executeCypherQuery(
            @Valid @RequestBody CypherQueryRequest request) {
        
        LOGGER.info("Executing Cypher query: {}", request.getQuery());
        
        try {
            QueryResult result = cypherQueryService.executeQuery(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("query", result.getQuery());
            response.put("executionTime", result.getExecutionTime());
            response.put("resultCount", result.getResultCount());
            response.put("results", result.getResults());
            
            if (request.getOptions().isIncludeStats()) {
                response.put("statistics", result.getStatistics());
            }
            
            LOGGER.info("Query executed successfully in {}ms", result.getExecutionTime());
            return ResponseEntity.ok(response);
            
        } catch (InvalidQueryException e) {
            LOGGER.warn("Invalid query: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("INVALID_QUERY", e.getMessage()));
            
        } catch (QueryExecutionException e) {
            LOGGER.error("Query execution failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("EXECUTION_ERROR", e.getMessage()));
                
        } catch (Exception e) {
            LOGGER.error("Unexpected error during query execution: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("UNEXPECTED_ERROR", "An unexpected error occurred"));
        }
    }
    
    private Map<String, Object> createErrorResponse(String errorType, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("error", errorType);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return errorResponse;
    }
    
    @GetMapping("/cypher/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "cypher-query");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return ResponseEntity.ok(response);
    }
}
