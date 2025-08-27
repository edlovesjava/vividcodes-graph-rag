# STORY_005_MULTI_PROJECT_REPOSITORY_SUPPORT

## Story Information

- **Story Number**: STORY_005
- **Story Name**: Multi-Project Repository Support
- **Epic**: Enterprise Repository Structure Support
- **Priority**: HIGH
- **Estimated Duration**: 3-4 weeks
- **Dependencies**: STORY_001_REPOSITORY_TRACKING (COMPLETED), STORY_004_NON_GIT_PROJECT_SUPPORT (COMPLETED)
- **Status**: NOT_STARTED

## Overview

This story provides comprehensive support for multi-project repositories (monorepos) by implementing hierarchical project containment, sub-project detection, cross-project analysis, and enterprise-grade repository management. It addresses the complex needs of organizations that maintain multiple related projects within single Git repositories.

## User Story

**As a** developer working with enterprise repositories containing multiple sub-projects  
**I want** the system to understand and properly organize hierarchical project structures  
**So that** I can analyze code across different modules and understand cross-project dependencies

## Background

Many enterprise organizations use monorepo patterns where a single Git repository contains multiple sub-projects, modules, or components. For example, the `catalog-service` repository contains:

- Main application code in `src/main/java`
- Sub-project `gatling` with its own source structure
- Sub-project `sdk` with separate build configuration
- Shared utilities and common libraries

Currently, when ingesting such repositories, the system treats all code as belonging to a single project, making it difficult to:

- Analyze code within specific sub-projects
- Understand dependencies between different modules
- Track changes across project boundaries
- Manage project-specific configurations
- Perform targeted code analysis

This story implements a comprehensive solution that:

- Detects sub-projects using common project indicators
- Creates hierarchical containment relationships
- Enables cross-project analysis and dependency tracking
- Provides project-specific statistics and insights
- Supports enterprise repository management workflows

## Acceptance Criteria

- [ ] Detect sub-projects using common project indicators (pom.xml, build.gradle, src/, etc.)
- [ ] Identify project types (Maven, Gradle, NPM, custom) automatically
- [ ] Create hierarchical containment structure (Repository -> SubProject -> CodeElements)
- [ ] Support cross-project dependency analysis
- [ ] Provide project-specific statistics and metrics
- [ ] Enable targeted code analysis by sub-project
- [ ] Handle mixed project types within single repository
- [ ] Support project-specific configuration and settings
- [ ] Create cross-project relationship mapping
- [ ] Provide project hierarchy visualization
- [ ] Enable project-specific query capabilities
- [ ] Support project metadata and documentation
- [ ] Handle project-specific build and dependency information
- [ ] Provide project health and quality metrics
- [ ] Enable project comparison and analysis

## Technical Requirements

### Functional Requirements

- [ ] Detect sub-projects using project indicators
- [ ] Support common project structures (Maven modules, Gradle sub-projects, etc.)
- [ ] Create hierarchical graph relationships
- [ ] Enable cross-project analysis
- [ ] Provide project-specific APIs and queries
- [ ] Support project metadata management
- [ ] Handle project-specific configurations

### Non-Functional Requirements

- [ ] Multi-project ingestion performance is acceptable
- [ ] Cross-project queries execute within reasonable time
- [ ] Memory usage scales appropriately with project complexity
- [ ] Support for repositories with 10+ sub-projects
- [ ] Clear project hierarchy visualization

## Technical Implementation

### Architecture Changes

Add hierarchical containment structure to the graph schema, introducing SubProject nodes between Repository and CodeElements, with comprehensive project detection and management capabilities.

### New Components

- **SubProjectDetector**: Service for detecting sub-projects within repositories
- **SubProjectNode**: Graph node representing a sub-project
- **SubProjectMetadata**: Enhanced metadata class for sub-project information
- **ProjectHierarchyService**: Service for managing project hierarchies
- **CrossProjectAnalysisService**: Service for cross-project analysis
- **ProjectConfigurationService**: Service for project-specific configurations

### Modified Components

- **RepositoryService**: Enhanced to handle multi-project repositories
- **JavaParserService**: Modified to work with sub-project structure
- **GraphService**: Updated for sub-project operations
- **CypherQueryService**: Enhanced for project-specific queries

