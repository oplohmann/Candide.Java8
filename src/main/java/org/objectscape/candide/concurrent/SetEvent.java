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
package org.objectscape.candide.concurrent;


import org.objectscape.candide.common.ValueHolderEvent;

public class SetEvent<V> extends ValueHolderEvent<V> {

    private final V previousValue;

    private SetEvent(SetEvent<V> event) {
        super(event);
        previousValue = null;
    }

    public SetEvent(String valueHolderName, V previousValue, V value) {
        super(valueHolderName, value, -1);
        this.previousValue = previousValue;
    }

    public SetEvent(String valueHolderName, V previousValue, V value, int invocationCount) {
        super(valueHolderName, value, invocationCount);
        this.previousValue = previousValue;
    }

    public SetEvent(SetEvent<V> event, int invocationCount) {
        super(event, invocationCount);
        this.previousValue = event.getPreviousValue();
    }

    public SetEvent(SetEvent<V> event, V previousValue) {
        super(event, -1);
        this.previousValue = previousValue;
    }

    public SetEvent(SetEvent<V> event, V previousValue, int invocationCount) {
        super(event, invocationCount);
        this.previousValue = previousValue;
    }

    public V getPreviousValue() {
        return previousValue;
    }
}
