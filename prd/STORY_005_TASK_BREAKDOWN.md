# STORY_005 - Multi-Project Repository Support: Task Breakdown

## Overview

This document breaks down STORY_005_MULTI_PROJECT_REPOSITORY_SUPPORT into smaller, implementable tasks that can be coded and tested incrementally. Each task is designed to be completed in 1-2 days with clear deliverables and testing criteria.

## Task Breakdown

### **TASK 1: SubProject Node Model & Database Schema** (1-2 days)

**Objective**: Create the foundational SubProject node model and database schema.

**Deliverables**:

- [ ] Create `SubProjectNode` class with basic properties
- [ ] Add `SubProjectMetadata` DTO class
- [ ] Create database indexes for SubProject nodes
- [ ] Add basic CRUD operations to `GraphService`

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

- [ ] Unit tests for SubProjectNode creation and persistence
- [ ] Integration test to create SubProject nodes in Neo4j
- [ ] Verify indexes are created correctly

**Acceptance Criteria**:

- SubProject nodes can be created and saved to Neo4j
- Basic properties (id, name, path, type) are stored correctly
- Database indexes improve query performance

---

### **TASK 2: Basic SubProject Detection** (1-2 days)

**Objective**: Implement basic sub-project detection using common project indicators.

**Deliverables**:

- [ ] Create `SubProjectDetector` service
- [ ] Implement detection for Maven projects (pom.xml)
- [ ] Implement detection for Gradle projects (build.gradle)
- [ ] Add basic project type identification

**Code Changes**:

```java
// New files to create:
src/main/java/com/vividcodes/graphrag/service/SubProjectDetector.java

// Modified files:
src/main/java/com/vividcodes/graphrag/service/RepositoryService.java
```

**Testing**:

- [ ] Unit tests for Maven project detection
- [ ] Unit tests for Gradle project detection
- [ ] Integration test with sample multi-project repository
- [ ] Test with repository containing no sub-projects

**Acceptance Criteria**:

- Can detect Maven modules in a repository
- Can detect Gradle sub-projects in a repository
- Returns correct project type identification
- Handles repositories with no sub-projects gracefully

---

### **TASK 3: Hierarchical Relationship Creation** (1-2 days)

**Objective**: Create hierarchical containment relationships between Repository, SubProject, and CodeElements.

**Deliverables**:

- [ ] Add `CONTAINS` relationships from Repository to SubProject
- [ ] Add `CONTAINS` relationships from SubProject to CodeElements
- [ ] Update `JavaParserService` to work with sub-project structure
- [ ] Modify `RepositoryService` to create sub-project relationships

**Code Changes**:

```java
// Modified files:
src/main/java/com/vividcodes/graphrag/service/JavaParserService.java
src/main/java/com/vividcodes/graphrag/service/RepositoryService.java
src/main/java/com/vividcodes/graphrag/service/GraphService.java
```

**Testing**:

- [ ] Unit tests for relationship creation
- [ ] Integration test for complete hierarchy creation
- [ ] Verify relationships are created correctly in Neo4j
- [ ] Test with existing single-project repositories

**Acceptance Criteria**:

- Repository -> SubProject -> CodeElement hierarchy is created
- Existing single-project repositories continue to work
- Relationships are properly typed and have correct properties

---

### **TASK 4: SubProject Metadata Enhancement** (1 day)

**Objective**: Enhance sub-project metadata with build file information and directory structure.

**Deliverables**:

- [ ] Add build file detection (pom.xml, build.gradle, package.json)
- [ ] Add source and test directory detection
- [ ] Add project version extraction
- [ ] Add project description extraction

**Code Changes**:

```java
// Modified files:
src/main/java/com/vividcodes/graphrag/service/SubProjectDetector.java
src/main/java/com/vividcodes/graphrag/model/dto/SubProjectMetadata.java
src/main/java/com/vividcodes/graphrag/model/graph/SubProjectNode.java
```

**Testing**:

- [ ] Unit tests for build file detection
- [ ] Unit tests for directory structure detection
- [ ] Unit tests for version and description extraction
- [ ] Integration test with real multi-project repository

**Acceptance Criteria**:

- Build files are correctly identified and stored
- Source and test directories are detected
- Project versions are extracted from build files
- Project descriptions are extracted when available

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

## Implementation Order

**Recommended Task Order**:

1. **TASK 1**: SubProject Node Model & Database Schema (Foundation)
2. **TASK 2**: Basic SubProject Detection (Core functionality)
3. **TASK 3**: Hierarchical Relationship Creation (Structure)
4. **TASK 4**: SubProject Metadata Enhancement (Details)
5. **TASK 5**: Cross-Project Dependency Analysis (Relationships)
6. **TASK 6**: Project-Specific Statistics API (Metrics)
7. **TASK 7**: Project Hierarchy Visualization (Visualization)
8. **TASK 8**: Enhanced Query Capabilities (Analysis)
9. **TASK 9**: Project Configuration Management (Configuration)
10. **TASK 10**: Integration Testing & Documentation (Finalization)

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
