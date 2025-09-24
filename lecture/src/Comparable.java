public interface Comparable<T> {
    /*
     * return a positive number if I am larger than o
     * return 0 if I am less than o
     * return a negative number if I am equal to o
     */
    public int compareTo(T o);
}
