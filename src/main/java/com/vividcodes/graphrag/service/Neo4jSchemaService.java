package com.vividcodes.graphrag.service;

import java.util.Arrays;
import java.util.List;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

/**
 * Service responsible for initializing Neo4j database schema,
 * including indexes and constraints for optimal performance.
 */
@Service
public class Neo4jSchemaService implements ApplicationRunner {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jSchemaService.class);
    
    private final Driver neo4jDriver;
    
    @Autowired
    public Neo4jSchemaService(Driver neo4jDriver) {
        this.neo4jDriver = neo4jDriver;
    }
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOGGER.info("Initializing Neo4j database schema...");
        initializeSchema();
        LOGGER.info("Neo4j database schema initialization completed");
    }
    
    /**
     * Initialize the database schema by creating indexes and constraints
     */
    public void initializeSchema() {
        try {
            createBasicIndexes();
            createConstraints();
            createCompositeIndexes();
            createFullTextIndexes();
        } catch (Exception e) {
            LOGGER.error("Error initializing Neo4j schema", e);
            // Don't throw exception to prevent application startup failure
        }
    }
    
    /**
     * Create basic single-property indexes
     */
    private void createBasicIndexes() {
        LOGGER.info("Creating basic indexes...");
        
        List<String> indexQueries = Arrays.asList(
            // Repository indexes
            "CREATE INDEX IF NOT EXISTS FOR (r:Repository) ON (r.id)",
            "CREATE INDEX IF NOT EXISTS FOR (r:Repository) ON (r.name)",
            "CREATE INDEX IF NOT EXISTS FOR (r:Repository) ON (r.organization)",
            
            // SubProject indexes
            "CREATE INDEX IF NOT EXISTS FOR (sp:SubProject) ON (sp.id)",
            "CREATE INDEX IF NOT EXISTS FOR (sp:SubProject) ON (sp.name)",
            "CREATE INDEX IF NOT EXISTS FOR (sp:SubProject) ON (sp.path)",
            "CREATE INDEX IF NOT EXISTS FOR (sp:SubProject) ON (sp.type)",
            "CREATE INDEX IF NOT EXISTS FOR (sp:SubProject) ON (sp.repository_id)",
            
            // Package indexes
            "CREATE INDEX IF NOT EXISTS FOR (p:Package) ON (p.id)",
            "CREATE INDEX IF NOT EXISTS FOR (p:Package) ON (p.name)",
            "CREATE INDEX IF NOT EXISTS FOR (p:Package) ON (p.path)",
            
            // Class indexes
            "CREATE INDEX IF NOT EXISTS FOR (c:Class) ON (c.id)",
            "CREATE INDEX IF NOT EXISTS FOR (c:Class) ON (c.name)",
            "CREATE INDEX IF NOT EXISTS FOR (c:Class) ON (c.package_name)",
            "CREATE INDEX IF NOT EXISTS FOR (c:Class) ON (c.repository_id)",
            
            // Method indexes
            "CREATE INDEX IF NOT EXISTS FOR (m:Method) ON (m.id)",
            "CREATE INDEX IF NOT EXISTS FOR (m:Method) ON (m.name)",
            "CREATE INDEX IF NOT EXISTS FOR (m:Method) ON (m.class_name)",
            "CREATE INDEX IF NOT EXISTS FOR (m:Method) ON (m.visibility)",
            
            // Field indexes
            "CREATE INDEX IF NOT EXISTS FOR (f:Field) ON (f.id)",
            "CREATE INDEX IF NOT EXISTS FOR (f:Field) ON (f.name)",
            "CREATE INDEX IF NOT EXISTS FOR (f:Field) ON (f.class_name)"
        );
        
        executeQueries(indexQueries);
        LOGGER.info("Basic indexes created successfully");
    }
    
    /**
     * Create unique constraints
     */
    private void createConstraints() {
        LOGGER.info("Creating constraints...");
        
        List<String> constraintQueries = Arrays.asList(
            "CREATE CONSTRAINT IF NOT EXISTS FOR (r:Repository) REQUIRE r.id IS UNIQUE",
            "CREATE CONSTRAINT IF NOT EXISTS FOR (sp:SubProject) REQUIRE sp.id IS UNIQUE",
            "CREATE CONSTRAINT IF NOT EXISTS FOR (p:Package) REQUIRE p.id IS UNIQUE",
            "CREATE CONSTRAINT IF NOT EXISTS FOR (c:Class) REQUIRE c.id IS UNIQUE",
            "CREATE CONSTRAINT IF NOT EXISTS FOR (m:Method) REQUIRE m.id IS UNIQUE",
            "CREATE CONSTRAINT IF NOT EXISTS FOR (f:Field) REQUIRE f.id IS UNIQUE"
        );
        
        executeQueries(constraintQueries);
        LOGGER.info("Constraints created successfully");
    }
    
    /**
     * Create composite indexes for common query patterns
     */
    private void createCompositeIndexes() {
        LOGGER.info("Creating composite indexes...");
        
        List<String> compositeIndexQueries = Arrays.asList(
            "CREATE INDEX IF NOT EXISTS FOR (c:Class) ON (c.repository_id, c.package_name)",
            "CREATE INDEX IF NOT EXISTS FOR (m:Method) ON (m.class_name, m.visibility)",
            "CREATE INDEX IF NOT EXISTS FOR (sp:SubProject) ON (sp.repository_id, sp.type)"
        );
        
        executeQueries(compositeIndexQueries);
        LOGGER.info("Composite indexes created successfully");
    }
    
    /**
     * Create full-text search indexes
     */
    private void createFullTextIndexes() {
        LOGGER.info("Creating full-text search indexes...");
        
        List<String> fullTextQueries = Arrays.asList(
            "CALL db.index.fulltext.createNodeIndex('classNameSearch', ['Class'], ['name']) YIELD name RETURN name",
            "CALL db.index.fulltext.createNodeIndex('methodNameSearch', ['Method'], ['name']) YIELD name RETURN name",
            "CALL db.index.fulltext.createNodeIndex('packageNameSearch', ['Package'], ['name']) YIELD name RETURN name",
            "CALL db.index.fulltext.createNodeIndex('subProjectNameSearch', ['SubProject'], ['name', 'description']) YIELD name RETURN name"
        );
        
        // Full-text indexes need special handling as they return results
        try (Session session = neo4jDriver.session()) {
            for (String query : fullTextQueries) {
                try {
                    session.run(query);
                    LOGGER.debug("Executed full-text index query: {}", query);
                } catch (Exception e) {
                    // Index might already exist, which is fine
                    if (!e.getMessage().contains("already exists")) {
                        LOGGER.warn("Warning creating full-text index: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error creating full-text indexes", e);
        }
        
        LOGGER.info("Full-text search indexes created successfully");
    }
    
    /**
     * Execute a list of Cypher queries
     */
    private void executeQueries(List<String> queries) {
        try (Session session = neo4jDriver.session()) {
            for (String query : queries) {
                try {
                    session.run(query);
                    LOGGER.debug("Executed query: {}", query);
                } catch (Exception e) {
                    // Many operations are idempotent, so existing indexes/constraints are fine
                    if (e.getMessage().contains("already exists") || 
                        e.getMessage().contains("An equivalent")) {
                        LOGGER.debug("Index/constraint already exists: {}", query);
                    } else {
                        LOGGER.warn("Warning executing query: {} - {}", query, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error executing schema queries", e);
            throw new RuntimeException("Failed to execute schema queries", e);
        }
    }
    
    /**
     * Check if indexes are properly created
     */
    public boolean validateSchema() {
        try (Session session = neo4jDriver.session()) {
            // Check if SubProject indexes exist
            var result = session.run("SHOW INDEXES YIELD name, labelsOrTypes, properties WHERE 'SubProject' IN labelsOrTypes");
            
            int subProjectIndexCount = 0;
            while (result.hasNext()) {
                result.next();
                subProjectIndexCount++;
            }
            
            LOGGER.info("Found {} SubProject indexes", subProjectIndexCount);
            return subProjectIndexCount >= 5; // We expect at least 5 SubProject indexes
            
        } catch (Exception e) {
            LOGGER.error("Error validating schema", e);
            return false;
        }
    }
    
    /**
     * Get schema information for debugging
     */
    public void logSchemaInfo() {
        try (Session session = neo4jDriver.session()) {
            LOGGER.info("=== Neo4j Schema Information ===");
            
            // Log indexes
            var indexResult = session.run("SHOW INDEXES");
            LOGGER.info("Database Indexes:");
            while (indexResult.hasNext()) {
                var record = indexResult.next();
                LOGGER.info("  {} - {} on {}", 
                    record.get("name").asString(),
                    record.get("labelsOrTypes").asList(),
                    record.get("properties").asList()
                );
            }
            
            // Log constraints
            var constraintResult = session.run("SHOW CONSTRAINTS");
            LOGGER.info("Database Constraints:");
            while (constraintResult.hasNext()) {
                var record = constraintResult.next();
                LOGGER.info("  {} - {}", 
                    record.get("name").asString(),
                    record.get("description").asString()
                );
            }
            
            LOGGER.info("=== End Schema Information ===");
            
        } catch (Exception e) {
            LOGGER.error("Error retrieving schema information", e);
        }
    }
}
