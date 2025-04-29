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

import io.jmix.groupgridflowui.component.GroupDataGrid;
import io.jmix.groupgridflowui.data.HierarchicalGroupDataGridItems;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("flowui_GroupGridDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GroupGridDelegate<E, ITEMS extends HierarchicalGroupDataGridItems<E>>
        extends AbstractGroupGridDelegate<GroupDataGrid<E>, E, ITEMS> {

    public GroupGridDelegate(GroupDataGrid<E> component) {
        super(component);
    }
}
