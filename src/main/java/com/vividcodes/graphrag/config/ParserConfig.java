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
} 