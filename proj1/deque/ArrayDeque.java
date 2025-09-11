package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] items;
    private int size;

    // indicate the position of first item, which would not be at 0;
    // for add and remove must take constant time, except during resizing operations.
    private int front;

    /* Some invariants:
     *  this.size + this.front - 1 is the position of the last item
     *  this.front is the position of the first item */

    public ArrayDeque() {
        // the default length of items is 8
        this.items = (T[]) new Object[8];
        this.size = 0;
    }

    public int getArrayLength() {
        return this.items.length;
    }

    @Override
    public void addFirst(T item) {
        if (this.front == 0)
            resize(this.items.length * 2);
        this.items[this.front - 1] = item;
        this.size++;
        this.front--;
    }

    @Override
    public void addLast(T item) {
        if (this.front + this.size() >= this.items.length) {
            resize(this.items.length * 2);
        }
        this.items[this.front + this.size()] = item;
        this.size++;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public String toString() {
        StringBuilder stb = new StringBuilder();
        for (int i = this.front; i < this.front + this.size(); i++) {
            stb.append(this.items[i]);
            stb.append(" ");
        }
        return stb.toString();
    }

    @Override
    public void printDeque() {
        System.out.println(this);
    }

    @Override
    public T removeLast() {
        if (this.size() == 0)
            return null;
        T ret = this.items[this.front + this.size() - 1];
        this.size--;
        if (this.items.length > 16 && this.size() <= (this.items.length / 4)) {
            resize(this.items.length / 2);
        }
        return ret;
    }

    @Override
    public T removeFirst() {
        if (this.size() == 0)
            return null;
        T ret = this.items[this.front];
        this.front++;
        this.size--;
        if (this.items.length > 16 && this.size() <= (this.items.length / 4)) {
            resize(this.items.length / 2);
        }
        return ret;
    }

    @Override
    public T get(int index) {
        if (index + 1 > this.size())
            return null;
        return this.items[this.front + index];
    }

    /* make a brand-new array with length changed, according to @capacity
     * to replace the items array, and change the this.first pointer */
    private void resize(int capacity) {
        T[] newItems = (T[]) new Object[capacity];
        int newFront = (capacity / 2) - 1;
        System.arraycopy(this.items, this.front, newItems, newFront, this.size);
        this.front = newFront;
        this.items = newItems;
    }

    @Override
    /* o is considered equal if it is a Deque and if it contains the same contents
     * (as goverened by the generic T’s equals method) in the same order. */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof ArrayDeque otherAd) {

            if (this.size() != otherAd.size())
                return false;

            // compare each the item of tow lld
            for (int i = 0; i < this.size(); i++) {
                if (!this.get(i).equals(otherAd.get(i)))
                    return false;
            }
            // Here is a problem. there is no way to
            // figure out whether generic type T is same,
            // and if the size is 0, the method would return true directly
            return true;
        }
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int pos = 0;

            @Override
            public boolean hasNext() {
                return front + pos < size;
            }

            @Override
            public T next() {
                T ret = items[pos];
                pos++;
                return ret;
            }
        };
    }
}
