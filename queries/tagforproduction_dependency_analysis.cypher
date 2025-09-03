// Cypher Query: TagForProduction Dependency Analysis
// Find all internal classes that import TagForProduction and transitively related classes up to 3 segments

// 1. Basic transitive dependency query (up to 3 levels)
MATCH path = (start:Class {name: "TagForProduction"})<-[:USES*1..3]-(dependent:Class) 
WHERE dependent.is_external = false 
  AND ALL(r IN relationships(path) WHERE r.type = "import")
RETURN 
  start.name as startClass, 
  start.package_name as startPackage, 
  dependent.name as dependentClass, 
  dependent.package_name as dependentPackage, 
  length(path) as depth,
  [r IN relationships(path) | r.type] as relationshipTypes
ORDER BY depth, dependentPackage, dependentClass;

// 2. Detailed path analysis showing the full dependency chain
MATCH path = (start:Class {name: "TagForProduction"})<-[:USES*1..3]-(dependent:Class) 
WHERE dependent.is_external = false 
  AND ALL(r IN relationships(path) WHERE r.type = "import")
WITH path, start, dependent, length(path) as depth
RETURN 
  start.name as startClass,
  start.package_name as startPackage,
  dependent.name as finalDependentClass,
  dependent.package_name as finalDependentPackage,
  depth,
  [node IN nodes(path) | node.name] as dependencyChain,
  [node IN nodes(path) | node.package_name] as packageChain
ORDER BY depth, finalDependentPackage, finalDependentClass;

// 3. Impact analysis - count dependencies by depth level
MATCH path = (start:Class {name: "TagForProduction"})<-[:USES*1..3]-(dependent:Class) 
WHERE dependent.is_external = false 
  AND ALL(r IN relationships(path) WHERE r.type = "import")
WITH length(path) as depth, dependent
RETURN 
  depth,
  count(DISTINCT dependent) as dependentClassCount,
  collect(DISTINCT dependent.package_name) as affectedPackages
ORDER BY depth;

// 4. Package-level impact analysis
MATCH path = (start:Class {name: "TagForProduction"})<-[:USES*1..3]-(dependent:Class) 
WHERE dependent.is_external = false 
  AND ALL(r IN relationships(path) WHERE r.type = "import")
RETURN 
  dependent.package_name as affectedPackage,
  count(DISTINCT dependent) as classesInPackage,
  collect(DISTINCT dependent.name) as affectedClasses,
  min(length(path)) as minDepth,
  max(length(path)) as maxDepth
ORDER BY classesInPackage DESC, affectedPackage;

// 5. Find all direct importers (depth 1 only)
MATCH (start:Class {name: "TagForProduction"})<-[r:USES]-(importer:Class)
WHERE importer.is_external = false 
  AND r.type = "import"
RETURN 
  start.name as startClass,
  start.package_name as startPackage,
  importer.name as importerClass,
  importer.package_name as importerPackage,
  r.context as importContext,
  r.fullyQualifiedName as fullyQualifiedName
ORDER BY importerPackage, importerClass;

// 6. Reverse dependency tree (showing intermediate nodes)
MATCH path = (start:Class {name: "TagForProduction"})<-[:USES*1..3]-(dependent:Class) 
WHERE dependent.is_external = false 
  AND ALL(r IN relationships(path) WHERE r.type = "import")
WITH path, nodes(path) as pathNodes, relationships(path) as pathRels
UNWIND range(0, size(pathNodes)-2) as i
WITH pathNodes[i] as source, pathNodes[i+1] as target, pathRels[i] as rel, length(path) as totalDepth
RETURN 
  source.name as sourceClass,
  source.package_name as sourcePackage,
  target.name as targetClass,
  target.package_name as targetPackage,
  rel.type as relationshipType,
  rel.context as context,
  totalDepth
ORDER BY totalDepth, sourcePackage, sourceClass;

// 7. Find classes that have multiple paths to TagForProduction
MATCH path = (start:Class {name: "TagForProduction"})<-[:USES*1..3]-(dependent:Class) 
WHERE dependent.is_external = false 
  AND ALL(r IN relationships(path) WHERE r.type = "import")
WITH dependent, collect(path) as paths
WHERE size(paths) > 1
RETURN 
  dependent.name as dependentClass,
  dependent.package_name as dependentPackage,
  size(paths) as pathCount,
  [p IN paths | length(p)] as pathLengths
ORDER BY pathCount DESC, dependentPackage, dependentClass;
