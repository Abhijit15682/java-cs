package ratelimiter;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static java.lang.System.*;

public class RateLimiterStressTest {
    private static final int TOTAL_THREADS = 100;
    private static final String STRESS_CLIENT = "STRESS_TEST_CLIENT_ID";
    public static void main(String[] args) throws InterruptedException {
        out.println("=== INITIALIZING CONCURRENT STRESS HARNESS ===");
        out.println("Simulating " + TOTAL_THREADS + " threads firing simultaneously...");
        long twoSecondsInNs = TimeUnit.SECONDS.toNanos(2);
        int maxAllowedRequests = 25;
        runStressTest("FIXED_WINDOW", maxAllowedRequests, twoSecondsInNs);
        runStressTest("SLIDING_WINDOW", maxAllowedRequests, twoSecondsInNs);
        runStressTest("TOKEN_BUCKET", maxAllowedRequests, twoSecondsInNs);
    }
    private static void runStressTest(String strategy, int maxRequests,long winNs) throws InterruptedException {
        out.println("\n-----------------------------------------------");
        RateLimiterLockFree.ClientRateLimiter rateLimiter = new RateLimiterLockFree.ClientRateLimiter(strategy, maxRequests, winNs);
        ExecutorService executor = Executors.newFixedThreadPool(TOTAL_THREADS);
        CountDownLatch startingGun = new CountDownLatch(1);
        CountDownLatch finishingLine = new CountDownLatch(TOTAL_THREADS);
        AtomicInteger successfulRequests = new AtomicInteger(0);
        AtomicInteger rejectedRequests = new AtomicInteger(0);
        AtomicInteger execeptionCount = new AtomicInteger(0);

        for(int i =0; i < TOTAL_THREADS; i++) {
            executor.submit(() -> {
                try {
                    startingGun.await();
                    boolean allowed = rateLimiter.checkAccess(STRESS_CLIENT);
                    if(allowed) {
                        successfulRequests.incrementAndGet();
                    } else {
                        rejectedRequests.incrementAndGet();
                    }
                } catch(Exception e) {
                    execeptionCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    finishingLine.countDown();
                }

            });
        }
        long startTime = System.nanoTime();
        startingGun.countDown();
        boolean completedOnTime = finishingLine.await(5, TimeUnit.SECONDS);
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        executor.shutdownNow();
        out.println("Execution completed in: " + durationMs + " ms");
        out.println("Total Threads Spawned : " + TOTAL_THREADS);
        out.println("Successful Requests : " + successfulRequests.get() + " (Expected: <= " + maxRequests + ")");
        out.println("Rejected Requests: " + rejectedRequests.get());
        out.println("Exceptions caught : " + execeptionCount.get());

        if(!completedOnTime) {
            err.println("[FAIL] Test timeout! Possible lock-free livelock or infinite loop detected.");
        } else if(execeptionCount.get() > maxRequests) {
            err.println("[FAIL] Threads threw execeptions during execution.");
        } else if(successfulRequests.get() > maxRequests) {
            err.println("[FAIL] RATE LIMITER LEAKED! Allowed " 
                + successfulRequests.get() 
                + " requests, but limit was " + maxRequests);
        } else {
            out.println("[SUCCESS] " + strategy + " passed multi-threaded safety invariants.");
        }
    }
}
