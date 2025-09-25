public interface Tree<T> {
    void insert(T item);
    boolean find(T item);
    T delete(T item);
}
