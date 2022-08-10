package org.sun.ahocorasick.fuzzy;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

public class PinyinDictTest {

    private PinyinDict pinyinDict;

    @BeforeClass
    public void setUp() {
        pinyinDict = new PinyinDict();
    }

    @Test
    public void testGetPinyin() {
        List<String> pinyin = pinyinDict.getPinyinList("\uD843\uDF7E");
        System.out.println(pinyin);
    }

    @Test
    public void testGetPinyinPlain() {
        List<String> pinyinListPlain = pinyinDict.getPinyinListPlain("\uD843\uDF7E");
        System.out.println(pinyinListPlain);
    }

    @Test
    public void testTransform2Plain() {
        String guang = PinyinDict.transformToPlain("guàng");
        System.out.println(guang);
    }

    @Test
    public void testPrintAllPinyin() {
        pinyinDict.printAllPinyin();
    }
}