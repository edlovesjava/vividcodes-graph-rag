# STORY_004_NON_GIT_PROJECT_SUPPORT

## Story Information

- **Story Number**: STORY_004
- **Story Name**: Non-Git Project Support
- **Epic**: Repository Tracking Feature
- **Priority**: MEDIUM
- **Estimated Duration**: 1-2 weeks
- **Dependencies**: STORY_001_REPOSITORY_TRACKING (COMPLETED)
- **Status**: PARTIALLY_IMPLEMENTED

## Overview

Currently, the system only processes files that are within Git repositories. This story adds support for non-Git projects by creating repository nodes based on directory structure and clearly indicating their Git status.

## User Story

**As a** developer working on projects that haven't been initialized as Git repositories  
**I want** the system to process and organize my code using directory names as repository identifiers  
**So that** I can analyze my code structure even before setting up version control

## Background

Many development projects start without Git initialization, especially during prototyping, learning, or when working with legacy codebases. Currently, when such projects are ingested, the system fails to create repository metadata and doesn't establish proper relationships between code elements and their source location.

This creates a gap where:

- Non-Git projects cannot be properly analyzed
- Code elements lack repository context
- No clear indication of Git status in the graph
- Inconsistent data structure for projects with different Git states

## Acceptance Criteria

- [x] Detect when a file is not within a Git repository
- [x] Create repository metadata using directory name as repository name
- [x] Set appropriate flags to indicate non-Git status
- [x] Handle nested directory structures appropriately
- [x] Add `localPath` field to store the local directory path
- [x] Set default values for Git-specific fields (branch, commit hash, remote URL)
- [x] Use directory name as repository name for non-Git projects
- [x] Update `RepositoryNode` model to include local path
- [x] Add properties to distinguish Git vs non-Git repositories
- [x] Ensure consistent relationship creation regardless of Git status
- [x] Add metadata properties for local path
- [ ] Add `isGitRepository` boolean field to `RepositoryMetadata`
- [ ] Update statistics API to include Git vs non-Git repository counts
- [ ] Add query capabilities to filter by Git status
- [ ] Update documentation to reflect non-Git support
- [ ] Add examples for non-Git project ingestion

## Technical Requirements

### Functional Requirements

- [x] Detect when a file is not within a Git repository
- [x] Create repository metadata using directory name as repository name
- [x] Set appropriate flags to indicate non-Git status
- [x] Handle nested directory structures appropriately
- [ ] Add `isGitRepository` boolean field to `RepositoryMetadata`
- [x] Add `localPath` field to store the local directory path
- [x] Set default values for Git-specific fields (branch, commit hash, remote URL)
- [x] Use directory name as repository name for non-Git projects

### Non-Functional Requirements

- [ ] Non-Git project ingestion performance is comparable to Git projects
- [ ] No significant impact on existing Git repository processing
- [ ] Memory usage remains within acceptable limits
- [ ] Clear distinction between Git and non-Git repositories in the graph

## Technical Implementation

### Architecture Changes

Enhance the repository detection logic to handle non-Git projects by creating fallback repository metadata when Git detection fails.

### New Components

- **ProjectRootDetector**: Service for detecting project root directories (NOT IMPLEMENTED)
- **NonGitRepositoryMetadata**: Enhanced metadata class with Git status fields (NOT IMPLEMENTED)

### Modified Components

- **GitService**: Enhanced to handle non-Git cases (PARTIALLY IMPLEMENTED - returns null for non-Git)
- **RepositoryService**: Updated to create non-Git repository nodes (PARTIALLY IMPLEMENTED)
- **JavaParserService**: Modified to work with non-Git repositories (PARTIALLY IMPLEMENTED)
- **GraphService**: Updated for Git status properties (NOT IMPLEMENTED)

### Database Schema Changes

```cypher
// Enhanced Repository node with Git status
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
  language_stats: Map,
  localPath: String,           // IMPLEMENTED: Local directory path
  isGitRepository: Boolean!,   // NOT IMPLEMENTED: Git status flag
  gitStatus: String            // NOT IMPLEMENTED: "git", "non-git", "unknown"
})
```

### API Changes

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

## Validation Cases

### Test Scenarios

- [ ] Ingest a directory with Java files but no `.git` directory
- [ ] Verify repository node is created with directory name
- [ ] Verify `isGitRepository` is set to `false`
- [ ] Verify all code elements are properly linked to repository
- [ ] Ingest multiple directories, some with Git, some without
- [ ] Verify correct repository detection for each
- [ ] Verify proper relationship creation for all repositories

### Edge Cases

- [ ] Test with deeply nested directory structures
- [ ] Verify correct project root detection
- [ ] Verify appropriate repository name assignment
- [ ] Test scenario where non-Git project is later initialized as Git
- [ ] Verify system can handle the transition
- [ ] Verify existing nodes are properly updated

## Success Criteria

### Functional Success

- [ ] Non-Git projects can be ingested successfully
- [ ] Repository nodes are created with appropriate metadata
- [ ] All code elements are properly linked to repositories
- [ ] Graph queries work correctly for both Git and non-Git repositories
- [ ] Statistics API correctly reports Git vs non-Git counts

### Performance Success

- [ ] Non-Git project ingestion performance is comparable to Git projects
- [ ] No significant impact on existing Git repository processing
- [ ] Memory usage remains within acceptable limits

### Quality Success

- [ ] Clear distinction between Git and non-Git repositories in the graph
- [ ] Consistent data structure regardless of Git status
- [ ] Proper error handling for edge cases
- [ ] Comprehensive logging for debugging

