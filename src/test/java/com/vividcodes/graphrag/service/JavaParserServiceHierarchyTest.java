package com.vividcodes.graphrag.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.vividcodes.graphrag.model.graph.SubProjectNode;
import com.vividcodes.graphrag.model.graph.RepositoryNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Unit tests for JavaParserService's SubProject detection and relationship logic
 */
class JavaParserServiceHierarchyTest {
    
    private JavaParserService javaParserService;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Create a minimal JavaParserService for testing
        javaParserService = new JavaParserService(null, null, null);
    }
    
    @Test
    void testFindContainingSubProject_WithMatchingPath() throws Exception {
        // Given: A Java file and sub-projects with different paths
        Path javaFile = tempDir.resolve("project1/src/main/java/com/example/Test.java");
        Files.createDirectories(javaFile.getParent());
        Files.createFile(javaFile);
        
        SubProjectNode project1 = createSubProject("project1", tempDir.resolve("project1").toString());
        SubProjectNode project2 = createSubProject("project2", tempDir.resolve("project2").toString());
        List<SubProjectNode> subProjects = List.of(project1, project2);
        
        // When: Find the containing sub-project
        SubProjectNode result = callFindContainingSubProject(javaFile, subProjects);
        
        // Then: Should return the matching sub-project
        assertNotNull(result);
        assertEquals("project1", result.getName());
    }
    
    @Test
    void testFindContainingSubProject_NoMatch() throws Exception {
        // Given: A Java file outside all sub-project paths
        Path javaFile = tempDir.resolve("outside/Test.java");
        Files.createDirectories(javaFile.getParent());
        Files.createFile(javaFile);
        
        SubProjectNode project1 = createSubProject("project1", tempDir.resolve("project1").toString());
        List<SubProjectNode> subProjects = List.of(project1);
        
        // When: Find the containing sub-project
        SubProjectNode result = callFindContainingSubProject(javaFile, subProjects);
        
        // Then: Should return null (no containing project)
        assertNull(result);
    }
    
    @Test
    void testFindContainingSubProject_EmptySubProjects() throws Exception {
        // Given: A Java file and no sub-projects
        Path javaFile = tempDir.resolve("Test.java");
        Files.createFile(javaFile);
        
        // When: Find the containing sub-project
        SubProjectNode result = callFindContainingSubProject(javaFile, List.of());
        
        // Then: Should return null
        assertNull(result);
    }
    
    @Test
    void testFindContainingSubProject_NestedProjects() throws Exception {
        // Given: Nested sub-projects (most specific should win)
        Path javaFile = tempDir.resolve("parent/child/src/Test.java");
        Files.createDirectories(javaFile.getParent());
        Files.createFile(javaFile);
        
        SubProjectNode parentProject = createSubProject("parent", tempDir.resolve("parent").toString());
        SubProjectNode childProject = createSubProject("child", tempDir.resolve("parent/child").toString());
        List<SubProjectNode> subProjects = List.of(parentProject, childProject);
        
        // When: Find the containing sub-project
        SubProjectNode result = callFindContainingSubProject(javaFile, subProjects);
        
        // Then: Should return the most specific (child) project
        assertNotNull(result);
        assertEquals("child", result.getName());
    }
    
    @Test
    void testFindContainingSubProject_NullSubProjects() throws Exception {
        // Given: A Java file and null sub-projects list
        Path javaFile = tempDir.resolve("Test.java");
        Files.createFile(javaFile);
        
        // When: Find the containing sub-project
        SubProjectNode result = callFindContainingSubProject(javaFile, null);
        
        // Then: Should return null gracefully
        assertNull(result);
    }
    
    private SubProjectNode createSubProject(String name, String path) {
        SubProjectNode project = new SubProjectNode();
        project.setId("test-repo_maven_" + name);
        project.setName(name);
        project.setPath(path);
        project.setType("maven");
        return project;
    }
    
    /**
     * Use reflection to call the private findContainingSubProject method
     */
    private SubProjectNode callFindContainingSubProject(Path javaFile, List<SubProjectNode> subProjects) throws Exception {
        // Create a mock repository for the test
        RepositoryNode mockRepository = new RepositoryNode();
        mockRepository.setLocalPath(tempDir.toString());
        
        Method method = JavaParserService.class.getDeclaredMethod("findContainingSubProject", Path.class, List.class, RepositoryNode.class);
        method.setAccessible(true);
        return (SubProjectNode) method.invoke(javaParserService, javaFile, subProjects, mockRepository);
    }
}
