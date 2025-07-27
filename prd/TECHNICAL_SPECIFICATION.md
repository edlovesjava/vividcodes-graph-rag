# Java Graph RAG - Technical Specification

## System Architecture

### High-Level Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Java Source   │───▶│  Parser Service │───▶│   Neo4j Graph   │
│   Code Files    │    │                 │    │    Database     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │  Query Service  │◀───│  Context Store  │
                       │                 │    │                 │
                       └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │   LLM Service   │
                       │                 │
                       └─────────────────┘
```

## Data Models

### Graph Schema (Neo4j)

#### Node Labels and Properties

**Package Node**
```cypher
(:Package {
  name: String,
  path: String,
  file_path: String,
  line_start: Integer,
  line_end: Integer,
  created_at: DateTime,
  updated_at: DateTime
})
```

**Class Node**
```cypher
(:Class {
  name: String,
  visibility: String, // PUBLIC, PRIVATE, PROTECTED, PACKAGE_PRIVATE
  modifiers: List<String>, // ABSTRACT, FINAL, STATIC, etc.
  is_interface: Boolean,
  is_enum: Boolean,
  is_annotation: Boolean,
  file_path: String,
  line_start: Integer,
  line_end: Integer,
  package_name: String,
  created_at: DateTime,
  updated_at: DateTime
})
```

**Method Node**
```cypher
(:Method {
  name: String,
  visibility: String,
  modifiers: List<String>,
  return_type: String,
  parameters: List<String>, // JSON array of parameter types
  parameter_names: List<String>,
  file_path: String,
  line_start: Integer,
  line_end: Integer,
  class_name: String,
  package_name: String,
  created_at: DateTime,
  updated_at: DateTime
})
```

**Field Node**
```cypher
(:Field {
  name: String,
  visibility: String,
  modifiers: List<String>,
  type: String,
  file_path: String,
  line_number: Integer,
  class_name: String,
  package_name: String,
  created_at: DateTime,
  updated_at: DateTime
})
```

#### Relationship Types

```cypher
// Containment relationships
(:Package)-[:CONTAINS]->(:Class)
(:Class)-[:CONTAINS]->(:Method)
(:Class)-[:CONTAINS]->(:Field)

// Inheritance relationships
(:Class)-[:EXTENDS]->(:Class)
(:Class)-[:IMPLEMENTS]->(:Class)

// Method relationships
(:Method)-[:CALLS]->(:Method)
(:Method)-[:USES]->(:Field)
(:Method)-[:USES]->(:Class)

// Dependency relationships
(:Class)-[:DEPENDS_ON]->(:Class)
(:Package)-[:DEPENDS_ON]->(:Package)

// Import relationships
(:Class)-[:IMPORTS]->(:Class)
(:Package)-[:IMPORTS]->(:Package)
```

## API Specifications

### REST API Endpoints

#### Code Ingestion
```http
POST /api/v1/ingest
Content-Type: application/json

{
  "source_path": "/path/to/java/project",
  "filters": {
    "include_private": false,
    "include_tests": false,
    "file_patterns": ["*.java"]
  }
}
```

#### Query Processing
```http
POST /api/v1/query
Content-Type: application/json

{
  "prompt": "Find all public methods that call the database",
  "max_results": 10,
  "include_code": true
}
```

#### Code Context Retrieval
```http
GET /api/v1/context/{node_id}
```

#### Graph Exploration
```http
GET /api/v1/graph/classes/{class_name}
GET /api/v1/graph/methods/{method_name}
GET /api/v1/graph/packages/{package_name}
```

### GraphQL Schema
```graphql
type Query {
  searchClasses(name: String): [Class]
  searchMethods(name: String): [Method]
  getClassContext(className: String!): ClassContext
  getMethodContext(methodName: String!): MethodContext
}

type Class {
  id: ID!
  name: String!
  visibility: String!
  modifiers: [String!]!
  filePath: String!
  lineStart: Int!
  lineEnd: Int!
  methods: [Method!]!
  fields: [Field!]!
  extends: [Class!]
  implements: [Class!]
}

type Method {
  id: ID!
  name: String!
  visibility: String!
  returnType: String!
  parameters: [String!]!
  calls: [Method!]!
  uses: [Field!]!
}
```

## Implementation Details

### Java Parser Service

#### AST Visitor Implementation
```java
public class JavaGraphVisitor extends VoidVisitorAdapter<Void> {
    private final GraphService graphService;
    private final String filePath;
    private final String packageName;
    
    @Override
    public void visit(ClassOrInterfaceDeclaration classDecl, Void arg) {
        if (classDecl.isPublic()) {
            ClassNode classNode = createClassNode(classDecl);
            graphService.saveClass(classNode);
            
            // Visit methods and fields
            super.visit(classDecl, arg);
        }
    }
    
