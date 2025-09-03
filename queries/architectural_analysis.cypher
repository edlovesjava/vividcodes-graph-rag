// Architectural Analysis Queries
// Queries to analyze software architecture patterns and violations

// 1. Detect layered architecture violations (controllers calling repositories directly)
MATCH (controller:Class)-[:USES]->(repo:Class)
WHERE controller.package_name CONTAINS "controller"
  AND repo.package_name CONTAINS "repository"
  AND controller.is_external = false
  AND repo.is_external = false
RETURN controller.name as controllerClass,
       controller.package_name as controllerPackage,
       repo.name as repositoryClass,
       repo.package_name as repositoryPackage
ORDER BY controllerPackage, controllerClass;

// 2. Find proper layered architecture flow (Controller -> Service -> Repository)
MATCH path = (controller:Class)-[:USES*2..3]->(repo:Class)
WHERE controller.package_name CONTAINS "controller"
  AND repo.package_name CONTAINS "repository"
  AND controller.is_external = false
  AND repo.is_external = false
  AND ALL(node IN nodes(path)[1..-1] WHERE 
    node.package_name CONTAINS "service" OR 
    node.package_name CONTAINS "repository")
WITH path, length(path) as pathLength
RETURN [n IN nodes(path) | n.name] as architecturalPath,
       [n IN nodes(path) | n.package_name] as packagePath,
       pathLength
ORDER BY pathLength, architecturalPath[0];

// 3. Identify God Classes (classes with too many dependencies)
MATCH (god:Class)-[:USES]->(dependency:Class)
WHERE god.is_external = false
WITH god, count(DISTINCT dependency) as dependencyCount
WHERE dependencyCount > 10
RETURN god.name as godClass,
       god.package_name as packageName,
       dependencyCount,
       god.method_count as methodCount,
       god.field_count as fieldCount
ORDER BY dependencyCount DESC;

// 4. Find data classes (classes with only fields, no methods)
MATCH (c:Class)
WHERE c.is_external = false
  AND (c.method_count = 0 OR c.method_count IS NULL)
  AND (c.field_count > 0)
RETURN c.name as dataClass,
       c.package_name as packageName,
       c.field_count as fieldCount
ORDER BY c.field_count DESC, c.package_name, c.name;

// 5. Analyze package cohesion (intra-package vs inter-package dependencies)
MATCH (source:Class)-[:USES]->(target:Class)
WHERE source.is_external = false AND target.is_external = false
WITH source.package_name as pkg,
     CASE WHEN source.package_name = target.package_name 
          THEN "internal" 
          ELSE "external" 
     END as dependencyType,
     count(*) as count
RETURN pkg as packageName,
       dependencyType,
       count
ORDER BY pkg, dependencyType;

// 6. Find utility classes (classes that are used by many others but use few)
MATCH (util:Class)
WHERE util.is_external = false
OPTIONAL MATCH (util)-[:USES]->(outgoing:Class)
WHERE outgoing.is_external = false
OPTIONAL MATCH (incoming:Class)-[:USES]->(util)
WHERE incoming.is_external = false
WITH util,
     count(DISTINCT outgoing) as outgoingCount,
     count(DISTINCT incoming) as incomingCount
WHERE incomingCount > 5 AND outgoingCount < 3
RETURN util.name as utilityClass,
       util.package_name as packageName,
       incomingCount,
       outgoingCount,
       round(toFloat(incomingCount) / (outgoingCount + 1) * 100) / 100 as utilityRatio
ORDER BY utilityRatio DESC, incomingCount DESC;

// 7. Detect feature envy (classes that use other packages more than their own)
MATCH (c:Class)-[:USES]->(target:Class)
WHERE c.is_external = false AND target.is_external = false
WITH c,
     c.package_name as ownPackage,
     target.package_name as targetPackage,
     count(*) as usageCount
WITH c, ownPackage,
     sum(CASE WHEN targetPackage = ownPackage THEN usageCount ELSE 0 END) as ownPackageUsage,
     sum(CASE WHEN targetPackage <> ownPackage THEN usageCount ELSE 0 END) as otherPackageUsage,
     sum(usageCount) as totalUsage
WHERE otherPackageUsage > ownPackageUsage AND totalUsage > 5
RETURN c.name as envyClass,
       c.package_name as packageName,
       ownPackageUsage,
       otherPackageUsage,
       totalUsage,
       round(toFloat(otherPackageUsage) / totalUsage * 100) as externalUsagePercent
ORDER BY externalUsagePercent DESC, otherPackageUsage DESC;

// 8. Find singleton patterns (classes with getInstance methods)
MATCH (c:Class)-[:CONTAINS]->(m:Method)
WHERE c.is_external = false
  AND (m.name CONTAINS "getInstance" OR m.name CONTAINS "instance")
  AND m.visibility = "public"
  AND m.is_static = true
RETURN c.name as singletonCandidate,
       c.package_name as packageName,
       collect(m.name) as instanceMethods
ORDER BY c.package_name, c.name;
