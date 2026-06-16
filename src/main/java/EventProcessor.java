import java.util.ArrayDeque;
import java.util.Queue;

public class EventProcessor {

    public static void main(String[] args) {
        // 1. Initialize queues using ArrayDeque
        Queue<String> orderCreatedQueue = new ArrayDeque<>();
        Queue<String> inventoryCheckedQueue = new ArrayDeque<>();
        Queue<String> paymentMadeQueue = new ArrayDeque<>();

        // 2. Simulate Order Event creation
        orderCreatedQueue.offer("Order #101 - John Doe");
        orderCreatedQueue.offer("Order #102 - Jane Smith");
        orderCreatedQueue.offer("Order #103 - Mark Johnson");

        System.out.println("--- Processing: Order Created ---");
        processQueue(orderCreatedQueue, inventoryCheckedQueue);

        System.out.println("\n--- Processing: Inventory Checked ---");
        processQueue(inventoryCheckedQueue, paymentMadeQueue);

        System.out.println("\n--- Processing: Payment Made ---");
        processQueue(paymentMadeQueue, null);
    }

    /**
     * Polls events from the current queue and processes/moves them.
     */
    private static void processQueue(Queue<String> currentQueue, Queue<String> nextQueue) {
        while (!currentQueue.isEmpty()) {
            // Retrieve and remove the event from the front of the queue
            String event = currentQueue.poll(); 
            
            // Perform processing logic
            System.out.println("Handled event: " + event);

            // Forward to the next queue in the pipeline, if applicable
            if (nextQueue != null) {
                nextQueue.offer(event);
            }
        }
    }
}
