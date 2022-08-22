package org.sun.ahocorasick.fussyzh;

import org.sun.ahocorasick.Emit;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class FZHAutomatonTest {


    @Test
    public void testFussyParseText() {

        FZHAutomaton.Builder builder = FZHAutomaton.builder();
        builder.put("习惯", null, true);
        builder.put("习大大", null, true);
        builder.put("中央银行", null, true);
        builder.put("d大", null, true);
        builder.put("xd", null, true);

        FZHAutomaton automaton = builder.build();

        List list = automaton.fussyParseText("xd大");
        System.out.println(list);

        List list1 = automaton.fussyParseText("众炎银行");
        System.out.println(list1);

        List list2 = automaton.fussyParseText("刁大大");
        System.out.println(list2);
    }

    @Test
    public void testIgnoreSpecialChar() {
        FZHAutomaton.Builder builder = FZHAutomaton.builder();
        builder.put("习惯", null, true);
        builder.put("习大大", null, false);
        builder.put("中央银行", null, true);
        builder.put("d大", null, true);
        //builder.put("xd", null, true);

        FZHAutomaton automaton = builder.build();

        String text = "中央yin行";
        List list = automaton.fussyParseText(text);

        System.out.println(list);

        List list2 = automaton.fussyParseText("xi大大");
        System.out.println(list2);
    }

    @Test
    public void testFuzzyOrAccurate() {

        FZHAutomaton.Builder builder = FZHAutomaton.builder();
        builder.put("李小明", null, false);
        builder.put("王一分", null, false);
        FZHAutomaton automaton = builder.build();

        List list1 = automaton.fussyParseText("李晓明");
        assertEquals(list1.size(), 0);

        List list2 = automaton.fussyParseText("li小明");
        assertEquals(list2.size(), 0);

        builder.put("习惯", null, true);
        builder.put("习大大", null, false);
        builder.put("大大", null, false);

        FZHAutomaton automaton2 = builder.build();
        List list = automaton2.fussyParseText("xi大大");
        System.out.println(list);
    }


    @Test
    public void testDuplication() {
        FZHAutomaton.Builder builder = FZHAutomaton.builder();
        //builder.put("习惯", null, true);
        builder.put("习大大", null, true);
        builder.put("大大", null, true);

        FZHAutomaton automaton = builder.build();
        List list = automaton.fussyParseText("xi打大");
        assertEquals(list.size(), 2);

        List list1 = automaton.fussyParseText("xi大大");
        assertEquals(list1.size(), 2);

        FZHAutomaton.Builder builder2 = FZHAutomaton.builder();
        builder2.put("习惯", null, true)
                .put("习以为常", null, false)
                .put("以为", null, true);
        FZHAutomaton automaton2 = builder2.build();
        List list2 = automaton2.fussyParseText("xi以w常");
        assertEquals(list2.size(), 1);
        assertEquals(((Emit)list2.get(0)).getKeyword(), "以为");


        FZHAutomaton.Builder builder3 = FZHAutomaton.builder();
        builder3.put("习惯", null, false)
                .put("习以为常", null, true)
                .put("以为", null, true);
        FZHAutomaton automaton3 = builder3.build();
        List list3 = automaton3.fussyParseText("xi以w常");
        assertEquals(list3.size(), 2);
    }


    @Test
    public void testPosition() {

        FZHAutomaton.Builder builder = FZHAutomaton.builder();
        builder.put("容易", null, true)
                .put("易学习", null, true)
                .put("习以为常", null, true)
                .put("以为", null, true)
                .put("代签", null, true)
                .put("代签收", null, true);
        FZHAutomaton automaton = builder.build();

        final String text = "小明容易学系，并且yiwei做出难题事xiy为尝的事情。";
        List<Emit> list = automaton.fussyParseText(text);
        Emit emit0 = list.get(0);
        assertEquals(emit0.getStart(), 2);
        assertEquals(emit0.getEnd(), 4);

        Emit emit1 = list.get(1);
        assertEquals(emit1.getStart(), 3);
        assertEquals(emit1.getEnd(), 6);

        Emit emit2 = list.get(2);
        assertEquals(emit2.getStart(), 9);
        assertEquals(emit2.getEnd(), 14);

        Emit emit3 = list.get(3);
        assertEquals(emit3.getStart(), 19);
        assertEquals(emit3.getEnd(), 24);

        Emit emit4 = list.get(4);
        assertEquals(emit4.getStart(), 21);
        assertEquals(emit4.getEnd(), 23);


        final String text2 = "小明容##易###学系，并且y###iw#ei做出难题事xi##y#为###尝的事情。";

        CharSequence cs = new CharSequence() {
            @Override
            public int length() {
                return text2.length();
            }

            @Override
            public char charAt(int index) {
                char ch = text2.charAt(index);
                if(ch == '#') return 0;
                return ch;
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return null;
            }
        };

        List list1 = automaton.fussyParseText(text2);
        assertEquals(list1.size(), 0);

        List<Emit> list2 = automaton.fussyParseText(cs);
        assertEquals(list2.size(), 5);

        Emit rongyi = list2.get(0);
        assertEquals(rongyi.getStart(), 2);
        assertEquals(rongyi.getEnd(), 6);

        Emit yixuexi = list2.get(1);
        assertEquals(yixuexi.getStart(), 5);
        assertEquals(yixuexi.getEnd(), 11);

        Emit yiwei = list2.get(2);
        assertEquals(yiwei.getStart(), 14);
        assertEquals(yiwei.getEnd(), 23);

        Emit xiyiweichang = list2.get(3);
        assertEquals(xiyiweichang.getStart(), 28);
        assertEquals(xiyiweichang.getEnd(), 39);

        Emit yiwei2 = list2.get(4);
        assertEquals(yiwei2.getStart(), 32);
        assertEquals(yiwei2.getEnd(), 35);
    }

    @Test
    public void testLoadBigDict() {

        FZHAutomaton.Builder builder = FZHAutomaton.builder();
        FZHAutomaton automaton = null;

        InputStream resourceAsStream = getClass().getResourceAsStream("/sensitive.txt");
        BufferedReader bufferedReader = null;

        int count = 0;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                count ++;
                builder.put(line, null, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (Exception e) {}
            try {
                resourceAsStream.close();
            } catch (Exception e) {}
        }

        automaton = builder.build();

        System.out.println("Added " + count + " words");

    }

}