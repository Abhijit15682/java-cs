import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class BoundedConcurrentLinkedQueue<E> {
    private final ConcurrentLinkedQueue<E> queue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger size = new AtomicInteger(0);
    private final int capacity;

    public BoundedConcurrentLinkedQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero");
        }
        this.capacity = capacity;
    }

    /**
     * Attempts to add an element if capacity allows.
     * @return true if added, false if queue is full.
     */
    public boolean offer(E element) {
        if (element == null) {
            throw new NullPointerException("Null elements are not allowed");
        }
        //while (true) {
            int currentSize = size.get();
            if (currentSize >= capacity) {
                return false; // Queue is full
            }
            if (size.compareAndSet(currentSize, currentSize + 1)) {
                queue.offer(element);
                return true;
            } else
                return false;
        //}
    }

    /**
     * Retrieves and removes the head of the queue, or returns null if empty.
     */
    public E poll() {
        E item = queue.poll();
        if (item != null) {
            size.decrementAndGet();
        }
        return item;
    }

    public int size() {
        return size.get();
    }

    public boolean isEmpty() {
        return size.get() == 0;
    }

    public boolean isFull() {
        return size.get() >= capacity;
    }

    public static void main(String[] args) {
        BoundedConcurrentLinkedQueue<Integer> boundedQueue = new BoundedConcurrentLinkedQueue<>(3);

        // Producer threads
        Runnable producer = () -> {
            for (int i = 1; i <= 5; i++) {
                if (boundedQueue.offer(i)) {
                    System.out.println(Thread.currentThread().getName() + " added: " + i);
                } else {
                    System.out.println(Thread.currentThread().getName() + " queue full, could not add: " + i);
                }
            }
        };

        // Consumer threads
        Runnable consumer = () -> {
            for (int i = 0; i < 5; i++) {
                Integer val = boundedQueue.poll();
                if (val != null) {
                    System.out.println(Thread.currentThread().getName() + " removed: " + val);
                } else {
                    System.out.println(Thread.currentThread().getName() + " queue empty");
                }
            }
        };

        Thread t1 = new Thread(producer, "Producer-1");
        Thread t2 = new Thread(producer, "Producer-2");
        Thread t3 = new Thread(consumer, "Consumer-1");

        t1.start();
        t2.start();
        t3.start();
    }
}
