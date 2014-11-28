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

package org.objectscape.candide.stm;


public class ListenableAtomicIntegerValue extends ListenableAtomicValue<Integer>
{
    public ListenableAtomicIntegerValue() {
        super(0);
    }

    public ListenableAtomicIntegerValue(Integer immutableValue) {
        super(immutableValue);
    }

    public ListenableAtomicIntegerValue(String valueName, Integer immutableValue) {
        super(valueName, immutableValue);
    }

    public Integer incrementAndGet() {
        return setAndGet((Integer value) -> value + 1);
    }

    public Integer getAndIncrement() {
        return getAndSet((Integer value) -> value + 1);
    }

    public Integer decrementAndGet() {
        return setAndGet((Integer value) -> value - 1);
    }

    public Integer getAndDecrement() {
        return getAndSet((Integer value) -> value - 1);
    }

    public Integer addAndGet(int delta) {
        return setAndGet((Integer value) -> value + delta);
    }

    public Integer getAndAdd(int delta) {
        return getAndSet((Integer value) -> value + delta);
    }

}
