package com.vividcodes.graphrag.controller;

import com.vividcodes.graphrag.model.dto.IngestionRequest;
import com.vividcodes.graphrag.service.JavaParserService;
import com.vividcodes.graphrag.service.Neo4jHealthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class IngestionController {
    
    private static final Logger logger = LoggerFactory.getLogger(IngestionController.class);
    
    private final JavaParserService javaParserService;
    private final Neo4jHealthService neo4jHealthService;
    
    @Autowired
    public IngestionController(JavaParserService javaParserService, Neo4jHealthService neo4jHealthService) {
        this.javaParserService = javaParserService;
        this.neo4jHealthService = neo4jHealthService;
    }
    
    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingestCode(@Valid @RequestBody IngestionRequest request) {
        logger.info("Starting code ingestion for path: {}", request.getSourcePath());
        
        try {
            javaParserService.parseDirectory(request.getSourcePath());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Code ingestion completed successfully");
            response.put("sourcePath", request.getSourcePath());
            
            logger.info("Code ingestion completed successfully for path: {}", request.getSourcePath());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error during code ingestion: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Code ingestion failed: " + e.getMessage());
            response.put("sourcePath", request.getSourcePath());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Java Graph RAG");
        response.put("version", "0.1.0-SNAPSHOT");
        
        // Check Neo4j health
        boolean neo4jHealthy = neo4jHealthService.isHealthy();
        response.put("neo4j", Map.of(
            "status", neo4jHealthy ? "UP" : "DOWN",
            "version", neo4jHealthService.getNeo4jVersion()
        ));
        
        return ResponseEntity.ok(response);
    }
} 