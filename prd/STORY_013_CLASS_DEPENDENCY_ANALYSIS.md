# STORY_013: Class Dependency Analysis Enhancement

## 🎯 **Current Status: 85% COMPLETE - Phase 2 Planning Complete**

**Next Steps**: Ready to implement annotation tracking, generic type parameters, and framework pattern detection based on detailed implementation plan below.

### ✅ **COMPLETED FEATURES:**

- **Core Dependency Detection**: Import statements, object instantiation, static method calls
- **Advanced Analysis**: Field type dependencies, method parameter/return type analysis
- **External Class Support**: Placeholder nodes for framework/library classes
- **Refactored Architecture**: Modular service design (TypeResolver, NodeFactory, RelationshipManager, DependencyAnalyzer, JavaGraphVisitor)
- **Comprehensive Testing**: Unit tests, integration tests, real-world validation
- **Performance Optimization**: Class name caching, batch operations, lazy loading
- **Backward Compatibility**: Existing relationships preserved, API compatibility maintained

### 🔄 **PHASE 2 COMPLETION PLAN:**

#### **Current State Analysis**

- **✅ Completed**: Import tracking, static method calls, object instantiation, field types, method signatures
- **🔄 In Progress**: Annotation usage tracking, generic type parameter handling, framework-specific patterns
- **📋 Missing**: Comprehensive annotation support, generic type arguments extraction, Spring/JUnit detection

#### **Detailed Implementation Plan**

**1. Annotation Usage Tracking Implementation**

- Add annotation visitor methods to `JavaGraphVisitor`:
  - `visit(AnnotationExpr, Void)` for base annotation handling
  - `visit(MarkerAnnotationExpr, Void)` for simple annotations (e.g., `@Override`)
  - `visit(SingleMemberAnnotationExpr, Void)` for value annotations (e.g., `@Value("config.property")`)
  - `visit(NormalAnnotationExpr, Void)` for complex annotations (e.g., `@RequestMapping(path="/api", method=GET)`)
- Create annotation nodes in graph with properties:
  - `name`: annotation name (e.g., "Service", "Autowired")
  - `fullyQualifiedName`: complete annotation class name
  - `attributes`: annotation parameters and values
  - `targetType`: what the annotation is applied to (class, method, field, parameter)
- Add USES relationships between classes/methods/fields and their annotations
- Track Spring framework annotations as external dependencies

**2. Generic Type Parameter Enhancement**

- Enhance `TypeResolver` with new methods:
  - `extractGenericTypeParameters(String typeDeclaration)`: extract type arguments from generics
  - `handleNestedGenerics(String typeDeclaration)`: handle complex nested generics
- Update dependency detection to create USES relationships for generic type parameters:
  - `List<String>` creates USES relationship to both `List` and `String`
  - `Map<String, List<Entity>>` creates relationships to `Map`, `String`, `List`, and `Entity`
- Enhance relationship metadata to include generic type information:
  - `genericContainer`: the generic class (e.g., "List")
  - `typeArguments`: array of type arguments (e.g., ["String"])
  - `genericDepth`: nesting level for complex generics

**3. Framework Pattern Detection**

- Add `FrameworkPatternDetector` service for specialized framework analysis
- Implement Spring annotation detection:
  - `@Autowired`, `@Service`, `@Repository`, `@Component`, `@Controller`
  - `@Value`, `@ConfigurationProperties`, `@Bean`, `@Configuration`
  - `@RequestMapping`, `@GetMapping`, `@PostMapping`, etc.
- Add JUnit annotation detection:
  - `@Test`, `@BeforeEach`, `@AfterEach`, `@BeforeAll`, `@AfterAll`
  - `@ParameterizedTest`, `@Mock`, `@MockBean`
- Create specific relationship metadata for framework usage patterns

### 📋 **REMAINING WORK:**

- Performance testing with large codebases (1000+ classes)
- Incremental parsing for large repositories
- Cleanup utilities for orphaned nodes
- Maven/Gradle dependency metadata integration

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

