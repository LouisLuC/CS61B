package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> comparator) {
        super();
        this.comparator = comparator;
    }

    public T max() {
        return this.max(this.comparator);
    }

    public T max(Comparator<T> c) {
        if (this.isEmpty()) {
            return null;
        }

        int maxPos = 0;
        for (int i = 0; i < this.size(); i++) {
            if (c.compare(this.get(i), this.get(maxPos)) > 0) {
                maxPos = i;
            }
        }
        return this.get(maxPos);
    }
}
