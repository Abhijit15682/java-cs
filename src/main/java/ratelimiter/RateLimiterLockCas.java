package ratelimiter;

import static java.lang.System.*;
import java.util.concurrent.atomic.*;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

public class RateLimiterLockCas {
  public static void main(String[] args) throws InterruptedException {
    unitTest("FIXED_WINDOW", 2000);
    unitTest("SLIDING_WINDOW", 1999);
    unitTest("TOKEN_BUCKET", 675);

  }

  private static void unitTest(String strategy, int sleepTime) throws InterruptedException {
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
    out.println("\nSleeping to allow window expiration..milliseconds: - " + sleepTime);
    Thread.sleep(sleepTime);
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

  // FIXED WINDOW: Standard Lock-Free Framework
  static class FixedWindowLimiter implements RateLimiter {
    private final int maxReq;
    private final long winNs;
    private record FixedWindow(long winStart, int reqCount) {}
    private final AtomicReference<FixedWindow> fixedWindowReference;

    public FixedWindowLimiter(int maxReq, long winNs) {
      this.maxReq = maxReq;
      this.winNs = winNs;
      this.fixedWindowReference = new AtomicReference<>(new FixedWindow(System.nanoTime(), 0));
    }

    @Override
    public boolean isAllowed(String clientID) {
      while(true) {
        long now = System.nanoTime();
        FixedWindow recentWindow = fixedWindowReference.get();
        long winStartTime = recentWindow.winStart;
        int winReqCount = recentWindow.reqCount;

        if (now - winStartTime >= winNs) {
          winStartTime = now;
          winReqCount = 0;
        }

        if (winReqCount >= maxReq) {
          return false;
        }
        FixedWindow next = new FixedWindow(winStartTime, winReqCount + 1);
        if (fixedWindowReference.compareAndSet(recentWindow, next)) {
          return true;
        }
      }
    }
  }

  static class SlidingWindowLimiter implements RateLimiter {
    private final int maxReq;
    private final long winNs;
    private final Queue<Long> reqQueue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger reqCounter = new AtomicInteger(0);

    public SlidingWindowLimiter(int maxReq, long winNs) {
      this.maxReq = maxReq;
      this.winNs = winNs;
    }

    @Override
    public boolean isAllowed(String clientId) {
      long now = System.nanoTime();
      long boundary = now - winNs;
      Long first;
      while((first = reqQueue.peek()) != null && first <=boundary){
        if(reqQueue.remove(first)) {
          reqCounter.decrementAndGet();
        }
      }
      reqQueue.add(now);
      int updatedSize = reqCounter.incrementAndGet();
      if( updatedSize <= maxReq) {
        return true;
      }
      if(reqQueue.remove(now)) {
        reqCounter.decrementAndGet();
      }
      return false;
    }
  }

  static class TokenBucketLimiter implements RateLimiter {
    private final long capacity;
    private final double refillRatePerWin;
    private record BucketState(long tokens, long lastRefillTime) {}
    private final AtomicReference<BucketState> state;

    public TokenBucketLimiter(long capacity, long winNs) {
      this.capacity = capacity;
      this.refillRatePerWin = (double) capacity/winNs;
      this.state = new AtomicReference<>(new BucketState(capacity, System.nanoTime()));
    }

    @Override
    public boolean isAllowed(String clientId) {
      while(true) {
        long now = System.nanoTime();
        BucketState current = state.get();
        long durationAfterLastRefill = Math.max(0, now - current.lastRefillTime);
        long tokensToAdd = (long) (durationAfterLastRefill * refillRatePerWin);
        long bucketTokens = current.tokens;
        if(tokensToAdd > 0) {
          bucketTokens = Math.min(capacity, bucketTokens + tokensToAdd);
        }
        if(bucketTokens < 1) {
          return false;
        }
        BucketState next = new BucketState( bucketTokens-1, now);
        if(state.compareAndSet(current, next)) {
          return true;
        }
      }
    }
  }
}