package org.sun.ahocorasick.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class HanziUtil {

    public HanziUtil() {

        InputStream resourceAsStream = HanziUtil.class.getResourceAsStream("/hanzi2pinyin.txt");

        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.split("#");
                String[] array = split[0].split(":");
                String codePoint = array[0].trim().substring(2);
                String text = array[1].trim();

                String[] pins = text.split(",");
                StringJoiner joiner = new StringJoiner(",");
                for(String origin: pins) {
                    joiner.add(plain(origin.trim()));
                }

                System.out.println(codePoint+":"+joiner.toString());
            }
        } catch (IOException e) {

        } catch (Exception e) {

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

    final static Map<Character, Character> map = new HashMap<>();
    static {
        map.put('à', 'a');
        map.put('ǎ', 'a');
        map.put('ā', 'a');
        map.put('á', 'a');
        map.put('ō', 'o');
        map.put('ó', 'o');
        map.put('ǒ', 'o');
        map.put('ò', 'o');
        map.put('ě', 'e');
        map.put('é', 'e');
        map.put('è', 'e');
        map.put('ē', 'e');
        map.put('ú', 'u');
        map.put('ù', 'u');
        map.put('ǔ', 'u');
        map.put('ū', 'u');
        map.put('ì', 'i');
        map.put('ī', 'i');
        map.put('í', 'i');
        map.put('ǐ', 'i');
        map.put('ń', 'n');
        map.put('ň', 'n');
        map.put('ǹ', 'n');

        map.put('ǚ', 'v');
        map.put('ǜ', 'v');
        map.put('ǘ', 'v');
        map.put('ǖ', 'v');
        map.put('ü', 'v');
        map.put('ḿ', 'm');
        //map.put('m̄', 'm');
    }

    private static String plain(String pinyin) {


        StringBuilder sb = new StringBuilder(pinyin.length());

        for (int i = 0; i < pinyin.length(); i++) {
            char ch = pinyin.charAt(i);
            if(ch < 128) {
                sb.append(ch);
            } else if(map.containsKey(ch)){

                sb.append(map.get(ch));
            } else {
                sb.append(ch);
            }
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        HanziUtil hanziUtil = new HanziUtil();
    }


}
