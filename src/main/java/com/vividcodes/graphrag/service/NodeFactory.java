package com.vividcodes.graphrag.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.vividcodes.graphrag.model.dto.RepositoryMetadata;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.FieldNode;
import com.vividcodes.graphrag.model.graph.MethodNode;
import com.vividcodes.graphrag.model.graph.SubProjectNode;


/**
 * Factory service for creating graph nodes from JavaParser AST elements.
 * Handles the creation of ClassNode, MethodNode, and FieldNode instances with proper metadata.
 */
@Component
public class NodeFactory {

    private final TypeResolver typeResolver;

    @Autowired
    public NodeFactory(final TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    /**
     * Create a ClassNode from a JavaParser ClassOrInterfaceDeclaration.
     * 
     * @param classDecl The class declaration from JavaParser
     * @param packageName The package name containing this class
     * @param filePath The file path where this class is defined
     * @param repositoryMetadata Repository metadata for this class
     * @param containingSubProject The sub-project containing this class (may be null)
     * @return A fully configured ClassNode
     */
    public ClassNode createClassNode(final ClassOrInterfaceDeclaration classDecl,
                                   final String packageName,
                                   final String filePath,
                                   final RepositoryMetadata repositoryMetadata,
                                   final SubProjectNode containingSubProject) {
        final String className = classDecl.getNameAsString();
        final String fullyQualifiedName = packageName.isEmpty() ? className : packageName + "." + className;
        
        final ClassNode classNode = new ClassNode(className, packageName, filePath);
        
        // Set basic properties
        classNode.setVisibility(getVisibility(classDecl));
        classNode.setModifiers(getModifiers(classDecl));
        classNode.setIsInterface(classDecl.isInterface());
        classNode.setIsEnum(classDecl.isEnumDeclaration());
        classNode.setIsAnnotation(classDecl.isAnnotationDeclaration());
        classNode.setFullyQualifiedName(fullyQualifiedName);
        classNode.setIsExternal(typeResolver.isExternalClass(fullyQualifiedName));
        
        // Set line numbers if available
        if (classDecl.getBegin().isPresent()) {
            classNode.setLineStart(classDecl.getBegin().get().line);
        }
        if (classDecl.getEnd().isPresent()) {
            classNode.setLineEnd(classDecl.getEnd().get().line);
        }
        
        // Set repository metadata if available
        if (repositoryMetadata != null) {
            classNode.setRepositoryId(repositoryMetadata.getRepositoryId());
            classNode.setRepositoryName(repositoryMetadata.getRepositoryName());
            classNode.setRepositoryUrl(repositoryMetadata.getRepositoryUrl());
            classNode.setBranch(repositoryMetadata.getBranch());
            classNode.setCommitHash(repositoryMetadata.getCommitHash());
            classNode.setCommitDate(repositoryMetadata.getCommitDate());
            classNode.setFileRelativePath(repositoryMetadata.getFileRelativePath());
        }
        
        // Note: SubProject relationship will be handled by RelationshipManager
        // ClassNode doesn't have a direct subProjectId field
        
        // Set timestamps
        final LocalDateTime now = LocalDateTime.now();
        classNode.setCreatedAt(now);
        classNode.setUpdatedAt(now);
        
        return classNode;
    }

    /**
     * Create a MethodNode from a JavaParser MethodDeclaration.
     * 
     * @param methodDecl The method declaration from JavaParser
     * @param className The name of the class containing this method
     * @param packageName The package name containing this method
     * @param filePath The file path where this method is defined
     * @return A fully configured MethodNode
     */
    public MethodNode createMethodNode(final MethodDeclaration methodDecl,
                                     final String className,
                                     final String packageName,
                                     final String filePath) {
        final String methodName = methodDecl.getNameAsString();
        
        final MethodNode methodNode = new MethodNode(methodName, className, packageName, filePath);
        
        // Set basic properties
        methodNode.setVisibility(getVisibility(methodDecl));
        methodNode.setModifiers(getModifiers(methodDecl));
        methodNode.setReturnType(methodDecl.getType().toString());
        methodNode.setParameters(extractParameters(methodDecl));
        
        // Set line numbers if available
        if (methodDecl.getBegin().isPresent()) {
            methodNode.setLineStart(methodDecl.getBegin().get().line);
        }
        if (methodDecl.getEnd().isPresent()) {
            methodNode.setLineEnd(methodDecl.getEnd().get().line);
        }
        
        // Set timestamps
        final LocalDateTime now = LocalDateTime.now();
        methodNode.setCreatedAt(now);
        methodNode.setUpdatedAt(now);
        
        return methodNode;
    }

    /**
     * Create a FieldNode from field information.
     * 
     * @param fieldName The name of the field
     * @param fieldDecl The field declaration from JavaParser
     * @param className The name of the class containing this field
     * @param packageName The package name containing this field
     * @param filePath The file path where this field is defined
     * @return A fully configured FieldNode
     */
    public FieldNode createFieldNode(final String fieldName,
                                   final FieldDeclaration fieldDecl,
                                   final String className,
                                   final String packageName,
                                   final String filePath) {
        final FieldNode fieldNode = new FieldNode(fieldName, className, packageName, filePath);
        
        // Set basic properties
        fieldNode.setVisibility(getVisibility(fieldDecl));
        fieldNode.setModifiers(getModifiers(fieldDecl));
        fieldNode.setType(fieldDecl.getElementType().toString());
        
        // Set line number if available (FieldNode uses setLineNumber, not setLineStart/End)
        if (fieldDecl.getBegin().isPresent()) {
            fieldNode.setLineNumber(fieldDecl.getBegin().get().line);
        }
        
        // Set timestamps
        final LocalDateTime now = LocalDateTime.now();
        fieldNode.setCreatedAt(now);
        fieldNode.setUpdatedAt(now);
        
        return fieldNode;
    }

    /**
     * Create or retrieve a ClassNode for external dependencies.
     * 
     * @param simpleClassName The simple class name
     * @param fullyQualifiedName The fully qualified class name
     * @param graphService The graph service for saving/retrieving nodes
     * @return A ClassNode for the external class
     */
    public ClassNode createExternalClassNode(final String simpleClassName,
                                           final String fullyQualifiedName,
                                           final GraphService graphService) {
        // Determine package name from fully qualified name
        final String packageName = typeResolver.extractPackageName(fullyQualifiedName);
        
        // Create external class node
        final ClassNode externalClass = new ClassNode(simpleClassName, packageName, "");
        externalClass.setFullyQualifiedName(fullyQualifiedName);
        externalClass.setIsExternal(true);
        
        // Set timestamps
        final LocalDateTime now = LocalDateTime.now();
        externalClass.setCreatedAt(now);
        externalClass.setUpdatedAt(now);
        
        // Save to graph database
        graphService.saveClass(externalClass);
        
        return externalClass;
    }

    /**
     * Extract visibility modifier from a declaration.
     * 
     * @param declaration The JavaParser declaration
     * @return The visibility as a string
     */
    private String getVisibility(final com.github.javaparser.ast.body.BodyDeclaration<?> declaration) {
        // For now, return a default visibility - we'll enhance this later
        return "PUBLIC";
    }

    /**
     * Extract modifiers from a declaration.
     * 
     * @param declaration The JavaParser declaration
     * @return List of modifiers as strings
     */
    private List<String> getModifiers(final com.github.javaparser.ast.body.BodyDeclaration<?> declaration) {
        // For now, return empty list - we'll enhance this later
        return new ArrayList<>();
    }

    /**
     * Extract parameter information from a method declaration.
     * 
     * @param methodDecl The method declaration
     * @return List of parameter strings
     */
    private List<String> extractParameters(final MethodDeclaration methodDecl) {
        final List<String> parameters = new ArrayList<>();
        
        methodDecl.getParameters().forEach(param -> {
            final String paramType = param.getType().toString();
            final String paramName = param.getNameAsString();
            parameters.add(paramType + " " + paramName);
        });
        
        return parameters;
    }
}
