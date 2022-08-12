package org.sun.ahocorasick.hanzi;

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
        assertEquals(dang, Arrays.asList("da", "dan", "dang"));
    }
}