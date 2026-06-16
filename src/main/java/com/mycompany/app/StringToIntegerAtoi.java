public class StringToIntegerAtoi {

    /**
     * Converts a string to a 32-bit signed integer (similar to C's atoi).
     * Handles whitespace, optional sign, non-digit characters, and overflow.
     */
    public static int myAtoi(String s) {
        if (s == null) {
            return 0; // Null string returns 0
        }

        int i = 0, n = s.length();
        // 1. Skip leading whitespaces
        while (i < n && Character.isWhitespace(s.charAt(i))) {
            i++;
        }

        // If string is empty after trimming spaces
        if (i >= n) {
            return 0;
        }

        // 2. Handle optional sign
        int sign = 1;
        char firstChar = s.charAt(i);
        if (firstChar == '+' || firstChar == '-') {
            sign = (firstChar == '-') ? -1 : 1;
            i++;
        }

        // 3. Convert digits to integer
        long result = 0; // Use long to detect overflow before casting
        while (i < n && Character.isDigit(s.charAt(i))) {
            int digit = s.charAt(i) - '0';
            result = result * 10 + digit;

            // 4. Handle overflow/underflow
            if (sign == 1 && result > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            if (sign == -1 && -result < Integer.MIN_VALUE) {
                return Integer.MIN_VALUE;
            }
            i++;
        }

        return (int) (sign * result);
    }

    // Test the implementation
    public static void main(String[] args) {
        String[] testCases = {
            "42",
            "   -42",
            "4193 with words",
            "words and 987",
            "-91283472332",
            "+1",
            "   +0 123",
            null,
            ""
        };

        for (String test : testCases) {
            System.out.printf("Input: %-15s -> Output: %d%n", test, myAtoi(test));
        }
    }
}
