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
import org.springframework.test.util.ReflectionTestUtils;
import com.vividcodes.graphrag.config.ParserConfig;
import com.vividcodes.graphrag.model.dto.CypherQueryRequest;
import com.vividcodes.graphrag.model.dto.QueryResult;
import com.vividcodes.graphrag.service.CypherQueryService;
import com.vividcodes.graphrag.service.GraphService;
import com.vividcodes.graphrag.service.JavaParserService;

@SpringBootTest
@ActiveProfiles("test")
public class AnnotationUsesRelationshipIntegrationTest {

    @Autowired
    private JavaParserService javaParserService;
    
    @Autowired 
    private GraphService graphService;
    
    @Autowired
    private CypherQueryService cypherQueryService;
    
    @Autowired
    private ParserConfig parserConfig;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Clear any existing data
        graphService.clearAllData();
        
        // Enable processing of private/package-private methods for JUnit test methods
        ReflectionTestUtils.setField(parserConfig, "includePrivate", true);
    }
    
    @Test
    void testSpringAnnotationsCreateUsesRelationships() throws IOException {
        // Create a test Java file with Spring annotations
        Path javaFile = tempDir.resolve("UserService.java");
        String javaContent = """
            package com.example.service;
            
            import org.springframework.stereotype.Service;
            import org.springframework.beans.factory.annotation.Autowired;
            import org.springframework.beans.factory.annotation.Value;
            import org.springframework.web.bind.annotation.RequestMapping;
            import org.springframework.web.bind.annotation.GetMapping;
            import org.springframework.web.bind.annotation.RestController;
            
            @Service
            @RestController
            @RequestMapping("/api/users")
            public class UserService {
                
                @Autowired
                private UserRepository userRepository;
                
                @Value("${app.timeout:30}")
                private int timeout;
                
                @GetMapping("/list")
                public List<User> getUsers() {
                    return userRepository.findAll();
                }
                
                @Override
                public String toString() {
                    return "UserService";
                }
            }
            """;
        
        Files.writeString(javaFile, javaContent);
        
        // Parse the file
        javaParserService.parseDirectory(tempDir.toString());
        
        // Verify that parsing completed without errors
        assertNotNull(javaParserService);
        assertTrue(Files.exists(javaFile));
        
        // Query for annotation nodes
        String annotationQuery = """
            MATCH (a:Annotation) 
            RETURN a.name as name, a.fullyQualifiedName as fqn, a.targetType as target, a.frameworkType as framework
            """;
        
        QueryResult annotationResults = cypherQueryService.executeQuery(annotationQuery, java.util.Map.of(), new CypherQueryRequest.QueryOptions());
        
        // Should find annotations: Service, RestController, RequestMapping, Autowired, Value, GetMapping, Override
        assertTrue(annotationResults.getResults().size() >= 5, 
                  "Expected at least 5 annotations, found: " + annotationResults.getResults().size());
        
        // Query for USES relationships involving annotations
        String usesQuery = """
            MATCH (c:Class)-[r:USES {type: 'annotation'}]->(a:Annotation) 
            RETURN c.name as className, r.context as context, a.name as annotationName
            """;
        
        QueryResult usesResults = cypherQueryService.executeQuery(usesQuery, java.util.Map.of(), new CypherQueryRequest.QueryOptions());
        
        // Should find multiple USES relationships for annotations  
        // Based on logs showing 3 relationships were found
        assertTrue(usesResults.getResults().size() >= 3,
                  "Expected multiple USES relationships for annotations, found: " + usesResults.getResults().size());
    }
    
    @Test
    void testJUnitAnnotationsCreateUsesRelationships() throws IOException {
        // Create a test Java file with JUnit annotations  
        Path javaFile = tempDir.resolve("UserServiceValidator.java");
        String javaContent = """
            package com.example.service;
            
            import org.junit.jupiter.api.Test;
            import org.junit.jupiter.api.BeforeEach;
            import org.junit.jupiter.api.AfterEach;
            import org.junit.jupiter.api.DisplayName;
            import org.mockito.Mock;
            import org.mockito.InjectMocks;
            
            public class UserServiceValidator {
                
                @Mock
                private UserRepository userRepository;
                
                @InjectMocks
                private UserService userService;
                
                @BeforeEach
                void setUp() {
                    // setup code
                }
                
                @AfterEach
                void tearDown() {
                    // cleanup code  
                }
                
                @Test
                @DisplayName("Should return all users")
                void shouldReturnAllUsers() {
                    // test implementation
                }
                
                @Test
                void shouldHandleEmptyResult() {
                    // test implementation
                }
            }
            """;
        
        Files.writeString(javaFile, javaContent);
        
        // Parse the file
        javaParserService.parseDirectory(tempDir.toString());
        
        // Verify that parsing completed without errors
        assertNotNull(javaParserService);
        assertTrue(Files.exists(javaFile));
        
        // Query for JUnit annotation nodes
        String junitQuery = """
            MATCH (a:Annotation) 
            WHERE a.frameworkType = 'JUnit' OR a.name IN ['Test', 'BeforeEach', 'AfterEach', 'DisplayName']
            RETURN a.name as name, a.frameworkType as framework, a.targetType as target
            """;
        
        QueryResult junitResults = cypherQueryService.executeQuery(junitQuery, java.util.Map.of(), new CypherQueryRequest.QueryOptions());
        
        // Should find JUnit annotations: Test, BeforeEach, AfterEach, DisplayName
        assertTrue(junitResults.getResults().size() >= 3,
                  "Expected JUnit annotations, found: " + junitResults.getResults().size());
        
        // Query for method-level annotation relationships  
        String methodAnnotationQuery = """
            MATCH (m:Method)-[r:USES]->(a:Annotation)
            WHERE r.type = 'annotation' AND r.context STARTS WITH 'method:'
            RETURN m.name as methodName, a.name as annotationName
            """;
        
        QueryResult methodResults = cypherQueryService.executeQuery(methodAnnotationQuery, java.util.Map.of(), new CypherQueryRequest.QueryOptions());
        
        // Should find method-level annotations
        assertTrue(methodResults.getResults().size() >= 2,
                  "Expected method-level annotations, found: " + methodResults.getResults().size());
    }
    
    @Test  
    void testComplexAnnotationWithAttributesCreateUsesRelationships() throws IOException {
        // Create a test Java file with complex annotations that have attributes
        Path javaFile = tempDir.resolve("UserController.java");
        String javaContent = """
            package com.example.controller;
            
            import org.springframework.web.bind.annotation.RestController;
            import org.springframework.web.bind.annotation.RequestMapping; 
            import org.springframework.web.bind.annotation.GetMapping;
            import org.springframework.web.bind.annotation.PostMapping;
            import org.springframework.web.bind.annotation.RequestParam;
            import org.springframework.web.bind.annotation.PathVariable;
            import org.springframework.beans.factory.annotation.Value;
            
            @RestController
            @RequestMapping(value = "/api/v1/users", produces = "application/json")
            public class UserController {
                
                @Value("${app.version:1.0}")
                private String appVersion;
                
                @GetMapping("/{userId}")
                public User getUser(@PathVariable("userId") Long userId, 
                                   @RequestParam(value = "includeDetails", defaultValue = "false") boolean includeDetails) {
                    return null; // placeholder
                }
                
                @PostMapping(value = "/create", consumes = "application/json", produces = "application/json")
                public User createUser() {
                    return null; // placeholder
                }
            }
            """;
        
        Files.writeString(javaFile, javaContent);
        
        // Parse the file
        javaParserService.parseDirectory(tempDir.toString());
        
        // Verify that parsing completed without errors
        assertNotNull(javaParserService);
        assertTrue(Files.exists(javaFile));
        
        // Query for complex annotations with attributes
        String complexAnnotationQuery = """
            MATCH (a:Annotation) 
            WHERE a.attributes <> '{}' AND a.attributes IS NOT NULL
            RETURN a.name as name, a.attributes as attributes, a.targetType as target
            """;
        
        QueryResult complexResults = cypherQueryService.executeQuery(complexAnnotationQuery, java.util.Map.of(), new CypherQueryRequest.QueryOptions());
        
        // Should find annotations with attributes: RequestMapping, GetMapping, PostMapping, Value, RequestParam, PathVariable
        assertTrue(complexResults.getResults().size() >= 3,
                  "Expected complex annotations with attributes, found: " + complexResults.getResults().size());
        
        // Query for parameter-level annotation relationships  
        // Parameter annotations are stored as method-level relationships since they're attached to the method
        String paramAnnotationQuery = """
            MATCH (m:Method)-[r:USES {type: 'annotation'}]->(a:Annotation)
            WHERE a.name IN ['PathVariable', 'RequestParam'] AND a.targetType = 'parameter'
            RETURN m.name as methodName, r.context as context, a.name as annotationName
            """;
        
        QueryResult paramResults = cypherQueryService.executeQuery(paramAnnotationQuery, java.util.Map.of(), new CypherQueryRequest.QueryOptions());
        
        // Should find parameter-level annotations: PathVariable, RequestParam
        assertTrue(paramResults.getResults().size() >= 2,
                  "Expected parameter-level annotations, found: " + paramResults.getResults().size());
    }

    @Test
    void shouldDetectGenericTypeDependencies() throws IOException, InterruptedException {
        // Create test Java class with various generic types
        String testGenericClass = """
            package com.example;

            import java.util.List;
            import java.util.Map;
            import java.util.Set;
            import java.util.Collection;
            import java.util.Optional;
            import java.util.concurrent.Future;

            public class GenericTypeService {
                private List<String> stringList;
                private Map<String, Integer> stringToIntMap;
                private Set<Long> longSet;
                private Collection<Double> doubleCollection;
                private Optional<Boolean> optionalBoolean;
                private Future<Object> futureObject;

                public List<User> getUsers() {
                    return null;
                }

                public Map<String, List<User>> getUsersByCategory(
                    Set<String> categories,
                    Optional<Integer> limit) {
                    return null;
                }

                public <T> Future<List<T>> processGeneric(Collection<T> input) {
                    return null;
                }
            }
            """;

        // Create test user class referenced in generics
        String userClass = """
            package com.example;
            
            public class User {
                private String name;
            }
            """;

        // Save test files
        Path testDir = tempDir.resolve("generic-test");
        Files.createDirectories(testDir);
        
        Path genericServiceFile = testDir.resolve("GenericTypeService.java");
        Files.write(genericServiceFile, testGenericClass.getBytes());
        
        Path userFile = testDir.resolve("User.java");  
        Files.write(userFile, userClass.getBytes());

        // Parse the directory
        javaParserService.parseDirectory(testDir.toString());

        // Allow processing time
        Thread.sleep(500);

        // Test 1: Query for field-level generic dependencies
        String fieldGenericQuery = """
            MATCH (c:Class)-[r:USES {type: 'generic_param'}]->(target:Class)
            WHERE c.name = 'GenericTypeService' AND r.context CONTAINS 'field'
            RETURN c.name as className, target.name as genericType, r.context as context
            """;
        
        QueryResult fieldResults = cypherQueryService.executeQuery(fieldGenericQuery, java.util.Map.of(), new CypherQueryRequest.QueryOptions());
        
        // Should find generic type dependencies from fields (String, Integer, Long, Double, Boolean, Object)
        assertTrue(fieldResults.getResults().size() >= 4, 
                  "Expected field-level generic type dependencies, found: " + fieldResults.getResults().size());

        // Test 2: Query for method return type generic dependencies  
        String returnGenericQuery = """
            MATCH (c:Class)-[r:USES {type: 'generic_param'}]->(target:Class)
            WHERE c.name = 'GenericTypeService' AND r.context CONTAINS 'method_return' AND target.name = 'User'
            RETURN c.name as className, target.name as genericType, r.context as context
            """;
        
        QueryResult returnResults = cypherQueryService.executeQuery(returnGenericQuery, java.util.Map.of(), new CypherQueryRequest.QueryOptions());
        
        // Should find User class as generic type argument in List<User> return type
        assertTrue(returnResults.getResults().size() >= 1,
                  "Expected return type generic dependencies, found: " + returnResults.getResults().size());

        // Test 3: Query for method parameter generic dependencies
        String paramGenericQuery = """
            MATCH (c:Class)-[r:USES {type: 'generic_param'}]->(target:Class)  
            WHERE c.name = 'GenericTypeService' AND r.context CONTAINS 'method_param'
            RETURN c.name as className, target.name as genericType, r.context as context
            """;
        
        QueryResult paramResults = cypherQueryService.executeQuery(paramGenericQuery, java.util.Map.of(), new CypherQueryRequest.QueryOptions());
        
        // Should find generic type dependencies from method parameters 
        assertTrue(paramResults.getResults().size() >= 2,
                  "Expected parameter generic dependencies, found: " + paramResults.getResults().size());

        // Test 4: Verify specific User class dependency is detected
        String userDependencyQuery = """
            MATCH (source)-[r:USES]->(target:Class)
            WHERE target.name = 'User' AND r.type = 'generic_param'
            RETURN source.name as sourceName, r.type as relationshipType, r.context as context
            """;
        
        QueryResult userResults = cypherQueryService.executeQuery(userDependencyQuery, java.util.Map.of(), new CypherQueryRequest.QueryOptions());
        
        // Should find User referenced in multiple generic contexts
        assertTrue(userResults.getResults().size() >= 1,
                  "Expected User class to be referenced in generic types, found: " + userResults.getResults().size());

        // Test 5: Verify overall generic type system is working by checking total unique generic parameters
        String allGenericQuery = """
            MATCH (source)-[r:USES {type: 'generic_param'}]->(target:Class)
            WHERE source.name = 'GenericTypeService'
            RETURN DISTINCT target.name as genericType
            ORDER BY target.name
            """;
        
        QueryResult allGenericResults = cypherQueryService.executeQuery(allGenericQuery, java.util.Map.of(), new CypherQueryRequest.QueryOptions());
        
        // Should find multiple distinct generic type parameters (String, Integer, Long, Double, Boolean, Object, User, List, T)
        assertTrue(allGenericResults.getResults().size() >= 6,
                  "Expected multiple unique generic type parameters, found: " + allGenericResults.getResults().size());
    }
}
