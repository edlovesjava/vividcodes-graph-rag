# API Query Examples - Sequence of Calls to Leaf Methods

This document provides comprehensive examples of API queries and corresponding Cypher queries that demonstrate the sequence of calls from API endpoints to leaf methods in the ingested application codebase.

## Current API Endpoints

### 1. Health Check API

```bash
curl http://localhost:8080/api/v1/health
```

**Response:**

```json
{
  "status": "UP",
  "service": "Java Graph RAG",
  "version": "0.1.0-SNAPSHOT",
  "neo4j": {
    "status": "UP",
    "version": "5.15.0"
  }
}
```

### 2. Code Ingestion API

```bash
curl -X POST http://localhost:8080/api/v1/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "sourcePath": "/path/to/your/java/project",
    "filters": {
      "includePrivate": false,
      "includeTests": false,
      "filePatterns": ["*.java"]
    }
  }'
```

### 3. Cypher Query API

```bash
curl -X POST http://localhost:8080/api/v1/cypher \
  -H "Content-Type: application/json" \
  -d '{
    "query": "MATCH (c:Class) RETURN c.name as className LIMIT 5"
  }'
```

**Response:**

```json
{
  "status": "success",
  "query": "MATCH (c:Class) RETURN c.name as className LIMIT 5",
  "executionTime": 15,
  "resultCount": 3,
  "results": [
    {
      "className": "UserService"
    },
    {
      "className": "UserController"
    },
    {
      "className": "UserRepository"
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

### 4. Data Management API

#### Clear All Data

```bash
curl -X DELETE http://localhost:8080/api/v1/data/clear
```

#### Get Data Statistics

```bash
curl -X GET http://localhost:8080/api/v1/data/stats
```

**Response:**

```json
{
  "status": "success",
  "statistics": {
    "totalNodes": 150,
    "totalRelationships": 300,
    "nodeTypes": {
      "Class": 25,
      "Method": 100,
      "Field": 20,
      "Package": 5
    },
    "relationshipTypes": {
      "CONTAINS": 150,
      "CALLS": 100,
      "EXTENDS": 25,
      "IMPLEMENTS": 25,
      "USES": 1773
    },
    "repositories": {
      "count": 3,
      "names": ["repo1", "repo2", "repo3"]
    }
  },
  "timestamp": "2025-08-25T13:45:30.123Z"
}
```

#### Clear and Ingest

```bash
curl -X POST http://localhost:8080/api/v1/data/clear-and-ingest \
  -H "Content-Type: application/json" \
  -d '{
    "sourcePath": "/path/to/java/project",
    "includeTestFiles": true
  }'
```

## Graph Query Examples

After ingesting your Java codebase, you can use these Cypher queries to explore the call sequences and relationships between methods. The system now captures comprehensive dependency relationships through the `USES` relationship type, providing deep insights into class dependencies, imports, object instantiation, and type usage patterns.

## Dependency Analysis with USES Relationships

### Overview of USES Relationships

The system captures five types of class dependencies through `USES` relationships:

1. **Import Dependencies** (`type: "import"`) - Classes imported via import statements
2. **Object Instantiation** (`type: "instantiation"`) - Classes instantiated with `new ClassName()`
3. **Parameter Type Dependencies** (`type: "parameter_type"`) - Classes used as method parameters
4. **Return Type Dependencies** (`type: "return_type"`) - Classes used as method return types
5. **Field Type Dependencies** (`type: "field_type"`) - Classes used as field types

Each `USES` relationship includes rich metadata:

- `type`: The type of dependency (import, instantiation, etc.)
- `context`: Where the dependency occurs (method name, field name, etc.)
- `fullyQualifiedName`: Full class name for external dependencies
- `isExternal`: Whether the dependency is external (framework/library class)

### Dependency Analysis Queries

#### 1. Analyze Dependency Types

```cypher
// Count dependencies by type
MATCH ()-[r:USES]->()
RETURN r.type as dependencyType, count(r) as count
ORDER BY count DESC
```

#### 2. Find Classes with Most Dependencies

```cypher
// Find classes with the most outgoing dependencies
MATCH (source:Class)-[r:USES]->(target:Class)
RETURN source.name as className,
       count(r) as dependencyCount,
       collect(DISTINCT r.type) as dependencyTypes
