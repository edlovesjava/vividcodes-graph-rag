package com.vividcodes.graphrag.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.vividcodes.graphrag.model.dto.ValidationResult;

@Component
public class QueryValidator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryValidator.class);
    
    // Patterns for dangerous operations
    private static final Pattern DANGEROUS_OPERATIONS = Pattern.compile(
        "\\b(DELETE|DROP|CREATE\\s+INDEX|CREATE\\s+CONSTRAINT|REMOVE|SET\\s+\\w+\\s*=\\s*null)\\b",
        Pattern.CASE_INSENSITIVE
    );
    
    // Pattern for basic Cypher syntax validation
    private static final Pattern BASIC_CYPHER_PATTERN = Pattern.compile(
        "\\b(MATCH|RETURN|WHERE|WITH|UNWIND|OPTIONAL\\s+MATCH|CREATE|MERGE|SET|DELETE|REMOVE|CALL|PROFILE|EXPLAIN)\\b",
        Pattern.CASE_INSENSITIVE
    );
    
    // Maximum query length to prevent overly complex queries
    private static final int MAX_QUERY_LENGTH = 10000;
    
    // Maximum number of clauses to prevent overly complex queries
    private static final int MAX_CLAUSES = 20;
    
    public ValidationResult validate(String cypherQuery) {
        List<String> errors = new ArrayList<>();
        
        if (cypherQuery == null || cypherQuery.trim().isEmpty()) {
            errors.add("Query cannot be null or empty");
            return new ValidationResult(false, errors);
        }
        
        // Check for dangerous operations
        if (containsDangerousOperations(cypherQuery)) {
            errors.add("Query contains dangerous operations (DELETE, DROP, CREATE INDEX, CREATE CONSTRAINT, etc.)");
        }
        
        // Validate basic syntax
        if (!hasValidBasicSyntax(cypherQuery)) {
            errors.add("Query does not contain valid Cypher keywords");
        }
        
        // Check query complexity
        if (isTooComplex(cypherQuery)) {
            errors.add("Query is too complex (too long or too many clauses)");
        }
        
        // Check for balanced parentheses
        if (!hasBalancedParentheses(cypherQuery)) {
            errors.add("Query has unbalanced parentheses");
        }
        
        // Check for balanced braces
        if (!hasBalancedBraces(cypherQuery)) {
            errors.add("Query has unbalanced braces");
        }
        
        boolean isValid = errors.isEmpty();
        
        if (!isValid) {
            LOGGER.warn("Query validation failed: {}", String.join(", ", errors));
        }
        
        return new ValidationResult(isValid, errors);
    }
    
    private boolean containsDangerousOperations(String query) {
        return DANGEROUS_OPERATIONS.matcher(query).find();
    }
    
    private boolean hasValidBasicSyntax(String query) {
        return BASIC_CYPHER_PATTERN.matcher(query).find();
    }
    
    private boolean isTooComplex(String query) {
        // Check query length
        if (query.length() > MAX_QUERY_LENGTH) {
            return true;
        }
        
        // Count clauses (rough estimate)
        String[] clauses = query.split("\\b(MATCH|RETURN|WHERE|WITH|UNWIND|OPTIONAL\\s+MATCH|CREATE|MERGE|SET|DELETE|REMOVE|CALL|PROFILE|EXPLAIN)\\b");
        if (clauses.length > MAX_CLAUSES) {
            return true;
        }
        
        return false;
    }
    
    private boolean hasBalancedParentheses(String query) {
        int count = 0;
        for (char c : query.toCharArray()) {
            if (c == '(') {
                count++;
            } else if (c == ')') {
                count--;
                if (count < 0) {
                    return false;
                }
            }
        }
        return count == 0;
    }
    
    private boolean hasBalancedBraces(String query) {
        int count = 0;
        for (char c : query.toCharArray()) {
            if (c == '{') {
                count++;
            } else if (c == '}') {
                count--;
                if (count < 0) {
                    return false;
                }
            }
        }
        return count == 0;
    }
}
