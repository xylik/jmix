package io.jmix.poc.core;

/**
 * Central interface for metadata-related functionality.
 * 
 * Demonstrates Jmix's metadata-driven approach:
 * - Dynamic entity creation
 * - Class management
 * - Entity introspection
 */
public interface Metadata {
    
    /**
     * Create a new entity instance taking into account extended entities.
     * 
     * @param entityClass entity class
     * @return new entity instance
     */
    <T> T create(Class<T> entityClass);
    
    /**
     * Create entity instance with provided id.
     * 
     * @param entityClass entity class
     * @param id entity identifier
     * @return new entity instance with id set
     */
    <T> T create(Class<T> entityClass, Object id);
    
    /**
     * Get entity class for the given entity instance.
     */
    Class<?> getClass(Object entity);
    
    /**
     * Check if class is an entity.
     */
    boolean isEntity(Class<?> clazz);
    
    /**
     * Get entity name for the class.
     */
    String getEntityName(Class<?> entityClass);
}