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
import org.objectscape.candide.common.SendEvent;
import org.objectscape.candide.concurrent.SetEvent;
import org.objectscape.candide.stm.ListenableAtomicIntegerValue;
import org.objectscape.candide.stm.ListenableAtomicValue;
import org.objectscape.candide.util.values.BooleanValue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class ListenableAtomicValueTest extends AbstractTest {

    @Test
    public void set()
    {
        ListenableAtomicValue<Integer> value = new ListenableAtomicValue<>(0);
        Integer newValue = value.setAndGet((Integer val) -> val + 1);
        Assert.assertEquals(new Integer(1), newValue);
    }

    @Test
    public void setInteger() throws InterruptedException
    {
        ListenableAtomicIntegerValue value = new ListenableAtomicIntegerValue();
        Integer newValue = value.incrementAndGet();
        Assert.assertEquals(new Integer(1), newValue);

        value = new ListenableAtomicIntegerValue();
        Integer returnedValue = value.getAndIncrement();
        Assert.assertEquals(new Integer(0), returnedValue);
        Assert.assertEquals(new Integer(1), value.get());

        value = new ListenableAtomicIntegerValue();
        newValue = value.decrementAndGet();
        Assert.assertEquals(new Integer(-1), newValue);

        value = new ListenableAtomicIntegerValue();
        returnedValue = value.getAndDecrement();
        Assert.assertEquals(new Integer(0), returnedValue);
        Assert.assertEquals(new Integer(-1), value.get());

        value = new ListenableAtomicIntegerValue();
        newValue = value.addAndGet(5);
        Assert.assertEquals(new Integer(5), newValue);

        value = new ListenableAtomicIntegerValue();
        returnedValue = value.getAndAdd(5);
        Assert.assertEquals(new Integer(0), returnedValue);
        Assert.assertEquals(new Integer(5), value.get());

        final CountDownLatch waitTillDone = new CountDownLatch(1);
        value = new ListenableAtomicIntegerValue(2);
        BooleanValue eventWasSent = new BooleanValue();

        value.addListener((SetEvent<Integer> event) -> {
            CompletableFuture.runAsync(() -> {
                Assert.assertEquals(new Integer(2), event.getPreviousValue());
                Assert.assertEquals(new Integer(25), event.getValue());
                eventWasSent.set(true);
                waitTillDone.countDown();
            });
        });

        value.getAndAdd(23);
        waitTillDone.await();
        Assert.assertEquals(new Integer(25), value.get());
        Assert.assertTrue(eventWasSent.get());

        value = new ListenableAtomicIntegerValue(2);
        final CountDownLatch waitTillDone2 = new CountDownLatch(1);
        eventWasSent.set(false);
        value.addListener((SendEvent<Integer> event) -> {
            Assert.assertEquals(new Integer(2), event.getValue());
            eventWasSent.set(true);
            waitTillDone2.countDown();
        });

        value.send();
        waitTillDone.await();
        System.out.println("done");
        Assert.assertTrue(eventWasSent.get());
    }

    @Test
    public void setIntegerSynchronousListener() throws InterruptedException
    {
        final CountDownLatch waitTillDone = new CountDownLatch(1);
        ListenableAtomicIntegerValue value = new ListenableAtomicIntegerValue(2);
        BooleanValue eventWasSent = new BooleanValue();
        value.addListener((SetEvent<Integer> event) -> {
            Assert.assertEquals(new Integer(2), event.getPreviousValue());
            Assert.assertEquals(new Integer(25), event.getValue());
            eventWasSent.set(true);
            waitTillDone.countDown();
        });
        value.getAndAdd(23);
        waitTillDone.await();
        Assert.assertEquals(new Integer(25), value.get());
        Assert.assertTrue(eventWasSent.get());
    }
}
