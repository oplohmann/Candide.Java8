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

import org.objectscape.candide.common.SendEvent;
import org.objectscape.candide.common.SendListener;
import org.objectscape.candide.util.CallerMustSynchronize;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

public class ListenableConcurrentValue<V> {

    protected String name = null;
    protected V value = null;
    protected StampedLock lock = new StampedLock();

    protected Map<SetListener<V>, ListenerValue> setListeners = new HashMap<>();
    protected Map<SendListener<V>, ListenerValue> sendListeners = new HashMap<>();

    public ListenableConcurrentValue() {
        super();
    }

    public ListenableConcurrentValue(String name) {
        super();
        this.name = name;
    }

    public ListenableConcurrentValue(V value) {
        super();
        this.value = value;
    }

    public ListenableConcurrentValue(String name, V value) {
        super();
        this.name = name;
        this.value = value;
    }

    public boolean set(V expectedValue, V newValue)
    {
        long stamp = lock.writeLock();
        try {
            if(value == null && expectedValue != null)
                return false;
            if(!value.equals(expectedValue))
                return false;
            V previousValue = value;
            this.value = newValue;
            notifySetListeners(previousValue);
            return true;
        }
        finally {
            lock.unlockWrite(stamp);
        }
    }

    public boolean set(V expectedValue, Function<V, V> function)
    {
        long stamp = lock.writeLock();
        try {
            if(value == null && expectedValue != null)
                return false;
            if(!value.equals(expectedValue))
                return false;
            V previousValue = value;
            this.value = function.apply(value);
            notifySetListeners(previousValue);
            return true;
        }
        finally {
            lock.unlockWrite(stamp);
        }
    }

    public V set(Function<V, V> function)
    {
        long stamp = lock.writeLock();
        try {
            V previousValue = value;
            this.value = function.apply(value);
            notifySetListeners(previousValue);
            return value;
        }
        finally {
            lock.unlockWrite(stamp);
        }
    }

    public V get()
    {
        long stamp = lock.readLock();
        try {
            return value;
        }
        finally {
            lock.unlockRead(stamp);
        }
    }

    @CallerMustSynchronize
    protected void notifySetListeners(V previousValue) {
        if(previousValue != null && previousValue.equals(value))
            return;
        else if(previousValue == value)
            return;
        notifySetListeners(new SetEvent<>(name, previousValue, value));
    }

    @CallerMustSynchronize
    protected void notifySendListeners(SendEvent<V> event) {
        for(Map.Entry<SendListener<V>, ListenerValue> entry : sendListeners.entrySet()) {
            ListenerValue value = entry.getValue();
            SendListener<V> listener = entry.getKey();
            listener.accept(new SendEvent<>(event, value.nextInvocationCount()));
        }
    }

    @CallerMustSynchronize
    protected void notifySetListeners(SetEvent<V> event) {
        for(Map.Entry<SetListener<V>, ListenerValue> entry : setListeners.entrySet()) {
            ListenerValue value = entry.getValue();
            SetListener<V> listener = entry.getKey();
            listener.accept(new SetEvent<>(event, value.nextInvocationCount()));
        }
    }

    @CallerMustSynchronize
    protected void notifySendListeners() {
        notifySendListeners(new SendEvent<>(name, value));
    }

    public V send()
    {
        long stamp = lock.readLock();
        try {
            notifySendListeners();
            return value;
        }
        finally {
            lock.unlockRead(stamp);
        }
    }

    public void addListener(SetListener<V> listener)
    {
        long stamp = lock.writeLock();

        try
        {
            setListeners.put(listener, new ListenerValue());
        }
        finally
        {
            lock.unlockWrite(stamp);
        }
    }

    public void addListener(SendListener<V> listener)
    {
        long stamp = lock.writeLock();

        try
        {
            sendListeners.put(listener, new ListenerValue());
        }
        finally
        {
            lock.unlockWrite(stamp);
        }
    }

    public boolean removeListener(SetListener<V> listener)
    {
        long stamp = lock.writeLock();

        try
        {
            return setListeners.remove(listener) != null;
        }
        finally
        {
            lock.unlockWrite(stamp);
        }
    }

    public boolean removeListener(SendListener<V> listener)
    {
        long stamp = lock.writeLock();

        try
        {
            return sendListeners.remove(listener) != null;
        }
        finally
        {
            lock.unlockWrite(stamp);
        }
    }

}
