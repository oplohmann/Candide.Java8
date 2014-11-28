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

import org.junit.Assert;
import org.junit.Test;
import org.objectscape.candide.stm.*;
import org.objectscape.candide.util.values.BooleanValue;
import org.objectscape.candide.util.values.IntValue;
import org.objectscape.candide.util.scalastm.AtomicUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ListenableAtomicMapTest extends AbstractTest implements AtomicUtils {

    @Test
    public void putRemoveSizeIsEmpty()
    {
        BooleanValue listener1Called = new BooleanValue();
        BooleanValue listener2Called = new BooleanValue();

        atomic(() ->
        {
            ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>("map1");

            map.addListener("1", (PutEvent<Integer> event) -> {
                listener1Called.set(true);
            });

            map.put("1", 1);
            Assert.assertTrue(map.size() == 1);

            // assertion below won't work since things don't get committed in between, but
            // when the atomic transaction block is finally exited.
            // Assert.assertTrue(listener1Called[0]);

            map.remove("1");
            Assert.assertTrue(map.size() == 0 && map.isEmpty());

            ListenableAtomicMap<String, Integer> map2 = new ListenableAtomicMap<>("map2");
            map2.addListener("2", (PutEvent<Integer> event) -> {
                listener2Called.set(true);
            });

            map2.put("2", 2);
        });

        Assert.assertTrue(listener1Called.get());
        Assert.assertTrue(listener2Called.get());
    }

    @Test
    public void getContainsKey()
    {
        BooleanValue removeListenerCalled = new BooleanValue();

        String key = "1";
        Integer value = 1;

        atomic(() ->
        {
            ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>("map1");

            Assert.assertFalse(map.containsKey(key));
            Assert.assertFalse(map.containsValue(value));
            Assert.assertTrue(map.isEmpty());
            Assert.assertTrue(map.size() == 0);
            Assert.assertTrue(map.values().isEmpty());
            Assert.assertTrue(map.keySet().isEmpty());
            Assert.assertTrue(map.entrySet().isEmpty());

            map.put(key, value);
            Assert.assertTrue(map.containsKey(key));
            Assert.assertTrue(map.containsValue(value));
            Assert.assertTrue(map.size() == 1);
            Assert.assertFalse(map.isEmpty());
            Assert.assertTrue(map.values().size() == 1);
            Assert.assertTrue(map.keySet().size() == 1);
            Assert.assertTrue(map.entrySet().size() == 1);

            Assert.assertTrue(map.get(key).equals(value));

            map.addListener("1", (RemoveEvent<Integer> event) -> {
                removeListenerCalled.set(true);
            });

            map.clear();
            Assert.assertTrue(map.isEmpty());
         });

        Assert.assertTrue(removeListenerCalled.get());
    }

    /**
     * Test whether ScalaSTM handles concurrency correctly, cinditio sine qua non.
     * In the test 2 puts are done in different threads. With the use of latches it is
     * made sure that the 2nd thread will put a value to the shared map after the 1st
     * thread. Hence, the value put into the map by the 2nd thread is expected to
     * survive.
     */
    @Test
    public void concurrenPut()
    {
        BooleanValue exceptionOccured = new BooleanValue();

        String key = "1";
        Integer value1 = 1;
        Integer value2 = 2;
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        CountDownLatch waitTillAllDone = new CountDownLatch(2);

        ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>("map");

        Runnable runnable1 = () -> {
            atomic(() ->
            {
                try {
                    latch1.await();
                    map.put(key, value1);
                    latch2.countDown(); // release the latch that blocks the 2nd put
                }
                catch (Exception e) {
                    exceptionOccured.set(true);
                }
            });
            waitTillAllDone.countDown();
        };

        Runnable runnable2 = () -> {
            atomic(() ->
            {
                try {
                    latch1.countDown();
                    latch2.await();
                    map.put(key, value2);
                }
                catch (Exception e) {
                    exceptionOccured.set(true);
                }
            });
            waitTillAllDone.countDown();
        };

        new Thread(runnable1).start();
        new Thread(runnable2).start();

        try {
            waitTillAllDone.await();
        } catch (InterruptedException e) {
            exceptionOccured.set(true);
        }

        atomic(() -> Assert.assertTrue(map.get(key).equals(value2)));

        Assert.assertFalse(exceptionOccured.get());
    }

    /**
     * Test whether the default Map.replace methods produces the correct result when Map.replace
     * is not re-implemented in ListenableAtomicMap
     */
    @Test
    public void replace()
    {
        IntValue timesPutListenerCalled = new IntValue();

        atomic(() ->
        {
            ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>("map");

            map.addListener("1", (PutEvent<Integer> event) -> {
                timesPutListenerCalled.increment();
            });

            map.put("1", 1);
            Assert.assertTrue(map.size() == 1);

            map.replace("1", 2);
            Assert.assertTrue(map.size() == 1);
        });

        Assert.assertTrue(timesPutListenerCalled.get() == 2);
    }

    @Test
    public void send()
    {
        BooleanValue listenerCalled = new BooleanValue();

        String key = "1";
        String mapName = "map";
        Integer value = 1;
        IntValue expectedInvocationCount = new IntValue(1);

        atomic(() -> {

            ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>(mapName);

            SendListener<Integer> sendListener = (SendEvent<Integer> event) -> {
                Assert.assertTrue(key.equals(event.getKey()));
                Assert.assertTrue(value.equals(event.getValue()));
                Assert.assertTrue(event.getMapName().equals(mapName));
                Assert.assertTrue(event.getInvocationCount() == expectedInvocationCount.get());
                listenerCalled.set(true);
            };

            map.addListener(key, sendListener);

            map.put(key, 1);
            Assert.assertTrue(map.size() == 1);

            map.send("InexistentKey");
            Assert.assertFalse(listenerCalled.get());

            map.send(key);
            Assert.assertTrue(listenerCalled.get());

            listenerCalled.set(false);
            expectedInvocationCount.increment();
            map.send(key);
            Assert.assertTrue(expectedInvocationCount.get() == 2);
            Assert.assertTrue(listenerCalled.get());

            listenerCalled.set(false);
            boolean listenerFound = map.removeListener(key, sendListener);
            Assert.assertTrue(listenerFound);

            map.send(key);
            Assert.assertFalse(listenerCalled.get());
        });
    }

    @Test
    public void putAsyncListener() throws InterruptedException
    {
        BooleanValue listenerCalled = new BooleanValue();
        CountDownLatch latch = new CountDownLatch(1);

        atomic(() ->
        {
            ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>("map");

            map.addListener(
                "1",
                (PutEvent<Integer> event) -> {
                    Assert.assertEquals("1", event.getKey());
                    Assert.assertTrue(1 == event.getValue());
                    CompletableFuture.runAsync(() -> {
                        listenerCalled.set(true);
                        latch.countDown();
                    });
                });

            map.put("1", 1);
        });

        Assert.assertTrue(latch.await(10, TimeUnit.SECONDS));
        Assert.assertTrue(listenerCalled.get());
    }

    @Test
    public void putAsyncListenerRollback() throws InterruptedException
    {
        BooleanValue listenerCalled = new BooleanValue();
        ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>("map1");
        boolean divisionByZeroExceptionOccured = false;

        try
        {
            atomic(() ->
            {
                map.addListener(
                    "1",
                    (PutEvent<Integer> event) -> {
                        CompletableFuture.runAsync(() -> listenerCalled.set(true));
                    });

                map.put("1", 1);

                int value = 1 / 0;
            });
        }
        catch (Exception e) {
            divisionByZeroExceptionOccured = true;
        }

        Thread.sleep(100);

        atomic(() -> Assert.assertTrue(map.isEmpty()));

        Assert.assertTrue(divisionByZeroExceptionOccured);
        Assert.assertFalse(listenerCalled.get());  // not triggered because of rollback
    }


    @Test
    public void putRollback() {
        BooleanValue listener1Called = new BooleanValue();
        BooleanValue listener2Called = new BooleanValue();

        boolean exceptionOccurred = false;

        ListenableAtomicMap<String, Integer>[] map = new ListenableAtomicMap[1];
        ListenableAtomicMap<String, Integer>[] map2 = new ListenableAtomicMap[1];

        atomic(() ->
                {
                    map[0] = new ListenableAtomicMap<>("map1");
                    map2[0] = new ListenableAtomicMap<>("map1");
                });

        try {
            atomic(() ->
                    {
                        map[0].addListener("1", (PutEvent<Integer> event) -> {
                            listener1Called.set(true);
                        });

                        map[0].put("1", 1);

                        map2[0].addListener("2", (PutEvent<Integer> event) -> {
                            listener2Called.set(true);
                        });

                        map2[0].put("2", 2);

                        int i = 1 / 0;
                        System.out.println("we won't get here");
                    });
        } catch (Exception e) {
            exceptionOccurred = true;
        }

        Assert.assertFalse(listener1Called.get());
        Assert.assertFalse(listener2Called.get());

        Assert.assertTrue(exceptionOccurred);

        atomic(() ->
                {
                    Assert.assertNull(map[0].get("1"));
                    Assert.assertNull(map2[0].get("1"));
                });
    }

    @Test
    public void putRemoveSend() {
        BooleanValue listener1Called = new BooleanValue();
        BooleanValue listener2Called = new BooleanValue();
        BooleanValue sendListenerCalled = new BooleanValue();

        boolean exceptionOccurred = false;

        ListenableAtomicMap<String, Integer>[] map = new ListenableAtomicMap[1];
        ListenableAtomicMap<String, Integer>[] map2 = new ListenableAtomicMap[1];

        atomic(() ->
                {
                    map[0] = new ListenableAtomicMap<>("map1");
                    map2[0] = new ListenableAtomicMap<>("map2");
                });

        try {
            atomic(() ->
                    {
                        map[0].addListener("1", (PutEvent<Integer> event) -> {
                            listener1Called.set(true);
                        });

                        map[0].put("1", 1);

                        map2[0].addListener("2", (PutEvent<Integer> event) -> {
                            listener2Called.set(true);
                        });

                        map2[0].put("2", map[0].get("1"));

                        map[0].remove("1");

                        map2[0].addListener("2", (SendEvent<Integer> event) -> {
                            sendListenerCalled.set(true);
                        });

                        map2[0].send("2");
                    });
        } catch (Exception e) {
            exceptionOccurred = true;
        }

        Assert.assertTrue(listener1Called.get());
        Assert.assertTrue(listener2Called.get());
        Assert.assertTrue(sendListenerCalled.get());

        Assert.assertFalse(exceptionOccurred);

        atomic(() ->
                {
                    Assert.assertNull(map[0].get("1"));
                    Assert.assertEquals(new Integer(1), map2[0].get("2"));
                });
    }

    @Test
    public void putRemoveSendRollback() {
        BooleanValue listener1Called = new BooleanValue();
        BooleanValue listener2Called = new BooleanValue();

        boolean exceptionOccurred = false;

        ListenableAtomicMap<String, Integer>[] map = new ListenableAtomicMap[1];
        ListenableAtomicMap<String, Integer>[] map2 = new ListenableAtomicMap[1];

        atomic(() ->
                {
                    map[0] = new ListenableAtomicMap<>("map1");
                    map2[0] = new ListenableAtomicMap<>("map2");
                });

        try {
            atomic(() ->
                    {
                        PutListener<Integer> listener = (PutEvent<Integer> event) -> {
                            listener1Called.set(true);
                        };

                        map[0].addListener("1", listener);
                        map[0].put("1", 1);

                        PutListener<Integer> listener2 = (PutEvent<Integer> event) -> {
                            listener2Called.set(true);
                        };

                        map2[0].addListener("2", listener2);
                        map2[0].put("2", map[0].get("1"));

                        map[0].remove("1");

                        BooleanValue sendListenerCalled = new BooleanValue();
                        SendListener<Integer> sendListener = (SendEvent<Integer> event) -> {
                            sendListenerCalled.set(true);
                        };

                        map2[0].send("2");

                        int i = 1 / 0;
                        System.out.println("we won't get here");
                    });
        } catch (Exception e) {
            exceptionOccurred = true;
        }

        Assert.assertFalse(listener1Called.get());
        Assert.assertFalse(listener2Called.get());

        Assert.assertTrue(exceptionOccurred);

        atomic(() ->
                {
                    Assert.assertNull(map[0].get("1"));
                    Assert.assertNull(map2[0].get("1"));
                });
    }
}
