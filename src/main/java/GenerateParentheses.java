import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GenerateParentheses {

    /**
     * Generates all combinations of well-formed parentheses.
     * @param n number of pairs of parentheses
     * @return list of valid combinations
     */
    public static List<String> generateParenthesis(int n) {
        List<String> result = new ArrayList<>();
        if (n <= 0) {
            return result; // No valid parentheses for non-positive n
        }
        backtrack(result, new StringBuilder(), 0, 0, n);
        return result;
    }

    /**
     * Backtracking helper method.
     * @param result list to store valid combinations
     * @param current current string being built
     * @param open number of '(' used so far
     * @param close number of ')' used so far
     * @param max total pairs allowed
     */
    private static void backtrack(List<String> result, StringBuilder current, int open, int close, int max) {
        // If the current string is complete, add to result
        if (current.length() == max * 2) {
            result.add(current.toString());
            return;
        }

        // Add '(' if we still have some left
        if (open < max) {
            current.append('(');
            backtrack(result, current, open + 1, close, max);
            current.deleteCharAt(current.length() - 1); // backtrack
        }

        // Add ')' if it won't break the validity
        if (close < open) {
            current.append(')');
            backtrack(result, current, open, close + 1, max);
            current.deleteCharAt(current.length() - 1); // backtrack
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter number of pairs of parentheses (1-8): ");
        
        try {
            int n = scanner.nextInt();
            if (n < 1 || n > 8) {
                System.out.println("Please enter a number between 1 and 8.");
                return;
            }

            List<String> combinations = generateParenthesis(n);
            System.out.println("All well-formed parentheses combinations:");
            for (String s : combinations) {
                System.out.println(s);
            }
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter an integer.");
        } finally {
            scanner.close();
        }
    }
}
