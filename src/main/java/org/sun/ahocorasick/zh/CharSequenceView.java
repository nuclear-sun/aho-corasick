package org.sun.ahocorasick.zh;

public class CharSequenceView implements CharSequence {

    private CharSequence charSequence;
    private int start;
    private int end;

    public CharSequenceView(CharSequence charSequence, int start, int end) {
        this.charSequence = charSequence;
        this.start = start;
        this.end = end;
    }

    public CharSequenceView(CharSequence charSequence, int start) {
        this(charSequence, start, charSequence.length());
    }

    @Override
    public int length() {
        return end - start;
    }

    @Override
    public char charAt(int index) {
        return charSequence.charAt(index - start);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        int actualStart = this.start + start;
        int actualEnd = this.end + end;
        return charSequence.subSequence(actualStart, actualEnd);
    }
}