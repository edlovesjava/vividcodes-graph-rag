package com.vividcodes.graphrag.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.MethodNode;

/**
 * Service responsible for analyzing and detecting various types of dependencies in Java code.
 * Handles import dependencies, static method calls, object instantiation, field types, and method signatures.
 */
@Component
public class DependencyAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyAnalyzer.class);

    private final TypeResolver typeResolver;
    private final NodeFactory nodeFactory;
    private final RelationshipManager relationshipManager;
    private final GraphService graphService;

    @Autowired
    public DependencyAnalyzer(final TypeResolver typeResolver,
                            final NodeFactory nodeFactory,
                            final RelationshipManager relationshipManager,
                            final GraphService graphService) {
        this.typeResolver = typeResolver;
        this.nodeFactory = nodeFactory;
        this.relationshipManager = relationshipManager;
        this.graphService = graphService;
    }

    /**
     * Create USES relationships for all imported classes.
     * 
     * @param currentClass The class that contains the imports
     * @param importedClasses Map of simple class names to fully qualified names
     */
    public void createImportUsesRelationships(final ClassNode currentClass,
                                            final Map<String, String> importedClasses) {
        if (currentClass == null || importedClasses.isEmpty()) {
            LOGGER.debug("Skipping import relationships: currentClass={}, importedClasses.size()={}", 
                       currentClass != null ? currentClass.getName() : "null", importedClasses.size());
            return;
        }

        for (final Map.Entry<String, String> entry : importedClasses.entrySet()) {
            final String className = entry.getKey();
            final String fullyQualifiedName = entry.getValue();

            // Create or get the imported class node
            final ClassNode importedClass = getOrCreateClassNode(className, fullyQualifiedName);

            // Create USES relationship
            relationshipManager.createImportUsesRelationship(currentClass, importedClass, fullyQualifiedName);
            LOGGER.debug("Created USES relationship for import: {} -> {} ({})", 
                       currentClass.getName(), className, fullyQualifiedName);
        }
    }

    /**
     * Detect and create relationships for static method calls.
     * 
     * @param methodDecl The method declaration to analyze
     * @param methodNode The method node
     * @param currentClass The class containing the method
     * @param importedClasses Map of imported classes
     */
    public void detectStaticMethodCalls(final MethodDeclaration methodDecl,
                                      final MethodNode methodNode,
                                      final ClassNode currentClass,
                                      final Map<String, String> importedClasses) {
        if (methodDecl == null || currentClass == null) {
            return;
        }

        // Find all method calls within this method
        methodDecl.findAll(MethodCallExpr.class).forEach(call -> {
            final String calledMethodName = call.getNameAsString();

            // Check if this is a static method call (has a scope)
            if (call.getScope().isPresent()) {
                final String scopeName = call.getScope().get().toString();
                LOGGER.debug("Detected method call with scope: {}.{}", scopeName, calledMethodName);

                // Check if the scope matches an imported class (static method call)
                if (importedClasses.containsKey(scopeName)) {
                    final String fullyQualifiedName = importedClasses.get(scopeName);
                    LOGGER.debug("Static method call detected: {}.{} -> {}", 
                               scopeName, calledMethodName, fullyQualifiedName);

                    // Create or get the target class node
                    final ClassNode targetClass = getOrCreateClassNode(scopeName, fullyQualifiedName);

                    // Create USES relationship from current class to target class (static method usage)
                    relationshipManager.createStaticMethodUsesRelationship(
                        currentClass, targetClass, calledMethodName);
                    LOGGER.debug("Created USES relationship: {} -> {} (static method call: {})", 
                               currentClass.getName(), scopeName, calledMethodName);
                } else {
                    LOGGER.debug("Method call scope not in imports: {}.{}", scopeName, calledMethodName);
                }
            }
        });
    }

    /**
     * Detect and create relationships for object instantiation.
     * 
     * @param methodDecl The method declaration to analyze
     * @param methodNode The method node
     * @param currentClass The class containing the method
     * @param importedClasses Map of imported classes
     */
    public void detectObjectInstantiation(final MethodDeclaration methodDecl,
                                        final MethodNode methodNode,
                                        final ClassNode currentClass,
                                        final Map<String, String> importedClasses) {
        if (methodDecl == null || currentClass == null) {
            return;
        }

        // Find all object creation expressions within this method
        methodDecl.findAll(ObjectCreationExpr.class).forEach(newExpr -> {
            final String className = newExpr.getType().getNameAsString();
            LOGGER.debug("Detected object instantiation: new {} in method {}", className, methodNode.getName());

                    // Resolve the fully qualified name
        final String fullyQualifiedName = typeResolver.resolveClassName(className, importedClasses);

            // Create or get the class node
            final ClassNode targetClass = getOrCreateClassNode(className, fullyQualifiedName);

            // Create USES relationship
            final String context = "method: " + methodNode.getName();
            relationshipManager.createInstantiationUsesRelationship(currentClass, targetClass, context);
            LOGGER.debug("Created USES relationship for instantiation: {} -> {} ({})", 
                       currentClass.getName(), className, fullyQualifiedName);
        });
    }

    /**
     * Detect and create relationships for field type dependencies.
     * 
     * @param variable The field variable
     * @param fieldDecl The field declaration
     * @param currentClass The class containing the field
     * @param importedClasses Map of imported classes
     */
    public void detectFieldTypeDependencies(final com.github.javaparser.ast.body.VariableDeclarator variable,
                                          final FieldDeclaration fieldDecl,
                                          final ClassNode currentClass,
                                          final Map<String, String> importedClasses) {
        if (variable == null || fieldDecl == null || currentClass == null) {
            return;
        }

        final String fieldName = variable.getNameAsString();
        final String fieldTypeName = fieldDecl.getElementType().toString();

        // Extract simple type name (remove generics, arrays, etc.)
        final String simpleTypeName = typeResolver.extractSimpleTypeName(fieldTypeName);

        // Skip primitive types
        if (typeResolver.isPrimitiveType(simpleTypeName)) {
            LOGGER.debug("Skipping primitive field type: {}", simpleTypeName);
            return;
        }

        LOGGER.debug("Detected field type dependency: field {} has type {}", fieldName, simpleTypeName);

        // Resolve the fully qualified name
        final String fullyQualifiedName = typeResolver.resolveClassName(simpleTypeName, importedClasses);

        // Create or get the class node for the field type
        final ClassNode fieldTypeClass = getOrCreateClassNode(simpleTypeName, fullyQualifiedName);

        // Create USES relationship
        relationshipManager.createFieldTypeUsesRelationship(
            currentClass, fieldTypeClass, fieldName, fieldTypeName);
        LOGGER.debug("Created USES relationship for field type: {} -> {} (field: {} type: {})", 
                   currentClass.getName(), simpleTypeName, fieldName, fieldTypeName);
    }

    /**
     * Detect and create relationships for method signature dependencies.
     * 
     * @param methodDecl The method declaration to analyze
     * @param methodNode The method node
     * @param currentClass The class containing the method
     * @param importedClasses Map of imported classes
     */
    public void detectMethodSignatureDependencies(final MethodDeclaration methodDecl,
                                                final MethodNode methodNode,
                                                final ClassNode currentClass,
                                                final Map<String, String> importedClasses) {
        if (methodDecl == null || methodNode == null || currentClass == null) {
            return;
        }

        final String methodName = methodNode.getName();

        // Detect return type dependencies
        final String returnType = methodDecl.getType().toString();
        final String simpleReturnType = typeResolver.extractSimpleTypeName(returnType);

        if (!typeResolver.isPrimitiveType(simpleReturnType) && !simpleReturnType.isEmpty()) {
            LOGGER.debug("Detected return type dependency: method {} returns {}", methodName, simpleReturnType);

            // Resolve the fully qualified name
            final String fullyQualifiedName = typeResolver.resolveClassName(simpleReturnType, importedClasses);

            // Create or get the class node for the return type
            final ClassNode returnTypeClass = getOrCreateClassNode(simpleReturnType, fullyQualifiedName);

            // Create USES relationship
            relationshipManager.createMethodSignatureUsesRelationship(
                currentClass, returnTypeClass, methodName, "return_type", returnType);
            LOGGER.debug("Created USES relationship for return type: {} -> {} (method: {} returns: {})", 
                       currentClass.getName(), simpleReturnType, methodName, returnType);
        }

        // Detect parameter type dependencies
        methodDecl.getParameters().forEach(param -> {
            final String paramType = param.getType().toString();
            final String paramName = param.getNameAsString();
            final String simpleParamType = typeResolver.extractSimpleTypeName(paramType);

            if (!typeResolver.isPrimitiveType(simpleParamType) && !simpleParamType.isEmpty()) {
                LOGGER.debug("Detected parameter type dependency: method {} parameter {} has type {}", 
                           methodName, paramName, simpleParamType);

                // Resolve the fully qualified name
                final String fullyQualifiedName = typeResolver.resolveClassName(simpleParamType, importedClasses);

                // Create or get the class node for the parameter type
                final ClassNode paramTypeClass = getOrCreateClassNode(simpleParamType, fullyQualifiedName);

                // Create USES relationship
                relationshipManager.createMethodSignatureUsesRelationship(
                    currentClass, paramTypeClass, methodName, "parameter_type", 
                    "param: " + paramName + " type: " + paramType);
                LOGGER.debug("Created USES relationship for parameter type: {} -> {} (method: {} param: {} type: {})", 
                           currentClass.getName(), simpleParamType, methodName, paramName, paramType);
            }
        });
    }



    /**
     * Get or create a ClassNode for a given class name.
     * 
     * @param simpleClassName The simple class name
     * @param fullyQualifiedName The fully qualified class name
     * @return The ClassNode for the class
     */
    private ClassNode getOrCreateClassNode(final String simpleClassName, final String fullyQualifiedName) {
        return nodeFactory.createExternalClassNode(simpleClassName, fullyQualifiedName, graphService);
    }
}
