package org.sun.ahocorasick.fuzzy;

import org.sun.ahocorasick.DATAutomaton;

import java.util.HashMap;
import java.util.Map;

public class C2CTransformer implements Transformer {

    private Map<Character, char[]> table = new HashMap<>();

    public C2CTransformer() {

        table.put('王', new char[]{'主', '汪'});
        table.put('仪', new char[]{'义', '×'});

    }

    @Override
    public int transform(DATAutomaton automaton, int state, CharSequence text, int i, char ch, int ruleIndex) {

        if(!table.containsKey(ch)) {
            return -1;
        }

        char[] array = table.get(ch);
        if(ruleIndex < 0 || ruleIndex >= array.length) {
            return -2;
        }

        char newChar = array[ruleIndex];

        return (1 << 16) | newChar;
    }

    @Override
    public RuleBuffer getTransformRules(DATAutomaton automaton, int state, CharSequence text, int i, char ch) {
        return null;
    }
}
