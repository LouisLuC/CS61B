public class Exercise2 {
    /**
     * Returns the maximum value from m.
     */
    public static int max(int[] m) {
        // assume that all the numbers are greater than 0
        int max = 0;
        int i = 0;
        while (i < m.length) {
            if (m[i] > max) max = m[i];
            i++;
        }
        return max;
    }

    public static void main(String[] args) {
        int[] numbers = new int[]{9, 2, 15, 2, 2, 10, 6};
        System.out.println(max(numbers));
    }
}
