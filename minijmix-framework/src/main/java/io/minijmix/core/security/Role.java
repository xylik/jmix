package io.minijmix.core.security;

/**
 * Represents a security role that can be assigned to users.
 * Simplified version of Jmix role system.
 */
public interface Role {
    
    /**
     * Get the role name/code
     */
    String getName();
    
    /**
     * Get human-readable role description
     */
    String getDescription();
    
    /**
     * Check if this role allows the specified operation on the entity
     */
    boolean allows(String operation, Class<?> entityClass);
    
    /**
     * Check if this role allows access to the specified entity property
     */
    boolean allowsProperty(Class<?> entityClass, String property);
}