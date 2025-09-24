
public class DLList<T> implements List<T> {

    @Override
    public T get(int i) {
        if (i + 1 > this.size()) {
            return null;
        }
        Node<T> curr = this.sentinel.next;
        int j = 0;
        while (j < i) {
            curr = curr.next;
            j++;
        }
        return curr.item;
    }

    @Override
    public T getLast() {
        return null;
    }

    @Override
    public T getFirst() {
        return null;
    }

    @Override
    public void addLast(T x) {

    }

    @Override
    public void addFirst(T x) {

    }

    @Override
    public void insert(T x, int pos) {

    }

    @Override
    public T removeLast() {
        return null;
    }

    @Override
    public T removeFirst() {
        return null;
    }

    @Override
    public int size() {
        return this.size;
    }

    private class Node<T> {
        Node<T> prev;
        Node<T> next;
        T item;

        Node(T item, Node<T> prev, Node<T> next) {
            this.item = item;
            this.next = next;
            this.prev = prev;
        }
    }

    private Node<T> sentinel;
    private int size;

    public DLList() {
        this.sentinel = new Node<T>(null, null, null);
    }

    ;
}
