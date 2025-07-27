# Java Graph RAG - Project Setup Guide

## Project Structure

```
vividcodes-graph-rag/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── vividcodes/
│   │   │           └── graphrag/
│   │   │               ├── GraphRagApplication.java
│   │   │               ├── config/
│   │   │               │   ├── Neo4jConfig.java
│   │   │               │   ├── LLMConfig.java
│   │   │               │   └── ParserConfig.java
│   │   │               ├── controller/
│   │   │               │   ├── IngestionController.java
│   │   │               │   ├── QueryController.java
│   │   │               │   └── GraphController.java
│   │   │               ├── service/
│   │   │               │   ├── JavaParserService.java
│   │   │               │   ├── GraphService.java
│   │   │               │   ├── QueryService.java
│   │   │               │   ├── LLMService.java
│   │   │               │   └── CodeContextService.java
│   │   │               ├── model/
│   │   │               │   ├── graph/
│   │   │               │   │   ├── PackageNode.java
│   │   │               │   │   ├── ClassNode.java
│   │   │               │   │   ├── MethodNode.java
│   │   │               │   │   └── FieldNode.java
│   │   │               │   ├── dto/
│   │   │               │   │   ├── IngestionRequest.java
│   │   │               │   │   ├── QueryRequest.java
│   │   │               │   │   └── CodeContext.java
│   │   │               │   └── visitor/
│   │   │               │       ├── JavaGraphVisitor.java
│   │   │               │       └── RelationshipDetector.java
│   │   │               └── repository/
│   │   │                   └── Neo4jRepository.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── vividcodes/
│       │           └── graphrag/
│       │               ├── service/
│       │               │   ├── JavaParserServiceTest.java
│       │               │   ├── GraphServiceTest.java
│       │               │   └── QueryServiceTest.java
│       │               └── integration/
│       │                   └── EndToEndTest.java
│       └── resources/
│           ├── test-data/
│           │   └── sample-java-files/
│           └── application-test.yml
├── docs/
│   ├── POC_PRD.md
│   ├── INITIAL_STORY.md
│   ├── TECHNICAL_SPECIFICATION.md
│   └── PROJECT_SETUP.md
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
├── scripts/
│   ├── setup-dev.sh
│   ├── run-tests.sh
│   └── deploy.sh
├── pom.xml
├── .gitignore
├── README.md
└── .env.example
```

## Initial Dependencies (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.vividcodes</groupId>
    <artifactId>graph-rag</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <name>Java Graph RAG</name>
    <description>Graph-based RAG system for Java code analysis</description>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-neo4j</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-graphql</artifactId>
        </dependency>

        <!-- Java Parser -->
        <dependency>
            <groupId>com.github.javaparser</groupId>
            <artifactId>javaparser-core</artifactId>
            <version>3.25.5</version>
        </dependency>

        <!-- LLM Integration -->
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j</artifactId>
            <version>0.24.0</version>
        </dependency>
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-open-ai</artifactId>
            <version>0.24.0</version>
        </dependency>

        <!-- Utilities -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>neo4j</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## Development Environment Setup

### Prerequisites
- Java 17+ (OpenJDK or Oracle JDK)
- Maven 3.8+
- Docker and Docker Compose
- Git
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Local Development Setup

1. **Clone and Setup Project**
```bash
git clone <repository-url>
cd vividcodes-graph-rag
cp .env.example .env
# Edit .env with your configuration
```

2. **Start Neo4j Database**
```bash
docker-compose up neo4j -d
```

3. **Build and Run Application**
```bash
mvn clean install
mvn spring-boot:run
```

4. **Verify Setup**
```bash
# Check Neo4j is running
curl http://localhost:7474

# Check application health
curl http://localhost:8080/actuator/health
```

### Environment Variables (.env)
```bash
# Neo4j Configuration
NEO4J_URI=bolt://localhost:7687
NEO4J_USERNAME=neo4j
NEO4J_PASSWORD=password

# LLM Configuration
OPENAI_API_KEY=your-openai-api-key
LLM_MODEL=gpt-4
LLM_MAX_TOKENS=2000

# Application Configuration
SERVER_PORT=8080
LOG_LEVEL=INFO
```

## Development Workflow

### 1. Code Ingestion Testing
```bash
# Test with sample Java project
curl -X POST http://localhost:8080/api/v1/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "source_path": "./test-data/sample-project",
    "filters": {
      "include_private": false,
      "include_tests": false
    }
  }'
```

### 2. Query Testing
```bash
# Test natural language query
curl -X POST http://localhost:8080/api/v1/query \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "Find all public methods that call database operations",
    "max_results": 5
  }'
```

### 3. Graph Exploration
```bash
# Browse graph in Neo4j Browser
open http://localhost:7474
```

## Testing Strategy

### Unit Tests
```bash
# Run unit tests
mvn test

# Run specific test class
mvn test -Dtest=JavaParserServiceTest

# Run with coverage
mvn test jacoco:report
```

### Integration Tests
```bash
# Run integration tests (requires Docker)
mvn test -Dtest=*IntegrationTest

# Run with TestContainers
mvn test -Dspring.profiles.active=test
```

### Performance Tests
```bash
# Run performance benchmarks
mvn test -Dtest=PerformanceTest

# Load testing
mvn test -Dtest=LoadTest
```

## Deployment

### Docker Build
```bash
# Build Docker image
docker build -t graph-rag:latest .

# Run with Docker Compose
docker-compose up -d
```

### Production Deployment
```bash
# Build for production
mvn clean package -Pprod

# Run with production profile
java -jar target/graph-rag-0.1.0-SNAPSHOT.jar --spring.profiles.active=prod
```

## Monitoring and Logging

### Health Checks
```bash
# Application health
curl http://localhost:8080/actuator/health

# Neo4j health
curl http://localhost:8080/actuator/health/neo4j

# LLM health
curl http://localhost:8080/actuator/health/llm
```

### Metrics
```bash
# Application metrics
curl http://localhost:8080/actuator/metrics

# Graph metrics
curl http://localhost:8080/actuator/metrics/graph.queries
```

## Troubleshooting

### Common Issues

1. **Neo4j Connection Issues**
```bash
# Check Neo4j is running
docker ps | grep neo4j

# Reset Neo4j password
docker exec -it neo4j cypher-shell -u neo4j -p neo4j
```

2. **LLM API Issues**
```bash
# Test OpenAI API
curl -H "Authorization: Bearer $OPENAI_API_KEY" \
  https://api.openai.com/v1/models
```

3. **Memory Issues**
```bash
# Increase JVM heap
export JAVA_OPTS="-Xmx4g -Xms2g"
mvn spring-boot:run
```

## Next Steps

1. **Set up development environment**
2. **Create initial Spring Boot application**
3. **Implement basic Java parser**
4. **Set up Neo4j integration**
5. **Build first graph ingestion pipeline**
6. **Create basic query interface**
7. **Integrate with LLM service**
8. **Add comprehensive testing**
9. **Performance optimization**
10. **Production deployment** 