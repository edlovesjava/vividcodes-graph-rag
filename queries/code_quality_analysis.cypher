// Code Quality Analysis Queries
// Queries to identify code quality issues and improvement opportunities

// 1. Find classes with potential naming issues
MATCH (c:Class)
WHERE c.is_external = false
  AND (c.name =~ ".*Manager.*" OR 
       c.name =~ ".*Helper.*" OR 
       c.name =~ ".*Util.*" OR
       c.name =~ ".*Handler.*")
RETURN c.name as className,
       c.package_name as packageName,
       CASE 
         WHEN c.name =~ ".*Manager.*" THEN "Manager anti-pattern"
         WHEN c.name =~ ".*Helper.*" THEN "Helper anti-pattern"
         WHEN c.name =~ ".*Util.*" THEN "Utility class"
         WHEN c.name =~ ".*Handler.*" THEN "Handler pattern"
       END as pattern
ORDER BY pattern, c.package_name, c.name;

// 2. Identify classes that might violate Single Responsibility Principle
MATCH (c:Class)-[:USES]->(target:Class)
WHERE c.is_external = false
WITH c, collect(DISTINCT target.package_name) as usedPackages
WHERE size(usedPackages) > 5
RETURN c.name as className,
       c.package_name as packageName,
       size(usedPackages) as packageDependencyCount,
       usedPackages
ORDER BY packageDependencyCount DESC;

// 3. Find classes with high complexity (many methods and fields)
MATCH (c:Class)
WHERE c.is_external = false
  AND c.method_count IS NOT NULL
  AND c.field_count IS NOT NULL
WITH c, (c.method_count + c.field_count) as complexity
WHERE complexity > 20
RETURN c.name as className,
       c.package_name as packageName,
       c.method_count as methodCount,
       c.field_count as fieldCount,
       complexity
ORDER BY complexity DESC;

// 4. Detect potential interface segregation violations
MATCH (c:Class)-[r:USES]->(interface:Class)
WHERE r.type = "implements"
  AND c.is_external = false
  AND interface.is_external = false
WITH interface, count(c) as implementationCount
WHERE implementationCount > 1
MATCH (interface)-[:CONTAINS]->(m:Method)
RETURN interface.name as interfaceName,
       interface.package_name as packageName,
       implementationCount,
       count(m) as methodCount
ORDER BY methodCount DESC, implementationCount DESC;

// 5. Find classes that might need refactoring (high fan-out)
MATCH (c:Class)-[:USES]->(target:Class)
WHERE c.is_external = false
WITH c, count(DISTINCT target) as fanOut
WHERE fanOut > 15
RETURN c.name as className,
       c.package_name as packageName,
       fanOut,
       c.method_count as methodCount
ORDER BY fanOut DESC;

// 6. Identify dead code (classes not used by others)
MATCH (c:Class)
WHERE c.is_external = false
  AND NOT (c)<-[:USES]-(:Class)
  AND NOT c.name IN ["Application", "Main", "Config"]
  AND NOT c.package_name CONTAINS "controller"
RETURN c.name as unusedClass,
       c.package_name as packageName,
       c.visibility as visibility
ORDER BY c.package_name, c.name;

// 7. Find classes with inconsistent naming conventions
MATCH (c:Class)
WHERE c.is_external = false
  AND NOT c.name =~ "[A-Z][a-zA-Z0-9]*"
RETURN c.name as nonConventionalName,
       c.package_name as packageName
ORDER BY c.package_name, c.name;

// 8. Analyze method visibility patterns
MATCH (c:Class)-[:CONTAINS]->(m:Method)
WHERE c.is_external = false
WITH c, m.visibility as visibility, count(*) as methodCount
RETURN c.name as className,
       c.package_name as packageName,
       collect({visibility: visibility, count: methodCount}) as visibilityDistribution
ORDER BY c.package_name, c.name;

// 9. Find potential Law of Demeter violations
MATCH (c:Class)-[r1:USES]->(intermediate:Class)-[r2:USES]->(final:Class)
WHERE c.is_external = false
  AND intermediate.is_external = false
  AND final.is_external = false
  AND r1.type = "method_call"
  AND r2.type = "field_access"
  AND c.package_name <> intermediate.package_name
  AND intermediate.package_name <> final.package_name
RETURN c.name as violatingClass,
       c.package_name as violatingPackage,
       intermediate.name as intermediateClass,
       final.name as finalClass
ORDER BY c.package_name, c.name
LIMIT 20;

// 10. Find classes with potential god object characteristics
MATCH (c:Class)
WHERE c.is_external = false
OPTIONAL MATCH (c)-[:USES]->(dep:Class)
WHERE dep.is_external = false
OPTIONAL MATCH (user:Class)-[:USES]->(c)
WHERE user.is_external = false
WITH c,
     count(DISTINCT dep) as dependencies,
     count(DISTINCT user) as dependents,
     coalesce(c.method_count, 0) as methods,
     coalesce(c.field_count, 0) as fields
WHERE dependencies > 10 OR dependents > 10 OR methods > 20 OR fields > 15
RETURN c.name as className,
       c.package_name as packageName,
       dependencies,
       dependents,
       methods,
       fields,
       (dependencies + dependents + methods + fields) as complexityScore
ORDER BY complexityScore DESC;
