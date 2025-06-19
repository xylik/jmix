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

import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;
import io.jmix.core.annotation.Internal;

import java.util.Optional;

/**
 * Hierarchical renderer that wraps another renderer to provide a custom data generator.
 * <p>
 * The {@link HierarchicalDataGeneratorWrapper} is used to skip generating data for group items
 * in non-hierarchical columns.
 *
 * @param <E> type of entity
 * @see DataGridColumn.DataGeneratorWrapper
 */
@Internal
public class HierarchicalColumnRendererWrapper<E> extends Renderer<E> {

    private final Renderer<E> renderer;

    public HierarchicalColumnRendererWrapper(Renderer<E> renderer) {
        this.renderer = renderer;
    }

    @Override
    public Rendering<E> render(Element container, DataKeyMapper<E> keyMapper, String rendererName) {
        Rendering<E> rendering = renderer.render(container, keyMapper, rendererName);
        return new Rendering<E>() {
            @Override
            public Optional<DataGenerator<E>> getDataGenerator() {

                return rendering.getDataGenerator().isPresent()
                        ? Optional.of(new HierarchicalDataGeneratorWrapper<>(rendering.getDataGenerator().get()))
                        : rendering.getDataGenerator();
            }

            @Override
            public Registration getRegistration() {
                return rendering.getRegistration();
            }
        };
    }

    public static class HierarchicalDataGeneratorWrapper<E> implements DataGenerator<E> {

        protected final DataGenerator<E> dataGenerator;

        public HierarchicalDataGeneratorWrapper(DataGenerator<E> dataGenerator) {
            this.dataGenerator = dataGenerator;
        }

        @Override
        public void generateData(E item, JsonObject jsonObject) {
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
    }
}
