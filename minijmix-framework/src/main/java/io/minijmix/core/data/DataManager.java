package io.minijmix.core.data;

import java.util.List;

/**
 * Central interface for data access operations.
 * Inspired by Jmix DataManager.
 */
public interface DataManager {
    
    /**
     * Load a single entity by ID
     */
    <T> T load(Class<T> entityClass, Object id);
    
    /**
     * Load all entities of a given type
     */
    <T> List<T> loadAll(Class<T> entityClass);
    
    /**
     * Load entities with a custom load context
     */
    <T> List<T> load(LoadContext<T> loadContext);
    
    /**
     * Save an entity (insert or update)
     */
    <T> T save(T entity);
    
    /**
     * Save multiple entities
     */
    <T> List<T> saveAll(List<T> entities);
    
    /**
     * Delete an entity
     */
    void delete(Object entity);
    
    /**
     * Delete an entity by ID
     */
    <T> void deleteById(Class<T> entityClass, Object id);
    
    /**
     * Count entities of a given type
     */
    <T> long count(Class<T> entityClass);
}