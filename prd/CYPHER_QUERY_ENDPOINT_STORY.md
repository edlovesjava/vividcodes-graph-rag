# Cypher Query Endpoint Story

## Overview

Implement a REST API endpoint that accepts Cypher queries as payload and executes them against the Neo4j graph database. This endpoint will serve as the foundation for the Graph Query Engine in Phase 2.

## Objectives

- Create a secure Cypher query execution endpoint
- Implement query validation and sanitization
- Add comprehensive error handling and logging
- Provide query result formatting and pagination
- Ensure proper security measures for query execution

## Timeline

**Duration**: 1 week
**Dependencies**: Phase 1 (Core Infrastructure) must be complete
**Prerequisites**: Before Phase 2 (Graph Query Engine)

## Core Requirements

### 1. Cypher Query Endpoint

- [ ] Create `/api/v1/cypher` POST endpoint
- [ ] Accept Cypher queries in request body
- [ ] Execute queries against Neo4j database
- [ ] Return formatted query results
- [ ] Implement proper HTTP status codes

### 2. Query Validation and Security

- [ ] Validate Cypher query syntax
- [ ] Implement query parameter sanitization
- [ ] Add query execution time limits
- [ ] Prevent dangerous operations (DELETE, DROP, etc.)
- [ ] Add query complexity analysis

### 3. Result Formatting

- [ ] Format Neo4j results as JSON
- [ ] Include metadata (execution time, result count)
- [ ] Support pagination for large result sets
- [ ] Add result size limits
- [ ] Include query statistics

### 4. Error Handling

- [ ] Handle Neo4j connection errors
- [ ] Manage query syntax errors
- [ ] Handle timeout scenarios
- [ ] Provide meaningful error messages
- [ ] Log errors for debugging

### 5. Performance and Monitoring

- [ ] Add query execution time tracking
- [ ] Implement query result caching
- [ ] Add query performance metrics
- [ ] Monitor database connection usage
- [ ] Add query execution logging

## Technical Implementation

### API Endpoint Specification

#### POST /api/v1/cypher

```http
POST /api/v1/cypher
Content-Type: application/json

{
  "query": "MATCH (m:Method) WHERE m.visibility = 'PUBLIC' RETURN m LIMIT 10",
  "parameters": {
    "limit": 10,
    "visibility": "PUBLIC"
  },
  "options": {
    "timeout": 30,
    "includeStats": true,
    "format": "json"
  }
}
```

#### Success Response

```json
{
  "status": "success",
  "query": "MATCH (m:Method) WHERE m.visibility = 'PUBLIC' RETURN m LIMIT 10",
  "executionTime": 45,
  "resultCount": 10,
  "results": [
    {
      "m": {
        "name": "saveUser",
        "visibility": "PUBLIC",
        "class_name": "UserService",
        "file_path": "/src/main/java/com/example/UserService.java",
        "line_start": 45,
        "line_end": 55
      }
    }
  ],
  "statistics": {
    "nodesCreated": 0,
    "nodesDeleted": 0,
    "relationshipsCreated": 0,
    "relationshipsDeleted": 0,
    "propertiesSet": 0,
    "labelsAdded": 0,
    "labelsRemoved": 0,
    "indexesAdded": 0,
    "indexesRemoved": 0,
    "constraintsAdded": 0,
    "constraintsRemoved": 0
  }
}
```

#### Error Response

```json
{
  "status": "error",
  "error": "INVALID_QUERY",
  "message": "Invalid Cypher syntax at line 1, column 15",
  "details": "Expected ':' but got '='",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Core Services

#### CypherQueryService

```java
@Service
public class CypherQueryService {

    private final Driver neo4jDriver;
    private final QueryValidator queryValidator;
    private final QueryExecutor queryExecutor;

    public QueryResult executeQuery(String cypherQuery, Map<String, Object> parameters, QueryOptions options) {
        // Validate query
        ValidationResult validation = queryValidator.validate(cypherQuery);
        if (!validation.isValid()) {
            throw new InvalidQueryException(validation.getErrors());
        }

        // Execute query with timeout
        return queryExecutor.execute(cypherQuery, parameters, options);
    }