### Database Schema Changes

```cypher
// New SubProject node with comprehensive metadata
(:SubProject {
  id: String!,
  name: String!,
  path: String!,
  type: String!,  // "maven", "gradle", "npm", "custom"
  buildFile: String,  // pom.xml, build.gradle, package.json
  sourceDirectories: List<String>,
  testDirectories: List<String>,
  dependencies: List<String>,
  description: String,
  version: String,
  created_at: DateTime,
  updated_at: DateTime,
  health_score: Float,
  complexity_score: Float,
  maintainability_score: Float
})

// Hierarchical containment relationships
(:Repository)-[:CONTAINS]->(:SubProject)
(:SubProject)-[:CONTAINS]->(:Package)
(:SubProject)-[:CONTAINS]->(:Class)
(:SubProject)-[:CONTAINS]->(:Method)
(:SubProject)-[:CONTAINS]->(:Field)

// Cross-project dependencies
(:SubProject)-[:DEPENDS_ON]->(:SubProject)
(:SubProject)-[:SHARES_WITH]->(:SubProject)
(:SubProject)-[:IMPORTS_FROM]->(:SubProject)

// Project configuration
(:ProjectConfig {
  subProjectId: String!,
  configType: String,  // "build", "test", "deploy", "monitor"
  configData: Map,
  environment: String,
  lastModified: DateTime
})
```

### API Changes

```json
{
  "subprojects": {
    "list": "/api/v1/repositories/{id}/subprojects",
    "details": "/api/v1/repositories/{id}/subprojects/{subprojectId}",
    "stats": "/api/v1/repositories/{id}/subprojects/{subprojectId}/stats",
    "dependencies": "/api/v1/repositories/{id}/subprojects/{subprojectId}/dependencies"
  },
  "analysis": {
    "cross-project": "/api/v1/repositories/{id}/analysis/cross-project",
    "dependencies": "/api/v1/repositories/{id}/analysis/dependencies",
    "health": "/api/v1/repositories/{id}/analysis/health"
  },
  "hierarchy": {
    "structure": "/api/v1/repositories/{id}/hierarchy",
    "visualization": "/api/v1/repositories/{id}/hierarchy/visualization"
  }
}
```

## Validation Cases

### Test Scenarios

- [ ] Ingest a repository with multiple Maven modules
- [ ] Test repository with Maven + Gradle + NPM projects
- [ ] Verify sub-project detection accuracy
- [ ] Test cross-project dependency analysis
- [ ] Verify project-specific statistics
- [ ] Test hierarchical relationship creation
- [ ] Verify project health metrics calculation

### Edge Cases

- [ ] Handle repositories with no clear sub-project structure
- [ ] Handle circular dependencies between sub-projects
- [ ] Handle sub-projects with identical names in different paths
- [ ] Handle deeply nested project structures
- [ ] Handle projects with mixed build systems
- [ ] Handle projects with custom build configurations

## Success Criteria

### Functional Success

- [ ] Sub-project detection accuracy: >95%
- [ ] Cross-project relationship identification: >90%
- [ ] Project hierarchy visualization is clear and accurate
- [ ] Project-specific queries return correct results
- [ ] Cross-project analysis provides meaningful insights

### Performance Success

- [ ] Multi-project ingestion performance is comparable to single-project
- [ ] Cross-project queries execute within reasonable time
- [ ] Memory usage scales appropriately with project complexity
- [ ] Support for repositories with 10+ sub-projects

### Quality Success

- [ ] Clear project hierarchy visualization
- [ ] Accurate project type detection
- [ ] Comprehensive project metadata
- [ ] Useful cross-project insights

## Dependencies

### External Dependencies

- Neo4j Graph Database: Available
- Java Parser Library: Available
- Build System Detection Libraries: Available

### Internal Dependencies

- STORY_001_REPOSITORY_TRACKING: COMPLETED
- STORY_004_NON_GIT_PROJECT_SUPPORT: COMPLETED
- GraphService: Available
- RepositoryService: Available

## Deliverables

### Code Changes

