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

import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.data.grid.DataGridItems;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface GroupDataGridItems<T> extends DataGridItems<T> {

    /**
     * Perform grouping by the list of properties
     */
    void groupBy(MetaPropertyPath[] properties);

    /**
     * @return the list of root groups
     */
    List<GroupInfo> getRootGroups();

    /**
     * @return the list of nested groups
     */
    List<GroupInfo> getChildren(GroupInfo groupId);

    /**
     * Indicates that group has nested groups
     */
    boolean hasChildren(GroupInfo groupId);

    /**
     * @return the list of nested items
     */
    List<T> getOwnChildItems(GroupInfo groupId);

    /**
     * @return the list of items from all nested group levels
     */
    List<T> getChildItems(GroupInfo groupId);

    /**
     * @return the parent group of passed item
     */
    @Nullable
    GroupInfo getParentGroup(T item);

    /**
     * @return the path through all parent groups
     */
    List<GroupInfo> getGroupPath(T item);

    /**
     * @return item ids that are contained in the selected group
     */
    Collection<T> getGroupItems(GroupInfo groupId);

    /**
     * @return a count of items that are contained in the selected group
     */
    int getGroupItemsCount(GroupInfo groupId);

    /**
     * Indicated that a datasource has groups
     */
    boolean hasGroups();

    /**
     * @return group properties
     */
    Collection<MetaPropertyPath> getGroupProperties();

    /**
     * Indicates that a group is contained in the groups tree
     */
    boolean containsGroup(GroupInfo groupId);
}
