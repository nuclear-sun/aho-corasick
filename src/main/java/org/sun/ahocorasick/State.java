package org.sun.ahocorasick;

import java.util.HashMap;
import java.util.Map;

class State<V> {

    private int ordinal;

    private String keyword;

    private Map<Character, State<V>> success;

    private State failure;

    private State prevWordState;

    private V payload;

    public State() {
        this.success = new HashMap<>();
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public Map<Character, State<V>> getSuccess() {
        return this.success;
    }

    public void setFailure(State failure) {
        this.failure = failure;
    }

    public State getFailure() {
        return this.failure;
    }

    public void setPrevWordState(State prevWordState) {
        this.prevWordState = prevWordState;
    }

    public State getPrevWordState() {
        return prevWordState;
    }

    public V getPayload() {
        return payload;
    }

    public void setPayload(V payload) {
        this.payload = payload;
    }

}