- [ ] New SubProjectNode model
- [ ] Enhanced SubProjectMetadata class
- [ ] SubProjectDetector service implementation
- [ ] ProjectHierarchyService implementation
- [ ] CrossProjectAnalysisService implementation
- [ ] ProjectConfigurationService implementation
- [ ] Enhanced RepositoryService for multi-project support
- [ ] Updated JavaParserService for sub-project parsing
- [ ] Enhanced GraphService for sub-project operations
- [ ] New API endpoints for sub-project management

### Documentation

- [ ] Updated API documentation for multi-project support
- [ ] Updated user guide with multi-project examples
- [ ] Project hierarchy visualization guide
- [ ] Cross-project analysis guide
- [ ] Sub-project detection configuration guide

### Testing

- [ ] Unit tests for sub-project detection
- [ ] Integration tests for multi-project repositories
- [ ] Performance tests for large multi-project repositories
- [ ] End-to-end tests for complete workflows
- [ ] Cross-project analysis tests

## Risk Assessment

### Technical Risks

- **Risk**: Complex project structure detection
- **Impact**: MEDIUM
- **Mitigation**: Start with common patterns, iterate based on real-world usage

- **Risk**: Performance impact on large multi-project repositories
- **Impact**: MEDIUM
- **Mitigation**: Implement efficient detection algorithms and caching

- **Risk**: Circular dependency detection and handling
- **Impact**: LOW
- **Mitigation**: Implement cycle detection and warning mechanisms

### Business Risks

- **Risk**: User confusion with new hierarchical structure
- **Impact**: LOW
- **Mitigation**: Clear documentation and examples

- **Risk**: Complexity of cross-project analysis
- **Impact**: MEDIUM
- **Mitigation**: Provide clear analysis tools and insights

## Example Usage

### API Examples

```bash
# Ingest a multi-project repository
curl -X POST http://localhost:8080/api/v1/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "sourcePath": "/path/to/multi-project-repo",
    "includeTestFiles": true,
    "detectSubProjects": true
  }'

# Get sub-projects in a repository
curl -X GET "http://localhost:8080/api/v1/repositories/catalog-service/subprojects" \
  -H "Content-Type: application/json"

# Get cross-project dependencies
curl -X GET "http://localhost:8080/api/v1/repositories/catalog-service/analysis/dependencies" \
  -H "Content-Type: application/json"

# Get project hierarchy visualization
curl -X GET "http://localhost:8080/api/v1/repositories/catalog-service/hierarchy/visualization" \
  -H "Content-Type: application/json"
```

### Query Examples

```cypher
// Find all sub-projects in a repository
MATCH (r:Repository {name: $repositoryName})-[:CONTAINS]->(sp:SubProject)
RETURN sp.name, sp.type, sp.path, sp.health_score
ORDER BY sp.health_score DESC

// Find cross-project dependencies
MATCH (sp1:SubProject)-[:DEPENDS_ON]->(sp2:SubProject)
WHERE sp1.repository = $repositoryName
RETURN sp1.name as dependent, sp2.name as dependency, sp1.type, sp2.type

// Get project hierarchy structure
MATCH (r:Repository {name: $repositoryName})-[:CONTAINS]->(sp:SubProject)
MATCH (sp)-[:CONTAINS]->(p:Package)
MATCH (p)-[:CONTAINS]->(c:Class)
RETURN r.name as repository, sp.name as subproject, sp.type,
       collect(DISTINCT p.name) as packages, count(c) as classCount
ORDER BY classCount DESC

// Find high complexity sub-projects
MATCH (sp:SubProject)-[:CONTAINS]->(c:Class)-[:CONTAINS]->(m:Method)
WITH sp, sum(m.complexity) as totalComplexity, count(c) as classCount
WHERE totalComplexity > 100 OR classCount > 50
RETURN sp.name, sp.type, totalComplexity, classCount
ORDER BY totalComplexity DESC
```

### Expected Output