ORDER BY dependencyCount DESC
LIMIT 10
```

#### 3. Analyze Import Dependencies

```cypher
// Find all import dependencies
MATCH (source:Class)-[r:USES]->(target:Class)
WHERE r.type = "import"
RETURN source.name as sourceClass,
       target.name as importedClass,
       r.fullyQualifiedName as fullyQualifiedName
ORDER BY sourceClass, importedClass
LIMIT 20
```

#### 4. Find Object Instantiation Patterns

```cypher
// Find classes that instantiate other classes
MATCH (source:Class)-[r:USES]->(target:Class)
WHERE r.type = "instantiation"
RETURN source.name as sourceClass,
       target.name as instantiatedClass,
       r.context as context
ORDER BY sourceClass, instantiatedClass
```

#### 5. Analyze Method Parameter Dependencies

```cypher
// Find parameter type dependencies
MATCH (source:Class)-[r:USES]->(target:Class)
WHERE r.type = "parameter_type"
RETURN source.name as sourceClass,
       target.name as parameterType,
       r.context as methodContext
ORDER BY sourceClass, parameterType
LIMIT 20
```

#### 6. Find Return Type Dependencies

```cypher
// Find return type dependencies
MATCH (source:Class)-[r:USES]->(target:Class)
WHERE r.type = "return_type"
RETURN source.name as sourceClass,
       target.name as returnType,
       r.context as methodContext
ORDER BY sourceClass, returnType
```

#### 7. Analyze Field Type Dependencies

```cypher
// Find field type dependencies
MATCH (source:Class)-[r:USES]->(target:Class)
WHERE r.type = "field_type"
RETURN source.name as sourceClass,
       target.name as fieldType,
       r.context as fieldContext
ORDER BY sourceClass, fieldType
```

#### 8. Find External Dependencies

```cypher
// Find external library/framework dependencies
MATCH (source:Class)-[r:USES]->(target:Class)
WHERE target.isExternal = true OR target.name IN ["String", "List", "Map", "Optional", "LocalDateTime"]
RETURN source.name as sourceClass,
       target.name as externalClass,
       r.type as dependencyType,
       count(r) as usageCount
ORDER BY usageCount DESC, sourceClass
```

#### 9. Dependency Impact Analysis

```cypher
// Find classes that would be impacted by changes to a specific class
MATCH (dependent:Class)-[r:USES]->(target:Class {name: "YourClassName"})
RETURN dependent.name as dependentClass,
       r.type as dependencyType,
       r.context as context,
       dependent.packageName as dependentPackage
ORDER BY dependentClass
```

#### 10. Package Coupling Analysis

```cypher
// Analyze coupling between packages
MATCH (source:Class)-[r:USES]->(target:Class)
WHERE source.packageName <> target.packageName
RETURN source.packageName as sourcePackage,
       target.packageName as targetPackage,
       count(r) as couplingStrength,
       collect(DISTINCT r.type) as dependencyTypes
ORDER BY couplingStrength DESC
LIMIT 15
```

#### 11. Circular Dependency Detection

```cypher
// Find potential circular dependencies
MATCH (c1:Class)-[:USES]->(c2:Class)-[:USES]->(c1)
WHERE c1 <> c2
RETURN c1.name as class1,
       c2.name as class2,
       c1.packageName as package1,
       c2.packageName as package2
ORDER BY class1, class2
```

#### 12. Architectural Layer Analysis

```cypher
// Analyze dependencies between architectural layers
MATCH (source:Class)-[r:USES]->(target:Class)
WHERE source.name CONTAINS "Controller" OR source.name CONTAINS "Service" OR source.name CONTAINS "Repository"
RETURN
  CASE
    WHEN source.name CONTAINS "Controller" THEN "Controller"
    WHEN source.name CONTAINS "Service" THEN "Service"
    WHEN source.name CONTAINS "Repository" THEN "Repository"
    ELSE "Other"
  END as sourceLayer,
  CASE
    WHEN target.name CONTAINS "Controller" THEN "Controller"
    WHEN target.name CONTAINS "Service" THEN "Service"
    WHEN target.name CONTAINS "Repository" THEN "Repository"
    ELSE "Other"
  END as targetLayer,
  count(r) as dependencyCount
