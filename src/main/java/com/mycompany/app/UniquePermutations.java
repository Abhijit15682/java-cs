import java.util.HashSet;
import java.util.Set;

public class UniquePermutations {

    public static void main(String[] args) {
        String input = "ABC";
        Set<String> uniqueResult = new HashSet<>();
        
        generateUniquePermutations("", input, uniqueResult);
        
        System.out.println("Unique Permutations: " + uniqueResult);
    }

    public static void generateUniquePermutations(String prefix, String remaining, Set<String> result) {
        int n = remaining.length();
        if (n == 0) {
            System.out.println("------------ no remaining: prefix added in result." + prefix);
            result.add(prefix);
            return;
        }

        for (int i = 0; i < n; i++) {
            char currentChar = remaining.charAt(i);
            String nextRemaining = remaining.substring(0, i) + remaining.substring(i + 1, n);
            System.out.println("remaining:" + remaining + " prefix: "+ prefix + " currentChar: index, ->"+ currentChar + " ," + i);
            // recursively shift elements to prefix with iterating character.
            // and exclude it from remaining till remaining becomes empty.
            generateUniquePermutations(prefix + currentChar, nextRemaining, result);
        }
    }

    /*
    *                      ("", "ABC")
                 /              |              \
                /               |               \
         ("A", "BC")       ("B", "AC")       ("C", "AB")
          /       \         /       \         /       \
     ("AB","C") ("AC","B") ("BA","C") ("BC","A") ("CA","B") ("CB","A")

        |          |          |          |          |          |
     ("ABC","") ("ACB","") ("BAC","") ("BCA","") ("CAB","") ("CBA","")

        |          |          |          |          |          |
      [ABC]      [ACB]      [BAC]      [BCA]      [CAB]      [CBA]
    *
    * */
}
