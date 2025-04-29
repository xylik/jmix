/*
 * Copyright 2022 Haulmont.
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

package io.jmix.groupgridflowui.kit.component;

import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.delegate.AbstractActionsHolderSupport;
import io.jmix.groupgridflowui.kit.vaadin.grid.Grid;
import io.jmix.groupgridflowui.kit.vaadin.grid.contextmenu.GridMenuItem;

import java.util.HashMap;
import java.util.Map;

public class JmixGroupGridActionsSupport<C extends Grid<T>, T> extends AbstractActionsHolderSupport<C> {

    protected Map<Action, JmixGroupGridMenuItemActionWrapper<T>> actionBinding = new HashMap<>();

    protected JmixGroupGridContextMenu<T> contextMenu;
    protected boolean showActionsInContextMenuEnabled = true;

    public JmixGroupGridActionsSupport(C component) {
        super(component);
    }

    @Override
    protected void addActionInternal(Action action, int index) {
        super.addActionInternal(action, index);

        if (showActionsInContextMenuEnabled) {
            addContextMenuItem(action);
            updateContextMenu();
        }
    }

    protected void addContextMenuItem(Action action) {
        int index = actions.indexOf(action);
        JmixGroupGridMenuItemActionWrapper<T> wrapper = createContextMenuItemComponent();
        GridMenuItem<T> menuItem = getContextMenu().addItemAtIndex(index, wrapper);

        wrapper.setMenuItem(menuItem);
        wrapper.setAction(action);

        actionBinding.put(action, wrapper);
    }

    protected JmixGroupGridMenuItemActionWrapper<T> createContextMenuItemComponent() {
        return new JmixGroupGridMenuItemActionWrapper<>();
    }

    protected JmixGroupGridContextMenu<T> getContextMenu() {
        if (contextMenu == null) {
            initContextMenu();
        }
        return contextMenu;
    }

    protected void initContextMenu() {
        contextMenu = new JmixGroupGridContextMenu<>();
        contextMenu.setTarget(component);
        contextMenu.setVisible(false);
    }

    protected void updateContextMenu() {
        JmixGroupGridContextMenu<T> contextMenu = getContextMenu();
        boolean empty = contextMenu.getItems().isEmpty();
        boolean visible = contextMenu.isVisible();

        // empty | visible | result visible
        //  true |    true |   -> false
        //  true |   false | keep false
        // false |    true | keep  true
        // false |   false |   ->  true
        if (empty == visible) {
            contextMenu.setVisible(!visible);
        }
    }

    @Override
    protected boolean removeActionInternal(Action action) {
        if (super.removeActionInternal(action)) {
            if (showActionsInContextMenuEnabled) {
                removeContextMenuItem(action);
                updateContextMenu();
            }

            return true;
        }

        return false;
    }

    protected void removeContextMenuItem(Action action) {
        JmixGroupGridMenuItemActionWrapper<T> item = actionBinding.remove(action);
        item.setAction(null);

        getContextMenu().remove(item.getMenuItem());
    }

    /**
     * @return true if actions are shown in grid context menu, false otherwise
     */
    public boolean isShowActionsInContextMenuEnabled() {
        return showActionsInContextMenuEnabled;
    }

    /**
     * Sets whether to show actions in grid context menu.
     *
     * @param showActionsInContextMenuEnabled true if actions should be shown to context menu, false otherwise
     */
    public void setShowActionsInContextMenuEnabled(boolean showActionsInContextMenuEnabled) {
        this.showActionsInContextMenuEnabled = showActionsInContextMenuEnabled;
    }
}
