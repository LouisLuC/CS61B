package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    // Intellij suggests sentinel to be final
    // sentinel won't be changed
    private final Node<T> sentinel;
    private int size;

    /* Double Linked Node */
    private static class Node<T> {
        T item;
        Node<T> prev;
        Node<T> next;

        Node(T item, Node<T> prev, Node<T> next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }


    public LinkedListDeque() {
        // circular sentinel
        this.sentinel = new Node<T>(null, null, null);
        // sentinel.next links the first item, sentinel.prev links the last item
        this.sentinel.next = this.sentinel;
        this.sentinel.prev = this.sentinel;
    }

    @Override
    public void addFirst(T item) {
        this.size++;
        Node<T> newNode = new Node<>(item, this.sentinel, this.sentinel.next);
        this.sentinel.next.prev = newNode;
        this.sentinel.next = newNode;
    }

    @Override
    public void addLast(T item) {
        this.size++;
        Node<T> newNode = new Node<>(item, this.sentinel.prev, this.sentinel);
        this.sentinel.prev.next = newNode;
        this.sentinel.prev = newNode;
    }


    @Override
    public T removeLast() {
        if (this.isEmpty()) {
            return null;
        }
        this.size--;
        T ret = this.sentinel.prev.item;
        this.sentinel.prev.prev.next = this.sentinel;
        this.sentinel.prev = this.sentinel.prev.prev;
        return ret;
    }

    @Override
    public T removeFirst() {
        if (this.isEmpty()) {

            return null;
        }
        this.size--;
        T ret = this.sentinel.next.item;
        this.sentinel.next.next.prev = this.sentinel;
        this.sentinel.next = this.sentinel.next.next;
        return ret;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    /* Get Item at position of @index
     * Iterate through the deque */
    public T get(int index) {
        if (index + 1 > this.size()) {
            return null;
        }
        Node<T> curr = this.sentinel.next;
        for (int i = 0; i < index; i++) {
            curr = curr.next;
        }
        return curr.item;
    }

    private T getRecursiveByNode(Node<T> node, int index) {
        if (index == 0) {
            return node.item;
        } else if (node == this.sentinel) {
            return null;
        }
        return getRecursiveByNode(node.next, index - 1);
    }

    public T getRecursive(int index) {
        return getRecursiveByNode(this.sentinel.next, index);
    }

    @Override
    public void printDeque() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (T item : this) {
            str.append(item);
            str.append(" ");
        }
        return str.toString();
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Node<T> curr = sentinel.next;
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < size();
            }

            @Override
            public T next() {
                T ret = curr.item;
                this.i++;
                this.curr = curr.next;
                return ret;
            }
        };
    }

    @Override
    /* o is considered equal if it is a Deque and if it contains the same contents
     * (as goverened by the generic T’s equals method) in the same order. */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof LinkedListDeque) {
            LinkedListDeque otherLLD = (LinkedListDeque) obj;
            if (this.size() != otherLLD.size()) {
                return false;
            }

            // compare each the item of tow lld
            Node<T> thisCurr = this.sentinel;
            Node otherCurr = otherLLD.sentinel;
            for (int i = 0; i < this.size(); i++) {
                if (!(thisCurr.next.item.equals(otherCurr.next.item))) {
                    return false;
                }
                thisCurr = thisCurr.next;
                otherCurr = otherCurr.next;
            }
            // Here is a problem. there is no way to
            // figure out whether generic type T is same,
            // and if the size is 0, the method would return true directly
            return true;
        }
        return false;
    }
}
