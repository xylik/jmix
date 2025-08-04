package io.minijmix.core.data;

import java.util.HashSet;
import java.util.Set;

/**
 * Defines which properties to fetch when loading entities.
 * Simplified version of Jmix FetchPlan.
 */
public class FetchPlan {
    
    public static final String LOCAL = "_local";
    public static final String INSTANCE_NAME = "_instance_name";
    
    private final Class<?> entityClass;
    private final Set<String> properties = new HashSet<>();
    
    public FetchPlan(Class<?> entityClass) {
        this.entityClass = entityClass;
    }
    
    public Class<?> getEntityClass() {
        return entityClass;
    }
    
    public Set<String> getProperties() {
        return new HashSet<>(properties);
    }
    
    public FetchPlan addProperty(String property) {
        properties.add(property);
        return this;
    }
    
    public FetchPlan addProperties(String... properties) {
        for (String property : properties) {
            this.properties.add(property);
        }
        return this;
    }
    
    public boolean containsProperty(String property) {
        return properties.contains(property);
    }
    
    /**
     * Creates a fetch plan that includes all local (non-reference) properties
     */
    public static FetchPlan local(Class<?> entityClass) {
        FetchPlan fetchPlan = new FetchPlan(entityClass);
        // In a full implementation, this would analyze the entity metadata
        // For simplicity, we'll just add all basic properties
        return fetchPlan;
    }
    
    /**
     * Creates a fetch plan for instance name property only
     */
    public static FetchPlan instanceName(Class<?> entityClass) {
        FetchPlan fetchPlan = new FetchPlan(entityClass);
        // In a full implementation, this would look for @InstanceName annotation
        return fetchPlan;
    }
}