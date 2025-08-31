package com.vividcodes.graphrag.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.vividcodes.graphrag.config.ParserConfig;
import com.vividcodes.graphrag.model.dto.RepositoryMetadata;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.FieldNode;
import com.vividcodes.graphrag.model.graph.MethodNode;
import com.vividcodes.graphrag.model.graph.PackageNode;
import com.vividcodes.graphrag.model.graph.SubProjectNode;

/**
 * JavaParser visitor for traversing Java AST and creating graph nodes and relationships.
 * This class is responsible for visiting different AST elements and coordinating with
 * other services to create the appropriate graph structure.
 */
@Component
@Scope("prototype") // Create new instance for each parsing operation
public class JavaGraphVisitor extends VoidVisitorAdapter<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaGraphVisitor.class);

    // Injected dependencies
    private final ParserConfig parserConfig;
    private final GraphService graphService;
    private final NodeFactory nodeFactory;
    private final RelationshipManager relationshipManager;
    private final DependencyAnalyzer dependencyAnalyzer;
    private final TypeResolver typeResolver;

    // Context for current parsing operation
    private String filePath;
    private RepositoryMetadata repositoryMetadata;
    private SubProjectNode containingSubProject;
    private String packageName = "";
    private ClassNode currentClass;
    private final List<MethodNode> currentMethods = new ArrayList<>();
    private final List<FieldNode> currentFields = new ArrayList<>();
    private final List<Object> createdNodes = new ArrayList<>();
    private PackageNode currentPackage;

    // Track imported classes for dependency analysis
    private final Map<String, String> importedClasses = new HashMap<>(); // className -> fullyQualifiedName

    @Autowired
    public JavaGraphVisitor(final ParserConfig parserConfig,
                          final GraphService graphService,
                          final NodeFactory nodeFactory,
                          final RelationshipManager relationshipManager,
                          final DependencyAnalyzer dependencyAnalyzer,
                          final TypeResolver typeResolver) {
        this.parserConfig = parserConfig;
        this.graphService = graphService;
        this.nodeFactory = nodeFactory;
        this.relationshipManager = relationshipManager;
        this.dependencyAnalyzer = dependencyAnalyzer;
        this.typeResolver = typeResolver;
    }

    /**
     * Initialize the visitor for a new parsing operation.
     * 
     * @param filePath The file path being parsed
     * @param repositoryMetadata Repository metadata for the file
     * @param containingSubProject The sub-project containing the file (may be null)
     */
    public void initialize(final String filePath,
                         final RepositoryMetadata repositoryMetadata,
                         final SubProjectNode containingSubProject) {
        this.filePath = filePath;
        this.repositoryMetadata = repositoryMetadata;
        this.containingSubProject = containingSubProject;
        
        // Clear state from any previous parsing operation
        this.packageName = "";
        this.currentClass = null;
        this.currentMethods.clear();
        this.currentFields.clear();
        this.createdNodes.clear();
        this.currentPackage = null;
        this.importedClasses.clear();
    }

    @Override
    public void visit(final PackageDeclaration packageDecl, final Void arg) {
        LOGGER.info("Visiting package declaration: {}", packageDecl.getNameAsString());
        this.packageName = packageDecl.getNameAsString();
        
        // Create package node
        final PackageNode packageNode = new PackageNode(packageName, packageName, filePath);
        LOGGER.info("Created package node: {} with ID: {}", packageNode.getName(), packageNode.getId());
        graphService.savePackage(packageNode);
        createdNodes.add(packageNode);
        this.currentPackage = packageNode;  // Set the current package for relationship creation
        LOGGER.info("Added package node to createdNodes list. Total nodes: {}", createdNodes.size());
        
        super.visit(packageDecl, arg);
    }

    @Override
    public void visit(final ImportDeclaration importDecl, final Void arg) {
        final String fullyQualifiedName = importDecl.getNameAsString();
        final String className = typeResolver.extractSimpleClassName(fullyQualifiedName);
        
        // Store the import for later dependency analysis
        importedClasses.put(className, fullyQualifiedName);
        
        LOGGER.debug("Processing import: {}", fullyQualifiedName);
        
        // Create or get the imported class node (creates it in the database)
        nodeFactory.createExternalClassNode(className, fullyQualifiedName, graphService);
        
        // We'll create the USES relationship when we process the class that contains this import
        // For now, just track the import for later processing
        LOGGER.debug("Registered import: {} -> {}", className, fullyQualifiedName);
        
        super.visit(importDecl, arg);
    }

    @Override
    public void visit(final ClassOrInterfaceDeclaration classDecl, final Void arg) {
        // Process public classes or all classes if includePrivate is enabled
        if (classDecl.isPublic() || parserConfig.isIncludePrivate()) {
            LOGGER.info("Processing class: {}", classDecl.getNameAsString());
            currentClass = nodeFactory.createClassNode(classDecl, packageName, filePath, repositoryMetadata, containingSubProject);
            LOGGER.info("Created class node: {}", currentClass.getId());
            graphService.saveClass(currentClass);
            createdNodes.add(currentClass);
            
            // Clear current methods and fields for this class
            currentMethods.clear();
            currentFields.clear();
            
            // Visit methods and fields
            super.visit(classDecl, arg);
            
            // After visiting all children, create relationships
            if (currentClass != null) {
                // Create CONTAINS relationships for this class
                relationshipManager.createContainsRelationships(
                    currentClass, currentPackage, containingSubProject, currentMethods, currentFields);
                
                // Create USES relationships for imported classes
                dependencyAnalyzer.createImportUsesRelationships(currentClass, importedClasses);
                
                // Create EXTENDS relationship if this class extends another
                if (classDecl.getExtendedTypes().isNonEmpty()) {
                    classDecl.getExtendedTypes().forEach(extendedType -> {
                        final String parentClassName = extendedType.getNameAsString();
                        final String parentFQN = typeResolver.resolveClassName(parentClassName, importedClasses);
                        final ClassNode parentClass = nodeFactory.createExternalClassNode(parentClassName, parentFQN, graphService);
                        relationshipManager.createInheritanceRelationship(currentClass, parentClass, "EXTENDS");
                    });
                }
                
                // Create IMPLEMENTS relationships if this class implements interfaces
                if (classDecl.getImplementedTypes().isNonEmpty()) {
                    classDecl.getImplementedTypes().forEach(implementedType -> {
                        final String interfaceName = implementedType.getNameAsString();
                        final String interfaceFQN = typeResolver.resolveClassName(interfaceName, importedClasses);
                        final ClassNode interfaceClass = nodeFactory.createExternalClassNode(interfaceName, interfaceFQN, graphService);
                        relationshipManager.createInheritanceRelationship(currentClass, interfaceClass, "IMPLEMENTS");
                    });
                }
                
                currentClass = null;
            }
        }
    }

    @Override
    public void visit(final MethodDeclaration methodDecl, final Void arg) {
        // Process public methods or all methods if includePrivate is enabled
        if (methodDecl.isPublic() || parserConfig.isIncludePrivate()) {
            final MethodNode methodNode = nodeFactory.createMethodNode(methodDecl, 
                currentClass != null ? currentClass.getName() : "", packageName, filePath);
            graphService.saveMethod(methodNode);
            currentMethods.add(methodNode);
            createdNodes.add(methodNode);
            
            // Detect method calls within this method
            dependencyAnalyzer.detectStaticMethodCalls(methodDecl, methodNode, currentClass, importedClasses);
            
            // Detect object instantiation within this method
            dependencyAnalyzer.detectObjectInstantiation(methodDecl, methodNode, currentClass, importedClasses);
            
            // Detect field usage within this method
            detectFieldUsage(methodDecl, methodNode);
            
            // Detect method signature dependencies
            dependencyAnalyzer.detectMethodSignatureDependencies(methodDecl, methodNode, currentClass, importedClasses);
        }
    }

    @Override
    public void visit(final FieldDeclaration fieldDecl, final Void arg) {
        // Process public fields or all fields if includePrivate is enabled
        if (fieldDecl.isPublic() || parserConfig.isIncludePrivate()) {
            fieldDecl.getVariables().forEach(variable -> {
                final String fieldName = variable.getNameAsString();
                final FieldNode fieldNode = nodeFactory.createFieldNode(fieldName, fieldDecl, 
                    currentClass != null ? currentClass.getName() : "", packageName, filePath);
                graphService.saveField(fieldNode);
                currentFields.add(fieldNode);
                createdNodes.add(fieldNode);
                
                // Detect object instantiation in field initializers
                if (variable.getInitializer().isPresent()) {
                    variable.getInitializer().get().findAll(ObjectCreationExpr.class).forEach(newExpr -> {
                        final String className = newExpr.getType().getNameAsString();
                        final String fullyQualifiedName = typeResolver.resolveClassName(className, importedClasses);
                        
                        LOGGER.debug("Detected object instantiation: {} in field: {}", className, fieldName);
                        
                        // Create or get the class node
                        final ClassNode targetClass = nodeFactory.createExternalClassNode(className, fullyQualifiedName, graphService);
                        
                        // Create USES relationship
                        if (currentClass != null) {
                            final String context = "field: " + fieldName;
                            relationshipManager.createInstantiationUsesRelationship(currentClass, targetClass, context);
                            LOGGER.debug("Created USES relationship for field instantiation: {} -> {} ({})", 
                                       currentClass.getName(), className, fullyQualifiedName);
                        }
                    });
                }
                
                // Detect field type dependencies
                dependencyAnalyzer.detectFieldTypeDependencies(variable, fieldDecl, currentClass, importedClasses);
            });
        }
    }

    /**
     * Detect field usage within a method.
     * 
     * @param methodDecl The method declaration
     * @param methodNode The method node
     */
    private void detectFieldUsage(final MethodDeclaration methodDecl, final MethodNode methodNode) {
        // Find all field access expressions within this method
        methodDecl.findAll(com.github.javaparser.ast.expr.FieldAccessExpr.class).forEach(fieldAccess -> {
            final String fieldName = fieldAccess.getNameAsString();
            
            // Look for the field in current fields
            currentFields.stream()
                .filter(field -> field.getName().equals(fieldName))
                .findFirst()
                .ifPresent(field -> {
                    relationshipManager.createMethodFieldUsesRelationship(methodNode, field);
                    LOGGER.debug("Created USES relationship: {} -> {}", methodNode.getName(), field.getName());
                });
        });
    }



    /**
     * Get the list of nodes created during this parsing operation.
     * 
     * @return List of created nodes
     */
    public List<Object> getCreatedNodes() {
        return new ArrayList<>(createdNodes);
    }

    /**
     * Get the current class being processed.
     * 
     * @return The current class node, or null if no class is being processed
     */
    public ClassNode getCurrentClass() {
        return currentClass;
    }

    /**
     * Get the current package being processed.
     * 
     * @return The current package node, or null if no package is being processed
     */
    public PackageNode getCurrentPackage() {
        return currentPackage;
    }

    /**
     * Get the methods found in the current class.
     * 
     * @return List of method nodes
     */
    public List<MethodNode> getCurrentMethods() {
        return new ArrayList<>(currentMethods);
    }

    /**
     * Get the fields found in the current class.
     * 
     * @return List of field nodes
     */
    public List<FieldNode> getCurrentFields() {
        return new ArrayList<>(currentFields);
    }
}
