package org.sun.ahocorasick.zh;

import org.sun.ahocorasick.*;
import org.sun.ahocorasick.fuzzy.FuzzyAutomaton;
import org.sun.ahocorasick.fuzzy.FuzzyDATAutomaton;
import org.sun.ahocorasick.hanzi.HanziDict;
import org.sun.ahocorasick.hanzi.PinyinEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.sun.ahocorasick.zh.Consts.FUSSY_MATCH_FLAG;

public class FCNDATAutomaton<V> implements FuzzyAutomaton<V> {

    private FuzzyDATAutomaton<V> fuzzyDATAutomaton;

    private FCNDATAutomaton(FuzzyDATAutomaton<V> fuzzyDATAutomaton) {
        this.fuzzyDATAutomaton = fuzzyDATAutomaton;
    }

    public static <V> Builder<V> builder() {
        return new Builder<>();
    }

    @Override
    public List<Emit<V>> fussyParseText(CharSequence text) {
        return this.fuzzyDATAutomaton.fussyParseText(text);
    }

    @Override
    public void fussyParseText(CharSequence text, MatchHandler<V> handler) {
        this.fuzzyDATAutomaton.fussyParseText(text, handler);
    }


    public static class Builder<V> {

        private static class WordItem<V> {

            private V value;
            private boolean supportFussyMatch;

            public WordItem(V value, boolean supportFussyMatch) {
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
            dataMap.put(key, new WordItem<>(value, fussyMatch));
            return this;
        }

        public Builder<V> putAll(Map<String, ? extends V> data, boolean fussyMatch) {
            builder.putAll(data);

            data.forEach((key, value) -> {
                dataMap.put(key, new WordItem<>(value, fussyMatch));
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

        // transform origin string to pinyin coding
        private void processBeforeBuild() {

            final Map<String, WordItem<V>> pinyinCodeMap = new HashMap<>(dataMap.size());

            dataMap.forEach((key, item) -> {
                CharSequence pinyinCodes = HanziDict.getInstance().getPinyinForString(key);
                pinyinCodeMap.put(pinyinCodes.toString(), item);
            });

            this.dataMap = pinyinCodeMap;
        }

        private Trie<V> buildTrie() {
            final Trie<V> trie = new Trie<>();
            BuildCallback<V> buildCallback = new BuildCallback<V>() {
                @Override
                public void onStateCreated(State<V> state, String word) {
                }

                @Override
                public void onStateChecked(State<V> state, String word) {
                    if(dataMap.get(word).supportFussyMatch) {
                        state.putData(FUSSY_MATCH_FLAG, true);
                    }
                }

                @Override
                public void onWordAdded(State<V> state, String word) {
                }
            };

            trie.setCallback(buildCallback);
            dataMap.forEach((key, item) -> {
                trie.putKeyword(key, item.value);
            });
            trie.constructFailureAndPrevWordPointer();
            return trie;
        }

        public FCNDATAutomaton build() {

            processBeforeBuild();
            final Trie<V> trie = buildTrie();
            DATAutomaton<V> datAutomaton = builder.buildFromTrie(trie);

            ShapeTransTable shapeTransTable = new ShapeTransTable(trie);
            PinyinTransTable pinyinTransTable = new PinyinTransTable(trie);
            ComplexTransformer complexTransformer = new ComplexTransformer(shapeTransTable, pinyinTransTable);
            PinyinifyProcessor processor = new PinyinifyProcessor();

            FuzzyDATAutomaton<V> fuzzyDATAutomaton = new FuzzyDATAutomaton<>(datAutomaton, complexTransformer, processor);

            return new FCNDATAutomaton(fuzzyDATAutomaton);
        }
    }

}



