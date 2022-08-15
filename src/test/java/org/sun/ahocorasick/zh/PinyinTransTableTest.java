package org.sun.ahocorasick.zh;

import org.sun.ahocorasick.Trie;
import org.sun.ahocorasick.hanzi.PinyinEngine;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class PinyinTransTableTest {

    @Test
    public void testGetTransformedChars() {

        Trie trie = new Trie();
        trie.addKeyword("习大大");

        PinyinTransTable pinyinTransTable = new PinyinTransTable(trie);

        int xi = PinyinEngine.getInstance().getCodeByPinyin("xi");
        CharSequence transformedChars = pinyinTransTable.getTransformedChars(1, xi);

        System.out.println(transformedChars);

    }
}