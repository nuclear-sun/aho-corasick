package org.sun.ahocorasick;

import java.util.Objects;

public class Emit<V> implements Comparable<Emit<V>> {
    private final String keyword;
    private final int start;
    private final int end;
    private final V value;

    public Emit(String keyword, int start, int end, V value) {
        this.keyword = keyword;
        this.start = start;
        this.end = end;
        this.value = value;
    }

    public Emit(String keyword, int start, int end) {
        this(keyword, start, end, null);
    }

    @Override
    public int compareTo(Emit<V> that) {
        int c = this.end - that.end;
        return c != 0 ? c : this.start - that.start;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Emit<?> emit = (Emit<?>) o;
        return start == emit.start && end == emit.end && keyword.equals(emit.keyword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyword, start, end);
    }

    public String toString() {
        String template = "[%d:%d]%s";
        String temp = String.format(template, start, end, keyword);
        return value == null ? temp : temp + "=" + value;
    }

}
