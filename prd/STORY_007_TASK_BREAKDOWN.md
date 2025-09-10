# STORY_007_UPSERT_PATTERN_IMPORT - Detailed Task Breakdown

## Overview

This document provides a comprehensive task breakdown for STORY_007_UPSERT_PATTERN_IMPORT, implementing an upsert pattern for preventing duplicate nodes and maintaining data integrity during re-imports.

## Phase 1: Foundation & Core Services (Week 1)

### Task 1.1: Database Schema Preparation
**Duration**: 1 day  
**Priority**: HIGH  
**Dependencies**: None

#### Subtasks:
- [ ] **1.1.1** Create unique constraints for all node types in Neo4j
  - [ ] Add unique constraint for Class nodes: `CREATE CONSTRAINT class_unique IF NOT EXISTS FOR (c:Class) REQUIRE c.id IS UNIQUE`
  - [ ] Add unique constraint for Method nodes: `CREATE CONSTRAINT method_unique IF NOT EXISTS FOR (m:Method) REQUIRE m.id IS UNIQUE`
  - [ ] Add unique constraint for Field nodes: `CREATE CONSTRAINT field_unique IF NOT EXISTS FOR (f:Field) REQUIRE f.id IS UNIQUE`
  - [ ] Add unique constraint for Package nodes: `CREATE CONSTRAINT package_unique IF NOT EXISTS FOR (p:Package) REQUIRE p.id IS UNIQUE`
  - [ ] Add unique constraint for Repository nodes: `CREATE CONSTRAINT repository_unique IF NOT EXISTS FOR (r:Repository) REQUIRE r.id IS UNIQUE`
  - [ ] Add unique constraint for SubProject nodes: `CREATE CONSTRAINT subproject_unique IF NOT EXISTS FOR (s:SubProject) REQUIRE s.id IS UNIQUE`
  - [ ] Add unique constraint for Annotation nodes: `CREATE CONSTRAINT annotation_unique IF NOT EXISTS FOR (a:Annotation) REQUIRE a.id IS UNIQUE`

- [ ] **1.1.2** Create UpsertAudit node schema
  - [ ] Define UpsertAudit node properties schema
  - [ ] Add index on operationId for efficient querying: `CREATE INDEX upsert_audit_operation IF NOT EXISTS FOR (a:UpsertAudit) ON (a.operationId)`
  - [ ] Add index on timestamp for chronological queries: `CREATE INDEX upsert_audit_timestamp IF NOT EXISTS FOR (a:UpsertAudit) ON (a.timestamp)`

- [ ] **1.1.3** Test constraint creation
  - [ ] Write test to verify all constraints are created successfully
  - [ ] Write test to ensure constraints prevent duplicate nodes
  - [ ] Write test to verify constraint behavior with existing data

**Deliverables**:
- [ ] `Neo4jSchemaService.addUpsertConstraints()` method
- [ ] `UpsertAuditNode` model class
- [ ] Unit tests for constraint creation
- [ ] Database migration script for existing deployments

---

### Task 1.2: NodeIdentifierService Implementation  
**Duration**: 2 days  
**Priority**: HIGH  
**Dependencies**: Task 1.1 completed

#### Subtasks:
- [ ] **1.2.1** Create NodeIdentifierService interface and implementation
  - [ ] Define `NodeIdentifierService` interface with methods for each node type
  - [ ] Implement `generateClassId(String packageName, String className)` method
  - [ ] Implement `generateMethodId(String classId, String methodName, List<String> parameterTypes)` method
  - [ ] Implement `generateFieldId(String classId, String fieldName, String fieldType)` method
  - [ ] Implement `generatePackageId(String packageName)` method
  - [ ] Implement `generateRepositoryId(String repositoryPath, String repositoryName)` method
  - [ ] Implement `generateSubProjectId(String repositoryId, String subProjectPath)` method
  - [ ] Implement `generateAnnotationId(String packageName, String annotationName)` method

- [ ] **1.2.2** Create ID validation and consistency checks
  - [ ] Implement `validateNodeId(String nodeId, NodeType nodeType)` method
  - [ ] Implement `isConsistentId(Node existingNode, Node newNode)` method
  - [ ] Add ID format validation (non-null, non-empty, consistent format)
  - [ ] Add collision detection for generated IDs

- [ ] **1.2.3** Create ID normalization utilities
  - [ ] Implement method signature normalization for method IDs
  - [ ] Implement package name normalization
  - [ ] Implement file path normalization for consistent IDs
  - [ ] Handle special characters in names/paths

