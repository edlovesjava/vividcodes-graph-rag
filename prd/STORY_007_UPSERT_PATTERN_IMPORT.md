# STORY_007_UPSERT_PATTERN_IMPORT

## Story Information

- **Story Number**: STORY_007
- **Story Name**: Upsert Pattern Import
- **Epic**: Data Management
- **Priority**: MEDIUM
- **Estimated Duration**: 1-2 weeks
- **Dependencies**: STORY_001_REPOSITORY_TRACKING (COMPLETED), STORY_003_DATA_MANAGEMENT_API (COMPLETED)
- **Status**: NOT_STARTED

## Overview

Currently, when re-importing code, the system creates duplicate nodes and relationships, leading to data proliferation and inconsistent graph state. This story implements an upsert pattern that updates existing nodes/relationships or inserts new ones during re-imports, preventing data duplication and maintaining data integrity.

## User Story

**As a** developer who frequently updates and re-imports code  
**I want** the system to intelligently update existing nodes or create new ones  
**So that** I can maintain a clean, consistent graph database without duplicates

## Background

The current import process always creates new nodes and relationships, which leads to several problems:

- Duplicate nodes for the same code elements
- Inconsistent graph state after multiple imports
- Performance degradation due to data proliferation
- Difficulty in tracking code changes over time
- Inefficient storage usage

An upsert pattern will:

- Identify existing nodes based on unique identifiers
- Update existing nodes with new information
- Create new nodes only when they don't exist
- Maintain relationship integrity
- Provide clear audit trail of changes

## Acceptance Criteria

- [ ] Implement unique node identification strategy
- [ ] Create upsert operations for all node types (Class, Method, Field, Package)
- [ ] Handle relationship upsert operations
- [ ] Maintain data integrity during upsert operations
- [ ] Provide performance optimization for large datasets
- [ ] Add audit trail for upsert operations
- [ ] Support both incremental and full upsert modes
- [ ] Handle conflicts during upsert operations
- [ ] Provide rollback capabilities for failed upserts
- [ ] Add configuration options for upsert behavior
- [ ] Update import API to support upsert mode
- [ ] Add validation for upsert operations
- [ ] Provide clear error messages for upsert failures
- [ ] Add monitoring and metrics for upsert performance

## Technical Requirements

### Functional Requirements

- [ ] Implement unique node identification strategy
- [ ] Create upsert operations for all node types
- [ ] Handle relationship upsert operations
- [ ] Maintain data integrity during upsert operations
- [ ] Provide performance optimization for large datasets
- [ ] Add audit trail for upsert operations
- [ ] Support both incremental and full upsert modes

### Non-Functional Requirements

- [ ] Upsert performance comparable to insert operations
- [ ] Memory usage remains within acceptable limits
- [ ] Transaction safety for upsert operations
- [ ] Scalable architecture for large datasets
- [ ] Comprehensive error handling and recovery

## Technical Implementation

### Architecture Changes

Add upsert layer to the import process, with intelligent node identification and conflict resolution.

### New Components

- **UpsertService**: Main service for handling upsert operations
- **NodeIdentifierService**: Service for unique node identification
- **ConflictResolutionService**: Service for handling upsert conflicts
- **AuditService**: Service for tracking upsert operations

### Modified Components

- **JavaParserService**: Enhanced to support upsert mode
- **GraphService**: Updated for upsert operations
- **IngestionController**: Modified to support upsert options

### Database Schema Changes

```cypher
// Add audit trail for upsert operations
(:UpsertAudit {
  id: String!,
  operationId: String!,
  operationType: String!,    // "insert", "update", "skip"
  nodeType: String!,         // "Class", "Method", "Field", "Package"
  nodeId: String!,
  oldValues: Map,           // Previous values (for updates)
  newValues: Map,           // New values
  timestamp: DateTime,
  source: String            // Import source identifier
})

// Add unique constraints for node identification
CREATE CONSTRAINT class_unique IF NOT EXISTS
FOR (c:Class) REQUIRE c.id IS UNIQUE

CREATE CONSTRAINT method_unique IF NOT EXISTS
FOR (m:Method) REQUIRE m.id IS UNIQUE

CREATE CONSTRAINT field_unique IF NOT EXISTS
FOR (f:Field) REQUIRE f.id IS UNIQUE

CREATE CONSTRAINT package_unique IF NOT EXISTS
FOR (p:Package) REQUIRE p.id IS UNIQUE
```

