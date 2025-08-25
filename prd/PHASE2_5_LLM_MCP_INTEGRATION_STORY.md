# Phase 2.5: LLM MCP Integration Story

## Overview

Implement an MCP (Model Context Protocol) server that enables LLM agents to dynamically compose and execute Cypher queries against the Neo4j graph database to gather relevant code context. This phase bridges the gap between the Cypher query endpoint and full natural language to Cypher conversion by providing a structured way for agents to query the codebase.

## Objectives

- Build an MCP server for code context retrieval
- Implement dynamic Cypher query composition
- Create code context formatting for LLM consumption
- Add semantic search capabilities for code elements
- Provide structured data retrieval for agent prompts

## Timeline

**Duration**: 1-2 weeks
**Dependencies**: Phase 1 (Core Infrastructure) and Cypher Query Endpoint must be complete
**Prerequisites**: Before Phase 3 (Full LLM Integration)

## Core Requirements

### 1. MCP Server Implementation

- [ ] Create MCP server with Java/Spring Boot
- [ ] Implement MCP protocol handlers
- [ ] Add resource discovery capabilities
- [ ] Support for dynamic query execution
- [ ] Implement proper error handling and logging

### 2. Code Context Retrieval

- [ ] Build context retrieval service
- [ ] Implement code chunk extraction with line numbers
- [ ] Add file path and repository context
- [ ] Create context relevance scoring
- [ ] Support for multiple context types

### 3. Dynamic Query Composition

- [ ] Create query builder service
- [ ] Implement parameterized query generation
- [ ] Add query validation and optimization
- [ ] Support for complex query patterns
- [ ] Create query result processing

### 4. Semantic Search Integration

- [ ] Add semantic search for method names
- [ ] Implement fuzzy matching for class names
- [ ] Support for partial matches and patterns
- [ ] Add relevance ranking algorithms
- [ ] Create search result formatting

### 5. Agent Integration

- [ ] Design agent-friendly API
- [ ] Implement context formatting for LLM consumption
- [ ] Add structured data responses
- [ ] Create agent authentication
- [ ] Support for batch operations

## Technical Implementation

### MCP Server Architecture

```java
@RestController
@RequestMapping("/mcp")
public class MCPServerController {

    private final CodeContextService contextService;
    private final QueryComposerService queryComposer;
    private final SemanticSearchService semanticSearch;

    @PostMapping("/resources")
    public ResponseEntity<MCPResourceList> listResources() {
        // Return available code context resources
    }

    @PostMapping("/read")
    public ResponseEntity<MCPReadResponse> readResource(@RequestBody MCPReadRequest request) {
        // Execute Cypher query and return formatted context
    }

    @PostMapping("/search")
    public ResponseEntity<MCPSearchResponse> searchCode(@RequestBody MCPSearchRequest request) {
        // Perform semantic search and return results
    }
}
```

### Code Context Service

```java
@Service
public class CodeContextService {

    private final CypherQueryService cypherQueryService;
    private final QueryComposerService queryComposer;

    public CodeContext getMethodContext(String methodName, String className) {
        // Build Cypher query to find method context
        String query = queryComposer.buildMethodContextQuery(methodName, className);
        
        // Execute query and format results
        QueryResult result = cypherQueryService.executeQuery(query);
        
        return formatContext(result);
    }

    public CodeContext getClassContext(String className) {
        // Build Cypher query to find class context
        String query = queryComposer.buildClassContextQuery(className);
        
        // Execute query and format results
        QueryResult result = cypherQueryService.executeQuery(query);
        
        return formatContext(result);
    }

    public CodeContext getCallHierarchy(String methodName) {
        // Build Cypher query to find call hierarchy
        String query = queryComposer.buildCallHierarchyQuery(methodName);
        
        // Execute query and format results
        QueryResult result = cypherQueryService.executeQuery(query);
        
        return formatContext(result);
    }

    private CodeContext formatContext(QueryResult result) {
        // Format Neo4j results into structured context
        // Include file paths, line numbers, repository info
    }
}
```

### Query Composer Service

```java
@Service
public class QueryComposerService {

    public String buildMethodContextQuery(String methodName, String className) {
        return """
            MATCH (c:Class {name: $className})-[:CONTAINS]->(m:Method {name: $methodName})
            OPTIONAL MATCH (m)-[:CALLS]->(called:Method)
            OPTIONAL MATCH (m)-[:HAS_PARAMETER]->(p:Parameter)
            RETURN c, m, collect(called) as calledMethods, collect(p) as parameters
            """;
    }

    public String buildClassContextQuery(String className) {
        return """
            MATCH (c:Class {name: $className})
            OPTIONAL MATCH (c)-[:CONTAINS]->(m:Method)
            OPTIONAL MATCH (c)-[:CONTAINS]->(f:Field)
            OPTIONAL MATCH (c)-[:EXTENDS]->(parent:Class)
            RETURN c, collect(m) as methods, collect(f) as fields, parent
            """;
    }

    public String buildCallHierarchyQuery(String methodName) {
        return """
            MATCH (m:Method {name: $methodName})
            OPTIONAL MATCH path = (m)-[:CALLS*]->(called:Method)
            RETURN m, collect(path) as callPaths
            """;
    }

    public String buildSemanticSearchQuery(String searchTerm) {
        return """
            MATCH (m:Method)
            WHERE m.name CONTAINS $searchTerm OR m.visibility = $searchTerm
            OPTIONAL MATCH (c:Class)-[:CONTAINS]->(m)
            RETURN m, c
            LIMIT 10
            """;
    }
}
```

