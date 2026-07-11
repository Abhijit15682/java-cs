package ratelimiter;

import static java.lang.System.*;
import java.util.concurrent.atomic.*;
import java.util.Map;
import java.util.concurrent.*;

/**
 * This is main outer class which holds fixed, sliding window and token bucket rate limiters.
 * It has main method with unit test for each limiter with two clients.
 * It checks if request is being restricted and allowed after elapsed time window.
 * Fixed window does not allow at all till fixed window. 3 request fixed for time window even at edge.
 * Where as sliding window allows with fractional amount as time window shifts. fraction of millisecond one
 * Also token bucket calculates elapsed time to allow per allowed percent. after percent time window allowed for new request.
 */
public class RateLimiterLockProdCas {
  public static void main(String[] args) throws InterruptedException {
    unitTest("FIXED_WINDOW", 2000);
    unitTest("SLIDING_WINDOW", 1999);
    unitTest("TOKEN_BUCKET", 675);
  }

  /**
   * Test different strategy for rate limiter with sleepTime to request again for window edges.
   * @param strategy
   * @param sleepTime
   * @throws InterruptedException
   */
  private static void unitTest(String strategy, int sleepTime) throws InterruptedException {
    long twoSecondsInNs = TimeUnit.SECONDS.toNanos(2);
    String client1 = "client_1_0";
    String client2 = "client_2_0";
    out.println("\n\nClient Rate Limiters: " + strategy);
    ClientRateLimiter limiter = new ClientRateLimiter(strategy, 3, twoSecondsInNs);
    out.println("Client 1 Req 1: " + (limiter.isAllowed(client1) ? "Success" : "Failure"));
    out.println("Client 1 Req 2: " + (limiter.isAllowed(client1) ? "Success" : "Failure"));
    out.println("Client 1 Req 3: " + (limiter.isAllowed(client1) ? "Success" : "Failure"));
    out.println("Client 1 Req 4: " + (limiter.isAllowed(client1) ? "Failure" : "Success"));
    out.println("Client 2 Req 1: " + (limiter.isAllowed(client2) ? "Success" : "Failure"));
    out.println("\nSleeping to allow window expiration..milliseconds: - " + sleepTime);
    Thread.sleep(sleepTime);
    out.println("Client 1 Req 5: " + (limiter.isAllowed(client1) ? "Success" : "Failure"));
  }

  /**
   * This class has concurrent map of clients,strategy, maximum request to allow with in time window in nano seconds.
   */
  static class ClientRateLimiter {
    private final Map<String, RateLimiter> clients = new ConcurrentHashMap<>();
    private final String strategy;
    private final int maxReq;
    private final long winNs;

    /**
     * Builds with strategy, maxReq and window in nanoseconds.
     * @param strategy
     * @param maxReq
     * @param winNs
     */
    public ClientRateLimiter(String strategy, int maxReq, long winNs) {
      this.strategy = strategy;
      this.maxReq = maxReq;
      this.winNs = winNs;
    }

    /**
     * method which internally invokes isAllowed retrieving/creating client from concurrent hashmap
     * @param clientId
     * @return
     */
    public boolean isAllowed(String clientId) {
      RateLimiter limiter = clients.computeIfAbsent(clientId, id -> createLimiter());
      return limiter.checkAccess();
    }

    /**
     * This method return RateLimiter with member strategy for variation.
     * @return
     */
    private RateLimiter createLimiter() {
      return switch(strategy.toUpperCase()) {
        case "FIXED_WINDOW" -> new FixedWindowLimiter(maxReq, winNs);
        case "SLIDING_WINDOW" -> new SlidingWindowLimiter(maxReq, winNs);
        case "TOKEN_BUCKET" -> new TokenBucketLimiter(maxReq, winNs);
        default -> throw new IllegalArgumentException("Unknown strategy: " + strategy);
      };
    }  
  }

  /**
   * 
   * Parent interface RateLimiter stating method to be allowed for variation implementation.
   */
  interface RateLimiter {
    boolean checkAccess();
  }

  /**
   * It holds maxReq,winNs primitives, FixedWindow record with winStart,reqCount and AtomicReference which holds it.
   * FixedWindowLimiter
   */
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

    /**
     * it get current time recent window start and request count.
     * compares if recent window elapsed to reset winStart and count.
     * also if winReqCount exceeds restricts.
     * Else create new fixedWindow record with new start time and with updated counter
     *  (for fixed same window or new window of time)
     */
    @Override
    public boolean checkAccess() {
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

  /** This is a sliding window implementing class
   * holds maxReq,winNs Queue for request and Atomic counter.
   * Idea here is to hold request time in queue and evict out of boundary requests to allow new.
   */
  static class SlidingWindowLimiter implements RateLimiter {
    private final int maxReq;
    private final long winNs;
    private record WindowLog(long[] timestamps){}
    private final AtomicReference<WindowLog> sliWinRef;

    /**
     * This constructs with maxReq and winNs window request limit and time limit
     * @param maxReq
     * @param winNs
     */
    public SlidingWindowLimiter(int maxReq, long winNs) {
      this.maxReq = maxReq;
      this.winNs = winNs;
      this.sliWinRef = new AtomicReference<>(new WindowLog(new long[0]));
    }

    /**
     * This implementation uses concurrent linked queue to fetch request 
     * check if exceeding window boundary per new request time(sliding)
     * and evict records to make space for new to allow.
     */
    @Override
    public boolean checkAccess() {
      while(true) {
        long now = System.nanoTime();
        long windowSlide = now - winNs;
        WindowLog current = sliWinRef.get();
        long[] currentTimestamps = current.timestamps();
        int expiredCount = 0;
        while(expiredCount < currentTimestamps.length && currentTimestamps[expiredCount] <= windowSlide) {
          expiredCount++;
        }
        int activeCount = currentTimestamps.length - expiredCount;
        if(activeCount >= maxReq) {
          return false;
        }
        long[] nextTimestamps = new long[ activeCount + 1 ];
        System.arraycopy(currentTimestamps, expiredCount, nextTimestamps, 0, activeCount);
        nextTimestamps[activeCount] = now;
        WindowLog next = new WindowLog(nextTimestamps);
        if(sliWinRef.compareAndSet(current, next)) {
          return true;
        }
      }
    }
  }

  /**
   * This implementation has token capacity, refillRatePerWin and record for BucketState and its AtomicReference
   * It calculates refillRatePerWindow to add new tokens in bucket and evict old or counter associated.
   * 
   * TokenBucketLimiter
   */
  static class TokenBucketLimiter implements RateLimiter {
    private final long capacity;
    private final double refillRatePerWin;
    private record BucketState(long tokens, long lastRefillTime) {}
    private final AtomicReference<BucketState> state;

    /**
     * Constructed with capacity, refillRatePerWin and bucket state with capacity to allow.
     * @param capacity
     * @param winNs
     */
    public TokenBucketLimiter(long capacity, long winNs) {
      this.capacity = capacity;
      this.refillRatePerWin = (double) capacity/winNs;
      this.state = new AtomicReference<>(new BucketState(capacity, System.nanoTime()));
    }

    /**
     * request gets allowed from duration after last refill which used to calculate
     * tokensToAdd from refillRatePerWin if exceeds defaults to capacity
     * if within capacity same get used with new bucket state
     * in case duration from last refill is not much to add any token it gets restricted.
     */
    @Override
    public boolean checkAccess() {
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