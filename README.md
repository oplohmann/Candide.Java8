## Candide for Java8+ ##

**Listenable Transactional Maps based on [Software Transactional Memory](http://en.wikipedia.org/wiki/Software_transactional_memory) (STM) and ConcurrentMaps for Java8 or later.**

*STM is a concurrency control mechanism analogous to database transactions for controlling access to shared memory in concurrent computing. STM does not apply any kind of locking, hence no deadlocks may occur. Transactions that do not terminate normally are rolled back.*  

STM in Candide is based on [ScalaSTM](http://nbronson.github.io/scala-stm/). 
There is also [Candide.preJava8](https://github.com/oplohmann/Candide.preJava8), which compiles with a JDK earlier than Java8.

----------


### Transactional Maps ###

#### Basics ####

The code in this section can also be executed from class [DemoTest](https://github.com/oplohmann/Candide.Java8/blob/master/src/test/java/org/objectscape/candide/DemoTest.java). 
In the sample code below an **atomic block** is defined in lines 3-7 using the atomic method, which takes a zero-argument lambda as its argument (e.g., a Runnable). **The atomic block defines the transaction:**  
 

	1 import org.objectscape.candide.stm.*;

	2 ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>();

	3 atomic(() -> {
    4 	if(map.containsKey("1")) {
    5   	map.put("2", 2);
    6   }
	7 });

The atomic block in the sample code above is analogous to this "conventional" solution using a synchronized block:

	Map<String, Integer> map = new HashMap<>();
        
	synchronized (map) {
    	if(map.containsKey("1")) {
        	map.put("2", 2);
        }
	}

The advantage of using STM compared to synchronized blocks/locks/semaphores/etc. is that STM does not do any locking and hence deadlocks cannot occur. 

**Furthermore, transactions in STM will be rolled back in case they don't terminate normally:**

	import org.objectscape.candide.stm.*;

	ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>();

    try {
		atomic(() -> {
        	map.put("1", 1);
            int infinity = 1 / 0;	// will cause exception resulting in trx being rolled back!
		});
	} catch (Exception ex) {
		System.out.println(ex.getMessage());
	}

	atomic(() -> {
    	System.out.println("map.size: " + map.size());
	});


In the code above the actions in the atomic block will be rolled back. **Note, that the try-catch block is defined *around* the atomic block and not inside it, which is required by ScalaSTM.** If defined inside the atomic block, the rollback will not happen. On the console you will see this as the put to the map is rolled back:


    / by zero
    map.size: 0


#### Making use of Listeners ####

For ListenableAtomicMaps you can define listeners that are invoked on put, remove, and send. 

This is how to define a **PutListener** by supplying a *PutEvent* lambda argument::

	import org.objectscape.candide.stm.*;
	import org.objectscape.candide.util.values.BooleanValue;
	import org.objectscape.candide.util.values.IntValue;

	ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>();

	BooleanValue listenerCalled = new BooleanValue(false);
    IntValue eventValue = new IntValue(0);

    atomic(() -> {
        map.addListener("1", (PutEvent<Integer> event) -> { // PutEvent defined here
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

	ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>();

    BooleanValue listenerCalled = new BooleanValue(false);
    IntValue eventValue = new IntValue(0);

    atomic(() -> {
    	map.addListener("1", (RemoveEvent<Integer> event) -> { // RemoveEvent defined here
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

	ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>();

    BooleanValue listenerCalled = new BooleanValue(false);
    IntValue eventValue = new IntValue(0);

    atomic(() -> {
    	map.addListener("1", (SendEvent<Integer> event) -> { // SendEvent defined here
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

#### Asynchronously executed Listeners ####

JDK8 adds [CompletableFutures](http://www.nurkiewicz.com/2013/05/java-8-definitive-guide-to.html), which make defining asynchronously executed callbacks easy:

	import org.objectscape.candide.stm.*;
	import org.objectscape.candide.util.values.BooleanValue;

	import java.util.concurrent.CompletableFuture;
	import java.util.concurrent.CountDownLatch;

	BooleanValue listenerCalled = new BooleanValue();
    CountDownLatch latch = new CountDownLatch(1);

	ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>("map");

    atomic(() ->
    {    
        map.addListener(
        	"1",
            (PutEvent<Integer> event) -> {
				CompletableFuture.runAsync(() -> {	// CompletableFuture defined here
                	listenerCalled.set(true);
                	latch.countDown();
				});
			});            
        });
	});

	atomic(() ->
    {
		map.put("1", 1);
	});

	Assert.assertTrue(latch.await(10, TimeUnit.SECONDS));
    Assert.assertTrue(listenerCalled.get());



----------

### Listenable Concurrent Maps ###

ListenableAtomicMaps are convenient in the way that they leverage the functionality provided by ScalaSTM. However, STM comes at a cost. ScalaSTM is very efficient. It beats a solution which defines 

#### Basics ####

----------

### Test Cases ###
Have a look at the test cases in subclasses of [AbstractTest](https://github.com/oplohmann/Candide.Java8/blob/master/src/test/java/org/objectscape/candide/AbstractTest.java) to see more sample code that is more intertwined. 
