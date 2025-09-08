package com.vividcodes.graphrag.model.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Result of comparing an existing node with an incoming node to determine changes.
 * 
 * @author Graph RAG System
 * @version 1.0
 * @since 2024
 */
public class NodeComparisonResult {

    public enum ComparisonOutcome {
        IDENTICAL,         // Nodes are exactly the same
        PROPERTIES_CHANGED, // Some properties have changed
        STRUCTURE_CHANGED,  // Fundamental structure has changed (rare)
        CONFLICT           // Conflicting changes that need resolution
    }

    private final ComparisonOutcome outcome;
    private final Map<String, PropertyChange> propertyChanges;
    private final boolean requiresUpdate;
    private final String conflictReason;

    private NodeComparisonResult(ComparisonOutcome outcome, Map<String, PropertyChange> propertyChanges,
                                boolean requiresUpdate, String conflictReason) {
        this.outcome = outcome;
        this.propertyChanges = propertyChanges != null ? propertyChanges : new HashMap<>();
        this.requiresUpdate = requiresUpdate;
        this.conflictReason = conflictReason;
    }

    // Static factory methods
    public static NodeComparisonResult identical() {
        return new NodeComparisonResult(ComparisonOutcome.IDENTICAL, null, false, null);
    }

    public static NodeComparisonResult propertiesChanged(Map<String, PropertyChange> changes) {
        return new NodeComparisonResult(ComparisonOutcome.PROPERTIES_CHANGED, changes, true, null);
    }

    public static NodeComparisonResult structureChanged(Map<String, PropertyChange> changes) {
        return new NodeComparisonResult(ComparisonOutcome.STRUCTURE_CHANGED, changes, true, null);
    }

    public static NodeComparisonResult conflict(String reason) {
        return new NodeComparisonResult(ComparisonOutcome.CONFLICT, null, false, reason);
    }

    // Getters
    public ComparisonOutcome getOutcome() {
        return outcome;
    }

    public Map<String, PropertyChange> getPropertyChanges() {
        return propertyChanges;
    }

    public boolean requiresUpdate() {
        return requiresUpdate;
    }

    public String getConflictReason() {
        return conflictReason;
    }

    // Helper methods
    public boolean isIdentical() {
        return outcome == ComparisonOutcome.IDENTICAL;
    }

    public boolean hasChanges() {
        return requiresUpdate;
    }

    public boolean hasConflict() {
        return outcome == ComparisonOutcome.CONFLICT;
    }

    public int getChangeCount() {
        return propertyChanges.size();
    }

    @Override
    public String toString() {
        return String.format("NodeComparisonResult{outcome=%s, changes=%d, requiresUpdate=%s}",
                outcome, getChangeCount(), requiresUpdate);
    }

    /**
     * Represents a change to a single property.
     */
    public static class PropertyChange {
        public enum ChangeType {
            ADDED,      // Property was added
            REMOVED,    // Property was removed
            MODIFIED,   // Property value was changed
            TYPE_CHANGED // Property type was changed
        }

        private final ChangeType changeType;
        private final Object oldValue;
        private final Object newValue;
        private final String propertyType;
        private final boolean isSignificant;

        public PropertyChange(ChangeType changeType, Object oldValue, Object newValue,
                            String propertyType, boolean isSignificant) {
            this.changeType = changeType;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.propertyType = propertyType;
            this.isSignificant = isSignificant;
        }

        // Static factory methods for common scenarios
        public static PropertyChange added(Object newValue, String propertyType) {
            return new PropertyChange(ChangeType.ADDED, null, newValue, propertyType, true);
        }

        public static PropertyChange removed(Object oldValue, String propertyType) {
            return new PropertyChange(ChangeType.REMOVED, oldValue, null, propertyType, true);
        }

        public static PropertyChange modified(Object oldValue, Object newValue, String propertyType, boolean isSignificant) {
            return new PropertyChange(ChangeType.MODIFIED, oldValue, newValue, propertyType, isSignificant);
        }

        public static PropertyChange typeChanged(Object oldValue, Object newValue, String propertyType) {
            return new PropertyChange(ChangeType.TYPE_CHANGED, oldValue, newValue, propertyType, true);
        }

        // Getters
        public ChangeType getChangeType() {
            return changeType;
        }

        public Object getOldValue() {
            return oldValue;
        }

        public Object getNewValue() {
            return newValue;
        }

        public String getPropertyType() {
            return propertyType;
        }

        public boolean isSignificant() {
            return isSignificant;
        }

        @Override
        public String toString() {
            return String.format("PropertyChange{type=%s, oldValue=%s, newValue=%s, significant=%s}",
                    changeType, oldValue, newValue, isSignificant);
        }
    }
}
