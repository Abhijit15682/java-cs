import java.util.*;

public class AmicablePairs {
    public static List<int[]> findAmicablePairs(int limit) {
        int[] divSums = new int[limit + 1];
        
        // Sieve-like approach to calculate sum of proper divisors
        for (int i = 1; i <= limit / 2; i++) {
            for (int j = 2 * i; j <= limit; j += i) {
                divSums[j] += i;
            }
        }
        
        List<int[]> pairs = new ArrayList<>();
        for (int i = 2; i <= limit; i++) {
            int j = divSums[i];
            if (j > i && j <= limit && divSums[j] == i) {
                pairs.add(new int[]{i, j});
            }
        }
        return pairs;
    }
}
