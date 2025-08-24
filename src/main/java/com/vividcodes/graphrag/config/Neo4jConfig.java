package com.vividcodes.graphrag.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Configuration
@EnableNeo4jRepositories(basePackages = "com.vividcodes.graphrag.repository")
public class Neo4jConfig {
    
    @Value("${spring.neo4j.uri}")
    private String uri;
    
    @Value("${spring.neo4j.authentication.username}")
    private String username;
    
    @Value("${spring.neo4j.authentication.password}")
    private String password;
    
    @Bean
    public Driver neo4jDriver() {
        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
        
        // Add shutdown hook to properly close the driver
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                driver.close();
            } catch (Exception e) {
                // Log but don't throw during shutdown
                System.err.println("Error closing Neo4j driver: " + e.getMessage());
            }
        }));
        
        return driver;
    }
} 