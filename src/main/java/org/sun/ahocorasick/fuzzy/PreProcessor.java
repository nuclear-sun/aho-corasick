package org.sun.ahocorasick.fuzzy;

import org.sun.ahocorasick.DATAutomaton;

public interface PreProcessor {

    default char process(char ch) {
        return ch;
    }

}
