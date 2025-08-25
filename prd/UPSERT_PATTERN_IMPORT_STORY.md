# Upsert Pattern Import Story

## Overview

Implement an upsert (update if exists, insert if not exists) pattern for code re-imports to prevent duplicate nodes and ensure existing nodes are properly updated with new information. This addresses the current issue where each import creates new nodes regardless of whether they already exist.

## Objectives

- Implement duplicate detection for existing nodes
- Create upsert operations for Class, Method, Field, and Package nodes
- Handle relationship updates and deduplication
- Maintain data integrity during re-imports
- Improve performance by avoiding unnecessary node creation

## Timeline

**Duration**: 1-2 weeks
**Dependencies**: Phase 1 (Core Infrastructure) must be complete
**Prerequisites**: Before Phase 2.5 (LLM MCP Integration)

## Core Requirements

### 1. Unique Node Identification

- [ ] Define unique composite keys for each node type
- [ ] Implement node lookup by unique identifiers
- [ ] Create indexes for efficient node discovery
- [ ] Handle edge cases for node identification

### 2. Upsert Operations

- [ ] Implement upsert for Class nodes
- [ ] Implement upsert for Method nodes
- [ ] Implement upsert for Field nodes
- [ ] Implement upsert for Package nodes
- [ ] Handle metadata updates (commit hash, timestamps)

### 3. Relationship Management

- [ ] Detect existing relationships
- [ ] Update relationship properties
- [ ] Remove obsolete relationships
- [ ] Create new relationships only when needed
- [ ] Handle relationship deduplication

### 4. Repository Integration

- [ ] Update repository relationships
- [ ] Handle repository metadata changes
- [ ] Maintain repository-to-content links
- [ ] Update repository statistics

### 5. Performance Optimization

- [ ] Implement batch upsert operations
- [ ] Add caching for node lookups
- [ ] Optimize database queries
- [ ] Add progress tracking for large imports

## Technical Implementation

### Unique Node Identification Strategy

```java
@Service
public class NodeIdentifierService {

    /**
     * Generate unique identifier for Class node
     */
    public String generateClassId(String className, String packageName, String filePath) {
        return String.format("class:%s:%s:%s", 
            packageName, className, filePath);
    }

    /**
     * Generate unique identifier for Method node
     */
    public String generateMethodId(String methodName, String className, String packageName, String filePath) {
        return String.format("method:%s:%s:%s:%s", 
            packageName, className, methodName, filePath);
    }

    /**
     * Generate unique identifier for Field node
     */
    public String generateFieldId(String fieldName, String className, String packageName, String filePath) {
        return String.format("field:%s:%s:%s:%s", 
            packageName, className, fieldName, filePath);
    }

    /**
     * Generate unique identifier for Package node
     */
    public String generatePackageId(String packageName, String filePath) {
        return String.format("package:%s:%s", 
            packageName, filePath);
    }
}
```

### Upsert Service Implementation

