package org.sun.ahocorasick;

public interface BuildCallback<V> {

    void onStateCreated(State<V> state, String word, State<V> parentState,char ch);

    void onStateChecked(State<V> state, String word, State<V> parentState,char ch);

    void onWordAdded(State<V> state, String word);
}
