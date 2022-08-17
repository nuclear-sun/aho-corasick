package org.sun.ahocorasick;

import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public class TrieTest {

    @Test
    public void testCount() {

        Trie trie = new Trie();
        trie.addKeyword("小明");
        assertEquals(trie.getKeywordCount(), 1);
        assertEquals(trie.getStateCount(), 3);

        trie.constructFailureAndPrevWordPointer();
        assertEquals(trie.getKeywordCount(), 1);
        assertEquals(trie.getStateCount(), 3);

    }

    @Test
    public void testNormal() {
        Trie<Character> trie = new Trie<>();
        trie.putKeyword("she", '她');
        trie.putKeyword("he", '他');
        trie.constructFailureAndPrevWordPointer();

        DATAutomaton.Builder<Character> builder = DATAutomaton.builder();
        DATAutomaton<Character> automaton = builder.buildFromTrie(trie);
        List<Emit<Character>> emits = automaton.parseText("she is a friend.");

        assertEquals(emits.size(), 2);
        assertEquals(emits.get(0).getValue(), Character.valueOf('她'));
        assertEquals(emits.get(1).getValue(), Character.valueOf('他'));
    }

    @Test
    public void testNormal2() {
        Trie<Character> trie = new Trie<>();
        trie.putKeyword("she", '她');
        trie.putKeyword("he", '他');
        trie.constructFailureAndPrevWordPointer();

        DATAutomaton.Builder<Character> builder = DATAutomaton.builder();
        DATAutomaton<Character> automaton = builder.buildFromTrie(trie);
    }

    @Test
    public void testParse() {
        Trie<Character> trie = new Trie<>();
        trie.putKeyword("she", '她');
        trie.putKeyword("he", '他');

        trie.constructFailureAndPrevWordPointer();
        List<Emit<Character>> list = trie.parseText("she");
        assertEquals(list.size(), 2);

        List<Emit<Character>> list1 = trie.parseText("he");
        assertEquals(list1.size(), 1);
    }

    @Test
    public void testArch() {

        BuildCallback addArchCallback = new BuildCallback() {
            @Override
            public void onStateCreated(State state, String word, State parentState, char ch) {
            }

            @Override
            public void onStateChecked(State state, String word, State parentState, char ch) {
                parentState.getSuccess().put((char)(ch+2), state);
            }

            @Override
            public void onWordAdded(State state, String word) {
            }
        };

        Trie trie1 = new Trie();
        trie1.setCallback(addArchCallback);
        trie1.addKeyword("小明");
        Map<Character, State> success = trie1.getRootState().getSuccess();
        assertEquals(success.size(), 2);
        assertTrue(success.containsKey('小'));
        assertTrue(success.containsKey('少')); // '小' + 2 == '少'

        State state1 = success.get('小');
        State state2 = success.get('少');
        assertTrue(state1 == state2);

        trie1.constructFailureAndPrevWordPointer();


        class IntHolder {
            int i = 0;
        }
        final IntHolder holder = new IntHolder();
        trie1.traverse(state -> {
            holder.i++;
        });
        assertEquals(holder.i, 3);

        List emits = trie1.parseText("我叫少明");
        assertEquals(emits.size(), 1);
        assertEquals(((Emit)emits.get(0)).getKeyword(), "小明");
    }
}