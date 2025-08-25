package com.vividcodes.graphrag.service;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vividcodes.graphrag.exception.InvalidQueryException;
import com.vividcodes.graphrag.model.dto.CypherQueryRequest;
import com.vividcodes.graphrag.model.dto.QueryResult;
import com.vividcodes.graphrag.model.dto.ValidationResult;

@Service
public class CypherQueryService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CypherQueryService.class);
    
    private final QueryValidator queryValidator;
    private final QueryExecutor queryExecutor;
    
    @Autowired
    public CypherQueryService(QueryValidator queryValidator, QueryExecutor queryExecutor) {
        this.queryValidator = queryValidator;
        this.queryExecutor = queryExecutor;
    }
    
    public QueryResult executeQuery(String cypherQuery, Map<String, Object> parameters, 
                                   CypherQueryRequest.QueryOptions options) {
        LOGGER.info("Executing Cypher query: {}", cypherQuery);
        
        // Validate query
        ValidationResult validation = queryValidator.validate(cypherQuery);
        if (!validation.isValid()) {
            String errorMessage = String.join(", ", validation.getErrors());
            LOGGER.warn("Query validation failed: {}", errorMessage);
            throw new InvalidQueryException(errorMessage);
        }
        
        // Execute query with timeout
        return queryExecutor.execute(cypherQuery, parameters, options);
    }
    
    public QueryResult executeQuery(CypherQueryRequest request) {
        return executeQuery(
            request.getQuery(),
            request.getParameters() != null ? request.getParameters() : new HashMap<>(),
            request.getOptions()
        );
    }
}
