# Repository Tracking Feature Story

## Overview

Add comprehensive repository tracking capabilities to the Java Graph RAG system to enable tracking of source code origins, repository metadata, and version control information.

## Problem Statement

Currently, the system only tracks local file paths but cannot identify:

- Which Git repository a source file belongs to
- Repository metadata (organization, name, URL)
- Branch and commit information
- Repository relationships between different codebases
- Historical tracking of code across repositories

This limits the system's ability to:

- Correlate code across multiple repositories
- Track code evolution and origins
- Provide repository-aware code analysis
- Support multi-repository RAG queries

## Business Value

- **Multi-Repository Analysis**: Analyze code patterns across different repositories
- **Code Origin Tracking**: Understand where specific code patterns originated
- **Repository Relationships**: Discover dependencies between repositories
- **Enhanced RAG**: Provide repository context in code retrieval
- **Enterprise Support**: Better support for organizations with multiple repositories

## User Stories

### Epic: Repository Metadata Tracking

**As a** developer analyzing code across multiple repositories  
**I want** to see which repository each class/method belongs to  
**So that** I can understand code origins and relationships

**Acceptance Criteria:**

- [ ] Repository information is captured during code ingestion
- [ ] Repository metadata is stored in the graph database
- [ ] Repository information is queryable via Cypher
- [ ] Repository relationships can be visualized

### Epic: Git Information Integration

**As a** code analyst  
**I want** to see branch and commit information for source files  
**So that** I can track code evolution and versions

**Acceptance Criteria:**

- [ ] Git branch information is captured
- [ ] Commit hashes are stored for files
- [ ] Git history can be queried
- [ ] Version differences can be analyzed

### Epic: Multi-Repository Queries

**As a** developer working across multiple projects  
**I want** to query code patterns across repositories  
**So that** I can find similar implementations and avoid duplication

**Acceptance Criteria:**

- [ ] Cross-repository code search works
- [ ] Repository-aware code similarity detection
- [ ] Repository relationship queries are supported
- [ ] Performance is acceptable for large repository sets

## Technical Design

### 1. New Graph Schema

#### Repository Node

```cypher
(:Repository {
  id: String!,
  name: String!,
  organization: String,
  url: String!,
  clone_url: String!,
  default_branch: String,
  created_at: DateTime,
  updated_at: DateTime,
  last_commit_hash: String,
  last_commit_date: DateTime,
  total_files: Integer,
  language_stats: Map
})
```

#### Enhanced Source File Nodes

```cypher
// Add repository properties to existing nodes
(:Class {
  // ... existing properties ...
  repository_id: String!,
  repository_name: String!,
  repository_url: String!,
  branch: String,
  commit_hash: String,
  commit_date: DateTime,
  file_relative_path: String
})

(:Method {
  // ... existing properties ...
  repository_id: String!,
  repository_name: String!,
  repository_url: String!,
  branch: String,
  commit_hash: String
})

(:Field {
  // ... existing properties ...
  repository_id: String!,
  repository_name: String!,
  repository_url: String!,
  branch: String,
  commit_hash: String
})

(:Package {
  // ... existing properties ...
  repository_id: String!,
  repository_name: String!,
  repository_url: String!,
  branch: String,
  commit_hash: String
})
```

#### New Relationships

```cypher
(:Repository)-[:CONTAINS]->(:Package)
(:Repository)-[:CONTAINS]->(:Class)
(:Repository)-[:CONTAINS]->(:Method)
(:Repository)-[:CONTAINS]->(:Field)
(:Repository)-[:DEPENDS_ON]->(:Repository)  // Repository dependencies
(:Repository)-[:SHARES_CODE_WITH]->(:Repository)  // Code similarity
```

### 2. New Model Classes

#### RepositoryNode.java

```java
@Node("Repository")
public class RepositoryNode {
    @Id
    private String id;

    @Property("name")
    private String name;

    @Property("organization")
    private String organization;

    @Property("url")
    private String url;

    @Property("clone_url")
    private String cloneUrl;

    @Property("default_branch")
    private String defaultBranch;

    @Property("created_at")
    private LocalDateTime createdAt;

    @Property("updated_at")
    private LocalDateTime updatedAt;

    @Property("last_commit_hash")
    private String lastCommitHash;

    @Property("last_commit_date")
    private LocalDateTime lastCommitDate;

    @Property("total_files")
    private Integer totalFiles;

    @Property("language_stats")
    private Map<String, Object> languageStats;

    // Constructor, getters, setters
}
```

