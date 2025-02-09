class CreativeExercise1a {
    public static void main(String[] args) {
        int lines = 5;  // how many lines
        int asteriskNum = 1;
        while (lines > 0) {
            System.out.println("*".repeat(asteriskNum++));
            lines--;
        }
    }
}