    @Override
    public void visit(MethodDeclaration methodDecl, Void arg) {
        if (methodDecl.isPublic()) {
            MethodNode methodNode = createMethodNode(methodDecl);
            graphService.saveMethod(methodNode);
        }
    }
}
```

#### Relationship Detection
```java
public class RelationshipDetector {
    public void detectMethodCalls(MethodDeclaration method) {
        method.findAll(MethodCallExpr.class).forEach(call -> {
            String calledMethod = call.getNameAsString();
            String calledClass = resolveClassName(call);
            
            if (calledClass != null) {
                graphService.createRelationship(
                    method.getNameAsString(),
                    calledMethod,
                    "CALLS",
                    Map.of("line", call.getBegin().get().line)
                );
            }
        });
    }
}
```

### Query Processing Pipeline

#### Natural Language to Cypher Conversion
```java
@Service
public class QueryProcessor {
    
    public String convertPromptToCypher(String prompt) {
        String systemPrompt = """
            Convert the following natural language query into a Cypher query for Neo4j.
            The graph contains Java code elements: Package, Class, Method, Field.
            Relationships: CONTAINS, EXTENDS, IMPLEMENTS, CALLS, USES, DEPENDS_ON.
            Return only the Cypher query, no explanations.
            """;
            
        return llmService.generate(systemPrompt + "\n\nQuery: " + prompt);
    }
    
    public List<CodeContext> executeQuery(String cypherQuery) {
        List<Map<String, Object>> results = neo4jService.executeQuery(cypherQuery);
        return results.stream()
            .map(this::mapToCodeContext)
            .collect(Collectors.toList());
    }
}
```

#### Code Context Retrieval
```java
public class CodeContextService {
    
    public CodeContext retrieveContext(String filePath, int lineStart, int lineEnd) {
        String codeChunk = readFileLines(filePath, lineStart, lineEnd);
        
        return CodeContext.builder()
            .filePath(filePath)
            .lineStart(lineStart)
            .lineEnd(lineEnd)
            .code(codeChunk)
            .build();
    }
    
    public String augmentPrompt(String originalPrompt, List<CodeContext> contexts) {
        StringBuilder augmented = new StringBuilder(originalPrompt);
        augmented.append("\n\nRelevant code context:\n");
        
        for (CodeContext context : contexts) {
            augmented.append(String.format("File: %s (lines %d-%d)\n", 
                context.getFilePath(), context.getLineStart(), context.getLineEnd()));
            augmented.append("```java\n");
            augmented.append(context.getCode());
            augmented.append("\n```\n\n");
        }
        
        return augmented.toString();
    }
}
```

## Configuration

### Application Properties
```yaml
# Neo4j Configuration
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=password

# LLM Configuration
llm.provider=openai
llm.api.key=${OPENAI_API_KEY}
llm.model=gpt-4
llm.max-tokens=2000

# Parser Configuration
parser.include-private=false
parser.include-tests=false
parser.max-file-size=10MB
parser.supported-extensions=java

# Graph Configuration
graph.batch-size=1000
graph.indexes.enabled=true
graph.cache.enabled=true
```

## Performance Considerations

### Indexing Strategy
```cypher
// Create indexes for common queries
CREATE INDEX class_name_index FOR (c:Class) ON (c.name);
CREATE INDEX method_name_index FOR (m:Method) ON (m.name);
CREATE INDEX package_name_index FOR (p:Package) ON (p.name);
CREATE INDEX file_path_index FOR (c:Class) ON (c.file_path);
```

### Batch Processing
```java
@Service
public class BatchProcessor {
    
    public void processBatch(List<JavaFile> files) {
        int batchSize = 1000;
        
        for (int i = 0; i < files.size(); i += batchSize) {
            List<JavaFile> batch = files.subList(i, Math.min(i + batchSize, files.size()));
            processBatch(batch);
        }
    }
}
```

## Testing Strategy

### Unit Tests
- Parser accuracy tests with sample Java files
- Graph query generation tests
- Code context retrieval tests

### Integration Tests
- End-to-end ingestion pipeline
- Query processing with real Neo4j instance
- LLM integration tests

### Performance Tests
- Large codebase ingestion (100k+ lines)
- Query response time benchmarks
- Memory usage profiling

## Deployment

### Docker Configuration
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app
COPY target/graph-rag-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose
```yaml
version: '3.8'
services:
  neo4j:
    image: neo4j:5.15
    environment:
      NEO4J_AUTH: neo4j/password
    ports:
      - "7474:7474"
      - "7687:7687"
    volumes:
      - neo4j_data:/data
      
  graph-rag:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_NEO4J_URI: bolt://neo4j:7687
    depends_on:
      - neo4j

volumes:
  neo4j_data:
``` 