package org.sun.ahocorasick.hanzi;


import org.sun.ahocorasick.DATAutomaton;
import org.sun.ahocorasick.Emit;
import org.sun.ahocorasick.MatchHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class PinyinEngine {

    // singleton
    private static final PinyinEngine instance = new PinyinEngine();

    public static PinyinEngine getInstance() {
        return instance;
    }


    private Map<Integer, PinyinInfo> pinyinTable;
    private DATAutomaton<PinyinInfo> automaton;

    private PinyinEngine() {

        InputStream inputStream = PinyinEngine.class.getResourceAsStream("/pinyin.txt");
        if(inputStream == null) {
            throw new RuntimeException("Resource pinyin.txt not found.");
        }

        BufferedReader bufferedReader = null;


        Map<Integer, PinyinInfo> pinyinMap = new HashMap<>();
        DATAutomaton.Builder<PinyinInfo> builder = DATAutomaton.builder();


        try {

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            int i = 1;
            while ((line = bufferedReader.readLine()) != null) {

                String[] keyValue = line.split(":");
                String pinyin = keyValue[0];

                PinyinInfo pinyinInfo = new PinyinInfo();
                pinyinInfo.setText(pinyin);
                pinyinInfo.setId(i++);

                builder.put(pinyin, pinyinInfo);
                pinyinMap.put(pinyinInfo.getId(), pinyinInfo);
            }

            this.pinyinTable = pinyinMap;
            this.automaton = builder.build();

        } catch (Exception e) {
            // this should not happen
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if(bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {}
            }
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {}
            }
        }

    }

    /**
     * 获取字母字符串中开头的拼音，如 "jiandan" 返回 "jian", "xjp", 返回 "x"
     * @param text
     * @return
     */
    public PinyinInfo parseFirstGreedyPinyin(String text) {
        if(text == null || text.isEmpty()) {
            return null;
        }

        class FirstGreedyMatchHandler implements MatchHandler<PinyinInfo> {

            private int mostRight = 1;
            private PinyinInfo firstGreedyPinyin;

            @Override
            public boolean onMatch(int start, int end, String key, PinyinInfo value) {
                if(start > 0 && end > mostRight) {
                    return false;
                } else {
                    if(start == 0 && end > mostRight) {
                        mostRight = end;
                        firstGreedyPinyin = value;
                    }
                    return true;
                }
            }

            public PinyinInfo getFirstGreedyMeet() {
                return this.firstGreedyPinyin;
            }
        }

        FirstGreedyMatchHandler matchHandler = new FirstGreedyMatchHandler();
        this.automaton.parseText(text, matchHandler);
        return matchHandler.getFirstGreedyMeet();
    }

    public PinyinInfo getPinyinInfoById(int id) {
        return pinyinTable.get(id);
    }

    public PinyinInfo getInfoByPinyin(String pinyin) {

        PinyinInfo info = parseFirstGreedyPinyin(pinyin);
        if(info == null) {
            return null;
        }

        String text = info.getText();

        if(text.length() == pinyin.length()) {
            return info;
        }

        return null;
    }

    public List<Emit<PinyinInfo>> parsePinyin(CharSequence text) {
        return this.automaton.parseText(text);
    }

    public void parsePinyin(CharSequence text, MatchHandler<PinyinInfo> handler) {
        this.automaton.parseText(text, handler);
    }

}
