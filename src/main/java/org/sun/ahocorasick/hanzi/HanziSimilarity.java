package org.sun.ahocorasick.hanzi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HanziSimilarity {

    private Map<Character, String> similarTable;

    public HanziSimilarity() {

        InputStream resourceAsStream = HanziSimilarity.class.getResourceAsStream("/shapeSim.txt");

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(resourceAsStream));
            String line;

            Map<Character, String> similarMap = new HashMap<>();

            while ((line = reader.readLine()) != null) {
                String[] split = line.split(":");
                String key = split[0].trim();
                String value;

                if(split.length == 2 && key.length() > 0 && (value = split[1].trim()).length() > 0) {
                    similarMap.put(key.charAt(0), value);
                }
            }

            similarTable = similarMap;
        } catch (IOException e) {
            e.printStackTrace();
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

    public double getSimilarity(char ch1, char ch2) {
        String similarChars = similarTable.get(ch1);
        for (int i = 0, length = similarChars.length(); i < length; i++) {
            if(similarChars.charAt(i) == ch2) {
                return 1.0;
            }
        }
        return 0.0;
    }

    public String getSimilarChars(char ch) {
        return similarTable.get(ch);
    }

}
