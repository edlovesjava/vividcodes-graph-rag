# Dependency Analysis Guide

## Overview

The Java Graph RAG system provides comprehensive dependency analysis capabilities through enhanced `USES` relationships. This guide explains how to leverage these features for architectural insights, impact analysis, and code quality assessment.

## USES Relationship Types

The system captures five distinct types of class dependencies:

### 1. Import Dependencies (`type: "import"`)
- **Description**: Classes imported via `import` statements
- **Example**: `import java.util.List;` creates `CurrentClass -[:USES {type: "import"}]-> List`
- **Use Cases**: Understanding external library usage, tracking framework dependencies

### 2. Object Instantiation (`type: "instantiation"`)
- **Description**: Classes instantiated with `new ClassName()`
- **Example**: `new ArrayList<>()` creates `CurrentClass -[:USES {type: "instantiation"}]-> ArrayList`
- **Use Cases**: Identifying object creation patterns, factory usage analysis

### 3. Parameter Type Dependencies (`type: "parameter_type"`)
- **Description**: Classes used as method parameters
- **Example**: `public void process(UserRequest request)` creates `CurrentClass -[:USES {type: "parameter_type"}]-> UserRequest`
- **Use Cases**: API contract analysis, method signature dependencies

### 4. Return Type Dependencies (`type: "return_type"`)
- **Description**: Classes used as method return types
- **Example**: `public UserResponse getUser()` creates `CurrentClass -[:USES {type: "return_type"}]-> UserResponse`
- **Use Cases**: Output contract analysis, response type tracking

### 5. Field Type Dependencies (`type: "field_type"`)
- **Description**: Classes used as field types
- **Example**: `private Logger logger;` creates `CurrentClass -[:USES {type: "field_type"}]-> Logger`
- **Use Cases**: Composition analysis, dependency injection patterns

## Relationship Metadata

Each `USES` relationship includes rich metadata:

| Property | Description | Example |
|----------|-------------|---------|
| `type` | Dependency type | `"import"`, `"instantiation"`, etc. |
| `context` | Usage context | `"method: saveUser"`, `"field: logger"` |
| `fullyQualifiedName` | Full class name | `"java.util.List"` |
| `isExternal` | External dependency flag | `true` for framework classes |

## Real-World Usage Examples

### Architecture Analysis

#### 1. Identify Highly Coupled Classes

```cypher
// Find classes with excessive dependencies (potential code smell)
MATCH (source:Class)-[r:USES]->(target:Class)
RETURN source.name as className,
       source.packageName as package,
       count(r) as dependencyCount,
       collect(DISTINCT r.type) as dependencyTypes
ORDER BY dependencyCount DESC
LIMIT 10
```

**Interpretation**: Classes with >50 dependencies may need refactoring.

#### 2. Analyze Layer Violations

```cypher
// Detect architectural layer violations (e.g., Controller directly using Repository)
MATCH (controller:Class)-[r:USES]->(repository:Class)
WHERE controller.name CONTAINS "Controller" 
  AND repository.name CONTAINS "Repository"
  AND r.type IN ["instantiation", "parameter_type", "return_type"]
RETURN controller.name as violatingController,
       repository.name as directRepository,
       r.type as violationType,
       r.context as context
ORDER BY violatingController
```

**Interpretation**: Controllers should typically use Services, not Repositories directly.

### Impact Analysis

#### 3. Change Impact Assessment

```cypher
// Find all classes affected by changes to a specific class
MATCH (dependent:Class)-[r:USES]->(target:Class {name: $targetClass})
RETURN dependent.name as affectedClass,
       dependent.packageName as affectedPackage,
       r.type as dependencyType,
       r.context as usageContext,
       CASE r.type
         WHEN "import" THEN "Low Impact"
         WHEN "parameter_type" THEN "Medium Impact"
         WHEN "return_type" THEN "Medium Impact"
         WHEN "instantiation" THEN "High Impact"
         WHEN "field_type" THEN "High Impact"
       END as impactLevel
ORDER BY impactLevel DESC, affectedClass
```

**Usage**: Replace `$targetClass` with the class you're planning to modify.

#### 4. Dependency Hotspots

```cypher
// Find classes that are dependencies for many other classes
MATCH (source:Class)-[r:USES]->(target:Class)
RETURN target.name as dependencyClass,
       target.packageName as package,
       count(DISTINCT source) as dependentCount,
       collect(DISTINCT r.type) as usageTypes
ORDER BY dependentCount DESC
LIMIT 15
```

**Interpretation**: High-dependency classes require careful change management.

### Code Quality Analysis

#### 5. External Dependency Analysis

```cypher
// Analyze external library usage patterns
MATCH (source:Class)-[r:USES]->(target:Class)
WHERE target.name IN ["String", "List", "Map", "Optional", "LocalDateTime", "BigDecimal"]
   OR target.name CONTAINS "Spring"
   OR target.name CONTAINS "Jackson"
RETURN target.name as externalClass,
       count(DISTINCT source) as usageCount,
       collect(DISTINCT r.type) as usageTypes,
       collect(DISTINCT source.packageName) as usingPackages
ORDER BY usageCount DESC
```

**Use Cases**: License compliance, upgrade impact assessment, vendor lock-in analysis.

#### 6. Package Coupling Metrics

```cypher
// Calculate package coupling strength
MATCH (source:Class)-[r:USES]->(target:Class)
WHERE source.packageName <> target.packageName
WITH source.packageName as sourcePackage,
     target.packageName as targetPackage,
     count(r) as couplingStrength
RETURN sourcePackage,
       targetPackage,
       couplingStrength,
       CASE 
         WHEN couplingStrength > 20 THEN "High Coupling"
         WHEN couplingStrength > 10 THEN "Medium Coupling"
         ELSE "Low Coupling"
       END as couplingLevel
ORDER BY couplingStrength DESC
```

