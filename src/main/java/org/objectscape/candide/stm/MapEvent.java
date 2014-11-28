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
 * Abstract superclass for all MapEvent classes that are passed on as a callback parameter
 * to events registered with a ListenableConcurrentMap.
 *
 * @see org.objectscape.candide.concurrent.ListenableConcurrentMap
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public abstract class MapEvent<V>
{
    /**
     * Name of the map the <code>MapEvent</code> refers to.
     */
    protected String mapName = null;

    /**
     * Key the <code>MapEvent</code> refers to.
     */
    protected Object key = null;

    protected int runningInvocationCount = -1;

    /**
     * Creates a new <code>MapEvent</code> object
     */
	public MapEvent() {
		super();
	}

    /**
     * Creates a new <code>MapEvent</code> object
     *
     * @param mapName name of the map that signaled the event
     * @param key key of the values that changed as a result of the map change
     */
	public MapEvent(String mapName, Object key, int nextInvocationCount) {
		super();
		this.mapName = mapName;
		this.key = key;
        this.runningInvocationCount = nextInvocationCount;
	}

    /**
     * Return the name of the map the <code>MapEvent</code> object
     *
     * @return the name of the map
     */
	public String getMapName()
	{
		return mapName;
	}

    /**
     * Return the key of values that were changed causing a <code>MapEvent</code> to be signaled.
     *
     * @return
     */
	public Object getKey()
	{
		return key;
	}

    public int getInvocationCount() {
        return runningInvocationCount;
    }

}
