package com.company.app;

import java.util.Arrays;
import java.util.Scanner;

public class AnagramChecker {

    /**
     * Checks if two strings are anagrams of each other.
     * This method ignores case and spaces.
     *
     * @param str1 First string
     * @param str2 Second string
     * @return true if anagrams, false otherwise
     */
    public static boolean isAnagram(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return false; // Null strings can't be anagrams
        }

        // Remove spaces and convert to lowercase for case-insensitive comparison
        String s1 = str1.replaceAll("\\s+", "").toLowerCase();
        String s2 = str2.replaceAll("\\s+", "").toLowerCase();

        // Quick length check
        if (s1.length() != s2.length()) {
            return false;
        }

        // Convert to char arrays and sort
        char[] arr1 = s1.toCharArray();
        char[] arr2 = s2.toCharArray();
        Arrays.sort(arr1);
        Arrays.sort(arr2);

        // Compare sorted arrays
        return Arrays.equals(arr1, arr2);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Enter first string: ");
            String str1 = scanner.nextLine();

            System.out.print("Enter second string: ");
            String str2 = scanner.nextLine();

            if (isAnagram(str1, str2)) {
                System.out.println("✅ The strings are anagrams.");
            } else {
                System.out.println("❌ The strings are NOT anagrams.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
