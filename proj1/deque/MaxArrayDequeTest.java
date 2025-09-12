package deque;

import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.*;

public class MaxArrayDequeTest {
    static Comparator<Integer> intCmp = new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1 - o2;
        }
    };

    @Test
    public void MaxIntegerNormalTest() {
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<>(MaxArrayDequeTest.intCmp);
        for (int i = 0; i < 100; i++) {
            mad.addLast(i);
        }
        for (int i = 999; i >= 0; i--) {
            mad.addFirst(i);
        }
        assertEquals("The max value should be 999", 999, (int) mad.max());
        assertEquals("The max value should be 0", 0, (int) mad.max(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        }));
        for (int i = 0; i < 999; i++) {
            mad.removeFirst();
            assertEquals(999, (int) mad.max());
        }
        mad.removeFirst();
        for (int i = 0; i < 99; i++) {
            assertEquals(99 - i, (int) mad.max());
            mad.removeLast();
        }
    }

    @Test
    public void MaxIntegerEmptyTest() {
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<>(MaxArrayDequeTest.intCmp);
        assertNull("The max value should be null", mad.max());
    }

    @Test
    public void MaxSameValueTest() {
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<>(MaxArrayDequeTest.intCmp);
        for (int i = 0; i < 100; i++) {
            mad.addFirst(10);
            assertEquals(10, (int) mad.max());
        }
    }
}
