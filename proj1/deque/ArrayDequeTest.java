package deque;

import org.junit.Test;

import java.lang.reflect.Array;

import static org.junit.Assert.*;


/**
 * Performs some basic array list tests.
 */
public class ArrayDequeTest {

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {

        ArrayDeque<String> lld1 = new ArrayDeque<>();

        assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
        lld1.addFirst("front");

        // The && operator is the same as "and" in Python.
        // It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

        lld1.addLast("middle");
        assertEquals(2, lld1.size());

        lld1.addLast("back");
        assertEquals(3, lld1.size());

        System.out.println("Printing out deque: ");
        lld1.printDeque();
    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterward. */
    public void addRemoveTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        // should be empty
        assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

        lld1.addFirst(10);
        // should not be empty
        assertFalse("lld1 should contain 1 item", lld1.isEmpty());

        lld1.removeFirst();
        // should be empty
        assertTrue("lld1 should be empty after removal", lld1.isEmpty());
    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);
    }

    @Test
    /* Check if you can create Array Deque with different parameterized types*/
    public void multipleParamTest() {

        ArrayDeque<String> lld1 = new ArrayDeque<>();
        ArrayDeque<Double> lld2 = new ArrayDeque<>();
        ArrayDeque<Boolean> lld3 = new ArrayDeque<>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();
    }

    @Test
    /* check if null is return when removing from an empty ArrayDeque. */
    public void emptyNullReturnTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, lld1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, lld1.removeLast());

    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }

    }

    @Test
    /* Test get method ensure we get the right item */
    public void getOrderTest() {
        ArrayDeque<Integer> lld = new ArrayDeque<>();
        for (int i = 0; i < 100; i++) {
            lld.addLast(i);
        }
        for (int i = 99; i >= 0; i--) {
            assertEquals(i, (int) lld.get(i));
        }
    }

    @Test
    /* Test get method ensure we get the right item */
    public void equalsTest() {
        ArrayDeque<Integer> lld = new ArrayDeque<>();
        ArrayDeque<Integer> lldEqual = new ArrayDeque<>();
        ArrayDeque<Integer> lldNotEqualSameSize = new ArrayDeque<>();
        ArrayDeque<Integer> lldNotEqualNotSameSize = new ArrayDeque<>();
        ArrayDeque<String> lldNotSameType = new ArrayDeque<>();
        ArrayDeque<Integer> lldEmpty = new ArrayDeque<>();
        ArrayDeque<Integer> lldSameEmpty = new ArrayDeque<>();
        ArrayDeque<String> lldEmptyAndNotSameType = new ArrayDeque<>();

        for (int i = 0; i < 5; i++) {
            lld.addLast(i);
            lldEqual.addLast(i);
            lldNotEqualSameSize.addLast(i - 1);
            lldNotEqualNotSameSize.addLast(i);
            lldNotSameType.addLast(Integer.valueOf(i).toString());
        }
        lldNotEqualNotSameSize.addLast(100);
        // Test if tow separate lld with same value is equal
        assertTrue("lld with SAME VALUES should be equal",
                lld.equals(lldEqual) && lldEqual.equals(lld));
        // Test lld with different values but same size
        assertFalse("lld with DIFFERENT VALUES should not be equal",
                lld.equals(lldNotEqualSameSize) || lldNotEqualSameSize.equals(lld));
        // Test lld with different size
        assertFalse("lld with DIFFERENT SIZE should not be equal",
                lld.equals(lldNotEqualNotSameSize) || lldNotEqualNotSameSize.equals(lld));
        // Test lld with different type
        assertFalse("lld with DIFFERENT TYPE OF VALUE should NOT be equal",
                lldNotSameType.equals(lldEmpty) || lldNotSameType.equals(lld));
        // Test Empty lld
        assertFalse("empty lld with SAME TPYE should be equal",
                lld.equals(lldEmpty) || lldEmpty.equals(lld));
        // Test tow empty lld
        assertTrue("Tow empty lld should be equal", lldEmpty.equals(lldSameEmpty) && lldSameEmpty.equals(lldEmpty));
        // Is there any way to figure out the empty lld with different generic type?
        // Test empty llds with different Type
        // assertFalse(lldEmpty.equals(lldEmptyAndNotSameType) || lldEmptyAndNotSameType.equals(lldEmpty));
    }

    @Test
    /* Test for each loop and if it is in right order */
    public void testIterator() {
        ArrayDeque<Integer> lld = new ArrayDeque<>();
        int[] actual = new int[1000];

        for (int i = 999; i >= 0; i--) {
            lld.addLast(i);
            actual[(1000 - i) - 1] = i;
        }
        for (int i : lld) {
            assertEquals(actual[(1000 - i) - 1], i);
        }
    }

    @Test
    public void largeScaleResizeTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        for (int i = 0; i < 1000; i++) {
            ad.addLast(i);
            assertEquals((i + 1), ad.size());
            if (ad.getArrayLength() > 16)
                assertTrue("usage factor should always be at least 25%",
                        ad.getArrayLength() / 4 <= ad.size());
        }
        for (int i = 0; i < 1000; i++) {
            ad.addFirst(i);
            assertEquals((1000 + i + 1), ad.size());
            if (ad.getArrayLength() > 16)
                assertTrue("usage factor should always be at least 25%",
                        ad.getArrayLength() / 4 <= ad.size());
        }
        for (int i = 0; i < 1000; i++) {
            ad.removeFirst();
            assertEquals(2000 - (i + 1), ad.size());
            if (ad.getArrayLength() > 16)
                assertTrue("usage factor should always be at least 25%",
                        ad.getArrayLength() / 4 <= ad.size());
        }
        for (int i = 0; i < 1000; i++) {
            ad.removeFirst();
            assertEquals(1000 - (i + 1), ad.size());
            if (ad.getArrayLength() > 16)
                assertTrue("usage factor should always be at least 25%",
                        ad.getArrayLength() / 4 <= ad.size());
        }
    }

    @Test
    public void largeAddRemoveOrderTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        for (int i = 0; i < 1000; i++) {
            ad.addLast(i);
        }
        for (int i = 0; i < 1000; i++) {
            int ret = ad.removeFirst();
            assertEquals(ret, i);
        }
        for (int i = 0; i < 1000; i++) {
            ad.addFirst(i);
        }
        for (int i = 0; i < 1000; i++) {
            int ret = ad.removeLast();
            assertEquals(ret, i);
        }
    }
}
