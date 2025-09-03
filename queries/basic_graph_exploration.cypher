// Basic Graph Exploration Queries
// These queries help you understand the structure and content of your graph database

// 1. Get overview of all node types and their counts
MATCH (n)
RETURN labels(n) as nodeType, count(n) as count
ORDER BY count DESC;

// 2. Get overview of all relationship types and their counts
MATCH ()-[r]->()
RETURN type(r) as relationshipType, count(r) as count
ORDER BY count DESC;

// 3. Find all repositories in the graph
MATCH (r:Repository)
RETURN r.name as repositoryName, 
       r.path as repositoryPath,
       r.created_at as createdAt
ORDER BY r.name;

// 4. Find all sub-projects and their repositories
MATCH (repo:Repository)-[:CONTAINS]->(sub:SubProject)
RETURN repo.name as repositoryName,
       sub.name as subProjectName,
       sub.type as projectType,
       sub.version as version
ORDER BY repo.name, sub.name;

// 5. Get package hierarchy for a repository
MATCH (repo:Repository)-[:CONTAINS]->(sub:SubProject)-[:CONTAINS]->(pkg:Package)
RETURN repo.name as repository,
       sub.name as subProject,
       pkg.name as packageName,
       pkg.class_count as classCount
ORDER BY repo.name, sub.name, pkg.name;

// 6. Find the largest classes by method count
MATCH (c:Class)
WHERE c.method_count IS NOT NULL
RETURN c.name as className,
       c.package_name as packageName,
       c.method_count as methodCount,
       c.field_count as fieldCount
ORDER BY c.method_count DESC
LIMIT 10;

// 7. Find classes with no dependencies (leaf nodes)
MATCH (c:Class)
WHERE NOT (c)-[:USES]->()
  AND c.is_external = false
RETURN c.name as className,
       c.package_name as packageName,
       c.visibility as visibility
ORDER BY c.package_name, c.name;

// 8. Find classes that are heavily used (most incoming dependencies)
MATCH (c:Class)<-[:USES]-(dependent:Class)
WHERE c.is_external = false
WITH c, count(dependent) as dependentCount
RETURN c.name as className,
       c.package_name as packageName,
       dependentCount
ORDER BY dependentCount DESC
LIMIT 10;
