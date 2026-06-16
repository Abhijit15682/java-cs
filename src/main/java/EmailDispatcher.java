import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmailDispatcher {
    // 1. Define a thread pool with a fixed size (e.g., based on CPU cores)
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

    public void processBulkEmails(List<String> recipients) {
        for (String recipient : recipients) {
            // 2. Instead of one long loop, we submit each email as a "short-span" task
            executor.submit(() -> sendEmail(recipient));
        }
    }

    private void sendEmail(String email) {
        // This is the short-span task
        System.out.println("Sending email to: " + email + " on " + Thread.currentThread().getName());
        // Logic for SMTP call here...
    }
}
