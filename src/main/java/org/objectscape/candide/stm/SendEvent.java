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

/**
 * Defines a callback event that is passed on to the callback lambda in case
 * the <tt>send(Key)</tt> method was called on a <code>ListenableConcurrentMap</code>
 *
 * @see org.objectscape.candide.concurrent.ListenableConcurrentHashMap
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 */
public class SendEvent<V> extends MapValueEvent<V>
{

    /**
     * Creates a new <code>SendEvent</code> object
     */
	public SendEvent() {
		super();
	}

    /**
     * Creates a new <code>SendEvent</code> object
     *
     * @param mapName name of the map the event refers to
     * @param key of the values that were changed
     */
	public SendEvent(String mapName, Object key) {
		super(mapName, key);
	}

    public SendEvent(String mapName, Object key, V value, int nextInvocationCount) {
        super(mapName, key, value, nextInvocationCount);
    }
}
