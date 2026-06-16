import java.util.ArrayList;
import java.util.List;

public class StringCombinations {

    public static void main(String[] args) {
        String input = "ABC";
        System.out.println("All combinations of '" + input + "':");
        List<String> permutations = getPermutations(input);
        permutations.forEach(System.out::println);
    }

    /*
    * With recursive call stacks select characters from 0 to length
    *   exclude indexed character and form remaining string
    *
    * */
    public static List<String> getPermutations(String str) {
        if (str.length() <= 1) { // Base case: if string length is 1, return the character in an array.
            return List.of(str);
        }
        List<String> results = new ArrayList<>();
        for (int i = 0; i < str.length(); i++) {
            char currentChar = str.charAt(i);
            // Remaining characters after removing currentChar
            String remainingChars = str.substring(0, i) + str.substring(i + 1);
            // Recursive call to get all permutations of remaining characters and then push in result
                /*
                *
                * */
            List<String> innerPermutations = getPermutations(remainingChars);
            // Combine picked character with each permutation of the rest
            for (String subPerm : innerPermutations) {
                results.add(currentChar + subPerm);
            }
        }
        return results;
    }
}


/*
* function getPermutations(str) {
  if (str.length <= 1) return [str];
  let results = [];
  for (let i = 0; i < str.length; i++) {
    const currentChar = str[i];
    const remainingChars = str.slice(0, i) + str.slice(i + 1);
    const innerPermutations = getPermutations(remainingChars);
    for (let subPerm of innerPermutations) {
      results.push(currentChar + subPerm);
    }
  }
  return results;
}
console.log(getPermutations("ABC"));
// Output: ["ABC", "ACB", "BAC", "BCA", "CAB", "CBA"]

*
* *
    public static void findCombinations(String str, String current, int index) {
        // Base Case: If we've considered all characters, print the current combination
        if (index == str.length()) {
            if (!current.isEmpty()) {
                System.out.println(current);
            }
            return;
        }

        // Recursive Step 1: Include the character at 'index'
        findCombinations(str, current + str.charAt(index), index + 1);

        // Recursive Step 2: Exclude the character at 'index'
        findCombinations(str, current, index + 1);
    }



* * */


