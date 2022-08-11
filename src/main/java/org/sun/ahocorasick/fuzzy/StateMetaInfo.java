package org.sun.ahocorasick.fuzzy;

import java.util.List;
import java.util.Map;

public class StateMetaInfo {

    private int counterPartChar;

    private boolean supportFuzzyMatch;


    public int getCounterPartChar() {
        return counterPartChar;
    }

    public void setCounterPartChar(int counterPartChar) {
        this.counterPartChar = counterPartChar;
    }

    public boolean isSupportFuzzyMatch() {
        return supportFuzzyMatch;
    }

    public void setSupportFuzzyMatch(boolean supportFuzzyMatch) {
        this.supportFuzzyMatch = supportFuzzyMatch;
    }
}
