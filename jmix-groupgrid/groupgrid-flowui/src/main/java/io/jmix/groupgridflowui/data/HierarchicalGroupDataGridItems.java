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

import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import io.jmix.core.annotation.Internal;
import io.jmix.groupgridflowui.component.GroupDataGrid;
import org.springframework.lang.Nullable;

/**
 * Represents hierarchical data provider for {@link GroupDataGrid}.
 *
 * @param <T> item type
 */
@Internal
public interface HierarchicalGroupDataGridItems<T> extends GroupDataGridItems<T>, HierarchicalDataProvider<T, Void> {

    /**
     * Returns {@link GroupInfo} by a group item.
     * <p>
     * Group item is an entity object that represents a grouping row in a {@link GroupDataGrid}.
     *
     * @param groupItem a group item
     * @return group info or {@code null} if there is no group info for the given item
     */
    @Nullable
    GroupInfo getGroupByItem(T groupItem);

    /**
     * Returns a group item by a {@link GroupInfo}.
     *
     * @param group a group info
     * @return group item or {@code null} if there is no item for the given group info
     */
    @Nullable
    T getItemByGroup(GroupInfo group);

    /**
     * @return group data grid items
     */
    default GroupDataGridItems<T> getGroupDataGridItems() {
        return this;
    }
}
