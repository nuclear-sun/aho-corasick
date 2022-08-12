package org.sun.ahocorasick;

public interface BuildCallback<V> {

    void onStateCreated(State<V> state, String word);

    void onStateChecked(State<V> state, String word);

    void onWordAdded(State<V> state, String word);
}
