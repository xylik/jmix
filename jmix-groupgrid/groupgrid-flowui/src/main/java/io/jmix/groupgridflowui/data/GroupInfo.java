/*
 * Copyright 2019 Haulmont.
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
package io.jmix.groupgridflowui.data;

import org.apache.commons.collections4.map.LinkedMap;

/**
 * Class contains information about the current cell value and cell value of parent group columns.
 */
// TODO: rp make interface?
public class GroupInfo {

    private LinkedMap<GroupProperty, Object> groupingValues;
    private GroupProperty groupProperty;

    public GroupInfo(LinkedMap<GroupProperty, Object> groupingValues) {
        this.groupingValues = new LinkedMap<>(groupingValues);
        this.groupProperty = groupingValues.get(groupingValues.size() - 1);
    }

    public Object getPropertyValue(GroupProperty propertyPath) {
        if (!groupingValues.containsKey(propertyPath)) {
            throw new IllegalArgumentException();
        }
        return groupingValues.get(propertyPath);
    }

    public GroupProperty getProperty() {
        return groupProperty;
    }

    public Object getValue() {
        if (groupProperty == null) {
            throw new IllegalStateException();
        }
        return getPropertyValue(groupProperty);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (int groupIndex = 0; groupIndex < groupingValues.size(); groupIndex++) {
            Object value = groupingValues.getValue(groupIndex);
            sb.append("[")
                    .append(groupingValues.get(groupIndex))
                    .append(":")
                    .append(value != null
                            ? value.toString() : "")
                    .append("]")
                    .append(",");
        }
        sb.deleteCharAt(sb.length() - 1).append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupInfo groupInfo = (GroupInfo) o;

        return toString().equals(groupInfo.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}