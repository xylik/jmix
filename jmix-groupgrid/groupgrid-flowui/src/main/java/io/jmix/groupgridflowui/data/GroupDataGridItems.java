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
import io.jmix.flowui.data.grid.DataGridItems;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.function.Consumer;

public interface GroupDataGridItems<T> extends DataGridItems<T> {

    /**
     * Perform grouping by the list of properties. The available values for properties are:
     * <ul>
     *     <li>
     *         MetaPropertyPath can be a property of current or a reference entity.
     *     </li>
     *     <li>
     *         Column key (String) of a generated value.
     *     </li>
     * </ul>
     */
    void groupBy(List<GroupProperty> properties);

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
     * @return items that are contained in the selected group
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
    Collection<GroupProperty> getGroupProperties();

    /**
     * Indicates that a group is contained in the group tree
     */
    boolean containsGroup(GroupInfo groupId);

    /**
     * Adds a value provider for a generated grouping property. This method enables defining
     * custom logic for computing property values during data grouping.
     *
     * @param customProperty        the name of the custom property (which may not exist on an item) to be used for
     *                              grouping
     * @param propertyValueProvider implementation of {@link GroupPropertyValueProvider}
     */
    void addGroupPropertyValueProvider(String customProperty, GroupPropertyValueProvider<T> propertyValueProvider);

    /**
     * Removes a value provider for a custom property that was previously added for grouping.
     *
     * @param customProperty the name of the custom property
     */
    void removeGroupPropertyValueProvider(String customProperty);

    /**
     * Removes all custom property value providers previously added for grouping.
     */
    void removeAllGroupPropertyValueProviders();

    // TODO: pinyazhin, get rid of
    Registration addGroupByListener(Consumer<GroupByEvent<T>> listener);

    // TODO: pinyazhin, get rid of
    class GroupByEvent<T> extends EventObject {

        public GroupByEvent(Object source) {
            super(source);
        }
    }
}