ORDER BY sourceLayer, targetLayer
```

### 1. Basic Graph Exploration Queries

#### Find All Public Methods

```cypher
MATCH (m:Method)
WHERE m.visibility = 'PUBLIC'
RETURN m.name, m.class_name, m.file_path, m.line_start, m.line_end
ORDER BY m.class_name, m.name
LIMIT 20
```

#### Find All Classes with Their Methods

```cypher
MATCH (c:Class)-[:CONTAINS]->(m:Method)
WHERE c.visibility = 'PUBLIC'
RETURN c.name as class_name,
       collect(m.name) as methods,
       c.file_path,
       c.line_start,
       c.line_end
ORDER BY c.name
```

### 2. Method Call Sequence Analysis

#### Find Methods by Name Pattern

```cypher
MATCH (m:Method)
WHERE m.name CONTAINS 'save' OR m.name CONTAINS 'create' OR m.name CONTAINS 'update'
RETURN m.name, m.class_name, m.visibility, m.return_type, m.file_path, m.line_start
ORDER BY m.class_name, m.name
```

#### Find Controller Methods (Common Patterns)

```cypher
MATCH (m:Method)
WHERE m.class_name CONTAINS 'Controller'
   OR m.class_name CONTAINS 'Service'
   OR m.name CONTAINS 'handle'
   OR m.name CONTAINS 'process'
RETURN m.name, m.class_name, m.visibility, m.return_type, m.file_path, m.line_start
ORDER BY m.class_name, m.name
```

#### Find Database-Related Methods

```cypher
MATCH (m:Method)
WHERE m.name CONTAINS 'save'
   OR m.name CONTAINS 'find'
   OR m.name CONTAINS 'query'
   OR m.name CONTAINS 'delete'
   OR m.name CONTAINS 'update'
   OR m.name CONTAINS 'insert'
RETURN m.name, m.class_name, m.visibility, m.return_type, m.file_path, m.line_start
ORDER BY m.class_name, m.name
```

### 3. Call Hierarchy Analysis

#### Find Entry Points (Public Methods in Controllers)

```cypher
MATCH (c:Class)-[:CONTAINS]->(m:Method)
WHERE c.name CONTAINS 'Controller'
   AND m.visibility = 'PUBLIC'
   AND m.name CONTAINS 'handle'
RETURN c.name as controller,
       m.name as entry_method,
       m.file_path,
       m.line_start,
       m.line_end
ORDER BY c.name, m.name
```

#### Find Service Layer Methods

```cypher
MATCH (c:Class)-[:CONTAINS]->(m:Method)
WHERE c.name CONTAINS 'Service'
   AND m.visibility = 'PUBLIC'
RETURN c.name as service_class,
       m.name as service_method,
       m.return_type,
       m.file_path,
       m.line_start,
       m.line_end
ORDER BY c.name, m.name
```

#### Find Repository/DAO Methods

```cypher
MATCH (c:Class)-[:CONTAINS]->(m:Method)
WHERE c.name CONTAINS 'Repository'
   OR c.name CONTAINS 'DAO'
   OR c.name CONTAINS 'Repository'
RETURN c.name as repository_class,
       m.name as repository_method,
       m.visibility,
       m.file_path,
       m.line_start,
       m.line_end
