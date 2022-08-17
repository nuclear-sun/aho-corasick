package org.sun.ahocorasick;

import java.util.LinkedList;
import java.util.List;

interface Automaton<V> {

    void parseText(CharSequence text, MatchHandler<V> handler);

    default List<Emit<V>> parseText(CharSequence text) {
        final List<Emit<V>> results = new LinkedList<>();

        MatchHandler<V> listener = new MatchHandler<V>() {
            @Override
            public boolean onMatch(int start, int end, String key, V value) {
                Emit<V> emit = new Emit<>(key, start, end, value);
                results.add(emit);
                return true;
            }
        };

        parseText(text, listener);
        return results;
    }

}