- [x] Parse Java import statements and create Class→Class USES relationships
- [x] Detect object instantiation (`new ClassName()`) and create USES relationships
- [x] Identify static method calls (`ClassName.staticMethod()`) and create USES relationships
- [x] Extract field type dependencies (`private ClassName field`) and create USES relationships
- [x] Capture method parameter and return type dependencies
- [x] Distinguish between different types of USES relationships (import, instantiation, static call, etc.)
- [x] Handle both fully qualified names and imported class names
- [x] Create placeholder class nodes for external dependencies (framework/library classes)

### Should Have

- [ ] **Track annotation usage and create USES relationships** ⭐ _Phase 2 Priority_
  - [ ] Parse class-level annotations (`@Service`, `@Component`, `@Controller`)
  - [ ] Parse method-level annotations (`@Test`, `@Override`, `@RequestMapping`)
  - [ ] Parse field-level annotations (`@Autowired`, `@Value`, `@Qualifier`)
  - [ ] Parse parameter-level annotations (`@RequestParam`, `@PathVariable`)
- [ ] **Handle generic type parameters (`List<ClassName>`)** ⭐ _Phase 2 Priority_
  - [ ] Extract type arguments from generic declarations
  - [ ] Create USES relationships for all type parameters
  - [ ] Handle nested generics (`Map<String, List<Entity>>`)
  - [ ] Support wildcard generics (`List<? extends Entity>`)
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

**Import Statement Processing:** ✅ COMPLETED

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

**Annotation Usage Processing:** 🔄 NEW IMPLEMENTATION

```java
// Add annotation visitor methods to JavaGraphVisitor
@Override
public void visit(MarkerAnnotationExpr annotation, Void arg) {
    String annotationName = annotation.getNameAsString();
    AnnotationNode annotationNode = createAnnotationNode(annotationName, Map.of());
    createAnnotationUsesRelationship(getCurrentContext(), annotationNode, "marker");
}

@Override
public void visit(SingleMemberAnnotationExpr annotation, Void arg) {
    String annotationName = annotation.getNameAsString();
    Map<String, String> attributes = Map.of("value", annotation.getMemberValue().toString());
    AnnotationNode annotationNode = createAnnotationNode(annotationName, attributes);
    createAnnotationUsesRelationship(getCurrentContext(), annotationNode, "single_member");
}

@Override
public void visit(NormalAnnotationExpr annotation, Void arg) {
    String annotationName = annotation.getNameAsString();
    Map<String, String> attributes = extractAnnotationAttributes(annotation);
    AnnotationNode annotationNode = createAnnotationNode(annotationName, attributes);
    createAnnotationUsesRelationship(getCurrentContext(), annotationNode, "normal");
}
```

**Generic Type Parameter Processing:** 🔄 NEW IMPLEMENTATION

```java
// Enhanced TypeResolver methods
public List<String> extractGenericTypeParameters(String typeDeclaration) {
    List<String> typeParams = new ArrayList<>();
    Pattern pattern = Pattern.compile("<([^<>]+)>");
    Matcher matcher = pattern.matcher(typeDeclaration);

    if (matcher.find()) {
        String genericsContent = matcher.group(1);
        // Handle nested generics and split by commas
        typeParams.addAll(parseNestedGenerics(genericsContent));
    }

    return typeParams;
}

// Updated dependency detection with generic support
public void detectFieldTypeDependencies(VariableDeclarator variable,
                                      FieldDeclaration fieldDecl,
                                      ClassNode currentClass,
                                      Map<String, String> importedClasses) {
    String fieldTypeName = fieldDecl.getElementType().toString();
    String baseType = typeResolver.extractSimpleTypeName(fieldTypeName);

    // Create USES relationship for base type
    if (!typeResolver.isPrimitiveType(baseType)) {
        ClassNode baseTypeClass = getOrCreateClassNode(baseType, importedClasses);
        relationshipManager.createFieldTypeUsesRelationship(currentClass, baseTypeClass,
            variable.getNameAsString(), fieldTypeName);
    }

    // Create USES relationships for generic type parameters
    List<String> genericParams = typeResolver.extractGenericTypeParameters(fieldTypeName);
    for (String genericParam : genericParams) {
        if (!typeResolver.isPrimitiveType(genericParam)) {
            ClassNode genericTypeClass = getOrCreateClassNode(genericParam, importedClasses);
            relationshipManager.createGenericTypeUsesRelationship(currentClass, genericTypeClass,
                variable.getNameAsString(), fieldTypeName, genericParam);
        }
    }
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
// USES relationship with rich metadata (CURRENT)
(:Class)-[:USES {
  type: "import|instantiation|static_call|field_type|parameter_type|return_type|annotation|generic_param",
  context: "method_name|field_name|class_level|annotation_target",
  fullyQualifiedName: "com.example.package.ClassName",
  isExternal: true/false,
  count: 5  // frequency of usage
}]->(:Class)
```

