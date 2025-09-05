package com.vividcodes.graphrag.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for TypeResolver functionality, especially generic type extraction.
 */
public class TypeResolverTest {

    private TypeResolver typeResolver;

    @BeforeEach
    void setUp() {
        typeResolver = new TypeResolver();
    }

    @Test
    void shouldExtractSingleGenericTypeArgument() {
        // Arrange
        String typeDeclaration = "List<String>";

        // Act
        List<String> typeArguments = typeResolver.extractGenericTypeArguments(typeDeclaration);

        // Assert
        assertEquals(1, typeArguments.size());
        assertEquals("String", typeArguments.get(0));
    }

    @Test
    void shouldExtractMultipleGenericTypeArguments() {
        // Arrange
        String typeDeclaration = "Map<String, Object>";

        // Act
        List<String> typeArguments = typeResolver.extractGenericTypeArguments(typeDeclaration);

        // Assert
        assertEquals(2, typeArguments.size());
        assertEquals("String", typeArguments.get(0));
        assertEquals("Object", typeArguments.get(1));
    }

    @Test
    void shouldReturnEmptyListForNonGenericTypes() {
        // Arrange
        String typeDeclaration = "String";

        // Act
        List<String> typeArguments = typeResolver.extractGenericTypeArguments(typeDeclaration);

        // Assert
        assertTrue(typeArguments.isEmpty());
    }

    @Test
    void shouldHandleComplexNestedGenerics() {
        // Arrange
        String typeDeclaration = "Map<String, List<Object>>";

        // Act
        List<String> typeArguments = typeResolver.extractGenericTypeArguments(typeDeclaration);

        // Assert
        assertEquals(2, typeArguments.size());
        assertEquals("String", typeArguments.get(0));
        assertEquals("List", typeArguments.get(1));
    }

    @Test
    void shouldHandleWildcardTypes() {
        // Arrange
        String typeDeclaration = "List<? extends Number>";

        // Act
        List<String> typeArguments = typeResolver.extractGenericTypeArguments(typeDeclaration);

        // Assert
        assertEquals(1, typeArguments.size());
        assertEquals("? extends Number", typeArguments.get(0));
    }

    @Test
    void shouldSkipPrimitiveTypes() {
        // Arrange
        String typeDeclaration = "List<int>";

        // Act
        List<String> typeArguments = typeResolver.extractGenericTypeArguments(typeDeclaration);

        // Assert
        assertTrue(typeArguments.isEmpty());
    }
}
