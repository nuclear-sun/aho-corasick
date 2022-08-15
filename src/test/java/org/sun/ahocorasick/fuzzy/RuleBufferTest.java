package org.sun.ahocorasick.fuzzy;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class RuleBufferTest {



    @Test
    public void testWriteReadRule() {

        RuleBuffer ruleBuffer = new RuleBuffer();

        char ruleHead = (1 << 8) + 1;
        ruleBuffer.putChar(ruleHead);
        ruleBuffer.putChar('x');

        ruleHead = (3 << 8) + 2;
        ruleBuffer.putChar(ruleHead);
        ruleBuffer.putChar('b');
        ruleBuffer.putChar('c');

        boolean hasNextRule = ruleBuffer.hasNextRule();
        assertTrue(hasNextRule);
        ruleBuffer.nextRule();
        assertEquals(ruleBuffer.getConsumedCharNum(), 1);
        assertEquals(ruleBuffer.getOutputCharNum(), 1);

        assertEquals(ruleBuffer.getNextChar(), 'x');
        assertEquals(ruleBuffer.getNextChar(), 0);
        assertEquals(ruleBuffer.getNextChar(), 0);

        assertTrue(ruleBuffer.hasNextRule());
        ruleBuffer.nextRule();
        assertEquals(ruleBuffer.getConsumedCharNum(), 3);
        assertEquals(ruleBuffer.getOutputCharNum(), 2);
        assertEquals(ruleBuffer.getNextChar(), 'b');
        assertEquals(ruleBuffer.getNextChar(), 'c');
        assertEquals(ruleBuffer.getNextChar(), 0);
        assertEquals(ruleBuffer.getNextChar(), 0);
        assertFalse(ruleBuffer.hasNextRule());
    }


}