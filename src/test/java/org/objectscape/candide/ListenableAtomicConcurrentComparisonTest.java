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

import org.junit.Ignore;
import org.junit.Test;
import org.objectscape.candide.concurrent.ListenableConcurrentIntegerValue;
import org.objectscape.candide.stm.ListenableAtomicIntegerValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 *
 * All measurements done on an Intel Core2 Duo CPU E8400 3.00 GHz using JDK1.8.0_11
 *
 * Result of running <code>incrementConcurrentValue</code> with ListenableConcurrentValue.lock
 * using >>ReentrantReadWriteLock<<:
 *
 * time for 2 threads concurrent: 17624 ms
 * time for 4 threads concurrent: 25728 ms
 * time for 6 threads concurrent: 25682 ms
 * time for 8 threads concurrent: 25599 ms
 * time for 10 threads concurrent: 25807 ms
 * time for 12 threads concurrent: 25773 ms
 * time for 14 threads concurrent: 25705 ms
 * time for 16 threads concurrent: 25666 ms
 *
 * Result of running <code>incrementConcurrentValue</code> with ListenableConcurrentValue.lock
 * using >>StampedLock<<:
 *
 time for 2 threads concurrent: 1329 ms
 time for 4 threads concurrent: 1384 ms
 time for 6 threads concurrent: 1438 ms
 time for 8 threads concurrent: 1452 ms
 time for 10 threads concurrent: 1501 ms
 time for 12 threads concurrent: 1483 ms
 time for 14 threads concurrent: 1361 ms
 time for 16 threads concurrent: 1454 ms
 *
 *
 * Result of running <code>incrementAtomicValue</code>:
 *
 * time for 2 threads scalastm: 3675 ms
 * time for 4 threads scalastm: 4321 ms
 * time for 6 threads scalastm: 4230 ms
 * time for 8 threads scalastm: 4788 ms
 * time for 10 threads scalastm: 5571 ms
 * time for 12 threads scalastm: 6997 ms
 * time for 14 threads scalastm: 6707 ms
 * time for 16 threads scalastm: 6783 ms
 *
 */

@Ignore // not part of regression tests - for performance comparison only
public class ListenableAtomicConcurrentComparisonTest extends AbstractTest
{

    private int max = 9000000;
    private int maxThreads = 16;

    @Test
    public void incrementConcurrentValue() throws InterruptedException
    {
        for(int i = 1; i * 2 <= maxThreads; i++) {
            incrementConcurrentValue(i * 2, max);
        }
    }

    @Test
    public void incrementAtomicValue() throws InterruptedException
    {
        for(int i = 1; i * 2 <= maxThreads; i++) {
            incrementAtomicValue(i * 2, max);
        }
    }

    private void incrementAtomicValue(int numThreads, int max) throws InterruptedException
    {
        CountDownLatch allDone = new CountDownLatch(numThreads);
        ListenableAtomicIntegerValue value = new ListenableAtomicIntegerValue();
        List<Thread> threads = new ArrayList<>(numThreads);

        for (int i = 0; i < numThreads; i++)
            threads.add(new Thread(atomicIncrementBlock(value, max, allDone)));

        long start = System.currentTimeMillis();

        for (int i = 0; i < numThreads; i++)
            threads.get(i).start();

        allDone.await();

        System.out.println("time for " + numThreads + " threads scalastm: " + (System.currentTimeMillis() - start) + " ms");
    }

    private void incrementConcurrentValue(int numThreads, int max) throws InterruptedException
    {
        CountDownLatch allDone = new CountDownLatch(numThreads);
        ListenableConcurrentIntegerValue value = new ListenableConcurrentIntegerValue();
        List<Thread> threads = new ArrayList<>(numThreads);

        for (int i = 0; i < numThreads; i++)
            threads.add(new Thread(concurrentIncrementBlock(value, max, allDone)));

        long start = System.currentTimeMillis();

        for (int i = 0; i < numThreads; i++)
            threads.get(i).start();

        allDone.await();

        System.out.println("time for " + numThreads + " threads concurrent: " + (System.currentTimeMillis() - start) + " ms");
    }

    private  Runnable concurrentIncrementBlock(ListenableConcurrentIntegerValue value, int max, CountDownLatch done)
    {
        return ()->
        {
            int newValue = 0;

            Function<Integer, Integer> adder = incrementBlock(max);

            while(newValue < max)
                newValue = value.set(adder);

            done.countDown();
        };
    }

    private  Runnable atomicIncrementBlock(ListenableAtomicIntegerValue value, int max, CountDownLatch done)
    {
        return ()->
        {
            int newValue = 0;

            Function<Integer, Integer> adder = incrementBlock(max);

            while(newValue < max)
                newValue = value.setAndGet(adder);

            done.countDown();
        };
    }

    private  Function<Integer, Integer> incrementBlock(int max) {
        return (Integer currentValue)-> {
            if(currentValue == max)
                return currentValue;
            return currentValue + 1;
        };
    }
}
