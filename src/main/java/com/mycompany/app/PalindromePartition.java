import java.util.*;

public class PalindromePartition {
    public static List<List<String>> partition(String s) {
        List<List<String>> res = new ArrayList<>();
        boolean[][] dp = new boolean[s.length()][s.length()];
        // Fill DP table for palindrome check optimization
        for (int i = 0; i < s.length(); i++) {
            for (int j = 0; j <= i; j++) {
                if (s.charAt(i) == s.charAt(j) && (i - j <= 2 || dp[j + 1][i - 1])) {
                    dp[j][i] = true;
                }
            }
        }
        dfs(res, new ArrayList<>(), dp, s, 0);
        return res;
    }

    private static void dfs(List<List<String>> res, List<String> temp, boolean[][] dp, String s, int start) {
        if (start == s.length()) {
            res.add(new ArrayList<>(temp));
            return;
        }
        for (int i = start; i < s.length(); i++) {
            if (dp[start][i]) {
                temp.add(s.substring(start, i + 1));
                dfs(res, temp, dp, s, i + 1);
                temp.remove(temp.size() - 1);
            }
        }
    }
}
