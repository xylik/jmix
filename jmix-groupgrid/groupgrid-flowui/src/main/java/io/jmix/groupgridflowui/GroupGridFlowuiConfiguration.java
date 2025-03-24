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

package io.jmix.groupgridflowui;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.sys.registration.ComponentRegistration;
import io.jmix.flowui.sys.registration.ComponentRegistrationBuilder;
import io.jmix.groupgridflowui.component.GroupDataGrid;
import io.jmix.groupgridflowui.loader.GroupDataGridLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@JmixModule(dependsOn = {CoreConfiguration.class, FlowuiConfiguration.class})
public class GroupGridFlowuiConfiguration {

    @Bean
    public ComponentRegistration groupDataGrid() {
        return ComponentRegistrationBuilder.create(GroupDataGrid.class)
                .withComponentLoader("groupDataGrid", GroupDataGridLoader.class)
                .build();
    }
}
