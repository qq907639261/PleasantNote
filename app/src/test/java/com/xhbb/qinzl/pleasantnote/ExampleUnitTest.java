package com.xhbb.qinzl.pleasantnote;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {
//        double num = (double) 479 / 60;
        assertEquals(7, 479 / 60);
        assertEquals(59, 479 % 60);
    }
}