**Deliverables**:
- [ ] `NodeIdentifierService` interface
- [ ] `NodeIdentifierServiceImpl` class  
- [ ] `NodeIdValidator` utility class
- [ ] Unit tests for ID generation and validation
- [ ] Integration tests with existing node creation

---

### Task 1.3: Core UpsertService Implementation
**Duration**: 3 days  
**Priority**: HIGH  
**Dependencies**: Task 1.1, 1.2 completed

#### Subtasks:
- [ ] **1.3.1** Create UpsertService interface and basic implementation
  - [ ] Define `UpsertService` interface with core upsert methods
  - [ ] Implement `upsertClass(ClassNode classNode)` method
  - [ ] Implement `upsertMethod(MethodNode methodNode)` method  
  - [ ] Implement `upsertField(FieldNode fieldNode)` method
  - [ ] Implement `upsertPackage(PackageNode packageNode)` method
  - [ ] Implement `upsertRepository(RepositoryNode repositoryNode)` method
  - [ ] Implement `upsertSubProject(SubProjectNode subProjectNode)` method
  - [ ] Implement `upsertAnnotation(AnnotationNode annotationNode)` method

- [ ] **1.3.2** Implement node existence checking logic
  - [ ] Create `nodeExists(String nodeId, NodeType nodeType)` method
  - [ ] Create `findExistingNode(String nodeId, NodeType nodeType)` method
  - [ ] Add efficient querying using unique constraints
  - [ ] Handle multiple node type checks in batch

- [ ] **1.3.3** Implement node comparison and update logic
  - [ ] Create `compareNodes(Node existing, Node incoming)` method
  - [ ] Create `updateNodeProperties(Node existing, Node incoming)` method
  - [ ] Implement property-level change detection
  - [ ] Handle different property types (String, List, Map, primitive types)
  - [ ] Preserve audit information in updated nodes

- [ ] **1.3.4** Implement transaction management for upsert operations
  - [ ] Wrap upsert operations in Neo4j transactions
  - [ ] Implement rollback logic for failed upserts
  - [ ] Add retry logic for transient failures
  - [ ] Handle concurrent upsert operations

**Deliverables**:
- [ ] `UpsertService` interface
- [ ] `UpsertServiceImpl` class
- [ ] `NodeComparisonResult` data class
- [ ] `UpsertResult` data class with operation statistics
- [ ] Unit tests for each upsert method
- [ ] Integration tests with Neo4j database

---

### Task 1.4: Basic Upsert Integration with GraphService
**Duration**: 1 day  
**Priority**: HIGH  
**Dependencies**: Task 1.3 completed

#### Subtasks:
- [ ] **1.4.1** Extend GraphService with upsert capabilities
  - [ ] Add `saveOrUpdateClass(ClassNode classNode)` method to GraphService
  - [ ] Add `saveOrUpdateMethod(MethodNode methodNode)` method to GraphService
  - [ ] Add `saveOrUpdateField(FieldNode fieldNode)` method to GraphService
  - [ ] Add `saveOrUpdatePackage(PackageNode packageNode)` method to GraphService
  - [ ] Add `saveOrUpdateRepository(RepositoryNode repositoryNode)` method to GraphService
  - [ ] Add `saveOrUpdateSubProject(SubProjectNode subProjectNode)` method to GraphService

- [ ] **1.4.2** Create configuration for upsert mode
  - [ ] Add upsert mode configuration to `ParserConfig`
  - [ ] Create `UpsertMode` enum (INSERT_ONLY, UPSERT, UPDATE_ONLY)
  - [ ] Add upsert configuration validation

- [ ] **1.4.3** Update relationship handling for upsert operations
  - [ ] Ensure relationships are properly handled during node upserts
  - [ ] Prevent duplicate relationships
  - [ ] Update relationship properties when needed

**Deliverables**:
- [ ] Updated `GraphService` interface and implementation
- [ ] `UpsertMode` enum and configuration classes
- [ ] Unit tests for extended GraphService methods
- [ ] Integration tests for upsert mode configuration

## Phase 2: Enhanced Features & Audit Trail (Week 2)

### Task 2.1: ConflictResolutionService Implementation
**Duration**: 2 days  
**Priority**: MEDIUM  
**Dependencies**: Phase 1 completed