#### RepositoryMetadata.java

```java
public class RepositoryMetadata {
    private String repositoryId;
    private String repositoryName;
    private String repositoryUrl;
    private String branch;
    private String commitHash;
    private LocalDateTime commitDate;
    private String fileRelativePath;

    // Constructor, getters, setters
}
```

### 3. Enhanced Service Layer

#### RepositoryService.java

```java
@Service
public class RepositoryService {

    /**
     * Detect repository information from a file path
     */
    public RepositoryMetadata detectRepositoryMetadata(Path filePath);

    /**
     * Extract Git information from a repository
     */
    public GitMetadata extractGitMetadata(Path repositoryPath);

    /**
     * Create or update repository node
     */
    public RepositoryNode createOrUpdateRepository(RepositoryMetadata metadata);

    /**
     * Link source file nodes to repository
     */
    public void linkNodesToRepository(List<GraphNode> nodes, RepositoryNode repository);
}
```

#### GitService.java

```java
@Service
public class GitService {

    /**
     * Check if path is a Git repository
     */
    public boolean isGitRepository(Path path);

    /**
     * Get current branch
     */
    public String getCurrentBranch(Path repositoryPath);

    /**
     * Get last commit hash
     */
    public String getLastCommitHash(Path repositoryPath);

    /**
     * Get commit date
     */
    public LocalDateTime getCommitDate(Path repositoryPath);

    /**
     * Get repository remote URL
     */
    public String getRemoteUrl(Path repositoryPath);
}
```

### 4. Enhanced Parser Service

#### JavaParserService Updates

```java
@Service
public class JavaParserService {

    private final RepositoryService repositoryService;
    private final GitService gitService;

    /**
     * Enhanced parsing with repository detection
     */
    private void parseJavaFile(final Path filePath) throws FileNotFoundException {
        // ... existing parsing logic ...

        // Detect repository information
        RepositoryMetadata repoMetadata = repositoryService.detectRepositoryMetadata(filePath);
        RepositoryNode repository = repositoryService.createOrUpdateRepository(repoMetadata);

        // Create nodes with repository information
        JavaGraphVisitor visitor = new JavaGraphVisitor(filePath.toString(), repoMetadata);

        // ... rest of parsing logic ...

        // Link nodes to repository
        repositoryService.linkNodesToRepository(createdNodes, repository);
    }
}
```

### 5. Enhanced Graph Service

#### GraphServiceImpl Updates

```java
@Service
public class GraphServiceImpl implements GraphService {

    /**
     * Create repository node
     */
    public RepositoryNode createRepository(RepositoryMetadata metadata);

    /**
     * Link nodes to repository
     */
    public void linkNodesToRepository(List<GraphNode> nodes, RepositoryNode repository);

    /**
     * Find repositories by organization
     */
    public List<RepositoryNode> findRepositoriesByOrganization(String organization);

    /**
     * Find similar code across repositories
     */
    public List<CodeSimilarityResult> findSimilarCodeAcrossRepositories(String codePattern);
}
```

## Implementation Phases

### Phase 1: Core Repository Tracking (Week 1-2)

- [ ] Create RepositoryNode model
- [ ] Implement RepositoryService
- [ ] Implement GitService
- [ ] Update existing node models with repository properties
- [ ] Modify JavaParserService to detect repositories
- [ ] Update GraphService to handle repository operations

### Phase 2: Enhanced Queries (Week 3)

- [ ] Add repository-aware Cypher queries
- [ ] Implement cross-repository search
- [ ] Add repository relationship queries
- [ ] Create repository visualization queries

### Phase 3: Advanced Features (Week 4)

- [ ] Implement repository dependency detection
- [ ] Add code similarity across repositories
- [ ] Implement repository statistics and metrics
- [ ] Add repository health monitoring

### Phase 4: Testing & Documentation (Week 5)

- [ ] Write comprehensive tests
- [ ] Update API documentation
- [ ] Create usage examples
- [ ] Performance testing and optimization

## Database Migration

### Neo4j Schema Updates

```cypher
// Create repository index
CREATE INDEX repository_id_index FOR (r:Repository) ON (r.id);
CREATE INDEX repository_name_index FOR (r:Repository) ON (r.name);
CREATE INDEX repository_organization_index FOR (r:Repository) ON (r.organization);

// Create repository property indexes on existing nodes
CREATE INDEX class_repository_id_index FOR (c:Class) ON (c.repository_id);
CREATE INDEX method_repository_id_index FOR (m:Method) ON (m.repository_id);
CREATE INDEX field_repository_id_index FOR (f:Field) ON (f.repository_id);
CREATE INDEX package_repository_id_index FOR (p:Package) ON (p.repository_id);
```

