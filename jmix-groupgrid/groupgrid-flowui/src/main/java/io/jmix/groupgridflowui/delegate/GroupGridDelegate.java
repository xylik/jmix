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

package io.jmix.groupgridflowui.delegate;

import com.vaadin.flow.data.renderer.Renderer;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.sys.BeanUtil;
import io.jmix.groupgridflowui.component.*;
import io.jmix.groupgridflowui.component.renderer.AbstractGroupRenderer;
import io.jmix.groupgridflowui.data.GroupProperty;
import io.jmix.groupgridflowui.data.HierarchicalGroupDataGridItems;
import io.jmix.groupgridflowui.kit.vaadin.grid.Grid;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Component("flowui_GroupGridDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GroupGridDelegate<E, ITEMS extends HierarchicalGroupDataGridItems<E>>
        extends AbstractGroupGridDelegate<GroupDataGrid<E>, E, ITEMS> {

    protected List<Grid.Column<E>> groupingColumns = new ArrayList<>();

    protected Consumer<ColumnGroupingContext<E>> afterColumnGroupingHandler;

    public GroupGridDelegate(GroupDataGrid<E> component) {
        super(component);
    }

    public void groupByKeysList(List<String> keys) {
        List<Grid.Column<E>> columns = component.getAllColumns().stream()
                .filter(c -> keys.contains(c.getKey()))
                .toList();

        groupByColumnsList(columns);
    }

    public void groupByColumnsList(List<Grid.Column<E>> columns) {
        Preconditions.checkNotNullArgument(columns);

        checkGroupingColumnsOwner(columns);

        List<Grid.Column<E>> columnsToGroup = super.deleteHiddenColumns(columns);

        doGroup(columnsToGroup);
    }

    public void ungroup() {
        doUngroup(Collections.emptyList());
    }

    public void ungroupByKeysList(List<String> keys) {
        List<Grid.Column<E>> columns = component.getAllColumns().stream()
                .filter(c -> keys.contains(c.getKey()))
                .toList();

        ungroupByColumnsList(columns);
    }

    public void ungroupByColumnsList(List<Grid.Column<E>> columns) {
        Preconditions.checkNotNullArgument(columns);

        checkGroupingColumnsOwner(columns);

        List<Grid.Column<E>> columnsToUngroup = super.deleteHiddenColumns(columns);

        doUngroup(columnsToUngroup);
    }

    public List<Grid.Column<E>> getGroupingColumns() {
        return Collections.unmodifiableList(groupingColumns);
    }

    public Consumer<ColumnGroupingContext<E>> getAfterColumnGroupingHandler() {
        return afterColumnGroupingHandler;
    }

    public void setAfterColumnGroupingHandler(Consumer<ColumnGroupingContext<E>> afterColumnGroupingHandler) {
        this.afterColumnGroupingHandler = afterColumnGroupingHandler;
    }

    public Grid.Column<E> reattachColumn(Grid.Column<E> column) {
        DataGridColumn<E> reattachedColumn = propertyColumns.containsKey(column)
                ? component.addColumn(propertyColumns.get(column))
                : component.addColumn(column.getRenderer());

        // Key already set if the column was added by MetaPropertyPath
        if (!propertyColumns.containsKey(column)) {
            reattachedColumn.setKey(column.getKey());
        }

        GroupDataGridUtils.copyColumnProperties((DataGridColumn) column, reattachedColumn);

        propertyColumns.remove(column);
        columns.remove(column);

        return reattachedColumn;
    }

    @Override
    public BiFunction<Renderer<E>, String, Grid.Column<E>> getDefaultColumnFactory() {
        return (Renderer<E> renderer, String columnId) -> {
            DataGridColumn<E> column = renderer instanceof AbstractGroupRenderer<E> groupRenderer
                    ? new GroupDataGridColumn<>(component, columnId, groupRenderer)
                    : new DataGridColumn<>(component, columnId, renderer);

            BeanUtil.autowireContext(applicationContext, column);
            return column;
        };
    }

    @Override
    protected List<Grid.Column<E>> deleteHiddenColumns(List<Grid.Column<E>> allColumns) {
        List<Grid.Column<E>> result = super.deleteHiddenColumns(allColumns);
        result.removeAll(groupingColumns);
        return result;
    }

    protected void doGroup(List<Grid.Column<E>> columns) {
        List<Grid.Column<?>> previousColumns = List.copyOf(groupingColumns);

        clearGroupColumns();

        if (columns.isEmpty()) {
            dataGridItems.groupBy(Collections.emptyList());

            clearGroupColumns();

            fireOnAfterColumnGrouping(previousColumns, Collections.emptyList(), true);
            return;
        }

        dataGridItems.removeAllGroupPropertyValueProviders();

        component.getGroupPropertyValueProviders().forEach((property, provider) ->
                dataGridItems.addGroupPropertyValueProvider(property, provider));

        List<GroupProperty> groupProperties = columns.stream()
                .map(c -> propertyColumns.containsKey(c)
                        ? (GroupProperty) () -> propertyColumns.get(c)
                        : (GroupProperty) c::getKey)
                .toList();

        dataGridItems.groupBy(groupProperties);

        groupingColumns.addAll(columns);

        fireOnAfterColumnGrouping(previousColumns, Collections.unmodifiableList(groupingColumns), true);
    }

    protected void doUngroup(List<Grid.Column<E>> columns) {
        List<Grid.Column<?>> previousColumns = List.copyOf(groupingColumns);

        if (columns.isEmpty()) {
            dataGridItems.groupBy(Collections.emptyList());

            clearGroupColumns();

            fireOnAfterColumnGrouping(previousColumns, Collections.emptyList(), false);
            return;
        }

        dataGridItems.removeAllGroupPropertyValueProviders();
        component.getGroupPropertyValueProviders().forEach((property, provider) ->
                dataGridItems.addGroupPropertyValueProvider(property, provider));

        groupingColumns.removeAll(columns);

        List<GroupProperty> groupProperties = groupingColumns.stream()
                .map(c -> propertyColumns.containsKey(c)
                        ? (GroupProperty) () -> propertyColumns.get(c)
                        : (GroupProperty) c::getKey)
                .toList();

        dataGridItems.groupBy(groupProperties);

        fireOnAfterColumnGrouping(previousColumns, Collections.unmodifiableList(groupingColumns), false);
    }

    protected void clearGroupColumns() {
        if (groupingColumns != null) {
            groupingColumns.clear();
        }
    }

    protected void checkGroupingColumnsOwner(List<Grid.Column<E>> columns) {
        String groupDataGridId = this.component.getId().orElse(null);
        for (Grid.Column<E> column : columns) {
            // Check by columns, because columns can be detached from the group data grid.
            if (!this.columns.contains(column)) {
                throw new IllegalArgumentException(String.format("'%s' column must be attached to the '%s' group data grid " +
                                "in order to call group/ungroup",
                        column.getKey(), groupDataGridId == null ? "no-id" : groupDataGridId));
            }
        }
    }

    protected List<Grid.Column<E>> removeHiddenColumnsBySecurity(List<Grid.Column<E>> columns) {
        List<Grid.Column<E>> availableColumns = new ArrayList<>(columns.size());
        for (Grid.Column<E> column : columns) {
            MetaPropertyPath mpp = propertyColumns.get(column);
            if (mpp == null || isPropertyEnabledBySecurity(mpp)) {
                availableColumns.add(column);
            }
        }
        return availableColumns;
    }

    protected void fireOnAfterColumnGrouping(List<Grid.Column<?>> previousColumns,
                                             List<Grid.Column<?>> columns, boolean groupBy) {
        if (afterColumnGroupingHandler != null) {
            afterColumnGroupingHandler.accept(new ColumnGroupingContext(previousColumns, columns, groupBy));
        }
    }

    public static class ColumnGroupingContext<E> {

        protected List<Grid.Column<E>> columns;
        protected List<Grid.Column<E>> previousColumns;

        protected boolean groupBy;

        public ColumnGroupingContext(List<Grid.Column<E>> previousColumns,
                                     List<Grid.Column<E>> columns,
                                     boolean groupBy) {
            this.previousColumns = previousColumns;
            this.columns = columns;
            this.groupBy = groupBy;
        }

        /**
         * @return new grouping columns
         */
        public List<Grid.Column<E>> getColumns() {
            return columns;
        }

        public List<Grid.Column<E>> getPreviousColumns() {
            return previousColumns;
        }

        public boolean isGroupBy() {
            return groupBy;
        }
    }
}
