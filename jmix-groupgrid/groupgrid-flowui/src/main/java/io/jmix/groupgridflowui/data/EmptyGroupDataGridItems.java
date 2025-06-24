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

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.data.grid.EmptyDataGridItems;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class EmptyGroupDataGridItems<T> extends EmptyDataGridItems<T> implements GroupDataGridItems<T> {

    public EmptyGroupDataGridItems(MetaClass metaClass) {
        super(metaClass);
    }

    @Override
    public void groupBy(@Nullable Object[] properties) {
        // Do nothing
    }

    @Override
    public List<GroupInfo> getRootGroups() {
        return List.of();
    }

    @Override
    public boolean hasChildren(GroupInfo groupId) {
        return false;
    }

    @Override
    public List<GroupInfo> getChildren(GroupInfo groupId) {
        return List.of();
    }

    @Override
    public List<T> getOwnChildItems(GroupInfo groupId) {
        return List.of();
    }

    @Override
    public List<T> getChildItems(GroupInfo groupId) {
        return List.of();
    }

    @Nullable
    @Override
    public GroupInfo getParentGroup(T item) {
        return null;
    }

    @Override
    public List<GroupInfo> getGroupPath(T item) {
        return List.of();
    }

    @Override
    public Collection<T> getGroupItems(GroupInfo groupId) {
        return List.of();
    }

    @Override
    public int getGroupItemsCount(GroupInfo groupId) {
        return 0;
    }

    @Override
    public boolean hasGroups() {
        return false;
    }

    @Override
    public Collection<GroupProperty> getGroupProperties() {
        return List.of();
    }

    @Override
    public boolean containsGroup(GroupInfo groupId) {
        return false;
    }

    @Override
    public void addGroupPropertyValueProvider(String customProperty, GroupPropertyValueProvider<T> propertyValueProvider) {
        // Do nothing.
    }

    @Override
    public void removeGroupPropertyValueProvider(String customProperty) {
        // Do nothing.
    }

    @Override
    public void removeAllGroupPropertyValueProviders() {
        // Do nothing.
    }
}
