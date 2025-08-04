package io.minijmix.core.metadata.impl;

import io.minijmix.core.metadata.MetaClass;
import io.minijmix.core.metadata.Metadata;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Default implementation of Metadata.
 */
@Component
public class MetadataImpl implements Metadata {
    
    private final Map<Class<?>, MetaClass> metaClasses = new ConcurrentHashMap<>();
    private final Map<String, MetaClass> metaClassesByName = new ConcurrentHashMap<>();
    
    @Override
    public MetaClass getClass(Class<?> entityClass) {
        return metaClasses.computeIfAbsent(entityClass, this::createMetaClass);
    }
    
    @Override
    public MetaClass getClass(Object entity) {
        if (entity == null) {
            return null;
        }
        return getClass(entity.getClass());
    }
    
    @Override
    public MetaClass getClass(String entityName) {
        return metaClassesByName.get(entityName);
    }
    
    @Override
    public <T> T create(Class<T> entityClass) {
        MetaClass metaClass = getClass(entityClass);
        return entityClass.cast(metaClass.createInstance());
    }
    
    @Override
    public void registerClass(Class<?> entityClass) {
        MetaClass metaClass = createMetaClass(entityClass);
        metaClasses.put(entityClass, metaClass);
        metaClassesByName.put(metaClass.getName(), metaClass);
    }
    
    private MetaClass createMetaClass(Class<?> entityClass) {
        MetaClass metaClass = new MetaClassImpl(entityClass);
        metaClassesByName.put(metaClass.getName(), metaClass);
        return metaClass;
    }
}