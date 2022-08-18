package org.sun.ahocorasick.fussyzh;

import org.sun.ahocorasick.*;
import org.sun.ahocorasick.fuzzy.FuzzyAutomaton;
import org.sun.ahocorasick.fuzzy.FuzzyDATAutomaton;
import org.sun.ahocorasick.zhtools.HanziDict;

import java.util.HashMap;
import java.util.Map;

import static org.sun.ahocorasick.fussyzh.Constants.FUSSY_MATCH_FLAG;

public class FCNDATAutomaton<V> implements FuzzyAutomaton<V> {

    private FuzzyDATAutomaton<V> fuzzyDATAutomaton;

    private FCNDATAutomaton(FuzzyDATAutomaton<V> fuzzyDATAutomaton) {
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

        private Trie<V> buildTrie() {
            final Trie<V> trie = new Trie<>();
            BuildCallback<V> buildCallback = new BuildCallback<V>() {
                @Override
                public void onStateCreated(State<V> state, String word, State<V> parentState, char ch) {
                }

                @Override
                public void onStateChecked(State<V> state, String word, State<V> parentState, char ch) {
                    if(dataMap.get(word).supportFussyMatch) {
                        state.putData(FUSSY_MATCH_FLAG, true);
                        int pinyinCode = HanziDict.getInstance().getPinyinCode(ch);
                        if(pinyinCode != ch) {
                            parentState.getSuccess().put((char) pinyinCode, state);
                        }
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

            final Trie<V> trie = buildTrie();
            builder.setWordInfoCallback((state, wordEntry) -> {
                Boolean fuzzyMatch = (Boolean) state.getData(FUSSY_MATCH_FLAG);
                if(fuzzyMatch != null && fuzzyMatch) {
                    wordEntry.setWordMetaFlags(1);
                }
            });
            DATAutomaton<V> datAutomaton = builder.buildFromTrie(trie);

            ShapeTransTable shapeTransTable = new ShapeTransTable(trie);
            PinyinTransTable pinyinTransTable = new PinyinTransTable(trie);
            ComplexTransformer complexTransformer = new ComplexTransformer(shapeTransTable, pinyinTransTable);
            FuzzyDATAutomaton<V> fuzzyDATAutomaton = new FuzzyDATAutomaton<>(datAutomaton, complexTransformer);

            return new FCNDATAutomaton(fuzzyDATAutomaton);
        }
    }

}



