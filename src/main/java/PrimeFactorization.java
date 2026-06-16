import java.util.Scanner;

public class PrimeFactorization {

    // Method to print all prime factors of a number
    public static void printPrimeFactors(long n) {
        if (n <= 1) {
            System.out.println("No prime factors for " + n);
            return;
        }

        // Handle negative numbers
        if (n < 0) {
            System.out.print("-1 ");
            n = Math.abs(n);
        }

        // Print the number of 2s that divide n
        while (n % 2 == 0) {
            System.out.print(2 + " ");
            n /= 2;
        }

        // n must be odd at this point, so skip even numbers
        for (long i = 3; i * i <= n; i += 2) {
            while (n % i == 0) {
                System.out.print(i + " ");
                n /= i;
            }
        }

        // If n is a prime number greater than 2
        if (n > 2) {
            System.out.print(n);
        }
        System.out.println();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter an integer: ");

        // Input validation
        if (!scanner.hasNextLong()) {
            System.out.println("Invalid input. Please enter a valid integer.");
            scanner.close();
            return;
        }

        long number = scanner.nextLong();
        System.out.print("Prime factors: ");
        printPrimeFactors(number);

        scanner.close();
    }
}
