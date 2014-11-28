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

public class SendEvent<V> extends ValueHolderEvent<V>
{
    public SendEvent(String valueHolderName, V value) {
        super(valueHolderName, value, -1);
    }

    public SendEvent(String valueHolderName, V value, int nextRunningInvocationCount) {
        super(valueHolderName, value, nextRunningInvocationCount);
    }

    public SendEvent(SendEvent<V> event, int nextRunningInvocationCount) {
        super(event.getName(), event.getValue(), nextRunningInvocationCount);
    }
}
