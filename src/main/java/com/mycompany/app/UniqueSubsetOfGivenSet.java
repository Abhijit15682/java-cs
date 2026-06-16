import java.util.Arrays;
import java.util.ArrayList;

class UniqueSubsetOfGivenSet {

    // Helper function to find all unique subsets
    static void findSubsetsRec(int[] arr, int idx, 
        ArrayList<Integer> subset, ArrayList<ArrayList<Integer>> res) {

        // include current subset
        res.add(new ArrayList<>(subset));

        for (int i = idx; i < arr.length; i++) {

            // Skip duplicates at the same recursion level
            if (i > idx && arr[i] == arr[i - 1]) continue;

            subset.add(arr[i]);
            findSubsetsRec(arr, i + 1, subset, res);
            subset.remove(subset.size() - 1);
        }
    }

    static ArrayList<ArrayList<Integer>> findSubsets(int[] arr) {
       
        // sort to handle duplicates
        Arrays.sort(arr); 

        ArrayList<ArrayList<Integer>> res = new ArrayList<>();
        findSubsetsRec(arr, 0, new ArrayList<>(), res);

        return res; 
    }

    public static void main(String[] args) {
        int[] arr = {1, 2, 2};

        ArrayList<ArrayList<Integer>> result = findSubsets(arr);

        for (ArrayList<Integer> subset : result) {
            System.out.println(subset);
        }
    }
}