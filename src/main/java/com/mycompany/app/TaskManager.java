import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskManager {

    public static void main(String[] args) {
        // 1. Create a Queue for holding the short-span tasks
        BlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<>(50);

        // 2. Create a Thread Pool (e.g., 5 worker threads)
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5, 5, 1, TimeUnit.MINUTES, taskQueue
        );

        // Pre start the core threads to avoid initialization latency
        executor.prestartAllCoreThreads();

        System.out.println("Submitting 20 sub-tasks to the queue...");

        // 3. Break the long task into 20 short span sub-tasks and queue them
        for (int i = 1; i <= 20; i++) {
            final int taskId = i;
            executor.execute(() -> {
                System.out.println("Processing sub-task " + taskId + " on thread: " + Thread.currentThread().getName());
                try {
                    // Simulate a quick chunk of work
                    Thread.sleep(500); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // 4. Gracefully shut down the executor once tasks are done
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("All tasks completed.");
    }
}
