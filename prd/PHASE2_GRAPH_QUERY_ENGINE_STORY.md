# Phase 2: Graph Query Engine Story

## Overview

Implement a comprehensive Graph Query Engine that can convert natural language prompts into Cypher queries, execute them against the Neo4j graph database, and return relevant code context with precise line number references.

## Objectives

- Build natural language to Cypher query conversion
- Implement comprehensive query execution engine
- Create code context retrieval service
- Add semantic search capabilities
- Implement query result ranking and relevance scoring

## Timeline

**Duration**: 2-3 weeks
**Dependencies**: Phase 1 (Core Infrastructure) and Cypher Query Endpoint must be complete
**Prerequisites**: Before Phase 2.5 (LLM MCP Integration)

## Core Requirements

### 1. Natural Language to Cypher Conversion

- [ ] Implement LLM-based prompt-to-query conversion
- [ ] Build Cypher query builder with parameterized queries
- [ ] Create query validation and optimization
- [ ] Support for complex query patterns

### 2. Code Context Retrieval Service

- [ ] Extract code chunks with line numbers
- [ ] Map graph results to source file locations
- [ ] Format context for LLM consumption
- [ ] Implement context relevance scoring

### 3. Semantic Search Capabilities

- [ ] Add semantic search for method names and descriptions
- [ ] Implement fuzzy matching for method names
- [ ] Support for partial matches and patterns
- [ ] Add relevance ranking algorithms

### 4. Query Result Ranking

- [ ] Implement relevance scoring for query results
- [ ] Add result filtering and sorting
- [ ] Support for result pagination
- [ ] Create result caching for performance

## Validation Cases

The following validation cases must be implemented and tested to ensure the Graph Query Engine meets requirements:

### 1. Basic Graph Exploration Queries

- [ ] **Find All Public Methods**: Query to retrieve all public methods with class names, file paths, and line numbers
- [ ] **Find All Classes with Methods**: Query to show class hierarchy with contained methods
- [ ] **Method Count Analysis**: Count methods by visibility, class type, and package

### 2. Method Call Sequence Analysis

- [ ] **Database-Related Methods**: Find methods containing 'save', 'create', 'update', 'find', 'query', 'delete', 'insert'
- [ ] **Controller Methods**: Identify methods in classes containing 'Controller' or 'Service'
- [ ] **Process Methods**: Find methods with 'handle', 'process' in their names
- [ ] **Pattern-Based Search**: Support for method name pattern matching

### 3. Call Hierarchy Analysis

- [ ] **Entry Points**: Find public methods in Controller classes
- [ ] **Service Layer**: Identify public methods in Service classes
- [ ] **Repository/DAO Methods**: Find methods in Repository, DAO classes
- [ ] **Call Chain Visualization**: Show Controller → Service → Repository relationships

### 4. Method Parameter Analysis

- [ ] **Request/Response Parameters**: Find methods with Request/Response parameter types
- [ ] **Entity Parameters**: Find methods with Entity/Model/DTO parameters
- [ ] **Parameter Type Filtering**: Filter by specific parameter types

### 5. File Structure Analysis

- [ ] **Controller File Methods**: Find methods in controller files
- [ ] **Package-Based Queries**: Query methods by package name patterns
- [ ] **File Path Filtering**: Filter by file path patterns

### 6. Complex Call Sequence Analysis

- [ ] **ResponseEntity Methods**: Find methods returning ResponseEntity/Response/Result
- [ ] **Void Entry Points**: Find public void methods (potential entry points)
- [ ] **Return Type Analysis**: Analyze methods by return type patterns

### 7. Line Number Analysis

- [ ] **Method Size Analysis**: Find large methods (>20 lines)
- [ ] **Line Range Queries**: Query methods in specific line ranges
- [ ] **Code Location Tracking**: Track method locations by file and line numbers

### 8. Visibility and Modifier Analysis

- [ ] **Public Static Methods**: Find public static methods
- [ ] **Abstract Methods**: Find abstract methods
- [ ] **Modifier-Based Filtering**: Filter by method modifiers

