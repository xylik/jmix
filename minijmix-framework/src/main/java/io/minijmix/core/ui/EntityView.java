package io.minijmix.core.ui;

import java.util.List;

/**
 * Base interface for UI views that work with entities.
 * Inspired by Jmix View system.
 */
public interface EntityView<T> {
    
    /**
     * Get the entity class this view works with
     */
    Class<T> getEntityClass();
    
    /**
     * Load and display entities
     */
    void loadEntities();
    
    /**
     * Refresh the view
     */
    void refresh();
    
    /**
     * Get currently selected entity
     */
    T getSelectedEntity();
    
    /**
     * Set the entities to display
     */
    void setEntities(List<T> entities);
}