package io.minijmix.core.security;

import java.util.Set;

/**
 * Manages access control for entities and operations.
 * Inspired by Jmix AccessManager.
 */
public interface AccessManager {
    
    /**
     * Check if current user can perform the operation on the entity class
     */
    boolean isEntityOperationPermitted(Class<?> entityClass, String operation);
    
    /**
     * Check if current user can access the specified entity property
     */
    boolean isEntityPropertyPermitted(Class<?> entityClass, String property);
    
    /**
     * Get all roles assigned to the current user
     */
    Set<Role> getCurrentUserRoles();
    
    /**
     * Apply security filtering to entities based on current user's roles
     */
    <T> T applyConstraints(T entity);
}