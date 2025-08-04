package io.minijmix.core.metadata;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Metadata information about an entity class.
 * Inspired by Jmix MetaClass.
 */
public interface MetaClass {
    
    /**
     * Get the entity class this metadata describes
     */
    Class<?> getJavaClass();
    
    /**
     * Get the entity name (simple class name by default)
     */
    String getName();
    
    /**
     * Get all properties of this entity
     */
    List<MetaProperty> getProperties();
    
    /**
     * Get a specific property by name
     */
    MetaProperty getProperty(String name);
    
    /**
     * Check if this entity has a specific property
     */
    boolean hasProperty(String name);
    
    /**
     * Create a new instance of this entity
     */
    Object createInstance();
}