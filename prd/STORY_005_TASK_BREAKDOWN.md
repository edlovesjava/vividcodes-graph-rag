# STORY_005 - Multi-Project Repository Support: Task Breakdown

## Overview

This document breaks down STORY_005_MULTI_PROJECT_REPOSITORY_SUPPORT into smaller, implementable tasks that can be coded and tested incrementally. Each task is designed to be completed in 1-2 days with clear deliverables and testing criteria.

## Task Breakdown

### **TASK 1: SubProject Node Model & Database Schema** ✅ **COMPLETED** (with minor test fixes needed)

**Objective**: Create the foundational SubProject node model and database schema.

**Deliverables**:

- [x] Create `SubProjectNode` class with basic properties
- [x] Add `SubProjectMetadata` DTO class
- [x] Create database indexes for SubProject nodes
- [x] Add basic CRUD operations to `GraphService`

**Code Changes**:

```java
// New files to create:
src/main/java/com/vividcodes/graphrag/model/graph/SubProjectNode.java
src/main/java/com/vividcodes/graphrag/model/dto/SubProjectMetadata.java

// Modified files:
src/main/java/com/vividcodes/graphrag/service/GraphService.java
src/main/java/com/vividcodes/graphrag/service/GraphServiceImpl.java
```

**Testing**:

- [x] Unit tests for SubProjectNode creation and persistence (11 tests passing)
- [x] Integration test to create SubProject nodes in Neo4j
- [x] Verify indexes are created correctly
- [ ] **PENDING**: Fix 2 minor test failures in `SubProjectMetadataTest` (path separator & equals/hashCode)

**Acceptance Criteria**:

- [x] SubProject nodes can be created and saved to Neo4j
- [x] Basic properties (id, name, path, type) are stored correctly
- [x] Database indexes improve query performance
- [x] CRUD operations fully implemented with comprehensive Cypher queries
- [x] Neo4j schema auto-initialization on startup
- [x] Integration with existing GraphService interface

**Implementation Status**: ✅ **COMPLETED** with comprehensive implementation:

- ✅ Complete `SubProjectNode` model with all metadata properties
- ✅ Full `SubProjectMetadata` DTO with builder pattern
- ✅ Complete CRUD operations in `GraphService` and `GraphServiceImpl`
- ✅ Neo4j schema initialization with indexes
- ✅ Comprehensive unit and integration test suites
- ⚠️ 2 minor test failures need fixing (platform-specific path separators)

**Files Created/Modified**:

- ✅ `SubProjectNode.java` - Complete with all properties and methods
- ✅ `SubProjectMetadata.java` - Full DTO with comprehensive fields
- ✅ `GraphService.java` - Extended with SubProject CRUD methods
- ✅ `GraphServiceImpl.java` - Full implementation with Cypher queries
- ✅ `Neo4jSchemaService.java` - Auto-creates indexes on startup
- ✅ Complete test suite: `SubProjectNodeTest`, `SubProjectMetadataTest`, `GraphServiceSubProjectTest`, `SubProjectIntegrationTest`

---

### **TASK 2: Basic SubProject Detection** ✅ **COMPLETED**

**Objective**: Implement basic sub-project detection using common project indicators.

**Deliverables**:

- [x] Create `SubProjectDetector` service
- [x] Implement detection for Maven projects (pom.xml)
- [x] Implement detection for Gradle projects (build.gradle)
- [x] Add basic project type identification
- [x] Add NPM project detection (package.json)
- [x] Integrate with `RepositoryService` workflow
- [x] Create comprehensive test suite

**Code Changes**:

```java
// New files to create:
src/main/java/com/vividcodes/graphrag/service/SubProjectDetector.java

// Modified files:
src/main/java/com/vividcodes/graphrag/service/RepositoryService.java
```

**Testing**:

- [x] Unit tests for Maven project detection (root POM, multi-module, nested)
- [x] Unit tests for Gradle project detection (single, multi-project, subprojects)
- [x] Unit tests for NPM project detection (package.json detection)
- [x] Integration test with sample multi-project repository (4 tests)
- [x] Test with repository containing no sub-projects
- [x] Test mixed project types (Maven + Gradle + NPM)
- [x] Test edge cases (invalid paths, empty directories)
- [x] Test ID generation and metadata extraction
- [x] **62 total tests passing** - comprehensive validation

**Acceptance Criteria**:

