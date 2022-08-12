package org.sun.ahocorasick.fuzzy;

import org.sun.ahocorasick.Emit;
import org.sun.ahocorasick.MatchHandler;

import java.util.List;

public interface FuzzyAutomaton<V> {

    List<Emit<V>> fussyParseText(CharSequence text);

    void fussyParseText(CharSequence text, MatchHandler<V> handler);
}
