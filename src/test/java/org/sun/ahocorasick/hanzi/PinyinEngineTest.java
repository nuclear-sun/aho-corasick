package org.sun.ahocorasick.hanzi;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import static org.testng.Assert.*;

public class PinyinEngineTest {

    private PinyinEngine pinyinEngine;

    @BeforeClass
    void setUp() {
        pinyinEngine = PinyinEngine.getInstance();
    }

    @Test
    public void testParsePinyin() {

        String firstPinyin = pinyinEngine.parseFirstGreedyPinyin("jiandan");
        assertEquals(firstPinyin, "jian");
        String pinyin = pinyinEngine.parseFirstGreedyPinyin("xjinping");
        assertEquals(pinyin, "x");

        assertEquals(pinyinEngine.parseFirstGreedyPinyin(""), "");
        assertNull(pinyinEngine.parseFirstGreedyPinyin(null));
    }

    @Test
    public void testGetPinyinById() {
        PinyinInfo pinyinInfo = pinyinEngine.getPinyinInfoById(424);
        System.out.println(pinyinInfo);
    }
}