package org.sun.ahocorasick.fussyzh;

public class LowerCaseCharSequence implements CharSequence {

    private CharSequence charSequence;

    public LowerCaseCharSequence(CharSequence charSequence) {
        this.charSequence = charSequence;
    }

    @Override
    public int length() {
        return charSequence.length();
    }

    @Override
    public char charAt(int index) {
        return Character.toLowerCase(charSequence.charAt(index));
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return null;
    }
}
