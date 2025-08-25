# Java Graph RAG System

A Graph-based Retrieval-Augmented Generation (RAG) system for Java code analysis. This system parses Java source code into a labeled property graph, enabling natural language queries to retrieve relevant code context.

## Features

- **Java Code Parsing**: Parse Java source files into Abstract Syntax Trees
- **Graph Storage**: Store code elements in Neo4j graph database
- **Natural Language Queries**: Convert prompts to graph queries
- **Code Context Retrieval**: Extract relevant code chunks with line numbers
- **LLM Integration**: Augment prompts with retrieved code context

## Architecture

```
Java Source Code → Parser → Graph Nodes/Edges → Neo4j → Query Engine → Code Context → LLM
```

### Graph Schema

- **Nodes**: Package, Class, Method, Field
- **Relationships**: CONTAINS, EXTENDS, IMPLEMENTS, CALLS, USES, DEPENDS_ON
- **Properties**: Line numbers, visibility, modifiers, file paths

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker and Docker Compose
- Neo4j (will be started via Docker)

## Quick Start

### Automated Setup (Recommended)

```bash
git clone <repository-url>
cd vividcodes-graph-rag
./scripts/setup-dev.sh
```

### Manual Setup

#### 1. Start Neo4j Database

```bash
docker-compose up neo4j -d
```

Wait for Neo4j to be ready (check http://localhost:7474)

#### 2. Build and Run Application

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on http://localhost:8080

### 3. Test the System

#### Health Check

```bash
curl http://localhost:8080/api/v1/health
```

#### Ingest Java Code

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

### 4. Explore the Graph

Open Neo4j Browser at http://localhost:7474 and run queries like:

```cypher
// View all classes
MATCH (c:Class) RETURN c LIMIT 10

// View all public methods
MATCH (m:Method) WHERE m.visibility = 'PUBLIC' RETURN m LIMIT 10

// View class relationships
MATCH (c1:Class)-[r]->(c2:Class) RETURN c1, r, c2 LIMIT 10
```

## API Endpoints

### POST /api/v1/ingest

Ingest Java source code into the graph database.

**Request Body:**

```json
{
  "sourcePath": "/path/to/java/project",
  "filters": {
    "includePrivate": false,
    "includeTests": false,
    "filePatterns": ["*.java"]
  }
}
```

### GET /api/v1/health

Check application health status.

### POST /api/v1/cypher

Execute Cypher queries against the Neo4j graph database.

**Request Body:**

```json
{
  "query": "MATCH (c:Class) RETURN c.name as className LIMIT 5",
  "parameters": {
    "className": "Service"
  },
  "options": {
    "timeout": 30
  }
}
```

### DELETE /api/v1/data/clear

Clear all data from the graph database.

### GET /api/v1/data/stats

Get statistics about the current data in the graph database.

### POST /api/v1/data/clear-and-ingest

Clear all data and immediately ingest new data from a specified path.

**Request Body:**

```json
{
  "sourcePath": "/path/to/java/project",
  "includeTestFiles": true
}
```

## Configuration

### Application Properties

```yaml
spring:
  neo4j:
    uri: bolt://localhost:7687
    authentication:
      username: neo4j
      password: password

parser:
  include-private: false
  include-tests: false
  max-file-size: 10485760 # 10MB

graph:
  batch-size: 1000
  indexes:
    enabled: true
```

## Development

### Project Structure

```
src/
├── main/
│   ├── java/com/vividcodes/graphrag/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST API controllers
│   │   ├── service/         # Business logic services
│   │   ├── model/           # Data models
│   │   └── repository/      # Data access layer
│   └── resources/
│       └── application.yml  # Application configuration
└── test/
    ├── java/                # Unit tests
    └── resources/
        └── test-data/       # Test data files
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=JavaParserServiceTest

# Run with coverage
mvn test jacoco:report
```

### Building for Production

```bash
mvn clean package -Pprod
java -jar target/graph-rag-0.1.0-SNAPSHOT.jar
```

## Docker Deployment

### Build Docker Image

```bash
docker build -t graph-rag:latest .
```

### Run with Docker Compose

```bash
docker-compose up -d
```

## Implementation Status

✅ **Completed (Phase 1 & Core Features):**

- Spring Boot application setup
- Neo4j integration
- Java parser service with JavaParser
- Graph model classes (Package, Class, Method, Field, Repository)
- Repository tracking and Git integration
- Basic REST API for code ingestion
- Cypher query endpoint with validation and caching
- Data management API (clear, stats, clear-and-ingest)
- Docker setup for Neo4j
- Comprehensive unit and integration tests

🔄 **In Progress (Phase 2):**

- Enhanced query capabilities
- Repository visualization queries
- API documentation updates
- Performance testing and optimization

📋 **Next Steps:**

- **Phase 2.5**: LLM MCP Integration
- **Phase 3**: Full LLM Integration
- **Future**: Upsert pattern implementation
- Repository dependency detection
- Code similarity across repositories

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Attribution

This project was coded with assistance of Cursor with Claude Sonnet model.

## Development Guidelines

### Cursor Guidelines

- **[.cursorrules](.cursorrules)** - Comprehensive Cursor guidelines for this project
- **[.editorconfig](.editorconfig)** - Editor configuration for consistent formatting
- **[checkstyle.xml](checkstyle.xml)** - Java code style rules

### Commit Standards

- **[docs/CONVENTIONAL_COMMITS.md](docs/CONVENTIONAL_COMMITS.md)** - Conventional commit message standards
- **Commit Validation**: Git hooks automatically validate commit message format
- **Types**: feat, fix, docs, style, refactor, perf, test, chore, ci, build, revert
- **Scopes**: parser, graph, api, config, test, docs, deps

### Development Scripts

- **[scripts/](scripts/)** - Development and quality assurance scripts
  - **[setup-dev.sh](scripts/setup-dev.sh)** - Complete development environment setup
  - **[pre-commit.sh](scripts/pre-commit.sh)** - Pre-commit quality checks
  - **[validate-commit-msg.sh](scripts/validate-commit-msg.sh)** - Commit message validation

## Documentation

For detailed documentation, see the [docs/](docs/) directory:

- **[Phase 1 Summary](docs/PHASE1_SUMMARY.md)** - Complete overview of Phase 1 implementation
- **[Architecture](docs/ARCHITECTURE.md)** - System architecture and design
- **[API Documentation](docs/API_DOCUMENTATION.md)** - REST API reference
- **[Deployment](docs/DEPLOYMENT.md)** - Deployment guides

## Support

For issues and questions, please create an issue in the repository.
