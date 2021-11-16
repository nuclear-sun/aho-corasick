package org.sun.ahocorasick;

import java.util.List;

interface Automaton<V> {

    void parseText(CharSequence text, MatchHandler<V> handler);

    List<Emit<V>> parseText(CharSequence text);

}
