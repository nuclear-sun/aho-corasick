package org.sun.ahocorasick.zhtools;

import org.sun.ahocorasick.DATAutomaton;
import org.sun.ahocorasick.Emit;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

public class HanziDictTest {

    @Test
    public void testGetPinyinCode() {

        for (char i = 'a'; i < 'z'; i++) {
            int pinyinCode = HanziDict.getInstance().getPinyinCode(i);
            assertEquals(pinyinCode, i);
        }

        for (char i = 'A'; i < 'Z'; i++) {
            int pinyinCode = HanziDict.getInstance().getPinyinCode(i);
            assertEquals(pinyinCode, i);
        }

        int pinyinCode = HanziDict.getInstance().getPinyinCode('&');
        assertEquals(pinyinCode, '&');

        assertEquals(HanziDict.getInstance().getPinyinCode('9'), '9');
    }

    @Test
    public void testGetPinyinForString() {

        String zhongguo = "中国";
        CharSequence pinyin = HanziDict.getInstance().getPinyinCodes(zhongguo);
        System.out.println(pinyin);

    }

    @Test
    public void test() {
        DATAutomaton.Builder builder = DATAutomaton.builder();
        builder.add("zhong");
        builder.add("zhi");
        DATAutomaton automaton = builder.build();
        List zhong = automaton.parseText("zhong");
        System.out.println(zhong);

        List<Emit<PinyinInfo>> zhong1 = PinyinEngine.getInstance().parsePinyin("zhong");
        System.out.println(zhong1);



        Emit<PinyinInfo> info = PinyinEngine.getInstance().parseFirstGreedyPinyin("zhong");
        System.out.println(info);
    }
}
