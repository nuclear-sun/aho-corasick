package org.sun.ahocorasick.zh;

import org.sun.ahocorasick.DATAutomaton;
import org.sun.ahocorasick.fuzzy.RuleBuffer;
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

    @Override
    public RuleBuffer getTransformRules(DATAutomaton automaton, int state, CharSequence text, int i, char ch) {
        CharSequence transformedChars = transTable.getTransformedChars(state, ch);

        if(transformedChars == null) {
            return null;
        }

        RuleBuffer ruleBuffer = new RuleBuffer();

        final char ruleHead = (1 << 8) + 1;
        for (int j = 0, length = transformedChars.length(); j < length; j++) {
            ruleBuffer.putChar(ruleHead);
            ruleBuffer.putChar(transformedChars.charAt(j));
        }

        return ruleBuffer;
    }

}