### 9. Comprehensive Call Sequence Queries

- [ ] **Complete Call Chain**: Show Controller → Service → Repository call sequences
- [ ] **Cross-Layer Relationships**: Identify relationships between different architectural layers
- [ ] **Method Dependency Mapping**: Map method dependencies across classes

### 10. API Endpoint to Leaf Method Sequence

- [ ] **REST API Entry Points**: Find REST endpoint methods (get/post/put/delete)
- [ ] **Service Method Mapping**: Map controller methods to service methods
- [ ] **Leaf Method Identification**: Identify methods that don't call other methods

### 11. Natural Language Query Examples

- [ ] **"Find all public methods that call the database"**
- [ ] **"Show me all controller methods"**
- [ ] **"Find methods that save data"**
- [ ] **"Show service layer methods"**
- [ ] **"Find methods with Request parameters"**

### 12. Performance Validation Cases

- [ ] **Large Result Set Handling**: Test with 1000+ methods
- [ ] **Query Response Time**: Ensure queries complete in <2 seconds
- [ ] **Memory Usage**: Monitor memory usage during complex queries
- [ ] **Index Performance**: Validate Neo4j index usage

### 13. Integration Test Cases

- [ ] **API Endpoint Testing**: Test `/api/v1/query` endpoint
- [ ] **Query Parameter Validation**: Test various query parameters
- [ ] **Error Handling**: Test invalid queries and error responses
- [ ] **Result Formatting**: Validate JSON response format

### 14. Code Context Retrieval Validation

- [ ] **Line Number Accuracy**: Ensure correct line number extraction
- [ ] **Code Chunk Extraction**: Validate code snippet retrieval
- [ ] **File Path Resolution**: Test file path resolution accuracy
- [ ] **Context Relevance**: Validate context relevance scoring

### 15. Query Builder Validation

- [ ] **Natural Language to Cypher**: Test prompt-to-query conversion
- [ ] **Query Optimization**: Ensure generated queries are optimized
- [ ] **Parameter Binding**: Test parameterized query generation
- [ ] **Query Validation**: Validate generated Cypher syntax

## Technical Implementation

### API Endpoints

```http
POST /api/v1/query
Content-Type: application/json

{
  "query": "Find all public methods that call the database",
  "context": {
    "maxResults": 10,
    "includeCode": true
  }
}
```

### Response Format

```json
{
  "status": "success",
  "query": "Find all public methods that call the database",
  "results": [
    {
      "nodeId": "method:com.example:UserService:saveUser",
      "nodeType": "Method",
      "relevance": 0.95,
      "codeContext": {
        "filePath": "/src/main/java/com/example/UserService.java",
        "lineStart": 45,
        "lineEnd": 55,
        "code": "public User saveUser(User user) {\n    return userRepository.save(user);\n}"
      }
    }
  ]
}
```

### Core Services

- **QueryProcessor**: Converts natural language to Cypher
- **GraphQueryService**: Executes queries against Neo4j
- **CodeContextService**: Retrieves and formats code context
- **RelevanceService**: Scores and ranks query results

## Success Criteria

- [ ] All 15 validation case categories implemented and tested
- [ ] Query response time < 2 seconds for complex queries
- [ ] Natural language query accuracy > 90%
- [ ] Code context retrieval accuracy > 95%
- [ ] API endpoint fully functional with proper error handling

## Dependencies

- Phase 1: Core Infrastructure (Java Parser, Graph Storage, Basic API) ✅ **COMPLETED**
- Cypher Query Endpoint Story (Cypher Query Execution) 📋 **PREREQUISITE**
- Neo4j database with ingested code data
- LLM service for natural language processing

## Deliverables

- [ ] Graph Query Engine implementation
- [ ] Natural language to Cypher conversion
- [ ] Code context retrieval service
- [ ] Query result ranking and relevance scoring
- [ ] Comprehensive test suite
- [ ] API documentation and examples
