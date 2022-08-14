package org.sun.ahocorasick.zh;

import org.sun.ahocorasick.DATAutomaton;
import org.sun.ahocorasick.fuzzy.RuleBuffer;
import org.sun.ahocorasick.fuzzy.Transformer;
import org.sun.ahocorasick.hanzi.HanziDict;
import org.sun.ahocorasick.hanzi.PinyinEngine;
import org.sun.ahocorasick.hanzi.PinyinInfo;

import java.util.List;

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

            CharSequence transformedChars = shapeTransTable.getTransformedChars(state, ch); // 形近转换
            if(transformedChars == null) {
                List<String> pinyinList = HanziDict.getInstance().getPinyin(ch);
                String pinyin = pinyinList.get(0);
                int pinyinCode = PinyinEngine.getInstance().getInfoByPinyin(pinyin).getId();
                transformedChars = pinyinTransTable.getTransformedChars(state, pinyinCode); // 音近转换
            }

            if(transformedChars != null) {

                final char ruleHead = (1 << 8) + 1;

                for (int j = 0, length = transformedChars.length(); j < length; j++) {
                    ruleBuffer.putChar(ruleHead);
                    ruleBuffer.putChar(transformedChars.charAt(j));
                }
            }

        } else if(Character.isAlphabetic(ch)) {  // 拼音收集转换，例： 中yang

            PinyinInfo info = pinyinEngine.parseFirstGreedyPinyin(new CharSequenceView(text, i));

            int consumedChars;
            int code;

            if(info == null) {
                code = ch;
                consumedChars = 1;
            } else {
                code = info.getId();
                consumedChars = info.getText().length();
            }


            final char ruleHead = (char)((consumedChars << 8) + 1);

            CharSequence transformedChars = pinyinTransTable.getTransformedChars(state, code);
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



