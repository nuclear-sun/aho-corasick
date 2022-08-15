package org.sun.ahocorasick.fuzzy;

import java.util.Arrays;


public class RuleBuffer {

    private char[] data;
    private int writeIndex = 0;
    private int ruleIndex = -1;
    private int readIndex = -1;
    private int currRuleChars = 0;


    public RuleBuffer() {
        data = new char[8];
    }

    private void resize() {
        int newCapacity = data.length << 1;
        data = Arrays.copyOf(data, newCapacity);
    }


    public void putChar(char ch) {

        if(writeIndex >= data.length) {
            resize();
        }
        data[writeIndex++] = ch;
    }

    public void putRule(int consumedCharNum, int outputCharNum, CharSequence outputChars) {
        char ruleHead = (char)((consumedCharNum << 8) | outputCharNum);
        putChar(ruleHead);
        for (int i = 0, length = outputChars.length(); i < length; i++) {
            putChar(outputChars.charAt(i));
        }
    }

    public void putOneCharRules(CharSequence rules) {
        if(rules == null || rules.length() == 0) {
            return;
        }

        final char ruleHead = (1 << 8) + 1;
        for (int i = 0, length = rules.length(); i < length; i++) {
            putChar(ruleHead);
            putChar(rules.charAt(i));
        }
    }


    public boolean hasNextRule() {

        int nextRuleIndex = 0;
        if(ruleIndex < 0) {
            nextRuleIndex = 0;
        } else {
            nextRuleIndex = ruleIndex + getOutputCharNum() + 1;
        }

        if(nextRuleIndex >= data.length || data[nextRuleIndex] == 0) {
            return false;
        }
        return true;
    }

    public void nextRule() {
        if(ruleIndex < 0) {
            ruleIndex = 0;
        } else {
            ruleIndex = ruleIndex + getOutputCharNum() + 1;
        }
        readIndex = ruleIndex + 1;
        currRuleChars = getOutputCharNum();
    }

    public int getConsumedCharNum() {
        return data[ruleIndex] >>> 8;
    }

    public int getOutputCharNum() {
        return (byte) data[ruleIndex];
    }

    public char getNextChar() {
        if(readIndex - ruleIndex > currRuleChars) {
            return 0;
        }
        return data[readIndex++];
    }

}