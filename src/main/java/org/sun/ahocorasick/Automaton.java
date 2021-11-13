package org.sun.ahocorasick;

interface Automaton<V> {

    void parse(CharSequence text, MatchListener<V> listener);

}
