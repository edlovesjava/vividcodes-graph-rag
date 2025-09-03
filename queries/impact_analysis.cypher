// Impact Analysis Queries
// Queries to analyze the impact of changes to specific classes or packages

// 1. Find all classes that would be affected by changing a specific class
// Replace 'TargetClassName' with the actual class name you want to analyze
MATCH path = (target:Class {name: "ProductionService"})<-[:USES*1..5]-(affected:Class)
WHERE target.is_external = false
  AND affected.is_external = false
  AND ALL(r IN relationships(path) WHERE r.type IN ["import", "instantiation", "method_call"])
RETURN target.name as targetClass,
       affected.name as affectedClass,
       affected.package_name as affectedPackage,
       length(path) as impactDepth,
       [r IN relationships(path) | r.type] as dependencyTypes
ORDER BY impactDepth, affectedPackage, affectedClass;

// 2. Package impact analysis - find all packages affected by a package change
MATCH (source:Class)-[:USES]->(target:Class)
WHERE source.package_name = "com.vividseats.catalog.api.v1.service"
  AND source.is_external = false
  AND target.is_external = false
  AND source.package_name <> target.package_name
WITH target.package_name as affectedPackage, count(DISTINCT source) as affectingClasses
RETURN affectedPackage,
       affectingClasses
ORDER BY affectingClasses DESC;

// 3. Find blast radius for interface changes
MATCH (interface:Class)<-[r:USES]-(implementer:Class)
WHERE r.type = "implements"
  AND interface.name = "ServiceInterface"  // Replace with actual interface name
  AND implementer.is_external = false
OPTIONAL MATCH (implementer)<-[:USES*1..3]-(dependent:Class)
WHERE dependent.is_external = false
RETURN interface.name as interfaceName,
       implementer.name as implementingClass,
       collect(DISTINCT dependent.name) as dependentClasses,
       size(collect(DISTINCT dependent)) as blastRadius
ORDER BY blastRadius DESC;

// 4. Analyze ripple effects of method signature changes
MATCH (c:Class)-[:CONTAINS]->(m:Method)
WHERE c.name = "UserService"  // Replace with target class
  AND m.name = "getUserById"   // Replace with target method
MATCH (caller:Class)-[r:USES]->(c)
WHERE r.type = "method_call"
  AND r.context CONTAINS m.name
  AND caller.is_external = false
RETURN c.name as targetClass,
       m.name as targetMethod,
       caller.name as callingClass,
       caller.package_name as callingPackage
ORDER BY callingPackage, callingClass;

// 5. Find critical classes (classes that many others depend on)
MATCH (critical:Class)<-[:USES]-(dependent:Class)
WHERE critical.is_external = false
  AND dependent.is_external = false
WITH critical, count(DISTINCT dependent) as dependentCount
WHERE dependentCount > 5
RETURN critical.name as criticalClass,
       critical.package_name as packageName,
       dependentCount
ORDER BY dependentCount DESC;

// 6. Analyze transitive closure for a specific class change
MATCH path = (start:Class {name: "DatabaseConfig"})<-[:USES*1..4]-(end:Class)
WHERE start.is_external = false
  AND end.is_external = false
WITH end, min(length(path)) as minDistance, max(length(path)) as maxDistance
RETURN end.name as affectedClass,
       end.package_name as affectedPackage,
       minDistance,
       maxDistance
ORDER BY minDistance, affectedPackage, affectedClass;

// 7. Find classes that would break if external dependencies change
MATCH (internal:Class)-[:USES]->(external:Class)
WHERE internal.is_external = false
  AND external.is_external = true
  AND external.package_name CONTAINS "springframework"  // Replace with specific external package
WITH internal, count(*) as externalDependencies
WHERE externalDependencies > 3
RETURN internal.name as vulnerableClass,
       internal.package_name as packageName,
       externalDependencies
ORDER BY externalDependencies DESC;

// 8. Identify bottleneck classes (high betweenness centrality)
MATCH (c:Class)
WHERE c.is_external = false
OPTIONAL MATCH (c)-[:USES]->(out:Class)
WHERE out.is_external = false
OPTIONAL MATCH (in:Class)-[:USES]->(c)
WHERE in.is_external = false
WITH c,
     count(DISTINCT out) as outDegree,
     count(DISTINCT in) as inDegree
WHERE outDegree > 2 AND inDegree > 2
RETURN c.name as bottleneckClass,
       c.package_name as packageName,
       inDegree,
       outDegree,
       (inDegree + outDegree) as totalConnections
ORDER BY totalConnections DESC;

// 9. Find classes in the critical path between two specific classes
MATCH path = shortestPath((source:Class {name: "UserController"})-[:USES*1..10]->(target:Class {name: "UserRepository"}))
WHERE source.is_external = false
  AND target.is_external = false
RETURN [n IN nodes(path) | n.name] as criticalPath,
       [n IN nodes(path) | n.package_name] as packagePath,
       length(path) as pathLength;

// 10. Analyze package stability and impact
MATCH (c:Class)
WHERE c.is_external = false
  AND c.package_name = "com.vividseats.catalog.api.v1.model"  // Replace with target package
OPTIONAL MATCH (c)-[:USES]->(outgoing:Class)
WHERE outgoing.is_external = false
  AND outgoing.package_name <> c.package_name
OPTIONAL MATCH (incoming:Class)-[:USES]->(c)
WHERE incoming.is_external = false
  AND incoming.package_name <> c.package_name
WITH c.package_name as packageName,
     count(DISTINCT outgoing) as efferentCoupling,
     count(DISTINCT incoming) as afferentCoupling
RETURN packageName,
       efferentCoupling,
       afferentCoupling,
       CASE WHEN (efferentCoupling + afferentCoupling) = 0 THEN 0
            ELSE round(toFloat(efferentCoupling) / (efferentCoupling + afferentCoupling) * 100) / 100
       END as instability,
       CASE WHEN afferentCoupling = 0 THEN 0
            ELSE round(toFloat(afferentCoupling) / (efferentCoupling + afferentCoupling) * 100) / 100
       END as stability;
