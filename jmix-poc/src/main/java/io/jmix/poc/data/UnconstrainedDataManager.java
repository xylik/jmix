package io.jmix.poc.data;

import io.jmix.poc.core.Entity;
import java.util.List;

/**
 * Central interface for CRUD operations without security constraints.
 * 
 * Demonstrates Jmix's approach to:
 * - Fluent API design
 * - Unconstrained data access
 * - Load context and save context patterns
 */
public interface UnconstrainedDataManager {
    
    /**
     * Save entity and reload it to return the saved instance.
     */
    <E> E save(E entity);
    
    /**
     * Save entities without reloading.
     */
    void saveWithoutReload(Object... entities);
    
    /**
     * Remove entity.
     */
    void remove(Object entity);
    
    /**
     * Remove entity by id.
     */
    <E> void remove(Class<E> entityClass, Object id);
    
    /**
     * Entry point to fluent API for loading entities.
     * 
     * Usage:
     * Customer customer = dataManager.load(Customer.class).id(someId).one();
     */
    <E> FluentLoader<E> load(Class<E> entityClass);
    
    /**
     * Entry point to fluent API for loading by id.
     * 
     * Usage:
     * Customer customer = dataManager.load(Customer.class, customerId).one();
     */
    <E> FluentLoader.ById<E> load(Class<E> entityClass, Object id);
    
    /**
     * Create new entity instance.
     */
    <T> T create(Class<T> entityClass);
    
    /**
     * Create reference to existing entity.
     * Useful for setting relationships without loading the full entity.
     */
    <T> T getReference(Class<T> entityClass, Object id);
}