# Cypher Query Collection

This directory contains a comprehensive collection of Cypher queries for analyzing Java codebases stored in Neo4j graph databases. These queries are designed to work with the Java Graph RAG system and provide insights into code structure, dependencies, architecture, and quality.

## Query Categories

### 📊 Basic Graph Exploration (`basic_graph_exploration.cypher`)
- **Purpose**: Understand the basic structure and content of your graph database
- **Use Cases**: Initial exploration, data validation, overview statistics
- **Key Queries**:
  - Node and relationship type counts
  - Repository and sub-project listings
  - Package hierarchies
  - Largest classes by method count
  - Leaf nodes (classes with no dependencies)

### 🔗 Dependency Analysis (`dependency_analysis.cypher`)
- **Purpose**: Analyze class-to-class and package-to-package dependencies
- **Use Cases**: Understanding coupling, finding circular dependencies, dependency mapping
- **Key Queries**:
  - Circular dependency detection
  - High coupling identification
  - External library usage analysis
  - Package-level dependency mapping
  - Instability metrics (efferent vs afferent coupling)

### 🏗️ Architectural Analysis (`architectural_analysis.cypher`)
- **Purpose**: Evaluate software architecture patterns and detect violations
- **Use Cases**: Architecture compliance, pattern detection, design quality assessment
- **Key Queries**:
  - Layered architecture violation detection
  - God class identification
  - Data class detection
  - Package cohesion analysis
  - Utility class identification
  - Feature envy detection

### 🔍 Code Quality Analysis (`code_quality_analysis.cypher`)
- **Purpose**: Identify code quality issues and improvement opportunities
- **Use Cases**: Code review, refactoring planning, technical debt assessment
- **Key Queries**:
  - Naming convention violations
  - Single Responsibility Principle violations
  - High complexity classes
  - Interface segregation issues
  - Dead code detection
  - Law of Demeter violations

### 📈 Impact Analysis (`impact_analysis.cypher`)
- **Purpose**: Analyze the impact of changes to specific classes or packages
- **Use Cases**: Change impact assessment, risk analysis, refactoring planning
- **Key Queries**:
  - Transitive dependency analysis
  - Package impact assessment
  - Interface change blast radius
  - Critical class identification
  - Bottleneck detection
  - Package stability metrics

### 🎯 Specific Use Cases

#### Internal Class Imports (`internal_class_imports_query.cypher`)
- Shows class-to-class USES relationships where imports exist but excludes external classes
- Useful for understanding internal application dependencies
- Includes variants for cross-package analysis and import counting

#### TagForProduction Dependency Analysis (`tagforproduction_dependency_analysis.cypher`)
- Comprehensive analysis starting from a specific class (`TagForProduction`)
- Shows transitive dependencies up to 3 levels deep
- Includes impact analysis, dependency chains, and multiple path detection
- Example of how to analyze the ripple effects of changes to a specific class

## Usage Instructions

### Prerequisites
1. Java Graph RAG system running with Neo4j database
2. Code repository ingested into the graph database
3. Access to the Cypher query endpoint (typically `http://localhost:8080/api/v1/cypher`)

### Running Queries

#### Via REST API
```bash
curl -X POST http://localhost:8080/api/v1/cypher \
  -H "Content-Type: application/json" \
  -d '{"query": "YOUR_CYPHER_QUERY_HERE"}'
```

#### Via Neo4j Browser
1. Open Neo4j Browser (typically `http://localhost:7474`)
2. Copy and paste queries from the `.cypher` files
3. Execute and analyze results

#### Via Application
Use the Java Graph RAG application's query interface to execute these queries.

### Customizing Queries

Most queries include parameters that you can customize:

- **Class Names**: Replace placeholder class names (e.g., `"ProductionService"`) with actual classes from your codebase
- **Package Names**: Update package name filters to match your application's package structure
- **Thresholds**: Adjust numeric thresholds (e.g., dependency counts, complexity metrics) based on your project's characteristics
- **Depth Limits**: Modify path length limits (`*1..3`, `*1..5`) based on your analysis needs

### Query Optimization Tips

1. **Use Indexes**: Ensure your Neo4j database has appropriate indexes on frequently queried properties
2. **Limit Results**: Add `LIMIT` clauses to large result sets for better performance
3. **Filter Early**: Apply `WHERE` clauses as early as possible in your queries
4. **Profile Queries**: Use `PROFILE` or `EXPLAIN` to understand query execution plans

## Example Workflows

### 1. Initial Codebase Analysis
```cypher
-- Start with basic exploration
-- Run queries from basic_graph_exploration.cypher
-- Understand the size and structure of your codebase
```

### 2. Architecture Assessment
```cypher
-- Use architectural_analysis.cypher queries
-- Check for layered architecture violations
-- Identify god classes and architectural smells
```

### 3. Dependency Review
```cypher
-- Run dependency_analysis.cypher queries
-- Look for circular dependencies
-- Analyze coupling metrics
```

### 4. Change Impact Analysis
```cypher
-- Before making changes, use impact_analysis.cypher
-- Understand the blast radius of your changes
-- Identify critical classes that need careful handling
```

### 5. Code Quality Assessment
```cypher
-- Use code_quality_analysis.cypher for code review
-- Identify refactoring opportunities
-- Find dead code and naming issues
```

## Contributing

When adding new queries to this collection:

1. **Follow Naming Conventions**: Use descriptive names and organize by category
2. **Add Comments**: Include clear comments explaining the purpose and usage
3. **Provide Examples**: Show sample parameters and expected results
4. **Test Thoroughly**: Ensure queries work with different codebase sizes and structures
5. **Document Performance**: Note any performance considerations or optimization tips

## Query Performance Notes

- **Large Codebases**: Some queries may be slow on very large codebases (>10k classes)
- **Memory Usage**: Transitive dependency queries can be memory-intensive
- **Indexing**: Ensure proper indexing on `name`, `package_name`, and `is_external` properties
- **Batching**: Consider breaking large analyses into smaller, focused queries

## Support

For questions about these queries or the Java Graph RAG system:
- Check the main project documentation
- Review the API documentation for query endpoint details
- Examine the graph schema documentation for available properties and relationships