```json
{
  "repository": "catalog-service",
  "subprojects": [
    {
      "name": "main-app",
      "type": "maven",
      "path": "src/main/java",
      "health_score": 85.5,
      "complexity_score": 45.2,
      "maintainability_score": 78.9,
      "classCount": 127,
      "methodCount": 892
    },
    {
      "name": "gatling",
      "type": "gradle",
      "path": "gatling",
      "health_score": 92.1,
      "complexity_score": 23.4,
      "maintainability_score": 88.7,
      "classCount": 45,
      "methodCount": 234
    },
    {
      "name": "sdk",
      "type": "maven",
      "path": "sdk",
      "health_score": 79.8,
      "complexity_score": 67.8,
      "maintainability_score": 72.3,
      "classCount": 89,
      "methodCount": 567
    }
  ],
  "crossProjectDependencies": [
    {
      "from": "main-app",
      "to": "sdk",
      "type": "DEPENDS_ON",
      "strength": "high"
    },
    {
      "from": "gatling",
      "to": "main-app",
      "type": "IMPORTS_FROM",
      "strength": "medium"
    }
  ],
  "hierarchy": {
    "totalSubProjects": 3,
    "totalClasses": 261,
    "totalMethods": 1693,
    "averageHealthScore": 85.8
  }
}
```

## Implementation Phases

### Phase 1: Core Sub-Project Detection (Week 1)

- [ ] Implement SubProjectDetector service
- [ ] Create SubProjectNode and SubProjectMetadata models
- [ ] Add project type detection (Maven, Gradle, NPM)
- [ ] Basic sub-project creation and relationships
- [ ] Update database schema with SubProject nodes

### Phase 2: Enhanced Parsing and Relationships (Week 2)

- [ ] Update JavaParserService for sub-project parsing
- [ ] Implement hierarchical relationship creation
- [ ] Add cross-project dependency detection
- [ ] Create project-specific statistics
- [ ] Add project health and quality metrics

### Phase 3: Advanced Features and Analysis (Week 3)

- [ ] Implement CrossProjectAnalysisService
- [ ] Add project hierarchy visualization
- [ ] Create project-specific APIs
- [ ] Implement project configuration management
- [ ] Add cross-project query capabilities

### Phase 4: Integration and Optimization (Week 4)

- [ ] Integrate with existing services
- [ ] Performance optimization for large repositories
- [ ] Comprehensive testing and validation
- [ ] Documentation and examples
- [ ] User acceptance testing

## Future Considerations

### Phase 2.5 Integration

- MCP server should understand sub-project structure
- Query composition should include sub-project filters
- Code context retrieval should work across sub-projects
- Semantic search should respect project boundaries

### Phase 3 Integration

- LLM integration should understand project hierarchy
- Advanced features should work with sub-project structure
- Dependency analysis across sub-projects
- Project-specific code generation and suggestions

## Acceptance Criteria Checklist

### Must Have

- [ ] Multi-project repositories can be ingested and processed
- [ ] Sub-project nodes are created with appropriate metadata
- [ ] Hierarchical relationships are established correctly
- [ ] Cross-project dependencies are detected and mapped
- [ ] Project-specific queries return accurate results

### Should Have

- [ ] Intelligent project type detection
- [ ] Comprehensive project health metrics
- [ ] Project hierarchy visualization
- [ ] Cross-project analysis capabilities
- [ ] Project-specific configuration management

### Could Have

- [ ] Advanced project dependency analysis
- [ ] Project health scoring and recommendations
- [ ] Project comparison and benchmarking
- [ ] Automated project structure optimization
- [ ] Project-specific code quality analysis

### Won't Have

- [ ] Automatic project restructuring
- [ ] Complex dependency resolution
- [ ] Project migration tools
- [ ] Real-time project monitoring

## Notes

This story addresses a critical gap in enterprise code analysis by providing comprehensive support for multi-project repositories. The implementation will make the system significantly more valuable for enterprise environments where repositories often contain multiple related projects or modules. The hierarchical structure and cross-project analysis capabilities will enable deeper insights into code organization and dependencies.

## Related Stories

- STORY_001_REPOSITORY_TRACKING: Provides foundation for repository metadata
- STORY_004_NON_GIT_PROJECT_SUPPORT: Related to repository structure support
- STORY_006_LLM_MCP_INTEGRATION: Will benefit from sub-project understanding
- STORY_009_PHASE3_LLM_INTEGRATION: Will leverage project hierarchy for better analysis
