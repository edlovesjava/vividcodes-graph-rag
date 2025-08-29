# STORY_013: Class Dependency Analysis Enhancement

## Overview

Enhance the Java parser to capture comprehensive class-to-class dependency relationships through import statements, object instantiation, method calls, and type usage patterns. This will enable complete dependency analysis and architectural insights for Java codebases.

## Business Value

**Problem**: The current parser only captures inheritance (`EXTENDS`/`IMPLEMENTS`) relationships between classes but misses critical runtime dependencies through imports, composition, and method calls. This limits architectural analysis, impact assessment, and dependency tracking capabilities.

**Solution**: Implement comprehensive class dependency detection to create `USES` relationships between classes based on imports, object instantiation, static method calls, field types, and method signatures.

**Impact**:
- **Complete Dependency Mapping**: Understand full class interaction patterns beyond inheritance
- **Impact Analysis**: Identify which classes are affected by changes to a specific class
- **Architecture Visualization**: Generate comprehensive dependency graphs for system understanding
- **Dead Code Detection**: Identify unused classes and dependencies
- **Package Coupling Analysis**: Measure and optimize inter-package dependencies
- **Refactoring Support**: Safely refactor code with full dependency awareness

## Acceptance Criteria

### Must Have
- [ ] Parse Java import statements and create Class→Class USES relationships
- [ ] Detect object instantiation (`new ClassName()`) and create USES relationships
- [ ] Identify static method calls (`ClassName.staticMethod()`) and create USES relationships
- [ ] Extract field type dependencies (`private ClassName field`) and create USES relationships
- [ ] Capture method parameter and return type dependencies
- [ ] Distinguish between different types of USES relationships (import, instantiation, static call, etc.)
- [ ] Handle both fully qualified names and imported class names
- [ ] Create placeholder class nodes for external dependencies (framework/library classes)

### Should Have
- [ ] Track annotation usage and create USES relationships
- [ ] Handle generic type parameters (`List<ClassName>`)
- [ ] Detect lambda expressions and method references
- [ ] Support inner class and nested class dependencies
- [ ] Capture exception handling dependencies (`catch (ExceptionClass e)`)

### Could Have
- [ ] Analyze reflection-based class usage
- [ ] Support annotation processor generated dependencies
- [ ] Track dependency injection patterns (Spring @Autowired, etc.)
- [ ] Measure dependency strength (frequency of usage)

## Technical Requirements

### Parser Enhancements

**Import Statement Processing:**
```java
// Add visitor method for import declarations
@Override
public void visit(ImportDeclaration importDecl, Void arg) {
    String importedClassName = extractClassName(importDecl.getNameAsString());
    ClassNode importedClass = getOrCreateClassNode(importedClassName);
    graphService.createRelationship(currentClass.getId(), importedClass.getId(), "USES", 
        Map.of("type", "import", "fullyQualifiedName", importDecl.getNameAsString()));
}
```

**Object Instantiation Detection:**
```java
// Detect new expressions
methodDecl.findAll(ObjectCreationExpr.class).forEach(newExpr -> {
    String className = newExpr.getType().getNameAsString();
    ClassNode usedClass = resolveClassName(className);
    graphService.createRelationship(currentClass.getId(), usedClass.getId(), "USES",
        Map.of("type", "instantiation", "context", "method:" + methodNode.getName()));
});
```

**Static Method Call Detection:**
```java
// Detect static method calls
methodDecl.findAll(MethodCallExpr.class).forEach(methodCall -> {
    if (methodCall.getScope().isPresent() && isClassReference(methodCall.getScope().get())) {
        String className = extractClassName(methodCall.getScope().get());
        ClassNode usedClass = resolveClassName(className);
        graphService.createRelationship(currentClass.getId(), usedClass.getId(), "USES",
            Map.of("type", "static_call", "method", methodCall.getNameAsString()));
    }
});
```

### Data Model Extensions

