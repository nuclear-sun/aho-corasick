package org.sun.ahocorasick.fuzzy;

import org.sun.ahocorasick.Emit;
import org.sun.ahocorasick.MatchHandler;

import java.util.LinkedList;
import java.util.List;

public interface FuzzyAutomaton<V> {

    default List<Emit<V>> fussyParseText(CharSequence text) {
        final List<Emit<V>> list = new LinkedList<>();

        MatchHandler<V> handler = new MatchHandler<V>() {
            @Override
            public boolean onMatch(int start, int end, String key, V value) {
                list.add(new Emit<V>(key, start, end, value));
                return true;
            }
        };

        fussyParseText(text, handler);

        return list;
    }

    void fussyParseText(CharSequence text, MatchHandler<V> handler);
}