## Dependencies

### External Dependencies

- Java NIO Path API: Available
- File System Access: Available

### Internal Dependencies

- STORY_001_REPOSITORY_TRACKING: COMPLETED
- GitService: Available
- RepositoryService: Available

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

## Risk Assessment

### Technical Risks

- **Risk**: Complex directory structure detection
- **Impact**: MEDIUM
- **Mitigation**: Start with simple heuristics, iterate based on real-world usage

- **Risk**: Performance impact on existing Git processing
- **Impact**: LOW
- **Mitigation**: Thorough testing and performance monitoring

- **Risk**: Inconsistent repository naming for non-Git projects
- **Impact**: MEDIUM
- **Mitigation**: Clear naming conventions and validation

### Business Risks

- **Risk**: Confusion between Git and non-Git repositories
- **Impact**: LOW
- **Mitigation**: Clear visual indicators and documentation

## Example Usage

### API Examples

```bash
# Ingest a non-Git project
curl -X POST http://localhost:8080/api/v1/ingest \
  -H "Content-Type: application/json" \
  -d '{"sourcePath": "/path/to/my-project/src", "includeTestFiles": false}'
```

### Query Examples

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

### Expected Output

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

## Implementation Phases

### Phase 1: Core Detection (Week 1)

- [x] Implement project root detection logic (PARTIALLY IMPLEMENTED)
- [ ] Add Git status fields to RepositoryMetadata
- [x] Update GitService to handle non-Git cases (PARTIALLY IMPLEMENTED - returns null)
- [x] Basic non-Git repository creation (PARTIALLY IMPLEMENTED)

### Phase 2: Enhanced Integration (Week 2)

- [x] Update RepositoryService for non-Git support (PARTIALLY IMPLEMENTED)
- [x] Modify JavaParserService to work with non-Git repositories (PARTIALLY IMPLEMENTED)
- [x] Add Git status properties to RepositoryNode (PARTIALLY IMPLEMENTED - localPath only)
- [ ] Update statistics API for Git status counts

### Phase 3: API and Query Support (Week 3)

- [ ] Add query capabilities to filter by Git status
- [ ] Update documentation with non-Git examples
- [ ] Create comprehensive test suite
- [ ] Performance optimization and testing

## Future Considerations

### Phase 2.5 Integration

- MCP server should handle both Git and non-Git repositories
- Query composition should include Git status filters
- Code context retrieval should work for all repository types

### Phase 3 Integration

- LLM integration should understand Git vs non-Git context
- Advanced features should work regardless of Git status
- Migration tools for converting non-Git to Git repositories

## Acceptance Criteria Checklist

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

## Implementation Status

### ✅ **What Has Been Implemented:**

1. **Basic Infrastructure** ✅

   - `RepositoryMetadata` class has `localPath` field
   - `RepositoryNode` model includes `localPath` property
   - Git detection logic in `GitService.isGitRepository()`
   - Repository root detection in `GitService.findGitRepositoryRoot()`

2. **Partial Non-Git Support** ✅

   - System can detect when files are not in Git repositories
   - `GitService.createRepositoryMetadata()` returns `null` for non-Git files
   - Basic repository metadata structure supports local paths

3. **Repository Service** ✅
   - `RepositoryService` can handle repository metadata creation
   - Repository nodes are created with local path information
   - Caching mechanism for repository nodes

### ❌ **What Still Needs Implementation:**

1. **Non-Git Repository Creation** ❌

   - `GitService.createRepositoryMetadata()` returns `null` instead of creating non-Git metadata
   - No fallback logic to create repository metadata for non-Git projects
   - Missing project root detection for non-Git directories

2. **Git Status Fields** ❌

   - `isGitRepository` boolean field not added to `RepositoryMetadata`
   - `gitStatus` field not implemented
   - No distinction between Git and non-Git repositories in the graph

3. **Statistics and Queries** ❌
   - Statistics API doesn't include Git vs non-Git repository counts
   - No query capabilities to filter by Git status
   - Missing documentation and examples for non-Git support

### 🔧 **Key Implementation Gap:**

The main issue is in `GitService.createRepositoryMetadata()` method:

```java
// Current implementation returns null for non-Git projects
if (repoRoot.isPresent()) {
    // Git repository logic
    return metadata;
} else {
    // NOT IMPLEMENTED: Should create non-Git metadata
    LOGGER.warn("File {} is not in a git repository", filePath);
    return null;  // This prevents non-Git projects from being processed
}
```

**Should be:**

```java
if (repoRoot.isPresent()) {
    // Git repository logic
    return metadata;
} else {
    // Non-Git project logic
    Path projectRoot = findProjectRoot(filePath);
    String repoName = projectRoot.getFileName().toString();

    RepositoryMetadata metadata = new RepositoryMetadata(repoName, projectRoot.toString());
    metadata.setIsGitRepository(false);
    metadata.setRepositoryUrl("local");
    metadata.setOrganization("local");
    // ... set other default values

    return metadata;
}
```

## Notes

This story addresses a critical gap in the system's ability to handle real-world development scenarios where projects may not be Git repositories. It provides a foundation for consistent code analysis regardless of version control status.

## Related Stories

- STORY_001_REPOSITORY_TRACKING: Provides foundation for repository metadata
- STORY_005_MULTI_PROJECT_REPOSITORY_SUPPORT: Related to repository structure support
- STORY_007_UPSERT_PATTERN_IMPORT: May benefit from Git status awareness
