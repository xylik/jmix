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

import io.jmix.flowui.UiComponentProperties;
import io.jmix.groupgridflowui.kit.component.JmixGroupGridMenuItemActionSupport;
import io.jmix.groupgridflowui.kit.component.JmixGroupGridMenuItemActionWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("groupg_GroupDataGridMenuItemActionSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GroupDataGridMenuItemActionSupport extends JmixGroupGridMenuItemActionSupport {

    protected UiComponentProperties uiComponentProperties;

    public GroupDataGridMenuItemActionSupport(JmixGroupGridMenuItemActionWrapper<?> menuItem) {
        super(menuItem);
    }

    @Autowired
    public void setUiComponentProperties(UiComponentProperties uiComponentProperties) {
        this.uiComponentProperties = uiComponentProperties;
    }

    @Override
    protected boolean isShowActionIconEnabled() {
        return uiComponentProperties.isGridContextMenuShowActionIcons();
    }

    @Override
    protected boolean isShowActionShortcutEnabled() {
        return uiComponentProperties.isGridContextMenuShowActionShortcuts();
    }
}
