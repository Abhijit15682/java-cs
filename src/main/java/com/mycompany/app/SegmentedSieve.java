import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SegmentedSieve {

    // Simple Sieve to generate all primes up to sqrt(R)
    private static List<Integer> simpleSieve(int limit) {
        boolean[] isPrime = new boolean[limit + 1];
        for (int i = 2; i <= limit; i++) {
            isPrime[i] = true;
        }

        for (int p = 2; p * p <= limit; p++) {
            if (isPrime[p]) {
                for (int multiple = p * p; multiple <= limit; multiple += p) {
                    isPrime[multiple] = false;
                }
            }
        }

        List<Integer> primes = new ArrayList<>();
        for (int i = 2; i <= limit; i++) {
            if (isPrime[i]) {
                primes.add(i);
            }
        }
        return primes;
    }

    // Segmented Sieve to find primes in range [L, R]
    public static List<Integer> segmentedSieve(long L, long R) {
        if (L > R) {
            throw new IllegalArgumentException("Invalid range: L must be <= R");
        }
        if (L < 2) L = 2; // No primes below 2

        int limit = (int) Math.sqrt(R) + 1;
        List<Integer> basePrimes = simpleSieve(limit);

        boolean[] isPrimeRange = new boolean[(int) (R - L + 1)];
        for (int i = 0; i < isPrimeRange.length; i++) {
            isPrimeRange[i] = true;
        }

        // Mark non-primes in the range
        for (int prime : basePrimes) {
            long start = Math.max((long) prime * prime, ((L + prime - 1) / prime) * prime);
            for (long j = start; j <= R; j += prime) {
                isPrimeRange[(int) (j - L)] = false;
            }
        }

        // Collect primes from the range
        List<Integer> primesInRange = new ArrayList<>();
        for (int i = 0; i < isPrimeRange.length; i++) {
            if (isPrimeRange[i]) {
                primesInRange.add((int) (i + L));
            }
        }
        return primesInRange;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            System.out.print("Enter L and R: ");
            long L = sc.nextLong();
            long R = sc.nextLong();

            List<Integer> primes = segmentedSieve(L, R);
            System.out.println("Primes in range [" + L + ", " + R + "]: " + primes);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            sc.close();
        }
    }
}
