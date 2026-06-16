import java.util.ArrayList;

class NQueueProblem {

    // Function to check if it is safe to place
    static int isSafe(int[][] mat, int row, int col) {
        int n = mat.length;
        int i, j;

        // Check this col on upper side
        for (i = 0; i < row; i++)
            if (mat[i][col] == 1)
                return 0;

        // Check upper diagonal on left side
        for (i = row - 1, j = col - 1; i >= 0 && j >= 0; i--, j--)
            if (mat[i][j] == 1)
                return 0;

        // Check upper diagonal on right side
        for (i = row - 1, j = col + 1; j < n && i >= 0; i--, j++)
            if (mat[i][j] == 1)
                return 0;

        return 1;
    }

    // Recursive function to place queens
    static void placeQueens(int row, int[][] mat, 
                ArrayList<ArrayList<Integer>> result) {
        int n = mat.length;

        // base case: If all queens are placed
        if (row == n) {

            // store current solution
            ArrayList<Integer> ans = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (mat[i][j] == 1) {
                        ans.add(j + 1);
                    }
                }
            }
            result.add(ans);
            return;
        }

        // Consider the row and try placing
        // queen in all columns one by one
        for (int i = 0; i < n; i++) {

            // Check if the queen can be placed
            if (isSafe(mat, row, i) == 1) {
                mat[row][i] = 1;
                placeQueens(row + 1, mat, result);

                // backtrack
                mat[row][i] = 0;
            }
        }
    }

    // Function to find all solutions
    static ArrayList<ArrayList<Integer>> nQueen(int n) {

        // Initialize the board
        int[][] mat = new int[n][n];
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();

        // Place queens
        placeQueens(0, mat, result);

        return result;
    }

    public static void main(String[] args) {
        int n = 4;
        ArrayList<ArrayList<Integer>> result = nQueen(n);
        for (ArrayList<Integer> ans : result) {
            for (int i : ans) {
                System.out.print(i + " ");
            }
            System.out.println();
        }
    }
}