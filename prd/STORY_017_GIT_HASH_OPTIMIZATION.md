# STORY_017_GIT_HASH_OPTIMIZATION

## Story Information

- **Story Number**: STORY_017
- **Story Name**: Git Hash Based Upsert Optimization
- **Epic**: Performance Optimization
- **Priority**: HIGH
- **Estimated Duration**: 2 weeks
- **Dependencies**: STORY_007_UPSERT_PATTERN_IMPORT (COMPLETED), STORY_018_FILE_NODE_MODELING (NOT_STARTED)
- **Status**: NOT_STARTED

## Overview

Optimize the upsert system by using git content hashes to determine file changes instead of property-by-property comparison. This will dramatically reduce ingestion time for unchanged files and provide more accurate change detection.

## User Story

**As a** developer re-ingesting large codebases  
**I want** extremely fast re-ingestion when files haven't changed  
**So that** I can iterate quickly and get immediate feedback on actual changes without waiting for unnecessary parsing

## Background

STORY_007 successfully implemented intelligent upsert operations using property-level comparison. However, testing with the catalog-service project revealed that even SKIP operations require:

1. Parsing Java files into AST
2. Extracting all node properties
3. Comparing 15+ properties per class
4. Database queries for existence checks

This results in ~1:05 duration even for identical re-ingestion. Git content hashes provide a much more efficient approach:

- **Current approach**: Parse → Compare → Skip (25% faster on re-runs)
- **Proposed approach**: Hash check → Skip (95% faster expected for unchanged files)

Real-world performance impact:

- First ingestion: 1:30 (1,326 nodes created)
- Second ingestion: 1:05 (all nodes skipped)
- **Target with git hashes**: ~10-20 seconds for identical re-ingestion

## Acceptance Criteria

- [ ] Build git file hash cache in under 5 seconds for repositories up to 10,000 files
- [ ] Achieve <1ms hash comparison per file for skip decisions
- [ ] Support mixed git-tracked and untracked files seamlessly
- [ ] Maintain backward compatibility with existing nodes (no contentHash property)
- [ ] Achieve >90% ingestion time reduction for unchanged files
- [ ] Handle edge cases: file renames, mode changes, partial git repositories

## Technical Requirements

### Functional Requirements

- [ ] Create GitHashCacheService for efficient batch hash retrieval
- [ ] Add contentHash property to all file-based node types (ClassNode, MethodNode, FieldNode)
- [ ] Implement smart hash lookup with fallback strategies
- [ ] Extend UpsertService to use hash-based skip decisions
- [ ] Support non-git content hash computation for untracked files

### Non-Functional Requirements

- [ ] **Performance**: <5 seconds cache building for 10k files
- [ ] **Performance**: <20 seconds total re-ingestion for unchanged large codebases
- [ ] **Memory**: Cache memory usage <50MB for large repositories
- [ ] **Compatibility**: Zero disruption to existing graph data

## Technical Implementation

### Architecture Changes

The optimization adds a new caching layer between file system and upsert operations, leveraging File nodes from STORY_018:

```
File System → GitHashCacheService → File Node (w/ content_hash) → UpsertService → Neo4j
```

Key performance improvement:

- **Before**: File → Parse → Extract Properties → Compare → Skip
- **After**: File → Hash Lookup → Skip (if unchanged)
- **With STORY_018**: File Node content_hash provides perfect foundation for hash comparison

**Note**: This story will be significantly more efficient when built on STORY_018 File nodes, as content hashes will be stored directly in File nodes rather than duplicated across Class/Method/Field nodes.

### New Components

- **GitHashCacheService**: Batch git hash operations and caching
- **FileContentHasher**: Content-based hashing for non-git files
- **HashBasedUpsertService**: Enhanced upsert logic with hash optimization

### Modified Components

- **ClassNode, MethodNode, FieldNode**: Add `contentHash` property
- **UpsertServiceImpl**: Integrate hash-based skip logic before property comparison
- **Neo4jSchemaService**: Add indexes for contentHash properties
- **NodeIdentifierServiceImpl**: Consider contentHash in consistency checks

### Database Schema Changes

```cypher
// Add contentHash properties to file-based nodes
ALTER TABLE Class ADD contentHash STRING;
ALTER TABLE Method ADD contentHash STRING;
ALTER TABLE Field ADD contentHash STRING;

// Add indexes for efficient hash-based queries
CREATE INDEX IF NOT EXISTS FOR (c:Class) ON (c.contentHash);
CREATE INDEX IF NOT EXISTS FOR (m:Method) ON (m.contentHash);
CREATE INDEX IF NOT EXISTS FOR (f:Field) ON (f.contentHash);
```

