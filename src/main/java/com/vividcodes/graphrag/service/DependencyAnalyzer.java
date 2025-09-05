package com.vividcodes.graphrag.service;

import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.vividcodes.graphrag.model.graph.AnnotationNode;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.FieldNode;
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

        // Create USES relationship for main type
        relationshipManager.createFieldTypeUsesRelationship(
            currentClass, fieldTypeClass, fieldName, fieldTypeName);
        LOGGER.debug("Created USES relationship for field type: {} -> {} (field: {} type: {})", 
                   currentClass.getName(), simpleTypeName, fieldName, fieldTypeName);
        
        // Handle generic type parameters
        detectGenericTypeDependencies(fieldTypeName, currentClass, "field", fieldName, importedClasses);
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

            // Create USES relationship for main return type
            relationshipManager.createMethodSignatureUsesRelationship(
                currentClass, returnTypeClass, methodName, "return_type", returnType);
            LOGGER.debug("Created USES relationship for return type: {} -> {} (method: {} returns: {})", 
                       currentClass.getName(), simpleReturnType, methodName, returnType);
            
            // Handle generic type parameters in return type
            detectGenericTypeDependencies(returnType, currentClass, "method_return", methodName, importedClasses);
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

                // Create USES relationship for main parameter type
                relationshipManager.createMethodSignatureUsesRelationship(
                    currentClass, paramTypeClass, methodName, "parameter_type", 
                    "param: " + paramName + " type: " + paramType);
                LOGGER.debug("Created USES relationship for parameter type: {} -> {} (method: {} param: {} type: {})", 
                           currentClass.getName(), simpleParamType, methodName, paramName, paramType);
                
                // Handle generic type parameters in parameter type
                detectGenericTypeDependencies(paramType, currentClass, "method_param", 
                    methodName + "." + paramName, importedClasses);
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
        // Determine if this is truly an external class using the same logic as regular class creation
        final boolean isExternal = typeResolver.isExternalClass(fullyQualifiedName);
        
        if (isExternal) {
            // Create external class node for framework/library classes
            return nodeFactory.createExternalClassNode(simpleClassName, fullyQualifiedName, graphService);
        } else {
            // Create a placeholder internal class node for application classes that weren't parsed yet
            // This can happen when a class is referenced but not included in the parsing scope
            return createPlaceholderInternalClassNode(simpleClassName, fullyQualifiedName);
        }
    }
    
    /**
     * Create a placeholder node for internal classes that are referenced but not parsed.
     * This maintains consistency with the isExternal property.
     */
    private ClassNode createPlaceholderInternalClassNode(final String simpleClassName, final String fullyQualifiedName) {
        final String packageName = typeResolver.extractPackageName(fullyQualifiedName);
        
        // Create internal class node (similar to createExternalClassNode but with isExternal = false)
        final ClassNode internalClass = new ClassNode(simpleClassName, packageName, "");
        internalClass.setFullyQualifiedName(fullyQualifiedName);
        internalClass.setIsExternal(false); // This is the key difference
        
        // Set timestamps
        final LocalDateTime now = LocalDateTime.now();
        internalClass.setCreatedAt(now);
        internalClass.setUpdatedAt(now);
        
        // Save to graph database
        graphService.saveClass(internalClass);
        
        return internalClass;
    }

    /**
     * Process annotation expressions and create annotation nodes and USES relationships.
     * 
     * @param annotationExpr The annotation expression to process
     * @param targetElement The element annotated (class, method, field)
     * @param targetType The type of target ("class", "method", "field", "parameter")
     * @param importedClasses Map of imported classes for annotation resolution
     */
    public void processAnnotation(final AnnotationExpr annotationExpr,
                                final Object targetElement,
                                final String targetType,
                                final Map<String, String> importedClasses) {
        if (annotationExpr == null || targetElement == null) {
            return;
        }

        final String annotationName = annotationExpr.getNameAsString();
        LOGGER.debug("Processing annotation: {} on {}", annotationName, targetType);

        // Resolve annotation fully qualified name
        final String fullyQualifiedName = typeResolver.resolveClassName(annotationName, importedClasses);
        
        // Create annotation node
        final AnnotationNode annotationNode = new AnnotationNode(annotationName, fullyQualifiedName);
        annotationNode.setTargetType(targetType);
        
        // Set framework properties
        annotationNode.setIsFramework(isFrameworkAnnotation(fullyQualifiedName));
        annotationNode.setFrameworkType(determineFrameworkType(fullyQualifiedName));
        
        // Extract annotation attributes based on type
        if (annotationExpr instanceof MarkerAnnotationExpr) {
            // No attributes for marker annotations
            LOGGER.debug("Marker annotation: {}", annotationName);
        } else if (annotationExpr instanceof SingleMemberAnnotationExpr) {
            final SingleMemberAnnotationExpr singleMember = (SingleMemberAnnotationExpr) annotationExpr;
            final String value = cleanAttributeValue(singleMember.getMemberValue().toString());
            annotationNode.addAttribute("value", value);
            LOGGER.debug("Single member annotation: {} with value: {}", annotationName, value);
        } else if (annotationExpr instanceof NormalAnnotationExpr) {
            final NormalAnnotationExpr normalAnnotation = (NormalAnnotationExpr) annotationExpr;
            normalAnnotation.getPairs().forEach(pair -> {
                final String value = cleanAttributeValue(pair.getValue().toString());
                annotationNode.addAttribute(pair.getNameAsString(), value);
                LOGGER.debug("Normal annotation: {} with attribute: {} = {}", 
                           annotationName, pair.getNameAsString(), value);
            });
        }

        // Save annotation node
        graphService.saveAnnotation(annotationNode);

        // Create USES relationship based on target type
        final String context = createAnnotationContext(targetElement, targetType);
        if (targetElement instanceof ClassNode) {
            relationshipManager.createClassAnnotationUsesRelationship(
                (ClassNode) targetElement, annotationNode, context);
        } else if (targetElement instanceof MethodNode) {
            relationshipManager.createMethodAnnotationUsesRelationship(
                (MethodNode) targetElement, annotationNode, context);
        } else if (targetElement instanceof FieldNode) {
            relationshipManager.createFieldAnnotationUsesRelationship(
                (FieldNode) targetElement, annotationNode, context);
        }

        LOGGER.debug("Created annotation relationship: {} -> {} ({})", 
                   getElementName(targetElement), annotationName, targetType);
    }

    /**
     * Process parameter-level annotation expressions and create annotation nodes and USES relationships.
     * 
     * @param annotationExpr The annotation expression to process
     * @param parameter The parameter being annotated
     * @param methodNode The method containing the parameter
     * @param importedClasses Map of imported classes for annotation resolution
     */
    public void processParameterAnnotation(final AnnotationExpr annotationExpr,
                                          final Parameter parameter,
                                          final MethodNode methodNode,
                                          final Map<String, String> importedClasses) {
        if (annotationExpr == null || parameter == null || methodNode == null) {
            return;
        }

        final String annotationName = annotationExpr.getNameAsString();
        LOGGER.debug("Processing parameter annotation: {} on parameter: {}", annotationName, parameter.getName());

        // Resolve annotation fully qualified name
        final String fullyQualifiedName = typeResolver.resolveClassName(annotationName, importedClasses);
        
        // Create annotation node
        final AnnotationNode annotationNode = new AnnotationNode(annotationName, fullyQualifiedName);
        annotationNode.setTargetType("parameter");
        
        // Set framework properties
        annotationNode.setIsFramework(isFrameworkAnnotation(fullyQualifiedName));
        annotationNode.setFrameworkType(determineFrameworkType(fullyQualifiedName));
        
        // Extract annotation attributes based on type
        if (annotationExpr instanceof MarkerAnnotationExpr) {
            // No attributes for marker annotations
            LOGGER.debug("Parameter marker annotation: {}", annotationName);
        } else if (annotationExpr instanceof SingleMemberAnnotationExpr) {
            final SingleMemberAnnotationExpr singleMember = (SingleMemberAnnotationExpr) annotationExpr;
            final String value = cleanAttributeValue(singleMember.getMemberValue().toString());
            annotationNode.addAttribute("value", value);
            LOGGER.debug("Parameter single member annotation: {} with value: {}", annotationName, value);
        } else if (annotationExpr instanceof NormalAnnotationExpr) {
            final NormalAnnotationExpr normalAnnotation = (NormalAnnotationExpr) annotationExpr;
            normalAnnotation.getPairs().forEach(pair -> {
                final String value = cleanAttributeValue(pair.getValue().toString());
                annotationNode.addAttribute(pair.getNameAsString(), value);
                LOGGER.debug("Parameter normal annotation: {} with attribute: {} = {}", 
                           annotationName, pair.getNameAsString(), value);
            });
        }

        // Save annotation node
        graphService.saveAnnotation(annotationNode);

        // Create USES relationship from method to annotation for parameter
        final String context = "parameter: " + parameter.getName() + " (method: " + methodNode.getName() + ")";
        relationshipManager.createMethodAnnotationUsesRelationship(methodNode, annotationNode, context);

        LOGGER.debug("Created parameter annotation relationship: method {} -> annotation {} for parameter {}", 
                   methodNode.getName(), annotationName, parameter.getName());
    }

    /**
     * Detect and create relationships for generic type parameters.
     * 
     * @param typeDeclaration The type declaration that may contain generics
     * @param currentClass The class containing the usage
     * @param usageContext The context of usage (field, method_return, method_param)
     * @param elementName The name of the element (field name, method name, etc.)
     * @param importedClasses Map of imported classes for resolution
     */
    public void detectGenericTypeDependencies(final String typeDeclaration,
                                            final ClassNode currentClass,
                                            final String usageContext,
                                            final String elementName,
                                            final Map<String, String> importedClasses) {
        if (typeDeclaration == null || currentClass == null) {
            return;
        }
        
        // Extract generic type arguments
        final java.util.List<String> typeArguments = typeResolver.extractGenericTypeArguments(typeDeclaration);
        
        if (!typeArguments.isEmpty()) {
            LOGGER.debug("Found {} generic type arguments in {}: {}", 
                       typeArguments.size(), typeDeclaration, typeArguments);
            
            typeArguments.forEach(typeArgument -> {
                // Resolve fully qualified name
                final String fullyQualifiedName = typeResolver.resolveClassName(typeArgument, importedClasses);
                
                // Create or get class node for the generic type argument
                final ClassNode genericTypeClass = getOrCreateClassNode(typeArgument, fullyQualifiedName);
                
                // Create USES relationship with generic_param type
                final String context = createGenericTypeContext(usageContext, elementName, typeDeclaration);
                relationshipManager.createGenericTypeUsesRelationship(
                    currentClass, genericTypeClass, context, typeArgument);
                
                LOGGER.debug("Created USES relationship for generic type: {} -> {} (context: {})", 
                           currentClass.getName(), typeArgument, context);
                
                // Recursively check for nested generics in this type argument
                if (typeArgument.contains("<")) {
                    detectGenericTypeDependencies(typeArgument, currentClass, 
                        "nested_generic", elementName, importedClasses);
                }
            });
        }
    }
    
    /**
     * Create context description for generic type usage.
     * 
     * @param usageContext The usage context
     * @param elementName The element name
     * @param typeDeclaration The full type declaration
     * @return Context description
     */
    private String createGenericTypeContext(final String usageContext, 
                                          final String elementName, 
                                          final String typeDeclaration) {
        return String.format("%s %s <%s>", usageContext, elementName, typeDeclaration);
    }
    
    /**
     * Create context string for annotation usage.
     * 
     * @param targetElement The annotated element
     * @param targetType The target type
     * @return Context description
     */
    private String createAnnotationContext(final Object targetElement, final String targetType) {
        switch (targetType) {
            case "class":
                return "class-level annotation";
            case "method":
                if (targetElement instanceof MethodNode) {
                    return "method: " + ((MethodNode) targetElement).getName();
                }
                return "method-level annotation";
            case "field":
                if (targetElement instanceof FieldNode) {
                    return "field: " + ((FieldNode) targetElement).getName();
                }
                return "field-level annotation";
            default:
                return targetType + "-level annotation";
        }
    }

    /**
     * Get element name for logging purposes.
     * 
     * @param element The element (ClassNode, MethodNode, or FieldNode)
     * @return The element name
     */
    private String getElementName(final Object element) {
        if (element instanceof ClassNode) {
            return ((ClassNode) element).getName();
        } else if (element instanceof MethodNode) {
            return ((MethodNode) element).getName();
        } else if (element instanceof FieldNode) {
            return ((FieldNode) element).getName();
        }
        return element.toString();
    }
    
    /**
     * Determine if an annotation is a framework annotation.
     * 
     * @param fullyQualifiedName The fully qualified annotation name
     * @return true if it's a framework annotation, false otherwise
     */
    private boolean isFrameworkAnnotation(final String fullyQualifiedName) {
        return fullyQualifiedName.startsWith("java.lang.")
            || fullyQualifiedName.startsWith("java.lang.annotation.")
            || fullyQualifiedName.startsWith("org.springframework.")
            || fullyQualifiedName.startsWith("org.junit.")
            || fullyQualifiedName.startsWith("jakarta.")
            || fullyQualifiedName.startsWith("javax.")
            || fullyQualifiedName.startsWith("com.fasterxml.jackson.");
    }
    
    /**
     * Determine the framework type based on the annotation package.
     * 
     * @param fullyQualifiedName The fully qualified annotation name
     * @return The framework type or null if not a framework annotation
     */
    private String determineFrameworkType(final String fullyQualifiedName) {
        if (fullyQualifiedName.startsWith("java.lang.")) {
            return "Java";
        } else if (fullyQualifiedName.startsWith("org.springframework.")) {
            return "Spring";
        } else if (fullyQualifiedName.startsWith("org.junit.") ||
                   fullyQualifiedName.startsWith("junit.")) {
            return "JUnit";
        } else if (fullyQualifiedName.startsWith("jakarta.validation.")) {
            return "Validation";
        } else if (fullyQualifiedName.startsWith("javax.validation.")) {
            return "Validation";
        } else if (fullyQualifiedName.startsWith("jakarta.persistence.")) {
            return "JPA";
        } else if (fullyQualifiedName.startsWith("javax.persistence.")) {
            return "JPA";
        } else if (fullyQualifiedName.startsWith("com.fasterxml.jackson.")) {
            return "Jackson";
        }
        return null;
    }
    
    /**
     * Clean attribute values by removing quotes and unnecessary characters.
     * 
     * @param rawValue The raw attribute value from JavaParser
     * @return The cleaned attribute value
     */
    private String cleanAttributeValue(final String rawValue) {
        if (rawValue == null) {
            return null;
        }
        
        String cleaned = rawValue.trim();
        
        // Remove surrounding quotes if present
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\""))
            || (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        
        return cleaned;
    }
}
