package org.sun.ahocorasick.zh;

import org.sun.ahocorasick.fuzzy.PreProcessor;
import org.sun.ahocorasick.hanzi.HanziDict;
import org.sun.ahocorasick.hanzi.PinyinEngine;
import org.sun.ahocorasick.hanzi.PinyinInfo;

import java.util.List;

public class PinyinCodePreProcessor implements PreProcessor {

    @Override
    public char process(char ch) {

        if(HanziDict.isBMPChineseChar(ch)) {
            List<String> pinyin = HanziDict.getInstance().getPinyin(ch);
            String firstPinyin = pinyin.get(0);
            PinyinInfo info = PinyinEngine.getInstance().getInfoByPinyin(firstPinyin);
            return (char) info.getId();
        } else {
            return ch;
        }
    }
}
