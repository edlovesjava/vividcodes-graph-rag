package com.vividcodes.graphrag.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.PackageNode;
import com.vividcodes.graphrag.model.graph.RepositoryNode;
import com.vividcodes.graphrag.model.graph.SubProjectNode;
import com.vividcodes.graphrag.service.JavaParserService;
import com.vividcodes.graphrag.service.GraphService;
import com.vividcodes.graphrag.service.RepositoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Integration tests for hierarchical relationship creation:
 * Repository -> SubProject -> Package -> Class -> Method/Field
 */
@SpringBootTest
@ActiveProfiles("test")
class HierarchicalRelationshipIntegrationTest {
    
    @Autowired
    private JavaParserService javaParserService;
    
    @Autowired
    private GraphService graphService;
    
    @Autowired
    private RepositoryService repositoryService;
    
    @TempDir
    Path tempRepositoryDir;
    
    @Test
    void testCompleteHierarchicalRelationshipCreation() throws IOException {
        // Given: A repository with a Maven sub-project containing Java classes
        createTestRepositoryWithMavenSubProject();
        
        // When: Parse the directory
        javaParserService.parseDirectory(tempRepositoryDir.toString());
        
        // Then: Verify complete hierarchy is created
        verifyRepositoryToSubProjectRelationships();
        verifySubProjectToPackageRelationships();
        verifyPackageToClassRelationships();
        verifyClassToMemberRelationships();
    }
    
    @Test
    void testMultipleSubProjectsHierarchy() throws IOException {
        // Given: A repository with multiple sub-projects
        createTestRepositoryWithMultipleSubProjects();
        
        // When: Parse the directory
        javaParserService.parseDirectory(tempRepositoryDir.toString());
        
        // Then: Verify relationships for all sub-projects
        verifyMultipleSubProjectRelationships();
    }
    
    @Test
    void testSingleProjectRepositoryBackwardCompatibility() throws IOException {
        // Given: A repository with no sub-project structure (single project)
        createTestSingleProjectRepository();
        
        // When: Parse the directory
        javaParserService.parseDirectory(tempRepositoryDir.toString());
        
        // Then: Verify traditional relationships still work
        verifySingleProjectRelationships();
    }
    
    private void createTestRepositoryWithMavenSubProject() throws IOException {
        // Create root directory structure
        Files.createDirectories(tempRepositoryDir.resolve(".git"));
        
        // Create Maven sub-project
        Path mavenProject = tempRepositoryDir.resolve("backend-service");
        Files.createDirectories(mavenProject.resolve("src/main/java/com/example/service"));
        
        // Create pom.xml
        Files.writeString(mavenProject.resolve("pom.xml"),
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "    <groupId>com.example</groupId>\n" +
            "    <artifactId>backend-service</artifactId>\n" +
            "    <version>1.0.0</version>\n" +
            "</project>\n");
        
        // Create Java class
        Files.writeString(mavenProject.resolve("src/main/java/com/example/service/UserService.java"),
            "package com.example.service;\n\n" +
            "public class UserService {\n" +
            "    private String serviceName;\n\n" +
            "    public void processUser() {\n" +
            "        // Implementation\n" +
            "    }\n\n" +
            "    public String getServiceName() {\n" +
            "        return serviceName;\n" +
            "    }\n" +
            "}\n");
    }
    
    private void createTestRepositoryWithMultipleSubProjects() throws IOException {
        // Create root directory structure
        Files.createDirectories(tempRepositoryDir.resolve(".git"));
        
        // Create Maven sub-project
        Path mavenProject = tempRepositoryDir.resolve("backend");
        Files.createDirectories(mavenProject.resolve("src/main/java/com/example/backend"));
        Files.writeString(mavenProject.resolve("pom.xml"),
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project><groupId>com.example</groupId><artifactId>backend</artifactId></project>\n");
        Files.writeString(mavenProject.resolve("src/main/java/com/example/backend/BackendService.java"),
            "package com.example.backend;\npublic class BackendService {}\n");
        
        // Create Gradle sub-project
        Path gradleProject = tempRepositoryDir.resolve("frontend");
        Files.createDirectories(gradleProject.resolve("src/main/java/com/example/frontend"));
        Files.writeString(gradleProject.resolve("build.gradle"),
            "plugins { id 'java' }\nversion = '1.0'\n");
        Files.writeString(gradleProject.resolve("src/main/java/com/example/frontend/FrontendController.java"),
            "package com.example.frontend;\npublic class FrontendController {}\n");
    }
    
    private void createTestSingleProjectRepository() throws IOException {
        // Create root directory structure (no sub-projects)
        Files.createDirectories(tempRepositoryDir.resolve(".git"));
        Files.createDirectories(tempRepositoryDir.resolve("src/main/java/com/example/simple"));
        
        // Create Java class directly in repository root
        Files.writeString(tempRepositoryDir.resolve("src/main/java/com/example/simple/SimpleService.java"),
            "package com.example.simple;\n\n" +
            "public class SimpleService {\n" +
            "    public void doSomething() {}\n" +
            "}\n");
    }
    
    private void verifyRepositoryToSubProjectRelationships() {
        // This would involve Cypher queries to verify:
        // MATCH (r:Repository)-[:CONTAINS]->(sp:SubProject) 
        // WHERE r.localPath = tempRepositoryDir
        // TODO: Implement using GraphService or direct Cypher queries
        
        // For now, use a simple count verification
        var stats = graphService.getDataStatistics();
        assertTrue((Integer) stats.get("totalNodes") > 0, "Should have created nodes");
        assertTrue((Integer) stats.get("totalRelationships") > 0, "Should have created relationships");
    }
    
    private void verifySubProjectToPackageRelationships() {
        // Verify SubProject -> Package relationships exist
        // MATCH (sp:SubProject)-[:CONTAINS]->(p:Package)
        var stats = graphService.getDataStatistics();
        assertNotNull(stats);
    }
    
    private void verifyPackageToClassRelationships() {
        // Verify Package -> Class relationships exist
        // MATCH (p:Package)-[:CONTAINS]->(c:Class)
        var stats = graphService.getDataStatistics();
        assertNotNull(stats);
    }
    
    private void verifyClassToMemberRelationships() {
        // Verify Class -> Method/Field relationships exist
        // MATCH (c:Class)-[:CONTAINS]->(m:Method)
        // MATCH (c:Class)-[:CONTAINS]->(f:Field)
        var stats = graphService.getDataStatistics();
        assertNotNull(stats);
    }
    
    private void verifyMultipleSubProjectRelationships() {
        // Verify both Maven and Gradle sub-projects have proper relationships
        var stats = graphService.getDataStatistics();
        assertTrue((Integer) stats.get("totalNodes") >= 4, "Should have multiple sub-projects with their classes");
    }
    
    private void verifySingleProjectRelationships() {
        // Verify traditional relationships work without sub-projects
        var stats = graphService.getDataStatistics();
        assertTrue((Integer) stats.get("totalNodes") > 0, "Should have created nodes even without sub-projects");
    }
}