**Enhanced USES Relationship Properties:**
```cypher
// USES relationship with rich metadata
(:Class)-[:USES {
  type: "import|instantiation|static_call|field_type|parameter_type|return_type|annotation",
  context: "method_name|field_name|class_level",
  fullyQualifiedName: "com.example.package.ClassName",
  isExternal: true/false,
  count: 5  // frequency of usage
}]->(:Class)
```

**Class Node Enhancements:**
```java
// Add flags to ClassNode
private boolean isExternal = false;        // External library/framework class
private boolean isPlaceholder = false;     // Created for unresolved reference
private String sourceLibrary = null;       // Maven/Gradle dependency source
private Set<String> importedBy = new HashSet<>();  // Classes that import this
```

### Implementation Phases

#### Phase 1: Core Import and Instantiation Tracking (1 week)
- [ ] Add `ImportDeclaration` visitor to `JavaParserService`
- [ ] Implement object instantiation detection (`new ClassName()`)
- [ ] Create basic Class→Class USES relationships
- [ ] Add relationship metadata (type, context)
- [ ] Handle class name resolution (imports vs fully qualified names)

#### Phase 2: Advanced Dependency Detection (1 week)  
- [ ] Static method call detection
- [ ] Field type dependency extraction
- [ ] Method parameter and return type analysis
- [ ] Annotation usage tracking
- [ ] Generic type parameter handling

#### Phase 3: External Dependency Management (3 days)
- [ ] Create placeholder nodes for external classes
- [ ] Mark external vs internal dependencies
- [ ] Integrate with Maven/Gradle dependency metadata
- [ ] Handle framework class detection (Spring, JUnit, etc.)

## API Enhancements

### New Cypher Query Capabilities

**Class Dependency Analysis:**
```cypher
// Find all classes that depend on a specific class
MATCH (dependent:Class)-[:USES]->(target:Class {name: "TargetClass"})
RETURN dependent.name, collect(DISTINCT dependent.packageName) as packages

// Analyze dependency types for a class
MATCH (c:Class {name: "SomeClass"})-[u:USES]->(dependency:Class)
RETURN dependency.name, u.type, u.count
ORDER BY u.count DESC

// Find circular dependencies
MATCH (c1:Class)-[:USES*]->(c2:Class)-[:USES*]->(c1)
WHERE c1 <> c2
RETURN c1.name, c2.name
```

**Package Coupling Metrics:**
```cypher
// Calculate efferent coupling (Ce) - outgoing dependencies
MATCH (c:Class {packageName: "com.example.package"})-[:USES]->(external:Class)
WHERE external.packageName <> "com.example.package"
RETURN count(DISTINCT external.packageName) as efferentCoupling

// Calculate afferent coupling (Ca) - incoming dependencies  
MATCH (external:Class)-[:USES]->(c:Class {packageName: "com.example.package"})
WHERE external.packageName <> "com.example.package"
RETURN count(DISTINCT external.packageName) as afferentCoupling
```

### REST API Extensions

**New Dependency Analysis Endpoints:**
```java
GET /api/v1/analysis/dependencies/{className}
GET /api/v1/analysis/package-coupling/{packageName}
GET /api/v1/analysis/circular-dependencies
GET /api/v1/analysis/unused-dependencies
GET /api/v1/analysis/dependency-graph/{scope}
```

## Testing Strategy

### Unit Tests
- [ ] Test import statement parsing with various import patterns
- [ ] Verify object instantiation detection in different contexts
- [ ] Test static method call identification
- [ ] Validate class name resolution logic
- [ ] Test external vs internal class classification

### Integration Tests
- [ ] End-to-end dependency analysis on sample Java projects
- [ ] Verify correct USES relationship creation and metadata
- [ ] Test with complex inheritance and composition hierarchies
- [ ] Validate performance with large codebases (1000+ classes)

### Real-World Validation
- [ ] Test against `catalog-service` project to ensure comprehensive dependency capture
- [ ] Verify Spring framework dependency detection
- [ ] Test Maven/Gradle library dependency identification
- [ ] Validate annotation processing (@Service, @Autowired, etc.)

## Performance Considerations

