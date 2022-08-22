package org.sun.ahocorasick.zhtools;

import org.sun.ahocorasick.Emit;
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

        String firstPinyin = pinyinEngine.parseFirstGreedyPinyin("jiandan").getValue().getText();
        assertEquals(firstPinyin, "jian");
        Emit<PinyinInfo> pinyinInfo = pinyinEngine.parseFirstGreedyPinyin("xjinping");
        assertNull(pinyinInfo);

        assertNull(pinyinEngine.parseFirstGreedyPinyin(""), null);
        assertNull(pinyinEngine.parseFirstGreedyPinyin(null));
    }

    @Test
    public void testGetPinyinById() {
        PinyinInfo pinyinInfo = pinyinEngine.getInfoByCode(63612);
        System.out.println(pinyinInfo);
    }

    @Test
    public void testParseFirstGreedyPinyin() {

        String zhong = "zhong";
        Emit<PinyinInfo> info = pinyinEngine.parseFirstGreedyPinyin(zhong);
        assertNotNull(info);
        assertEquals(info.getValue().getText(), zhong);

        Emit<PinyinInfo> emit = pinyinEngine.parseFirstGreedyPinyin("\0\0z\0\0h\0\0\0on\0g\0");
        assertEquals(emit.getStart(), 0);
        assertEquals(emit.getEnd(), 13);
        assertEquals(emit.getValue().getText(), zhong);

        Emit<PinyinInfo> emit1 = pinyinEngine.parseFirstGreedyPinyin("a\0n\0\0g");
        assertEquals(emit1.getEnd(), 6);
        assertEquals(emit1.getValue().getText(), "ang");

        String yi = "yiwei";
        Emit<PinyinInfo> emit2 = pinyinEngine.parseFirstGreedyPinyin(yi);
        assertEquals(emit2.getStart(), 0);
        assertEquals(emit2.getEnd(), 2);

        String yiwei = "y\0\0i\0w\0e\0\0i\0";
        Emit<PinyinInfo> emit3 = pinyinEngine.parseFirstGreedyPinyin(yiwei);
        assertEquals(emit3.getEnd(), 4);

        String notPinyin = "小明";
        Emit<PinyinInfo> emit4 = pinyinEngine.parseFirstGreedyPinyin(notPinyin);
        assertNull(emit4);

        String combined = "y\0\0f";
        Emit<PinyinInfo> emit5 = pinyinEngine.parseFirstGreedyPinyin(combined);
        assertNull(emit5);

        String ankang = "\0\0a\0\0n康";
        Emit<PinyinInfo> emit6 = pinyinEngine.parseFirstGreedyPinyin(ankang);
        assertEquals(emit6.getEnd(), 6);
    }

    @Test
    public void testGetPinyinInfoById() {
        String pinyin = "ying";
        PinyinInfo info = pinyinEngine.getInfoByPinyin(pinyin);
        assertEquals(info.getText(), pinyin);

        PinyinInfo infoByCode = pinyinEngine.getInfoByCode(info.getCode());
        assertEquals(infoByCode.getText(), pinyin);
    }

    @Test
    public void testGetInfoByPinyin() {
        PinyinInfo info = pinyinEngine.getInfoByPinyin("ying");
        assertEquals(info.getText(), "ying");
    }

    @Test
    public void testGetCodeByPinyin() {
        int code = pinyinEngine.getCodeByPinyin("ying");
        assertTrue(code > 128);
    }


}