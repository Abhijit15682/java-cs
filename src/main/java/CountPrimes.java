import java.util.Scanner;

public class CountPrimes {

    /**
     * Counts the number of prime numbers less than n using the Sieve of Eratosthenes.
     * @param n the upper limit (exclusive)
     * @return count of prime numbers less than n
     */
    public static int countPrimes(int n) {
        if (n <= 2) return 0; // No primes less than 2

        boolean[] isPrime = new boolean[n];
        // Initially assume all numbers >= 2 are prime
        for (int i = 2; i < n; i++) {
            isPrime[i] = true;
        }

        // Sieve process
        for (int i = 2; i * i < n; i++) {
            if (isPrime[i]) {
                for (int j = i * i; j < n; j += i) {
                    isPrime[j] = false;
                }
            }
        }

        // Count primes
        int count = 0;
        for (int i = 2; i < n; i++) {
            if (isPrime[i]) count++;
        }
        return count;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a non-negative integer n: ");
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter an integer.");
            scanner.close();
            return;
        }

        int n = scanner.nextInt();
        if (n < 0) {
            System.out.println("Please enter a non-negative integer.");
            scanner.close();
            return;
        }

        int result = countPrimes(n);
        System.out.println("Number of primes less than " + n + " is: " + result);

        scanner.close();
    }
}
