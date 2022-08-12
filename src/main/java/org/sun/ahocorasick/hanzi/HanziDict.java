package org.sun.ahocorasick.hanzi;

import java.io.*;
import java.util.*;

public class HanziDict {

    private Map<Integer, List<String>> map;

    private HanziDict() {

        InputStream resourceAsStream = HanziDict.class.getResourceAsStream("/hanzi2plainPinyin.txt");

        BufferedReader bufferedReader = null;
        map = new HashMap<>();

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.split(":");
                int codePoint = Integer.parseInt(split[0].trim(), 16);
                String pinyins = split[1];

                String[] pinyinArray = pinyins.split(",");
                List<String> pinyinList = new ArrayList<>(pinyinArray.length);
                for (String pinyin : pinyinArray) {
                    pinyinList.add(pinyin.trim());
                }

                map.put(codePoint, pinyinList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {}
            }
            if(resourceAsStream != null) {
                try {
                    resourceAsStream.close();
                } catch (Exception e) {}
            }
        }

    }

    public List<String> getPinyin(int codePoint) {
        return map.get(codePoint);
    }

    private static final HanziDict instance = new HanziDict();

    public static HanziDict getInstance() {
        return instance;
    }

    public static boolean isBMPChineseChar(char ch) {
        return ch >= 0x4E00 && ch <= 0x9FFF;
    }
}
