package timingtest;

/** Array based list.
 *  @author Josh Hug
 */

//         0 1  2 3 4 5 6 7
// items: [6 9 -1 2 0 0 0 0 ...]
// size: 5

/* Invariants:
 addLast: The next item we want to add, will go into position size
 getLast: The item we want to return is in position size - 1
 size: The number of items in the list should be size.
*/

public class AList<Item> {
    private Item[] items;
    private int size;

    /** Creates an empty list. */
    public AList() {
        items = (Item[]) new Object[100];
        size = 0;
    }

    /** Resizes the underlying array to the target capacity. */
    private void resize(int capacity) {
        Item[] a = (Item[]) new Object[capacity];
        System.arraycopy(items, 0, a, 0, size);
        items = a;
    }

    /** Inserts X into the back of the list. */
    public void addLast(Item x) {
        if (size == items.length) {
            resize(size + 1);
            // resize((int)(size * 1.01));
            /*
                additive strategy
                         N     time (s)        # ops  microsec/op
                ------------------------------------------------------------
                        1000         0.00         1000         1.00
                        2000         0.00         2000         0.50
                        4000         0.00         4000         1.00
                        8000         0.01         8000         1.75
                       16000         0.04        16000         2.56
                       32000         0.14        32000         4.44
                       64000         0.45        64000         7.05
                      128000         1.46       128000        11.41

              multiplicative strategy is much better than additive strategy
                       N     time (s)        # ops  microsec/op
            ------------------------------------------------------------
                    1000         0.00         1000         1.00
                    2000         0.00         2000         0.00
                    4000         0.00         4000         0.00
                    8000         0.00         8000         0.13
                   16000         0.00        16000         0.06
                   32000         0.00        32000         0.13
                   64000         0.00        64000         0.05
                  128000         0.01       128000         0.05
             */
        }

        items[size] = x;
        size = size + 1;
    }

    /** Returns the item from the back of the list. */
    public Item getLast() {
        return items[size - 1];
    }
    /** Gets the ith item in the list (0 is the front). */
    public Item get(int i) {
        return items[i];
    }

    /** Returns the number of items in the list. */
    public int size() {
        return size;
    }

    /** Deletes item from back of the list and
      * returns deleted item. */
    public Item removeLast() {
        Item x = getLast();
        items[size - 1] = null;
        size = size - 1;
        return x;
    }
}
