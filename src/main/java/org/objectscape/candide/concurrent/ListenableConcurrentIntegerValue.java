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
        lock.writeLock().lock();
        try {
            Integer previousValue = value;
            this.value = previousValue + 1;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return value;
        }
        finally {
            lock.writeLock().unlock();
        }
    }


    public Integer getAndIncrement() {
        lock.writeLock().lock();
        try {
            Integer previousValue = value;
            this.value = previousValue + 1;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return previousValue;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public Integer decrementAndGet() {
        lock.writeLock().lock();
        try {
            Integer previousValue = value;
            this.value = previousValue - 1;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return value;
        }
        finally {
            lock.writeLock().unlock();
        }
    }


    public Integer getAndDecrement() {
        lock.writeLock().lock();
        try {
            Integer previousValue = value;
            this.value = previousValue - 1;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return previousValue;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public Integer addAndGet(int delta) {
        lock.writeLock().lock();
        try {
            Integer previousValue = value;
            this.value = previousValue + delta;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return value;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public Integer getAndAdd(int delta) {
        lock.writeLock().lock();
        try {
            Integer previousValue = value;
            this.value = previousValue + delta;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return value;
        }
        finally {
            lock.writeLock().unlock();
        }
    }
}
