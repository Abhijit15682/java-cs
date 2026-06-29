package ratelimiter;

import static java.lang.System.*;
import java.util.concurrent.atomic.*;
import java.util.Map;
import java.util.concurrent.*;
import java.util.Arrays;

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
  private record BucketState(long tokensScaled, long lastRefillTime, long remainderNs) {}
  private record SlidingState(long[] timestamps) {}

  // FIXED WINDOW: Standard Lock-Free Framework
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
          long windowsPassed = (now - currentStart) / winNs;
          currentStart = currentStart + (windowsPassed * winNs);
          currentCount = 0;
        }

        if (currentCount >= maxReq) {
          return false;
        }

        if (state.compareAndSet(current, new WindowState(currentStart, currentCount + 1))) {
          return true;
        }
      }
    }
  }

  // FIXED: Atomic Snapshot State Pattern. 
  // Prevents partial-write leaks and guarantees monotonic timeline synchronization.
  static class SlidingWindowLimiter implements RateLimiter {
    private final int maxReq;
    private final long winNs;
    private final AtomicReference<SlidingState> stateRef;

    public SlidingWindowLimiter(int maxReq, long winNs) {
      this.maxReq = maxReq;
      this.winNs = winNs;
      this.stateRef = new AtomicReference<>(new SlidingState(new long[0]));
    }

    @Override
    public boolean isAllowed(String clientId) {
      while (true) {
        long now = System.nanoTime();
        long boundary = now - winNs;
        SlidingState current = stateRef.get();
        long[] oldArray = current.timestamps;

        // Enforce chronological stability to catch multi-core CPU clock skews
        if (oldArray.length > 0) {
          now = Math.max(now, oldArray[oldArray.length - 1]);
          boundary = now - winNs;
        }

        // Count active records within the rolling frame bounds
        int validCount = 0;
        for (long ts : oldArray) {
          if (ts > boundary) {
            validCount++;
          }
        }

        // Early Exit: Rejects requests immediately before initiating heap instantiations
        if (validCount >= maxReq) {
          return false; 
        }

        // Build a fresh replacement array inside the isolated thread retry space
        long[] newArray = new long[validCount + 1];
        int idx = 0;
        for (long ts : oldArray) {
          if (ts > boundary) {
            newArray[idx++] = ts;
          }
        }
        newArray[validCount] = now;
        Arrays.sort(newArray);

        // Atomic reference update transition ensures absolute consistency
        if (stateRef.compareAndSet(current, new SlidingState(newArray))) {
          return true;
        }
      }
    }
  }

  // TOKEN BUCKET: Fixed-Point Scaled Framework with Remainder Continuity Accrual
  static class TokenBucketLimiter implements RateLimiter {
    private final long scale;
    private final long capacityScaled;
    private final long tokensPerNsScaled;
    private final AtomicReference<BucketState> state;

    public TokenBucketLimiter(long capacity, long winNs) {
      long computedScale = 1_000_000_000L;
      while ((capacity * computedScale) / winNs == 0 && computedScale < Long.MAX_VALUE / 10_000L) {
        computedScale *= 10L;
      }
      this.scale = computedScale;
      this.capacityScaled = capacity * scale;
      this.tokensPerNsScaled = (capacity * scale) / winNs;
      this.state = new AtomicReference<>(new BucketState(capacityScaled, System.nanoTime(), 0L));
    }

    @Override
    public boolean isAllowed(String clientId) {
      long now = System.nanoTime();
      
      while(true) {
        BucketState current = state.get();
        
        long effectiveNow = Math.max(now, current.lastRefillTime);
        long totalElapsedNs = (effectiveNow - current.lastRefillTime) + current.remainderNs();
        long tokensToAdd = totalElapsedNs * tokensPerNsScaled;
        long currentTokens = current.tokensScaled;
        
        long updatedRefillTime = effectiveNow;
        long nextRemainderNs = 0L;
        
        if (tokensToAdd > 0) {
          long totalTokens = currentTokens + tokensToAdd;
          if (totalTokens >= capacityScaled) {
            currentTokens = capacityScaled;
            updatedRefillTime = effectiveNow;
            nextRemainderNs = 0L;
          } else {
            currentTokens = totalTokens;
            long actualConsumedNs = tokensToAdd / tokensPerNsScaled;
            updatedRefillTime = current.lastRefillTime + actualConsumedNs;
            nextRemainderNs = totalElapsedNs - actualConsumedNs;
          }
        } else {
          updatedRefillTime = current.lastRefillTime;
          nextRemainderNs = totalElapsedNs; 
        }

        if (currentTokens < scale) {
          return false; 
        }

        if (state.compareAndSet(current, new BucketState(currentTokens - scale, updatedRefillTime, nextRemainderNs))) {
          return true;
        }
        
        now = System.nanoTime();
      }
    }
  }
}
