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

package io.jmix.groupgridflowui.data.provider;

import com.vaadin.flow.function.ValueProvider;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.groupgridflowui.data.HierarchicalGroupDataGridItems;
import org.springframework.lang.Nullable;

import java.util.function.Supplier;

public class GroupDataGridStringPresentationValueProvider<T> implements ValueProvider<T, String> {

    protected MetaPropertyPath propertyPath;
    protected MetadataTools metadataTools;
    protected Supplier<HierarchicalGroupDataGridItems<T>> dataGridItemsSupplier;

    public GroupDataGridStringPresentationValueProvider(@Nullable MetaPropertyPath propertyPath,
                                                        MetadataTools metadataTools,
                                                        Supplier<HierarchicalGroupDataGridItems<T>> dataGridItemsSupplier) {
        this.propertyPath = propertyPath;
        this.metadataTools = metadataTools;
        this.dataGridItemsSupplier = dataGridItemsSupplier;
    }

    @Override
    public String apply(T entity) {
        if (dataGridItemsSupplier.get() != null
                && dataGridItemsSupplier.get().hasChildren(entity)) {
            return "";
        }

        Object value = EntityValues.getValueEx(entity, propertyPath);
        return propertyPath != null
                ? metadataTools.format(value, propertyPath.getMetaProperty())
                : metadataTools.format(value);
    }
}
