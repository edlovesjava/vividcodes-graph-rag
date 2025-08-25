package com.vividcodes.graphrag.service;

import java.util.Map;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vividcodes.graphrag.model.graph.ClassNode;
import com.vividcodes.graphrag.model.graph.FieldNode;
import com.vividcodes.graphrag.model.graph.MethodNode;
import com.vividcodes.graphrag.model.graph.PackageNode;
import com.vividcodes.graphrag.model.graph.RepositoryNode;

@Service
public class GraphServiceImpl implements GraphService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphServiceImpl.class);

    private final Driver neo4jDriver;

    @Autowired
    public GraphServiceImpl(final Driver neo4jDriver) {
        this.neo4jDriver = neo4jDriver;
    }
    
    @Override
    public void savePackage(PackageNode packageNode) {
        try (Session session = neo4jDriver.session()) {
            String cypher = """
                MERGE (p:Package {id: $id})
                SET p.name = $name,
                    p.path = $path,
                    p.file_path = $filePath,
                    p.line_start = $lineStart,
                    p.line_end = $lineEnd,
                    p.created_at = $createdAt,
                    p.updated_at = $updatedAt
                """;
            
            session.run(cypher, Values.parameters(
                "id", packageNode.getId(),
                "name", packageNode.getName(),
                "path", packageNode.getPath(),
                "filePath", packageNode.getFilePath(),
                "lineStart", packageNode.getLineStart(),
                "lineEnd", packageNode.getLineEnd(),
                "createdAt", packageNode.getCreatedAt(),
                "updatedAt", packageNode.getUpdatedAt()
            ));
            
            LOGGER.debug("Saved package: {}", packageNode.getName());
        } catch (Exception e) {
            LOGGER.error("Error saving package: {}", packageNode.getName(), e);
            throw new RuntimeException("Failed to save package", e);
        }
    }
    
    @Override
    public void saveClass(ClassNode classNode) {
        try (Session session = neo4jDriver.session()) {
            String cypher = """
                MERGE (c:Class {id: $id})
                SET c.name = $name,
                    c.visibility = $visibility,
                    c.modifiers = $modifiers,
                    c.is_interface = $isInterface,
                    c.is_enum = $isEnum,
                    c.is_annotation = $isAnnotation,
                    c.file_path = $filePath,
                    c.line_start = $lineStart,
                    c.line_end = $lineEnd,
                    c.package_name = $packageName,
                    c.repository_id = $repositoryId,
                    c.repository_name = $repositoryName,
                    c.repository_url = $repositoryUrl,
                    c.branch = $branch,
                    c.commit_hash = $commitHash,
                    c.commit_date = $commitDate,
                    c.file_relative_path = $fileRelativePath,
                    c.created_at = $createdAt,
                    c.updated_at = $updatedAt
                """;
            
            session.run(cypher, Values.parameters(
                "id", classNode.getId(),
                "name", classNode.getName(),
                "visibility", classNode.getVisibility(),
                "modifiers", classNode.getModifiers(),
                "isInterface", classNode.getIsInterface(),
                "isEnum", classNode.getIsEnum(),
                "isAnnotation", classNode.getIsAnnotation(),
                "filePath", classNode.getFilePath(),
                "lineStart", classNode.getLineStart(),
                "lineEnd", classNode.getLineEnd(),
                "packageName", classNode.getPackageName(),
                "repositoryId", classNode.getRepositoryId(),
                "repositoryName", classNode.getRepositoryName(),
                "repositoryUrl", classNode.getRepositoryUrl(),
                "branch", classNode.getBranch(),
                "commitHash", classNode.getCommitHash(),
                "commitDate", classNode.getCommitDate(),
                "fileRelativePath", classNode.getFileRelativePath(),
                "createdAt", classNode.getCreatedAt(),
                "updatedAt", classNode.getUpdatedAt()
            ));
            
            LOGGER.info("Saved class: {} with ID: {}", classNode.getName(), classNode.getId());
        } catch (Exception e) {
            LOGGER.error("Error saving class: {}", classNode.getName(), e);
            throw new RuntimeException("Failed to save class", e);
        }
    }
    
    @Override
    public void saveMethod(MethodNode methodNode) {
        try (Session session = neo4jDriver.session()) {
            String cypher = """
                MERGE (m:Method {id: $id})
                SET m.name = $name,
                    m.visibility = $visibility,
                    m.modifiers = $modifiers,
                    m.return_type = $returnType,
                    m.parameters = $parameters,
                    m.parameter_names = $parameterNames,
                    m.file_path = $filePath,
                    m.line_start = $lineStart,
                    m.line_end = $lineEnd,
                    m.class_name = $className,
                    m.package_name = $packageName,
                    m.created_at = $createdAt,
                    m.updated_at = $updatedAt
                """;
            
            session.run(cypher, Values.parameters(
                "id", methodNode.getId(),
                "name", methodNode.getName(),
                "visibility", methodNode.getVisibility(),
                "modifiers", methodNode.getModifiers(),
                "returnType", methodNode.getReturnType(),
                "parameters", methodNode.getParameters(),
                "parameterNames", methodNode.getParameterNames(),
                "filePath", methodNode.getFilePath(),
                "lineStart", methodNode.getLineStart(),
                "lineEnd", methodNode.getLineEnd(),
                "className", methodNode.getClassName(),
                "packageName", methodNode.getPackageName(),
                "createdAt", methodNode.getCreatedAt(),
                "updatedAt", methodNode.getUpdatedAt()
            ));
            
            LOGGER.debug("Saved method: {}", methodNode.getName());
        } catch (Exception e) {
            LOGGER.error("Error saving method: {}", methodNode.getName(), e);
            throw new RuntimeException("Failed to save method", e);
        }
    }
    
    @Override
    public void saveField(FieldNode fieldNode) {
        try (Session session = neo4jDriver.session()) {
            String cypher = """
                MERGE (f:Field {id: $id})
                SET f.name = $name,
                    f.visibility = $visibility,
                    f.modifiers = $modifiers,
                    f.type = $type,
                    f.file_path = $filePath,
                    f.line_number = $lineNumber,
                    f.class_name = $className,
                    f.package_name = $packageName,
                    f.created_at = $createdAt,
                    f.updated_at = $updatedAt
                """;
            
            session.run(cypher, Values.parameters(
                "id", fieldNode.getId(),
                "name", fieldNode.getName(),
                "visibility", fieldNode.getVisibility(),
                "modifiers", fieldNode.getModifiers(),
                "type", fieldNode.getType(),
                "filePath", fieldNode.getFilePath(),
                "lineNumber", fieldNode.getLineNumber(),
                "className", fieldNode.getClassName(),
                "packageName", fieldNode.getPackageName(),
                "createdAt", fieldNode.getCreatedAt(),
                "updatedAt", fieldNode.getUpdatedAt()
            ));
            
            LOGGER.debug("Saved field: {}", fieldNode.getName());
        } catch (Exception e) {
            LOGGER.error("Error saving field: {}", fieldNode.getName(), e);
            throw new RuntimeException("Failed to save field", e);
        }
    }
    
    @Override
    public void saveRepository(RepositoryNode repositoryNode) {
        try (Session session = neo4jDriver.session()) {
            String cypher = """
                MERGE (r:Repository {id: $id})
                SET r.name = $name,
                    r.organization = $organization,
                    r.url = $url,
                    r.clone_url = $cloneUrl,
                    r.default_branch = $defaultBranch,
                    r.created_at = $createdAt,
                    r.updated_at = $updatedAt,
                    r.last_commit_hash = $lastCommitHash,
                    r.last_commit_date = $lastCommitDate,
                    r.total_files = $totalFiles,
                    r.language_stats = $languageStats,
                    r.local_path = $localPath
                """;
            
            session.run(cypher, Values.parameters(
                "id", repositoryNode.getId(),
                "name", repositoryNode.getName(),
                "organization", repositoryNode.getOrganization(),
                "url", repositoryNode.getUrl(),
                "cloneUrl", repositoryNode.getCloneUrl(),
                "defaultBranch", repositoryNode.getDefaultBranch(),
                "createdAt", repositoryNode.getCreatedAt(),
                "updatedAt", repositoryNode.getUpdatedAt(),
                "lastCommitHash", repositoryNode.getLastCommitHash(),
                "lastCommitDate", repositoryNode.getLastCommitDate(),
                "totalFiles", repositoryNode.getTotalFiles(),
                "languageStats", repositoryNode.getLanguageStats(),
                "localPath", repositoryNode.getLocalPath()
            ));
            
            LOGGER.info("Saved repository: {} with ID: {}", repositoryNode.getName(), repositoryNode.getId());
        } catch (Exception e) {
            LOGGER.error("Error saving repository: {}", repositoryNode.getName(), e);
            throw new RuntimeException("Failed to save repository", e);
        }
    }
    
    @Override
    public void createRelationship(String fromId, String toId, String relationshipType) {
        createRelationship(fromId, toId, relationshipType, null);
    }
    
    @Override
    public void createRelationship(String fromId, String toId, String relationshipType, Map<String, Object> properties) {
        try (Session session = neo4jDriver.session()) {
            String cypher = """
                MATCH (from {id: $fromId}), (to {id: $toId})
                MERGE (from)-[r:%s]->(to)
                """.formatted(relationshipType);
            
            if (properties != null && !properties.isEmpty()) {
                cypher += " SET r += $properties";
                session.run(cypher, Values.parameters(
                    "fromId", fromId,
                    "toId", toId,
                    "properties", properties
                ));
            } else {
                session.run(cypher, Values.parameters(
                    "fromId", fromId,
                    "toId", toId
                ));
            }
            
            LOGGER.debug("Created relationship: {} -> {} -> {}", fromId, relationshipType, toId);
        } catch (Exception e) {
            LOGGER.error("Error creating relationship: {} -> {} -> {}", fromId, relationshipType, toId, e);
            throw new RuntimeException("Failed to create relationship", e);
        }
    }
} 