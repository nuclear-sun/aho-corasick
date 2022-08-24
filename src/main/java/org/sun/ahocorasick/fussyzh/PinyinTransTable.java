package org.sun.ahocorasick.fussyzh;

import org.sun.ahocorasick.State;
import org.sun.ahocorasick.Trie;
import org.sun.ahocorasick.fuzzy.DATransformTable;
import org.sun.ahocorasick.fuzzy.TransformTable;
import org.sun.ahocorasick.zhtools.HanziDict;
import org.sun.ahocorasick.zhtools.PinyinSimTable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.sun.ahocorasick.fussyzh.Constants.FUSSY_MATCH_FLAG;

public class PinyinTransTable implements TransformTable {

    private DATransformTable transformTable;

    public PinyinTransTable(Trie<?> trie) {

        final PinyinSimTable simTable = PinyinSimTable.getInstance();

        final DATransformTable.Builder builder = DATransformTable.builder();

        trie.traverse(state -> {

            Map<Integer, StringBuilder> rawTransMap = new HashMap<>();

            Map<Character, ? extends State<?>> childrenMap = state.getSuccess();

            // 处理同一个状态下相同拼音，如存在词库： [以为, 易学习], 这种情况无法直接在前缀树上添加拼音编码
            Map<Integer, StringBuilder> samePinyinSet = new HashMap<>();

            for (Map.Entry<Character, ? extends State<?>> entry : childrenMap.entrySet()) {
                Character ch = entry.getKey();
                State childState = entry.getValue();
                if(!HanziDict.isBMPChineseChar(ch)) {
                    continue;
                }

                Boolean supportFuzzyMatch = (Boolean) childState.getData(FUSSY_MATCH_FLAG);

                if(supportFuzzyMatch == null || !supportFuzzyMatch) {
                    continue;
                }

                // TODO 这里对于非 BMP 字符是没有考虑的
                int pinyinCode = HanziDict.getInstance().getPinyinCode(ch);

                // 处理同一个状态下相同拼音，如存在词库： [以为, 易学习], 这种情况无法直接在前缀树上添加拼音编码
                StringBuilder sameCharSet = samePinyinSet.get(pinyinCode);
                if(sameCharSet == null) {
                    sameCharSet = new StringBuilder();
                    samePinyinSet.put(pinyinCode, sameCharSet);
                }
                sameCharSet.append(ch);

                CharSequence similarChars = simTable.getSimilarPinyinOrHeadCharByCode(pinyinCode); // 这里是不同的地方
                if(similarChars != null) {
                    for (int i = 0, length = similarChars.length(); i < length; i++) {
                        char c = similarChars.charAt(i);
                        StringBuilder transformTargetChars = rawTransMap.get(c);
                        if (transformTargetChars == null) {
                            transformTargetChars = new StringBuilder();
                            rawTransMap.put((int) c, transformTargetChars);
                        }
                        transformTargetChars.append(ch);
                    }
                }
            }

            samePinyinSet.forEach((pinyinCode, charSet) -> {
                if(charSet.length() > 1) {
                    rawTransMap.put(pinyinCode, charSet);
                }
            });

            builder.putTransforms(state.getOrdinal(), rawTransMap);
        });

        this.transformTable = builder.build();
    }

    public CharSequence getTransformedChars(int state, int ch) {
        return transformTable.getTransformedChars(state, ch);
    }

}
