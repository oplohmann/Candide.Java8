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
import org.objectscape.candide.common.SendEvent;
import org.objectscape.candide.concurrent.ListenableConcurrentValue;
import org.objectscape.candide.concurrent.SetEvent;
import org.objectscape.candide.util.values.BooleanValue;
import org.objectscape.candide.util.values.IntValue;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class ListenableConcurrentValueTest extends AbstractTest {

    @Test
    public void set()
    {
        ListenableConcurrentValue<Integer> valueHolder = new ListenableConcurrentValue<>("TestValueHolder", 0);
        boolean success = false;

        do {
            Integer currentValue = valueHolder.get();
            if(currentValue != null && currentValue.equals(new Integer(1))) {
                success = true;
                break;
            }
            success = valueHolder.set(currentValue, currentValue + 1);
        }
        while(!success);

        Assert.assertEquals(new Integer(1), valueHolder.get());
    }

    @Test
    public void setExpectWithFunction()
    {
        int delta = new Random(System.currentTimeMillis()).nextInt();
        ListenableConcurrentValue<Integer> valueHolder = new ListenableConcurrentValue<>("TestValueHolder", 0);
        boolean success = false;

        do {
            Integer currentValue = valueHolder.get();
            success = valueHolder.set(currentValue, (Integer i)-> i + delta);
        }
        while(!success);

        Assert.assertEquals(new Integer(delta), valueHolder.get());
    }

    @Test
    public void setWithFunction()
    {
        int delta = new Random(System.currentTimeMillis()).nextInt();
        ListenableConcurrentValue<Integer> valueHolder = new ListenableConcurrentValue<>("TestValueHolder", 0);
        Integer newValue = null;

        Integer currentValue = valueHolder.get();
        newValue = valueHolder.set((Integer i)-> {
            if(valueHolder.get() == 0)
                return i + delta;
            return -1;
        });

        Assert.assertEquals(new Integer(delta), valueHolder.get());
    }

    @Test
    public void setWithFunctionException()
    {
        ListenableConcurrentValue<Integer> valueHolder = new ListenableConcurrentValue<>("TestValueHolder", 0);
        BooleanValue listenerInvoked = new BooleanValue();
        valueHolder.addListener((SetEvent<Integer> event) -> {
            listenerInvoked.set(true);
        });

        try
        {
            valueHolder.set((Integer i)-> {
                if(true)
                    throw new RuntimeException("test exception");
                return -1;
            });
        }
        catch (RuntimeException e) {
            // common remained unchanged, because changes were rolled back when exception occurred
            Assert.assertEquals(new Integer(0), valueHolder.get());
        }

        Assert.assertFalse(listenerInvoked.get());
    }

    @Test
    public void setWithAynchronousSetListener() throws InterruptedException {
        ListenableConcurrentValue<Integer> valueHolder = new ListenableConcurrentValue<>("TestValueHolder", 0);

        IntValue previousValue = new IntValue(-1);
        IntValue newValue = new IntValue(-1);

        CountDownLatch latch = new CountDownLatch(1);

        valueHolder.addListener((SetEvent<Integer> event) -> {
            previousValue.set(event.getPreviousValue());
            newValue.set(event.getValue());
            latch.countDown();
        });

        valueHolder.set((Integer i)-> 5);

        latch.await();
        Assert.assertEquals(new Integer(0), previousValue.getObject());
        Assert.assertEquals(new Integer(5), newValue.getObject());
    }

    @Test
    public void setWithSynchronousSetListener() throws InterruptedException
    {
        ListenableConcurrentValue<Integer> valueHolder = new ListenableConcurrentValue<>("TestValueHolder", 0);

        IntValue previousValue = new IntValue(-1);
        IntValue newValue = new IntValue(-1);

        valueHolder.addListener((SetEvent<Integer> event) -> {
            previousValue.set(event.getPreviousValue());
            newValue.set(event.getValue());
        });

        valueHolder.set((Integer i)-> 5);

        Assert.assertEquals(new Integer(0), previousValue.getObject());
        Assert.assertEquals(new Integer(5), newValue.getObject());
    }

    @Test
    public void setWithSynchronousSendListener() throws InterruptedException {
        ListenableConcurrentValue<Integer> valueHolder = new ListenableConcurrentValue<>("TestValueHolder", 0);

        IntValue newValue = new IntValue(-1);

        valueHolder.addListener((SendEvent<Integer> event) -> {
            newValue.set(event.getValue());
        });

        valueHolder.set((Integer i)-> i + 5);
        valueHolder.send();

        Assert.assertEquals(new Integer(5), newValue.getObject());
    }

    @Test
    public void setWithAsynchronousSendListener() throws InterruptedException
    {
        ListenableConcurrentValue<Integer> valueHolder = new ListenableConcurrentValue<>("TestValueHolder", 0);

        IntValue newValue = new IntValue(-1);
        CountDownLatch latch = new CountDownLatch(1);

        valueHolder.addListener((SendEvent<Integer> event) -> {
            CompletableFuture.runAsync(() -> {
                newValue.set(event.getValue());
                latch.countDown();
            });
        });

        valueHolder.set((Integer i)-> i + 5);
        valueHolder.send();

        latch.await();
        Assert.assertEquals(new Integer(5), newValue.getObject());
    }
}