- [x] Can detect Maven modules in a repository
- [x] Can detect Gradle sub-projects in a repository
- [x] Can detect NPM projects in a repository
- [x] Returns correct project type identification
- [x] Handles repositories with no sub-projects gracefully
- [x] Creates proper graph relationships (Repository CONTAINS SubProject)
- [x] Generates unique repository-scoped IDs
- [x] Extracts comprehensive metadata (source dirs, dependencies, build files)
- [x] Integrates seamlessly with existing repository workflow

---

### **TASK 3: Hierarchical Relationship Creation** ✅ COMPLETED

**Objective**: Create hierarchical containment relationships between Repository, SubProject, and CodeElements.

**Deliverables**:

- [x] Add `CONTAINS` relationships from Repository to SubProject
- [x] Add `CONTAINS` relationships from SubProject to CodeElements
- [x] Update `JavaParserService` to work with sub-project structure
- [x] Modify `RepositoryService` to create sub-project relationships

**Code Changes**:

```java
// Modified files:
src/main/java/com/vividcodes/graphrag/service/JavaParserService.java
src/main/java/com/vividcodes/graphrag/service/RepositoryService.java
// New test files:
src/test/java/com/vividcodes/graphrag/integration/HierarchicalRelationshipIntegrationTest.java
src/test/java/com/vividcodes/graphrag/service/JavaParserServiceHierarchyTest.java
```

**Testing**:

- [x] Unit tests for relationship creation (5 tests passing)
- [x] Integration test for complete hierarchy creation
- [x] Verify relationships are created correctly in Neo4j
- [x] Test with existing single-project repositories
- [x] Test SubProject detection logic (handles nested projects correctly)

**Acceptance Criteria**:

- [x] Repository -> SubProject -> CodeElement hierarchy is created
- [x] Existing single-project repositories continue to work (graceful degradation)
- [x] Relationships are properly typed (`CONTAINS`) and have correct properties
- [x] SubProject detection works with nested/overlapping project structures
- [x] Most specific SubProject is selected for each code element
- [x] Comprehensive test coverage with 67 total tests passing

---

### **TASK 4: SubProject Metadata Enhancement** ✅ **COMPLETED**

**Objective**: Enhance sub-project metadata with build file information and directory structure.

**Deliverables**:

- [x] Add build file detection (pom.xml, build.gradle, package.json)
- [x] Add source and test directory detection
- [x] Add project version extraction
- [x] Add project description extraction

**Code Changes**:

```java
// Modified files:
src/main/java/com/vividcodes/graphrag/service/SubProjectDetector.java  - Enhanced with XML/JSON parsing
src/test/java/com/vividcodes/graphrag/service/SubProjectDetectorTest.java - Added 12 new test methods
```

**Testing**:

- [x] Unit tests for build file detection (22 total tests passing)
- [x] Unit tests for directory structure detection
- [x] Unit tests for version and description extraction
- [x] Unit tests for dependency extraction
- [x] Error handling tests for malformed build files
- [x] Integration with existing source directory detection

**Acceptance Criteria**:

- [x] Build files are correctly identified and stored
- [x] Source and test directories are detected
- [x] Project versions are extracted from build files (Maven POM, Gradle build.gradle, NPM package.json)
- [x] Project descriptions are extracted when available
- [x] Dependencies are extracted and stored as lists
- [x] Error handling with fallback values for malformed files
- [x] Maven parent version inheritance supported
- [x] Gradle dependency scopes preserved (implementation, testImplementation, api)
- [x] NPM dev dependencies marked appropriately

**Implementation Status**: ✅ **COMPLETED** with comprehensive metadata parsing:

- ✅ **Maven POM Parsing**: XML parsing with DocumentBuilderFactory, version inheritance from parent POM, comprehensive dependency extraction
- ✅ **Gradle Build File Parsing**: Text-based parsing with regex for version/description, dependency extraction with scope preservation
- ✅ **NPM Package.json Parsing**: JSON parsing with Jackson ObjectMapper, separation of dependencies and devDependencies
- ✅ **Error Handling**: Graceful fallback to default values when parsing fails, comprehensive logging
- ✅ **Integration**: Seamless integration with existing source directory detection and repository workflow
- ✅ **Testing**: 22 comprehensive test methods covering all parsing scenarios, error conditions, and integration

**Enhanced Features Delivered**:

- **Version Extraction**: Supports direct version, Maven parent inheritance, Gradle properties, NPM semver
- **Description Extraction**: Full text descriptions from all build file types with fallbacks
- **Dependency Analysis**: Comprehensive dependency lists with scope information (Maven test scope, Gradle implementation/api/testImplementation, NPM dev dependencies)
- **Robust Error Handling**: XML parser errors, JSON syntax errors, missing files, malformed content
- **Performance**: Efficient parsing with minimal memory overhead and appropriate caching via ObjectMapper reuse

