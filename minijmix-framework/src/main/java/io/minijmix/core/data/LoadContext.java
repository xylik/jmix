package io.minijmix.core.data;

import java.util.List;

/**
 * Context for loading entities with specific criteria.
 * Simplified version of Jmix LoadContext.
 */
public class LoadContext<T> {
    
    private final Class<T> entityClass;
    private String query;
    private List<Object> parameters;
    private FetchPlan fetchPlan;
    private int maxResults = -1;
    private int firstResult = 0;
    
    public LoadContext(Class<T> entityClass) {
        this.entityClass = entityClass;
    }
    
    public Class<T> getEntityClass() {
        return entityClass;
    }
    
    public String getQuery() {
        return query;
    }
    
    public LoadContext<T> setQuery(String query) {
        this.query = query;
        return this;
    }
    
    public List<Object> getParameters() {
        return parameters;
    }
    
    public LoadContext<T> setParameters(List<Object> parameters) {
        this.parameters = parameters;
        return this;
    }
    
    public FetchPlan getFetchPlan() {
        return fetchPlan;
    }
    
    public LoadContext<T> setFetchPlan(FetchPlan fetchPlan) {
        this.fetchPlan = fetchPlan;
        return this;
    }
    
    public int getMaxResults() {
        return maxResults;
    }
    
    public LoadContext<T> setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }
    
    public int getFirstResult() {
        return firstResult;
    }
    
    public LoadContext<T> setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        return this;
    }
}