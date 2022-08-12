package org.sun.ahocorasick.zh;

import org.sun.ahocorasick.DATAutomaton;
import org.sun.ahocorasick.fuzzy.Transformer;
import org.sun.ahocorasick.hanzi.HanziDict;
import org.sun.ahocorasick.hanzi.PinyinEngine;
import org.sun.ahocorasick.hanzi.PinyinInfo;

import java.util.List;

public class ShapeTransformer implements Transformer {

    private ShapeTransTable transTable;

    private final HanziDict hanziDict = HanziDict.getInstance();

    private final PinyinEngine pinyinEngine = PinyinEngine.getInstance();

    public ShapeTransformer() {
        //this.transTable = new ShapeTransTable();
    }

    @Override
    public int transform(DATAutomaton automaton, int state, CharSequence text, int i, char ch, int ruleIndex) {

        CharSequence transformedChars = transTable.getTransformedChars(state, ch);
        if(transformedChars == null) {
            return -1;
        }

        if(ruleIndex >= transformedChars.length()) {
            return -2;
        }

        char transformedChar = transformedChars.charAt(ruleIndex);

        List<String> pinyinList = hanziDict.getPinyin(transformedChar);
        if(pinyinList == null) { // no pin found for this char
            return transformedChar;
        }

        PinyinInfo pinyinInfo = pinyinEngine.getInfoByPinyin(pinyinList.get(0));

        if(pinyinInfo == null) {
            return transformedChar;
        }

        return pinyinInfo.getId();
    }
}
