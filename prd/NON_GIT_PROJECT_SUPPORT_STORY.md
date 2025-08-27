# Non-Git Project Support Story

## Overview

Currently, the system only processes files that are within Git repositories. This story adds support for non-Git projects by creating repository nodes based on directory structure and clearly indicating their Git status.

## Objectives

- Enable code ingestion for projects that haven't been initialized as Git repositories
- Use directory names as repository identifiers for non-Git projects
- Maintain consistent graph structure regardless of Git status
- Provide clear indication of Git status in the graph database
- Allow seamless transition when projects are later initialized as Git repositories

## Timeline

- **Estimated Duration**: 1-2 weeks
- **Priority**: Medium
- **Dependencies**: Phase 1 (Core Infrastructure) - COMPLETED

## Core Requirements

### 1. Non-Git Repository Detection

- [ ] Detect when a file is not within a Git repository
- [ ] Create repository metadata using directory name as repository name
- [ ] Set appropriate flags to indicate non-Git status
- [ ] Handle nested directory structures appropriately

### 2. Repository Metadata Enhancement

- [ ] Add `isGitRepository` boolean field to `RepositoryMetadata`
- [ ] Add `localPath` field to store the local directory path
- [ ] Set default values for Git-specific fields (branch, commit hash, remote URL)
- [ ] Use directory name as repository name for non-Git projects

### 3. Graph Node Updates

- [ ] Update `RepositoryNode` model to include Git status
- [ ] Add properties to distinguish Git vs non-Git repositories
- [ ] Ensure consistent relationship creation regardless of Git status
- [ ] Add metadata properties for local path and Git status

### 4. Service Layer Updates

- [ ] Modify `GitService.createRepositoryMetadata()` to handle non-Git cases
- [ ] Update `RepositoryService` to create non-Git repository nodes
- [ ] Ensure `JavaParserService` works with non-Git repositories
- [ ] Add validation for non-Git repository creation

### 5. API and Query Support

- [ ] Update statistics API to include Git vs non-Git repository counts
- [ ] Add query capabilities to filter by Git status
- [ ] Update documentation to reflect non-Git support
- [ ] Add examples for non-Git project ingestion

## Technical Implementation

### Repository Metadata Structure

```java
public class RepositoryMetadata {
    private String name;                    // Directory name for non-Git
    private String localPath;              // Local directory path
    private boolean isGitRepository;       // Git status flag
    private String repositoryUrl;          // "local" for non-Git
    private String organization;           // "local" for non-Git
    private String branch;                 // "main" for non-Git
    private String commitHash;             // "unknown" for non-Git
    private LocalDateTime commitDate;      // null for non-Git
    private String fileRelativePath;       // Relative to local path
}
```

### Non-Git Repository Creation Logic

```java
// In GitService.createRepositoryMetadata()
if (repoRoot.isPresent()) {
    // Existing Git repository logic
} else {
    // Non-Git project logic
    Path projectRoot = findProjectRoot(filePath);
    String repoName = projectRoot.getFileName().toString();
    String relativePath = projectRoot.relativize(filePath).toString();

    RepositoryMetadata metadata = new RepositoryMetadata(repoName, projectRoot.toString());
    metadata.setGitRepository(false);
    metadata.setRepositoryUrl("local");
    metadata.setOrganization("local");
    metadata.setBranch("main");
    metadata.setCommitHash("unknown");
    metadata.setCommitDate(null);
    metadata.setFileRelativePath(relativePath);

    return metadata;
}
```

### Project Root Detection

```java
private Path findProjectRoot(Path filePath) {
    Path current = filePath.getParent();

    // Look for common project indicators
    while (current != null) {
        if (hasProjectIndicators(current)) {
            return current;
        }
        current = current.getParent();
    }

    // Fallback to immediate parent directory
    return filePath.getParent();
}

private boolean hasProjectIndicators(Path directory) {
    return Files.exists(directory.resolve("pom.xml")) ||           // Maven
           Files.exists(directory.resolve("build.gradle")) ||     // Gradle
           Files.exists(directory.resolve("package.json")) ||      // Node.js
           Files.exists(directory.resolve("src")) ||              // Source directory
           Files.exists(directory.resolve("main"));               // Maven source
}
```

## Validation Cases

### 1. Non-Git Project Ingestion

- [ ] Ingest a directory with Java files but no `.git` directory
- [ ] Verify repository node is created with directory name
- [ ] Verify `isGitRepository` is set to `false`
- [ ] Verify all code elements are properly linked to repository

### 2. Mixed Git/Non-Git Environment

- [ ] Ingest multiple directories, some with Git, some without
- [ ] Verify correct repository detection for each
- [ ] Verify proper relationship creation for all repositories

### 3. Nested Project Structure