ORDER BY c.name, m.name
```

### 4. Method Parameter Analysis

#### Find Methods with Specific Parameter Types

```cypher
MATCH (m:Method)
WHERE ANY(param IN m.parameters WHERE param CONTAINS 'Request' OR param CONTAINS 'Response')
RETURN m.name, m.class_name, m.parameters, m.file_path, m.line_start
ORDER BY m.class_name, m.name
```

#### Find Methods with Database Entity Parameters

```cypher
MATCH (m:Method)
WHERE ANY(param IN m.parameters WHERE param CONTAINS 'Entity' OR param CONTAINS 'Model' OR param CONTAINS 'DTO')
RETURN m.name, m.class_name, m.parameters, m.file_path, m.line_start
ORDER BY m.class_name, m.name
```

### 5. File Structure Analysis

#### Find Methods by File Path Pattern

```cypher
MATCH (m:Method)
WHERE m.file_path CONTAINS 'controller' OR m.file_path CONTAINS 'Controller'
RETURN m.name, m.class_name, m.file_path, m.line_start, m.line_end
ORDER BY m.file_path, m.line_start
```

#### Find Methods by Package

```cypher
MATCH (m:Method)
WHERE m.package_name CONTAINS 'controller' OR m.package_name CONTAINS 'service'
RETURN m.package_name, m.class_name, m.name, m.visibility, m.file_path, m.line_start
ORDER BY m.package_name, m.class_name, m.name
```

### 6. Complex Call Sequence Analysis

#### Find Methods with Specific Return Types

```cypher
MATCH (m:Method)
WHERE m.return_type CONTAINS 'ResponseEntity'
   OR m.return_type CONTAINS 'Response'
   OR m.return_type CONTAINS 'Result'
RETURN m.name, m.class_name, m.return_type, m.file_path, m.line_start
ORDER BY m.class_name, m.name
```

#### Find Methods with Void Return Type (Potential Entry Points)

```cypher
MATCH (m:Method)
WHERE m.return_type = 'void' AND m.visibility = 'PUBLIC'
RETURN m.name, m.class_name, m.file_path, m.line_start, m.line_end
ORDER BY m.class_name, m.name
```

### 7. Line Number Analysis

#### Find Methods in Specific Line Ranges

```cypher
MATCH (m:Method)
WHERE m.line_start >= 1 AND m.line_end <= 100
RETURN m.name, m.class_name, m.file_path, m.line_start, m.line_end
ORDER BY m.file_path, m.line_start
```

#### Find Large Methods (Many Lines)

```cypher
MATCH (m:Method)
WHERE (m.line_end - m.line_start) > 20
RETURN m.name, m.class_name, (m.line_end - m.line_start) as method_size, m.file_path, m.line_start, m.line_end
ORDER BY method_size DESC
```

### 8. Visibility and Modifier Analysis

#### Find Public Methods by Modifier

```cypher
MATCH (m:Method)
WHERE m.visibility = 'PUBLIC' AND ANY(mod IN m.modifiers WHERE mod = 'STATIC')
RETURN m.name, m.class_name, m.modifiers, m.file_path, m.line_start
ORDER BY m.class_name, m.name
```

#### Find Abstract Methods

```cypher
MATCH (m:Method)
WHERE ANY(mod IN m.modifiers WHERE mod = 'ABSTRACT')
RETURN m.name, m.class_name, m.modifiers, m.file_path, m.line_start
ORDER BY m.class_name, m.name
```

### 9. Comprehensive Call Sequence Query

#### Find Complete Method Call Chain

```cypher
// This query shows the potential call sequence from controllers to services to repositories
MATCH (controller:Class)-[:CONTAINS]->(controllerMethod:Method)
WHERE controller.name CONTAINS 'Controller' AND controllerMethod.visibility = 'PUBLIC'

OPTIONAL MATCH (service:Class)-[:CONTAINS]->(serviceMethod:Method)
WHERE service.name CONTAINS 'Service' AND serviceMethod.visibility = 'PUBLIC'

OPTIONAL MATCH (repository:Class)-[:CONTAINS]->(repositoryMethod:Method)
WHERE repository.name CONTAINS 'Repository' AND repositoryMethod.visibility = 'PUBLIC'

RETURN
    controller.name as controller_class,
    controllerMethod.name as controller_method,
    controllerMethod.file_path as controller_file,
    controllerMethod.line_start as controller_line,

    service.name as service_class,
    serviceMethod.name as service_method,
    serviceMethod.file_path as service_file,
    serviceMethod.line_start as service_line,

    repository.name as repository_class,
    repositoryMethod.name as repository_method,
    repositoryMethod.file_path as repository_file,
    repositoryMethod.line_start as repository_line

