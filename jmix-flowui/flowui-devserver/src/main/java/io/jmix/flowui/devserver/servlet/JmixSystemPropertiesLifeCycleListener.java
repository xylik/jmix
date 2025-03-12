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

package io.jmix.flowui.devserver.servlet;

import java.util.Properties;

import org.eclipse.jetty.util.component.LifeCycle;

import static com.vaadin.flow.server.Constants.VAADIN_PREFIX;
import static com.vaadin.flow.server.InitParameters.BUILD_FOLDER;
import static com.vaadin.flow.server.InitParameters.FRONTEND_HOTDEPLOY;
import static com.vaadin.flow.server.InitParameters.SERVLET_PARAMETER_ENABLE_PNPM;
import static com.vaadin.flow.server.frontend.FrontendUtils.PROJECT_BASEDIR;

public class JmixSystemPropertiesLifeCycleListener implements LifeCycle.Listener {

    public static final String STUDIO_VIEW_DESIGNER_DIR_PROPERTY = "STUDIO_VIEW_DESIGNER_DIR";
    public static final String VIEW_DESIGNER_FOLDER = "/.jmix/screen-designer";

    private final String projectBaseDir;
    private final String isPnpmEnabled;
    private final Properties properties;

    public JmixSystemPropertiesLifeCycleListener(String projectBaseDir, String isPnpmEnabled, Properties properties) {
        this.projectBaseDir = projectBaseDir;
        this.isPnpmEnabled = isPnpmEnabled;
        this.properties = properties;
        initializeProperties();
    }

    @Override
    public void lifeCycleStarting(LifeCycle event) {
        System.getProperties().putAll(properties);
    }

    private void initializeProperties() {
        String studioViewDesignerDir = projectBaseDir + VIEW_DESIGNER_FOLDER;
        this.properties.setProperty(STUDIO_VIEW_DESIGNER_DIR_PROPERTY, studioViewDesignerDir);
        this.properties.setProperty(VAADIN_PREFIX + PROJECT_BASEDIR, projectBaseDir);
        this.properties.setProperty(VAADIN_PREFIX + BUILD_FOLDER, "build");
        this.properties.setProperty(VAADIN_PREFIX + SERVLET_PARAMETER_ENABLE_PNPM, isPnpmEnabled);
        // FIXME: something strange happens if we enable frontend hot deploy
        //  see: https://github.com/vaadin/flow/issues/19748
        this.properties.setProperty(VAADIN_PREFIX + FRONTEND_HOTDEPLOY, "false");
    }
}
