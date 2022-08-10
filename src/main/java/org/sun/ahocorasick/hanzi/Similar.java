package org.sun.ahocorasick.hanzi;

public interface Similar {

    double getSimilarity(char ch1, char ch2);

    char[] getSimilarChars(char ch);
}
