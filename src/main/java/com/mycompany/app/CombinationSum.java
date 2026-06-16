import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CombinationSum {

    // Main function to find all unique combinations
    public static List<List<Integer>> combinationSum(int[] candidates, int target) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(candidates); // Sorting helps avoid duplicates and optimize search
        backtrack(candidates, target, 0, new ArrayList<>(), result);
        return result;
    }

    // Backtracking helper function
    private static void backtrack(int[] candidates, int target, int start,
                                   List<Integer> current, List<List<Integer>> result) {
        if (target == 0) {
            // Found a valid combination
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < candidates.length; i++) {
            if (candidates[i] > target) {
                break; // No need to continue if number exceeds target
            }
            current.add(candidates[i]);
            // Allow reuse of the same element, so pass 'i' instead of 'i+1'
            backtrack(candidates, target - candidates[i], i, current, result);
            current.remove(current.size() - 1); // Backtrack
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try {
            // Input array
            System.out.print("Enter number of elements: ");
            int n = sc.nextInt();
            if (n <= 0) {
                System.out.println("Array size must be positive.");
                return;
            }

            int[] candidates = new int[n];
            System.out.println("Enter " + n + " positive integers:");
            for (int i = 0; i < n; i++) {
                candidates[i] = sc.nextInt();
                if (candidates[i] <= 0) {
                    System.out.println("Only positive integers are allowed.");
                    return;
                }
            }

            // Input target
            System.out.print("Enter target sum: ");
            int target = sc.nextInt();
            if (target <= 0) {
                System.out.println("Target must be a positive integer.");
                return;
            }

            // Compute and display results
            List<List<Integer>> combinations = combinationSum(candidates, target);
            System.out.println("Unique combinations that sum to " + target + ":");
            if (combinations.isEmpty()) {
                System.out.println("No combination found.");
            } else {
                for (List<Integer> combo : combinations) {
                    System.out.println(combo);
                }
            }

        } catch (Exception e) {
            System.out.println("Invalid input. Please enter integers only.");
        } finally {
            sc.close();
        }
    }
}
