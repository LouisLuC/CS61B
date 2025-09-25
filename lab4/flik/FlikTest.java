package flik;

import flik.Flik;

import static org.junit.Assert.*;

import org.junit.Test;

public class FlikTest {
    @Test
    public void integerUtil500Test() {
        for (int i = 0; i < 500; i++) {
            int j = i;
            assertTrue("the i(" + i + ") should be equal to j(" + j + ")", Flik.isSameNumber(i, j));
        }

    }

}
