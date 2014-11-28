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


import java.util.function.Consumer;

/**
 * Convenience interface to reduce the number of nested type parameters that have to be typed in.
 *
 * @see org.objectscape.candide.concurrent.ListenableConcurrentHashMap
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 */
public interface PutListener<V> extends Consumer<PutEvent<V>> {

}
