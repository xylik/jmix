package io.minijmix.core.metadata.impl;

import io.minijmix.core.metadata.MetaClass;
import io.minijmix.core.metadata.MetaProperty;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.lang.reflect.Field;

/**
 * Default implementation of MetaProperty.
 */
public class MetaPropertyImpl implements MetaProperty {
    
    private final MetaClass domain;
    private final Field javaField;
    private final String name;
    private final Class<?> type;
    private final boolean isId;
    private final boolean isMandatory;
    
    public MetaPropertyImpl(MetaClass domain, Field javaField) {
        this.domain = domain;
        this.javaField = javaField;
        this.name = javaField.getName();
        this.type = javaField.getType();
        this.isId = javaField.isAnnotationPresent(Id.class);
        
        // Check if field is mandatory based on annotations
        Column columnAnnotation = javaField.getAnnotation(Column.class);
        this.isMandatory = columnAnnotation != null && !columnAnnotation.nullable();
        
        // Make field accessible for reflection
        javaField.setAccessible(true);
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Field getJavaField() {
        return javaField;
    }
    
    @Override
    public Class<?> getType() {
        return type;
    }
    
    @Override
    public MetaClass getDomain() {
        return domain;
    }
    
    @Override
    public boolean isId() {
        return isId;
    }
    
    @Override
    public boolean isMandatory() {
        return isMandatory;
    }
    
    @Override
    public Object getValue(Object entity) {
        try {
            return javaField.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot get value of property " + name + " from entity " + entity, e);
        }
    }
    
    @Override
    public void setValue(Object entity, Object value) {
        try {
            javaField.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot set value of property " + name + " on entity " + entity, e);
        }
    }
}