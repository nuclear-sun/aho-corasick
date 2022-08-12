package org.sun.ahocorasick.zh;

import org.sun.ahocorasick.DATAutomaton;
import org.sun.ahocorasick.fuzzy.Transformer;
import org.sun.ahocorasick.hanzi.HanziDict;
import org.sun.ahocorasick.hanzi.PinyinEngine;
import org.sun.ahocorasick.hanzi.PinyinInfo;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

class ComplexTransformer implements Transformer {

    private ShapeTransTable shapeTransTable;
    private PinyinTransTable pinyinTransTable;
    private PinyinEngine pinyinEngine;

    // TODO 这样是不行的
    private ConcurrentHashMap<Long, Deque<StateTransformCache>> threadLocalTransformStackMap;

    public ComplexTransformer(ShapeTransTable shapeTransTable, PinyinTransTable pinyinTransTable) {

        this.pinyinEngine = PinyinEngine.getInstance();
        this.shapeTransTable = shapeTransTable;
        this.pinyinTransTable = pinyinTransTable;

        this.threadLocalTransformStackMap = new ConcurrentHashMap<>();
    }

    @Override
    public int transform(DATAutomaton automaton, int state, CharSequence text, int i, char ch, int ruleIndex) {

        Deque<StateTransformCache> transformCacheStack = threadLocalTransformStackMap.get(Thread.currentThread().getId());


        StateTransformCache top = transformCacheStack.peek();

        if(top.state != state && ruleIndex != 0) {
            throw new RuntimeException("Transform rule access invalid: stack top: " + top.state + ", expect:" + state);
        }

        if(ruleIndex != 0) {
            Integer result = -1;
            if(ruleIndex < top.transformRules.size()) {
                result = top.transformRules.get(ruleIndex);
            }
            if(ruleIndex >= top.transformRules.size() - 1) {
                transformCacheStack.pop();
            }
            return result;
        }

        // ruleIndex == 0 , try to collect transform rules for state
        List<Integer> rules = new ArrayList<>();
        if(HanziDict.isBMPChineseChar(ch)) {

            CharSequence transformedChars = shapeTransTable.getTransformedChars(state, ch); // 形近转换
            if(transformedChars == null) {
                List<String> pinyinList = HanziDict.getInstance().getPinyin(ch);
                String pinyin = pinyinList.get(0);
                int pinyinCode = PinyinEngine.getInstance().getInfoByPinyin(pinyin).getId();
                transformedChars = pinyinTransTable.getTransformedChars(state, pinyinCode); // 音近转换
            }

            for (int j = 0, length = transformedChars.length(); j < length; j++) {
                int rule = (1 << 16) | transformedChars.charAt(j);
                rules.add(rule);
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
            CharSequence transformedChars = pinyinTransTable.getTransformedChars(state, code);
            if(transformedChars != null) {
                for (int j = 0, length = transformedChars.length(); j < length; j++) {
                    int rule = (consumedChars << 16) | transformedChars.charAt(j);
                    rules.add(rule);
                }
            }
        }

        if(rules.isEmpty()) { // 没有发现任何模糊转换规则
            return -1;
        } else {
            StateTransformCache cache = new StateTransformCache(state, rules);
            transformCacheStack.push(cache);
            return rules.get(0);
        }
    }

    private static class StateTransformCache {
        public int state;
        public List<Integer> transformRules;

        public StateTransformCache(int state, List<Integer> transformRules) {
            this.state = state;
            this.transformRules = transformRules;
        }
    }

    private static class CharSequenceView implements CharSequence {

        private CharSequence charSequence;
        private int start;
        private int end;

        public CharSequenceView(CharSequence charSequence, int start, int end) {
            this.charSequence = charSequence;
            this.start = start;
            this.end = end;
        }

        public CharSequenceView(CharSequence charSequence, int start) {
            this(charSequence, start, charSequence.length());
        }

        @Override
        public int length() {
            return end - start;
        }

        @Override
        public char charAt(int index) {
            return charSequence.charAt(index - start);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            int actualStart = this.start + start;
            int actualEnd = this.end + end;
            return charSequence.subSequence(actualStart, actualEnd);
        }
    }
}


