package io.minijmix.core.metadata;

/**
 * Central interface for accessing metadata about entities.
 * Inspired by Jmix Metadata interface.
 */
public interface Metadata {
    
    /**
     * Get MetaClass for a given entity class
     */
    MetaClass getClass(Class<?> entityClass);
    
    /**
     * Get MetaClass for a given entity instance
     */
    MetaClass getClass(Object entity);
    
    /**
     * Get MetaClass by entity name
     */
    MetaClass getClass(String entityName);
    
    /**
     * Create a new instance of the specified entity class
     */
    <T> T create(Class<T> entityClass);
    
    /**
     * Register a new entity class with the metadata system
     */
    void registerClass(Class<?> entityClass);
}