package org.sun.ahocorasick.fussyzh;

import org.sun.ahocorasick.State;
import org.sun.ahocorasick.Trie;
import org.sun.ahocorasick.fuzzy.DATransformTable;
import org.sun.ahocorasick.fuzzy.TransformTable;
import org.sun.ahocorasick.zhtools.ShapeSimTable;

import java.util.HashMap;
import java.util.Map;

import static org.sun.ahocorasick.fussyzh.Constants.FUSSY_MATCH_FLAG;

/**
 * A transform table built from a trie ac automaton and similarity table
 */
public class ShapeTransTable implements TransformTable {

    private DATransformTable transformTable;

    public ShapeTransTable(Trie<?> trie) {

        final ShapeSimTable simTable = ShapeSimTable.getInstance();
        final DATransformTable.Builder builder = DATransformTable.builder();

        trie.traverse(state -> {

            Map<Integer, StringBuilder> rawTransMap = new HashMap<>();

            Map<Character, ? extends State<?>> childrenMap = state.getSuccess();

            for (Map.Entry<Character, ? extends State<?>> entry : childrenMap.entrySet()) {
                Character ch = entry.getKey();
                State childState = entry.getValue();

                Boolean supportFuzzyMatch = (Boolean) childState.getData(FUSSY_MATCH_FLAG);
                if(supportFuzzyMatch == null || !supportFuzzyMatch) {
                    continue;
                }

                // TODO 这里对于非 BMP 字符是没有考虑的
                String similarChars = simTable.getSimilarChars(ch);
                if(similarChars != null) {
                    for (int i = 0, length = similarChars.length(); i < length; i++) {
                        char c = similarChars.charAt(i);
                        StringBuilder transformTargetChars = rawTransMap.get(c);
                        if (transformTargetChars == null) {
                            transformTargetChars = new StringBuilder(2);
                            rawTransMap.put((int) c, transformTargetChars);
                        }
                        transformTargetChars.append(ch);
                    }
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
