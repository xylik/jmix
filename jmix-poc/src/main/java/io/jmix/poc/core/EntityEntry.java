package io.jmix.poc.core;

/**
 * Entity entry provides metadata and state management for entities.
 * 
 * Demonstrates Jmix's approach to:
 * - Entity state tracking
 * - Change detection
 * - Metadata access
 * - Identifier management
 */
public interface EntityEntry {
    
    /**
     * Get the entity this entry belongs to.
     */
    Entity getEntity();
    
    /**
     * Get entity identifier.
     */
    Object getEntityId();
    
    /**
     * Set entity identifier.
     */
    void setEntityId(Object id);
    
    /**
     * Check if entity has been modified.
     */
    boolean isModified();
    
    /**
     * Mark entity as modified.
     */
    void setModified(boolean modified);
    
    /**
     * Get entity class.
     */
    Class<?> getEntityClass();
    
    /**
     * Check if entity is new (not yet persisted).
     */
    boolean isNew();
    
    /**
     * Mark entity as new or existing.
     */
    void setNew(boolean isNew);
}