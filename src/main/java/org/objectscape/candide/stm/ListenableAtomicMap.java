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

import org.objectscape.candide.concurrent.ListenerValue;
import org.objectscape.candide.util.CallerMustSynchronize;
import org.objectscape.candide.util.scalastm.AtomicUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static scala.concurrent.stm.japi.STM.afterCommit;

/**
 * Class still under development.
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 */
public class ListenableAtomicMap<K, V> implements Map<K, V>, AtomicUtils {

    private Map<K, V> stmMap = newMap();

    private String mapName = null;

    private Map<K, Map<PutListener<V>, ListenerValue>> putListeners = newMap();
    private Map<K, Map<RemoveListener<V>, ListenerValue>> removeListeners = newMap();
    private Map<K, Map<SendListener<V>, ListenerValue>> sendListeners = newMap();

    public ListenableAtomicMap() {
    }

    public ListenableAtomicMap(String mapName) {
        this.mapName = mapName;
    }

    public int size() {
        return stmMap.size();
    }

    @Override
    public boolean isEmpty() {
        return stmMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return stmMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return stmMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return stmMap.get(key);
    }

    @Override
    public V put(K key, V value) {
        V previousValue = stmMap.put(key, value);
        afterCommit(() -> notifyPutListeners(key, value));
        return previousValue;
    }

    @Override
    public V remove(Object key) {
        V value = stmMap.remove(key);
        afterCommit(() -> notifyRemoveListeners(key, value));
        return value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        stmMap.putAll(m);
        afterCommit(() -> {
            for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
                notifyPutListeners(entry.getKey(), entry.getValue());
            }
        });
    }

    @Override
    public void clear() {
        Set<Entry<K, V>> entries = new HashSet<>(stmMap.entrySet());
        stmMap.clear();
        afterCommit(() -> {
            for(Entry<K, V> entry : entries) {
                notifyRemoveListeners(entry.getKey(), entry.getValue());
            }
        });
    }

    @Override
    public Set<K> keySet() {
        return stmMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return stmMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return stmMap.entrySet();
    }

    public int send(K key) {
        Map<SendListener<V>, ListenerValue> listeners = sendListeners.get(key);
        if(listeners == null)
            return 0;
        notifySendListeners(key, stmMap.get(key));
        return listeners.size();
    }

    public void addListener(K key, PutListener<V> listener) {
        addListener(key, listener, false);
    }

    public void addListener(K key, PutListener<V> listener, boolean notifyWhenKeyPresent)
    {
        Map<PutListener<V>, ListenerValue> listeners = putListeners.get(key);
        if (listeners == null) {
            listeners = newMap();
            putListeners.put(key, listeners);
        }
        listeners.put(listener, new ListenerValue());
        if (!notifyWhenKeyPresent)
            return;
        V value = stmMap.get(key);
        if(value == null)
            return;
        notifyPutListeners(key, value);
    }

    @CallerMustSynchronize
    protected void notifyRemoveListeners(Object key, V value)
    {
        Map<RemoveListener<V>, ListenerValue> listeners = removeListeners.get(key);
        if (listeners == null)
            return;

        for(Entry<RemoveListener<V>, ListenerValue> entry : listeners.entrySet()) {
            ListenerValue listenerValue = entry.getValue();
            RemoveListener<V> listener = entry.getKey();
            listener.accept(new RemoveEvent<>(mapName, key, value, listenerValue.nextInvocationCount()));
        }
    }

    @CallerMustSynchronize
    protected void notifySendListeners(K key, V value)
    {
        Map<SendListener<V>, ListenerValue> listeners = sendListeners.get(key);
        if(listeners == null)
            return;

        for(Entry<SendListener<V>, ListenerValue> entry : listeners.entrySet()) {
            ListenerValue listenerValue = entry.getValue();
            SendListener<V> listener = entry.getKey();
            listener.accept(new SendEvent<>(mapName, key, value, listenerValue.nextInvocationCount()));
        }
    }

    @CallerMustSynchronize
    protected void notifyPutListeners(K key, V value)
    {
        Map<PutListener<V>, ListenerValue> listeners = putListeners.get(key);
        if(listeners == null)
            return;

        for(Entry<PutListener<V>, ListenerValue> entry : listeners.entrySet()) {
            ListenerValue listenerValue = entry.getValue();
            PutListener<V> listener = entry.getKey();
            listener.accept(new PutEvent<>(mapName, key, value, listenerValue.nextInvocationCount()));
        }
    }

    public void addListener(K key, RemoveListener<V> listener) {
        Map<RemoveListener<V>, ListenerValue> listeners = removeListeners.get(key);
        if (listeners == null) {
            listeners = newMap();
            removeListeners.put(key, listeners);
        }
        listeners.put(listener, new ListenerValue());
    }

    public void addListener(K key, SendListener<V> listener) {
        addListener(key, listener, false);
    }

    public void addListener(K key, SendListener<V> listener, boolean notifyWhenKeyPresent) {
        Map<SendListener<V>, ListenerValue> listeners = sendListeners.get(key);
        if (listeners == null) {
            listeners = newMap();
            sendListeners.put(key, listeners);
        }
        listeners.put(listener, new ListenerValue());
        if(!notifyWhenKeyPresent)
            return;
        notifySendListeners(key, stmMap.get(key));
    }

    public boolean removeListener(K key, RemoveListener<V> listener) {
        Map<RemoveListener<V>, ListenerValue> listeners = removeListeners.get(key);
        if(listeners == null)
            return false;

        boolean found = listeners.remove(listener) != null;
        if(listeners.isEmpty()) {
            listeners.remove(key);
        }

        return found;
    }

    public boolean removeListener(K key, PutListener<V> listener) {
        Map<PutListener<V>, ListenerValue> listeners = putListeners.get(key);
        if(listeners == null)
            return false;

        boolean found = listeners.remove(listener) != null;
        if(listeners.isEmpty()) {
            putListeners.remove(key);
        }

        return found;
    }

    public boolean removeListener(K key, SendListener<V> listener)
    {
        Map<SendListener<V>, ListenerValue> listeners = sendListeners.get(key);
        if(listeners == null)
            return false;

        boolean found = listeners.remove(listener) != null;
        if(listeners.isEmpty()) {
            sendListeners.remove(key);
        }

        return found;
    }
}
