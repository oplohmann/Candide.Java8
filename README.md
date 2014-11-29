## Candide for Java8+ ##

**Listenable Transactional Maps based on [Software Transactional Memory](http://en.wikipedia.org/wiki/Software_transactional_memory) (STM) and ConcurrentMaps for Java8 or later.**

*STM is a concurrency control mechanism analogous to database transactions for controlling access to shared memory in concurrent computing. STM does not apply any kind of locking, hence no deadlocks may occur. Transactions that do not terminate normally are rolled back.*  

STM in Candide is based on [ScalaSTM](http://nbronson.github.io/scala-stm/). 
There is also [Candide.preJava8](https://github.com/oplohmann/Candide.preJava8), which compiles with a JDK earlier than Java8.

----------


### Transactional Maps ###

#### Basics ####

In the sample code below an **atomic block** is defined in lines 4-8 using the atomic method, which takes a zero-argument lambda as its argument (e.g., a Runnable). **The atomic block defines the transaction:**  
 

	1 import org.objectscape.candide.stm.ListenableAtomicMap;
	2 import static scala.concurrent.stm.japi.STM.atomic;

	3 ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>();

	4 atomic(() -> {
    5 	if(map.containsKey("1")) {
    6   	map.put("2", 2);
    7   }
	8 });

The atomic block in the sample code above is analogous to this "conventional" solution below:

	Map<String, Integer> map = new HashMap<>();
        
	synchronized (map) {
    	if(map.containsKey("1")) {
        	map.put("2", 2);
        }
	}

The advantage of using STM compared to synchronized blocks/locks/semaphores/etc. is that STM does not do any locking and hence deadlocks cannot occur. 

**Furthermore, transactions in STM will be rolled back in case they don't terminate normally:**

	import org.objectscape.candide.stm.ListenableAtomicMap;
	import static scala.concurrent.stm.japi.STM.atomic;

	ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>();

    try {
		atomic(() -> {
        	map.put("1", 1);
            int infinity = 1 / 0;	// will cause exception to be thrown!
		});
	} catch (Exception ex) {
		System.out.println(ex.getMessage());
	}

	atomic(() -> {
    	System.out.println("map.size: " + map.size());
	});


In the code above the actions in the atomic block will be rolled back. **Note, that the try-catch block is defined *around* the atomic block and not inside it as required by ScalaSTM.** If defined inside the atomic block, the rollback will not happen. On the console you will see this:


    / by zero
    map.size: 0


#### Making use of Listeners ####

For ListenableAtomicMaps you can define listeners that are invoked on put, remove, and send. 

This is how to define a **PutListener** by supplying a *PutEvent* lambda argument::

	import org.objectscape.candide.stm.*;
	import org.objectscape.candide.util.values.BooleanValue;
	import org.objectscape.candide.util.values.IntValue;

	import static scala.concurrent.stm.japi.STM.atomic;

	ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>();

	BooleanValue listenerCalled = new BooleanValue(false);
    IntValue eventValue = new IntValue(0);

    atomic(() -> {
        map.addListener("1", (PutEvent<Integer> event) -> { // PutEvent defined here!
            listenerCalled.set(true);
            eventValue.set(event.getValue());
        });
    });

    atomic(() -> {
        map.put("1", 1);
    });

    assertTrue(listenerCalled.get());
    assertEquals(1, eventValue.get());

This is how to define a **RemoveListener** by supplying a *RemoveEvent* lambda argument:

	import org.objectscape.candide.stm.*;
	import org.objectscape.candide.util.values.BooleanValue;
	import org.objectscape.candide.util.values.IntValue;

	import static scala.concurrent.stm.japi.STM.atomic;

	ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>();

    BooleanValue listenerCalled = new BooleanValue(false);
    IntValue eventValue = new IntValue(0);

    atomic(() -> {
    	map.addListener("1", (RemoveEvent<Integer> event) -> { // RemoveEvent defined here!
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
    

This is how to define a **SendListener** by supplying a *SendEvent* lambda argument:

	import org.objectscape.candide.stm.*;
	import org.objectscape.candide.util.values.BooleanValue;
	import org.objectscape.candide.util.values.IntValue;

	import static scala.concurrent.stm.japi.STM.atomic;

	ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>();

    BooleanValue listenerCalled = new BooleanValue(false);
    IntValue eventValue = new IntValue(0);

    atomic(() -> {
    	map.addListener("1", (SendEvent<Integer> event) -> { // SendEvent defined here!
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

Run the test cases in subclasses of AbstractTest to understand the basic concept.
Creating the documentation is in progress ...
For the time being see http://www.objectscape.org/candide
