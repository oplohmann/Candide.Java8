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

import org.objectscape.candide.stm.MapEvent;
import org.objectscape.candide.util.immutable.ImmutableList;

public class MapValuesEvent<V> extends MapEvent<V> {

    /**
     * Values that were involved in the <code>MapEvent</code>, such as added or removed elements
     */
    protected ImmutableList<V> values = null;

    /**
     * Creates a new <code>MapEvent</code> object
     */
    public MapValuesEvent() {
        super();
    }

    /**
     * Creates a new <code>MapEvent</code> object
     *
     * @param mapName name of the map that signaled the event
     * @param key key of the values that changed as a result of the map change
     */
    public MapValuesEvent(String mapName, Object key, int nextInvocationCount) {
        super(mapName, key, nextInvocationCount);
    }

    /**
     * Creates a new <code>MapEvent</code> object
     *
     * @param mapName name of the map that signaled the event
     * @param key key of the values that changed as a result of the map change
     */
    public MapValuesEvent(String mapName, Object key, ImmutableList<V> values, int nextInvocationCount) {
        super(mapName, key, nextInvocationCount);
        this.values = values;
    }

    /**
     * Return the values that were involved in the <code>MapEvent</code>, such as added or removed elements
     *
     * @return
     */
    public ImmutableList<V> getValues() {
        return values;
    }

    /**
     * Return a singlevalue element in case the values of the key that signaled the event contains a singlevalue
     * element only. Throws an <code>IllegalStateException</code> in case the values collection contains
     * more than one element.
     *
     * @return the singlevalue element in the values collection or null if it is empty
     */
    public V getValue() {
        if(values == null || values.isEmpty())
            return null;
        if(values.size() > 1)
            throw new IllegalStateException("values contain " + values.size() + " elements");
        return values.get(0);
    }

}
