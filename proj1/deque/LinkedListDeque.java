package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Iterable<T> {
    private Node<T> sentinel;
    private int size;

    public LinkedListDeque() {
        // circular sentinel
        this.sentinel = new Node<T>(null, null, null);
        this.sentinel.next = this.sentinel;
        this.sentinel.prev = this.sentinel;
    }

    public void addFirst(T item) {
        this.size++;
        Node<T> newNode = new Node<>(item, this.sentinel, this.sentinel.next);
        this.sentinel.next.prev = newNode;
        this.sentinel.next = newNode;
    }

    public void addLast(T item) {
        this.size++;
        Node<T> newNode = new Node<>(item, this.sentinel.prev, this.sentinel);
        this.sentinel.prev.next = newNode;
        this.sentinel.prev = newNode;
    }


    public T removeLast() {
        if (this.isEmpty())
            return null;
        this.size--;
        T ret = this.sentinel.prev.item;
        this.sentinel.prev.prev.next = this.sentinel;
        this.sentinel.prev = this.sentinel.prev.prev;
        return ret;
    }

    public T removeFirst() {
        if (this.isEmpty())
            return null;
        this.size--;
        T ret = this.sentinel.next.item;
        this.sentinel.next.next.prev = this.sentinel;
        this.sentinel.next = this.sentinel.next.next;
        return ret;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        if (this.size() == 0)
            return true;
        return false;
    }

    /* Get Item at position of @index
     * Iterate through the deque */
    public T get(int index) {
        if (index + 1 > this.size())
            return null;
        Node<T> curr = this.sentinel.next;
        for (int i = 0; i < index; i++) {
            curr = curr.next;
        }
        return curr.item;
    }

    public void printDeque() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for(T item: this) {
            str.append(item);
            str.append(" ");
        }
        return str.toString();
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Node<T> curr = sentinel.next;
            int i = 0;

            @Override
            public boolean hasNext() {
                if (i < size())
                    return true;
                return false;
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
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof LinkedListDeque otherLLD) {
            if(this.size() != otherLLD.size())
                return false;
            Node<T> thisCurr = this.sentinel;
            Node otherCurr = otherLLD.sentinel;
            for (int i = 0; i < this.size(); i++) {
                if (!(thisCurr.next.item.equals(otherCurr.next.item)))
                    return false;
                thisCurr = thisCurr.next;
                otherCurr = otherCurr.next;
            }
            return true;
        }
        return false;
    }


    private class Node<T> {
        T item;
        Node<T> prev;
        Node<T> next;

        public Node(T item, Node<T> prev, Node<T> next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }
}
