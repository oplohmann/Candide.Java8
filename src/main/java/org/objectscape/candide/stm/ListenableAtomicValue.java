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

import org.objectscape.candide.common.SendEvent;
import org.objectscape.candide.common.SendListener;
import org.objectscape.candide.concurrent.ListenerValue;
import org.objectscape.candide.concurrent.SetEvent;
import org.objectscape.candide.concurrent.SetListener;
import org.objectscape.candide.util.scalastm.AtomicUtils;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static scala.concurrent.stm.japi.STM.afterCommit;

public class ListenableAtomicValue<V> implements AtomicUtils {

    protected String name = null;
    protected V immutableValue = null;

    protected Map<SetListener<V>, ListenerValue> setListeners = newMap();
    protected Map<SendListener<V>, ListenerValue> sendListeners = newMap();


    protected ListenableAtomicValue() {
        super();
    }

    public ListenableAtomicValue(V immutableValue) {
        super();
        this.immutableValue = immutableValue;
    }

    public ListenableAtomicValue(String name, V immutableValue) {
        super();
        this.name = name;
        this.immutableValue = immutableValue;
    }

    public V setAndGet(Function<V, V> function) {
        return atomic(() -> {
            V previousValue = immutableValue;
            immutableValue = function.apply(immutableValue);
            notifySetListeners(previousValue);
            return immutableValue;
        });
    }

    public V getAndSet(Function<V, V> function) {
        return atomic(() -> {
            V previousValue = immutableValue;
            immutableValue = function.apply(immutableValue);
            notifySetListeners(previousValue);
            return previousValue;
        });
    }

    public V send() {
        return atomic(() -> {
            notifySendListeners();
            return immutableValue;
        });
    }

    private void notifySetListeners(V previousValue) {
        afterCommit(() -> {
            for(Map.Entry<SetListener<V>, ListenerValue> entry : setListeners.entrySet()) {
                ListenerValue value = entry.getValue();
                SetListener<V> listener = entry.getKey();
                listener.accept(new SetEvent<>(name, previousValue, immutableValue, value.nextInvocationCount()));
            }
        });
    }

    private void notifySendListeners()
    {
        afterCommit(() -> {
            for(Map.Entry<SendListener<V>, ListenerValue> entry : sendListeners.entrySet()) {
                ListenerValue value = entry.getValue();
                SendListener<V> listener = entry.getKey();
                listener.accept(new SendEvent<>(name, immutableValue, value.nextInvocationCount()));
            }
        });
    }

    public V get() {
        return atomic(() -> immutableValue);
    }

    public void addListener(SetListener<V> listener)
    {
        atomic(() -> {
            setListeners.put(listener, new ListenerValue());
        });
    }

    public void addListener(SendListener<V> listener)
    {
        atomic(() -> {
            sendListeners.put(listener, new ListenerValue());
        });
    }

    public boolean removeListener(SetListener<V> listener)
    {
        Callable<Boolean> callable = () -> setListeners.remove(listener) != null;
        return atomic( callable );
    }

    public boolean removeListener(SendListener<V> listener)
    {
        Callable<Boolean> callable = () -> sendListeners.remove(listener) != null;
        return atomic(callable);
    }

}
