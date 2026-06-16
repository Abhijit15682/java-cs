

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class LongestSubstringWithoutRepeating {

    // Method to find the length of the longest substring without repeating characters
    public static int lengthOfLongestSubstring(String s) {
        if (s == null) {
            throw new IllegalArgumentException("Input string cannot be null");
        }

        Set<Character> window = new HashSet<>();
        int left = 0, maxLength = 0;

        // Sliding window approach
        for (int right = 0; right < s.length(); right++) {
            char currentChar = s.charAt(right);

            // If character already exists in the window, shrink from the left
            while (window.contains(currentChar)) {
                window.remove(s.charAt(left));
                left++;
            }

            // Add current character to the window
            window.add(currentChar);

            // Update max length
            maxLength = Math.max(maxLength, right - left + 1);
        }

        return maxLength;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a string: ");
        String input = scanner.nextLine();

        // Validate input 
        if (input.trim().isEmpty()) {
            System.out.println("Empty string provided. Length is 0.");
        } else {
            try {
                int result = lengthOfLongestSubstring(input);
                System.out.println("Length of the longest substring without repeating characters: " + result);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
