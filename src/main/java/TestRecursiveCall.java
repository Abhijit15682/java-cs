import java.util.ArrayList;
import java.util.List;

public class TestRecursiveCall {

    public static void main(String[] args) {
        String str = "ABC";
        List<String> result = new ArrayList<>();
        methodRecursive(null, str, result);
    }

    public static void methodRecursive(Character prefix, String remaining, List<String> result) {
        if(remaining != null && remaining.isBlank()) {
            return ;
        }

        for(int i = 0;  i < remaining.length(); i ++ ) {
            // iterating cursor character to be prefix.
            prefix = remaining.charAt(i);
            remaining = remaining.substring(0, i) + remaining.substring(i + 1 );
            System.out.println("remaining:" + remaining);
            System.out.println("prefix: "+ prefix);
            methodRecursive(prefix, remaining, result);
        }
    }
}
