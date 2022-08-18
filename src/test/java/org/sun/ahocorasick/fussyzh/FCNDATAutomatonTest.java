package org.sun.ahocorasick.fussyzh;

import org.sun.ahocorasick.Emit;
import org.testng.annotations.Test;

import java.util.ArrayDeque;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class FCNDATAutomatonTest {


    @Test
    public void testFussyParseText() {

        FCNDATAutomaton.Builder builder = FCNDATAutomaton.builder();
        builder.put("习惯", null, true);
        builder.put("习大大", null, true);
        builder.put("中央银行", null, true);
        builder.put("d大", null, true);
        builder.put("xd", null, true);

        FCNDATAutomaton automaton = builder.build();

        List list = automaton.fussyParseText("xd大");
        System.out.println(list);

        List list1 = automaton.fussyParseText("众炎银行");
        System.out.println(list1);

        List list2 = automaton.fussyParseText("刁大大");
        System.out.println(list2);
    }

    @Test
    public void testIgnoreSpecialChar() {
        FCNDATAutomaton.Builder builder = FCNDATAutomaton.builder();
        builder.put("习惯", null, true);
        builder.put("习大大", null, false);
        builder.put("中央银行", null, true);
        builder.put("d大", null, true);
        //builder.put("xd", null, true);

        FCNDATAutomaton automaton = builder.build();

        String text = "中央yin行";
        List list = automaton.fussyParseText(text);

        System.out.println(list);

        List list2 = automaton.fussyParseText("xi大大");
        System.out.println(list2);
    }

    @Test
    public void testFuzzyOrAccurate() {

        FCNDATAutomaton.Builder builder = FCNDATAutomaton.builder();
        builder.put("李小明", null, false);
        builder.put("王一分", null, false);
        FCNDATAutomaton automaton = builder.build();

        List list1 = automaton.fussyParseText("李晓明");
        assertEquals(list1.size(), 0);

        List list2 = automaton.fussyParseText("li小明");
        assertEquals(list2.size(), 0);

        builder.put("习惯", null, true);
        builder.put("习大大", null, false);
        builder.put("大大", null, false);

        FCNDATAutomaton automaton2 = builder.build();
        List list = automaton2.fussyParseText("xi大大");
        System.out.println(list);
    }


    @Test
    public void testDuplication() {
        FCNDATAutomaton.Builder builder = FCNDATAutomaton.builder();
        //builder.put("习惯", null, true);
        builder.put("习大大", null, true);
        builder.put("大大", null, true);

        FCNDATAutomaton automaton = builder.build();
        List list = automaton.fussyParseText("xi打大");
        assertEquals(list.size(), 2);

        List list1 = automaton.fussyParseText("xi大大");
        assertEquals(list1.size(), 2);

        FCNDATAutomaton.Builder builder2 = FCNDATAutomaton.builder();
        builder2.put("习惯", null, true)
                .put("习以为常", null, false)
                .put("以为", null, true);
        FCNDATAutomaton automaton2 = builder2.build();
        List list2 = automaton2.fussyParseText("xi以w常");
        assertEquals(list2.size(), 1);
        assertEquals(((Emit)list2.get(0)).getKeyword(), "以为");


        FCNDATAutomaton.Builder builder3 = FCNDATAutomaton.builder();
        builder3.put("习惯", null, false)
                .put("习以为常", null, true)
                .put("以为", null, true);
        FCNDATAutomaton automaton3 = builder3.build();
        List list3 = automaton3.fussyParseText("xi以w常");
        assertEquals(list3.size(), 2);
    }


    @Test
    public void testPosition() {

        FCNDATAutomaton.Builder builder = FCNDATAutomaton.builder();
        builder.put("容易", null, true)
                .put("易学习", null, true)
                .put("习以为常", null, true)
                .put("以为", null, true)
                .put("代签", null, true)
                .put("代签收", null, true);
        FCNDATAutomaton automaton = builder.build();
        List list = automaton.fussyParseText("小明容易学系，并且yiwei做出难题事xiy为尝的事情。");
        System.out.println(list);

        List list1 = automaton.fussyParseText("袋牵 dq dqian");
        System.out.println(list1);
    }

}