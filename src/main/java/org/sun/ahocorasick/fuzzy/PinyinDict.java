package org.sun.ahocorasick.fuzzy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class PinyinDict {

    private Map<Integer, List<String>> pinyinTable;

    public PinyinDict() {

        InputStream inputStream = PinyinDict.class.getResourceAsStream("/hanzi2pinyin.txt");

        BufferedReader bufferedReader = null;

        Map<Integer, List<String>> pinyinMap = new HashMap<>();

        try {

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                int i = line.indexOf(":");
                int j = line.indexOf("#");

                int codePoint = Integer.parseInt(line.substring(2, i).trim(), 16);

                if(j <= 0) j = line.length();
                String pinyins = line.substring(i+1, j).trim();
                String[] split = pinyins.split(",");

                List<String> pinyinList = new ArrayList<>(split.length);


                for (String pinyin : split) {
                    pinyinList.add(pinyin);
                }

                pinyinMap.put(codePoint, pinyinList);
            }


        } catch (IOException e) {

        } finally {

        }

        this.pinyinTable = pinyinMap;
    }

    public List<String> getPinyinList(int codePoint) {
        return this.pinyinTable.get(codePoint);
    }

    public List<String> getPinyinList(String ch) {
        return getPinyinList(Character.codePointAt(ch, 0));
    }

    public List<String> getPinyinListPlain(String ch) {

        List<String> pinyinList = getPinyinList(ch);
        for (int i = 0, size = pinyinList.size(); i < size; i++) {
            String pinyin = pinyinList.get(i);
            pinyinList.set(i, transformToPlain(pinyin));
        }

        return pinyinList;
    }

    public void printAllPinyin() {

        this.pinyinTable.forEach((key, list) -> {

            StringJoiner joiner = new StringJoiner(",");
            for (String pinyin : list) {
                joiner.add(transformToPlain(pinyin));
            }

            String entry = Integer.toHexString(key) + ":" + joiner.toString();

            System.out.println(entry);
        });
    }


    static String transformToPlain(String pinyin) {

        StringBuilder sb = new StringBuilder(pinyin.length());
        for (int i = 0, length = pinyin.length(); i < length; i++) {
            char ch = pinyin.charAt(i);
            Character convert;
            if(ch > 128 && (convert = pinyinCharMap.get(ch)) != null) {
                sb.append(convert);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private final static Map<Character, Character> pinyinCharMap;

    static {
        pinyinCharMap = new HashMap<>();
        pinyinCharMap.put('ū', 'u');
        pinyinCharMap.put('ú', 'u');
        pinyinCharMap.put('ù', 'u');
        pinyinCharMap.put('ǔ', 'u');
        pinyinCharMap.put('á', 'a');
        pinyinCharMap.put('à', 'a');
        pinyinCharMap.put('ā', 'a');
        pinyinCharMap.put('ǎ', 'a');
        pinyinCharMap.put('ō', 'o');
        pinyinCharMap.put('ǒ', 'o');
        pinyinCharMap.put('ó', 'o');
        pinyinCharMap.put('ò', 'o');
        pinyinCharMap.put('ī', 'i');
        pinyinCharMap.put('ì', 'i');
        pinyinCharMap.put('ǐ', 'i');
        pinyinCharMap.put('í', 'i');
        pinyinCharMap.put('ē', 'e');
        pinyinCharMap.put('ě', 'e');
        pinyinCharMap.put('è', 'e');
        pinyinCharMap.put('é', 'e');
        pinyinCharMap.put('ǚ', 'v');
        pinyinCharMap.put('ǜ', 'v');
        pinyinCharMap.put('ǘ', 'v');
    }

}