```java
@Service
public class UpsertService {

    private final GraphService graphService;
    private final NodeIdentifierService nodeIdentifierService;

    /**
     * Upsert Class node
     */
    public ClassNode upsertClass(ClassNode classNode) {
        String uniqueId = nodeIdentifierService.generateClassId(
            classNode.getName(), classNode.getPackageName(), classNode.getFilePath());
        
        // Check if node exists
        ClassNode existingNode = graphService.findClassByUniqueId(uniqueId);
        
        if (existingNode != null) {
            // Update existing node
            updateClassNode(existingNode, classNode);
            return graphService.updateClass(existingNode);
        } else {
            // Create new node
            classNode.setUniqueId(uniqueId);
            return graphService.saveClass(classNode);
        }
    }

    /**
     * Upsert Method node
     */
    public MethodNode upsertMethod(MethodNode methodNode) {
        String uniqueId = nodeIdentifierService.generateMethodId(
            methodNode.getName(), methodNode.getClassName(), 
            methodNode.getPackageName(), methodNode.getFilePath());
        
        MethodNode existingNode = graphService.findMethodByUniqueId(uniqueId);
        
        if (existingNode != null) {
            updateMethodNode(existingNode, methodNode);
            return graphService.updateMethod(existingNode);
        } else {
            methodNode.setUniqueId(uniqueId);
            return graphService.saveMethod(methodNode);
        }
    }

    /**
     * Upsert Field node
     */
    public FieldNode upsertField(FieldNode fieldNode) {
        String uniqueId = nodeIdentifierService.generateFieldId(
            fieldNode.getName(), fieldNode.getClassName(), 
            fieldNode.getPackageName(), fieldNode.getFilePath());
        
        FieldNode existingNode = graphService.findFieldByUniqueId(uniqueId);
        
        if (existingNode != null) {
            updateFieldNode(existingNode, fieldNode);
            return graphService.updateField(existingNode);
        } else {
            fieldNode.setUniqueId(uniqueId);
            return graphService.saveField(fieldNode);
        }
    }

    /**
     * Upsert Package node
     */
    public PackageNode upsertPackage(PackageNode packageNode) {
        String uniqueId = nodeIdentifierService.generatePackageId(
            packageNode.getName(), packageNode.getFilePath());
        
        PackageNode existingNode = graphService.findPackageByUniqueId(uniqueId);
        
        if (existingNode != null) {
            updatePackageNode(existingNode, packageNode);
            return graphService.updatePackage(existingNode);
        } else {
            packageNode.setUniqueId(uniqueId);
            return graphService.savePackage(packageNode);
        }
    }

    private void updateClassNode(ClassNode existing, ClassNode updated) {
        // Update metadata
        existing.setVisibility(updated.getVisibility());
        existing.setModifiers(updated.getModifiers());
        existing.setIsInterface(updated.getIsInterface());
        existing.setIsEnum(updated.getIsEnum());
        existing.setIsAnnotation(updated.getIsAnnotation());
        existing.setLineStart(updated.getLineStart());
        existing.setLineEnd(updated.getLineEnd());
        
        // Update repository metadata
        existing.setRepositoryId(updated.getRepositoryId());
        existing.setRepositoryName(updated.getRepositoryName());
        existing.setRepositoryUrl(updated.getRepositoryUrl());
        existing.setBranch(updated.getBranch());
        existing.setCommitHash(updated.getCommitHash());
        existing.setCommitDate(updated.getCommitDate());
        existing.setFileRelativePath(updated.getFileRelativePath());
        
        // Update timestamps
        existing.setUpdatedAt(LocalDateTime.now());
    }

    private void updateMethodNode(MethodNode existing, MethodNode updated) {
        // Update method-specific properties
        existing.setVisibility(updated.getVisibility());
        existing.setModifiers(updated.getModifiers());
        existing.setReturnType(updated.getReturnType());
        existing.setParameters(updated.getParameters());
        existing.setParameterNames(updated.getParameterNames());
        existing.setLineStart(updated.getLineStart());
        existing.setLineEnd(updated.getLineEnd());
        
        // Update timestamps
        existing.setUpdatedAt(LocalDateTime.now());
    }

    private void updateFieldNode(FieldNode existing, FieldNode updated) {
        // Update field-specific properties
        existing.setVisibility(updated.getVisibility());
        existing.setModifiers(updated.getModifiers());
        existing.setType(updated.getType());
        existing.setLineNumber(updated.getLineNumber());
        
        // Update timestamps
        existing.setUpdatedAt(LocalDateTime.now());
    }

    private void updatePackageNode(PackageNode existing, PackageNode updated) {
        // Update package-specific properties
        existing.setDescription(updated.getDescription());
        
        // Update timestamps
        existing.setUpdatedAt(LocalDateTime.now());
    }
}
```

### Relationship Upsert Service

```java
@Service
public class RelationshipUpsertService {

    private final GraphService graphService;

    /**
     * Upsert CONTAINS relationship
     */
    public void upsertContainsRelationship(String fromId, String toId, String relationshipType) {
        // Check if relationship exists
        boolean exists = graphService.relationshipExists(fromId, toId, relationshipType);
        
        if (!exists) {
            graphService.createRelationship(fromId, toId, relationshipType);
        }
    }

    /**
     * Upsert CALLS relationship
     */
    public void upsertCallsRelationship(String fromMethodId, String toMethodId) {
        boolean exists = graphService.relationshipExists(fromMethodId, toMethodId, "CALLS");
        
        if (!exists) {
            graphService.createRelationship(fromMethodId, toMethodId, "CALLS");
        }
    }

    /**
     * Upsert USES relationship
     */
    public void upsertUsesRelationship(String methodId, String fieldId) {
        boolean exists = graphService.relationshipExists(methodId, fieldId, "USES");
        
        if (!exists) {
            graphService.createRelationship(methodId, fieldId, "USES");
        }
    }

    /**
     * Upsert EXTENDS relationship
     */
    public void upsertExtendsRelationship(String classId, String parentClassId) {
        boolean exists = graphService.relationshipExists(classId, parentClassId, "EXTENDS");
        
        if (!exists) {
            graphService.createRelationship(classId, parentClassId, "EXTENDS");
        }
    }

    /**
     * Upsert IMPLEMENTS relationship
     */
    public void upsertImplementsRelationship(String classId, String interfaceId) {
        boolean exists = graphService.relationshipExists(classId, interfaceId, "IMPLEMENTS");
        
        if (!exists) {
            graphService.createRelationship(classId, interfaceId, "IMPLEMENTS");
        }
    }

    /**
     * Upsert Repository CONTAINS relationship
     */
    public void upsertRepositoryContainsRelationship(String repositoryId, String nodeId) {
        boolean exists = graphService.relationshipExists(repositoryId, nodeId, "CONTAINS");
        
        if (!exists) {
            graphService.createRelationship(repositoryId, nodeId, "CONTAINS");
        }
    }
}
```