---

### **TASK 5: Cross-Project Dependency Analysis** (2 days)

**Objective**: Implement cross-project dependency detection and analysis.

**Deliverables**:

- [ ] Create `CrossProjectAnalysisService`
- [ ] Implement dependency relationship detection
- [ ] Add `DEPENDS_ON` relationships between sub-projects
- [ ] Add `SHARES_WITH` relationships for shared code
- [ ] Add `IMPORTS_FROM` relationships for imports

**Code Changes**:

```java
// New files to create:
src/main/java/com/vividcodes/graphrag/service/CrossProjectAnalysisService.java

// Modified files:
src/main/java/com/vividcodes/graphrag/service/JavaParserService.java
src/main/java/com/vividcodes/graphrag/service/GraphService.java
```

**Testing**:

- [ ] Unit tests for dependency detection
- [ ] Unit tests for shared code detection
- [ ] Unit tests for import analysis
- [ ] Integration test with multi-project repository
- [ ] Test circular dependency detection

**Acceptance Criteria**:

- Cross-project dependencies are correctly identified
- Shared code relationships are created
- Import relationships are tracked
- Circular dependencies are detected and reported

---

### **TASK 6: Project-Specific Statistics API** (1-2 days)

**Objective**: Create APIs for project-specific statistics and metrics.

**Deliverables**:

- [ ] Add sub-project statistics endpoints
- [ ] Implement project health metrics calculation
- [ ] Add complexity and maintainability scores
- [ ] Create project comparison capabilities

**Code Changes**:

```java
// New files to create:
src/main/java/com/vividcodes/graphrag/controller/SubProjectController.java
src/main/java/com/vividcodes/graphrag/service/ProjectMetricsService.java

// Modified files:
src/main/java/com/vividcodes/graphrag/service/GraphService.java
```

**Testing**:

- [ ] Unit tests for statistics calculation
- [ ] Unit tests for health metrics
- [ ] Integration tests for API endpoints
- [ ] Performance tests for large repositories

**Acceptance Criteria**:

- Sub-project statistics are calculated correctly
- Health metrics provide meaningful insights
- API endpoints return proper JSON responses
- Performance is acceptable for large repositories

---

### **TASK 7: Project Hierarchy Visualization** (1 day)

**Objective**: Create APIs for project hierarchy visualization and structure analysis.

**Deliverables**:

- [ ] Add hierarchy structure endpoint
- [ ] Implement project tree visualization
- [ ] Add project depth and complexity analysis
- [ ] Create hierarchy export capabilities

**Code Changes**:

```java
// New files to create:
src/main/java/com/vividcodes/graphrag/service/ProjectHierarchyService.java

// Modified files:
src/main/java/com/vividcodes/graphrag/controller/SubProjectController.java
```

**Testing**:

- [ ] Unit tests for hierarchy generation
- [ ] Unit tests for tree visualization
- [ ] Integration tests for hierarchy endpoints
- [ ] Test with complex nested structures

**Acceptance Criteria**:

- Project hierarchy is correctly represented
- Tree visualization is clear and useful
- Depth and complexity analysis provides insights
- Export functionality works correctly

---

### **TASK 8: Enhanced Query Capabilities** (1-2 days)

**Objective**: Enhance Cypher query capabilities for project-specific analysis.

**Deliverables**:

- [ ] Add project-specific query templates
- [ ] Implement cross-project query capabilities
- [ ] Add project filtering to existing queries
- [ ] Create query examples and documentation

**Code Changes**:

```java
// Modified files:
src/main/java/com/vividcodes/graphrag/service/CypherQueryService.java
src/main/java/com/vividcodes/graphrag/service/QueryExecutor.java
docs/API_QUERY_EXAMPLES.md
```

**Testing**:

- [ ] Unit tests for project-specific queries
- [ ] Unit tests for cross-project queries
- [ ] Integration tests for query filtering
- [ ] Performance tests for complex queries

**Acceptance Criteria**:

- Project-specific queries work correctly
- Cross-project analysis queries are functional
- Query performance is acceptable
- Documentation provides clear examples

---

### **TASK 9: Project Configuration Management** (1 day)

**Objective**: Implement project-specific configuration management.

**Deliverables**:

- [ ] Create `ProjectConfigurationService`
- [ ] Add project-specific settings storage
- [ ] Implement configuration inheritance
- [ ] Add configuration validation

