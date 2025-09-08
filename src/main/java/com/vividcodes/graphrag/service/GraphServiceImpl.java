package com.vividcodes.graphrag.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vividcodes.graphrag.model.dto.UpsertResult;
import com.vividcodes.graphrag.model.graph.AnnotationNode;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.FieldNode;
import com.vividcodes.graphrag.model.graph.MethodNode;
import com.vividcodes.graphrag.model.graph.PackageNode;
import com.vividcodes.graphrag.model.graph.RepositoryNode;
import com.vividcodes.graphrag.model.graph.SubProjectNode;

@Service
public class GraphServiceImpl implements GraphService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphServiceImpl.class);

    private final Driver neo4jDriver;
    private final UpsertService upsertService;

    @Autowired
    public GraphServiceImpl(final Driver neo4jDriver, final UpsertService upsertService) {
        this.neo4jDriver = neo4jDriver;
        this.upsertService = upsertService;
    }
    
    @Override
    public UpsertResult savePackage(PackageNode packageNode) {
        LOGGER.debug("Performing upsert for package: {}", packageNode.getName());
        return upsertService.upsertPackage(packageNode);
    }
    
    @Override
    public UpsertResult saveClass(ClassNode classNode) {
        LOGGER.debug("Performing upsert for class: {}", classNode.getName());
        return upsertService.upsertClass(classNode);
    }
    
    @Override
    public UpsertResult saveMethod(MethodNode methodNode) {
        LOGGER.debug("Performing upsert for method: {}.{}", methodNode.getClassName(), methodNode.getName());
        return upsertService.upsertMethod(methodNode);
    }
    
    @Override
    public UpsertResult saveField(FieldNode fieldNode) {
        LOGGER.debug("Performing upsert for field: {}.{}", fieldNode.getClassName(), fieldNode.getName());
        return upsertService.upsertField(fieldNode);
    }
    
    @Override
    public UpsertResult saveAnnotation(final AnnotationNode annotationNode) {
        LOGGER.debug("Performing upsert for annotation: {}", annotationNode.getName());
        return upsertService.upsertAnnotation(annotationNode);
    }
    
    @Override
    public UpsertResult saveRepository(RepositoryNode repositoryNode) {
        LOGGER.debug("Performing upsert for repository: {}", repositoryNode.getName());
        return upsertService.upsertRepository(repositoryNode);
    }
    
    @Override
    public UpsertResult saveSubProject(SubProjectNode subProjectNode) {
        LOGGER.debug("Performing upsert for sub-project: {}", subProjectNode.getName());
        return upsertService.upsertSubProject(subProjectNode);
    }
    
    @Override
    public SubProjectNode findSubProjectById(String id) {
        try (Session session = neo4jDriver.session()) {
            String cypher = """
                MATCH (sp:SubProject {id: $id})
                RETURN sp
                """;
            
            Result result = session.run(cypher, Values.parameters("id", id));
            
            if (result.hasNext()) {
                Record record = result.next();
                Value spValue = record.get("sp");
                
                SubProjectNode subProject = new SubProjectNode();
                subProject.setId(spValue.get("id").asString());
                subProject.setName(spValue.get("name").asString());
                subProject.setPath(spValue.get("path").asString());
                subProject.setType(spValue.get("type").asString());
                
                if (!spValue.get("buildFile").isNull()) {
                    subProject.setBuildFile(spValue.get("buildFile").asString());
                }
                if (!spValue.get("description").isNull()) {
                    subProject.setDescription(spValue.get("description").asString());
                }
                if (!spValue.get("version").isNull()) {
                    subProject.setVersion(spValue.get("version").asString());
                }
                if (!spValue.get("repository_id").isNull()) {
                    subProject.setRepositoryId(spValue.get("repository_id").asString());
                }
                if (!spValue.get("sourceDirectories").isNull()) {
                    subProject.setSourceDirectories(spValue.get("sourceDirectories").asList(Value::asString));
                }
                if (!spValue.get("testDirectories").isNull()) {
                    subProject.setTestDirectories(spValue.get("testDirectories").asList(Value::asString));
                }
                if (!spValue.get("dependencies").isNull()) {
                    subProject.setDependencies(spValue.get("dependencies").asList(Value::asString));
                }
                
                LOGGER.debug("Found sub-project: {}", subProject.getName());
                return subProject;
            }
            
            LOGGER.debug("Sub-project not found with ID: {}", id);
            return null;
            
        } catch (Exception e) {
            LOGGER.error("Error finding sub-project by ID: {}", id, e);
            throw new RuntimeException("Failed to find sub-project", e);
        }
    }
    
    @Override
    public List<SubProjectNode> findSubProjectsByRepositoryId(String repositoryId) {
        try (Session session = neo4jDriver.session()) {
            String cypher = """
                MATCH (sp:SubProject {repository_id: $repositoryId})
                RETURN sp
                ORDER BY sp.name
                """;
            
            Result result = session.run(cypher, Values.parameters("repositoryId", repositoryId));
            List<SubProjectNode> subProjects = new ArrayList<>();
            
            while (result.hasNext()) {
                Record record = result.next();
                Value spValue = record.get("sp");
                
                SubProjectNode subProject = new SubProjectNode();
                subProject.setId(spValue.get("id").asString());
                subProject.setName(spValue.get("name").asString());
                subProject.setPath(spValue.get("path").asString());
                subProject.setType(spValue.get("type").asString());
                
                if (!spValue.get("buildFile").isNull()) {
                    subProject.setBuildFile(spValue.get("buildFile").asString());
                }
                if (!spValue.get("description").isNull()) {
                    subProject.setDescription(spValue.get("description").asString());
                }
                if (!spValue.get("version").isNull()) {
                    subProject.setVersion(spValue.get("version").asString());
                }
                if (!spValue.get("repository_id").isNull()) {
                    subProject.setRepositoryId(spValue.get("repository_id").asString());
                }
                
                subProjects.add(subProject);
            }
            
            LOGGER.debug("Found {} sub-projects for repository: {}", subProjects.size(), repositoryId);
            return subProjects;
            
        } catch (Exception e) {
            LOGGER.error("Error finding sub-projects for repository: {}", repositoryId, e);
            throw new RuntimeException("Failed to find sub-projects", e);
        }
    }
    
    @Override
    public void deleteSubProject(String id) {
        try (Session session = neo4jDriver.session()) {
            String cypher = """
                MATCH (sp:SubProject {id: $id})
                DETACH DELETE sp
                """;
            
            session.run(cypher, Values.parameters("id", id));
            LOGGER.info("Deleted sub-project with ID: {}", id);
            
        } catch (Exception e) {
            LOGGER.error("Error deleting sub-project: {}", id, e);
            throw new RuntimeException("Failed to delete sub-project", e);
        }
    }
    
    @Override
    public void clearAllData() {
        try (Session session = neo4jDriver.session()) {
            String cypher = "MATCH (n) DETACH DELETE n";
            session.run(cypher);
            LOGGER.info("Cleared all data from the graph database");
        } catch (Exception e) {
            LOGGER.error("Error clearing data from graph database", e);
            throw new RuntimeException("Failed to clear data", e);
        }
    }
    
    @Override
    public Map<String, Object> getDataStatistics() {
        try (Session session = neo4jDriver.session()) {
            Map<String, Object> stats = new HashMap<>();
            
            // Get node counts by type
            String nodeCountsQuery = """
                MATCH (n)
                RETURN labels(n) as nodeType, count(n) as count
                ORDER BY nodeType
                """;
            
            Result nodeCountsResult = session.run(nodeCountsQuery);
            Map<String, Long> nodeCounts = new HashMap<>();
            while (nodeCountsResult.hasNext()) {
                Record record = nodeCountsResult.next();
                List<String> labels = record.get("nodeType").asList(Value::asString);
                String nodeType = String.join(":", labels);
                long count = record.get("count").asLong();
                nodeCounts.put(nodeType, count);
            }
            stats.put("nodeCounts", nodeCounts);
            
            // Get total node count
            String totalNodesQuery = "MATCH (n) RETURN count(n) as totalNodes";
            Result totalNodesResult = session.run(totalNodesQuery);
            long totalNodes = totalNodesResult.single().get("totalNodes").asLong();
            stats.put("totalNodes", totalNodes);
            
            // Get relationship counts by type
            String relationshipCountsQuery = """
                MATCH ()-[r]->()
                RETURN type(r) as relationshipType, count(r) as count
                ORDER BY relationshipType
                """;
            
            Result relationshipCountsResult = session.run(relationshipCountsQuery);
            Map<String, Long> relationshipCounts = new HashMap<>();
            while (relationshipCountsResult.hasNext()) {
                Record record = relationshipCountsResult.next();
                String relationshipType = record.get("relationshipType").asString();
                long count = record.get("count").asLong();
                relationshipCounts.put(relationshipType, count);
            }
            stats.put("relationshipCounts", relationshipCounts);
            
            // Get total relationship count
            String totalRelationshipsQuery = "MATCH ()-[r]->() RETURN count(r) as totalRelationships";
            Result totalRelationshipsResult = session.run(totalRelationshipsQuery);
            long totalRelationships = totalRelationshipsResult.single().get("totalRelationships").asLong();
            stats.put("totalRelationships", totalRelationships);
            
            // Get repository statistics
            String repositoryStatsQuery = """
                MATCH (r:Repository)
                RETURN count(r) as repositoryCount,
                       collect(r.name) as repositoryNames,
                       collect(r.organization) as organizations
                """;
            
            Result repositoryStatsResult = session.run(repositoryStatsQuery);
            if (repositoryStatsResult.hasNext()) {
                Record record = repositoryStatsResult.single();
                long repositoryCount = record.get("repositoryCount").asLong();
                List<String> repositoryNames = record.get("repositoryNames").asList(Value::asString);
                List<String> organizations = record.get("organizations").asList(Value::asString);
                
                stats.put("repositoryCount", repositoryCount);
                stats.put("repositoryNames", repositoryNames);
                stats.put("organizations", organizations.stream().distinct().toList());
            } else {
                stats.put("repositoryCount", 0L);
                stats.put("repositoryNames", new ArrayList<>());
                stats.put("organizations", new ArrayList<>());
            }
            
            LOGGER.debug("Retrieved data statistics: {} nodes, {} relationships", totalNodes, totalRelationships);
            return stats;
            
        } catch (Exception e) {
            LOGGER.error("Error retrieving data statistics", e);
            throw new RuntimeException("Failed to retrieve data statistics", e);
        }
    }
    
    
    @Override
    public List<UpsertResult> saveBatch(List<Object> nodes) {
        LOGGER.debug("Performing batch upsert for {} nodes", nodes.size());
        return upsertService.upsertBatch(nodes);
    }
    
    @Override
    public boolean createRelationship(String fromId, String toId, String relationshipType) {
        return createRelationship(fromId, toId, relationshipType, new HashMap<>());
    }
    
    @Override
    public boolean createRelationship(String fromId, String toId, String relationshipType, java.util.Map<String, Object> properties) {
        LOGGER.debug("Creating relationship: {} -[{}]-> {}", fromId, relationshipType, toId);
        
        try (Session session = neo4jDriver.session()) {
            return session.writeTransaction(tx -> {
                // Check if relationship already exists
                String checkQuery = """
                    MATCH (from {id: $fromId})-[r:%s]->(to {id: $toId})
                    RETURN COUNT(r) as count
                    """.formatted(relationshipType);
                
                Result checkResult = tx.run(checkQuery, Values.parameters("fromId", fromId, "toId", toId));
                long existingCount = checkResult.single().get("count").asLong();
                
                if (existingCount > 0) {
                    LOGGER.debug("Relationship already exists and no updates needed: {} -[{}]-> {}", fromId, relationshipType, toId);
                    return false; // Relationship already exists
                }
                
                // Create new relationship
                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("MATCH (from {id: $fromId}), (to {id: $toId}) ");
                queryBuilder.append("CREATE (from)-[r:").append(relationshipType);
                
                if (!properties.isEmpty()) {
                    queryBuilder.append(" {");
                    boolean first = true;
                    for (String key : properties.keySet()) {
                        if (!first) queryBuilder.append(", ");
                        queryBuilder.append(key).append(": $").append(key);
                        first = false;
                    }
                    queryBuilder.append("}");
                }
                
                queryBuilder.append("]->(to) RETURN r");
                
                Map<String, Object> params = new HashMap<>();
                params.put("fromId", fromId);
                params.put("toId", toId);
                params.putAll(properties);
                
                Result createResult = tx.run(queryBuilder.toString(), params);
                boolean created = createResult.hasNext();
                
                if (created) {
                    LOGGER.debug("Created relationship: {} -[{}]-> {}", fromId, relationshipType, toId);
                } else {
                    LOGGER.warn("Failed to create relationship: {} -[{}]-> {}", fromId, relationshipType, toId);
                }
                
                return created;
            });
        } catch (Exception e) {
            LOGGER.error("Error creating relationship: {} -[{}]-> {}", fromId, relationshipType, toId, e);
            return false;
        }
    }
    
} 