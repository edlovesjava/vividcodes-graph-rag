package com.vividcodes.graphrag.config;

/**
 * Enumeration defining different modes for handling node operations in the graph database.
 * Controls whether to create new nodes, update existing ones, or perform intelligent upserts.
 * 
 * @author Graph RAG System
 * @version 1.0
 * @since 2024
 */
public enum UpsertMode {
    
    /**
     * Insert new nodes only. Throws error if node already exists.
     * This mode is useful for initial data loads or when data integrity 
     * requires avoiding duplicate nodes.
     */
    INSERT_ONLY("insert_only", "Insert new nodes only, fail if node exists"),
    
    /**
     * Intelligent upsert: create new nodes if they don't exist, 
     * update existing nodes only if changes are detected.
     * This is the recommended mode for most use cases as it provides
     * optimal performance and data consistency.
     */
    UPSERT("upsert", "Create or update nodes intelligently based on changes"),
    
    /**
     * Update existing nodes only. Throws error if node doesn't exist.
     * This mode is useful for batch updates or when working with 
     * a known set of existing data.
     */
    UPDATE_ONLY("update_only", "Update existing nodes only, fail if node doesn't exist");
    
    private final String value;
    private final String description;
    
    UpsertMode(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Parse an UpsertMode from a string value.
     * 
     * @param value The string value to parse
     * @return The corresponding UpsertMode
     * @throws IllegalArgumentException if the value doesn't match any mode
     */
    public static UpsertMode fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return UPSERT; // Default mode
        }
        
        for (UpsertMode mode : values()) {
            if (mode.value.equalsIgnoreCase(value.trim())) {
                return mode;
            }
        }
        
        throw new IllegalArgumentException("Unknown UpsertMode: " + value + 
                ". Valid values are: insert_only, upsert, update_only");
    }
    
    /**
     * Check if this mode allows creating new nodes.
     * 
     * @return true if new nodes can be created in this mode
     */
    public boolean allowsInsert() {
        return this == INSERT_ONLY || this == UPSERT;
    }
    
    /**
     * Check if this mode allows updating existing nodes.
     * 
     * @return true if existing nodes can be updated in this mode
     */
    public boolean allowsUpdate() {
        return this == UPDATE_ONLY || this == UPSERT;
    }
    
    /**
     * Check if this mode performs intelligent upserts.
     * 
     * @return true if this mode supports intelligent create-or-update logic
     */
    public boolean isUpsertMode() {
        return this == UPSERT;
    }
    
    @Override
    public String toString() {
        return value + " (" + description + ")";
    }
}
