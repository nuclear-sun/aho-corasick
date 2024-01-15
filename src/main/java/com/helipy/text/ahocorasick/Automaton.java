package com.helipy.text.ahocorasick;

import java.util.List;

/**
 * @param <V> The related object type
 * @author nuclear-sun
 */
public interface Automaton<V> {

    /**
     * Parse matched keywords in text with callback handler
     *
     * @param text    text to scan
     * @param handler how to handle matched keyword
     */
    void parseText(CharSequence text, MatchHandler<V> handler);

    /**
     * Parse matched keywords in text
     *
     * @param text text to scan
     * @return matched keyword list, with word, start and end location in text, and related object if having config
     */
    List<Emit<V>> parseText(CharSequence text);

}
