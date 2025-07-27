# Graph Query Sequence Flow

This document illustrates the complete sequence flow from a graph query API call through to the leaf method calls in the Neo4j container.

## Sequence Diagram

```mermaid
sequenceDiagram
    participant Client as Client
    participant Controller as IngestionController
    participant ParserService as JavaParserService
    participant GraphService as GraphService
    participant GraphImpl as GraphServiceImpl
    participant Neo4jDriver as Neo4j Driver
    participant Neo4jSession as Neo4j Session
    participant Neo4jDB as Neo4j Database
    participant ASTVisitor as JavaGraphVisitor
    participant JavaParser as JavaParser Library

    Note over Client,Neo4jDB: Graph Query Flow - From API to Neo4j Container

    %% API Call Flow
    Client->>Controller: POST /api/v1/ingest
    Note right of Controller: Request Body: {sourcePath, filters}

    %% Controller Processing
    Controller->>Controller: Validate request body
    Controller->>Controller: Log ingestion start

    %% Parser Service Flow
    Controller->>ParserService: parseDirectory(sourcePath)
    ParserService->>ParserService: Validate source path exists
    ParserService->>ParserService: findJavaFiles(rootPath)

    %% File Discovery
    ParserService->>ParserService: shouldIncludeFile(filePath)
    Note right of ParserService: Check file size, extensions, filters

    %% For each Java file found
    loop For each Java file
        ParserService->>ParserService: parseJavaFile(filePath)
        ParserService->>JavaParser: parse(filePath)
        JavaParser->>ASTVisitor: visit(CompilationUnit)

        %% AST Visitor Processing
        ASTVisitor->>ASTVisitor: visit(PackageDeclaration)
        ASTVisitor->>ASTVisitor: visit(ClassOrInterfaceDeclaration)
        ASTVisitor->>ASTVisitor: visit(MethodDeclaration)
        ASTVisitor->>ASTVisitor: visit(FieldDeclaration)

        %% Node Creation
        ASTVisitor->>ASTVisitor: createClassNode(classDecl)
        ASTVisitor->>ASTVisitor: createMethodNode(methodDecl)
        ASTVisitor->>ASTVisitor: createFieldNode(fieldName, fieldDecl)

        %% Graph Service Calls
        ASTVisitor->>GraphService: saveClass(classNode)
        GraphService->>GraphImpl: saveClass(classNode)
        GraphImpl->>Neo4jDriver: session()
        Neo4jDriver->>Neo4jSession: session.run(cypher, parameters)
        Neo4jSession->>Neo4jDB: Execute MERGE query
        Neo4jDB-->>Neo4jSession: Query result
        Neo4jSession-->>Neo4jDriver: Result
        Neo4jDriver-->>GraphImpl: Success
        GraphImpl-->>GraphService: Success
        GraphService-->>ASTVisitor: Success

        %% Method Node Processing
        ASTVisitor->>GraphService: saveMethod(methodNode)
        GraphService->>GraphImpl: saveMethod(methodNode)
        GraphImpl->>Neo4jDriver: session()
        Neo4jDriver->>Neo4jSession: session.run(cypher, parameters)
        Neo4jSession->>Neo4jDB: Execute MERGE query
        Neo4jDB-->>Neo4jSession: Query result
        Neo4jSession-->>Neo4jDriver: Result
        Neo4jDriver-->>GraphImpl: Success
        GraphImpl-->>GraphService: Success
        GraphService-->>ASTVisitor: Success

        %% Field Node Processing
        ASTVisitor->>GraphService: saveField(fieldNode)
        GraphService->>GraphImpl: saveField(fieldNode)
        GraphImpl->>Neo4jDriver: session()
        Neo4jDriver->>Neo4jSession: session.run(cypher, parameters)
        Neo4jSession->>Neo4jDB: Execute MERGE query
        Neo4jDB-->>Neo4jSession: Query result
        Neo4jSession-->>Neo4jDriver: Result
        Neo4jDriver-->>GraphImpl: Success
        GraphImpl-->>GraphService: Success
        GraphService-->>ASTVisitor: Success

        %% Relationship Creation (if implemented)
        ASTVisitor->>GraphService: createRelationship(fromId, toId, type)
        GraphService->>GraphImpl: createRelationship(fromId, toId, type)
        GraphImpl->>Neo4jDriver: session()
        Neo4jDriver->>Neo4jSession: session.run(cypher, parameters)
        Neo4jSession->>Neo4jDB: Execute MATCH/MERGE query
        Neo4jDB-->>Neo4jSession: Query result
        Neo4jSession-->>Neo4jDriver: Result
        Neo4jDriver-->>GraphImpl: Success
        GraphImpl-->>GraphService: Success
        GraphService-->>ASTVisitor: Success
    end

    %% Completion
    ParserService-->>Controller: Parsing completed
    Controller->>Controller: Log completion
    Controller-->>Client: Success response

    Note over Client,Neo4jDB: Error Handling Flow

    %% Error Scenarios
    alt File not found
        ParserService-->>Controller: IllegalArgumentException
        Controller-->>Client: 400 Bad Request
    else Database error
        Neo4jDB-->>Neo4jSession: Database error
        Neo4jSession-->>Neo4jDriver: Exception
        Neo4jDriver-->>GraphImpl: RuntimeException
        GraphImpl-->>GraphService: RuntimeException
        GraphService-->>ASTVisitor: RuntimeException
        ASTVisitor-->>ParserService: RuntimeException
        ParserService-->>Controller: RuntimeException
        Controller-->>Client: 500 Internal Server Error
    else Parsing error
        JavaParser-->>ParserService: ParseException
        ParserService-->>Controller: RuntimeException
        Controller-->>Client: 500 Internal Server Error
    end
```

