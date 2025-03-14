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

package io.jmix.flowui.xml.layout.loader.component;

import com.google.common.base.Strings;
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.grid.GroupDataGrid;
import io.jmix.flowui.data.grid.ContainerGroupDataGridItems;
import io.jmix.flowui.data.grid.EmptyGroupDataGridItems;
import io.jmix.flowui.view.View;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class GroupDataGridLoader extends AbstractGridLoader<GroupDataGrid<?>> {

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
    protected void loadColumns(GroupDataGrid<?> resultComponent, Element columnsElement, MetaClass metaClass, FetchPlan fetchPlan) {
        loadGroupColumns(resultComponent, columnsElement, metaClass);

        super.loadColumns(resultComponent, columnsElement, metaClass, fetchPlan);
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

    protected void loadGroupColumns(GroupDataGrid<?> resultComponent, Element columnsElement, MetaClass metaClass) {
        Element groupElement = columnsElement.element("groupColumn");
        if (groupElement == null) {
            return;
        }

        resultComponent.addHierarchyColumn();

        boolean sortable = loadBoolean(columnsElement, "sortable").orElse(true);
        boolean resizable = loadBoolean(columnsElement, "resizable").orElse(false);

        List<MetaPropertyPath> groupProperties = new ArrayList<>();

        List<Element> columnElements = groupElement.elements("column");
        for (Element columnElement : columnElements) {
            // todo rp loadGroupAllowed and create GroupDataGridColumn
            loadColumnsElementChild(resultComponent, columnElement, metaClass, sortable, resizable);

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
            getComponentContext().addPreInitTask(new InitTask() {
                @Override
                public void execute(ComponentContext context, View<?> view) {
                    // Do nothing
                }

                @Override
                public void execute(Context context) {
                    InitTask.super.execute(context);

                    // enable grouping columns from descriptor if columnReorderingAllowed = false
//                        boolean reorderDisabled = !resultComponent.getColumnReorderingAllowed();
//                        component.setColumnReorderingAllowed(true);

                    resultComponent.getItems().groupBy(groupProperties.toArray(new MetaPropertyPath[0]));

//                        if (reorderDisabled) {
//                            component.setColumnReorderingAllowed(false);
//                        }
                }
            });

        }
    }
}
