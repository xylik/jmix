package io.jmix.poc.data;

/**
 * Secure data manager that applies access constraints.
 * 
 * Demonstrates Jmix's security-first approach:
 * - All operations go through security layer
 * - Access to unconstrained operations when needed
 * - Same API as unconstrained manager
 */
public interface DataManager extends UnconstrainedDataManager {
    
    /**
     * Access to unconstrained data manager that bypasses security.
     * Use with caution - typically for system operations.
     */
    UnconstrainedDataManager unconstrained();
}