## API Enhancements

### New Endpoints

```http
# Get all repositories
GET /api/v1/repositories

# Get repository by ID
GET /api/v1/repositories/{id}

# Get repository statistics
GET /api/v1/repositories/{id}/stats

# Find similar code across repositories
POST /api/v1/repositories/similar-code
{
  "codePattern": "public class UserService",
  "language": "java",
  "maxResults": 10
}

# Get repository dependencies
GET /api/v1/repositories/{id}/dependencies
```

### Enhanced Ingestion Endpoint

```http
POST /api/v1/ingest
{
  "sourcePath": "/path/to/repository",
  "repositoryInfo": {
    "name": "user-service",
    "organization": "mycompany",
    "url": "https://github.com/mycompany/user-service"
  },
  "filters": {
    "includePrivate": false,
    "includeTests": false
  }
}
```

## Testing Strategy

### Unit Tests

- [ ] RepositoryService tests
- [ ] GitService tests
- [ ] Enhanced model tests
- [ ] Service integration tests

### Integration Tests

- [ ] Repository detection tests
- [ ] Git metadata extraction tests
- [ ] Cross-repository query tests
- [ ] Performance tests

### Test Data

- [ ] Mock Git repositories
- [ ] Sample multi-repository scenarios
- [ ] Performance test datasets

## Performance Considerations

### Database Optimization

- [ ] Proper indexing strategy
- [ ] Query optimization
- [ ] Batch operations for large repositories
- [ ] Connection pooling

### Caching Strategy

- [ ] Repository metadata caching
- [ ] Git information caching
- [ ] Query result caching
- [ ] Cache invalidation strategy

## Security Considerations

### Access Control

- [ ] Repository access permissions
- [ ] Organization-based access control
- [ ] Private repository handling
- [ ] API authentication for repository operations

### Data Privacy

- [ ] Sensitive repository information handling
- [ ] User access logging
- [ ] Data retention policies

## Monitoring & Observability

### Metrics

- [ ] Repository ingestion performance
- [ ] Cross-repository query performance
- [ ] Repository relationship complexity
- [ ] Error rates and failure modes

### Logging

- [ ] Repository detection logs
- [ ] Git operation logs
- [ ] Cross-repository query logs
- [ ] Performance monitoring logs

## Future Enhancements

### Phase 5: Advanced Repository Features

- [ ] Repository health scoring
- [ ] Code quality metrics per repository
- [ ] Repository dependency graph visualization
- [ ] Automated repository discovery

### Phase 6: Enterprise Features

- [ ] Multi-tenant repository support
- [ ] Repository access control integration
- [ ] Repository audit logging
- [ ] Repository compliance reporting

## Success Metrics

### Technical Metrics

- [ ] Repository detection accuracy: >95%
- [ ] Cross-repository query performance: <2s for typical queries
- [ ] Repository metadata completeness: >90%
- [ ] System uptime: >99.9%

### Business Metrics

- [ ] Multi-repository analysis adoption
- [ ] Cross-repository code reuse identification
- [ ] Repository relationship discovery rate
- [ ] User satisfaction with repository features

## Risk Assessment

### Technical Risks

- **Git Integration Complexity**: Git operations can be complex and error-prone
- **Performance Impact**: Cross-repository queries may be slower
- **Data Consistency**: Repository metadata may become stale

### Mitigation Strategies

- **Robust Git Service**: Comprehensive error handling and fallbacks
- **Query Optimization**: Careful indexing and query design
- **Metadata Refresh**: Regular repository metadata updates

## Dependencies

### External Dependencies

- [ ] JGit library for Git operations
- [ ] Enhanced Neo4j query capabilities
- [ ] Git command-line tools availability

### Internal Dependencies

- [ ] Existing graph model stability
- [ ] JavaParserService refactoring
- [ ] GraphService enhancement
- [ ] Test infrastructure updates

## Conclusion

Adding repository tracking capabilities will significantly enhance the Java Graph RAG system's value by enabling:

1. **Multi-repository analysis** and code pattern discovery
2. **Code origin tracking** and evolution analysis
3. **Repository relationship mapping** and dependency analysis
4. **Enhanced RAG capabilities** with repository context

This feature will position the system as a comprehensive enterprise-grade code analysis tool capable of handling complex, multi-repository development environments.
