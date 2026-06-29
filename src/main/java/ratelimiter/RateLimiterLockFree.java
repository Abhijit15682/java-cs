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
        default -> throw new IllegalArgumentException("Unknown strategy" + strategy);
      };
    }  
  }

  interface RateLimiter {
    boolean isAllowed(String clientId);
  }

  private record WindowState(long start, int count) {}
  private record BucketState(double tokens, long lastRefillTime){}

  // FIXED: Lock-free atomic state transition prevents window time-shifting races
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
          currentStart = now;
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

  // FIXED: Discarded ConcurrentLinkedQueue removal race conditions. 
  // Relies on AtomicReference-wrapped Immutable state for absolute atomicity.
  static class SlidingWindowLimiter implements RateLimiter {
    private final int maxReq;
    private final long winNs;
    
    private record WindowLog(long[] timestamps) {}
    private final AtomicReference<WindowLog> logRef = new AtomicReference<>(new WindowLog(new long[0]));

    public SlidingWindowLimiter(int maxReq, long winNs) {
      this.maxReq = maxReq;
      this.winNs = winNs;
    }

    @Override
    public boolean isAllowed(String clientId) {
      while (true) {
        long now = System.nanoTime();
        long boundary = now - winNs;
        WindowLog current = logRef.get();
        long[] oldArray = current.timestamps;

        // Count how many timestamps are still inside the valid window
        int validCount = 0;
        for (long ts : oldArray) {
          if (ts > boundary) {
            validCount++;
          }
        }

        if (validCount >= maxReq) {
          return false; // Limit exceeded
        }

        // Rebuild immutable array with valid previous items + current item
        long[] newArray = new long[validCount + 1];
        int idx = 0;
        for (long ts : oldArray) {
          if (ts > boundary) {
            newArray[idx++] = ts;
          }
        }
        newArray[validCount] = now;

        if (logRef.compareAndSet(current, new WindowLog(newArray))) {
          return true;
        }
      }
    }
  }

  // FIXED: Preserves precision of timestamps if CAS fails due to cross-thread mutations
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
        
        long elapsedNs = Math.max(0, now - current.lastRefillTime);
        double tokensToAdd = elapsedNs * refillRateNs;
        double currentTokens = current.tokens;
        
        if(tokensToAdd > 0) {
          currentTokens = Math.min(capacity, currentTokens + tokensToAdd);
        } else {
          now = current.lastRefillTime; // Avoid drifting clock forwards without adding tokens
        }

        if(currentTokens < 1.0) {
          return false;
        }

        BucketState next = new BucketState(currentTokens - 1.0, now);
        if(state.compareAndSet(current, next)) {
          return true;
        }
      }
    }
  }
}
