package org.sun.ahocorasick;

interface MatchListener<V> {

    /**
     * Callback on matching a keyword
     * @param start start index in query text
     * @param end   end index in query text, exclusive
     * @param key   matched keyword
     * @param value attached object to the keyword
     * @return a boolean value indicating if continuing or stopping parsing,
     *          true indicates continue, false indicates stop.
     */
    boolean onMatch(int start, int end, String key, V value);
}
