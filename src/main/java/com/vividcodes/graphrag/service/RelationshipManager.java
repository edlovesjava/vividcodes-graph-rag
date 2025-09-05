package com.vividcodes.graphrag.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.vividcodes.graphrag.model.graph.AnnotationNode;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.FieldNode;
import com.vividcodes.graphrag.model.graph.MethodNode;
import com.vividcodes.graphrag.model.graph.PackageNode;
import com.vividcodes.graphrag.model.graph.SubProjectNode;

/**
 * Service responsible for creating and managing relationships between graph nodes.
 * Handles CONTAINS relationships (hierarchical) and USES relationships (dependencies).
 */
@Component
public class RelationshipManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelationshipManager.class);

    private final GraphService graphService;

    @Autowired
    public RelationshipManager(final GraphService graphService) {
        this.graphService = graphService;
    }

    /**
     * Create hierarchical CONTAINS relationships for a class and its elements.
     * 
     * @param currentClass The class node
     * @param currentPackage The package containing the class (may be null)
     * @param containingSubProject The sub-project containing the class (may be null)
     * @param currentMethods List of methods in the class
     * @param currentFields List of fields in the class
     */
    public void createContainsRelationships(final ClassNode currentClass,
                                          final PackageNode currentPackage,
                                          final SubProjectNode containingSubProject,
                                          final List<MethodNode> currentMethods,
                                          final List<FieldNode> currentFields) {
        if (currentClass == null) {
            LOGGER.warn("Cannot create CONTAINS relationships: currentClass is null");
            return;
        }

        // Create CONTAINS relationship from package to class
        if (currentPackage != null) {
            graphService.createRelationship(currentPackage.getId(), currentClass.getId(), "CONTAINS");
            LOGGER.debug("Created CONTAINS relationship: package {} -> class {}", 
                       currentPackage.getName(), currentClass.getName());
        }

        // Create CONTAINS relationships from class to methods
        if (currentMethods != null) {
            for (final MethodNode method : currentMethods) {
                graphService.createRelationship(currentClass.getId(), method.getId(), "CONTAINS");
                LOGGER.debug("Created CONTAINS relationship: {} -> {}", 
                           currentClass.getName(), method.getName());
            }
        }

        // Create CONTAINS relationships from class to fields
        if (currentFields != null) {
            for (final FieldNode field : currentFields) {
                graphService.createRelationship(currentClass.getId(), field.getId(), "CONTAINS");
                LOGGER.debug("Created CONTAINS relationship: {} -> {}", 
                           currentClass.getName(), field.getName());
            }
        }

        // Create CONTAINS relationships from SubProject to CodeElements
        if (containingSubProject != null) {
            // SubProject -> Class
            graphService.createRelationship(containingSubProject.getId(), currentClass.getId(), "CONTAINS");
            LOGGER.debug("Created CONTAINS relationship: SubProject {} -> Class {}", 
                       containingSubProject.getName(), currentClass.getName());

            // SubProject -> Package
            if (currentPackage != null) {
                graphService.createRelationship(containingSubProject.getId(), currentPackage.getId(), "CONTAINS");
                LOGGER.debug("Created CONTAINS relationship: SubProject {} -> Package {}", 
                           containingSubProject.getName(), currentPackage.getName());
            }

            // SubProject -> Methods
            if (currentMethods != null) {
                for (final MethodNode method : currentMethods) {
                    graphService.createRelationship(containingSubProject.getId(), method.getId(), "CONTAINS");
                    LOGGER.debug("Created CONTAINS relationship: SubProject {} -> Method {}", 
                               containingSubProject.getName(), method.getName());
                }
            }

            // SubProject -> Fields
            if (currentFields != null) {
                for (final FieldNode field : currentFields) {
                    graphService.createRelationship(containingSubProject.getId(), field.getId(), "CONTAINS");
                    LOGGER.debug("Created CONTAINS relationship: SubProject {} -> Field {}", 
                               containingSubProject.getName(), field.getName());
                }
            }
        }
    }

    /**
     * Create a USES relationship between two classes with metadata.
     * 
     * @param fromClass The source class
     * @param toClass The target class
     * @param dependencyType The type of dependency (e.g., "import", "instantiation", "static_method_call")
     * @param context Additional context information
     */
    public void createUsesRelationship(final ClassNode fromClass,
                                     final ClassNode toClass,
                                     final String dependencyType,
                                     final String context) {
        if (fromClass == null || toClass == null) {
            LOGGER.warn("Cannot create USES relationship: fromClass or toClass is null");
            return;
        }

        final Map<String, Object> properties = Map.of(
            "type", dependencyType,
            "context", context != null ? context : ""
        );

        graphService.createRelationship(fromClass.getId(), toClass.getId(), "USES", properties);
        LOGGER.debug("Created USES relationship: {} -> {} ({})", 
                   fromClass.getName(), toClass.getName(), dependencyType);
    }

    /**
     * Create a USES relationship for import dependencies.
     * 
     * @param fromClass The class that imports
     * @param toClass The imported class
     * @param fullyQualifiedName The fully qualified name of the imported class
     */
    public void createImportUsesRelationship(final ClassNode fromClass,
                                           final ClassNode toClass,
                                           final String fullyQualifiedName) {
        createUsesRelationship(fromClass, toClass, "import", fullyQualifiedName);
    }

    /**
     * Create a USES relationship for static method calls.
     * 
     * @param fromClass The class making the static call
     * @param toClass The class containing the static method
     * @param methodName The name of the static method
     */
    public void createStaticMethodUsesRelationship(final ClassNode fromClass,
                                                 final ClassNode toClass,
                                                 final String methodName) {
        final String context = "static method call: " + methodName;
        createUsesRelationship(fromClass, toClass, "static_method_call", context);
    }

    /**
     * Create a USES relationship for object instantiation.
     * 
     * @param fromClass The class creating the instance
     * @param toClass The class being instantiated
     * @param context Additional context (e.g., field name, method name)
     */
    public void createInstantiationUsesRelationship(final ClassNode fromClass,
                                                   final ClassNode toClass,
                                                   final String context) {
        createUsesRelationship(fromClass, toClass, "instantiation", context);
    }

    /**
     * Create a USES relationship for field type dependencies.
     * 
     * @param fromClass The class containing the field
     * @param toClass The class used as field type
     * @param fieldName The name of the field
     * @param fieldType The type of the field
     */
    public void createFieldTypeUsesRelationship(final ClassNode fromClass,
                                              final ClassNode toClass,
                                              final String fieldName,
                                              final String fieldType) {
        final String context = "field: " + fieldName + " type: " + fieldType;
        createUsesRelationship(fromClass, toClass, "field_type", context);
    }

    /**
     * Create a USES relationship for method signature dependencies.
     * 
     * @param fromClass The class containing the method
     * @param toClass The class used in method signature
     * @param methodName The name of the method
     * @param signatureType The type of signature dependency ("return_type" or "parameter_type")
     * @param typeInfo Additional type information
     */
    public void createMethodSignatureUsesRelationship(final ClassNode fromClass,
                                                    final ClassNode toClass,
                                                    final String methodName,
                                                    final String signatureType,
                                                    final String typeInfo) {
        final String context = "method: " + methodName + " " + signatureType + ": " + typeInfo;
        createUsesRelationship(fromClass, toClass, signatureType, context);
    }

    /**
     * Create inheritance relationships (EXTENDS/IMPLEMENTS).
     * 
     * @param childClass The child class
     * @param parentClass The parent class or interface
     * @param relationshipType Either "EXTENDS" or "IMPLEMENTS"
     */
    public void createInheritanceRelationship(final ClassNode childClass,
                                            final ClassNode parentClass,
                                            final String relationshipType) {
        if (childClass == null || parentClass == null) {
            LOGGER.warn("Cannot create {} relationship: childClass or parentClass is null", relationshipType);
            return;
        }

        graphService.createRelationship(childClass.getId(), parentClass.getId(), relationshipType);
        LOGGER.debug("Created {} relationship: {} -> {}", 
                   relationshipType, childClass.getName(), parentClass.getName());
    }

    /**
     * Create a USES relationship between a method and a field.
     * 
     * @param method The method using the field
     * @param field The field being used
     */
    public void createMethodFieldUsesRelationship(final MethodNode method, final FieldNode field) {
        if (method == null || field == null) {
            LOGGER.warn("Cannot create method-field USES relationship: method or field is null");
            return;
        }

        graphService.createRelationship(method.getId(), field.getId(), "USES");
        LOGGER.debug("Created USES relationship: {} -> {}", method.getName(), field.getName());
    }

    /**
     * Create a USES relationship from a class to an annotation.
     * 
     * @param fromClass The class using the annotation
     * @param annotation The annotation being used
     * @param context Additional context information
     */
    public void createClassAnnotationUsesRelationship(final ClassNode fromClass,
                                                    final AnnotationNode annotation,
                                                    final String context) {
        if (fromClass == null || annotation == null) {
            LOGGER.warn("Cannot create class-annotation USES relationship: fromClass or annotation is null");
            return;
        }

        final Map<String, Object> properties = Map.of(
            "type", "annotation",
            "targetType", "class",
            "context", context != null ? context : "",
            "annotationAttributes", annotation.getAttributes().toString(),
            "frameworkType", annotation.getFrameworkType() != null ? annotation.getFrameworkType() : ""
        );

        graphService.createRelationship(fromClass.getId(), annotation.getId(), "USES", properties);
        LOGGER.debug("Created annotation USES relationship: {} -> {} (class-level)", 
                   fromClass.getName(), annotation.getName());
    }

    /**
     * Create a USES relationship from a method to an annotation.
     * 
     * @param fromMethod The method using the annotation
     * @param annotation The annotation being used
     * @param context Additional context information
     */
    public void createMethodAnnotationUsesRelationship(final MethodNode fromMethod,
                                                     final AnnotationNode annotation,
                                                     final String context) {
        if (fromMethod == null || annotation == null) {
            LOGGER.warn("Cannot create method-annotation USES relationship: fromMethod or annotation is null");
            return;
        }

        final Map<String, Object> properties = Map.of(
            "type", "annotation",
            "targetType", "method",
            "context", context != null ? context : "",
            "annotationAttributes", annotation.getAttributes().toString(),
            "frameworkType", annotation.getFrameworkType() != null ? annotation.getFrameworkType() : ""
        );

        graphService.createRelationship(fromMethod.getId(), annotation.getId(), "USES", properties);
        LOGGER.debug("Created annotation USES relationship: {} -> {} (method-level)", 
                   fromMethod.getName(), annotation.getName());
    }

    /**
     * Create a USES relationship from a field to an annotation.
     * 
     * @param fromField The field using the annotation
     * @param annotation The annotation being used
     * @param context Additional context information
     */
    public void createFieldAnnotationUsesRelationship(final FieldNode fromField,
                                                    final AnnotationNode annotation,
                                                    final String context) {
        if (fromField == null || annotation == null) {
            LOGGER.warn("Cannot create field-annotation USES relationship: fromField or annotation is null");
            return;
        }

        final Map<String, Object> properties = Map.of(
            "type", "annotation",
            "targetType", "field",
            "context", context != null ? context : "",
            "annotationAttributes", annotation.getAttributes().toString(),
            "frameworkType", annotation.getFrameworkType() != null ? annotation.getFrameworkType() : ""
        );

        graphService.createRelationship(fromField.getId(), annotation.getId(), "USES", properties);
        LOGGER.debug("Created annotation USES relationship: {} -> {} (field-level)", 
                   fromField.getName(), annotation.getName());
    }
    
    /**
     * Create USES relationship for generic type parameter dependencies.
     * 
     * @param sourceClass The class that uses the generic type
     * @param targetClass The class representing the generic type argument
     * @param context The usage context description
     * @param typeArgument The generic type argument name
     */
    public void createGenericTypeUsesRelationship(final ClassNode sourceClass, 
                                                 final ClassNode targetClass, 
                                                 final String context,
                                                 final String typeArgument) {
        if (sourceClass == null || targetClass == null) {
            LOGGER.warn("Cannot create generic type USES relationship: source or target class is null");
            return;
        }

        final Map<String, Object> properties = new HashMap<>();
        properties.put("type", "generic_param");
        properties.put("context", context != null ? context : "generic type usage");
        properties.put("typeArgument", typeArgument);
        properties.put("fullyQualifiedName", targetClass.getFullyQualifiedName());
        properties.put("isExternal", targetClass.getIsExternal());
        properties.put("count", 1);

        graphService.createRelationship(sourceClass.getId(), targetClass.getId(), "USES", properties);
        LOGGER.debug("Created generic type USES relationship: {} -> {} ({})", 
                   sourceClass.getName(), targetClass.getName(), typeArgument);
    }
}
