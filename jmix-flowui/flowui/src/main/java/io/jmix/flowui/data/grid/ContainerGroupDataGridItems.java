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

package io.jmix.flowui.data.grid;

import com.google.common.collect.ImmutableList;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.model.CollectionContainer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ContainerGroupDataGridItems<T> extends ContainerDataGridItems<T> implements GroupDataGridItems<T> {

    // todo rp rework API to make it more friendly
    protected Object[] groupProperties = null;

    protected Map<GroupInfo, GroupInfo> parents;

    protected Map<GroupInfo, Set<GroupInfo>> children;

    protected List<GroupInfo> roots;

    protected Map<GroupInfo, List<?>> groupItems;
    // reversed relations from groupItems
    protected Map<Object, GroupInfo> itemGroups;

    protected boolean isGrouping;

    protected Object[] sortProperties;
    protected boolean[] sortAscending;

    public ContainerGroupDataGridItems(CollectionContainer<T> container) {
        super(container);
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        sortProperties = propertyId;
        sortAscending = ascending;
        super.sort(propertyId, ascending);
    }

    @Override
    public void groupBy(@Nullable Object[] properties) {
        if (isGrouping) {
            return;
        }
        isGrouping = true;
        try {
            if (properties != null) {
                groupProperties = properties;

                if (!ArrayUtils.isEmpty(groupProperties)) {
                    doGroup();
                } else {
                    roots = null;
                    parents = null;
                    children = null;
                    groupItems = null;
                    itemGroups = null;
                }
            }
        } finally {
            isGrouping = false;
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

            List itemsIds = groupItems.computeIfAbsent(groupInfo, k -> new ArrayList<>());
            itemsIds.add(EntityValues.getId(item));
        }
    }

    @Nullable
    protected GroupInfo groupItems(int propertyIndex, @Nullable GroupInfo parent,
                                   Collection<GroupInfo> children,
                                   T item, LinkedMap<MetaPropertyPath, Object> groupValues) {
        MetaPropertyPath property = (MetaPropertyPath) groupProperties[propertyIndex++];
        Object itemValue = getValueByProperty(item, property);
        groupValues.put(property, itemValue);

        GroupInfo groupInfo = new GroupInfo((LinkedMap) groupValues);
        itemGroups.put(EntityValues.getId(item), groupInfo);

        if (!parents.containsKey(groupInfo)) {
            parents.put(groupInfo, parent);
        }

        if (!children.contains(groupInfo)) {
            children.add(groupInfo);
        }

        Set<GroupInfo> groupChildren =
                this.children.computeIfAbsent(groupInfo, k -> new LinkedHashSet<>());

        if (propertyIndex < groupProperties.length) {
            groupInfo = groupItems(propertyIndex, groupInfo, groupChildren, item, groupValues);
        }

        return groupInfo;
    }

    @Nullable
    protected Object getValueByProperty(T item, MetaPropertyPath property) {
        Preconditions.checkNotNullArgument(item);

        return EntityValues.getValueEx(item, property.toString());
    }

    @Override
    public List<GroupInfo> rootGroups() {
        if (hasGroups()) {
            return Collections.unmodifiableList(roots);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean hasChildren(GroupInfo groupId) {
        boolean groupExists = containsGroup(groupId);
        Set<GroupInfo> groupChildren = this.children.get(groupId);
        return groupExists && CollectionUtils.isNotEmpty(groupChildren);
    }

    @Override
    public List<GroupInfo> getChildren(GroupInfo groupId) {
        if (hasChildren(groupId)) {
            return List.copyOf(children.get(groupId));
        }
        return Collections.emptyList();
    }

    @Override
    public List<T> getOwnChildItems(GroupInfo groupId) {
        if (groupItems == null) {
            return Collections.emptyList();
        }

        List<?> idsList = groupItems.get(groupId);
        if (containsGroup(groupId) && CollectionUtils.isNotEmpty(idsList)) {
            return idsList.stream()
                    .map(id -> container.getItem(id))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<T> getChildItems(GroupInfo groupId) {
        if (groupItems == null) {
            return Collections.emptyList();
        }

        if (containsGroup(groupId)) {
            List<T> entities = new ArrayList<>();

            // if current group contains other groups
            if (hasChildren(groupId)) {
                Set<GroupInfo> children = getChildrenInternal(groupId);
                for (GroupInfo childGroup : children) {
                    entities.addAll(getChildItems(childGroup));
                }
            }

            for (Object id : groupItems.getOrDefault(groupId, Collections.emptyList())) {
                T item = container.getItem(id);
                entities.add(item);
            }

            return entities;
        }
        return Collections.emptyList();
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
        Object id = EntityValues.getId(item);
        if (container.getItemOrNull(id) == null) {
            throw new IllegalArgumentException("Datasource doesn't contain passed entity");
        }

        if (itemGroups == null) {
            return null;
        }
        return itemGroups.get(EntityValues.getId(item));
    }

    @Override
    public List<GroupInfo> getGroupPath(T item) {
        Object id = EntityValues.getId(item);
        if (container.getItemOrNull(id) == null) {
            throw new IllegalArgumentException("Datasource doesn't contain passed entity");
        }

        if (itemGroups == null) {
            return Collections.emptyList();
        }

        GroupInfo groupInfo = itemGroups.get(EntityValues.getId(item));
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

    @Nullable
    @Override
    public Object getGroupProperty(GroupInfo groupId) {
        if (containsGroup(groupId)) {
            return groupId.getProperty();
        }
        return null;
    }

    @Nullable
    @Override
    public Object getGroupPropertyValue(GroupInfo groupId) {
        if (containsGroup(groupId)) {
            return groupId.getValue();
        }
        return null;
    }

    @Override
    public Collection getGroupItemIds(GroupInfo groupId) {
        if (containsGroup(groupId)) {
            List itemIds;
            if ((itemIds = groupItems.get(groupId)) == null) {
                itemIds = new ArrayList<>();
                Set<GroupInfo> children = getChildrenInternal(groupId);
                for (GroupInfo child : children) {
                    itemIds.addAll(getGroupItemIds(child));
                }
            }
            return ImmutableList.copyOf(itemIds);
        }
        return Collections.emptyList();
    }

    @Override
    public int getGroupItemsCount(GroupInfo groupId) {
        if (containsGroup(groupId)) {
            List itemIds;
            if ((itemIds = groupItems.get(groupId)) == null) {
                int count = 0;
                Set<GroupInfo> children = getChildrenInternal(groupId);
                for (GroupInfo child : children) {
                    count += getGroupItemsCount(child);
                }
                return count;
            } else {
                return itemIds.size();
            }
        }
        return 0;
    }

    @Override
    public boolean hasGroups() {
        return roots != null;
    }

    @Override
    public Collection<?> getGroupProperties() {
        if (groupProperties == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(groupProperties);
    }

    @Override
    public boolean containsGroup(GroupInfo groupId) {
        return hasGroups() && parents.containsKey(groupId);
    }
}
