package org.sun.ahocorasick.hanzi;

import org.sun.ahocorasick.MatchHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PinyinSimilarity {

    private Map<String, List<String>> similarTable;

    public PinyinSimilarity() {

        InputStream resourceAsStream = PinyinSimilarity.class.getResourceAsStream("/pinyinSim.txt");

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



    public List<String> getSimilarPinyins(String pinyin) {

        List<String> similarPinyins = this.similarTable.get(pinyin);
        if(similarPinyins != null) {
            return similarPinyins;
        }

        final List<String> commonPrefixPinyinList = new ArrayList<>();

        // 同前缀的拼音
        class CommonPrefixMatchHandler implements MatchHandler<PinyinInfo> {

            private int mostRight = 1;

            @Override
            public boolean onMatch(int start, int end, String key, PinyinInfo value) {
                if(start > 0 && end > mostRight) {
                    return false;
                } else {
                    if(start == 0 && end > mostRight) {
                        mostRight = end;
                    }
                    if(start == 0) {
                        commonPrefixPinyinList.add(value.getPinyin());
                    }
                    return true;
                }
            }
        }

        PinyinEngine.getInstance().parsePinyin(pinyin, new CommonPrefixMatchHandler());
        return commonPrefixPinyinList;
    }


}
