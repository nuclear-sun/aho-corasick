package org.sun.ahocorasick.zh;

import org.sun.ahocorasick.fuzzy.PreProcessor;
import org.sun.ahocorasick.hanzi.HanziDict;
import org.sun.ahocorasick.hanzi.PinyinEngine;
import org.sun.ahocorasick.hanzi.PinyinInfo;

import java.util.List;

public class PinyinifyProcessor implements PreProcessor {

    @Override
    public char process(char ch) {
        if(HanziDict.isBMPChineseChar(ch)) {
            List<String> pinyinList = HanziDict.getInstance().getPinyin(ch);
            if(pinyinList == null || pinyinList.isEmpty()) {
                return ch;
            }
            PinyinInfo info = PinyinEngine.getInstance().getInfoByPinyin(pinyinList.get(0));
            if(info == null) {
                return ch;
            }
            return (char) info.getId();
        } else {
            return ch;
        }
    }
}