### MCP Protocol Models

```java
public class MCPResourceList {
    private List<MCPResource> resources;
    
    public static class MCPResource {
        private String uri;
        private String name;
        private String description;
        private String mimeType;
    }
}

public class MCPReadRequest {
    private String uri;
    private Map<String, Object> arguments;
}

public class MCPReadResponse {
    private String contents;
    private String mimeType;
    private Map<String, Object> metadata;
}

public class MCPSearchRequest {
    private String query;
    private String contextType; // "method", "class", "file"
    private Map<String, Object> filters;
}

public class MCPSearchResponse {
    private List<SearchResult> results;
    private int totalCount;
    private double relevanceScore;
}
```

## Validation Cases

### 1. MCP Server Functionality

- [ ] **Resource Discovery**: Agent can discover available code context resources
- [ ] **Protocol Compliance**: Server implements MCP protocol correctly
- [ ] **Error Handling**: Proper error responses for invalid requests
- [ ] **Authentication**: Agent authentication and authorization
- [ ] **Rate Limiting**: Request rate limiting and throttling

### 2. Code Context Retrieval

- [ ] **Method Context**: Retrieve complete method context with parameters
- [ ] **Class Context**: Retrieve class structure with methods and fields
- [ ] **Call Hierarchy**: Retrieve method call chains and dependencies
- [ ] **File Context**: Retrieve file-level context and structure
- [ ] **Repository Context**: Retrieve repository metadata and relationships

### 3. Dynamic Query Composition

- [ ] **Parameterized Queries**: Generate parameterized Cypher queries
- [ ] **Query Validation**: Validate generated queries before execution
- [ ] **Query Optimization**: Optimize queries for performance
- [ ] **Complex Patterns**: Handle complex query patterns and relationships
- [ ] **Error Recovery**: Handle query execution errors gracefully

### 4. Semantic Search

- [ ] **Method Search**: Find methods by name patterns and descriptions
- [ ] **Class Search**: Find classes by name and package patterns
- [ ] **Fuzzy Matching**: Support for approximate string matching
- [ ] **Relevance Ranking**: Rank search results by relevance
- [ ] **Filtering**: Filter results by various criteria

### 5. Agent Integration

- [ ] **Context Formatting**: Format context for LLM consumption
- [ ] **Structured Data**: Provide structured data responses
- [ ] **Batch Operations**: Support for batch context retrieval
- [ ] **Caching**: Cache frequently requested contexts
- [ ] **Performance**: Fast response times for agent queries

### 6. Integration Testing

- [ ] **End-to-End Flow**: Test complete agent to context flow
- [ ] **Multiple Agents**: Support for multiple concurrent agents
- [ ] **Load Testing**: Test server performance under load
- [ ] **Error Scenarios**: Test various error conditions
- [ ] **Security**: Test security and access control

## Success Criteria

- [ ] MCP server responds to agent requests within 500ms
- [ ] Code context retrieval provides relevant information
- [ ] Dynamic query composition generates valid Cypher queries
- [ ] Semantic search returns relevant results with good ranking
- [ ] Agent integration provides structured, consumable data
- [ ] Server handles concurrent agent requests efficiently
- [ ] Error handling provides meaningful error messages
- [ ] Security measures prevent unauthorized access

## Dependencies

- Phase 1: Core Infrastructure (Neo4j integration, basic API)
- Cypher Query Endpoint: Query execution capabilities
- Neo4j database with ingested code data
- Spring Boot application running

## Deliverables

- [ ] MCP server implementation
- [ ] Code context retrieval service
- [ ] Dynamic query composer service
- [ ] Semantic search integration
- [ ] Agent integration API
- [ ] Comprehensive test suite
- [ ] MCP protocol documentation
- [ ] Agent integration guide

## Risk Mitigation

- **Performance**: Implement caching and query optimization
- **Security**: Add proper authentication and authorization
- **Scalability**: Design for concurrent agent access
- **Reliability**: Add comprehensive error handling and logging
- **Compatibility**: Ensure MCP protocol compliance

## Example Usage

### Agent Requesting Method Context

```json
POST /mcp/read
{
  "uri": "method://saveUser",
  "arguments": {
    "className": "UserService",
    "includeCalls": true,
    "includeParameters": true
  }
}
```

### Agent Searching for Methods

```json
POST /mcp/search
{
  "query": "database save operations",
  "contextType": "method",
  "filters": {
    "visibility": "PUBLIC",
    "repository": "main"
  }
}
```

### Agent Requesting Class Structure

```json
POST /mcp/read
{
  "uri": "class://UserController",
  "arguments": {
    "includeMethods": true,
    "includeFields": true,
    "includeInheritance": true
  }
}
```

This MCP server will provide the foundation for LLM agents to intelligently query the codebase and gather relevant context for their prompts, enabling more accurate and contextually aware code analysis and generation.
