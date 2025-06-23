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

import com.google.common.base.Strings;
import com.google.common.primitives.Booleans;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.AccessManager;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.accesscontext.EntityAttributeContext;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.action.list.EditAction;
import io.jmix.flowui.action.list.ReadAction;
import io.jmix.flowui.app.datagrid.DataGridEmptyStateByPermissionsFragment;
import io.jmix.flowui.component.*;
import io.jmix.flowui.component.delegate.AbstractComponentDelegate;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.data.aggregation.Aggregation;
import io.jmix.flowui.data.aggregation.Aggregations;
import io.jmix.flowui.data.aggregation.impl.AggregatableDelegate;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.sys.BeanUtil;
import io.jmix.groupgridflowui.component.editor.GroupDataGridEditor;
import io.jmix.groupgridflowui.component.DataGridColumn;
import io.jmix.groupgridflowui.component.EnhancedGroupDataGrid;
import io.jmix.groupgridflowui.component.GroupDataGridDataProviderChangeObserver;
import io.jmix.groupgridflowui.component.editor.GroupDataGridEditorImpl;
import io.jmix.groupgridflowui.data.HierarchicalGroupDataGridItems;
import io.jmix.groupgridflowui.data.provider.GroupDataGridStringPresentationValueProvider;
import io.jmix.groupgridflowui.kit.vaadin.grid.*;
import io.jmix.groupgridflowui.kit.vaadin.grid.Grid.Column;
import io.jmix.groupgridflowui.kit.vaadin.grid.Grid.SelectionMode;
import io.jmix.groupgridflowui.kit.vaadin.grid.editor.Editor;
import io.jmix.groupgridflowui.kit.vaadin.grid.editor.EditorCloseEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractGroupGridDelegate<C extends Grid<E> & ListDataComponent<E> & EnhancedGroupDataGrid<E>
        & HasActions, E, ITEMS extends HierarchicalGroupDataGridItems<E>> extends AbstractComponentDelegate<C>
        implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected Fragments fragments;
    protected MetadataTools metadataTools;
    protected MessageTools messageTools;
    protected AccessManager accessManager;
    protected Aggregations aggregations;
    protected AggregatableDelegate<Object> aggregatableDelegate;

    protected ITEMS dataGridItems;

    protected Registration selectionListenerRegistration;
    protected Registration itemSetChangeRegistration;
    protected Registration valueChangeRegistration;

    // own selection listeners registration is needed to keep listeners if selection model is changed
    protected Set<SelectionListener<Grid<E>, E>> selectionListeners = new HashSet<>();

    protected Set<ComponentEventListener<ItemDoubleClickEvent<E>>> itemDoubleClickListeners = new HashSet<>();
    protected Consumer<SupportsEnterPress.EnterPressEvent<C>> enterPressHandler;

    protected Consumer<ColumnSecurityContext<E>> afterColumnSecurityApplyHandler;

    protected String emptyStateTextInternal;
    protected Component emptyStateComponentInternal;

    protected Consumer<String> componentEmptyStateTextDelegate;
    protected Consumer<Component> componentEmptyStateComponentDelegate;
    protected Registration emptyStateByPermissionRegistration;

    protected boolean aggregatable;
    protected EnhancedGroupDataGrid.AggregationPosition aggregationPosition = EnhancedGroupDataGrid.AggregationPosition.BOTTOM;
    protected Map<Column<E>, AggregationInfo> aggregationMap = new LinkedHashMap<>();

    protected HeaderRow aggregationHeader;
    protected FooterRow aggregationFooter;

    /**
     * Columns that are bounded with data container (loaded from descriptor or
     * added using {@link #addColumn(String, MetaPropertyPath)}).
     */
    protected Map<Column<E>, MetaPropertyPath> propertyColumns = new HashMap<>();

    /**
     * Contains all columns like a Grid and additionally hidden columns by security.
     * The order of columns corresponds to the client side column order.
     */
    protected List<Column<E>> columns = new ArrayList<>();

    public AbstractGroupGridDelegate(C component) {
        super(component);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        fragments = applicationContext.getBean(Fragments.class);
        metadataTools = applicationContext.getBean(MetadataTools.class);
        messageTools = applicationContext.getBean(MessageTools.class);
        accessManager = applicationContext.getBean(AccessManager.class);
        aggregations = applicationContext.getBean(Aggregations.class);
    }

    protected void initComponent() {
        component.addSortListener(this::onSort);
        component.addColumnReorderListener(this::onColumnReorderChange);
        addSelectionListener(this::notifyDataProviderSelectionChanged);

        // Can't use method reference, because of compilation error
        //noinspection Convert2Lambda,Anonymous2MethodRef
        ComponentUtil.addListener(component, ItemDoubleClickEvent.class, new ComponentEventListener<>() {
            @Override
            public void onComponentEvent(ItemDoubleClickEvent event) {
                //noinspection unchecked
                onItemDoubleClick(event);
            }
        });

        Shortcuts.addShortcutListener(component, this::handleEnterPress, Key.ENTER)
                .listenOn(component)
                .allowBrowserDefault();
    }

    @Nullable
    public ITEMS getItems() {
        return dataGridItems;
    }

    public void setItems(@Nullable ITEMS dataGridItems) {
        unbind();

        if (dataGridItems != null) {
            this.dataGridItems = dataGridItems;

            bind(dataGridItems);
            updateAggregationRow();

            applySecurityToPropertyColumns();
        }
    }

    protected void bind(DataGridItems<E> dataGridItems) {
        itemSetChangeRegistration = dataGridItems.addItemSetChangeListener(this::itemsItemSetChanged);
        valueChangeRegistration = dataGridItems.addValueChangeListener(this::itemsValueChanged);
    }

    protected void unbind() {
        if (dataGridItems != null) {
            dataGridItems = null;
            setupEmptyDataProvider();
        }

        if (itemSetChangeRegistration != null) {
            itemSetChangeRegistration.remove();
            itemSetChangeRegistration = null;
        }

        if (valueChangeRegistration != null) {
            valueChangeRegistration.remove();
            valueChangeRegistration = null;
        }
    }

    protected void itemsItemSetChanged(DataGridItems.ItemSetChangeEvent<E> event) {
        closeEditorIfOpened();
        component.getDataCommunicator().reset();
        updateAggregationRow();
        //refresh selection because it contains old item instances which may not exist in the container anymore
        refreshSelection(event.getSource().getItems());
    }

    protected void closeEditorIfOpened() {
        if (getComponent().isEditorCreated()
                && getComponent().getEditor().isOpen()) {
            Editor<E> editor = getComponent().getEditor();
            if (editor.isBuffered()) {
                editor.cancel();
            } else {
                editor.closeEditor();
            }

            if (editor instanceof GroupDataGridDataProviderChangeObserver) {
                ((GroupDataGridDataProviderChangeObserver) editor).dataProviderChanged();
            }
        }
    }

    /**
     * Refreshes current selection using provided items.
     */
    protected void refreshSelection(Collection<E> items) {
        Set<E> prevSelectedItemsToRefresh = new HashSet<>(getSelectedItems());

        List<E> itemsToSelect = new ArrayList<>(prevSelectedItemsToRefresh.size());
        for (E item : items) {
            //select the item if it was selected before refresh
            if (prevSelectedItemsToRefresh.remove(item)) {
                itemsToSelect.add(item);
            }

            //skip further checks if no more items were selected
            if (prevSelectedItemsToRefresh.isEmpty()) {
                break;
            }
        }
        //selection model doesn't provide direct access to selected items,
        //so to update the model we are forced to deselect all items and select new items again
        //to handle any changes in item collection or items themselves
        deselectAll();
        select(itemsToSelect);
    }

    protected void itemsValueChanged(DataGridItems.ValueChangeEvent<E> event) {
        if (itemIsBeingEdited(event.getItem())) {
            GroupDataGridEditor<E> editor = ((GroupDataGridEditor<E>) getComponent().getEditor());
            // Do not interrupt the save process
            if (editor.isBuffered() && !editor.isSaving()) {
                editor.cancel();
            } else {
                // In case of unbuffered editor, we don't need to refresh an item,
                // because it results in row repainting, i.e. all editor components
                // are recreated and focus lost. In case of buffered editor in a
                // save process, an item will be refreshed after editor is closed.
                return;
            }
        }

        component.getDataCommunicator().refresh(event.getItem());
        updateAggregationRow();
    }

    protected boolean itemIsBeingEdited(E item) {
        if (getComponent().isEditorCreated()) {
            Editor<E> editor = getComponent().getEditor();
            return editor.isOpen()
                    && Objects.equals(item, editor.getItem());
        }

        return false;
    }

    @Nullable
    public E getSingleSelectedItem() {
        return getSelectionModel()
                .getFirstSelectedItem()
                .orElse(null);
    }

    public Set<E> getSelectedItems() {
        return getSelectionModel().getSelectedItems();
    }

    public void select(E item) {
        Preconditions.checkNotNullArgument(item);

        select(Collections.singletonList(item));
    }

    public void select(Collection<E> items) {
        if (CollectionUtils.isNotEmpty(items)) {
            if (isMultiSelect()) {
                //noinspection unchecked
                ((SelectionModel.Multi<Grid<E>, E>) getSelectionModel())
                        .updateSelection(new LinkedHashSet<>(items), Collections.emptySet());
            } else {
                getSelectionModel().select(items.iterator().next());
            }
        } else {
            deselectAll();
        }
    }

    public void deselect(E item) {
        getSelectionModel().deselect(item);
    }

    public void deselectAll() {
        getSelectionModel().deselectAll();
    }

    public boolean isMultiSelect() {
        return getSelectionModel() instanceof SelectionModel.Multi;
    }

    public void enableMultiSelect() {
        setMultiSelect(true);
    }

    public void setMultiSelect(boolean multiSelect) {
        component.setSelectionMode(multiSelect
                ? SelectionMode.MULTI
                : SelectionMode.SINGLE);
    }

    public Registration addSelectionListener(SelectionListener<Grid<E>, E> listener) {
        if (selectionListenerRegistration == null) {
            attachSelectionListener();
        }

        selectionListeners.add(listener);

        return () -> {
            selectionListeners.remove(listener);
            if (selectionListeners.isEmpty()) {
                detachSelectionListener();
            }
        };
    }

    public Registration addItemDoubleClickListener(ComponentEventListener<ItemDoubleClickEvent<E>> listener) {
        itemDoubleClickListeners.add(listener);
        return () -> itemDoubleClickListeners.remove(listener);
    }


    public void setEnterPressHandler(@Nullable Consumer<SupportsEnterPress.EnterPressEvent<C>> handler) {
        this.enterPressHandler = handler;
    }

    public boolean isAggregatable() {
        return aggregatable;
    }

    public void setAggregatable(boolean aggregatable) {
        this.aggregatable = aggregatable;

        updateAggregationRow();
    }

    public EnhancedGroupDataGrid.AggregationPosition getAggregationPosition() {
        return aggregationPosition;
    }

    public void setAggregationPosition(EnhancedGroupDataGrid.AggregationPosition position) {
        this.aggregationPosition = position;
    }

    public void addAggregationInfo(Column<E> column, AggregationInfo info) {
        if (aggregationMap.containsKey(column)) {
            throw new IllegalStateException(String.format("Aggregation property %s already exists", column.getKey()));
        }

        aggregationMap.put(column, info);
    }

    public void removeAggregationInfo(Column<E> column) {
        aggregationMap.remove(column);
    }

    public Map<Column<E>, Object> getAggregationResults() {
        return aggregateValues();
    }

    protected Map<Column<E>, String> aggregate() {
        if (!isAggregatable() || getItems() == null) {
            throw new IllegalStateException(String.format("%s must be aggregatable and items must not be null in " +
                    "order to use aggregation", component.getClass().getSimpleName()));
        }

        List<AggregationInfo> aggregationInfos = getAggregationInfos();

        Map<AggregationInfo, String> aggregationInfoMap = getAggregatableDelegate().aggregate(
                aggregationInfos.toArray(new AggregationInfo[0]),
                getItems().getItems().stream()
                        .map(EntityValues::getIdOrEntity)
                        .toList()
        );

        return convertAggregationKeyMapToColumnMap(aggregationInfoMap);
    }

    protected Map<Column<E>, Object> aggregateValues() {
        if (!isAggregatable() || getItems() == null) {
            throw new IllegalStateException("DataGrid must be aggregatable and items must not be null in order to " +
                    "use aggregation");
        }

        List<AggregationInfo> aggregationInfos = getAggregationInfos();

        Map<AggregationInfo, Object> aggregationInfoMap = getAggregatableDelegate().aggregateValues(
                aggregationInfos.toArray(new AggregationInfo[0]),
                getItems().getItems().stream()
                        .map(EntityValues::getId)
                        .toList()
        );

        return convertAggregationKeyMapToColumnMap(aggregationInfoMap);
    }

    protected <V> Map<Column<E>, V> convertAggregationKeyMapToColumnMap(Map<AggregationInfo, V> aggregationInfos) {
        return aggregationMap.entrySet()
                .stream()
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), aggregationInfos.get(entry.getValue())),
                        LinkedHashMap::putAll);
    }

    protected List<AggregationInfo> getAggregationInfos() {
        return aggregationMap.values()
                .stream()
                .filter(this::checkAggregation)
                .toList();
    }

    protected boolean checkAggregation(AggregationInfo aggregationInfo) {
        AggregationInfo.Type aggregationType = aggregationInfo.getType();
        if (aggregationType == AggregationInfo.Type.CUSTOM) {
            return true;
        }

        MetaPropertyPath propertyPath = aggregationInfo.getPropertyPath();
        if (propertyPath == null) {
            throw new IllegalArgumentException("Unable to aggregate column without property");
        }

        Class<?> javaType = propertyPath.getMetaProperty().getJavaType();
        Aggregation<?> aggregation = aggregations.get(javaType);

        if (aggregation != null && aggregation.getSupportedAggregationTypes().contains(aggregationType)) {
            return true;
        }

        String message = String.format("Unable to aggregate column \"%s\" with data type %s " +
                        "with default aggregation strategy: %s",
                propertyPath, propertyPath.getRange(), aggregationInfo.getType());

        throw new IllegalArgumentException(message);
    }

    @SuppressWarnings("DuplicatedCode")
    protected void fillAggregationRow(Map<Column<E>, String> values) {
        switch (getAggregationPosition()) {
            case TOP -> {
                if (aggregationHeader == null) {
                    aggregationHeader = component.appendHeaderRow();
                }

                fillHeaderRow(values);
            }
            case BOTTOM -> {
                if (aggregationFooter == null) {
                    aggregationFooter = component.prependFooterRow();
                }

                fillFooterRow(values);
            }
        }
    }

    protected void fillHeaderRow(Map<Column<E>, String> values) {
        for (Map.Entry<Column<E>, String> entry : values.entrySet()) {
            Column<E> column = entry.getKey();
            HeaderRow.HeaderCell cell = aggregationHeader.getCell(column);
            String cellTitle = aggregationMap.get(column).getCellTitle();

            if (cellTitle != null) {
                Span headerSpan = new Span(entry.getValue());
                headerSpan.setTitle(cellTitle);

                cell.setComponent(headerSpan);
            } else {
                cell.setText(entry.getValue());
            }
        }
    }

    protected void fillFooterRow(Map<Column<E>, String> values) {
        for (Map.Entry<Column<E>, String> entry : values.entrySet()) {
            Column<E> column = entry.getKey();
            FooterRow.FooterCell cell = aggregationFooter.getCell(column);
            String cellTitle = aggregationMap.get(column).getCellTitle();

            if (cellTitle != null) {
                Span footerSpan = new Span(entry.getValue());
                footerSpan.setTitle(cellTitle);

                cell.setComponent(footerSpan);
            } else {
                cell.setText(entry.getValue());
            }
        }
    }

    protected void updateAggregationRow() {
        if (isAggregatable()
                && getItems() != null
                && MapUtils.isNotEmpty(aggregationMap)) {
            Map<Column<E>, String> results = aggregate();
            fillAggregationRow(results);
        }
    }

    public BiFunction<Renderer<E>, String, Column<E>> getDefaultColumnFactory() {
        return (Renderer<E> renderer, String columnId) -> {
            DataGridColumn<E> dataGridColumn =
                    new DataGridColumn<>(component, columnId, renderer);
            BeanUtil.autowireContext(applicationContext, dataGridColumn);
            return dataGridColumn;
        };
    }

    @Nullable
    public MetaPropertyPath getColumnMetaPropertyPath(Column<E> column) {
        return propertyColumns.get(column);
    }

    /**
     * @param propertyPath {@link MetaPropertyPath} that refers to the column
     * @return {@link DataGridColumn} that is bound to the passed {@code propertyPath}
     */
    @Nullable
    public DataGridColumn<E> getColumnByMetaPropertyPath(MetaPropertyPath propertyPath) {
        return (DataGridColumn<E>) propertyColumns.entrySet().stream()
                .filter(entry -> propertyPath.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findAny()
                .orElse(null);
    }

    public DataGridColumn<E> addColumn(String key, MetaPropertyPath metaPropertyPath) {
        Column<E> column = addColumnInternal(key, metaPropertyPath);
        propertyColumns.put(column, metaPropertyPath);
        return (DataGridColumn<E>) column;
    }

    public DataGridColumn<E> addColumn(Column<E> column) {
        columns.add(column);

        // always DataGridColumn expected cause #getDefaultColumnFactory
        return (DataGridColumn<E>) column;
    }

    protected void setupEmptyDataProvider() {
        component.setItems(new ListDataProvider<>(Collections.emptyList()));
    }

    protected Column<E> addColumnInternal(String key, MetaPropertyPath metaPropertyPath) {
        ValueProvider<E, ?> valueProvider = getValueProvider(metaPropertyPath);

        // Also it leads to adding column to {@link #columns} list
        Column<E> column = component.addColumn(valueProvider);
        column.setKey(key);

        initColumn(column, metaPropertyPath);

        return column;
    }

    protected ValueProvider<E, ?> getValueProvider(MetaPropertyPath metaPropertyPath) {
        return new GroupDataGridStringPresentationValueProvider<>(metaPropertyPath, metadataTools, () -> dataGridItems);
    }

    protected void initColumn(Column<E> column, MetaPropertyPath metaPropertyPath) {
        MetaProperty metaProperty = metaPropertyPath.getMetaProperty();
        column.setSortable(true);

        MetaClass propertyMetaClass = metadataTools.getPropertyEnclosingMetaClass(metaPropertyPath);
        column.setHeader(messageTools.getPropertyCaption(propertyMetaClass, metaProperty.getName()));
    }

    protected void onSelectionChange(SelectionEvent<Grid<E>, E> event) {
        for (SelectionListener<Grid<E>, E> listener : selectionListeners) {
            listener.selectionChange(event);
        }
    }

    public void onSelectionModelChange(SelectionModel<Grid<E>, E> selectionModel) {
        if (selectionModel instanceof GridNoneSelectionModel) {
            detachSelectionListener();
            return;
        }

        if (!selectionListeners.isEmpty()) {
            attachSelectionListener();
        }
    }

    protected void attachSelectionListener() {
        detachSelectionListener();

        if (!(getSelectionModel() instanceof GridNoneSelectionModel)) {
            selectionListenerRegistration = getSelectionModel().addSelectionListener(this::onSelectionChange);
        }
    }

    protected void detachSelectionListener() {
        if (selectionListenerRegistration != null) {
            selectionListenerRegistration.remove();
            selectionListenerRegistration = null;
        }
    }

    protected GridSelectionModel<E> getSelectionModel() {
        return component.getSelectionModel();
    }

    protected void onColumnReorderChange(ColumnReorderEvent<E> event) {
        // Grid doesn't know about columns hidden by security permissions,
        // so we need to return them back to their previous positions
        columns = restoreColumnsOrder(event.getColumns());
    }

    /**
     * Inserts columns hidden by security permissions into a list of visible columns in their original positions.
     *
     * @param visibleColumns the list of DataGrid columns, not hidden by security permissions
     * @return a list of all columns in DataGrid
     */
    protected List<Column<E>> restoreColumnsOrder(List<Column<E>> visibleColumns) {
        List<Column<E>> newOrderColumns = new ArrayList<>(visibleColumns);
        for (Column<E> column : columns) {
            if (!newOrderColumns.contains(column)) {
                newOrderColumns.add(columns.indexOf(column), column);
            }
        }
        return newOrderColumns;
    }

    protected List<Column<E>> deleteHiddenColumns(List<Column<E>> allColumns) {
        return allColumns.stream()
                .filter(c -> !propertyColumns.containsKey(c)
                        || isPropertyEnabledBySecurity(propertyColumns.get(c)))
                .collect(Collectors.toList());
    }

    protected void onSort(SortEvent<Grid<E>, GridSortOrder<E>> event) {
        if (!(dataGridItems instanceof DataGridItems.Sortable)
                || !(dataGridItems instanceof EntityDataUnit)) {
            return;
        }

        //noinspection unchecked
        DataGridItems.Sortable<E> dataProvider = (DataGridItems.Sortable<E>) dataGridItems;

        List<GridSortOrder<E>> sortOrders = event.getSortOrder();
        if (sortOrders.isEmpty()) {
            dataProvider.resetSortOrder();
        } else {
            Map<Object, Boolean> sortedColumnMap = new LinkedHashMap<>();

            for (GridSortOrder<E> sortOrder : sortOrders) {
                Column<E> column = sortOrder.getSorted();

                if (column != null) {
                    MetaPropertyPath mpp = propertyColumns.get(column);

                    if (mpp != null) {
                        boolean ascending = SortDirection.ASCENDING.equals(sortOrder.getDirection());
                        sortedColumnMap.put(mpp, ascending);
                    }
                }
            }

            dataProvider.sort(sortedColumnMap.keySet().toArray(), Booleans.toArray(sortedColumnMap.values()));
        }
    }

    protected void notifyDataProviderSelectionChanged(SelectionEvent<Grid<E>, E> ignore) {
        ITEMS items = getItems();

        if (items == null
                || items.getState() == BindingState.INACTIVE) {
            return;
        }

        Set<E> selected = getSelectedItems();
        if (selected.isEmpty()) {
            items.setSelectedItem(null);
        } else {
            E newItem = selected.iterator().next();
            // In some cases, the container may not contain
            // an item that we want to set as the selected
            if (items.containsItem(newItem)) {
                items.setSelectedItem(newItem);
            }
        }
    }

    protected void applySecurityToPropertyColumns() {
        for (Map.Entry<Column<E>, MetaPropertyPath> e : propertyColumns.entrySet()) {
            if (afterColumnSecurityApplyHandler != null) {
                afterColumnSecurityApplyHandler.accept(
                        new ColumnSecurityContext<>(e.getKey(), e.getValue(),
                                isPropertyEnabledBySecurity(e.getValue())));
            }
        }

        updateEmptyState();
    }

    public boolean isPropertyEnabledBySecurity(MetaPropertyPath mpp) {
        EntityAttributeContext context = new EntityAttributeContext(mpp);
        accessManager.applyRegisteredConstraints(context);
        return context.canView();
    }

    public String getEmptyStateText() {
        return emptyStateTextInternal;
    }

    public void setEmptyStateText(String emptyStateText) {
        this.emptyStateTextInternal = emptyStateText;
        this.emptyStateComponentInternal = null;

        updateEmptyState();
    }

    public Component getEmptyStateComponent() {
        return emptyStateComponentInternal;
    }

    public void setEmptyStateComponent(Component emptyStateComponent) {
        this.emptyStateComponentInternal = emptyStateComponent;
        this.emptyStateTextInternal = null;

        updateEmptyState();
    }

    public void setEmptyStateTextDelegate(Consumer<String> emptyStateTextDelegate) {
        this.componentEmptyStateTextDelegate = emptyStateTextDelegate;
    }

    public void setEmptyStateComponentDelegate(Consumer<Component> emptyStateComponentDelegate) {
        this.componentEmptyStateComponentDelegate = emptyStateComponentDelegate;
    }

    protected void updateEmptyState() {
        if (emptyStateByPermissionRegistration != null) {
            emptyStateByPermissionRegistration.remove();
            emptyStateByPermissionRegistration = null;
        }

        if (MapUtils.isNotEmpty(propertyColumns) &&
                !CollectionUtils.containsAny(component.getColumns(), propertyColumns.keySet())) {
            setupEmptyStateByPermissionComponent();
        } else if (emptyStateComponentInternal != null) {
            componentEmptyStateComponentDelegate.accept(emptyStateComponentInternal);
        } else if (emptyStateTextInternal != null) {
            componentEmptyStateTextDelegate.accept(emptyStateTextInternal);
        } else {
            // to remove any empty state component
            componentEmptyStateComponentDelegate.accept(null);
        }
    }

    protected void setupEmptyStateByPermissionComponent() {
        if (component.isAttached()) {
            initEmptyStateByPermissionsComponent();
        } else {
            emptyStateByPermissionRegistration = component.addAttachListener(event -> {
                event.unregisterListener();
                emptyStateByPermissionRegistration = null;

                initEmptyStateByPermissionsComponent();
            });
        }
    }

    protected void initEmptyStateByPermissionsComponent() {
        // TODO: pinyazhin, use fragment from flowui?
        DataGridEmptyStateByPermissionsFragment gridEmptyStateFragment =
                fragments.create(UiComponentUtils.getView(component), DataGridEmptyStateByPermissionsFragment.class);
        componentEmptyStateComponentDelegate.accept(gridEmptyStateFragment);
    }

    public List<Column<E>> getColumns() {
        return List.copyOf(columns);
    }

    @Nullable
    public DataGridColumn<E> getColumnByKey(String key) {
        if (Strings.isNullOrEmpty(key)) {
            return null;
        }
        return (DataGridColumn<E>) columns.stream()
                .filter(c -> key.equals(c.getKey()))
                .findFirst()
                .orElse(null);
    }

    public void removeColumn(Column<E> column) {
        columns.remove(column);

        propertyColumns.remove(column);
        removeAggregationInfo(column);
    }

    public boolean isDataGridOwner(Column<E> column) {
        return column.getGrid().equals(component)
                && column.getElement().getParent() != null;
    }

    public void setColumnPosition(Column<E> column, int index) {
        Preconditions.checkNotNullArgument(column);
        if (index >= columns.size() || index < 0) {
            throw new IndexOutOfBoundsException(String.format("Index '%s' is out of range. Available indexes " +
                    "to move column: from 0 to %s including bounds", index, columns.size() - 1));
        }

        columns.remove(column);
        columns.add(index, column);

        // remove hidden columns by security
        List<Column<E>> newColumnOrder = deleteHiddenColumns(columns);

        component.setColumnOrder(newColumnOrder);
    }

    @SuppressWarnings("unchecked")
    protected AggregatableDelegate<Object> getAggregatableDelegate() {
        if (aggregatableDelegate == null) {
            aggregatableDelegate = applicationContext.getBean(AggregatableDelegate.class);
        }

        if (getItems() != null) {
            aggregatableDelegate.setItemProvider(getItems()::getItem);
            aggregatableDelegate.setItemValueProvider(getItems()::getItemValue);
        }
        return aggregatableDelegate;
    }

    @Nullable
    public Consumer<ColumnSecurityContext<E>> getAfterColumnSecurityApplyHandler() {
        return afterColumnSecurityApplyHandler;
    }

    public void setAfterColumnSecurityApplyHandler(
            @Nullable Consumer<ColumnSecurityContext<E>> afterColumnSecurityApplyHandler) {
        this.afterColumnSecurityApplyHandler = afterColumnSecurityApplyHandler;
    }

    public GroupDataGridEditorImpl<E> createEditor() {
        GroupDataGridEditorImpl<E> editor = new GroupDataGridEditorImpl<>(component, applicationContext);
        editor.addCloseListener(this::onGridEditorClose);
        return editor;
    }

    protected void onItemDoubleClick(ItemDoubleClickEvent<E> itemDoubleClickEvent) {
        if (dataGridItems.hasChildren(itemDoubleClickEvent.getItem())) {
            // Do not handle double click for group items.
            return;
        }

        if (itemDoubleClickListeners.isEmpty()) {
            handleDoubleClickAction(itemDoubleClickEvent.getItem());
        } else {
            fireItemDoubleClick(itemDoubleClickEvent);
        }
    }

    protected void fireItemDoubleClick(ItemDoubleClickEvent<E> itemDoubleClickEvent) {
        for (ComponentEventListener<ItemDoubleClickEvent<E>> listener : itemDoubleClickListeners) {
            listener.onComponentEvent(itemDoubleClickEvent);
        }
    }

    protected void handleEnterPress() {
        handleDoubleClickAction(null);
    }

    protected void handleDoubleClickAction(@Nullable E item) {
        if (component.getEditor().isOpen()) {
            return;
        }

        if (item != null) {
            // have to select clicked item to make action work, otherwise
            // consecutive clicks on the same item deselect it
            // selection from client is mandatory due to programmatic selection ignores selectableProvider
            component.getSelectionModel().selectFromClient(item);
        }

        if (enterPressHandler != null) {
            enterPressHandler.accept(new SupportsEnterPress.EnterPressEvent<>(component));
            return;
        }

        Action action = findEnterAction();
        if (action == null) {
            action = component.getAction(EditAction.ID);
            if (action == null) {
                action = component.getAction(ReadAction.ID);
            }
        }

        if (action != null && action.isEnabled()) {
            action.actionPerform(component);
        }
    }

    @Nullable
    protected Action findEnterAction() {
        for (Action action : component.getActions()) {
            KeyCombination keyCombination = action.getShortcutCombination();
            if (keyCombination != null) {
                if ((keyCombination.getKeyModifiers() == null || keyCombination.getKeyModifiers().length == 0)
                        && keyCombination.getKey() == Key.ENTER) {
                    return action;
                }
            }
        }

        return null;
    }

    protected void onGridEditorClose(EditorCloseEvent<E> eEditorCloseEvent) {
        updateAggregationRow();
    }

    public static class ColumnSecurityContext<E> {

        protected Column<E> column;
        protected MetaPropertyPath metaPropertyPath;
        protected Boolean propertyEnabled;

        public ColumnSecurityContext(Column<E> column,
                                     MetaPropertyPath metaPropertyPath,
                                     Boolean propertyEnabled) {
            this.column = column;
            this.metaPropertyPath = metaPropertyPath;
            this.propertyEnabled = propertyEnabled;
        }

        public DataGridColumn<E> getColumn() {
            return (DataGridColumn<E>) column;
        }

        public MetaPropertyPath getMetaPropertyPath() {
            return metaPropertyPath;
        }

        public Boolean isPropertyEnabled() {
            return propertyEnabled;
        }
    }
}
