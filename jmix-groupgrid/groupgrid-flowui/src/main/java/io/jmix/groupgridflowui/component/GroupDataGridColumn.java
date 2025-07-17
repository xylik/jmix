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

import io.jmix.groupgridflowui.component.renderer.AbstractGroupRenderer;
import io.jmix.groupgridflowui.kit.vaadin.grid.Grid;

public class GroupDataGridColumn<E> extends DataGridColumn<E> {

    protected boolean autoHidden;

    public GroupDataGridColumn(Grid<E> grid, String columnId, AbstractGroupRenderer<E> renderer) {
        super(grid, columnId, renderer);
    }

    public boolean isAutoHidden() {
        return autoHidden;
    }

    public void setAutoHidden(boolean autoHidden) {
        this.autoHidden = autoHidden;
    }
}