### API Changes

No external API changes - purely internal optimization.

## Validation Cases

### Test Scenarios

- [ ] **Large unchanged codebase**: Re-ingest 1000+ files with zero changes → 95% time reduction
- [ ] **Mixed changes**: Re-ingest with 10% files changed → Only parse changed files
- [ ] **New files**: Add new files to tracked repository → Detect and process normally
- [ ] **Untracked files**: Support projects with mixed git/non-git files
- [ ] **File moves**: Detect file renames through git tracking
- [ ] **Cache invalidation**: Handle git operations that modify file hashes

### Edge Cases

- [ ] **Empty repository**: Handle repositories with no git history
- [ ] **Large files**: Efficiently handle files >1MB in hash computation
- [ ] **Binary files**: Skip binary files in hash processing
- [ ] **Corrupted git repo**: Fallback to content-based hashing
- [ ] **Permission issues**: Handle unreadable files gracefully
- [ ] **Concurrent access**: Thread-safe cache operations

## Success Criteria

### Functional Success

- [ ] All existing upsert functionality preserved and enhanced
- [ ] Support for git and non-git file change detection
- [ ] Seamless migration of existing graph data

### Performance Success

- [ ] **>90% time reduction** for unchanged file re-ingestion
- [ ] **<5 second** cache build time for repositories up to 10k files
- [ ] **<1ms per file** hash comparison performance
- [ ] **Memory efficient**: <50MB cache overhead for large repositories

### Quality Success

- [ ] 100% test coverage for new hash-based logic
- [ ] Integration tests covering all git scenarios
- [ ] Backward compatibility verified with existing data

## Dependencies

### External Dependencies

- **Git**: Requires git binary for hash operations
- **Java NIO**: For efficient file content hashing
- **Neo4j**: Schema migration support for new properties

### Internal Dependencies

- **STORY_007**: ✅ COMPLETED - Provides upsert infrastructure to enhance
- **STORY_018**: 📋 NOT_STARTED - File nodes with content hashes provide optimal foundation for hash-based optimization
- **Current upsert system**: Proven and tested foundation

## Deliverables

### Code Changes

- [ ] GitHashCacheService implementation with batch operations
- [ ] Enhanced node models (ClassNode, MethodNode, FieldNode) with contentHash
- [ ] Hash-optimized UpsertService implementation
- [ ] FileContentHasher for non-git content computation
- [ ] Neo4j schema migration for contentHash properties

### Documentation

- [ ] Performance benchmark comparison (before/after)
- [ ] Git hash caching strategy documentation
- [ ] Migration guide for existing deployments

### Testing

- [ ] Comprehensive unit tests for hash-based logic
- [ ] Integration tests with real git repositories
- [ ] Performance benchmarks with large codebases
- [ ] Edge case testing for git edge scenarios

## Risk Assessment

### Technical Risks

- **Risk**: Git command failures or missing git binary
- **Impact**: MEDIUM
- **Mitigation**: Fallback to content-based hashing, clear error messages

- **Risk**: Memory consumption for large repository caches
- **Impact**: MEDIUM
- **Mitigation**: Implement cache size limits and LRU eviction

- **Risk**: Hash collisions causing incorrect skip decisions
- **Impact**: LOW
- **Mitigation**: Use SHA-256, detect and log collisions

### Business Risks

- **Risk**: Migration complexity for existing deployments
- **Impact**: LOW
- **Mitigation**: Backward compatible design, gradual rollout strategy

- **Risk**: Development time investment vs. performance gain
- **Impact**: LOW
- **Mitigation**: Clear performance benchmarks justify development cost

## Example Usage

### API Examples

```bash
# Performance comparison example
time curl -X POST http://localhost:8080/api/v1/ingest \
  -d '{"sourcePath": "/path/to/large-project"}'

# Before optimization: ~90 seconds
# After optimization: ~15 seconds (unchanged files)
```

### Internal Usage

```java
// GitHashCacheService usage
GitHashCacheService cacheService = new GitHashCacheService();
cacheService.buildCache(repositoryPath); // 3 seconds for 10k files

String currentHash = cacheService.getFileHash(repoPath, filePath); // <1ms
if (currentHash.equals(storedHash)) {
    return UpsertResult.skip(nodeId, nodeType, 1, operationId);
}
```

### Performance Benchmarks

```
Catalog Service (602 classes, 1,326 total nodes):
- Current STORY_007:
  * First run: 1:30
  * Second run: 1:05 (25% improvement)

- Expected STORY_017:
  * First run: 1:30 (same - full processing needed)
  * Second run: 0:15 (85% improvement over current)
  * Hash cache build: 0:03
  * Hash comparisons: 0:01 (602 files × 1ms)
```

