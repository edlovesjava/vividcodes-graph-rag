// Dependency Analysis Queries
// Advanced queries for analyzing class dependencies and relationships

// 1. Find circular dependencies between classes
MATCH path = (c1:Class)-[:USES*2..10]->(c1)
WHERE c1.is_external = false
  AND ALL(r IN relationships(path) WHERE r.type IN ["import", "instantiation", "field_type"])
RETURN [n IN nodes(path) | n.name] as circularPath,
       [n IN nodes(path) | n.package_name] as packagePath,
       length(path) as pathLength
ORDER BY pathLength
LIMIT 10;

// 2. Find classes with high coupling (many outgoing dependencies)
MATCH (c:Class)-[r:USES]->(target:Class)
WHERE c.is_external = false
WITH c, count(DISTINCT target) as dependencyCount,
     collect(DISTINCT r.type) as relationshipTypes
RETURN c.name as className,
       c.package_name as packageName,
       dependencyCount,
       relationshipTypes
ORDER BY dependencyCount DESC
LIMIT 15;

// 3. Analyze dependency types for a specific class
MATCH (source:Class {name: "ProductionService"})-[r:USES]->(target:Class)
WHERE target.is_external = false
RETURN r.type as dependencyType,
       count(*) as count,
       collect(DISTINCT target.name) as targetClasses
ORDER BY count DESC;

// 4. Find classes that depend on external libraries
MATCH (internal:Class)-[r:USES]->(external:Class)
WHERE internal.is_external = false 
  AND external.is_external = true
WITH internal, external.package_name as externalPackage, count(*) as usageCount
RETURN internal.name as internalClass,
       internal.package_name as internalPackage,
       externalPackage,
       usageCount
ORDER BY usageCount DESC, internalPackage, internalClass;

// 5. Package-level dependency analysis
MATCH (source:Class)-[r:USES]->(target:Class)
WHERE source.is_external = false 
  AND target.is_external = false
  AND source.package_name <> target.package_name
WITH source.package_name as sourcePackage, 
     target.package_name as targetPackage, 
     count(*) as dependencyCount
RETURN sourcePackage,
       targetPackage,
       dependencyCount
ORDER BY dependencyCount DESC;

// 6. Find unstable classes (high efferent coupling, low afferent coupling)
MATCH (c:Class)
WHERE c.is_external = false
OPTIONAL MATCH (c)-[:USES]->(outgoing:Class)
WHERE outgoing.is_external = false
OPTIONAL MATCH (incoming:Class)-[:USES]->(c)
WHERE incoming.is_external = false
WITH c, 
     count(DISTINCT outgoing) as efferentCoupling,
     count(DISTINCT incoming) as afferentCoupling
WHERE efferentCoupling > 0 OR afferentCoupling > 0
WITH c, efferentCoupling, afferentCoupling,
     CASE WHEN (efferentCoupling + afferentCoupling) = 0 THEN 0
          ELSE toFloat(efferentCoupling) / (efferentCoupling + afferentCoupling)
     END as instability
RETURN c.name as className,
       c.package_name as packageName,
       efferentCoupling,
       afferentCoupling,
       round(instability * 100) / 100 as instabilityScore
ORDER BY instabilityScore DESC, efferentCoupling DESC
LIMIT 20;

// 7. Find interface implementations and extensions
MATCH (impl:Class)-[r:USES]->(target:Class)
WHERE r.type IN ["extends", "implements"]
  AND impl.is_external = false
  AND target.is_external = false
RETURN impl.name as implementingClass,
       impl.package_name as implementingPackage,
       target.name as targetClass,
       target.package_name as targetPackage,
       r.type as relationshipType
ORDER BY target.name, impl.name;

// 8. Analyze method call patterns
MATCH (caller:Class)-[r:USES]->(callee:Class)
WHERE r.type = "method_call"
  AND caller.is_external = false
  AND callee.is_external = false
WITH caller.package_name as callerPackage,
     callee.package_name as calleePackage,
     count(*) as callCount
RETURN callerPackage,
       calleePackage,
       callCount,
       CASE WHEN callerPackage = calleePackage THEN "intra-package" ELSE "cross-package" END as callType
ORDER BY callCount DESC;
