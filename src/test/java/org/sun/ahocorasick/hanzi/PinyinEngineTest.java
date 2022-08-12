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

        String firstPinyin = pinyinEngine.parseFirstGreedyPinyin("jiandan").getText();
        assertEquals(firstPinyin, "jian");
        PinyinInfo pinyinInfo = pinyinEngine.parseFirstGreedyPinyin("xjinping");
        assertNull(pinyinInfo);

        assertNull(pinyinEngine.parseFirstGreedyPinyin(""), null);
        assertNull(pinyinEngine.parseFirstGreedyPinyin(null));
    }

    @Test
    public void testGetPinyinById() {
        PinyinInfo pinyinInfo = pinyinEngine.getPinyinInfoById(424);
        System.out.println(pinyinInfo);
    }
}