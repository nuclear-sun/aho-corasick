package org.sun.ahocorasick;

public class Tuple<V1, V2> {

    public V1 first;
    public V2 second;

    public Tuple(V1 first, V2 second) {
        this.first = first;
        this.second = second;
    }
}