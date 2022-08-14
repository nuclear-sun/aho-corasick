package org.sun.ahocorasick.fuzzy;


public interface PreProcessor {

    default char process(char ch) {
        return ch;
    }

}
