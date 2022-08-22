package org.sun.ahocorasick.zhtools;

import org.sun.ahocorasick.DATAutomaton;
import org.sun.ahocorasick.Emit;
import org.testng.annotations.Test;

import java.util.List;

public class HanziDictTest {

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
