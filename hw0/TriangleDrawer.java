/*
Homework0: Creative Exercise 1b
 */
public class TriangleDrawer {
    public static void drawTriangle(int N) {
        for (int i = 0; i < N; i++) {
            System.out.println("*".repeat(i + 1));
        }
    }

    public static void main(String[] args) {
        drawTriangle(10);
    }
}