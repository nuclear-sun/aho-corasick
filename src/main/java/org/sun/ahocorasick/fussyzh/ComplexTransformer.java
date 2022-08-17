package org.sun.ahocorasick.fussyzh;

import org.sun.ahocorasick.DATAutomaton;
import org.sun.ahocorasick.fuzzy.RuleBuffer;
import org.sun.ahocorasick.fuzzy.Transformer;
import org.sun.ahocorasick.zhtools.HanziDict;
import org.sun.ahocorasick.zhtools.PinyinEngine;
import org.sun.ahocorasick.zhtools.PinyinInfo;


class ComplexTransformer implements Transformer {

    private ShapeTransTable shapeTransTable;
    private PinyinTransTable pinyinTransTable;
    private PinyinEngine pinyinEngine;


    public ComplexTransformer(ShapeTransTable shapeTransTable, PinyinTransTable pinyinTransTable) {

        this.pinyinEngine = PinyinEngine.getInstance();
        this.shapeTransTable = shapeTransTable;
        this.pinyinTransTable = pinyinTransTable;

    }

    @Override
    public int transform(DATAutomaton automaton, int state, CharSequence text, int i, char ch, int ruleIndex) {
        return 0;
    }

    @Override
    public RuleBuffer getTransformRules(DATAutomaton automaton, int state, CharSequence text, int i, char ch) {

        final RuleBuffer ruleBuffer = new RuleBuffer();

        if(HanziDict.isBMPChineseChar(ch)) {

            CharSequence transformedChars = shapeTransTable.getTransformedChars(state, ch); // 1. 形近转换
            ruleBuffer.putOneCharRules(transformedChars);

            int pinyinCode = HanziDict.getInstance().getPinyinCode(ch);                    // 2. 同音转换
            ruleBuffer.putOneCharRules(String.valueOf((char) pinyinCode));

            CharSequence pinyinTransChars = pinyinTransTable.getTransformedChars(state, pinyinCode); // 3. 音近转换
            ruleBuffer.putOneCharRules(pinyinTransChars);

        } else if(Character.isAlphabetic(ch)) {  // 拼音收集, 例： 中yang

            PinyinInfo info = pinyinEngine.parseFirstGreedyPinyin(new CharSequenceView(text, i));

            int consumedChars;
            int code;

            if(info == null) {
                code = ch;
                consumedChars = 1;
            } else {
                code = info.getCode();
                consumedChars = info.getText().length();
            }

            final char ruleHead = (char)((consumedChars << 8) + 1);
            ruleBuffer.putChar(ruleHead);
            ruleBuffer.putChar((char) code);                                                         // 4. 拼音收集转换，例：中yang

            CharSequence transformedChars = pinyinTransTable.getTransformedChars(state, code);        // 5. 收集拼音的近似音转换
            if(transformedChars != null) {
                for (int j = 0, length = transformedChars.length(); j < length; j++) {
                    ruleBuffer.putChar(ruleHead);
                    ruleBuffer.putChar(transformedChars.charAt(j));
                }
            }
        }

        if(ruleBuffer.hasNextRule()) {
            return ruleBuffer;
        } else {
            return null;
        }

    }

}



