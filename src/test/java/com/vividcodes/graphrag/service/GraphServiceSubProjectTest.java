package com.vividcodes.graphrag.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import com.vividcodes.graphrag.model.graph.SubProjectNode;

/**
 * Unit tests for GraphService SubProject operations
 */
class GraphServiceSubProjectTest {
    
    @Mock
    private Driver neo4jDriver;
    
    @Mock
    private Session session;
    
    @Mock
    private Result result;
    
    @Mock
    private Record record;
    
    @Mock
    private Value value;
    
    private GraphServiceImpl graphService;
    private SubProjectNode testSubProject;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        graphService = new GraphServiceImpl(neo4jDriver);
        
        // Create test SubProject
        testSubProject = new SubProjectNode("test-id", "test-project", "/path/to/project", "maven");
        testSubProject.setBuildFile("pom.xml");
        testSubProject.setDescription("Test project");
        testSubProject.setVersion("1.0.0");
        testSubProject.setRepositoryId("repo-123");
        testSubProject.setSourceDirectories(Arrays.asList("src/main/java"));
        testSubProject.setTestDirectories(Arrays.asList("src/test/java"));
        testSubProject.setDependencies(Arrays.asList("junit", "mockito"));
    }
    
    @Test
    void testSaveSubProject() {
        // Mock session behavior
        when(neo4jDriver.session()).thenReturn(session);
        when(session.run(anyString(), any(org.neo4j.driver.Value.class))).thenReturn(result);
        
        // Execute
        assertDoesNotThrow(() -> graphService.saveSubProject(testSubProject));
        
        // Verify session was used
        verify(neo4jDriver).session();
        verify(session).run(anyString(), any(org.neo4j.driver.Value.class));
        verify(session).close();
    }
    
    @Test
    void testSaveSubProjectWithNullValues() {
        // Create SubProject with minimal data
        SubProjectNode minimalProject = new SubProjectNode("min-id", "minimal", "/min/path", "custom");
        
        when(neo4jDriver.session()).thenReturn(session);
        when(session.run(anyString(), any(org.neo4j.driver.Value.class))).thenReturn(result);
        
        // Should not throw exception with null optional fields
        assertDoesNotThrow(() -> graphService.saveSubProject(minimalProject));
        
        verify(neo4jDriver).session();
        verify(session).run(anyString(), any(org.neo4j.driver.Value.class));
    }
    
    @Test
    void testSaveSubProjectThrowsExceptionOnError() {
        when(neo4jDriver.session()).thenReturn(session);
        when(session.run(anyString(), any(org.neo4j.driver.Value.class))).thenThrow(new RuntimeException("Database error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            graphService.saveSubProject(testSubProject);
        });
        
        assertEquals("Failed to save sub-project", exception.getMessage());
        verify(session).close();
    }
    
    @Test
    void testFindSubProjectById() {
        String testId = "test-id";
        
        // Mock successful find
        when(neo4jDriver.session()).thenReturn(session);
        when(session.run(anyString(), any(org.neo4j.driver.Value.class))).thenReturn(result);
        when(result.hasNext()).thenReturn(true);
        when(result.next()).thenReturn(record);
        when(record.get("sp")).thenReturn(value);
        
        // Mock the value properties
        when(value.get("id")).thenReturn(Values.value("test-id"));
        when(value.get("name")).thenReturn(Values.value("test-project"));
        when(value.get("path")).thenReturn(Values.value("/path/to/project"));
        when(value.get("type")).thenReturn(Values.value("maven"));
        when(value.get("buildFile")).thenReturn(Values.value("pom.xml"));
        when(value.get("description")).thenReturn(Values.value("Test project"));
        when(value.get("version")).thenReturn(Values.value("1.0.0"));
        when(value.get("repository_id")).thenReturn(Values.value("repo-123"));
        when(value.get("sourceDirectories")).thenReturn(Values.value(Arrays.asList("src/main/java")));
        when(value.get("testDirectories")).thenReturn(Values.value(Arrays.asList("src/test/java")));
        when(value.get("dependencies")).thenReturn(Values.value(Arrays.asList("junit", "mockito")));
        
        // Execute
        SubProjectNode found = graphService.findSubProjectById(testId);
        
        // Verify
        assertNotNull(found);
        assertEquals("test-id", found.getId());
        assertEquals("test-project", found.getName());
        assertEquals("/path/to/project", found.getPath());
        assertEquals("maven", found.getType());
        
        verify(neo4jDriver).session();
        verify(session).run(anyString(), any(org.neo4j.driver.Value.class));
    }
    
    @Test
    void testFindSubProjectByIdNotFound() {
        String testId = "non-existent-id";
        
        when(neo4jDriver.session()).thenReturn(session);
        when(session.run(anyString(), any(org.neo4j.driver.Value.class))).thenReturn(result);
        when(result.hasNext()).thenReturn(false);
        
        SubProjectNode found = graphService.findSubProjectById(testId);
        
        assertNull(found);
        verify(neo4jDriver).session();
        verify(session).run(anyString(), any(org.neo4j.driver.Value.class));
    }
    
    @Test
    void testFindSubProjectByIdThrowsExceptionOnError() {
        when(neo4jDriver.session()).thenReturn(session);
        when(session.run(anyString(), any(org.neo4j.driver.Value.class))).thenThrow(new RuntimeException("Database error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            graphService.findSubProjectById("test-id");
        });
        
        assertEquals("Failed to find sub-project", exception.getMessage());
        verify(session).close();
    }
    
    @Test
    void testFindSubProjectsByRepositoryId() {
        String repositoryId = "repo-123";
        
        when(neo4jDriver.session()).thenReturn(session);
        when(session.run(anyString(), any(org.neo4j.driver.Value.class))).thenReturn(result);
        when(result.hasNext()).thenReturn(true, true, false); // Two results
        when(result.next()).thenReturn(record);
        when(record.get("sp")).thenReturn(value);
        
        // Mock the value properties for two different projects
        when(value.get("id")).thenReturn(Values.value("project-1"), Values.value("project-2"));
        when(value.get("name")).thenReturn(Values.value("Project 1"), Values.value("Project 2"));
        when(value.get("path")).thenReturn(Values.value("/path/1"), Values.value("/path/2"));
        when(value.get("type")).thenReturn(Values.value("maven"), Values.value("gradle"));
        when(value.get("buildFile")).thenReturn(Values.NULL, Values.NULL);
        when(value.get("description")).thenReturn(Values.NULL, Values.NULL);
        when(value.get("version")).thenReturn(Values.NULL, Values.NULL);
        when(value.get("repository_id")).thenReturn(Values.value(repositoryId), Values.value(repositoryId));
        
        List<SubProjectNode> subProjects = graphService.findSubProjectsByRepositoryId(repositoryId);
        
        assertNotNull(subProjects);
        assertEquals(2, subProjects.size());
        assertEquals("project-1", subProjects.get(0).getId());
        assertEquals("project-2", subProjects.get(1).getId());
        
        verify(neo4jDriver).session();
        verify(session).run(anyString(), any(org.neo4j.driver.Value.class));
    }
    
    @Test
    void testFindSubProjectsByRepositoryIdEmpty() {
        String repositoryId = "empty-repo";
        
        when(neo4jDriver.session()).thenReturn(session);
        when(session.run(anyString(), any(org.neo4j.driver.Value.class))).thenReturn(result);
        when(result.hasNext()).thenReturn(false);
        
        List<SubProjectNode> subProjects = graphService.findSubProjectsByRepositoryId(repositoryId);
        
        assertNotNull(subProjects);
        assertTrue(subProjects.isEmpty());
        
        verify(neo4jDriver).session();
        verify(session).run(anyString(), any(org.neo4j.driver.Value.class));
    }
    
    @Test
    void testFindSubProjectsByRepositoryIdThrowsExceptionOnError() {
        when(neo4jDriver.session()).thenReturn(session);
        when(session.run(anyString(), any(org.neo4j.driver.Value.class))).thenThrow(new RuntimeException("Database error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            graphService.findSubProjectsByRepositoryId("repo-123");
        });
        
        assertEquals("Failed to find sub-projects", exception.getMessage());
        verify(session).close();
    }
    
    @Test
    void testDeleteSubProject() {
        String testId = "test-id";
        
        when(neo4jDriver.session()).thenReturn(session);
        when(session.run(anyString(), any(org.neo4j.driver.Value.class))).thenReturn(result);
        
        assertDoesNotThrow(() -> graphService.deleteSubProject(testId));
        
        verify(neo4jDriver).session();
        verify(session).run(anyString(), any(org.neo4j.driver.Value.class));
        verify(session).close();
    }
    
    @Test
    void testDeleteSubProjectThrowsExceptionOnError() {
        when(neo4jDriver.session()).thenReturn(session);
        when(session.run(anyString(), any(org.neo4j.driver.Value.class))).thenThrow(new RuntimeException("Database error"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            graphService.deleteSubProject("test-id");
        });
        
        assertEquals("Failed to delete sub-project", exception.getMessage());
        verify(session).close();
    }
    
    @Test
    void testSubProjectCypherQueries() {
        // This test verifies that the correct Cypher queries are being constructed
        // We'll capture the actual queries being executed
        
        when(neo4jDriver.session()).thenReturn(session);
        when(session.run(anyString(), any(org.neo4j.driver.Value.class))).thenReturn(result);
        
        // Test save query
        graphService.saveSubProject(testSubProject);
        
        // Verify that a MERGE query was executed with correct parameters
        verify(session).run(contains("MERGE (sp:SubProject {id: $id})"), any(org.neo4j.driver.Value.class));
        
        // Test find by ID query
        when(result.hasNext()).thenReturn(false);
        graphService.findSubProjectById("test-id");
        
        verify(session).run(contains("MATCH (sp:SubProject {id: $id})"), any(org.neo4j.driver.Value.class));
        
        // Test find by repository ID query
        graphService.findSubProjectsByRepositoryId("repo-123");
        
        verify(session).run(contains("MATCH (sp:SubProject {repository_id: $repositoryId})"), any(org.neo4j.driver.Value.class));
        
        // Test delete query
        graphService.deleteSubProject("test-id");
        
        verify(session).run(contains("MATCH (sp:SubProject {id: $id})"), any(org.neo4j.driver.Value.class));
        verify(session).run(contains("DETACH DELETE sp"), any(org.neo4j.driver.Value.class));
    }
}