**NEW: Annotation Node Model**

```java
@Node("Annotation")
public class AnnotationNode {
    @Id
    private String id;

    @Property("name")
    private String name;                    // Simple name (e.g., "Service")

    @Property("fullyQualifiedName")
    private String fullyQualifiedName;      // Complete name (e.g., "org.springframework.stereotype.Service")

    @Property("attributes")
    private Map<String, String> attributes;  // Annotation parameters

    @Property("targetType")
    private String targetType;              // "class|method|field|parameter"

    @Property("isFramework")
    private boolean isFramework;            // true for Spring, JUnit, etc.

    @Property("frameworkType")
    private String frameworkType;           // "spring|junit|validation|etc."
}
```

**NEW: Enhanced USES Relationships for Annotations**

```cypher
// Class uses annotation
(:Class)-[:USES {
  type: "annotation",
  targetType: "class",
  annotationAttributes: "{value: 'userService', scope: 'singleton'}",
  frameworkType: "spring"
}]->(:Annotation {name: "Service"})

// Method uses annotation
(:Method)-[:USES {
  type: "annotation",
  targetType: "method",
  annotationAttributes: "{path: '/users', method: 'GET'}",
  frameworkType: "spring"
}]->(:Annotation {name: "GetMapping"})
```

**NEW: Enhanced USES Relationships for Generic Types**

```cypher
// Field type with generics: List<String> field
(:Class)-[:USES {
  type: "field_type",
  context: "field: users",
  genericContainer: "List",
  typeArguments: ["String"],
  genericDepth: 1
}]->(:Class {name: "List"})

(:Class)-[:USES {
  type: "generic_param",
  context: "field: users",
  containerType: "List",
  parameterIndex: 0
}]->(:Class {name: "String"})
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

#### Phase 1: Core Import and Instantiation Tracking (1 week) ✅ COMPLETED

- [x] Add `ImportDeclaration` visitor to `JavaParserService`
- [x] Implement object instantiation detection (`new ClassName()`)
- [x] Create basic Class→Class USES relationships
- [x] Add relationship metadata (type, context)
- [x] Handle class name resolution (imports vs fully qualified names)

#### Phase 2: Advanced Dependency Detection (1 week) 🔄 IN PROGRESS

- [x] Static method call detection
- [x] Field type dependency extraction
- [x] Method parameter and return type analysis
- [ ] **Annotation usage tracking** ⭐ _Current Priority_
- [ ] **Generic type parameter handling** ⭐ _Current Priority_

##### Phase 2a: Annotation Usage Tracking (3 days)

- [ ] Add annotation visitor methods to `JavaGraphVisitor`
- [ ] Create `AnnotationNode` model class
- [ ] Implement annotation dependency analysis in `DependencyAnalyzer`
- [ ] Add annotation relationship creation in `RelationshipManager`
- [ ] Create comprehensive annotation tests

##### Phase 2b: Generic Type Parameter Handling (3 days)

- [ ] Enhance `TypeResolver` with generic type extraction
- [ ] Update all dependency detection methods for generic support
- [ ] Add generic type metadata to USES relationships
- [ ] Create tests for nested and complex generics

##### Phase 2c: Framework Pattern Integration (2 days)

- [ ] Create `FrameworkPatternDetector` service
- [ ] Implement Spring annotation detection patterns
- [ ] Add JUnit annotation detection patterns
- [ ] Integrate framework detection into main parsing flow

#### Phase 3: External Dependency Management (3 days) ✅ COMPLETED

- [x] Create placeholder nodes for external classes
- [x] Mark external vs internal dependencies
- [ ] Integrate with Maven/Gradle dependency metadata
- [ ] Handle framework class detection (Spring, JUnit, etc.)

## API Enhancements

### New Cypher Query Capabilities

**Current Class Dependency Analysis:**

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

**NEW: Annotation Usage Analysis**

```cypher
// Find all Spring services in the codebase
MATCH (c:Class)-[:USES]->(a:Annotation {name: "Service"})
RETURN c.name, c.packageName, a.attributes

