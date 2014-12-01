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

public class ListenableConcurrentIntegerValue extends ListenableConcurrentValue<Integer> {

    public ListenableConcurrentIntegerValue() {
        super(0);
    }

    public ListenableConcurrentIntegerValue(String name) {
        super(name, 0);
    }

    public ListenableConcurrentIntegerValue(String name, Integer value) {
        super(name, value);
    }

    public Integer incrementAndGet() {
        long stamp = lock.writeLock();
        try {
            Integer previousValue = value;
            this.value = previousValue + 1;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return value;
        }
        finally {
            lock.unlockWrite(stamp);
        }
    }


    public Integer getAndIncrement() {
        long stamp = lock.writeLock();
        try {
            Integer previousValue = value;
            this.value = previousValue + 1;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return previousValue;
        }
        finally {
            lock.unlockWrite(stamp);
        }
    }

    public Integer decrementAndGet() {
        long stamp = lock.writeLock();
        try {
            Integer previousValue = value;
            this.value = previousValue - 1;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return value;
        }
        finally {
            lock.unlockWrite(stamp);
        }
    }


    public Integer getAndDecrement() {
        long stamp = lock.writeLock();
        try {
            Integer previousValue = value;
            this.value = previousValue - 1;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return previousValue;
        }
        finally {
            lock.unlockWrite(stamp);
        }
    }

    public Integer addAndGet(int delta) {
        long stamp = lock.writeLock();
        try {
            Integer previousValue = value;
            this.value = previousValue + delta;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return value;
        }
        finally {
            lock.unlockWrite(stamp);
        }
    }

    public Integer getAndAdd(int delta) {
        long stamp = lock.writeLock();
        try {
            Integer previousValue = value;
            this.value = previousValue + delta;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return value;
        }
        finally {
            lock.unlockWrite(stamp);
        }
    }
}
