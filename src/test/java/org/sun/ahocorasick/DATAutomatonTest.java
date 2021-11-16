package org.sun.ahocorasick;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class DATAutomatonTest {

    class Person {
        final String name;
        final int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }


    private DATAutomaton automaton;

    @BeforeClass
    public void setUp() {
        build();
    }

    public void build() {

        DATAutomaton.Builder builder = DATAutomaton.builder();
        builder.add("she")
                .add("he")
                .add("say");
        DATAutomaton automaton = builder.build();
        automaton.printStats(false);

        this.automaton = automaton;
    }

    public void build2() {

        DATAutomaton.Builder<Person> builder = DATAutomaton.builder();
        builder.put("Jack", new Person("Jack", 12))
                .put("Julia", new Person("Julia", 16))
                .put("Jane", new Person("Jane", 18));

        DATAutomaton<Person> automaton = builder.build();
        automaton.printStats(false);

        this.automaton = automaton;
    }


    @Test
    public void testParseList() {

        String text = "He said it's ok to pick she up.";
        List list = automaton.parseText(text);
        System.out.println(list);
    }

    @Test
    public void testParseIgnoreCase() {

        class LowerCaseCS implements CharSequence {
            private final String text;
            public LowerCaseCS(String text) {
                this.text = text;
            }

            @Override
            public int length() {
                return this.text.length();
            }

            @Override
            public char charAt(int index) {
                return Character.toLowerCase(text.charAt(index));
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return null;
            }
        }

        String text = "He said it's ok to pick she up.";
        List list = automaton.parseText(new LowerCaseCS(text));

        System.out.println(list);

    }

    @Test
    public void testParseSkipCharacters() {

        class SkipDotCS implements CharSequence {

            private final String text;
            public SkipDotCS(String text) {
                this.text = text;
            }

            @Override
            public int length() {
                return text.length();
            }

            @Override
            public char charAt(int index) {
                char c = text.charAt(index);
                return c == '.' ? 0 : c;
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return null;
            }
        }


        String text = "sa...y,s..h..e";

        List list = automaton.parseText(new SkipDotCS(text));
        System.out.println(list);

    }

    @Test
    public void testParseUseHandler() {
        build2();

        class FindAge16MatchHandler implements MatchHandler<Person> {

            Person whoAges16;

            @Override
            public boolean onMatch(int start, int end, String key, Person person) {
                if(person.age == 16) {
                    whoAges16 = person;
                    return false;      // stops parsing when found
                }
                return true;
            }
        }

        FindAge16MatchHandler find16Handler = new FindAge16MatchHandler();


        String text = "Jane, get Julia and Jack into my office";

        automaton.parseText(text, find16Handler);

        System.out.println(find16Handler.whoAges16);
    }
}