## Key Components in the Flow

### 1. **API Layer**

- **IngestionController**: Handles HTTP requests and responses
- **Request Validation**: Validates input parameters
- **Error Handling**: Returns appropriate HTTP status codes

### 2. **Parser Service**

- **JavaParserService**: Orchestrates the parsing process
- **File Discovery**: Recursively finds Java files
- **File Filtering**: Applies size and extension filters
- **AST Processing**: Uses JavaParser to create Abstract Syntax Trees

### 3. **AST Visitor**

- **JavaGraphVisitor**: Custom visitor for extracting code elements
- **Node Creation**: Creates ClassNode, MethodNode, FieldNode objects
- **Metadata Extraction**: Extracts line numbers, visibility, modifiers

### 4. **Graph Service**

- **GraphService Interface**: Abstract service for graph operations
- **GraphServiceImpl**: Neo4j implementation
- **CRUD Operations**: Save operations for all node types
- **Relationship Management**: Creates relationships between nodes

### 5. **Neo4j Integration**

- **Neo4j Driver**: Database connection management
- **Session Management**: Handles database sessions
- **Cypher Queries**: Executes parameterized Cypher queries
- **Transaction Handling**: Manages database transactions

## Database Operations

### Node Creation Queries

```cypher
// Class Node
MERGE (c:Class {id: $id})
SET c.name = $name,
    c.visibility = $visibility,
    c.modifiers = $modifiers,
    c.file_path = $filePath,
    c.line_start = $lineStart,
    c.line_end = $lineEnd

// Method Node
MERGE (m:Method {id: $id})
SET m.name = $name,
    m.visibility = $visibility,
    m.return_type = $returnType,
    m.parameters = $parameters,
    m.file_path = $filePath,
    m.line_start = $lineStart,
    m.line_end = $lineEnd

// Field Node
MERGE (f:Field {id: $id})
SET f.name = $name,
    f.visibility = $visibility,
    f.type = $type,
    f.file_path = $filePath,
    f.line_number = $lineNumber
```

### Relationship Creation

```cypher
// Relationship between nodes
MATCH (from), (to)
WHERE from.id = $fromId AND to.id = $toId
MERGE (from)-[r:RELATIONSHIP_TYPE]->(to)
```

## Error Handling

### 1. **File System Errors**

- Invalid source path
- File size exceeds limits
- Permission denied

### 2. **Parsing Errors**

- Invalid Java syntax
- Unsupported language features
- Memory issues with large files

### 3. **Database Errors**

- Connection failures
- Query execution errors
- Transaction rollbacks

### 4. **Application Errors**

- Null pointer exceptions
- Validation failures
- Configuration errors

## Performance Considerations

### 1. **Batch Processing**

- Process multiple files efficiently
- Use connection pooling
- Implement batch inserts

### 2. **Memory Management**

- Stream large files
- Clean up AST objects
- Monitor heap usage

### 3. **Database Optimization**

- Use parameterized queries
- Implement proper indexing
- Monitor query performance

## Monitoring and Logging

### 1. **Application Logs**

- Request/response logging
- Parsing progress tracking
- Error details and stack traces

### 2. **Database Monitoring**

- Query execution times
- Connection pool usage
- Transaction statistics

### 3. **Performance Metrics**

- Files processed per second
- Database operations per second
- Memory usage patterns

This sequence diagram provides a comprehensive view of how the Java Graph RAG system processes code ingestion requests from the API layer through to the Neo4j database container, showing all the key interactions and error handling paths.
