package org.sun.ahocorasick.fuzzy;

import org.sun.ahocorasick.State;
import org.sun.ahocorasick.Trie;
import org.sun.ahocorasick.hanzi.HanziSimilarity;

import java.util.HashMap;
import java.util.Map;

/**
 * A transform table built from a trie ac automaton and similarity table
 */
public class ShapeTransTable {

    private DATransformTable transformTable;

    public ShapeTransTable(Trie<StateMetaInfo> trie) {

        final HanziSimilarity simTable = HanziSimilarity.getInstance();
        final DATransformTable.Builder builder = DATransformTable.builder();

        trie.traverse(state -> {

            Map<Integer, StringBuilder> rawTransMap = new HashMap<>();

            Map<Character, State<StateMetaInfo>> childrenMap = state.getSuccess();

            for (Map.Entry<Character, State<StateMetaInfo>> entry : childrenMap.entrySet()) {
                Character ch = entry.getKey();
                State<StateMetaInfo> childState = entry.getValue();

                if(!childState.getPayload().isSupportFuzzyMatch()) {
                    continue;
                }

                // TODO 这里对于非 BMP 字符是没有考虑的
                String similarChars = simTable.getSimilarChars(ch);
                for (int i = 0, length = similarChars.length(); i < length; i++) {
                    char c = similarChars.charAt(i);
                    StringBuilder transformTargetChars = rawTransMap.get(c);
                    if(transformTargetChars == null) {
                        transformTargetChars = new StringBuilder();
                        rawTransMap.put((int) c, transformTargetChars);
                    }
                    transformTargetChars.append(ch);
                }
            }

            builder.putTransforms(state.getOrdinal(), rawTransMap);
        });

        this.transformTable = builder.build();
    }

    public CharSequence getTransformedChars(int state, int ch) {
        return transformTable.getTransformedChars(state, ch);
    }

}