ORDER BY controller.name, controllerMethod.name
```

### 10. API Endpoint to Leaf Method Sequence

#### Find REST API Entry Points

```cypher
MATCH (c:Class)-[:CONTAINS]->(m:Method)
WHERE c.name CONTAINS 'Controller'
   AND m.visibility = 'PUBLIC'
   AND (m.name CONTAINS 'get' OR m.name CONTAINS 'post' OR m.name CONTAINS 'put' OR m.name CONTAINS 'delete')
RETURN
    c.name as controller,
    m.name as endpoint_method,
    m.parameters,
    m.return_type,
    m.file_path,
    m.line_start,
    m.line_end
ORDER BY c.name, m.name
```

#### Find Service Methods Called by Controllers

```cypher
MATCH (controller:Class)-[:CONTAINS]->(controllerMethod:Method)
WHERE controller.name CONTAINS 'Controller' AND controllerMethod.visibility = 'PUBLIC'

MATCH (service:Class)-[:CONTAINS]->(serviceMethod:Method)
WHERE service.name CONTAINS 'Service' AND serviceMethod.visibility = 'PUBLIC'

RETURN
    controller.name as controller_class,
    controllerMethod.name as controller_method,
    controllerMethod.file_path as controller_file,
    controllerMethod.line_start as controller_line,

    service.name as service_class,
    serviceMethod.name as service_method,
    serviceMethod.file_path as service_file,
    serviceMethod.line_start as service_line

ORDER BY controller.name, controllerMethod.name, service.name, serviceMethod.name
```

## Using These Queries

### 1. Via Neo4j Browser

1. Open Neo4j Browser at http://localhost:7474
2. Copy and paste any of the Cypher queries above
3. Click "Run" to execute

### 2. Via Cypher REST API

```bash
# Execute any Cypher query via the REST API
curl -X POST http://localhost:8080/api/v1/cypher \
  -H "Content-Type: application/json" \
  -d '{
    "query": "MATCH (m:Method) WHERE m.visibility = \"PUBLIC\" RETURN m.name, m.class_name LIMIT 10"
  }'
```

**Example with Parameters:**

```bash
curl -X POST http://localhost:8080/api/v1/cypher \
  -H "Content-Type: application/json" \
  -d '{
    "query": "MATCH (c:Class) WHERE c.name CONTAINS $className RETURN c.name, c.file_path",
    "parameters": {
      "className": "Service"
    }
  }'
```

### 3. Via Natural Language Query API (Future Implementation)

```bash
# When the natural language query endpoint is implemented in Phase 2
curl -X POST http://localhost:8080/api/v1/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "Find all public methods that call the database",
    "context": {
      "maxResults": 10,
      "includeCode": true
    }
  }'
```

### 3. Via Application Code

```java
// Example of how to execute these queries programmatically
@Service
public class GraphQueryService {

    public List<MethodNode> findPublicMethods() {
        String cypher = """
            MATCH (m:Method)
            WHERE m.visibility = 'PUBLIC'
            RETURN m.name, m.class_name, m.file_path, m.line_start, m.line_end
            ORDER BY m.class_name, m.name
            LIMIT 20
            """;

        // Execute query and map results
        return executeQuery(cypher);
    }
}
```

## Query Results Interpretation

### Understanding the Call Sequence

1. **Entry Points**: Look for methods in Controller classes
2. **Service Layer**: Find methods in Service classes
3. **Data Layer**: Look for Repository/DAO methods
4. **Leaf Methods**: Identify methods that don't call other methods

### Common Patterns

- **Controller → Service → Repository**: Standard layered architecture
- **Controller → Service → External API**: Integration patterns
- **Controller → Service → Service**: Business logic orchestration
- **Controller → Repository**: Direct data access (less common)

### Performance Considerations

- Use `LIMIT` clauses for large result sets
- Add `WHERE` clauses to filter results
- Use `ORDER BY` for consistent results
- Consider indexing frequently queried properties

This comprehensive set of queries allows you to analyze the complete call sequence from API endpoints to leaf methods in your ingested Java codebase, providing insights into the architecture and flow of your application.
