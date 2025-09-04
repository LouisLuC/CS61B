package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.checkerframework.checker.units.qual.A;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> anr = new AListNoResizing<>();
        BuggyAList<Integer> ba = new BuggyAList<>();

        for (int i = 4; i < 7; i++) {
            anr.addLast(i);
            ba.addLast(i);
        }

        // test if size is equaled
        assertEquals(anr.size(), ba.size());

        for (int i = 0; i < 3; i++) {
            assertEquals(ba.removeLast(), anr.removeLast());
        }
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> BL = new BuggyAList<>();
        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                BL.addLast(randVal);
                L.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                assertEquals(size, BL.size());
                System.out.println("size: " + size);
            } else if (operationNumber == 2) {
                if (L.size() == 0 || BL.size() == 0) {
                    System.out.println("Pass getLast() because the size of one of Lists is 0");
                    assertEquals(L.size(), BL.size());
                    continue;
                }
                int last = L.getLast();
                assertEquals(last, (int) BL.getLast());
                System.out.println("getLast() -> " + last);
            } else if (operationNumber == 3) {
                // removeLast
                if (L.size() == 0 || BL.size() == 0) {
                    System.out.println("Pass removeLast() because the size of one of Lists is 0");
                    assertEquals(L.size(), BL.size());
                    continue;
                }
                int removed = L.removeLast();
                assertEquals(removed, (int) BL.removeLast());
                System.out.println("removeLast() -> " + removed);
            }
        }
    }
}
