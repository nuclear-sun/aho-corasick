package org.sun.ahocorasick.fuzzy;

import java.util.Arrays;


public class RuleBuffer {

    private char[] data;
    private int writeIndex = 0;
    private int ruleIndex = 0;
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


    public boolean hasNextRule() {

        if(ruleIndex >= data.length || data[ruleIndex] == 0) {
            return false;
        }
        return true;
    }

    public void nextRule() {
        ruleIndex = readIndex + 1;
        readIndex += 2;
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