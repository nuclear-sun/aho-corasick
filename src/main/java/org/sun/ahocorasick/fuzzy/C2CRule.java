package org.sun.ahocorasick.fuzzy;

import org.sun.ahocorasick.DATAutomaton;
import org.sun.ahocorasick.Tuple;

public class C2CRule implements ConvertRule {

    private char char1;
    private char char2;

    public C2CRule(char char1, char char2) {
        this.char1 = char1;
        this.char2 = char2;
    }

    @Override
    public Tuple<Character, Integer> apply(DATAutomaton automaton, int state, CharSequence text, int i, char ch) {


        return null;
    }
}
