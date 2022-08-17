package org.sun.ahocorasick.zhtools;

import org.sun.ahocorasick.MatchHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PinyinSimTable implements SimilarityTable {

    private Map<String, List<String>> similarTable;

    private final PinyinEngine pinyinEngine = PinyinEngine.getInstance();

    private PinyinSimTable() {

        InputStream resourceAsStream = PinyinSimTable.class.getResourceAsStream("/pinyinSim.txt");

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(resourceAsStream));
            String line;

            Map<String, List<String>> similarMap = new HashMap<>();

            while ((line = reader.readLine()) != null) {
                String[] split = line.split(":");
                String key = split[0].trim();
                String value;

                if(split.length == 2 && key.length() > 0 && (value = split[1].trim()).length() > 0) {
                    String[] pinyinArray = value.split(",");
                    List<String> pinyinList = new ArrayList<>(pinyinArray.length);
                    for (String item : pinyinArray) {
                        String pinyin = item.trim();
                        if(pinyin.length() > 0) {
                            pinyinList.add(pinyin);
                        }
                    }
                    similarMap.put(key, pinyinList);
                }
            }

            this.similarTable = similarMap;

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                resourceAsStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getSimilarPinyinOrHeadChar(final String pinyin) {
        List<String> similarPinyins = getSimilarPinyins(pinyin);
        char head = pinyin.charAt(0);
        if(Character.isAlphabetic(head)) {
            String headString = String.valueOf(head);
            if(similarPinyins.isEmpty() || !similarPinyins.get(0).equalsIgnoreCase((headString))) {
                similarPinyins.add(headString);
            }
        }
        return similarPinyins;
    }

    public List<String> getSimilarPinyins(final String pinyin) {

        List<String> similarPinyins = this.similarTable.get(pinyin);
        if(similarPinyins != null) {
            return similarPinyins;
        }

        final List<String> commonPrefixPinyinList = new ArrayList<>();

        // 同前缀的拼音
        class CommonPrefixMatchHandler implements MatchHandler<PinyinInfo> {

            private int mostRight = 0;

            @Override
            public boolean onMatch(int start, int end, String key, PinyinInfo value) {

                if(end > 6) {
                    return false;
                }

                if(start == 0 && end > mostRight) {
                    mostRight = end;
                    if(end != pinyin.length()) {
                        commonPrefixPinyinList.add(value.getText());
                    }
                }
                return true;
            }
        }

        PinyinEngine.getInstance().parsePinyin(pinyin, new CommonPrefixMatchHandler());
        return commonPrefixPinyinList;
    }


    public CharSequence getSimilarPinyinByCode(int pinyinCode) {
        PinyinInfo info = pinyinEngine.getInfoByCode(pinyinCode);
        if(info == null) {
            return null;
        }

        String pinyin = info.getText();
        List<String> similarPinyins = getSimilarPinyins(pinyin);

        StringBuilder sb = new StringBuilder(similarPinyins.size());

        for (String similarPinyin : similarPinyins) {
            int codeByPinyin = pinyinEngine.getCodeByPinyin(similarPinyin);
            if(codeByPinyin != 0) {
                sb.append((char) codeByPinyin);
            }
        }
        return sb.toString();
    }


    public CharSequence getSimilarPinyinOrHeadCharByCode(int pinyinCode) {
        PinyinInfo info = pinyinEngine.getInfoByCode(pinyinCode);
        if(info == null) {
            return null;
        }

        String pinyin = info.getText();
        List<String> similarPinyins = getSimilarPinyinOrHeadChar(pinyin);

        StringBuilder sb = new StringBuilder(similarPinyins.size());

        for (String similarPinyin : similarPinyins) {
            int codeByPinyin = pinyinEngine.getCodeByPinyin(similarPinyin);
            if(codeByPinyin != 0) {
                sb.append((char) codeByPinyin);
            }
        }
        return sb.toString();
    }


    private static final PinyinSimTable instance = new PinyinSimTable();

    public static PinyinSimTable getInstance() {
        return instance;
    }

    @Override
    public CharSequence getSimilarChars(char ch) {
        return getSimilarPinyinByCode(ch);
    }
}