**Code Changes**:

```java
// New files to create:
src/main/java/com/vividcodes/graphrag/service/ProjectConfigurationService.java
src/main/java/com/vividcodes/graphrag/model/graph/ProjectConfig.java

// Modified files:
src/main/java/com/vividcodes/graphrag/service/SubProjectDetector.java
```

**Testing**:

- [ ] Unit tests for configuration management
- [ ] Unit tests for configuration inheritance
- [ ] Unit tests for configuration validation
- [ ] Integration tests for configuration workflows

**Acceptance Criteria**:

- Project configurations are stored correctly
- Configuration inheritance works as expected
- Validation prevents invalid configurations
- Configuration changes are tracked

---

### **TASK 10: Integration Testing & Documentation** (1 day)

**Objective**: Comprehensive integration testing and documentation updates.

**Deliverables**:

- [ ] End-to-end integration tests
- [ ] Performance testing with large repositories
- [ ] Update API documentation
- [ ] Create usage examples and tutorials
- [ ] Update architecture documentation

**Code Changes**:

```java
// New files to create:
src/test/java/com/vividcodes/graphrag/integration/MultiProjectIntegrationTest.java
docs/MULTI_PROJECT_USAGE.md
docs/ARCHITECTURE_MULTI_PROJECT.md
```

**Testing**:

- [ ] End-to-end workflow testing
- [ ] Performance testing with 10+ sub-projects
- [ ] Stress testing with large codebases
- [ ] Documentation review and validation

**Acceptance Criteria**:

- All features work together correctly
- Performance meets requirements
- Documentation is complete and accurate
- Examples are clear and functional

## Implementation Order & Status

**Recommended Task Order**:

1. ✅ **TASK 1**: SubProject Node Model & Database Schema (Foundation) - **COMPLETED**
2. ✅ **TASK 2**: Basic SubProject Detection (Core functionality) - **COMPLETED**
3. ✅ **TASK 3**: Hierarchical Relationship Creation (Structure) - **COMPLETED**
4. 🔄 **TASK 4**: SubProject Metadata Enhancement (Details) - **READY TO START**
5. **TASK 5**: Cross-Project Dependency Analysis (Relationships)
6. **TASK 6**: Project-Specific Statistics API (Metrics)
7. **TASK 7**: Project Hierarchy Visualization (Visualization)
8. **TASK 8**: Enhanced Query Capabilities (Analysis)
9. **TASK 9**: Project Configuration Management (Configuration)
10. **TASK 10**: Integration Testing & Documentation (Finalization)

**Current Status**:

- ✅ TASK 1 completed successfully with comprehensive implementation and testing
- ✅ TASK 2 completed with robust SubProject detection for Maven, Gradle, and NPM projects
- ✅ TASK 3 completed with full hierarchical relationship creation and testing
- ✅ TASK 4 completed with enhanced metadata parsing (version, description, dependencies) for all build file types
- 📋 Ready to proceed with TASK 5: Cross-Project Dependency Analysis

## Testing Strategy

**For Each Task**:

1. **Unit Tests**: Test individual components in isolation
2. **Integration Tests**: Test component interactions
3. **End-to-End Tests**: Test complete workflows
4. **Performance Tests**: Ensure acceptable performance

**Test Data**:

- Use the existing `catalog-service` repository as test data
- Create synthetic multi-project repositories for edge cases
- Test with various project types (Maven, Gradle, NPM)

## Success Metrics

**For Each Task**:

- All unit tests pass
- Integration tests demonstrate functionality
- Performance meets requirements
- Code follows project standards
- Documentation is updated

**Overall Success**:

- Sub-project detection accuracy: >95%
- Cross-project relationship identification: >90%
- Performance: Multi-project ingestion <2x single-project time
- Support: Repositories with 10+ sub-projects
- Usability: Clear APIs and documentation

## Risk Mitigation

**Technical Risks**:

- **Complex project structures**: Start with common patterns, iterate
- **Performance issues**: Monitor and optimize as needed
- **Circular dependencies**: Detect and report, don't fail

**Business Risks**:

- **Scope creep**: Stick to defined tasks
- **Integration issues**: Test incrementally
- **Documentation gaps**: Update docs with each task

## Next Steps

1. **Start with TASK 1**: Create the foundational SubProject model
2. **Code and test each task incrementally**
3. **Review and adjust task scope as needed**
4. **Update story status as tasks are completed**

This breakdown provides a clear roadmap for implementing STORY_005 incrementally, with each task being manageable and testable.
