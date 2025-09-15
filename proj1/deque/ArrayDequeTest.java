package deque;

import org.junit.Test;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import static org.junit.Assert.*;


/**
 * Performs some basic array list tests.
 */
public class ArrayDequeTest {
    public static int getArrayLength(ArrayDeque ad) {
        try {
            Field itemsField = ad.getClass().getDeclaredField("items");
            itemsField.setAccessible(true);
            Object[] items = (Object[]) itemsField.get(ad);
            return items.length;
        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {

        ArrayDeque<String> ad1 = new ArrayDeque<>();

        assertTrue("A newly initialized LLDeque should be empty", ad1.isEmpty());
        ad1.addFirst("front");

        // The && operator is the same as "and" in Python.
        // It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, ad1.size());
        assertFalse("ad1 should now contain 1 item", ad1.isEmpty());

        ad1.addLast("middle");
        assertEquals(2, ad1.size());

        ad1.addLast("back");
        assertEquals(3, ad1.size());

        System.out.println("Printing out deque: ");
        ad1.printDeque();
    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterward. */
    public void addRemoveTest() {

        ArrayDeque<Integer> ad1 = new ArrayDeque<>();
        // should be empty
        assertTrue("ad1 should be empty upon initialization", ad1.isEmpty());

        ad1.addFirst(10);
        // should not be empty
        assertFalse("ad1 should contain 1 item", ad1.isEmpty());

        ad1.removeFirst();
        // should be empty
        assertTrue("ad1 should be empty after removal", ad1.isEmpty());
    }

    @Test
    /** Adds some item, then removes them, and ensures that dll is empty afterward. */
    public void addAndRemoveToEmptyTest() {

        ArrayDeque<Integer> ad = new ArrayDeque<>();
        // should be empty
        assertTrue("ad1 should be empty upon initialization", ad.isEmpty());

        for (int i = 0; i < 20; i++) {
            ad.addFirst(i);
        }
        if (getArrayLength(ad) > 16)
            assertTrue("usage factor should always be at least 25%",
                    getArrayLength(ad) / 4 <= ad.size());
        // should  be 20
        assertEquals("ad should contain 20 item", 20, ad.size());

        for (int i = 0; i < 20; i++) {
            ad.removeLast();
        }

        assertTrue("usage factor should be lower than 16 when it's empty",
                getArrayLength(ad) <= 16);
        // should be empty
        assertTrue("ad1 should be empty after removal", ad.isEmpty());
    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {

        ArrayDeque<Integer> ad1 = new ArrayDeque<>();
        ad1.addFirst(3);

        ad1.removeLast();
        ad1.removeFirst();
        ad1.removeLast();
        ad1.removeFirst();

        int size = ad1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);
    }

    @Test
    /* Check if you can create Array Deque with different parameterized types*/
    public void multipleParamTest() {

        ArrayDeque<String> ad1 = new ArrayDeque<>();
        ArrayDeque<Double> ad2 = new ArrayDeque<>();
        ArrayDeque<Boolean> ad3 = new ArrayDeque<>();

        ad1.addFirst("string");
        ad2.addFirst(3.14159);
        ad3.addFirst(true);

        String s = ad1.removeFirst();
        double d = ad2.removeFirst();
        boolean b = ad3.removeFirst();
    }

    @Test
    /* check if null is return when removing from an empty ArrayDeque. */
    public void emptyNullReturnTest() {

        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,",
                null, ad1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,",
                null, ad1.removeLast());

    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {

        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 1000000; i++) {
            ad1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) ad1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) ad1.removeLast(), 0.0);
        }

    }

    @Test
    /* Test get method ensure we get the right item */
    public void getOrderTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        for (int i = 0; i < 100; i++) {
            ad.addLast(i);
        }
        for (int i = 99; i >= 0; i--) {
            assertEquals(i, (int) ad.get(i));
        }
    }

    @Test
    /* Test get method ensure we get the right item */
    public void equalsTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        ArrayDeque<Integer> adEqual = new ArrayDeque<>();
        ArrayDeque<Integer> adNotEqualSameSize = new ArrayDeque<>();
        ArrayDeque<Integer> adNotEqualNotSameSize = new ArrayDeque<>();
        ArrayDeque<String> adNotSameType = new ArrayDeque<>();
        ArrayDeque<Integer> adEmpty = new ArrayDeque<>();
        ArrayDeque<Integer> adSameEmpty = new ArrayDeque<>();
        ArrayDeque<String> adEmptyAndNotSameType = new ArrayDeque<>();
        LinkedListDeque<Integer> lldWithEqual = new LinkedListDeque<>();
        LinkedListDeque<Integer> lldNotEqual = new LinkedListDeque<>();

        for (int i = 0; i < 5; i++) {
            ad.addLast(i);
            adEqual.addLast(i);
            lldWithEqual.addLast(i);
            lldNotEqual.addLast(i - 1);
            adNotEqualSameSize.addLast(i - 1);
            adNotEqualNotSameSize.addLast(i);
            adNotSameType.addLast(Integer.valueOf(i).toString());
        }
        adNotEqualNotSameSize.addLast(100);
        // Test if tow separate ad with same value is equal
        assertTrue("ad with SAME VALUES should be equal",
                ad.equals(adEqual) && adEqual.equals(ad));
        // Test ad with different values but same size
        assertFalse("ad with DIFFERENT VALUES should not be equal",
                ad.equals(adNotEqualSameSize) || adNotEqualSameSize.equals(ad));
        // Test ad with different size
        assertFalse("ad with DIFFERENT SIZE should not be equal",
                ad.equals(adNotEqualNotSameSize) || adNotEqualNotSameSize.equals(ad));
        // Test ad with different type
        assertFalse("ad with DIFFERENT TYPE OF VALUE should NOT be equal",
                adNotSameType.equals(adEmpty) || adNotSameType.equals(ad));
        // Test Empty ad
        assertFalse("empty ad with SAME TPYE should be equal",
                ad.equals(adEmpty) || adEmpty.equals(ad));
        // Test tow empty ad
        assertTrue("Tow empty ad should be equal",
                adEmpty.equals(adSameEmpty) && adSameEmpty.equals(adEmpty));
        // Is there any way to figure out the empty ad with different generic type?
        // Test empty ads with different Type
        // assertFalse(adEmpty.equals(adEmptyAndNotSameType) || adEmptyAndNotSameType.equals(adEmpty));
        assertTrue("LLD and AD with same items should be equal.",
                lldWithEqual.equals(ad) && ad.equals(lldWithEqual));
        assertFalse("LLD and AD with same items should be equal.",
                lldNotEqual.equals(ad) || ad.equals(lldNotEqual));
    }

    @Test
    /* Test for each loop and if it is in right order */
    public void testIterator() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        int[] actual = new int[1000];

        for (int i = 999; i >= 0; i--) {
            ad.addLast(i);
            actual[(1000 - i) - 1] = i;
        }
        for (int i : ad) {
            assertEquals(actual[(1000 - i) - 1], i);
        }
    }

    @Test
    public void largeScaleResizeTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        for (int i = 0; i < 1000; i++) {
            ad.addLast(i);
            assertEquals((i + 1), ad.size());
            if (getArrayLength(ad) > 16)
                assertTrue("usage factor should always be at least 25%",
                        getArrayLength(ad) / 4 <= ad.size());
        }
        for (int i = 0; i < 1000; i++) {
            ad.addFirst(i);
            assertEquals((1000 + i + 1), ad.size());
            if (getArrayLength(ad) > 16)
                assertTrue("usage factor should always be at least 25%",
                        getArrayLength(ad) / 4 <= ad.size());
        }
        for (int i = 0; i < 1000; i++) {
            ad.removeFirst();
            assertEquals(2000 - (i + 1), ad.size());
            if (getArrayLength(ad) > 16)
                assertTrue("usage factor should always be at least 25%",
                        getArrayLength(ad) / 4 <= ad.size());
        }
        for (int i = 0; i < 1000; i++) {
            ad.removeFirst();
            assertEquals(1000 - (i + 1), ad.size());
            if (getArrayLength(ad) > 16)
                assertTrue("usage factor should always be at least 25%",
                        getArrayLength(ad) / 4 <= ad.size());
        }

        assertEquals("size should be 0 now", 0, ad.size());
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
