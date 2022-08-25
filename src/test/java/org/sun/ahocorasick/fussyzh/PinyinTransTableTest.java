package org.sun.ahocorasick.fussyzh;

import org.sun.ahocorasick.Trie;
import org.sun.ahocorasick.zhtools.PinyinEngine;
import org.sun.ahocorasick.zhtools.PinyinSimTable;
import org.testng.annotations.Test;

import java.util.List;

public class PinyinTransTableTest {

    @Test
    public void testGetTransformedChars() {

        Trie trie = new Trie();
        trie.addKeyword("习大大");
        trie.constructFailureAndPrevWordPointer();

        PinyinTransTable pinyinTransTable = new PinyinTransTable(trie);

        int xi = PinyinEngine.getInstance().getCodeByPinyin("xi");
        CharSequence transformedChars = pinyinTransTable.getTransformedChars(1, xi);

        System.out.println(transformedChars);

        CharSequence xing = pinyinTransTable.getTransformedChars(1, PinyinEngine.getInstance().getCodeByPinyin("xing"));

        System.out.println(xing);

        List<String> xi1 = PinyinSimTable.getInstance().getSimilarPinyins("xi");
        System.out.println(xi1);
    }
}