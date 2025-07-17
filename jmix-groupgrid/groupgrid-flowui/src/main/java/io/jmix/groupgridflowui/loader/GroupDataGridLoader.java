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

package io.jmix.groupgridflowui.loader;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.view.View;
import io.jmix.groupgridflowui.component.GroupDataGrid;
import io.jmix.groupgridflowui.component.GroupDataGridColumn;
import io.jmix.groupgridflowui.data.ContainerGroupDataGridItems;
import io.jmix.groupgridflowui.data.EmptyGroupDataGridItems;
import io.jmix.groupgridflowui.kit.vaadin.grid.Grid.Column;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class GroupDataGridLoader extends AbstractGroupGridLoader<GroupDataGrid<?>> {

    public static final String GROUP_COLUMN_ELEMENT_NAME = "groupColumn";
    public static final String GROUP_BY_ELEMENT_NAME = "groupBy";

    @Override
    protected GroupDataGrid<?> createComponent() {
        return factory.create(GroupDataGrid.class);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void setupDataProvider(GridDataHolder holder) {
        if (holder.getContainer() != null) {
            resultComponent.setItems(new ContainerGroupDataGridItems(holder.getContainer()));
        } else if (holder.getMetaClass() != null) {
            resultComponent.setItems(new EmptyGroupDataGridItems<>(holder.getMetaClass()));
        }
    }

    @Override
    protected void loadColumnsElementChild(GroupDataGrid<?> resultComponent, Element columnElement, MetaClass metaClass, boolean sortableColumns, boolean resizableColumns) {
        switch (columnElement.getName()) {
            case GROUP_BY_ELEMENT_NAME:
                loadGroupByElement(resultComponent, columnElement, metaClass, sortableColumns, resizableColumns);
                return;
            case GROUP_COLUMN_ELEMENT_NAME:
                loadGroupColumn(resultComponent, columnElement, resizableColumns);
                return;
        }

        super.loadColumnsElementChild(resultComponent, columnElement, metaClass, sortableColumns, resizableColumns);
    }

    protected Column<?> loadGroupColumn(GroupDataGrid<?> resultComponent, Element groupColumnElement,
                                        boolean resizableColumns) {
        GroupDataGridColumn<?> groupColumn = resultComponent.addGroupColumn();

        loadString(groupColumnElement, "width", groupColumn::setWidth);
        loadResourceString(groupColumnElement, "header", context.getMessageGroup(), groupColumn::setHeader);
        loadResourceString(groupColumnElement, "footer", context.getMessageGroup(), groupColumn::setFooter);
        loadBoolean(groupColumnElement, "frozen", groupColumn::setFrozen);
        loadInteger(groupColumnElement, "flexGrow", groupColumn::setFlexGrow);
        loadBoolean(groupColumnElement, "autoWidth", groupColumn::setAutoWidth);
        loadBoolean(groupColumnElement, "visible", groupColumn::setVisible);
        loadBoolean(groupColumnElement, "autoHidden", groupColumn::setAutoHidden);

        loadColumnResizable(groupColumnElement, groupColumn, resizableColumns);

        return groupColumn;
    }

    protected void loadGroupByElement(GroupDataGrid<?> resultComponent, Element groupByElement,
                                      MetaClass metaClass, boolean sortableColumns, boolean resizableColumns) {
        List<Column<?>> groupingColumns = new ArrayList<>();

        for (Element columnElement : groupByElement.elements("column")) {
            Column<?> groupingColumn = loadColumn(resultComponent, columnElement, metaClass,
                    sortableColumns, resizableColumns);

            groupingColumns.add(groupingColumn);
        }

        if (!groupingColumns.isEmpty()) {
            getComponentContext().addPreInitTask(new GroupItemsInitTask(groupingColumns));
        }
    }

    @Override
    protected void loadActions() {
        loadActionsAttributes();
        super.loadActions();
    }

    protected void loadActionsAttributes() {
        Element actions = element.element("actions");
        if (actions == null) {
            return;
        }

        loaderSupport.loadBoolean(actions, "showInContextMenuEnabled",
                resultComponent.getActionsSupport()::setShowActionsInContextMenuEnabled);
    }

    protected class GroupItemsInitTask implements InitTask {

        protected final List<Column<?>> groupingColumns;

        public GroupItemsInitTask(List<Column<?>> groupingColumns) {
            this.groupingColumns = groupingColumns;
        }

        @Override
        public void execute(ComponentContext context, View<?> view) {
            // Is not invoked. Do nothing.
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void execute(Context context) {
            InitTask.super.execute(context);

            resultComponent.groupByColumnsList((List) groupingColumns);
        }
    }
}
