public class Exercise3 {
    /**
     * Returns the maximum value from m using a for loop.
     */
    public static int forMax(int[] m) {
        // assume all numbers greater than 0
        int max = 0;
        for (int i = 0; i < m.length; i++) {
            if (m[i] > max) {
                max = m[i];
            }
        }
        return max;
    }

    public static void main(String[] args) {
        int[] numbers = new int[]{9, 2, 15, 2, 22, 10, 6};
        System.out.println(forMax(numbers));
    }
}
