package io.jmix.poc.data;

import java.util.List;
import java.util.Optional;

/**
 * Fluent API for loading entities.
 * 
 * Demonstrates Jmix's fluent interface design:
 * - Method chaining for readability
 * - Progressive disclosure of options
 * - Type-safe API
 */
public interface FluentLoader<E> {
    
    /**
     * Set entity id to load.
     */
    ById<E> id(Object id);
    
    /**
     * Set query for loading.
     */
    FluentLoader<E> query(String query);
    
    /**
     * Set query parameter.
     */
    FluentLoader<E> parameter(String name, Object value);
    
    /**
     * Set fetch plan to control what data to load.
     */
    FluentLoader<E> fetchPlan(String fetchPlanName);
    
    /**
     * Set fetch plan.
     */
    FluentLoader<E> fetchPlan(FetchPlan fetchPlan);
    
    /**
     * Set maximum number of results.
     */
    FluentLoader<E> maxResults(int maxResults);
    
    /**
     * Set first result offset.
     */
    FluentLoader<E> firstResult(int firstResult);
    
    /**
     * Load single entity. Throws exception if more than one found.
     */
    E one();
    
    /**
     * Load single entity or return null if not found.
     */
    Optional<E> optional();
    
    /**
     * Load list of entities.
     */
    List<E> list();
    
    /**
     * Fluent loader for loading by id.
     */
    interface ById<E> {
        
        /**
         * Set fetch plan.
         */
        ById<E> fetchPlan(String fetchPlanName);
        
        /**
         * Set fetch plan.
         */
        ById<E> fetchPlan(FetchPlan fetchPlan);
        
        /**
         * Load the entity.
         */
        E one();
        
        /**
         * Load entity or return null if not found.
         */
        Optional<E> optional();
    }
}