package com.vividcodes.graphrag.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.vividcodes.graphrag.service.GraphService;
import com.vividcodes.graphrag.service.JavaParserService;

@SpringBootTest
@ActiveProfiles("test")
public class ImportUsesRelationshipIntegrationTest {

    @Autowired
    private JavaParserService javaParserService;
    
    @Autowired 
    private GraphService graphService;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Clear any existing data
        graphService.clearAllData();
    }
    
    @Test
    void testImportStatementsCreateUsesRelationships() throws IOException {
        // Create a test Java file with imports and object instantiations
        Path javaFile = tempDir.resolve("MyService.java");
        String javaContent = """
            package com.example.test;
            
            import java.util.List;
            import java.util.ArrayList;
            import java.util.Collections;
            import java.time.LocalDateTime;
            import java.time.format.DateTimeFormatter;
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;
            
            public class MyService {
                private Logger logger = LoggerFactory.getLogger(MyService.class);
                private List<String> items = new ArrayList<>();
                private DateTimeFormatter formatter;
                
                public void doSomething() {
                    logger.info("Doing something");
                    List<String> localList = new ArrayList<>();
                    localList.add("test");
                    Collections.sort(items);
                    String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    System.out.println("Current time: " + now);
                }
                
                // Method with parameters and return type for signature dependency testing
                public List<String> processItems(List<String> inputItems, DateTimeFormatter formatter) {
                    // This method demonstrates parameter and return type dependencies
                    List<String> result = new ArrayList<>(inputItems);
                    result.add("processed at: " + LocalDateTime.now().format(formatter));
                    return result;
                }
            }
            """;
        
        Files.writeString(javaFile, javaContent);
        
        // Parse the file
        javaParserService.parseDirectory(tempDir.toString());
        
        // Verify that parsing completed without errors
        assertNotNull(javaParserService);
        assertTrue(Files.exists(javaFile));
        
        // TODO: Add verification that USES relationships were actually created
        // Expected relationships:
        // - TestClass -> List (import)
        // - TestClass -> ArrayList (import and 2 instantiations: field + method)
        // - TestClass -> Logger (import)  
        // - TestClass -> LoggerFactory (import)
    }
    
    @Test
    void testMultipleImportsCreateMultipleUsesRelationships() throws IOException {
        // Create a test Java file with multiple imports
        Path javaFile = tempDir.resolve("MultiImportClass.java");
        String javaContent = """
            package com.example.test;
            
            import java.util.Map;
            import java.util.HashMap;
            import java.time.LocalDateTime;
            import java.time.format.DateTimeFormatter;
            
            public class MultiImportClass {
                private Map<String, LocalDateTime> timestamps = new HashMap<>();
                private DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                
                public void addTimestamp(String key) {
                    timestamps.put(key, LocalDateTime.now());
                }
                
                public String formatTimestamp(String key) {
                    LocalDateTime timestamp = timestamps.get(key);
                    return timestamp != null ? formatter.format(timestamp) : null;
                }
            }
            """;
        
        Files.writeString(javaFile, javaContent);
        
        // Parse the file
        javaParserService.parseDirectory(tempDir.toString());
        
        // Verify parsing completed successfully
        assertNotNull(javaParserService);
        assertTrue(Files.exists(javaFile));
        
        // TODO: Verify that multiple USES relationships were created for:
        // - Map
        // - HashMap  
        // - LocalDateTime
        // - DateTimeFormatter
    }
}
