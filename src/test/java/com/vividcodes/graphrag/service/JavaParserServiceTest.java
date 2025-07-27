package com.vividcodes.graphrag.service;

import com.vividcodes.graphrag.config.ParserConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JavaParserServiceTest {
    
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
    void testParseDirectory_ValidPath() {
        // Get the test resources directory
        Path testResourcesPath = Paths.get("src", "test", "resources", "test-data");
        
        // This test will verify that the service can handle a valid directory
        assertDoesNotThrow(() -> {
            javaParserService.parseDirectory(testResourcesPath.toString());
        });
    }
    
    @Test
    void testParseDirectory_InvalidPath() {
        String invalidPath = "/non/existent/path";
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            javaParserService.parseDirectory(invalidPath);
        });
        
        assertTrue(exception.getMessage().contains("Source path does not exist") || 
                  exception.getCause().getMessage().contains("Source path does not exist"));
    }
    
    @Test
    void testParserConfig() {
        assertFalse(parserConfig.isIncludePrivate());
        assertFalse(parserConfig.isIncludeTests());
        assertEquals(10 * 1024 * 1024L, parserConfig.getMaxFileSize());
        assertEquals("java", parserConfig.getSupportedExtensions());
    }
} 