#### Subtasks:
- [ ] **2.1.1** Create ConflictResolutionService interface and implementation
  - [ ] Define conflict resolution strategies enum (UPDATE, SKIP, FAIL)
  - [ ] Implement `resolveClassConflict(ClassNode existing, ClassNode incoming, ConflictStrategy strategy)` method
  - [ ] Implement `resolveMethodConflict(MethodNode existing, MethodNode incoming, ConflictStrategy strategy)` method
  - [ ] Implement `resolveFieldConflict(FieldNode existing, FieldNode incoming, ConflictStrategy strategy)` method
  - [ ] Implement conflict resolution for all node types

- [ ] **2.1.2** Implement conflict detection logic
  - [ ] Create `detectConflicts(Node existing, Node incoming)` method
  - [ ] Identify property-level conflicts
  - [ ] Create conflict severity assessment (MINOR, MAJOR, CRITICAL)
  - [ ] Generate detailed conflict reports

- [ ] **2.1.3** Create configurable conflict resolution policies
  - [ ] Implement per-property conflict resolution rules
  - [ ] Add timestamp-based conflict resolution
  - [ ] Add source priority-based conflict resolution
  - [ ] Create policy configuration system

**Deliverables**:
- [ ] `ConflictResolutionService` interface and implementation
- [ ] `ConflictStrategy` enum and related classes
- [ ] `ConflictReport` data class
- [ ] Unit tests for conflict resolution scenarios
- [ ] Integration tests with UpsertService

---

### Task 2.2: AuditService Implementation
**Duration**: 2 days  
**Priority**: MEDIUM  
**Dependencies**: Task 2.1 completed

#### Subtasks:
- [ ] **2.2.1** Create AuditService interface and implementation
  - [ ] Define `AuditService` interface with audit methods
  - [ ] Implement `recordUpsertOperation(UpsertOperation operation)` method
  - [ ] Implement `recordConflictResolution(ConflictResolution resolution)` method
  - [ ] Implement `generateOperationId()` method for unique operation tracking

- [ ] **2.2.2** Implement audit trail data model
  - [ ] Create `UpsertOperation` data class with all operation details
  - [ ] Create `ConflictResolution` data class
  - [ ] Implement audit trail querying methods
  - [ ] Add audit trail cleanup/archival methods

- [ ] **2.2.3** Create audit trail persistence
  - [ ] Implement Neo4j persistence for UpsertAudit nodes
  - [ ] Create relationships between audit nodes and affected entities
  - [ ] Implement efficient audit querying with indexes
  - [ ] Add audit trail export functionality

**Deliverables**:
- [ ] `AuditService` interface and implementation  
- [ ] `UpsertOperation` and related audit data classes
- [ ] Audit trail querying API
- [ ] Unit tests for audit functionality
- [ ] Integration tests with UpsertService

---

### Task 2.3: Upsert Mode Configuration & API Enhancement
**Duration**: 1 day  
**Priority**: MEDIUM  
**Dependencies**: Task 2.1, 2.2 completed

#### Subtasks:
- [ ] **2.3.1** Extend IngestionRequest with upsert options
  - [ ] Add `upsertMode` field to `IngestionRequest` (INCREMENTAL, FULL, INSERT_ONLY)
  - [ ] Add `conflictResolution` field to specify conflict resolution strategy
  - [ ] Add `auditTrail` boolean flag to enable/disable audit trail
  - [ ] Add validation for upsert configuration combinations

- [ ] **2.3.2** Update IngestionController to handle upsert options
  - [ ] Modify ingestion endpoint to process upsert configuration
  - [ ] Add upsert mode validation
  - [ ] Return upsert statistics in API response
  - [ ] Add error handling for upsert-specific failures

- [ ] **2.3.3** Create upsert configuration documentation
  - [ ] Document all upsert mode options
  - [ ] Create examples for different upsert scenarios
  - [ ] Document conflict resolution strategies
  - [ ] Create best practices guide

**Deliverables**:
- [ ] Updated `IngestionRequest` with upsert fields
- [ ] Updated `IngestionController` with upsert handling
- [ ] `UpsertStatistics` response class
- [ ] API documentation for upsert options
- [ ] Configuration examples and best practices

## Phase 3: Integration & Performance Optimization (Week 3)

### Task 3.1: JavaParserService Integration
**Duration**: 2 days  
**Priority**: HIGH  
**Dependencies**: Phase 2 completed

#### Subtasks:
- [ ] **3.1.1** Modify JavaParserService to support upsert mode
  - [ ] Update `parseDirectory()` method to check upsert configuration
  - [ ] Modify node creation logic to use UpsertService when enabled
  - [ ] Update visitor pattern to handle upsert operations
  - [ ] Ensure backward compatibility with insert-only mode

