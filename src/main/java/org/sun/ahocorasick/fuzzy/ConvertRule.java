package org.sun.ahocorasick.fuzzy;

import org.sun.ahocorasick.DATAutomaton;
import org.sun.ahocorasick.Tuple;

public interface ConvertRule {

    Tuple<Character, Integer> apply(DATAutomaton automaton, int state, CharSequence text, int i, char ch);
}
