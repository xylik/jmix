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

import com.google.common.base.Strings;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.view.View;
import io.jmix.groupgridflowui.component.GroupDataGrid;
import io.jmix.groupgridflowui.data.ContainerGroupDataGridItems;
import io.jmix.groupgridflowui.data.EmptyGroupDataGridItems;
import io.jmix.groupgridflowui.data.GroupDataGridItems;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class GroupDataGridLoader extends AbstractGroupGridLoader<GroupDataGrid<?>> {

    public static final String GROUP_COLUMN_ELEMENT_NAME = "groupColumn";

    @Override
    protected GroupDataGrid<?> createComponent() {
        return factory.create(GroupDataGrid.class);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void setupDataProvider(GridDataHolder holder) {
        if (holder.getContainer() != null) {
            resultComponent.setItems(new ContainerGroupDataGridItems(holder.getContainer(), applicationContext.getBean(Metadata.class)));
        } else if (holder.getMetaClass() != null) {
            resultComponent.setItems(new EmptyGroupDataGridItems<>(holder.getMetaClass()));
        }
    }

    @Override
    protected void loadColumnsElementChild(GroupDataGrid<?> resultComponent, Element columnElement, MetaClass metaClass, boolean sortableColumns, boolean resizableColumns) {
        if (GROUP_COLUMN_ELEMENT_NAME.equals(columnElement.getName())) {
            loadGroupColumns(resultComponent, columnElement, metaClass, sortableColumns, resizableColumns);
            return;
        }

        super.loadColumnsElementChild(resultComponent, columnElement, metaClass, sortableColumns, resizableColumns);
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

    protected void loadGroupColumns(GroupDataGrid<?> resultComponent, Element groupColumnElement,
                                    MetaClass metaClass, boolean sortableColumns, boolean resizableColumns) {
        List<MetaPropertyPath> groupProperties = new ArrayList<>();

        // TODO: pinyazhin, when add a column?
        resultComponent.addHierarchyColumn();

        List<Element> columnElements = groupColumnElement.elements("column");
        for (Element columnElement : columnElements) {
            loadColumnsElementChild(resultComponent, columnElement, metaClass, sortableColumns, resizableColumns);

            // Skip property if not visible
            String visible = columnElement.attributeValue("visible");
            if (!Strings.isNullOrEmpty(visible) && !Boolean.parseBoolean(visible)) {
                continue;
            }

            String property = loadString(columnElement, "property")
                    .orElse(null);
            MetaPropertyPath metaPropertyPath = property != null
                    ? getMetaDataTools().resolveMetaPropertyPathOrNull(metaClass, property)
                    : null;

            if (metaPropertyPath != null) {
                groupProperties.add(metaPropertyPath);
            }
        }

        if (!groupProperties.isEmpty()) {
            getComponentContext().addPreInitTask(new GroupItemsInitTask(groupProperties));
        }
    }

    protected class GroupItemsInitTask implements InitTask {

        protected final List<MetaPropertyPath> groupProperties;

        public GroupItemsInitTask(List<MetaPropertyPath> groupProperties) {
            this.groupProperties = groupProperties;
        }

        @Override
        public void execute(ComponentContext context, View<?> view) {
            // Is not invoked. Do nothing.
        }

        @Override
        public void execute(Context context) {
            InitTask.super.execute(context);

            GroupDataGridItems<?> items = resultComponent.getItems();
            if (items != null) {
                items.groupBy(groupProperties.toArray(new MetaPropertyPath[0]));
            }
        }
    }
}