### Enhanced JavaParserService

```java
@Service
public class JavaParserService {

    private final UpsertService upsertService;
    private final RelationshipUpsertService relationshipUpsertService;
    private final RepositoryService repositoryService;

    private void parseJavaFile(final Path filePath) throws FileNotFoundException {
        // Detect repository metadata for this file
        RepositoryMetadata repoMetadata = repositoryService.detectRepositoryMetadata(filePath);
        RepositoryNode repository = repositoryService.createOrUpdateRepository(repoMetadata);

        ParseResult<CompilationUnit> parseResult = javaParser.parse(filePath.toFile());

        if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
            CompilationUnit cu = parseResult.getResult().get();
            JavaGraphVisitor visitor = new JavaGraphVisitor(filePath.toString(), repoMetadata, upsertService, relationshipUpsertService);
            visitor.visit(cu, null);
            
            // Link all created/updated nodes to the repository
            if (repository != null && !visitor.processedNodes.isEmpty()) {
                repositoryService.linkNodesToRepository(visitor.processedNodes, repository);
                LOGGER.info("Linked {} nodes to repository: {}", visitor.processedNodes.size(), repository.getName());
            }
        } else {
            LOGGER.warn("Failed to parse file: {}", filePath);
        }
    }

    private class JavaGraphVisitor extends VoidVisitorAdapter<Void> {
        
        private final UpsertService upsertService;
        private final RelationshipUpsertService relationshipUpsertService;
        private final List<Object> processedNodes = new ArrayList<>();
        
        @Override
        public void visit(final ClassOrInterfaceDeclaration classDecl, final Void arg) {
            if (classDecl.isPublic() || parserConfig.isIncludePrivate()) {
                LOGGER.info("Processing class: {}", classDecl.getNameAsString());
                currentClass = createClassNode(classDecl);
                
                // Use upsert instead of save
                currentClass = upsertService.upsertClass(currentClass);
                processedNodes.add(currentClass);
                
                // Clear current methods and fields for this class
                currentMethods.clear();
                currentFields.clear();
                
                // Visit methods and fields
                super.visit(classDecl, arg);
                
                // Create CONTAINS relationships using upsert
                createContainsRelationships();
                
                // Create EXTENDS relationship using upsert
                if (classDecl.getExtendedTypes().isNonEmpty()) {
                    classDecl.getExtendedTypes().forEach(extendedType -> {
                        String parentClassName = extendedType.getNameAsString();
                        ClassNode parentClass = new ClassNode(parentClassName, packageName, filePath);
                        parentClass = upsertService.upsertClass(parentClass);
                        relationshipUpsertService.upsertExtendsRelationship(currentClass.getId(), parentClass.getId());
                        LOGGER.debug("Created EXTENDS relationship: {} -> {}", currentClass.getName(), parentClassName);
                    });
                }

                // Create IMPLEMENTS relationships using upsert
                if (classDecl.getImplementedTypes().isNonEmpty()) {
                    classDecl.getImplementedTypes().forEach(implementedType -> {
                        String interfaceName = implementedType.getNameAsString();
                        ClassNode interfaceClass = new ClassNode(interfaceName, packageName, filePath);
                        interfaceClass.setIsInterface(true);
                        interfaceClass = upsertService.upsertClass(interfaceClass);
                        relationshipUpsertService.upsertImplementsRelationship(currentClass.getId(), interfaceClass.getId());
                        LOGGER.debug("Created IMPLEMENTS relationship: {} -> {}", currentClass.getName(), interfaceName);
                    });
                }
                
                currentClass = null;
            }
        }
        
        @Override
        public void visit(final MethodDeclaration methodDecl, final Void arg) {
            if (methodDecl.isPublic() || parserConfig.isIncludePrivate()) {
                MethodNode methodNode = createMethodNode(methodDecl);
                
                // Use upsert instead of save
                methodNode = upsertService.upsertMethod(methodNode);
                currentMethods.add(methodNode);
                processedNodes.add(methodNode);

                // Detect method calls within this method
                detectMethodCalls(methodDecl, methodNode);

                // Detect field usage within this method
                detectFieldUsage(methodDecl, methodNode);
            }
        }
        
        @Override
        public void visit(FieldDeclaration fieldDecl, Void arg) {
            if (fieldDecl.isPublic() || parserConfig.isIncludePrivate()) {
                fieldDecl.getVariables().forEach(variable -> {
                    FieldNode fieldNode = createFieldNode(variable.getNameAsString(), fieldDecl);
                    
                    // Use upsert instead of save
                    fieldNode = upsertService.upsertField(fieldNode);
                    currentFields.add(fieldNode);
                    processedNodes.add(fieldNode);
                });
            }
        }
        
        private void createContainsRelationships() {
            // Create CONTAINS relationships from class to methods using upsert
            for (MethodNode method : currentMethods) {
                relationshipUpsertService.upsertContainsRelationship(currentClass.getId(), method.getId(), "CONTAINS");
                LOGGER.debug("Created CONTAINS relationship: {} -> {}", currentClass.getName(), method.getName());
            }

            // Create CONTAINS relationships from class to fields using upsert
            for (FieldNode field : currentFields) {
                relationshipUpsertService.upsertContainsRelationship(currentClass.getId(), field.getId(), "CONTAINS");
                LOGGER.debug("Created CONTAINS relationship: {} -> {}", currentClass.getName(), field.getName());
            }
        }
        
        private void detectMethodCalls(MethodDeclaration methodDecl, MethodNode methodNode) {
            // Find all method calls within this method
            methodDecl.findAll(com.github.javaparser.ast.expr.MethodCallExpr.class).forEach(call -> {
                String calledMethodName = call.getNameAsString();
                
                // Look for the called method in current methods or create a placeholder
                MethodNode calledMethod = currentMethods.stream()
                    .filter(m -> m.getName().equals(calledMethodName))
                    .findFirst()
                    .orElseGet(() -> {
                        // Create a placeholder method node
                        MethodNode placeholder = new MethodNode(calledMethodName, currentClass.getName(), packageName, filePath);
                        return upsertService.upsertMethod(placeholder);
                    });
                
                // Create CALLS relationship using upsert
                relationshipUpsertService.upsertCallsRelationship(methodNode.getId(), calledMethod.getId());
                LOGGER.debug("Created CALLS relationship: {} -> {}", methodNode.getName(), calledMethodName);
            });
        }
        
        private void detectFieldUsage(MethodDeclaration methodDecl, MethodNode methodNode) {
            // Find all field access expressions within this method
            methodDecl.findAll(com.github.javaparser.ast.expr.FieldAccessExpr.class).forEach(fieldAccess -> {
                String fieldName = fieldAccess.getNameAsString();
                
                // Look for the field in current fields or create a placeholder
                FieldNode field = currentFields.stream()
                    .filter(f -> f.getName().equals(fieldName))
                    .findFirst()
                    .orElseGet(() -> {
                        // Create a placeholder field node
                        FieldNode placeholder = new FieldNode(fieldName, currentClass.getName(), packageName, filePath);
                        return upsertService.upsertField(placeholder);
                    });
                
                // Create USES relationship using upsert
                relationshipUpsertService.upsertUsesRelationship(methodNode.getId(), field.getId());
                LOGGER.debug("Created USES relationship: {} -> {}", methodNode.getName(), fieldName);
            });
        }
    }
}
```

