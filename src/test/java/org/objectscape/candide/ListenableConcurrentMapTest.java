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
package org.objectscape.candide;

import junit.framework.Assert;
import org.junit.Test;
import org.objectscape.candide.concurrent.*;
import org.objectscape.candide.util.ImmutableList;
import org.objectscape.candide.util.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class ListenableConcurrentMapTest extends AbstractTest implements ListUtils
{

	private static final Random RANDOM = new Random(System.currentTimeMillis());
	
	@Test
	public void putSingleValue() 
	{
		ListenableConcurrentMap<String, Long> map = new ListenableConcurrentHashMap<>("myMap");
		
		String key = "12";
		Assert.assertEquals(0, map.size());
		ImmutableList<Long> returnedValues = map.putSingleValue(key, 1L);
		Assert.assertNull(returnedValues);
		returnedValues = map.putSingleValue(key, 2L);
		Assert.assertEquals(1, returnedValues.size());
		Assert.assertEquals(Long.valueOf(1), returnedValues.get(0));
	}

	@Test
	public void putList()
	{
		ListenableConcurrentMap<String, Long> map = new ListenableConcurrentHashMap<>("myMap");

		String key = "12";
		Assert.assertEquals(0, map.size());
		List<Long> values = new ArrayList<>();
		values.add(1L);
		ImmutableList<Long> returnedValues = map.put(key, immutable(values));
		Assert.assertNull(returnedValues);
		values = new ArrayList<>();
		values.add(2L);
		returnedValues = map.put(key, immutable(values));
		Assert.assertEquals(1, returnedValues.size());
		Assert.assertEquals(Long.valueOf(1), returnedValues.get(0));
	}

	@Test
	public void putGetRemove()
	{
		ListenableConcurrentMap<Long, String> map = new ListenableConcurrentHashMap<>("myMap");

		for(int i = 0; i < 100000; i++)
		{
			Assert.assertEquals(0, map.size());
			long key = RANDOM.nextLong();
			String value = String.valueOf(RANDOM.nextLong());
			List<String> values = new ArrayList<>();
			values.add(value);
			ImmutableList<String> returnedValues = map.putSingleValue(key, value);
			Assert.assertEquals(1, map.size());
			Assert.assertNull(returnedValues);
			returnedValues = map.get(key);
			Assert.assertEquals(values, returnedValues.mutableList());
			returnedValues = map.remove(key);
			Assert.assertEquals(0, map.size());
			Assert.assertEquals(values, returnedValues.mutableList());
		}
	}

	@Test
	public void remove()
	{
		ListenableConcurrentMap<Long, String> map = new ListenableConcurrentHashMap<>("myMap");

		Assert.assertEquals(0, map.size());
		long key = 123L;
		String value = "123";
		List<String> values = new ArrayList<>();
		values.add(value);
		ImmutableList<String> returnedValues = map.putSingleValue(key, value);
		Assert.assertEquals(1, map.size());
		Assert.assertNull(returnedValues);
		returnedValues = map.get(key);
		Assert.assertEquals(values, returnedValues.mutableList());
		boolean removed = map.remove(key, "789");
		Assert.assertEquals(1, map.size());
		Assert.assertFalse(removed);
		removed = map.remove(key, value);
		Assert.assertTrue(removed);
		Assert.assertEquals(1, map.size());
		returnedValues = map.get(key);
		Assert.assertEquals(0, returnedValues.size());
	}

	@Test
	public void replace()
	{
		ListenableConcurrentMap<Long, String> map = new ListenableConcurrentHashMap<>("myMap");

		Long key = 123L;
		Long otherKey = 234L;
		String value = "123";
		String otherValue = "234";

		// testing replace(K key, V common)

		Assert.assertEquals(0, map.size());
		ImmutableList<String> returnedValues = map.replaceSingleValue(key, value);
		Assert.assertNull(returnedValues);

		returnedValues = map.putSingleValue(key, value);
		Assert.assertNull(returnedValues);
		returnedValues = map.get(key);
		Assert.assertEquals(1, returnedValues.size());
		Assert.assertEquals(value, returnedValues.get(0));

		// testing replace(K key, List<V> values)

		map.clear();
		List<String> values = new ArrayList<>();
		values.add(value);
		returnedValues = map.replace(key, returnedValues);
		Assert.assertNull(returnedValues);
		map.putSingleValue(key, value);
		List<String> otherValues = new ArrayList<>();
		otherValues.add(otherValue);
        ImmutableList<String> immutableOtherValues = immutable(otherValues);
		returnedValues = map.replace(otherKey, immutableOtherValues);
		Assert.assertNull(returnedValues);
		returnedValues = map.replace(key, immutableOtherValues);
		Assert.assertEquals(returnedValues, values);

		// testing replace(K key, List<V> oldValues, List<V> newValues)

		map.clear();
        ImmutableList<String> immutableValues = immutableList(value);
        ImmutableList<String> otherImmutableValues = immutableList(otherValue);
		boolean replaced = map.replace(key, immutableValues, otherImmutableValues);
		Assert.assertFalse(replaced);
		map.put(key, immutableValues);
		replaced = map.replace(otherKey, immutableValues, otherImmutableValues);
		Assert.assertFalse(replaced);
		replaced = map.replace(key, otherImmutableValues, otherImmutableValues);
		Assert.assertFalse(replaced);
		replaced = map.replace(key, immutableValues, otherImmutableValues);
		Assert.assertTrue(replaced);
	}

	@Test
	public void clear()
	{
		ListenableConcurrentMap<String, Long> map = new ListenableConcurrentHashMap<>("myMap");

		String key = "12";
		Assert.assertEquals(0, map.size());
		map.putSingleValue(key, 1L);
		map.putSingleValue(key, 2L);
		Assert.assertEquals(1, map.size());
		map.putSingleValue("3", 3L);
		Assert.assertEquals(2, map.size());
		map.clear();
		Assert.assertEquals(0, map.size());
	}

	@Test
	public void putIfAbsentOrIfEmpty()
	{
		ListenableConcurrentMap<String, Long> map = new ListenableConcurrentHashMap<>("myMap");

		String key = "12";
		Assert.assertEquals(0, map.size());
		ImmutableList<Long> returnedValues = map.putIfAbsentOrIfEmpty(key, 1L);
		Assert.assertNull(returnedValues);
		returnedValues = map.putIfAbsentOrIfEmpty(key, 2L);
		Assert.assertEquals(1, returnedValues.size());
		Assert.assertEquals(Long.valueOf(1), returnedValues.get(0));

		map.clear();
		Assert.assertEquals(0, map.size());
		map.putSingleValue(key, 1L);
		map.putSingleValue(key, 2L);
		returnedValues = map.putIfAbsentOrIfEmpty(key, 3L);
		Assert.assertEquals(1, returnedValues.size());
		Assert.assertEquals(Long.valueOf(2), returnedValues.get(0));
	}

	@Test
	public void putIfAbsent()
	{
		ListenableConcurrentMap<String, Long> map = new ListenableConcurrentHashMap<>("myMap");

		String key = "12";
		Assert.assertEquals(0, map.size());
		ImmutableList<Long> returnedValues = map.putIfAbsentSingleValue(key, 1L);
		Assert.assertNull(returnedValues);
		returnedValues = map.putIfAbsentSingleValue(key, 2L);
		Assert.assertEquals(1, returnedValues.size());
		Assert.assertEquals(Long.valueOf(1), returnedValues.get(0));

		map.clear();
		Assert.assertEquals(0, map.size());
		ImmutableList<Long> values = immutableList(1L);
		returnedValues = map.putIfAbsent(key, values);
		Assert.assertNull(returnedValues);
		values = immutableList(2L);
		returnedValues = map.putIfAbsent(key, values);
		Assert.assertEquals(1, returnedValues.size());
		Assert.assertEquals(Long.valueOf(1), returnedValues.get(0));
	}
	
	@Test
	public void send() throws InterruptedException 
	{
		final String mapName = "myMap";
		ListenableConcurrentMap<String, Long> map = new ListenableConcurrentHashMap<>(mapName);
		
		final CountDownLatch latch = new CountDownLatch(1);
		final String key = "12";
		
		SendListener<Long> listener = (SendEvent<Long> event) ->
		{
			System.out.println("SendListener invoked with SendEvent{mapName=\"" + event.getMapName() + "\" key=\"" + event.getKey() + "\"}");
			Assert.assertEquals(mapName, event.getMapName());
			Assert.assertEquals(key, event.getKey());
			latch.countDown();
		};
		
		map.addListener(key, listener);
		map.send(key);
		
		boolean noTimeout = latch.await(5, TimeUnit.SECONDS);
		Assert.assertTrue(noTimeout);
		
		boolean found = map.removeListener(key, listener);
		Assert.assertTrue(found);	
	}
	
	@Test
	public void putListener() throws InterruptedException 
	{
		final String mapName = "myMap";
		ListenableConcurrentMap<String, Long> map = new ListenableConcurrentHashMap<>(mapName);
		
		final CountDownLatch latch = new CountDownLatch(1);
		final String key = "12";
		final Long value = 12L;

        PutListener<Long> listener = (PutEvent<Long> event) ->
		{
            CompletableFuture.runAsync(() ->
            {
                System.out.println("PutListener invoked with PutEvent{mapName=\"" + event.getMapName() + "\" key=\"" + event.getKey() + "\" putValues=\"" + event.getValues() + "\"}");
                Assert.assertEquals(mapName, event.getMapName());
                Assert.assertEquals(key, event.getKey());
                Assert.assertEquals(1, event.getValues().size());
                Assert.assertEquals(value, event.getValues().get(0));
                latch.countDown();
            });
		};

		map.addListener(key, listener);
		map.putSingleValue(key, value);
		
		boolean noTimeout = latch.await(5, TimeUnit.SECONDS);
		Assert.assertTrue(noTimeout);
		
		boolean found = map.removeListener(key, listener);
		Assert.assertTrue(found);	
	}
	
	@Test
	public void removeListener() throws InterruptedException 
	{
		final String mapName = "myMap";
		ListenableConcurrentMap<String, Long> map = new ListenableConcurrentHashMap<>(mapName);
		
		final CountDownLatch latch = new CountDownLatch(1);
		final String key = "12";
		final Long value = 12L;
		
		RemoveListener<Long> listener = (RemoveEvent<Long> event) ->
        {
			System.out.println("RemoveListener invoked with RemoveEvent{mapName=\"" + event.getMapName() + "\" key=\"" + event.getKey() + "\" removedValues=\"" + event.getValues() + "\"}");
			Assert.assertEquals(mapName, event.getMapName());
			Assert.assertEquals(key, event.getKey());
			Assert.assertEquals(1, event.getValues().size());
			Assert.assertEquals(value, event.getValues().get(0));
			latch.countDown();
		};
		
		map.addListener(key, listener);
		map.putSingleValue(key, value);
		map.remove(key, value);
		
		boolean noTimeout = latch.await(5, TimeUnit.SECONDS);
		Assert.assertTrue(noTimeout);
		
		boolean found = map.removeListener(key, listener);
		Assert.assertTrue(found);	
	}

}
