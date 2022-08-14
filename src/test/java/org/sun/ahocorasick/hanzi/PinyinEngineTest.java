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
        PinyinInfo pinyinInfo = pinyinEngine.getPinyinInfoByCode(424);
        System.out.println(pinyinInfo);
    }

    @Test
    public void testParseFirstGreedyPinyin() {

        String zhong = "zhong";

        PinyinInfo info = pinyinEngine.parseFirstGreedyPinyin(zhong);

        assertNotNull(info);

        assertEquals(info.getText(), zhong);

    }

    @Test
    public void testGetPinyinInfoById() {
    }

    @Test
    public void testGetInfoByPinyin() {
    }

    @Test
    public void testParsePinyin1() {
    }

    @Test
    public void testParsePinyin2() {
    }
}