### Optimization Strategies
- [ ] Implement class name caching to avoid repeated lookups
- [ ] Batch relationship creation for improved database performance
- [ ] Use lazy loading for external class metadata
- [ ] Implement incremental parsing for large codebases
- [ ] Cache resolved class mappings between parser runs

### Memory Management
- [ ] Limit placeholder class creation for unknown external dependencies
- [ ] Implement cleanup for unused external class nodes
- [ ] Use weak references for transient parsing data structures

## Migration Strategy

### Backward Compatibility
- [ ] Ensure existing EXTENDS/IMPLEMENTS relationships remain unchanged
- [ ] Add new USES relationships without affecting current queries
- [ ] Provide migration path for existing graph data
- [ ] Maintain API compatibility for current endpoints

### Data Migration
- [ ] Re-parse existing repositories to add missing USES relationships
- [ ] Provide option to incrementally update dependencies
- [ ] Create utility to identify and clean up orphaned class nodes

## Success Metrics

### Functional Metrics
- [ ] **Dependency Coverage**: >95% of actual class dependencies captured
- [ ] **External Library Detection**: >90% of framework/library usage identified  
- [ ] **Performance**: Parse 1000 classes with dependencies in <60 seconds
- [ ] **Accuracy**: <1% false positive dependency relationships

### Business Metrics
- [ ] **Impact Analysis**: Enable accurate change impact assessment
- [ ] **Architecture Insights**: Generate meaningful dependency visualizations
- [ ] **Code Quality**: Support refactoring decisions with dependency data
- [ ] **Technical Debt**: Identify tightly coupled packages and circular dependencies

## Examples

### Before Enhancement
```cypher
// Current state - only inheritance relationships
MATCH (c:Class {name: "UserService"})-[r]->(related:Class)
RETURN type(r), related.name
// Results: 
// "EXTENDS" -> "BaseService"
// "IMPLEMENTS" -> "UserOperations"
```

### After Enhancement  
```cypher
// Enhanced state - comprehensive dependencies
MATCH (c:Class {name: "UserService"})-[r]->(related:Class)
RETURN type(r), r.type, related.name
// Results:
// "EXTENDS" -> null -> "BaseService"
// "IMPLEMENTS" -> null -> "UserOperations" 
// "USES" -> "import" -> "UserRepository"
// "USES" -> "instantiation" -> "EmailService"
// "USES" -> "static_call" -> "ValidationUtils"
// "USES" -> "field_type" -> "Logger"
```

## Dependencies

### Prerequisites
- [ ] Current JavaParser integration working correctly
- [ ] Neo4j graph database with existing class nodes
- [ ] GraphService supporting relationship metadata

### External Dependencies
- [ ] JavaParser library (already integrated)
- [ ] Neo4j Java Driver (already integrated)
- [ ] Spring Boot framework (already integrated)

## Risks and Mitigation

### Technical Risks
- **Risk**: Performance degradation with large codebases
  - **Mitigation**: Implement incremental parsing and caching strategies

- **Risk**: Memory usage increase from external class placeholders  
  - **Mitigation**: Limit placeholder creation and implement cleanup policies

- **Risk**: Complex class name resolution in edge cases
  - **Mitigation**: Comprehensive testing with various Java patterns and fallback strategies

### Business Risks
- **Risk**: Breaking changes to existing API consumers
  - **Mitigation**: Maintain backward compatibility and provide migration documentation

## Future Enhancements

### Advanced Analysis
- [ ] Dependency strength scoring based on usage frequency
- [ ] Temporal dependency analysis (track changes over time)
- [ ] Code smell detection based on dependency patterns
- [ ] Automated refactoring suggestions for improving coupling

### Integration Opportunities
- [ ] IDE plugin for real-time dependency visualization
- [ ] CI/CD integration for dependency change alerts
- [ ] Code review automation with dependency impact analysis
- [ ] Architecture compliance checking against dependency rules

---

This story will transform the Java Graph RAG system from a basic structural analyzer into a comprehensive dependency analysis platform, enabling deep architectural insights and supporting advanced code analysis workflows.
