package com.vividcodes.graphrag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "parser")
public class ParserConfig {
    
    private boolean includePrivate = false;
    private boolean includeTests = false;
    private long maxFileSize = 10 * 1024 * 1024; // 10MB
    private String supportedExtensions = "java";
    private String upsertMode = "upsert"; // Default to intelligent upsert mode
    private boolean enableAuditTrail = true;
    
    public boolean isIncludePrivate() {
        return includePrivate;
    }
    
    public void setIncludePrivate(boolean includePrivate) {
        this.includePrivate = includePrivate;
    }
    
    public boolean isIncludeTests() {
        return includeTests;
    }
    
    public void setIncludeTests(boolean includeTests) {
        this.includeTests = includeTests;
    }
    
    public long getMaxFileSize() {
        return maxFileSize;
    }
    
    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
    
    public String getSupportedExtensions() {
        return supportedExtensions;
    }
    
    public void setSupportedExtensions(String supportedExtensions) {
        this.supportedExtensions = supportedExtensions;
    }
    
    public String getUpsertMode() {
        return upsertMode;
    }
    
    public void setUpsertMode(String upsertMode) {
        this.upsertMode = upsertMode;
    }
    
    /**
     * Get the UpsertMode enum value from the configured string.
     * 
     * @return UpsertMode enum value
     */
    public UpsertMode getUpsertModeEnum() {
        return UpsertMode.fromValue(upsertMode);
    }
    
    public boolean isEnableAuditTrail() {
        return enableAuditTrail;
    }
    
    public void setEnableAuditTrail(boolean enableAuditTrail) {
        this.enableAuditTrail = enableAuditTrail;
    }
    
    /**
     * Validate the parser configuration.
     * 
     * @throws IllegalArgumentException if configuration is invalid
     */
    public void validate() {
        if (maxFileSize <= 0) {
            throw new IllegalArgumentException("maxFileSize must be positive");
        }
        
        if (supportedExtensions == null || supportedExtensions.trim().isEmpty()) {
            throw new IllegalArgumentException("supportedExtensions cannot be null or empty");
        }
        
        // Validate upsert mode
        try {
            UpsertMode.fromValue(upsertMode);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid upsertMode configuration: " + e.getMessage());
        }
    }
} 