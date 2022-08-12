package org.sun.ahocorasick.fuzzy;

public interface TransformTable {

    CharSequence getTransformedChars(int state, int originChar);

}
