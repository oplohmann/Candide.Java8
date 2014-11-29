package org.objectscape.candide;

import static org.junit.Assert.*;
import org.junit.Test;

import org.objectscape.candide.stm.*;
import org.objectscape.candide.util.values.BooleanValue;
import org.objectscape.candide.util.values.IntValue;


import static scala.concurrent.stm.japi.STM.atomic;

/**
 * Created by Oliver Plohmann on 29.11.2014.
 */
public class DemoTest extends AbstractTest {

    @Test
    public void simpleTrx() {
        final ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>();

        atomic(() -> {
            if (map.containsKey("1")) {
                map.put("2", 2);
            }
        });
    }

    @Test
    public void trxRollback() {
        final ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>();

        try {
            atomic(() -> {
                map.put("1", 1);
                int infinity = 1 / 0;
            });
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        atomic(() -> {
            System.out.println("map.size: " + map.size());
        });
    }

    @Test
    public void putListener()
    {
        ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>();

        BooleanValue listenerCalled = new BooleanValue(false);
        IntValue eventValue = new IntValue(0);

        atomic(() -> {
            map.addListener("1", (PutEvent<Integer> event) -> {
                listenerCalled.set(true);
                eventValue.set(event.getValue());
            });
        });

        atomic(() -> {
            map.put("1", 1);
        });

        assertTrue(listenerCalled.get());
        assertEquals(1, eventValue.get());
    }

    @Test
    public void removeListener()
    {
        ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>();

        BooleanValue listenerCalled = new BooleanValue(false);
        IntValue eventValue = new IntValue(0);

        atomic(() -> {
            map.addListener("1", (RemoveEvent<Integer> event) -> {
                listenerCalled.set(true);
                eventValue.set(event.getValue());
            });
            map.put("1", 1);
        });

        atomic(() -> {
            map.remove("1");
        });

        assertTrue(listenerCalled.get());
        assertEquals(1, eventValue.get());
    }

    @Test
    public void sendListener()
    {
        ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>();

        BooleanValue listenerCalled = new BooleanValue(false);
        IntValue eventValue = new IntValue(0);

        atomic(() -> {
            map.addListener("1", (SendEvent<Integer> event) -> {
                listenerCalled.set(true);
                eventValue.set(event.getValue());
            });
            map.put("1", 1);
        });

        atomic(() -> {
            map.send("1");
        });

        assertTrue(listenerCalled.get());
        assertEquals(1, eventValue.get());
    }
}
