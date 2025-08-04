package io.jmix.poc.core;

import java.io.Serializable;

/**
 * Core interface for domain model objects with identifiers.
 * 
 * Simplified version of Jmix Entity interface demonstrating:
 * - Entity state management through EntityEntry
 * - Identifier abstraction
 * - Serialization support
 */
public interface Entity extends Serializable {
    
    /**
     * Get the entity entry containing metadata and state information.
     * Entity entry provides access to:
     * - Entity metadata (class, properties)
     * - Change tracking
     * - Identifier management
     * 
     * @return EntityEntry instance for this entity
     */
    default EntityEntry __getEntityEntry() {
        throw new UnsupportedOperationException("Entity enhancement required");
    }
    
    /**
     * Copy entity entry state.
     * Used during entity cloning and state management.
     */
    default void __copyEntityEntry() {
        throw new UnsupportedOperationException("Entity enhancement required");
    }
}