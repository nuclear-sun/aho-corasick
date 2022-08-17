package org.sun.ahocorasick.fussyzh;

import org.testng.annotations.Test;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;

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
        builder.put("习大大", null, true);
        builder.put("中央银行", null, true);
        builder.put("d大", null, true);
        builder.put("xd", null, true);

        FCNDATAutomaton automaton = builder.build();

        String text = "中央yin行";
        List list = automaton.fussyParseText(text);

        System.out.println(list);

        List list2 = automaton.fussyParseText("xidada");
        System.out.println(list2);

    }

    @Test
    public void test() {
        ArrayDeque<Object> deque = new ArrayDeque<>();
        deque.push(null);

    }
}