- [ ] Test with deeply nested directory structures
- [ ] Verify correct project root detection
- [ ] Verify appropriate repository name assignment

### 4. Git Repository Conversion

- [ ] Test scenario where non-Git project is later initialized as Git
- [ ] Verify system can handle the transition
- [ ] Verify existing nodes are properly updated

## Success Criteria

### Functional Requirements

- [ ] Non-Git projects can be ingested successfully
- [ ] Repository nodes are created with appropriate metadata
- [ ] All code elements are properly linked to repositories
- [ ] Graph queries work correctly for both Git and non-Git repositories
- [ ] Statistics API correctly reports Git vs non-Git counts

### Performance Requirements

- [ ] Non-Git project ingestion performance is comparable to Git projects
- [ ] No significant impact on existing Git repository processing
- [ ] Memory usage remains within acceptable limits

### Quality Requirements

- [ ] Clear distinction between Git and non-Git repositories in the graph
- [ ] Consistent data structure regardless of Git status
- [ ] Proper error handling for edge cases
- [ ] Comprehensive logging for debugging

## Dependencies

- **Phase 1 (Core Infrastructure)** - COMPLETED
- **Repository Tracking Feature** - COMPLETED
- **Git Integration** - COMPLETED

## Deliverables

### Code Changes

- [ ] Updated `RepositoryMetadata` model with Git status fields
- [ ] Enhanced `GitService` with non-Git project detection
- [ ] Updated `RepositoryService` for non-Git repository creation
- [ ] Modified `RepositoryNode` model with Git status properties
- [ ] Updated statistics and query services

### Documentation

- [ ] Updated API documentation for non-Git support
- [ ] Updated user guide with non-Git project examples
- [ ] Updated query examples for Git status filtering
- [ ] Updated architecture documentation

### Testing

- [ ] Unit tests for non-Git repository detection
- [ ] Integration tests for mixed Git/non-Git environments
- [ ] Performance tests for non-Git project ingestion
- [ ] End-to-end tests for complete workflows

## Risk Mitigation

### Technical Risks

- **Risk**: Complex directory structure detection
- **Mitigation**: Start with simple heuristics, iterate based on real-world usage

- **Risk**: Performance impact on existing Git processing
- **Mitigation**: Thorough testing and performance monitoring

### Data Quality Risks

- **Risk**: Inconsistent repository naming for non-Git projects
- **Mitigation**: Clear naming conventions and validation

- **Risk**: Confusion between Git and non-Git repositories
- **Mitigation**: Clear visual indicators and documentation

## Example Usage

### Non-Git Project Ingestion

```bash
# Ingest a non-Git project
curl -X POST http://localhost:8080/api/v1/ingest \
  -H "Content-Type: application/json" \
  -d '{"sourcePath": "/path/to/my-project/src", "includeTestFiles": false}'
```

### Query Non-Git Repositories

```cypher
// Find all non-Git repositories
MATCH (r:Repository)
WHERE r.isGitRepository = false
RETURN r.name, r.localPath, r.organization

// Find all repositories (Git and non-Git)
MATCH (r:Repository)
RETURN r.name, r.isGitRepository, r.organization
ORDER BY r.isGitRepository DESC
```

### Statistics for Non-Git Projects

```json
{
  "statistics": {
    "repositoryCount": 3,
    "gitRepositoryCount": 2,
    "nonGitRepositoryCount": 1,
    "repositoryNames": ["project-a", "project-b", "my-local-project"],
    "organizations": ["github-org", "gitlab-org", "local"]
  }
}
```

## Future Enhancements

### Phase 2.5 Considerations

- MCP server integration should handle both Git and non-Git repositories
- Query composition should include Git status filters
- Code context retrieval should work for all repository types

### Phase 3 Considerations

- LLM integration should understand Git vs non-Git context
- Advanced features should work regardless of Git status
- Migration tools for converting non-Git to Git repositories

## Acceptance Criteria

### Must Have

- [ ] Non-Git projects can be ingested and processed
- [ ] Repository nodes are created with appropriate metadata
- [ ] All code elements are properly linked to repositories
- [ ] Clear distinction between Git and non-Git repositories
- [ ] Existing Git functionality remains unchanged

### Should Have

- [ ] Intelligent project root detection
- [ ] Comprehensive error handling
- [ ] Performance monitoring and optimization
- [ ] Clear documentation and examples

### Could Have

- [ ] Automatic Git repository initialization suggestions
- [ ] Migration tools for non-Git to Git conversion
- [ ] Advanced project structure detection
- [ ] Integration with IDE project detection

### Won't Have

- [ ] Automatic Git repository creation
- [ ] Complex project dependency analysis
- [ ] Integration with external project management tools
- [ ] Real-time Git status monitoring
