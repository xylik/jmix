package io.minijmix.core.metadata;

import java.lang.reflect.Field;

/**
 * Metadata information about an entity property.
 * Inspired by Jmix MetaProperty.
 */
public interface MetaProperty {
    
    /**
     * Get the property name
     */
    String getName();
    
    /**
     * Get the Java field this property represents
     */
    Field getJavaField();
    
    /**
     * Get the property type
     */
    Class<?> getType();
    
    /**
     * Get the owning entity class
     */
    MetaClass getDomain();
    
    /**
     * Check if this is an ID property
     */
    boolean isId();
    
    /**
     * Check if this property is required/mandatory
     */
    boolean isMandatory();
    
    /**
     * Get property value from an entity instance
     */
    Object getValue(Object entity);
    
    /**
     * Set property value on an entity instance
     */
    void setValue(Object entity, Object value);
}