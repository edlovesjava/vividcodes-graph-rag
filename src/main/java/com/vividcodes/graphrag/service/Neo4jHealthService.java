package com.vividcodes.graphrag.service;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Neo4jHealthService {
    
    private static final Logger logger = LoggerFactory.getLogger(Neo4jHealthService.class);
    
    private final Driver neo4jDriver;
    
    @Autowired
    public Neo4jHealthService(Driver neo4jDriver) {
        this.neo4jDriver = neo4jDriver;
    }
    
    public boolean isHealthy() {
        try (Session session = neo4jDriver.session()) {
            Result result = session.run("RETURN 1 as test");
            result.single();
            logger.info("Neo4j connection is healthy");
            return true;
        } catch (Exception e) {
            logger.error("Neo4j connection failed: {}", e.getMessage(), e);
            return false;
        }
    }
    
    public String getNeo4jVersion() {
        try (Session session = neo4jDriver.session()) {
            Result result = session.run("CALL dbms.components() YIELD name, versions, edition RETURN name, versions[0] as version, edition");
            return result.single().get("version").asString();
        } catch (Exception e) {
            logger.error("Failed to get Neo4j version: {}", e.getMessage(), e);
            return "Unknown";
        }
    }
} 