**Interpretation**: High coupling between packages may indicate architectural issues.

### Refactoring Support

#### 7. Dead Code Detection

```cypher
// Find classes with no incoming dependencies (potential dead code)
MATCH (c:Class)
WHERE NOT EXISTS {
  MATCH ()-[:USES]->(c)
}
AND NOT c.name CONTAINS "Main"
AND NOT c.name CONTAINS "Application"
AND NOT c.name CONTAINS "Test"
RETURN c.name as potentialDeadClass,
       c.packageName as package,
       c.filePath as location
ORDER BY c.packageName, c.name
```

**Note**: Verify results manually as some classes may be used via reflection or configuration.

#### 8. Circular Dependency Detection

```cypher
// Find circular dependencies between classes
MATCH path = (c1:Class)-[:USES*2..5]->(c1)
WHERE ALL(r in relationships(path) WHERE r.type IN ["instantiation", "field_type"])
UNWIND nodes(path) as n
RETURN DISTINCT [node in nodes(path) | node.name] as circularPath,
       length(path) as pathLength
ORDER BY pathLength, circularPath[0]
```

**Interpretation**: Circular dependencies can cause compilation issues and tight coupling.

## Performance Optimization Queries

#### 9. Query Performance Tips

```cypher
// Use indexes for better performance
CREATE INDEX class_name_index FOR (c:Class) ON (c.name);
CREATE INDEX package_name_index FOR (c:Class) ON (c.packageName);
CREATE INDEX uses_type_index FOR ()-[r:USES]-() ON (r.type);

// Efficient dependency counting
MATCH (c:Class)
CALL {
  WITH c
  MATCH (c)-[r:USES]->()
  RETURN count(r) as outgoingDeps
}
CALL {
  WITH c
  MATCH ()-[r:USES]->(c)
  RETURN count(r) as incomingDeps
}
RETURN c.name as className,
       outgoingDeps,
       incomingDeps,
       (outgoingDeps + incomingDeps) as totalDeps
ORDER BY totalDeps DESC
LIMIT 20
```

## Integration with Development Workflow

### 1. Pre-commit Analysis

```bash
# Check for new circular dependencies before commit
curl -X POST http://localhost:8080/api/v1/cypher \
  -H "Content-Type: application/json" \
  -d '{
    "query": "MATCH path = (c1:Class)-[:USES*2..3]->(c1) WHERE ALL(r in relationships(path) WHERE r.type IN [\"instantiation\", \"field_type\"]) RETURN count(path) as circularCount"
  }'
```

### 2. Code Review Insights

```bash
# Analyze impact of changes to specific class
curl -X POST http://localhost:8080/api/v1/cypher \
  -H "Content-Type: application/json" \
  -d '{
    "query": "MATCH (dependent:Class)-[r:USES]->(target:Class {name: $className}) RETURN count(dependent) as impactedClasses",
    "parameters": {
      "className": "YourModifiedClass"
    }
  }'
```

### 3. Architecture Compliance

```bash
# Check for layer violations
curl -X POST http://localhost:8080/api/v1/cypher \
  -H "Content-Type: application/json" \
  -d '{
    "query": "MATCH (controller:Class)-[r:USES]->(repo:Class) WHERE controller.name CONTAINS \"Controller\" AND repo.name CONTAINS \"Repository\" RETURN count(r) as violations"
  }'
```

## Best Practices

### 1. Regular Dependency Audits
- Run dependency analysis weekly
- Monitor coupling metrics trends
- Track external dependency growth

### 2. Architectural Governance
- Set coupling thresholds (e.g., max 30 dependencies per class)
- Enforce layer separation rules
- Monitor circular dependency introduction

### 3. Refactoring Prioritization
- Focus on high-coupling classes first
- Address circular dependencies immediately
- Consider dead code removal during major releases

### 4. Documentation Integration
- Include dependency graphs in architecture documentation
- Document high-impact classes and their usage patterns
- Maintain external dependency inventory

## Troubleshooting

### Common Issues

1. **Missing Dependencies**: Ensure all source files are included in ingestion
2. **External Class Detection**: Some framework classes may not be marked as external
3. **Performance**: Use LIMIT clauses for large codebases
4. **False Positives**: Verify circular dependency results manually

### Query Optimization

- Use specific relationship types in WHERE clauses
- Add LIMIT clauses for exploratory queries
- Create indexes on frequently queried properties
- Use EXPLAIN to analyze query performance

## Advanced Use Cases

### 1. Microservice Boundary Analysis

```cypher
// Identify potential microservice boundaries based on coupling
MATCH (c1:Class)-[r:USES]->(c2:Class)
WHERE c1.packageName <> c2.packageName
WITH c1.packageName as pkg1, c2.packageName as pkg2, count(r) as coupling
WHERE coupling < 5  // Low coupling threshold
RETURN pkg1, pkg2, coupling
ORDER BY coupling
```

### 2. Technology Migration Impact

```cypher
// Assess impact of replacing a specific framework
MATCH (source:Class)-[r:USES]->(target:Class)
WHERE target.name CONTAINS "Spring" OR target.fullyQualifiedName CONTAINS "springframework"
RETURN source.packageName as affectedPackage,
       count(DISTINCT source) as affectedClasses,
       collect(DISTINCT target.name) as springDependencies
ORDER BY affectedClasses DESC
```

This comprehensive dependency analysis capability enables data-driven architectural decisions, proactive code quality management, and efficient refactoring strategies.