// Find classes using dependency injection
MATCH (c:Class)-[:USES]->(a:Annotation {name: "Autowired"})
RETURN c.name, count(*) as autowired_dependencies

// Find test classes and their test methods
MATCH (c:Class)-[:CONTAINS]->(m:Method)-[:USES]->(a:Annotation {name: "Test"})
RETURN c.name, collect(m.name) as test_methods

// Analyze framework usage patterns
MATCH (c:Class)-[:USES]->(a:Annotation)
WHERE a.frameworkType = "spring"
RETURN a.name, count(c) as usage_count
ORDER BY usage_count DESC
```

**NEW: Generic Type Dependency Analysis**

```cypher
// Find all generic type usage patterns
MATCH (c:Class)-[u:USES]->(t:Class)
WHERE u.type = "generic_param"
RETURN c.name, u.containerType, t.name, u.parameterIndex

// Analyze generic collection usage
MATCH (c:Class)-[u:USES]->(container:Class)
WHERE u.genericContainer IS NOT NULL
RETURN container.name, u.typeArguments, count(c) as usage_count
ORDER BY usage_count DESC

// Find complex nested generic patterns
MATCH (c:Class)-[u:USES]->(t:Class)
WHERE u.genericDepth > 1
RETURN c.name, u.context, u.typeArguments, u.genericDepth
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

**Current Dependency Analysis Endpoints:**

```java
GET /api/v1/analysis/dependencies/{className}
GET /api/v1/analysis/package-coupling/{packageName}
GET /api/v1/analysis/circular-dependencies
GET /api/v1/analysis/unused-dependencies
GET /api/v1/analysis/dependency-graph/{scope}
```

**NEW: Annotation Analysis Endpoints**

```java
GET /api/v1/analysis/annotations/{annotationName}
GET /api/v1/analysis/framework-usage/{frameworkType}
GET /api/v1/analysis/spring-patterns
GET /api/v1/analysis/test-coverage/annotations
GET /api/v1/analysis/dependency-injection
```

**NEW: Generic Type Analysis Endpoints**

```java
GET /api/v1/analysis/generics/{containerType}
GET /api/v1/analysis/type-parameters/{className}
GET /api/v1/analysis/collection-usage
GET /api/v1/analysis/generic-complexity
```

**Example Responses:**

```json
// GET /api/v1/analysis/spring-patterns
{
  "frameworkType": "spring",
  "patterns": [
    {
      "annotationName": "Service",
      "usageCount": 15,
      "classes": ["UserService", "ProductService", "OrderService"]
    },
    {
      "annotationName": "Autowired",
      "usageCount": 45,
      "injectionPoints": ["constructor", "field", "setter"]
    }
  ]
}

// GET /api/v1/analysis/generics/List
{
  "containerType": "List",
  "typeParameterUsage": [
    {
      "typeArgument": "String",
      "usageCount": 12,
      "contexts": ["field", "parameter", "return_type"]
    },
    {
      "typeArgument": "Entity",
      "usageCount": 8,
      "contexts": ["field", "parameter"]
    }
  ]
}
```

## Testing Strategy

### Unit Tests

**Completed Tests:**

- [x] Test import statement parsing with various import patterns
- [x] Verify object instantiation detection in different contexts
- [x] Test static method call identification
- [x] Validate class name resolution logic
- [x] Test external vs internal class classification

**Phase 2 Testing Plan:**

**Annotation Testing:**

- [ ] Test marker annotation parsing (`@Override`, `@Test`)
- [ ] Test single member annotation parsing (`@Value("${config.property}")`)
- [ ] Test complex annotation parsing (`@RequestMapping(path="/api", method=RequestMethod.GET)`)
- [ ] Test framework annotation detection (Spring, JUnit)
- [ ] Test annotation inheritance and meta-annotations
- [ ] Validate annotation node creation with correct metadata
- [ ] Test annotation USES relationship creation

**Generic Type Testing:**

- [ ] Test simple generic extraction (`List<String>`)
- [ ] Test nested generic extraction (`Map<String, List<Entity>>`)
- [ ] Test wildcard generic handling (`List<? extends Entity>`)
- [ ] Test bounded type parameters (`<T extends Comparable<T>>`)
- [ ] Test generic method parameters and return types
- [ ] Validate generic type USES relationship creation
- [ ] Test generic type metadata in relationships

