/*
 * Copyright 2023 Haulmont.
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

package io.jmix.groupgridflowui.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataCommunicator;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.kit.meta.StudioIgnore;
import io.jmix.flowui.sys.BeanUtil;
import io.jmix.groupgridflowui.component.HierarchicalColumnRendererWrapper.HierarchicalDataGeneratorWrapper;
import io.jmix.groupgridflowui.component.headerfilter.GroupDataGridHeaderFilter;
import io.jmix.groupgridflowui.kit.vaadin.grid.Grid;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DataGridColumn<E> extends Grid.Column<E> implements ApplicationContextAware {

    protected GroupDataGridHeaderFilter dataGridFilter;
    protected ApplicationContext applicationContext;

    /**
     * Constructs a new DataGridColumn for use inside a {@link GroupDataGrid}.
     *
     * @param grid     the grid this column is attached to
     * @param columnId unique identifier of this column
     * @param renderer the renderer to use in this column, must not be
     *                 {@code null}
     */
    public DataGridColumn(Grid<E> grid, String columnId, Renderer<E> renderer) {
        super(grid, columnId, renderer);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    @StudioIgnore
    public Grid.Column<E> setClassNameGenerator(SerializableFunction<E, String> classNameGenerator) {
        return super.setClassNameGenerator(classNameGenerator);
    }

    /**
     * Sets the filtering for a column. If the filtering is enabled,
     * a filter button will be added to the column header.
     * The filtering is disabled by default.
     *
     * @param filterable whether to add a filter to the header
     */
    public void setFilterable(boolean filterable) {
        if (filterable && dataGridFilter == null) {
            dataGridFilter = new GroupDataGridHeaderFilter(new GroupDataGridHeaderFilter.HeaderFilterContext(grid, this));
            super.setHeader(dataGridFilter);

            BeanUtil.autowireContext(applicationContext, dataGridFilter);
        } else if (!filterable && dataGridFilter != null) {
            Component currentHeader = dataGridFilter.getHeader();
            dataGridFilter = null;

            if (currentHeader != null) {
                currentHeader.removeFromParent();
            }
            super.setHeader(currentHeader);
        }
    }

    /**
     * @return {@code true} if the filter is added to the column header, {@code false} otherwise
     */
    public boolean isFilterable() {
        return dataGridFilter != null;
    }

    @Override
    public Grid.Column<E> setHeader(String labelText) {
        if (dataGridFilter != null) {
            dataGridFilter.setHeader(labelText);
            return this;
        }

        return super.setHeader(labelText);
    }

    @Override
    public Grid.Column<E> setHeader(Component headerComponent) {
        if (dataGridFilter != null && !(headerComponent instanceof GroupDataGridHeaderFilter)) {
            dataGridFilter.setHeader(headerComponent);
            return this;
        }

        return super.setHeader(headerComponent);
    }

    @Override
    public void setVisible(boolean visible) {
        boolean prevVisible = isVisible();
        super.setVisible(visible);
        if (prevVisible != visible) {
            fireEvent(new GroupDataGridColumnVisibilityChangedEvent<>(this, false, visible));
        }
    }

    /**
     * Add listener for event of column visibility change
     *
     * @param listener the listener to add
     * @return a registration handle to remove the listener
     */
    public Registration addColumnVisibilityChangedListener(
            ComponentEventListener<GroupDataGridColumnVisibilityChangedEvent<E>> listener) {
        Preconditions.checkNotNullArgument(listener);

        //noinspection unchecked,rawtypes
        return addListener(GroupDataGridColumnVisibilityChangedEvent.class, (ComponentEventListener) listener);
    }

    @Override
    protected Registration addDataGenerator(DataGenerator<E> dataGenerator) {
        DataGenerator<E> dataGeneratorWrapper = new DataGeneratorWrapper<E>(dataGenerator);
        return super.addDataGenerator(dataGeneratorWrapper);
    }

    /**
     * The data generator that wraps another one to manage generation data for group items.
     *
     * @param <E> type of entity
     */
    @Internal
    protected class DataGeneratorWrapper<E> implements DataGenerator<E> {

        protected final DataGenerator<E> dataGenerator;

        public DataGeneratorWrapper(DataGenerator<E> dataGenerator) {
            this.dataGenerator = dataGenerator;
        }

        @Override
        public void generateData(E item, JsonObject jsonObject) {
            if (isGroupItem(item) && !(dataGenerator instanceof HierarchicalDataGeneratorWrapper)) {
                // Skip generating data for group item
                return;
            }
            dataGenerator.generateData(item, jsonObject);
        }

        @Override
        public void destroyData(E item) {
            dataGenerator.destroyData(item);
        }

        @Override
        public void destroyAllData() {
            dataGenerator.destroyAllData();
        }

        @Override
        public void refreshData(E item) {
            dataGenerator.refreshData(item);
        }

        @SuppressWarnings("unchecked")
        protected boolean isGroupItem(E item) {
            DataCommunicator<E> dataCommunicator = (DataCommunicator<E>) grid.getDataCommunicator();

            return dataCommunicator instanceof HierarchicalDataCommunicator<E> hierarchicalDataCommunicator
                    && hierarchicalDataCommunicator.hasChildren(item);
        }
    }
}
