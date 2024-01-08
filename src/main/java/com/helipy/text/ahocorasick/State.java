package com.helipy.text.ahocorasick;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nuclear-sun
 */
public class State {

    private int ordinal;

    private String keyword;

    private Map<Character, State> success;

    private State failure;

    private State prevWordState;

    public State() {
        this.success = new HashMap<>();
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public Map<Character, State> getSuccess() {
        return this.success;
    }

    public State getFailure() {
        return this.failure;
    }

    public void setFailure(State failure) {
        this.failure = failure;
    }

    public State getPrevWordState() {
        return prevWordState;
    }

    public void setPrevWordState(State prevWordState) {
        this.prevWordState = prevWordState;
    }
}
