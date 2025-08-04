package io.minijmix.core.ui;

/**
 * Factory for creating UI components based on entity metadata.
 * Inspired by Jmix UiComponents.
 */
public interface UiComponents {
    
    /**
     * Create a list view for the specified entity class
     */
    <T> EntityView<T> createEntityListView(Class<T> entityClass);
    
    /**
     * Create an edit view for the specified entity class
     */
    <T> EntityView<T> createEntityEditView(Class<T> entityClass);
    
    /**
     * Create a form component for editing entity properties
     */
    <T> Object createEntityForm(Class<T> entityClass);
}