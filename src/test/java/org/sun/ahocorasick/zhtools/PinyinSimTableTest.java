package org.sun.ahocorasick.zhtools;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

public class PinyinSimTableTest {

    @Test
    public void testGetSimilarPinyins() {

        PinyinSimTable pinyinSimTable = PinyinSimTable.getInstance();

        List<String> jian = pinyinSimTable.getSimilarPinyins("jian");
        assertEquals(jian, Arrays.asList("j", "ji"));

        List<String> dang = pinyinSimTable.getSimilarPinyins("dang");
        assertEquals(dang, Arrays.asList("da", "dan"));

        List<String> xi = pinyinSimTable.getSimilarPinyins("xi");
        System.out.println(xi);

        List<String> zhong = pinyinSimTable.getSimilarPinyins("zhong");
        System.out.println(zhong);
    }

    @Test
    public void testWithHeadChar() {
        PinyinSimTable pinyinSimTable = PinyinSimTable.getInstance();
        List<String> xi = pinyinSimTable.getSimilarPinyinOrHeadChar("xi");
        assertEquals(xi, Arrays.asList("x"));
        List<String> dang = pinyinSimTable.getSimilarPinyinOrHeadChar("dang");
        assertEquals(dang, Arrays.asList("da", "dan", "d"));

        CharSequence dang1 = pinyinSimTable.getSimilarPinyinOrHeadCharByCode(PinyinEngine.getInstance().getCodeByPinyin("dang"));
        assertEquals(dang1.length(), 3);
        assertEquals(dang1.charAt(dang1.length() - 1), 'd');

    }
}