### API Changes

```json
{
  "ingest": {
    "sourcePath": "/path/to/code",
    "includeTestFiles": false,
    "upsertMode": "incremental", // NEW: "incremental", "full", "insert-only"
    "conflictResolution": "update", // NEW: "update", "skip", "fail"
    "auditTrail": true // NEW: Enable audit trail
  }
}
```

## Validation Cases

### Test Scenarios

- [ ] Import same code twice with upsert mode enabled
- [ ] Verify no duplicate nodes are created
- [ ] Verify existing nodes are updated with new information
- [ ] Test incremental upsert mode
- [ ] Test full upsert mode
- [ ] Verify relationship integrity is maintained
- [ ] Test conflict resolution strategies

### Edge Cases

- [ ] Handle nodes with conflicting unique identifiers
- [ ] Manage large datasets with mixed insert/update operations
- [ ] Handle partial upsert failures
- [ ] Deal with concurrent upsert operations
- [ ] Handle schema changes during upsert

## Success Criteria

### Functional Success

- [ ] No duplicate nodes created during re-imports
- [ ] Existing nodes updated with new information
- [ ] Relationship integrity maintained
- [ ] Audit trail provides clear change history
- [ ] Conflict resolution works correctly

### Performance Success

- [ ] Upsert performance comparable to insert operations
- [ ] Memory usage remains within acceptable limits
- [ ] Scalable for large datasets
- [ ] Efficient conflict detection and resolution

### Quality Success

- [ ] Data integrity maintained throughout upsert process
- [ ] Clear audit trail for all operations
- [ ] Comprehensive error handling
- [ ] Rollback capabilities for failed operations

## Dependencies

### External Dependencies

- Neo4j Graph Database: Available
- Transaction Management: Available

### Internal Dependencies

- STORY_001_REPOSITORY_TRACKING: COMPLETED
- STORY_003_DATA_MANAGEMENT_API: COMPLETED
- GraphService: Available
- JavaParserService: Available

## Deliverables

### Code Changes

- [ ] UpsertService implementation
- [ ] NodeIdentifierService implementation
- [ ] ConflictResolutionService implementation
- [ ] AuditService implementation
- [ ] Enhanced JavaParserService for upsert mode
- [ ] Updated GraphService for upsert operations
- [ ] Modified IngestionController for upsert options

### Documentation

- [ ] Upsert pattern implementation guide
- [ ] Configuration options documentation
- [ ] Best practices for upsert usage
- [ ] Troubleshooting guide for upsert issues

### Testing

- [ ] Unit tests for upsert services
- [ ] Integration tests for upsert operations
- [ ] Performance tests for large datasets
- [ ] End-to-end tests for complete upsert workflows

## Risk Assessment

### Technical Risks

- **Risk**: Complex conflict resolution logic
- **Impact**: MEDIUM
- **Mitigation**: Start with simple strategies, iterate based on real-world usage

- **Risk**: Performance impact on large datasets
- **Impact**: MEDIUM
- **Mitigation**: Implement efficient indexing and batch operations

- **Risk**: Data integrity issues during upsert
- **Impact**: HIGH
- **Mitigation**: Comprehensive transaction management and rollback capabilities

### Business Risks

- **Risk**: User confusion about upsert behavior
- **Impact**: LOW
- **Mitigation**: Clear documentation and configuration options

## Example Usage

### API Examples

```bash
# Incremental upsert mode
curl -X POST http://localhost:8080/api/v1/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "sourcePath": "/path/to/code",
    "includeTestFiles": false,
    "upsertMode": "incremental",
    "conflictResolution": "update",
    "auditTrail": true
  }'

# Full upsert mode
curl -X POST http://localhost:8080/api/v1/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "sourcePath": "/path/to/code",
    "includeTestFiles": false,
    "upsertMode": "full",
    "conflictResolution": "skip",
    "auditTrail": true
  }'
```

