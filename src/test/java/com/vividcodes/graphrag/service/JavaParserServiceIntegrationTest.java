package com.vividcodes.graphrag.service;

import com.vividcodes.graphrag.config.ParserConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JavaParserServiceIntegrationTest {
    
    @Mock
    private GraphService graphService;
    
    private ParserConfig parserConfig;
    private JavaParserService javaParserService;
    
    @BeforeEach
    void setUp() {
        parserConfig = new ParserConfig();
        ReflectionTestUtils.setField(parserConfig, "includePrivate", false);
        ReflectionTestUtils.setField(parserConfig, "includeTests", false);
        ReflectionTestUtils.setField(parserConfig, "maxFileSize", 10 * 1024 * 1024L);
        
        javaParserService = new JavaParserService(parserConfig, graphService);
    }
    
    @Test
    void testParseSimpleJavaFile(@TempDir Path tempDir) throws Exception {
        // Create a simple Java file for testing
        Path testFile = tempDir.resolve("SimpleClass.java");
        String javaCode = """
            package test;
            
            public class SimpleClass {
                public void testMethod() {
                    System.out.println("Hello World");
                }
            }
            """;
        
        System.out.println("Creating file at: " + testFile.toAbsolutePath());
        Files.write(testFile, javaCode.getBytes());
        
        // Verify file was created
        assertTrue(Files.exists(testFile), "Test file should exist");
        System.out.println("File exists: " + Files.exists(testFile));
        System.out.println("File size: " + Files.size(testFile));
        
        // Parse the file
        javaParserService.parseDirectory(tempDir.toString());
        
        // Verify that the GraphService was called
        verify(graphService, atLeastOnce()).saveClass(any());
    }
} 