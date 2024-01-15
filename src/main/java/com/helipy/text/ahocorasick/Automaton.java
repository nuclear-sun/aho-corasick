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
     * @param handler how to handle matched keyword.
     *                If you want to interrupt the searching process after a certain match,
     *                just make the handler return false; if want to continue searching, make it return true.
     * @return matched keyword list, with word, start and end location in text, and related object if having config.
     * Not all matched keywords if handler return false on the half way
     */
    List<Emit<V>> parseText(CharSequence text, MatchHandler<V> handler);

    /**
     * Parse matched keywords in text
     *
     * @param text text to scan
     * @return matched keyword list, with word, start and end location in text, and related object if having config
     */
    List<Emit<V>> parseText(CharSequence text);

}