- [ ] **3.1.2** Update JavaGraphVisitor for upsert operations
  - [ ] Modify visitor methods to use upsert instead of direct creation
  - [ ] Update relationship creation to handle upserted nodes
  - [ ] Add upsert statistics collection during parsing
  - [ ] Handle upsert failures gracefully during parsing

- [ ] **3.1.3** Create upsert-aware parsing flow
  - [ ] Implement pre-parsing phase to identify existing nodes
  - [ ] Create parsing batches for efficient upsert operations
  - [ ] Add progress reporting for upsert operations
  - [ ] Implement error recovery for failed upserts

**Deliverables**:
- [ ] Updated `JavaParserService` with upsert support
- [ ] Updated `JavaGraphVisitor` for upsert operations  
- [ ] Upsert-aware parsing flow implementation
- [ ] Integration tests for parsing with upsert mode
- [ ] Performance benchmarks for upsert vs. insert-only parsing

---

### Task 3.2: Performance Optimization & Monitoring
**Duration**: 2 days  
**Priority**: MEDIUM  
**Dependencies**: Task 3.1 completed

#### Subtasks:
- [ ] **3.2.1** Implement batch upsert operations
  - [ ] Create `batchUpsert()` methods for processing multiple nodes
  - [ ] Implement optimal batch sizes for different node types
  - [ ] Add parallel processing for independent upsert operations
  - [ ] Create batch error handling and partial success reporting

- [ ] **3.2.2** Add performance monitoring
  - [ ] Create metrics collection for upsert operations
  - [ ] Implement timing measurements for each upsert phase
  - [ ] Add memory usage monitoring during upsert operations
  - [ ] Create performance reporting and alerting

- [ ] **3.2.3** Optimize database queries for upsert operations
  - [ ] Review and optimize Cypher queries for node existence checks
  - [ ] Implement query caching for frequently accessed nodes
  - [ ] Add database connection pooling optimization
  - [ ] Create index optimization recommendations

**Deliverables**:
- [ ] Batch upsert implementation with optimal performance
- [ ] Performance monitoring and metrics collection
- [ ] Database query optimization for upsert operations
- [ ] Performance benchmarking report
- [ ] Optimization recommendations documentation

---

### Task 3.3: Comprehensive Testing & Validation
**Duration**: 2 days  
**Priority**: HIGH  
**Dependencies**: Task 3.1, 3.2 completed

#### Subtasks:
- [ ] **3.3.1** Create comprehensive unit test suite
  - [ ] Write unit tests for all UpsertService methods
  - [ ] Write unit tests for NodeIdentifierService methods
  - [ ] Write unit tests for ConflictResolutionService methods
  - [ ] Write unit tests for AuditService methods
  - [ ] Achieve >90% code coverage for upsert-related code

- [ ] **3.3.2** Create integration test suite
  - [ ] Write integration tests for full upsert workflow
  - [ ] Test upsert with real-world code repositories
  - [ ] Test concurrent upsert operations
  - [ ] Test upsert performance with large datasets
  - [ ] Test error scenarios and recovery

- [ ] **3.3.3** Create end-to-end test scenarios
  - [ ] Test complete import → re-import → upsert workflow
  - [ ] Test upsert with different configuration combinations
  - [ ] Test audit trail functionality end-to-end
  - [ ] Test API integration with upsert options
  - [ ] Validate no data loss or corruption during upsert

**Deliverables**:
- [ ] Comprehensive unit test suite with >90% coverage
- [ ] Integration test suite covering all major scenarios
- [ ] End-to-end test suite with real-world validation
- [ ] Test automation and CI/CD integration
- [ ] Test documentation and maintenance guide

---

### Task 3.4: Documentation & User Guide
**Duration**: 1 day  
**Priority**: MEDIUM  
**Dependencies**: Task 3.3 completed

#### Subtasks:
- [ ] **3.4.1** Create technical documentation
  - [ ] Document upsert architecture and design decisions
  - [ ] Create API reference for upsert-related endpoints
  - [ ] Document configuration options and their effects
  - [ ] Create troubleshooting guide for common issues

- [ ] **3.4.2** Create user guide and examples
  - [ ] Write user guide for upsert functionality
  - [ ] Create practical examples for different use cases
  - [ ] Document best practices for upsert usage
  - [ ] Create migration guide from insert-only to upsert mode

- [ ] **3.4.3** Create developer documentation
  - [ ] Document code structure and key classes
  - [ ] Create contribution guide for upsert-related features
  - [ ] Document extension points for custom conflict resolution
  - [ ] Create performance tuning guide