## Implementation Phases

### Phase 1: Core Git Hash Infrastructure (Week 1)

- [ ] **Day 1-2**: GitHashCacheService implementation

  - [ ] Batch git ls-files -s command execution
  - [ ] Parse git index into hash cache
  - [ ] Individual file hash retrieval with caching
  - [ ] Handle git command failures and edge cases

- [ ] **Day 3-4**: Node model enhancements

  - [ ] Add contentHash property to ClassNode, MethodNode, FieldNode
  - [ ] Database migration scripts for schema changes
  - [ ] Update constructors and serialization
  - [ ] Neo4j indexes for contentHash properties

- [ ] **Day 5**: FileContentHasher for non-git files
  - [ ] SHA-256 content computation for untracked files
  - [ ] File change detection for mixed repositories
  - [ ] Performance optimization for large files

### Phase 2: Upsert Integration and Testing (Week 2)

- [ ] **Day 1-2**: UpsertService hash integration

  - [ ] Modify UpsertServiceImpl for hash-first logic
  - [ ] Implement hash comparison before property parsing
  - [ ] Update audit trail to track hash-based skips
  - [ ] Ensure backward compatibility for nodes without hashes

- [ ] **Day 3-4**: Comprehensive testing

  - [ ] Unit tests for all hash-based logic
  - [ ] Integration tests with real git repositories
  - [ ] Performance benchmarks with large codebases
  - [ ] Edge case testing (corrupted repos, permissions, etc.)

- [ ] **Day 5**: Performance validation and documentation
  - [ ] Real-world performance testing with catalog-service
  - [ ] Document performance improvements and migration steps
  - [ ] Create deployment migration guide

## Future Considerations

### Phase 2.5 Integration

Git hash optimization provides excellent foundation for LLM MCP integration:

- **Fast re-analysis**: Only process files that actually changed
- **Incremental insights**: LLM can focus on modified code sections
- **Change tracking**: Hash-based change detection for smarter AI analysis

### Phase 3 Integration

Advanced features benefit from git hash optimization:

- **Temporal analysis**: Track code evolution through hash history
- **Change impact**: Correlate hash changes with dependency relationships
- **Smart caching**: Cache LLM analysis results based on content hashes

## Acceptance Criteria Checklist

### Must Have

- [ ] **Performance**: >90% time reduction for unchanged file re-ingestion
- [ ] **Compatibility**: Zero disruption to existing graph data and APIs
- [ ] **Reliability**: Robust fallback for non-git and error scenarios
- [ ] **Accuracy**: Perfect change detection with no false positives/negatives

### Should Have

- [ ] **Memory efficiency**: <50MB cache overhead for large repositories
- [ ] **Fast cache building**: <5 seconds for 10k file repositories
- [ ] **Comprehensive testing**: 100% coverage of new hash logic
- [ ] **Clear migration**: Smooth upgrade path for existing deployments

### Could Have

- [ ] **Hash persistence**: Cache git hashes across application restarts
- [ ] **Advanced git integration**: Support for git submodules and worktrees
- [ ] **Monitoring**: Metrics for hash hit rates and performance gains
- [ ] **Configuration**: Tunable cache sizes and hash strategies

### Won't Have

- [ ] **Complex git history**: Deep git history analysis beyond current state
- [ ] **Branch comparison**: Cross-branch hash comparison capabilities
- [ ] **Remote git operations**: Direct integration with remote git repositories
- [ ] **Binary file analysis**: Hash-based analysis of non-text files

## Notes

This story builds directly on the proven STORY_007 upsert infrastructure. The hash optimization is purely additive - it adds a fast-path for unchanged files while preserving all existing functionality.

Key insight: The current 25% improvement from SKIP operations shows the upsert system works perfectly. This optimization targets the remaining 75% performance opportunity by avoiding unnecessary parsing entirely.

The implementation prioritizes robustness over complexity - fallback strategies ensure the system works even when git operations fail.

## Related Stories

- **STORY_007_UPSERT_PATTERN_IMPORT**: ✅ Direct foundation - provides upsert infrastructure to optimize
- **STORY_018_FILE_NODE_MODELING**: 📋 Critical dependency - File nodes with content hashes provide optimal foundation for performance optimization
- **STORY_001_REPOSITORY_TRACKING**: ✅ Provides git integration foundation
- **STORY_005_MULTI_PROJECT_REPOSITORY_SUPPORT**: 🔄 Will benefit from performance improvements
- **STORY_016_XML_CONFIGURATION_PARSING**: 📋 Can leverage same hash-based optimization approach
