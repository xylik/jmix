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

package io.jmix.groupgridflowui.component;

import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.groupgridflowui.kit.component.JmixGroupGridContextMenu;
import io.jmix.groupgridflowui.kit.vaadin.grid.Grid.Column;
import org.springframework.lang.Nullable;

public interface EnhancedGroupDataGrid<T> {


    @Nullable
    MetaPropertyPath getColumnMetaPropertyPath(Column<T> column);

    /**
     * @param metaPropertyPath {@link MetaPropertyPath} that refers to the column
     * @return {@link DataGridColumn} that is bound to the passed {@code metaPropertyPath}
     */
    @Nullable
    DataGridColumn<T> getColumnByMetaPropertyPath(MetaPropertyPath metaPropertyPath);

    DataGridColumn<T> addColumn(MetaPropertyPath metaPropertyPath);

    DataGridColumn<T> addColumn(String key, MetaPropertyPath metaPropertyPath);

    boolean isEditorCreated();

    /**
     * @return true if DataGrid is aggregatable
     */
    /*boolean isAggregatable();*/ // TODO: pinyazhin, aggregation

    /**
     * Set to true if aggregation should be enabled. Default value is false.
     *
     * @param aggregatable whether to aggregate DataGrid columns
     */
    /*void setAggregatable(boolean aggregatable);*/ // TODO: pinyazhin, aggregation

    /**
     * @return return aggregation row position
     */
    /*AggregationPosition getAggregationPosition();*/ // TODO: pinyazhin, aggregation

    /**
     * Sets aggregation row position. Default value is {@link AggregationPosition#BOTTOM}.
     *
     * @param position position: {@link AggregationPosition#TOP} or {@link AggregationPosition#BOTTOM}
     */
    /*void setAggregationPosition(AggregationPosition position);*/ // TODO: pinyazhin, aggregation

    /**
     * Add an aggregation info in order to perform aggregation for column.
     *
     * @param column column for aggregation
     * @param info   aggregation info
     * @see GroupDataGrid#setAggregatable(boolean)
     */
    /*void addAggregation(Column<T> column, AggregationInfo info);*/ // TODO: pinyazhin, aggregation

    /**
     * @return aggregated values for columns
     */
    /*Map<Column<T>, Object> getAggregationResults();*/ // TODO: pinyazhin, aggregation

    /**
     * @return context menu instance attached to the grid
     */
    JmixGroupGridContextMenu<T> getContextMenu();

    /**
     * Defines the position of aggregation row.
     */
/*    enum AggregationPosition { // TODO: pinyazhin, aggregation
        TOP,
        BOTTOM
    }*/
}
