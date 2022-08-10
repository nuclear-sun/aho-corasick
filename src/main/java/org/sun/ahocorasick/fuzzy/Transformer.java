package org.sun.ahocorasick.fuzzy;

import org.sun.ahocorasick.DATAutomaton;

public interface Transformer {

    /**
     * Transform a char into another given DATAutomaton env and source text env
     * @param automaton a DATAutomaton
     * @param state     current state
     * @param text      input text
     * @param i         current index for input text
     * @param ch        current character to process
     * @param ruleIndex a char may have many convert targets, this index shows which target
     * @return  (consumed indexes, target char)
     */
    int transform(DATAutomaton automaton, int state, CharSequence text, int i, char ch, int ruleIndex);
}
