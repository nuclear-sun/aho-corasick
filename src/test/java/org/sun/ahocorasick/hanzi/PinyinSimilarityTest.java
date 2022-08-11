package org.sun.ahocorasick.hanzi;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

public class PinyinSimilarityTest {

    @Test
    public void testGetSimilarPinyins() {

        PinyinSimilarity pinyinSimilarity = PinyinSimilarity.getInstance();

        List<String> jian = pinyinSimilarity.getSimilarPinyins("jian");
        assertEquals(jian, Arrays.asList("j", "ji"));

        List<String> dang = pinyinSimilarity.getSimilarPinyins("dang");
        assertEquals(dang, Arrays.asList("da", "dan", "dang"));
    }
}