### Database Schema Updates

```cypher
// Add unique constraints for node identification
CREATE CONSTRAINT class_unique_id IF NOT EXISTS FOR (c:Class) REQUIRE c.uniqueId IS UNIQUE;
CREATE CONSTRAINT method_unique_id IF NOT EXISTS FOR (m:Method) REQUIRE m.uniqueId IS UNIQUE;
CREATE CONSTRAINT field_unique_id IF NOT EXISTS FOR (f:Field) REQUIRE f.uniqueId IS UNIQUE;
CREATE CONSTRAINT package_unique_id IF NOT EXISTS FOR (p:Package) REQUIRE p.uniqueId IS UNIQUE;

// Add indexes for efficient lookups
CREATE INDEX class_lookup IF NOT EXISTS FOR (c:Class) ON (c.uniqueId);
CREATE INDEX method_lookup IF NOT EXISTS FOR (m:Method) ON (m.uniqueId);
CREATE INDEX field_lookup IF NOT EXISTS FOR (f:Field) ON (f.uniqueId);
CREATE INDEX package_lookup IF NOT EXISTS FOR (p:Package) ON (p.uniqueId);

// Add indexes for relationship lookups
CREATE INDEX relationship_lookup IF NOT EXISTS FOR ()-[r]-() ON (type(r));
```

