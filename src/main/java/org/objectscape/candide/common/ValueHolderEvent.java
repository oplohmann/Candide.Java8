/**
 * Copyright (c) 2013 Oliver Plohmann
 * http://www.objectscape.org/candide
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.objectscape.candide.common;

public class ValueHolderEvent<V> {

    protected final String name;
    protected final V value;
    protected final int invocationCount;

    public ValueHolderEvent(ValueHolderEvent<V> event) {
        this.name = event.getName();
        this.value = event.getValue();
        this.invocationCount = event.getInvocationCount();
    }

    public ValueHolderEvent(String name, V value, int invocationCount) {
        this.name = name;
        this.value = value;
        this.invocationCount = invocationCount;
    }

    public ValueHolderEvent(ValueHolderEvent<V> event, int invocationCount) {
        this.name = event.getName();
        this.value = event.getValue();
        this.invocationCount = invocationCount;
    }

    public String getName() {
        return name;
    }

    public V getValue() {
        return value;
    }

    public int getInvocationCount() {
        return invocationCount;
    }

}