    public QueryStatistics getQueryStatistics(String cypherQuery, Map<String, Object> parameters) {
        return queryExecutor.getStatistics(cypherQuery, parameters);
    }
}
```

#### QueryValidator

```java
@Component
public class QueryValidator {

    public ValidationResult validate(String cypherQuery) {
        List<String> errors = new ArrayList<>();

        // Check for dangerous operations
        if (containsDangerousOperations(cypherQuery)) {
            errors.add("Query contains dangerous operations (DELETE, DROP, etc.)");
        }

        // Validate syntax
        if (!isValidSyntax(cypherQuery)) {
            errors.add("Invalid Cypher syntax");
        }

        // Check query complexity
        if (isTooComplex(cypherQuery)) {
            errors.add("Query is too complex");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    private boolean containsDangerousOperations(String query) {
        String upperQuery = query.toUpperCase();
        return upperQuery.contains("DELETE") ||
               upperQuery.contains("DROP") ||
               upperQuery.contains("CREATE INDEX") ||
               upperQuery.contains("CREATE CONSTRAINT");
    }
}
```

#### QueryExecutor

```java
@Component
public class QueryExecutor {

    private final Driver neo4jDriver;
    private final QueryCache queryCache;

    public QueryResult execute(String cypherQuery, Map<String, Object> parameters, QueryOptions options) {
        long startTime = System.currentTimeMillis();

        try (Session session = neo4jDriver.session()) {
            // Check cache first
            String cacheKey = generateCacheKey(cypherQuery, parameters);
            QueryResult cachedResult = queryCache.get(cacheKey);
            if (cachedResult != null) {
                return cachedResult;
            }

            // Execute query with timeout
            Result result = session.run(cypherQuery, Values.parameters(parameters));

            // Process results
            List<Map<String, Object>> results = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                results.add(record.asMap());
            }

            // Get statistics
            QueryStatistics statistics = getStatistics(result);

            // Create result object
            QueryResult queryResult = new QueryResult(
                cypherQuery,
                System.currentTimeMillis() - startTime,
                results.size(),
                results,
                statistics
            );

            // Cache result
            queryCache.put(cacheKey, queryResult);

            return queryResult;

        } catch (Exception e) {
            throw new QueryExecutionException("Failed to execute query", e);
        }
    }
}
```

### Controller Implementation

```java
@RestController
@RequestMapping("/api/v1")
public class CypherQueryController {

    private final CypherQueryService cypherQueryService;
    private final Logger logger = LoggerFactory.getLogger(CypherQueryController.class);

