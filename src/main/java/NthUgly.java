// This function divides a by greatest
// divisible power of b
class NthUgly {

    // This function divides a by greatest
    // divisible power of b
    static int maxDivide(int a, int b)
    {
        while (a % b == 0)
            a = a / b;

        return a;
    }

    // Function to check if a number is ugly or not
    static int isUgly(int val)
    {
        val = maxDivide(val, 2);
        val = maxDivide(val, 3);
        val = maxDivide(val, 5);

        return (val == 1) ? 1 : 0;
    }

    // Function to get the nth ugly number
    static int uglyNumber(int n)
    {
        int i = 1;

        // Ugly number count
        int count = 1;

        // Check for all integers until ugly
        // count becomes n
        while (n > count) {
            i++;
            if (isUgly(i) == 1)
                count++;
        }
        return i;
    }

    public static void main(String[] args)
    {
        int n = 10;
        System.out.println(uglyNumber(n));
    }
}