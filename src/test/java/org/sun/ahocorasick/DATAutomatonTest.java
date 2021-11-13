package org.sun.ahocorasick;


import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

class DATAutomatonTest {

    @Test
    void parse() {


        DATAutomaton.Builder builder = DATAutomaton.builder();

        builder.add("中国")
                .add("中央")
                .add("政治");

        DATAutomaton automaton = builder.build();

        List<Emit> list = automaton.parse("这里是中国的中央政治局");

        System.out.println(list);
    }

    @Test
    void testParse() {

    }


    static void testIgnoreSpecialChars() {

    }

    public static void main(String[] args) {

//        DATAutomaton.Builder builder = DATAutomaton.builder();
//
//        builder.add("he")
//                .add("she")
//                .add("say");
//
//        DATAutomaton automaton = builder.build();
//
//        automaton.printStats(true);
//
//        List<Emit> list = automaton.parse("eheshe");
//
//        String withIgnore = "s##h####e#h##e";
//
//        CharSequence convert = new CharSequence() {
//
//            @Override
//            public int length() {
//                return withIgnore.length();
//            }
//
//            @Override
//            public char charAt(int index) {
//                char ch = withIgnore.charAt(index);
//                if(ch == '#') {
//                    return 0;
//                }
//                return ch;
//            }
//
//            @Override
//            public CharSequence subSequence(int start, int end) {
//                return null;
//            }
//        };
//
//        List parse = automaton.parse(convert);
//
//        System.out.println(parse);

        testStop();

    }


    static void testStop() {

        List<String> list = new ArrayList<>();

        MatchListener listener = new MatchListener() {
            @Override
            public boolean onMatch(int start, int end, String key, Object value) {
                list.add(key);
                if(list.size() >= 2) {
                    return false;
                }
                return true;
            }
        };

        DATAutomaton.Builder builder = DATAutomaton.builder();

        builder.add("he")
                .add("she")
                .add("say");

        DATAutomaton automaton = builder.build();

        String text = "he said she say not his he";
        List parse = automaton.parse(text);

        System.out.println(parse);

        automaton.parse(text, listener);

        System.out.println(list);

    }

}