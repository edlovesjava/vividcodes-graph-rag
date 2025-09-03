package com.vividcodes.graphrag.service;

import org.springframework.stereotype.Component;

/**
 * Utility service for resolving and analyzing Java types.
 * Handles type name extraction, primitive type detection, and external class identification.
 */
@Component
public class TypeResolver {

    /**
     * Extract simple class name from fully qualified name.
     * 
     * @param fullyQualifiedName The fully qualified class name (e.g., "java.util.List")
     * @return The simple class name (e.g., "List")
     */
    public String extractSimpleClassName(final String fullyQualifiedName) {
        if (fullyQualifiedName == null || fullyQualifiedName.isEmpty()) {
            return "";
        }
        final int lastDotIndex = fullyQualifiedName.lastIndexOf('.');
        return lastDotIndex >= 0 ? fullyQualifiedName.substring(lastDotIndex + 1) : fullyQualifiedName;
    }

    /**
     * Extract simple type name from type declaration, handling generics and arrays.
     * 
     * @param typeDeclaration The type declaration string (e.g., "List<String>", "String[]")
     * @return The simple type name (e.g., "List", "String")
     */
    public String extractSimpleTypeName(final String typeDeclaration) {
        if (typeDeclaration == null || typeDeclaration.isEmpty()) {
            return "";
        }
        
        String typeName = typeDeclaration.trim();
        
        // Remove generic type parameters (e.g., "List<String>" -> "List")
        final int genericStart = typeName.indexOf('<');
        if (genericStart >= 0) {
            typeName = typeName.substring(0, genericStart);
        }
        
        // Remove array brackets (e.g., "String[]" -> "String")
        final int arrayStart = typeName.indexOf('[');
        if (arrayStart >= 0) {
            typeName = typeName.substring(0, arrayStart);
        }
        
        // Extract simple name from fully qualified name
        return extractSimpleClassName(typeName.trim());
    }

    /**
     * Check if a type name represents a primitive type.
     * 
     * @param typeName The type name to check
     * @return true if the type is a primitive type, false otherwise
     */
    public boolean isPrimitiveType(final String typeName) {
        return typeName.equals("int") || typeName.equals("long") || typeName.equals("double") ||
               typeName.equals("float") || typeName.equals("boolean") || typeName.equals("char") ||
               typeName.equals("byte") || typeName.equals("short") || typeName.equals("void");
    }

    /**
     * Determine if a class is external (from common frameworks/libraries).
     * 
     * @param fullyQualifiedName The fully qualified class name
     * @return true if the class is considered external, false otherwise
     */
    public boolean isExternalClass(final String fullyQualifiedName) {
        // Handle common Java classes that appear without package names in code
        if (isCommonJavaClass(fullyQualifiedName)) {
            return true;
        }
        
        // Basic heuristic: consider classes external if they're from common frameworks/libraries
        return fullyQualifiedName.startsWith("java.") ||
               fullyQualifiedName.startsWith("javax.") ||
               fullyQualifiedName.startsWith("org.springframework.") ||
               fullyQualifiedName.startsWith("org.slf4j.") ||
               fullyQualifiedName.startsWith("com.fasterxml.jackson.") ||
               fullyQualifiedName.startsWith("org.apache.") ||
               fullyQualifiedName.startsWith("com.google.");
    }
    
    /**
     * Check if a class name (without package) is a common Java standard library class.
     * These classes are automatically imported and often appear without package names.
     */
    private boolean isCommonJavaClass(final String className) {
        // Common java.lang classes (automatically imported)
        if (className.equals("String") || className.equals("Object") || className.equals("Class") ||
            className.equals("Integer") || className.equals("Long") || className.equals("Double") ||
            className.equals("Float") || className.equals("Boolean") || className.equals("Character") ||
            className.equals("Byte") || className.equals("Short") || className.equals("Number") ||
            className.equals("Exception") || className.equals("RuntimeException") || className.equals("Error") ||
            className.equals("Thread") || className.equals("Runnable") || className.equals("System") ||
            className.equals("Math") || className.equals("StringBuilder") || className.equals("StringBuffer")) {
            return true;
        }
        
        // Common collection classes that might appear without full package names
        if (className.equals("List") || className.equals("Map") || className.equals("Set") ||
            className.equals("Collection") || className.equals("ArrayList") || className.equals("HashMap") ||
            className.equals("HashSet") || className.equals("LinkedList") || className.equals("TreeMap") ||
            className.equals("TreeSet") || className.equals("Optional") || className.equals("Stream")) {
            return true;
        }
        
        return false;
    }

    /**
     * Resolve package name from fully qualified class name.
     * 
     * @param fullyQualifiedName The fully qualified class name
     * @return The package name, or empty string if no package
     */
    public String extractPackageName(final String fullyQualifiedName) {
        if (fullyQualifiedName == null || fullyQualifiedName.isEmpty()) {
            return "";
        }
        final int lastDotIndex = fullyQualifiedName.lastIndexOf('.');
        return lastDotIndex >= 0 ? fullyQualifiedName.substring(0, lastDotIndex) : "";
    }

    /**
     * Check if a fully qualified name represents a valid class name format.
     * 
     * @param fullyQualifiedName The name to validate
     * @return true if the name appears to be a valid class name, false otherwise
     */
    public boolean isValidClassName(final String fullyQualifiedName) {
        if (fullyQualifiedName == null || fullyQualifiedName.trim().isEmpty()) {
            return false;
        }
        
        // Basic validation: should contain only letters, digits, dots, and underscores
        // Should not start or end with a dot
        final String trimmed = fullyQualifiedName.trim();
        return trimmed.matches("^[a-zA-Z_][a-zA-Z0-9_.]*[a-zA-Z0-9_]$") ||
               trimmed.matches("^[a-zA-Z_][a-zA-Z0-9_]*$"); // Single word class names
    }

    /**
     * Resolve a class name to its fully qualified name using imports.
     * 
     * @param className The simple class name
     * @param importedClasses Map of imported classes (className -> fullyQualifiedName)
     * @return The fully qualified name, or the simple name if not found in imports
     */
    public String resolveClassName(final String className, final java.util.Map<String, String> importedClasses) {
        return importedClasses.getOrDefault(className, className);
    }
}
