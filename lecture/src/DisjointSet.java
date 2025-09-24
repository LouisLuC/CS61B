public interface DisjointSet<T> {
    void connect(T i1, T i2);
    boolean isConnect(T i1, T i2);
}