## Validation Cases

### 1. Node Upsert Functionality

- [ ] **Class Upsert**: Update existing class with new metadata
- [ ] **Method Upsert**: Update existing method with new parameters
- [ ] **Field Upsert**: Update existing field with new type information
- [ ] **Package Upsert**: Update existing package with new description
- [ ] **New Node Creation**: Create new nodes when they don't exist

### 2. Relationship Management

- [ ] **Existing Relationships**: Don't create duplicate relationships
- [ ] **New Relationships**: Create relationships only when needed
- [ ] **Relationship Updates**: Update relationship properties if changed
- [ ] **Repository Links**: Maintain repository-to-content relationships

### 3. Performance Testing

- [ ] **Large Codebase**: Handle re-import of large codebases efficiently
- [ ] **Batch Operations**: Process nodes in batches for better performance
- [ ] **Memory Usage**: Monitor memory usage during large imports
- [ ] **Database Performance**: Optimize database queries and indexes

### 4. Data Integrity

- [ ] **Unique Constraints**: Ensure no duplicate nodes are created
- [ ] **Referential Integrity**: Maintain proper relationships between nodes
- [ ] **Metadata Consistency**: Keep metadata up to date
- [ ] **Repository Consistency**: Maintain repository information accuracy

### 5. Edge Cases

- [ ] **File Renames**: Handle files that have been renamed
- [ ] **Class Moves**: Handle classes that have been moved to different packages
- [ ] **Method Signature Changes**: Handle methods with changed signatures
- [ ] **Deleted Code**: Handle code that has been removed

## Success Criteria

- [ ] No duplicate nodes created during re-imports
- [ ] Existing nodes are properly updated with new information
- [ ] Relationships are deduplicated and maintained correctly
- [ ] Performance is improved compared to current implementation
- [ ] Data integrity is maintained throughout the process
- [ ] Repository relationships are properly managed
- [ ] Large codebases can be re-imported efficiently

## Dependencies

- Phase 1: Core Infrastructure (Neo4j integration, basic API)
- GraphService: Must support find operations by unique identifiers
- RepositoryService: Must support repository metadata updates
- Database: Must support unique constraints and indexes

## Deliverables

- [ ] UpsertService implementation
- [ ] RelationshipUpsertService implementation
- [ ] Enhanced JavaParserService with upsert support
- [ ] NodeIdentifierService implementation
- [ ] Database schema updates with constraints and indexes
- [ ] Comprehensive test suite
- [ ] Performance benchmarks
- [ ] Documentation updates

## Risk Mitigation

- **Performance**: Implement batch operations and caching
- **Data Integrity**: Use database constraints and validation
- **Complexity**: Incremental implementation with thorough testing
- **Migration**: Provide migration path for existing data
- **Rollback**: Ensure ability to rollback changes if needed

## Example Usage

### Before Upsert (Current Behavior)
```java
// Always creates new nodes
graphService.saveClass(classNode);
graphService.saveMethod(methodNode);
graphService.createRelationship(classId, methodId, "CONTAINS");
```

### After Upsert (New Behavior)
```java
// Updates existing nodes or creates new ones
classNode = upsertService.upsertClass(classNode);
methodNode = upsertService.upsertMethod(methodNode);
relationshipUpsertService.upsertContainsRelationship(classId, methodId, "CONTAINS");
```

This upsert pattern will significantly improve the efficiency and reliability of code re-imports, ensuring that the graph database maintains accurate and up-to-date information without duplication.
