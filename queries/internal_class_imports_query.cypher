// Cypher Query: Show class-to-class USES relationships where an import exists but not to external classes
// This query finds internal dependencies within your application based on import statements

MATCH (source:Class)-[r:USES]->(target:Class) 
WHERE r.type = "import" 
  AND target.is_external = false 
RETURN 
  source.name as sourceClass, 
  source.package_name as sourcePackage, 
  target.name as targetClass, 
  target.package_name as targetPackage, 
  r.fullyQualifiedName as importedClass,
  r.context as importContext
ORDER BY sourcePackage, sourceClass, targetPackage, targetClass

// Alternative query with counts and grouping:
// MATCH (source:Class)-[r:USES]->(target:Class) 
// WHERE r.type = "import" AND target.is_external = false 
// RETURN 
//   source.package_name as sourcePackage,
//   target.package_name as targetPackage,
//   count(*) as importCount
// ORDER BY importCount DESC, sourcePackage, targetPackage

// Query to find cross-package internal imports:
// MATCH (source:Class)-[r:USES]->(target:Class) 
// WHERE r.type = "import" 
//   AND target.is_external = false 
//   AND source.package_name <> target.package_name
// RETURN 
//   source.package_name as sourcePackage,
//   target.package_name as targetPackage,
//   count(*) as crossPackageImports
// ORDER BY crossPackageImports DESC