### Query Examples

```cypher
// Check for duplicate nodes
MATCH (c:Class)
WITH c.name, c.package, count(*) as count
WHERE count > 1
RETURN c.name, c.package, count

// View audit trail
MATCH (a:UpsertAudit)
WHERE a.operationId = $operationId
RETURN a.operationType, a.nodeType, a.nodeId, a.timestamp
ORDER BY a.timestamp DESC
```

### Expected Output

```json
{
  "upsert": {
    "operationId": "upsert-2024-01-15-001",
    "summary": {
      "totalNodes": 150,
      "inserted": 25,
      "updated": 120,
      "skipped": 5,
      "errors": 0
    },
    "auditTrail": {
      "enabled": true,
      "entries": 150
    },
    "performance": {
      "executionTime": 2500,
      "memoryUsage": "45MB"
    }
  }
}
```

## Implementation Phases

> **📋 Detailed Task Breakdown**: See [`STORY_007_TASK_BREAKDOWN.md`](./STORY_007_TASK_BREAKDOWN.md) for comprehensive task-by-task implementation plan with 45 specific subtasks, dependencies, and deliverables.

### Phase 1: Foundation & Core Services (Week 1)

- [ ] **Task 1.1**: Database Schema Preparation - Create unique constraints for all node types
- [ ] **Task 1.2**: NodeIdentifierService Implementation - Create service for unique node identification  
- [ ] **Task 1.3**: Core UpsertService Implementation - Implement basic upsert operations for all node types
- [ ] **Task 1.4**: Basic Upsert Integration with GraphService - Extend GraphService with upsert capabilities

### Phase 2: Enhanced Features & Audit Trail (Week 2)

- [ ] **Task 2.1**: ConflictResolutionService Implementation - Create configurable conflict resolution strategies
- [ ] **Task 2.2**: AuditService Implementation - Create audit trail tracking for all upsert operations
- [ ] **Task 2.3**: Upsert Mode Configuration & API Enhancement - Extend API with upsert options

### Phase 3: Integration & Performance Optimization (Week 3)

- [ ] **Task 3.1**: JavaParserService Integration - Modify JavaParserService to support upsert mode
- [ ] **Task 3.2**: Performance Optimization & Monitoring - Implement batch operations and performance monitoring
- [ ] **Task 3.3**: Comprehensive Testing & Validation - Create end-to-end test suite with real-world validation
- [ ] **Task 3.4**: Documentation & User Guide - Create technical and user documentation

## Future Considerations

### Phase 2.5 Integration

- MCP server can leverage upsert for efficient data updates
- Agent queries can benefit from consistent data state
- Semantic search can use updated node information

### Phase 3 Integration

- LLM integration can rely on consistent graph state
- Advanced features can use audit trail for change analysis
- Real-time updates can use upsert pattern

## Acceptance Criteria Checklist

### Must Have

- [ ] No duplicate nodes created during re-imports
- [ ] Existing nodes updated with new information
- [ ] Relationship integrity maintained
- [ ] Audit trail provides clear change history
- [ ] Performance comparable to insert operations

### Should Have

- [ ] Support for incremental and full upsert modes
- [ ] Configurable conflict resolution strategies
- [ ] Rollback capabilities for failed operations
- [ ] Clear documentation and examples

### Could Have

- [ ] Advanced conflict resolution strategies
- [ ] Real-time upsert monitoring
- [ ] Automated duplicate detection
- [ ] Integration with external change tracking

### Won't Have

- [ ] Automatic schema migration during upsert
- [ ] Cross-database upsert operations
- [ ] Real-time synchronization
- [ ] Integration with external version control

## Notes

This story addresses a critical data management issue that affects system performance and data quality. The upsert pattern will significantly improve the user experience for developers who frequently update and re-import code.

## Related Stories

- STORY_001_REPOSITORY_TRACKING: Provides foundation for node identification
- STORY_003_DATA_MANAGEMENT_API: Related to data management capabilities
- STORY_004_NON_GIT_PROJECT_SUPPORT: May benefit from Git status awareness
- STORY_006_LLM_MCP_INTEGRATION: Can leverage consistent data state
