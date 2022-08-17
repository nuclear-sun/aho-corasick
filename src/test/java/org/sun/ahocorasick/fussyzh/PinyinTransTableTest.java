package org.sun.ahocorasick.fussyzh;

import org.sun.ahocorasick.BuildCallback;
import org.sun.ahocorasick.State;
import org.sun.ahocorasick.Trie;
import org.sun.ahocorasick.zhtools.HanziDict;
import org.sun.ahocorasick.zhtools.PinyinEngine;
import org.sun.ahocorasick.zhtools.PinyinSimTable;
import org.testng.annotations.Test;

import java.util.List;

import static org.sun.ahocorasick.fussyzh.Constants.FUSSY_MATCH_FLAG;

public class PinyinTransTableTest {

    @Test
    public void testGetTransformedChars() {

        BuildCallback callback = new BuildCallback() {
            @Override
            public void onStateCreated(State state, String word, State parentState, char ch) {

            }

            @Override
            public void onStateChecked(State state, String word, State parentState, char ch) {
                int pinyinCode = HanziDict.getInstance().getPinyinCode(ch);
                parentState.getSuccess().put((char)pinyinCode, state);
            }

            @Override
            public void onWordAdded(State state, String word) {

            }
        };

        Trie trie = new Trie();
        trie.setCallback(callback);
        trie.addKeyword("习大大");
        trie.constructFailureAndPrevWordPointer();

        PinyinTransTable pinyinTransTable = new PinyinTransTable(trie);

        int xi = PinyinEngine.getInstance().getCodeByPinyin("xi");
        CharSequence transformedChars = pinyinTransTable.getTransformedChars(1, xi);

        System.out.println(transformedChars);

        CharSequence xing = pinyinTransTable.getTransformedChars(1, PinyinEngine.getInstance().getCodeByPinyin("xing"));

        System.out.println(xing);

        List<String> xi1 = PinyinSimTable.getInstance().getSimilarPinyins("xi");
        System.out.println(xi1);
    }
}