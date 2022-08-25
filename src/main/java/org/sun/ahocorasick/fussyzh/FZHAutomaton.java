package org.sun.ahocorasick.fussyzh;

import org.sun.ahocorasick.*;
import org.sun.ahocorasick.fuzzy.FuzzyAutomaton;
import org.sun.ahocorasick.fuzzy.FuzzyDATAutomaton;
import org.sun.ahocorasick.zhtools.HanziDict;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.sun.ahocorasick.fussyzh.Constants.FUSSY_MATCH_FLAG;

public class FZHAutomaton<V> implements FuzzyAutomaton<V> {

    private FuzzyDATAutomaton<V> fuzzyDATAutomaton;

    private FZHAutomaton(FuzzyDATAutomaton<V> fuzzyDATAutomaton) {
        this.fuzzyDATAutomaton = fuzzyDATAutomaton;
    }

    public static <V> Builder<V> builder() {
        return new Builder<>();
    }


    @Override
    public void fussyParseText(CharSequence text, MatchHandler<V> handler) {
        this.fuzzyDATAutomaton.fussyParseText(text, handler);
    }

    public static class Builder<V> {

        private static class WordItem<V> {

            private String key;
            private V value;
            private boolean supportFussyMatch;

            public WordItem(String key, V value, boolean supportFussyMatch) {
                this.key = key;
                this.value = value;
                this.supportFussyMatch = supportFussyMatch;
            }
        }

        private DATAutomaton.Builder<V> builder;
        private Map<String, WordItem<V>> dataMap;


        public Builder() {
            this.builder = DATAutomaton.builder();
            this.dataMap = new HashMap<>();
        }

        public Builder<V> put(String key, V value, boolean fussyMatch) {
            dataMap.put(key, new WordItem<>(key, value, fussyMatch));
            return this;
        }

        public Builder<V> putAll(Map<String, ? extends V> data, boolean fussyMatch) {
            builder.putAll(data);

            data.forEach((key, value) -> {
                dataMap.put(key, new WordItem<>(key, value, fussyMatch));
            });

            return this;
        }

        public Builder<V> put(String key, V value) {
            this.put(key, value, false);
            return this;
        }

        public Builder<V> putAll(Map<String, ? extends V> data) {
            this.putAll(data, false);
            return this;
        }

        // open for test
        void postProcessTrie(Trie<V> trie) {

            // 记录同一节点下相同的拼音编码，这种情况使用近似拼音技术解决，本节点不再添加拼音弧
            final Map<State<V>, Set<Character>> removeSet = new HashMap<>();

            for (Map.Entry<String, WordItem<V>> entry : dataMap.entrySet()) {
                String keyword = entry.getKey();
                WordItem<V> item = entry.getValue();

                if(!item.supportFussyMatch) {
                    continue;
                }

                State<V> currState = trie.getRootState();

                for (int i = 0; i < keyword.length(); i++) {
                    char ch = keyword.charAt(i);
                    State<V> child = currState.getSuccess().get(ch);
                    assert child != null;

                    child.putData(FUSSY_MATCH_FLAG, true);
                    int pinyinCode = HanziDict.getInstance().getPinyinCode(ch);

                    if(pinyinCode != ch) {  // 有效的拼音编码
                        if(currState.getSuccess().containsKey((char) pinyinCode)) {   // 该状态下存在重复的拼音
                            Set<Character> duplicatePinyinSet = removeSet.get(currState);
                            if(duplicatePinyinSet == null) {
                                duplicatePinyinSet = new HashSet<>();
                                removeSet.put(currState, duplicatePinyinSet);
                            }
                            duplicatePinyinSet.add((char) pinyinCode);
                        }
                        currState.getSuccess().put((char) pinyinCode, child);
                    }
                    currState = child;
                }
            }

            // 清楚拼音弧边
            for (Map.Entry<State<V>, Set<Character>> duplicateEntry : removeSet.entrySet()) {
                State<V> state = duplicateEntry.getKey();
                Set<Character> duplicatePinyinCodes = duplicateEntry.getValue();
                for (Character pinyinCode : duplicatePinyinCodes) {
                    state.getSuccess().remove(pinyinCode);
                }
            }
        }

        private Trie<V> buildTrie() {
            final Trie<V> trie = new Trie<>();
            dataMap.forEach((key, item) -> {
                trie.putKeyword(key, item.value);
            });
            trie.constructFailureAndPrevWordPointer();
            postProcessTrie(trie);
            return trie;
        }

        public FZHAutomaton build() {

            long t1 = System.currentTimeMillis();
            final Trie<V> trie = buildTrie();
            builder.setWordInfoCallback((state, wordEntry) -> {
                Boolean fuzzyMatch = (Boolean) state.getData(FUSSY_MATCH_FLAG);
                if(fuzzyMatch != null && fuzzyMatch) {
                    wordEntry.setWordMetaFlags(1);
                }
            });

            long t2 = System.currentTimeMillis();
            System.out.println("Trie complete: " + (t2 - t1));

            ShapeTransTable shapeTransTable = new ShapeTransTable(trie);

            long t3 = System.currentTimeMillis();
            System.out.println("ShapeTransTable complete: " + (t3 - t2));

            PinyinTransTable pinyinTransTable = new PinyinTransTable(trie);
            long t4 = System.currentTimeMillis();
            System.out.println("PinyinTransTable complete: " + (t4 - t3));

            ComplexTransformer complexTransformer = new ComplexTransformer(shapeTransTable, pinyinTransTable);

            DATAutomaton<V> datAutomaton = builder.buildFromTrie(trie);
            long t5 = System.currentTimeMillis();
            System.out.println("DATAutomaton complete: " + (t5 - t4));

            FuzzyDATAutomaton<V> fuzzyDATAutomaton = new FuzzyDATAutomaton<>(datAutomaton, complexTransformer);

            return new FZHAutomaton(fuzzyDATAutomaton);
        }
    }

}



