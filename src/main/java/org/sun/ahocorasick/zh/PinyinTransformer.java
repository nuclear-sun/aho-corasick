package org.sun.ahocorasick.zh;

import org.sun.ahocorasick.DATAutomaton;
import org.sun.ahocorasick.fuzzy.Transformer;

public class PinyinTransformer implements Transformer {

    private PinyinTransTable transTable;

    @Override
    public int transform(DATAutomaton automaton, int state, CharSequence text, int i, char ch, int ruleIndex) {

        CharSequence transformedChars = transTable.getTransformedChars(state, ch);
        if(transformedChars == null) {
            return -1;
        }
        if(ruleIndex >= transformedChars.length()) {
            return -2;
        }
        return transformedChars.charAt(ruleIndex);
    }

}