**Deliverables**:
- [ ] Complete technical documentation
- [ ] User guide with practical examples
- [ ] Developer documentation and contribution guide
- [ ] API reference documentation
- [ ] Performance tuning and best practices guide

## Success Criteria & Validation

### Functional Validation
- [ ] **No Duplicates**: Import same code twice - verify no duplicate nodes created
- [ ] **Update Accuracy**: Modify code and re-import - verify existing nodes updated correctly
- [ ] **Relationship Integrity**: Verify all relationships maintained during upsert operations
- [ ] **Audit Trail**: Verify complete audit trail for all upsert operations
- [ ] **Performance**: Upsert performance within 20% of insert-only performance

### Quality Validation  
- [ ] **Test Coverage**: >90% code coverage for all upsert-related code
- [ ] **Error Handling**: Graceful handling of all error scenarios
- [ ] **Data Integrity**: No data loss or corruption during upsert operations
- [ ] **Scalability**: Successful operation with datasets >10,000 nodes
- [ ] **Documentation**: Complete documentation for users and developers

### Integration Validation
- [ ] **API Integration**: All upsert options work correctly via REST API
- [ ] **Configuration**: All configuration combinations work as expected
- [ ] **Backward Compatibility**: Insert-only mode continues to work unchanged
- [ ] **Real-world Testing**: Successful upsert with vivid-coreapi project
- [ ] **Monitoring**: Performance monitoring works correctly in production

## Risk Mitigation

### Technical Risks
- [ ] **Performance Impact**: Benchmark all operations, optimize critical paths
- [ ] **Data Integrity**: Comprehensive transaction management and rollback
- [ ] **Complex Logic**: Start simple, iterate based on real usage
- [ ] **Memory Usage**: Monitor memory during operations, implement streaming for large datasets

### Implementation Risks
- [ ] **Scope Creep**: Stick to defined acceptance criteria
- [ ] **Testing Gaps**: Create comprehensive test matrix covering all scenarios
- [ ] **Integration Issues**: Test integration points early and frequently
- [ ] **Performance Regression**: Continuous performance monitoring and benchmarking

## Dependencies & Prerequisites

### External Dependencies
- [ ] Neo4j database with constraint support
- [ ] Java 17+ for advanced language features
- [ ] Spring Boot transaction management
- [ ] Existing GraphService and JavaParserService implementations

### Internal Dependencies
- [ ] STORY_001_REPOSITORY_TRACKING: ✅ COMPLETED
- [ ] STORY_003_DATA_MANAGEMENT_API: ✅ COMPLETED
- [ ] Existing node model classes (ClassNode, MethodNode, etc.)
- [ ] Existing GraphService interface and implementation

## Deliverables Summary

### Code Components
- [ ] `NodeIdentifierService` - Unique ID generation for all node types
- [ ] `UpsertService` - Core upsert operations for all node types
- [ ] `ConflictResolutionService` - Configurable conflict resolution strategies
- [ ] `AuditService` - Audit trail tracking for all upsert operations
- [ ] Updated `GraphService` - Extended with upsert capabilities
- [ ] Updated `JavaParserService` - Integrated with upsert operations
- [ ] Updated `IngestionController` - API support for upsert options

### Testing & Validation
- [ ] Comprehensive unit test suite (>90% coverage)
- [ ] Integration test suite for all major scenarios
- [ ] End-to-end test suite with real-world validation
- [ ] Performance benchmarking and monitoring
- [ ] Load testing with large datasets

### Documentation
- [ ] Technical architecture documentation
- [ ] API reference for upsert functionality
- [ ] User guide with practical examples
- [ ] Developer guide and contribution documentation
- [ ] Performance tuning and best practices guide

## Next Steps After Completion

### Integration Opportunities
- [ ] **STORY_016_XML_CONFIGURATION_PARSING**: Leverage upsert for efficient XML config updates
- [ ] **STORY_006_MCP_INTEGRATION**: Use upsert for consistent data state in MCP server
- [ ] **Phase 3 LLM Integration**: Benefit from consistent graph state for LLM operations

### Future Enhancements
- [ ] Real-time upsert capabilities for live code analysis
- [ ] Advanced conflict resolution strategies based on code analysis
- [ ] Integration with version control for change-based upserts
- [ ] Distributed upsert operations for multi-repository scenarios

---

**Total Estimated Duration**: 3 weeks  
**Total Tasks**: 45 subtasks across 12 major tasks  
**Critical Path**: Phase 1 → Phase 2 → Phase 3 (sequential dependency)  
**Team Size**: 1-2 developers with Neo4j and Spring Boot expertise