    @PostMapping("/cypher")
    public ResponseEntity<Map<String, Object>> executeCypherQuery(
            @Valid @RequestBody CypherQueryRequest request) {

        logger.info("Executing Cypher query: {}", request.getQuery());

        try {
            QueryResult result = cypherQueryService.executeQuery(
                request.getQuery(),
                request.getParameters(),
                request.getOptions()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("query", result.getQuery());
            response.put("executionTime", result.getExecutionTime());
            response.put("resultCount", result.getResultCount());
            response.put("results", result.getResults());

            if (request.getOptions().isIncludeStats()) {
                response.put("statistics", result.getStatistics());
            }

            logger.info("Query executed successfully in {}ms", result.getExecutionTime());
            return ResponseEntity.ok(response);

        } catch (InvalidQueryException e) {
            logger.warn("Invalid query: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("INVALID_QUERY", e.getMessage()));

        } catch (QueryExecutionException e) {
            logger.error("Query execution failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("EXECUTION_ERROR", e.getMessage()));
        }
    }
}
```

## Validation Cases

### 1. Basic Query Execution

- [ ] **Simple MATCH Query**: Execute basic MATCH query
- [ ] **Query with Parameters**: Test parameterized queries
- [ ] **Query with LIMIT**: Test result limiting
- [ ] **Query with WHERE Clause**: Test filtering
- [ ] **Query with RETURN**: Test result formatting

### 2. Query Validation Testing

- [ ] **Valid Query**: Test syntactically correct queries
- [ ] **Invalid Syntax**: Test malformed queries
- [ ] **Dangerous Operations**: Test blocked operations (DELETE, DROP)
- [ ] **Complex Queries**: Test query complexity limits
- [ ] **Parameter Validation**: Test parameter sanitization

### 3. Error Handling Testing

- [ ] **Connection Errors**: Test Neo4j connection failures
- [ ] **Timeout Scenarios**: Test query timeout handling
- [ ] **Invalid Parameters**: Test parameter validation
- [ ] **Large Results**: Test result size limits
- [ ] **Memory Issues**: Test memory constraint handling

### 4. Performance Testing

- [ ] **Response Time**: Ensure queries complete in < 5 seconds
- [ ] **Concurrent Queries**: Test multiple simultaneous queries
- [ ] **Cache Effectiveness**: Test query result caching
- [ ] **Memory Usage**: Monitor memory during query execution
- [ ] **Database Load**: Test Neo4j performance under load

### 5. Security Testing

- [ ] **Query Injection**: Test for injection vulnerabilities
- [ ] **Parameter Sanitization**: Validate parameter cleaning
- [ ] **Access Control**: Test endpoint access restrictions
- [ ] **Resource Limits**: Test resource usage limits
- [ ] **Audit Logging**: Test query execution logging

### 6. Integration Testing

- [ ] **API Endpoint**: Test complete HTTP request/response cycle
- [ ] **Content Types**: Test different content type handling
- [ ] **HTTP Methods**: Test proper HTTP method usage
- [ ] **Status Codes**: Validate correct HTTP status codes
- [ ] **Response Format**: Test JSON response formatting

### 7. Query Examples Testing

- [ ] **Method Queries**: Test queries for Method nodes
- [ ] **Class Queries**: Test queries for Class nodes
- [ ] **Relationship Queries**: Test queries with relationships
- [ ] **Aggregation Queries**: Test COUNT, AVG, etc.
- [ ] **Complex Joins**: Test multi-node queries

## Success Criteria

- [ ] Endpoint accepts and executes valid Cypher queries
- [ ] Query validation prevents dangerous operations
- [ ] Error handling provides meaningful error messages
- [ ] Response times < 5 seconds for complex queries
- [ ] Proper HTTP status codes for all scenarios
- [ ] Comprehensive logging of all query executions
- [ ] Query result caching improves performance
- [ ] Security measures prevent injection attacks

## Dependencies

- Phase 1: Core Infrastructure (Neo4j integration, basic API)
- Neo4j database with ingested code data
- Spring Boot application running

## Deliverables

- [ ] Cypher query endpoint (`/api/v1/cypher`)
- [ ] Query validation and security service
- [ ] Query execution service with timeout handling
- [ ] Result formatting and pagination
- [ ] Comprehensive error handling
- [ ] Query caching mechanism
- [ ] Performance monitoring and logging
- [ ] Complete test suite
- [ ] API documentation

## Risk Mitigation

- **Security**: Implement strict query validation and sanitization
- **Performance**: Add query timeouts and result size limits
- **Reliability**: Add comprehensive error handling and logging
- **Scalability**: Implement query caching and connection pooling
- **Monitoring**: Add detailed metrics and audit logging

## Example Usage

### Basic Method Query

```bash
curl -X POST http://localhost:8080/api/v1/cypher \
  -H "Content-Type: application/json" \
  -d '{
    "query": "MATCH (m:Method) WHERE m.visibility = '\''PUBLIC'\'' RETURN m LIMIT 10",
    "options": {
      "timeout": 30,
      "includeStats": true
    }
  }'
```

### Parameterized Query

```bash
curl -X POST http://localhost:8080/api/v1/cypher \
  -H "Content-Type: application/json" \
  -d '{
    "query": "MATCH (m:Method) WHERE m.visibility = $visibility RETURN m LIMIT $limit",
    "parameters": {
      "visibility": "PUBLIC",
      "limit": 5
    },
    "options": {
      "timeout": 30
    }
  }'
```

This endpoint will serve as the foundation for the Graph Query Engine in Phase 2, providing the core query execution capabilities needed for natural language to Cypher conversion.
