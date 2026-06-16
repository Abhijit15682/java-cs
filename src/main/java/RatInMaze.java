import java.util.*;

public class RatInMaze {
    public static ArrayList<String> findPath(int[][] m, int n) {
        ArrayList<String> ans = new ArrayList<>();
        if (m[0][0] == 0 || m[n - 1][n - 1] == 0) return ans;
        boolean[][] vis = new boolean[n][n];
        dfs(0, 0, m, n, ans, "", vis);
        return ans;
    }

    private static void dfs(int i, int j, int[][] m, int n, ArrayList<String> ans, String move, boolean[][] vis) {
        if (i == n - 1 && j == n - 1) {
            ans.add(move);
            return;
        }
        // Downward
        if (i + 1 < n && !vis[i + 1][j] && m[i + 1][j] == 1) {
            vis[i][j] = true;
            dfs(i + 1, j, m, n, ans, move + 'D', vis);
            vis[i][j] = false;
        }
        // Left
        if (j - 1 >= 0 && !vis[i][j - 1] && m[i][j - 1] == 1) {
            vis[i][j] = true;
            dfs(i, j - 1, m, n, ans, move + 'L', vis);
            vis[i][j] = false;
        }
        // Right
        if (j + 1 < n && !vis[i][j + 1] && m[i][j + 1] == 1) {
            vis[i][j] = true;
            dfs(i, j + 1, m, n, ans, move + 'R', vis);
            vis[i][j] = false;
        }
        // Upward
        if (i - 1 >= 0 && !vis[i - 1][j] && m[i - 1][j] == 1) {
            vis[i][j] = true;
            dfs(i - 1, j, m, n, ans, move + 'U', vis);
            vis[i][j] = false;
        }
    }
}
