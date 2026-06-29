package ratelimiter;

import static java.lang.System.*;
import java.util.concurrent.atomic.*;
import java.util.Map;
import java.util.concurrent.*;

public class RateLimiterLockFree {
  public static void main(String[] args) throws InterruptedException {
    unitTest("FIXED_WINDOW", 2000);
    unitTest("SLIDING_WINDOW", 2000);
    unitTest("TOKEN_BUCKET", 2000);

    unitTest("FIXED_WINDOW", 1999);
    unitTest("SLIDING_WINDOW", 1999);
    unitTest("TOKEN_BUCKET", 1999);
  }

  private static void unitTest(String strategy, int millis) throws InterruptedException {
    long twoSecondsInNs = TimeUnit.SECONDS.toNanos(2);
    String client1 = "client_1_0";
    String client2 = "client_2_0";
    out.println("\n\nClient Rate Limiters: " + strategy);
    ClientRateLimiter fixedWindowLim = new ClientRateLimiter(strategy, 3, twoSecondsInNs);
    out.println("Client 1 Req 1: " + fixedWindowLim.checkAccess(client1));
    out.println("Client 1 Req 2: " + fixedWindowLim.checkAccess(client1));
    out.println("Client 1 Req 3: " + fixedWindowLim.checkAccess(client1));
    out.println("Client 1 Req 4: " + fixedWindowLim.checkAccess(client1));
    out.println("Client 2 Req 1: " + fixedWindowLim.checkAccess(client2));
    out.println("\nSleeping to allow window expiration..milliseconds: - " + millis);
    Thread.sleep(millis);
    out.println("Client 1 Req 5: " + fixedWindowLim.checkAccess(client1));
  }

  static class ClientRateLimiter {
    private final Map<String, RateLimiter> clients = new ConcurrentHashMap<>();
    private final String strategy;
    private final int maxReq;
    private final long winNs;

    public ClientRateLimiter(String strategy, int maxReq, long winNs) {
      this.strategy = strategy;
      this.maxReq = maxReq;
      this.winNs = winNs;
    }

    public boolean checkAccess(String clientId) {
      RateLimiter limiter = clients.computeIfAbsent(clientId, id -> createLimiter());
      return limiter.isAllowed(clientId);
    }

    private RateLimiter createLimiter() {
      return switch(strategy.toUpperCase()) {
        case "FIXED_WINDOW" -> new FixedWindowLimiter(maxReq, winNs);
        case "SLIDING_WINDOW" -> new SlidingWindowLimiter(maxReq, winNs);
        case "TOKEN_BUCKET" -> new TokenBucketLimiter(maxReq, winNs);
        default -> throw new IllegalArgumentException("Unknown strategy: " + strategy);
      };
    }  
  }

  interface RateLimiter {
    boolean isAllowed(String clientId);
  }

  private record WindowState(long start, int count) {}
  private record BucketState(double tokens, long lastRefillTime){}

  // FIXED: Window start positions strictly align with step boundaries to avoid drift
  static class FixedWindowLimiter implements RateLimiter {
    private final int maxReq;
    private final long winNs;
    private final AtomicReference<WindowState> state;

    public FixedWindowLimiter(int maxReq, long winNs) {
      this.maxReq = maxReq;
      this.winNs = winNs;
      this.state = new AtomicReference<>(new WindowState(System.nanoTime(), 0));
    }

    @Override
    public boolean isAllowed(String clientID) {
      while(true) {
        long now = System.nanoTime();
        WindowState current = state.get();
        long currentStart = current.start;
        int currentCount = current.count;

        if (now - currentStart >= winNs) {
          // Calculate the correct window alignment relative to the current timeline
          long windowsPassed = (now - currentStart) / winNs;
          currentStart = currentStart + (windowsPassed * winNs);
          currentCount = 0;
        }

        if (currentCount >= maxReq) {
          return false;
        }

        WindowState next = new WindowState(currentStart, currentCount + 1);
        if (state.compareAndSet(current, next)) {
          return true;
        }
      }
    }
  }

  // FIXED: Leverages an optimized atomic cyclic array structure 
  // Allocations are decoupled from the CAS loop to maintain high performance.
  static class SlidingWindowLimiter implements RateLimiter {
    private final int maxReq;
    private final long winNs;
    private final AtomicLongArray ringBuffer;
    private final AtomicInteger head = new AtomicInteger(0);

    public SlidingWindowLimiter(int maxReq, long winNs) {
      this.maxReq = maxReq;
      this.winNs = winNs;
      // Pre-allocate a lock-free fixed circular log sized to maxReq limits
      this.ringBuffer = new AtomicLongArray(maxReq);
    }

    @Override
    public boolean isAllowed(String clientId) {
      long now = System.nanoTime();
      long boundary = now - winNs;

      while (true) {
        int currentHead = head.get();
        int targetIndex = currentHead % maxReq;
        long oldestTimestamp = ringBuffer.get(targetIndex);

        // Reject if the oldest position slot contains a valid active timestamp
        if (oldestTimestamp > boundary && oldestTimestamp != 0) {
          return false; 
        }

        // Advance slot ownership lock-free
        if (head.compareAndSet(currentHead, currentHead + 1)) {
          ringBuffer.set(targetIndex, now);
          return true;
        }
      }
    }
  }

  // FIXED: Relies on current.lastRefillTime inside loop to calculate additions accurately
  static class TokenBucketLimiter implements RateLimiter {
    private final double capacity;
    private final double refillRateNs;
    private final AtomicReference<BucketState> state;

    public TokenBucketLimiter(long capacity, long winNs) {
      this.capacity = capacity;
      this.refillRateNs = (double) capacity / winNs;
      this.state = new AtomicReference<>(new BucketState(capacity, System.nanoTime()));
    }

    @Override
    public boolean isAllowed(String clientId) {
      while(true) {
        long now = System.nanoTime();
        BucketState current = state.get();
        
        // Base timeline evaluation relative to previous structural transitions
        long elapsedNs = Math.max(0, now - current.lastRefillTime);
        double tokensToAdd = elapsedNs * refillRateNs;
        double currentTokens = current.tokens;
        
        long nextRefillTime = now;
        if(tokensToAdd > 0) {
          currentTokens = Math.min(capacity, currentTokens + tokensToAdd);
        } else {
          nextRefillTime = current.lastRefillTime; 
        }

        if(currentTokens < 1.0) {
          return false;
        }

        BucketState next = new BucketState(currentTokens - 1.0, nextRefillTime);
        if(state.compareAndSet(current, next)) {
          return true;
        }
      }
    }
  }
}
