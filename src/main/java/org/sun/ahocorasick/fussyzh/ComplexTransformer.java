package org.sun.ahocorasick.fussyzh;

import org.sun.ahocorasick.DATAutomaton;
import org.sun.ahocorasick.Emit;
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

    private static boolean isCharAcceptable(DATAutomaton automaton, int state, char ch) {
        return automaton.childState(state, ch) > 1;
    }

    @Override
    public RuleBuffer getTransformRules(DATAutomaton automaton, int state, CharSequence text, int i, char ch) {

        final RuleBuffer ruleBuffer = new RuleBuffer();

        if(HanziDict.isBMPChineseChar(ch)) {

            CharSequence transformedChars = shapeTransTable.getTransformedChars(state, ch); // 1. 形近转换
            ruleBuffer.putOneCharRules(transformedChars);

            int pinyinCode = HanziDict.getInstance().getPinyinCode(ch);                     // 2. 同音转换
            if(isCharAcceptable(automaton, state, (char) pinyinCode)) {
                ruleBuffer.putOneCharRule((char) pinyinCode);
            }

            CharSequence pinyinTransChars = pinyinTransTable.getTransformedChars(state, pinyinCode); // 3. 音近转换
            if(pinyinTransChars != null) {
                for (int j = 0, length = pinyinTransChars.length(); j < length; j++) {
                    char transChar = pinyinTransChars.charAt(j);
                    if(isCharAcceptable(automaton, state, transChar)) {
                        ruleBuffer.putOneCharRule(transChar);
                    }
                }
            }

        } else if(Character.isLetter(ch)) {  // 拼音收集, 例： 中yang, 注意，收集的拼音中可能含有停字符如 中y###an##g, 位置信息无法在算法框架中更新，需要在这里记录

            Emit<PinyinInfo> emit = pinyinEngine.parseFirstGreedyPinyin(new LowerCaseCharSequence(new CharSequenceView(text, i)));

            int consumedChars;
            int code;

            if(emit == null || emit.getEnd() > 127) { // 可能包含过多停止字符，则认为只识别到首字符，这可能是不合理的
                code = ch;
                consumedChars = 1;
            } else {
                code = emit.getValue().getCode();
                consumedChars = emit.getEnd() - emit.getStart();
            }

            final char ruleHead = (char) ((consumedChars << 8) + 1);
            if(isCharAcceptable(automaton, state, (char) code)) {
                ruleBuffer.putChar(ruleHead);
                ruleBuffer.putChar((char) code);                                                         // 4. 拼音收集转换，例：中yang
            }

            CharSequence transformedChars = pinyinTransTable.getTransformedChars(state, code);           // 5. 收集拼音的近似音转换
            if(transformedChars != null) {
                for (int j = 0, length = transformedChars.length(); j < length; j++) {
                    final char transChar = transformedChars.charAt(j);
                    if(isCharAcceptable(automaton, state, transChar)) {
                        ruleBuffer.putChar(ruleHead);
                        ruleBuffer.putChar(transChar);
                    }
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



