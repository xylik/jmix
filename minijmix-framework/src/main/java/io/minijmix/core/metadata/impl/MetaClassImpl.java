package io.minijmix.core.metadata.impl;

import io.minijmix.core.metadata.MetaClass;
import io.minijmix.core.metadata.MetaProperty;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of MetaClass.
 */
public class MetaClassImpl implements MetaClass {
    
    private final Class<?> javaClass;
    private final String name;
    private final Map<String, MetaProperty> properties = new HashMap<>();
    private final List<MetaProperty> propertyList = new ArrayList<>();
    
    public MetaClassImpl(Class<?> javaClass) {
        this.javaClass = javaClass;
        this.name = javaClass.getSimpleName();
        
        // Scan all fields and create MetaProperty instances
        scanFields();
    }
    
    private void scanFields() {
        Class<?> currentClass = javaClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (!field.isSynthetic() && !java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    MetaProperty property = new MetaPropertyImpl(this, field);
                    properties.put(field.getName(), property);
                    propertyList.add(property);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }
    
    @Override
    public Class<?> getJavaClass() {
        return javaClass;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public List<MetaProperty> getProperties() {
        return new ArrayList<>(propertyList);
    }
    
    @Override
    public MetaProperty getProperty(String name) {
        return properties.get(name);
    }
    
    @Override
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }
    
    @Override
    public Object createInstance() {
        try {
            return javaClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot create instance of " + javaClass.getName(), e);
        }
    }
}