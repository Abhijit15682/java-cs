import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderEventProcessor {


    /*
    * Why use ConcurrentLinkedQueue?Non-Blocking: Uses lock-free Compare-And-Swap (CAS) instructions.
    * Threads writing events don't block each other, which prevents bottlenecks in
    * event-driven architectures.Unbounded: It can grow dynamically without throwing an exception.
    * Weakly Consistent Iterators: Iterators can be used safely even when other threads are
    * appending to the queue at the same time.
    *
    * Best Practices & Alternative Collections When retrieving elements,
    * always prefer the poll() method over remove() as it safely retrieves and
    * removes the item from the queue without throwing exceptions if it's empty.
    * If your requirements differ, explore these alternatives:For similar lock-free
    * behavior but with double-ended support (add/remove from both head and tail).
    *
    * */
    // Thread-safe queue to store our event strings
    private static final Queue<String> eventQueue = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // 1. Simulate multiple producer threads logging events
        executor.submit(() -> eventQueue.offer("Event: Order Created - Order #10024"));
        executor.submit(() -> eventQueue.offer("Event: Inventory Checked - Order #10024"));
        executor.submit(() -> eventQueue.offer("Event: Payment Made - Order #10024"));

        // Let background threads run and process events
        Thread.sleep(1000);

        // 2. Consumer thread processes the events
        executor.submit(() -> {
            String event;
            while ((event = eventQueue.poll()) != null) {
                System.out.println("Processing -> " + event);
            }
        });

        executor.shutdown();
    }
}
