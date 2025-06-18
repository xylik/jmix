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

package io.jmix.groupgridflowui.kit.component;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.internal.AllowInert;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalArrayUpdater.HierarchicalUpdate;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.*;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.server.VaadinRequest;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.SelectionChangeNotifier;
import io.jmix.groupgridflowui.kit.vaadin.grid.Grid;
import io.jmix.groupgridflowui.kit.vaadin.grid.GridArrayUpdater;
import io.jmix.groupgridflowui.kit.vaadin.grid.GridArrayUpdater.UpdateQueueData;
import io.jmix.groupgridflowui.kit.vaadin.treegrid.TreeGrid;
import io.jmix.groupgridflowui.kit.vaadin.treegrid.TreeGridArrayUpdater;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JmixGroupGrid<T> extends Grid<T> implements SelectionChangeNotifier<Grid<T>, T>, HasActions {

    protected JmixGroupGridActionsSupport<JmixGroupGrid<T>, T> actionsSupport;

    public JmixGroupGrid() {
        super(50, JmixGroupGridUpdateQueue::new, new JmixGroupGridDataCommunicatorBuilder<>());

        setUniqueKeyProperty("key");
        getArrayUpdater().getUpdateQueueData()
                .setHasExpandedItems(getDataCommunicator()::hasExpandedItems);

        addItemHasChildrenPathGenerator();
    }

    @Override
    public void addAction(Action action) {
        getActionsSupport().addAction(action);
    }

    @Override
    public void addAction(Action action, int index) {
        getActionsSupport().addAction(action, index);
    }

    @Override
    public void removeAction(Action action) {
        getActionsSupport().removeAction(action);
    }

    @Override
    public Collection<Action> getActions() {
        return getActionsSupport().getActions();
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        return getActionsSupport().getAction(id).orElse(null);
    }

    public JmixGroupGridActionsSupport<JmixGroupGrid<T>, T> getActionsSupport() {
        if (actionsSupport == null) {
            actionsSupport = createActionsSupport();
        }
        return actionsSupport;
    }

    // TODO: rp

    /**
     * See {@link TreeGrid} and {@code addItemHasChildrenPathGenerator()}.
     */
    private void addItemHasChildrenPathGenerator() {
        addDataGenerator((T item, JsonObject jsonObject) -> {
            if (getDataCommunicator().hasChildren(item)) {
                jsonObject.put("children", true);
            }
        });
    }

    @Override
    public JmixGroupGridDataCommunicator<T> getDataCommunicator() {
        return (JmixGroupGridDataCommunicator<T>) super.getDataCommunicator();
    }

    protected void expand(Collection<T> items, boolean userOriginated) {
        Collection<T> collapsedItems = getDataCommunicator().expand(items);
        // TODO: rp Fire event
    }

    protected void collapse(Collection<T> items, boolean userOriginated) {
        Collection<T> collapsedItems = getDataCommunicator().collapse(items);
        // TODO: rp Fire event
    }

    protected JmixGroupGridActionsSupport<JmixGroupGrid<T>, T> createActionsSupport() {
        return new JmixGroupGridActionsSupport<>(this);
    }

    @Override
    protected GridArrayUpdater createDefaultArrayUpdater(
            SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueFactory) {
        return new JmixGroupGridArrayUpdaterImpl(updateQueueFactory);
    }

    @Override
    protected void generateSelectableData(T item, JsonObject jsonObject) {
        if (getDataCommunicator().hasChildren(item)) {
            jsonObject.put("selectable", false);
        } else {
            super.generateSelectableData(item, jsonObject);
        }
    }

    @Override
    protected Grid<T>.DetailsManager createDetailsManager() {
        return new JmixDetailsManager(this);
    }

    @AllowInert
    @ClientCallable(DisabledUpdateMode.ALWAYS)
    private void setParentRequestedRange(int start, int length, String parentKey) {
        T item = getDataCommunicator().getKeyMapper().get(parentKey);
        if (item != null) {
            getDataCommunicator().setParentRequestedRange(start, length, item);
        }
    }

    @AllowInert
    @ClientCallable(DisabledUpdateMode.ALWAYS)
    private void setParentRequestedRanges(JsonArray array) {
        for (int index = 0; index < array.length(); index++) {
            JsonObject object = array.getObject(index);
            setParentRequestedRange((int) object.getNumber("firstIndex"),
                    (int) object.getNumber("size"),
                    object.getString("parentKey"));
        }
    }

    @ClientCallable(DisabledUpdateMode.ONLY_WHEN_ENABLED)
    private void updateExpandedState(String key, boolean expanded) {
        T item = getDataCommunicator().getKeyMapper().get(key);
        if (item != null) {
            if (expanded) {
                expand(List.of(item), true);
            } else {
                collapse(List.of(item), true);
            }
        }
    }

    @AllowInert
    @ClientCallable(DisabledUpdateMode.ALWAYS)
    private void confirmParentUpdate(int id, String parentKey) {
        getDataCommunicator().confirmUpdate(id, parentKey);
    }

    protected static class JmixGroupGridDataCommunicatorBuilder<E>
            extends DataCommunicatorBuilder<E, TreeGridArrayUpdater> {

        @Override
        protected DataCommunicator<E> build(Element element,
                                            CompositeDataGenerator<E> dataGenerator,
                                            TreeGridArrayUpdater arrayUpdater,
                                            SerializableSupplier<ValueProvider<E, String>> uniqueKeyProviderSupplier) {
            SerializableConsumer<JsonArray> dataUpdater = (data) -> {
                element.callJsFunction("$connector.updateHierarchicalData", data);
            };

            return new JmixGroupGridDataCommunicator<>(dataGenerator, arrayUpdater, dataUpdater, element.getNode(),
                    uniqueKeyProviderSupplier);
        }
    }

    protected static class JmixGroupGridUpdateQueue extends UpdateQueue
            implements HierarchicalUpdate {

        private SerializableConsumer<List<JsonValue>> arrayUpdateListener;

        private JmixGroupGridUpdateQueue(UpdateQueueData data, int size) {
            super(data, size);
        }

        public void setArrayUpdateListener(
                SerializableConsumer<List<JsonValue>> arrayUpdateListener) {
            this.arrayUpdateListener = arrayUpdateListener;
        }

        @Override
        public void set(int start, List<JsonValue> items) {
            super.set(start, items);

            if (arrayUpdateListener != null) {
                arrayUpdateListener.accept(items);
            }
        }

        @Override
        public void set(int start, List<JsonValue> items, String parentKey) {
            enqueue("$connector.set", start,
                    items.stream().collect(JsonUtils.asArray()), parentKey);

            if (arrayUpdateListener != null) {
                arrayUpdateListener.accept(items);
            }
        }

        @Override
        public void clear(int start, int length) {
            if (!getData().getHasExpandedItems().get()) {
                enqueue("$connector.clearExpanded");
            }
            super.clear(start, length);
        }

        @Override
        public void clear(int start, int length, String parentKey) {
            enqueue("$connector.clear", start, length, parentKey);
        }

        @Override
        public void commit(int updateId, String parentKey, int levelSize) {
            enqueue("$connector.confirmParent", updateId, parentKey, levelSize);
            commit();
        }
    }

    private class JmixGroupGridArrayUpdaterImpl implements TreeGridArrayUpdater {
        // Approximated size of the viewport. Used for eager fetching.
        private static final int EAGER_FETCH_VIEWPORT_SIZE_ESTIMATE = 40;

        private UpdateQueueData data;
        private SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueFactory;
        private int viewportRemaining = 0;
        private final List<JsonValue> queuedParents = new ArrayList<>();
        private transient VaadinRequest previousRequest;

        public JmixGroupGridArrayUpdaterImpl(
                SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueFactory) {
            this.updateQueueFactory = updateQueueFactory;
        }

        @Override
        public JmixGroupGridUpdateQueue startUpdate(int sizeChange) {
            JmixGroupGridUpdateQueue queue = (JmixGroupGridUpdateQueue) updateQueueFactory.apply(data, sizeChange);

            if (VaadinRequest.getCurrent() != null
                    && !VaadinRequest.getCurrent().equals(previousRequest)) {
                // Reset the viewportRemaining once for a server roundtrip.
                viewportRemaining = EAGER_FETCH_VIEWPORT_SIZE_ESTIMATE;
                queuedParents.clear();
                previousRequest = VaadinRequest.getCurrent();
            }

            queue.setArrayUpdateListener((items) -> {
                // Prepend the items to the queue of potential parents.
                queuedParents.addAll(0, items);

                while (viewportRemaining > 0 && !queuedParents.isEmpty()) {
                    viewportRemaining--;
                    JsonObject parent = (JsonObject) queuedParents.remove(0);
                    T parentItem = getDataCommunicator().getKeyMapper()
                            .get(parent.getString("key"));

                    if (getDataCommunicator().isExpanded(parentItem)) {
                        int childLength = Math.max(
                                EAGER_FETCH_VIEWPORT_SIZE_ESTIMATE,
                                getPageSize());

                        // There's still room left in the viewport and the item
                        // is expanded. Set parent requested range for it.
                        getDataCommunicator().setParentRequestedRange(0,
                                childLength, parentItem);

                        // Stop iterating the items on this level. The request
                        // for child items above will end up back in this while
                        // loop, and to processing any parent siblings that
                        // might be left in the queue.
                        break;
                    }

                }
            });

            return queue;
        }

        @Override
        public void initialize() {
            initConnector();
            updateSelectionModeOnClient();
            getDataCommunicator().setRequestedRange(0, getPageSize());
        }

        @Override
        public void setUpdateQueueData(UpdateQueueData data) {
            this.data = data;
        }

        @Override
        public UpdateQueueData getUpdateQueueData() {
            return data;
        }
    }

    protected class JmixDetailsManager extends DetailsManager {

        public JmixDetailsManager(Grid<T> grid) {
            super(grid);
        }

        @Override
        public void generateData(T item, JsonObject jsonObject) {
            if (getDataCommunicator().hasChildren(item)) {
                // Do not generate details for group items
                return;
            }

            super.generateData(item, jsonObject);
        }
    }
}
