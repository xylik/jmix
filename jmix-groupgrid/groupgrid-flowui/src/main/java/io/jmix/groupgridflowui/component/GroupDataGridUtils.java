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

import com.vaadin.flow.data.provider.SortDirection;

public final class GroupDataGridUtils {

    private GroupDataGridUtils() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void copyColumnProperties(DataGridColumn from, DataGridColumn to) {
        to.setRenderer(from.getRenderer());
        if (from.getWidth() != null) {
            to.setWidth(from.getWidth());
        }
        to.setFlexGrow(from.getFlexGrow());
        to.setAutoWidth(from.isAutoWidth());
        to.setComparator(from.getComparator(SortDirection.ASCENDING)); // It returns comparator as is
        to.setClassName(from.getClassName());
        to.setSortOrderProvider(from._getSortOrderProvider());
        to.setSortable(from.isSortable());
        if (from.getHeaderComponent() != null) {
            to.setHeader(from.getHeaderComponent());
        } else {
            to.setHeader(from.getHeaderText());
        }
        if (from.getFooterComponent() != null) {
            to.setFooter(from.getFooterComponent());
        } else {
            to.setFooter(from.getFooterText());
        }
        if (from.getEditorComponent() != null) {
            to.setEditorComponent(from.getEditorComponent());
        } else if (from._getEditorComponentCallback() != null) {
            to.setEditorComponent(from._getEditorComponentCallback());
        }
        if (from.getClassNameGenerator() != null) {
            to.setClassNameGenerator(from.getClassNameGenerator());
        }
        if (from.getPartNameGenerator() != null) {
            to.setPartNameGenerator(from.getPartNameGenerator());
        }
        if (from.getTooltipGenerator() != null) {
            to.setTooltipGenerator(from.getTooltipGenerator());
        }
        if (from.isRowHeader()) {
            to.setRowHeader(from.isRowHeader());
        }
    }
}
