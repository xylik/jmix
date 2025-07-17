/*
 * Copyright 2025 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.groupgridflowui.data;

import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.data.grid.ContainerDataGridItems;
import io.jmix.flowui.model.CollectionContainer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.LinkedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class ContainerGroupDataGridItems<T> extends ContainerDataGridItems<T> implements GroupDataGridItems<T> {
    private static final Logger log = LoggerFactory.getLogger(ContainerGroupDataGridItems.class);

    // todo rp rework API to make it more friendly
    protected List<GroupProperty> groupProperties = null;

    protected Map<GroupInfo, GroupInfo> parents;
    protected Map<GroupInfo, Set<GroupInfo>> children;
    protected Map<Object, GroupPropertyValueProvider<T>> groupPropertyValueProviders;

    protected List<GroupInfo> roots;

    protected Map<GroupInfo, List<T>> groupItems;
    protected Map<T, GroupInfo> itemGroups;

    protected Object[] sortProperties;
    protected boolean[] sortAscending;

    public ContainerGroupDataGridItems(CollectionContainer<T> container) {
        super(container);
    }

    @Override
    protected void containerCollectionChanged(CollectionContainer.CollectionChangeEvent<T> event) {
        refreshGroups();

        super.containerCollectionChanged(event);
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        sortProperties = propertyId;
        sortAscending = ascending;
        super.sort(propertyId, ascending);
    }

    @Override
    public void groupBy(List<GroupProperty> properties) {
        Preconditions.checkNotNullArgument(properties);

        groupProperties = new ArrayList<>(properties);

        try {
            if (!groupProperties.isEmpty()) {
                doGroup();
            } else {
                roots = null;
                parents = null;
                children = null;
                groupItems = null;
                itemGroups = null;

                fireGroupByEvent();
            }
        } finally {
            if (sortProperties != null && sortProperties.length > 0 && !hasGroups()) {
                super.sort(sortProperties, sortAscending);
            }
        }
    }

    protected void doGroup() {
        roots = new LinkedList<>();
        parents = new LinkedHashMap<>();
        children = new LinkedHashMap<>();
        groupItems = new HashMap<>();
        itemGroups = new HashMap<>();

        for (T item : container.getItems()) {
            GroupInfo groupInfo = groupItems(0, null, roots, item, new LinkedMap<>());

            if (groupInfo == null) {
                throw new IllegalStateException("Item group cannot be NULL");
            }

            List<T> items = groupItems.computeIfAbsent(groupInfo, k -> new ArrayList<>());
            items.add(item);
        }

        fireGroupByEvent();
    }

    @Nullable
    protected GroupInfo groupItems(int propertyIndex, @Nullable GroupInfo parent,
                                   Collection<GroupInfo> children,
                                   T item, LinkedMap<GroupProperty, Object> groupValues) {
        GroupProperty property = groupProperties.get(propertyIndex++);
        Object itemValue = getValueByProperty(item, property);
        groupValues.put(property, itemValue);

        GroupInfo groupInfo = new GroupInfo(groupValues);
        itemGroups.put(item, groupInfo);

        if (!parents.containsKey(groupInfo)) {
            parents.put(groupInfo, parent);
        }

        if (!children.contains(groupInfo)) {
            children.add(groupInfo);
        }

        Set<GroupInfo> groupChildren =
                this.children.computeIfAbsent(groupInfo, k -> new LinkedHashSet<>());

        if (propertyIndex < groupProperties.size()) {
            groupInfo = groupItems(propertyIndex, groupInfo, groupChildren, item, groupValues);
        }

        return groupInfo;
    }

    @Nullable
    protected Object getValueByProperty(T item, GroupProperty property) {
        Preconditions.checkNotNullArgument(item);

        if (property.get() instanceof MetaPropertyPath metaPropertyPath) {
            return EntityValues.getValueEx(item, metaPropertyPath);
        } else if (property.get() instanceof String generatedProperty) {
            GroupPropertyValueProvider<T> valueProvider = groupPropertyValueProviders.get(generatedProperty);
            if (valueProvider == null) {
                log.warn("The group property value provider is not set for '{}' property." +
                        " Please use GroupDataGridItems#addGroupPropertyValueProvider() method", generatedProperty);

                return null;
            }
            return valueProvider.accept(new GroupPropertyValueProvider.GroupingPropertyContext<>(item, property));
        }

        log.warn("Unsupported group property value type: '{}'",  property.get().getClass());

        return null;
    }

    @Override
    public List<GroupInfo> getRootGroups() {
        if (hasGroups()) {
            return Collections.unmodifiableList(roots);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean hasChildren(GroupInfo groupId) {
        if (children == null) {
            return false;
        }

        boolean groupExists = containsGroup(groupId);
        Set<GroupInfo> groupChildren = children.get(groupId);
        return groupExists && CollectionUtils.isNotEmpty(groupChildren);
    }

    @Override
    public List<GroupInfo> getChildren(GroupInfo groupId) {
        if (children != null && hasChildren(groupId)) {
            return List.copyOf(children.get(groupId));
        }
        return Collections.emptyList();
    }

    @Override
    public List<T> getOwnChildItems(GroupInfo groupId) {
        if (groupItems == null) {
            return Collections.emptyList();
        }

        List<T> items = groupItems.get(groupId);
        if (containsGroup(groupId) && CollectionUtils.isNotEmpty(items)) {
            return new ArrayList<>(items);
        }

        return Collections.emptyList();
    }

    @Override
    public List<T> getChildItems(GroupInfo groupId) {
        if (groupItems == null) {
            return Collections.emptyList();
        }

        List<T> entities = new ArrayList<>();
        if (containsGroup(groupId)) {
            // if current group contains other groups
            if (hasChildren(groupId)) {
                Set<GroupInfo> children = getChildrenInternal(groupId);
                for (GroupInfo childGroup : children) {
                    entities.addAll(getChildItems(childGroup));
                }
            }
            entities.addAll(groupItems.getOrDefault(groupId, Collections.emptyList()));
        }
        return entities;
    }

    // return collection as is
    protected Set<GroupInfo> getChildrenInternal(GroupInfo groupId) {
        if (hasChildren(groupId)) {
            return children.get(groupId);
        }
        return Collections.emptySet();
    }

    @Nullable
    @Override
    public GroupInfo getParentGroup(T item) {
        if (itemGroups == null) {
            return null;
        }
        return itemGroups.get(item);
    }

    @Override
    public List<GroupInfo> getGroupPath(T item) {
        if (itemGroups == null) {
            return Collections.emptyList();
        }

        GroupInfo groupInfo = itemGroups.get(item);
        if (groupInfo == null) {
            return Collections.emptyList();
        }
        LinkedList<GroupInfo> parentGroups = new LinkedList<>();
        parentGroups.add(groupInfo);

        GroupInfo parent = parents.get(groupInfo);
        while (parent != null) {
            parentGroups.addFirst(parent);
            parent = parents.get(parent);
        }

        return parentGroups;
    }

    @Override
    public Collection<T> getGroupItems(GroupInfo groupId) {
        if (containsGroup(groupId) && groupItems != null) {
            List<T> items = groupItems.get(groupId);
            if (items == null) {
                items = new ArrayList<>();
                Set<GroupInfo> children = getChildrenInternal(groupId);
                for (GroupInfo child : children) {
                    items.addAll(getGroupItems(child));
                }
            }
            return Collections.unmodifiableList(items);
        }
        return Collections.emptyList();
    }

    @Override
    public int getGroupItemsCount(GroupInfo groupId) {
        if (!containsGroup(groupId) || groupItems == null) {
            return 0;
        }
        List<T> items = groupItems.get(groupId);
        if (items != null) {
            return items.size();
        }
        int count = 0;
        Set<GroupInfo> children = getChildrenInternal(groupId);
        for (GroupInfo child : children) {
            count += getGroupItemsCount(child);
        }
        return count;
    }

    @Override
    public boolean hasGroups() {
        return roots != null;
    }

    @Override
    public Collection<GroupProperty> getGroupProperties() {
        if (groupProperties == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(groupProperties);
    }

    @Override
    public boolean containsGroup(GroupInfo groupId) {
        return hasGroups() && parents != null && parents.containsKey(groupId);
    }

    @Override
    public void addGroupPropertyValueProvider(String customProperty, GroupPropertyValueProvider<T> propertyValueProvider) {
        if (groupPropertyValueProviders == null) {
            groupPropertyValueProviders = new HashMap<>();
        }
        groupPropertyValueProviders.put(customProperty, propertyValueProvider);
    }

    @Override
    public void removeGroupPropertyValueProvider(String customProperty) {
        if (groupPropertyValueProviders == null) {
            return;
        }
        groupPropertyValueProviders.remove(customProperty);
    }

    @Override
    public void removeAllGroupPropertyValueProviders() {
        if (groupPropertyValueProviders == null) {
            return;
        }
        groupPropertyValueProviders.clear();
    }

    @Override
    public Registration addGroupByListener(Consumer<GroupByEvent<T>> listener) {
        return getEventBus().addListener(GroupByEvent.class, (Consumer) listener);
    }

    protected void fireGroupByEvent() {
        getEventBus().fireEvent(new GroupByEvent<>(this));
    }

    protected void refreshGroups() {
        if (hasGroups()) {
            doGroup();
        }
    }
}