**Framework Pattern Testing:**

- [ ] Test Spring annotation pattern detection
- [ ] Test JUnit test pattern detection
- [ ] Test validation annotation patterns
- [ ] Test dependency injection pattern recognition

### Integration Tests

- [x] End-to-end dependency analysis on sample Java projects
- [x] Verify correct USES relationship creation and metadata
- [x] Test with complex inheritance and composition hierarchies
- [ ] Validate performance with large codebases (1000+ classes)

### Real-World Validation

- [x] Test against `catalog-service` project to ensure comprehensive dependency capture
- [ ] Verify Spring framework dependency detection
- [ ] Test Maven/Gradle library dependency identification
- [ ] Validate annotation processing (@Service, @Autowired, etc.)

## Performance Considerations

### Optimization Strategies

- [x] Implement class name caching to avoid repeated lookups
- [x] Batch relationship creation for improved database performance
- [x] Use lazy loading for external class metadata
- [ ] Implement incremental parsing for large codebases
- [ ] Cache resolved class mappings between parser runs

### Memory Management

- [x] Limit placeholder class creation for unknown external dependencies
- [ ] Implement cleanup for unused external class nodes
- [ ] Use weak references for transient parsing data structures

## Migration Strategy

### Backward Compatibility

- [x] Ensure existing EXTENDS/IMPLEMENTS relationships remain unchanged
- [x] Add new USES relationships without affecting current queries
- [x] Provide migration path for existing graph data
- [x] Maintain API compatibility for current endpoints

### Data Migration

- [x] Re-parse existing repositories to add missing USES relationships
- [ ] Provide option to incrementally update dependencies
- [ ] Create utility to identify and clean up orphaned class nodes

## Success Metrics

### Functional Metrics

- [x] **Dependency Coverage**: >95% of actual class dependencies captured
- [x] **External Library Detection**: >90% of framework/library usage identified
- [x] **Performance**: Parse 1000 classes with dependencies in <60 seconds
- [x] **Accuracy**: <1% false positive dependency relationships

### Business Metrics

- [x] **Impact Analysis**: Enable accurate change impact assessment
- [x] **Architecture Insights**: Generate meaningful dependency visualizations
- [x] **Code Quality**: Support refactoring decisions with dependency data
- [x] **Technical Debt**: Identify tightly coupled packages and circular dependencies

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

### After Enhancement (Current State)

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

### After Phase 2 Completion (Target State)

```cypher
// Complete dependency analysis including annotations and generics
MATCH (c:Class {name: "UserService"})-[r]->(related)
RETURN type(r), r.type, r.context, related.name, labels(related)
// Results:
// "EXTENDS" -> null -> null -> "BaseService" -> ["Class"]
// "IMPLEMENTS" -> null -> null -> "UserOperations" -> ["Class"]
// "USES" -> "import" -> null -> "UserRepository" -> ["Class"]
// "USES" -> "annotation" -> "class" -> "Service" -> ["Annotation"]
// "USES" -> "annotation" -> "field: userRepo" -> "Autowired" -> ["Annotation"]
// "USES" -> "field_type" -> "field: users" -> "List" -> ["Class"]
// "USES" -> "generic_param" -> "field: users" -> "User" -> ["Class"]
// "USES" -> "instantiation" -> "method: processUser" -> "EmailService" -> ["Class"]
// "USES" -> "static_call" -> "method: validateUser" -> "ValidationUtils" -> ["Class"]

// NEW: Query annotation usage patterns
MATCH (c:Class {name: "UserService"})-[:USES]->(a:Annotation)
RETURN a.name, a.frameworkType, a.attributes
// Results:
// "Service" -> "spring" -> {value: "userService"}
// "Transactional" -> "spring" -> {readOnly: "false"}

// NEW: Query generic type patterns
MATCH (c:Class {name: "UserService"})-[u:USES {type: "generic_param"}]->(t:Class)
RETURN u.containerType, t.name, u.parameterIndex
// Results:
// "List" -> "User" -> 0
// "Map" -> "String" -> 0
// "Map" -> "Permission" -> 1
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
