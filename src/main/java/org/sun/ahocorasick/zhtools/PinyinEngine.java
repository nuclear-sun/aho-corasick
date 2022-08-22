package org.sun.ahocorasick.zhtools;


import org.sun.ahocorasick.DATAutomaton;
import org.sun.ahocorasick.Emit;
import org.sun.ahocorasick.MatchHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class PinyinEngine {

    private static class PinyinAutomaton extends DATAutomaton<PinyinInfo> {

        protected PinyinAutomaton(DATAutomaton that) {
            super(that);
        }

        public Emit<PinyinInfo> parseFirstGreedyPinyin(CharSequence text) {

            int i = 0;
            int length = text.length();
            int state = 1;
            int wordState = -1;
            int wordEnd = 0;

            while (state > 0 && i < length) {
                char ch = text.charAt(i++);
                if(ch == 0) {
                    continue;
                }

                int child = childState(state, ch);

                if(child > 1 && check[child] > 0) {
                    wordState = child;
                    wordEnd = i;
                }

                state = child;
            }

            if(wordState > 1) {
                WordEntry<PinyinInfo> wordEntry = data[check[wordState]];
                Emit<PinyinInfo> emit = new Emit<>(wordEntry.getKeyword(), 0, wordEnd, wordEntry.getPayload());
                return emit;
            }
            return null;
        }

    }

    // singleton
    private static final PinyinEngine instance = new PinyinEngine();

    public static PinyinEngine getInstance() {
        return instance;
    }


    private Map<Integer, PinyinInfo> pinyinTable;
    private PinyinAutomaton automaton;

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

            int id = 0xF700;  // 0xF700 ~ 0xF8FF is a reversed area in unicode, use this area for pinyin code here

            while ((line = bufferedReader.readLine()) != null) {

                String[] keyValue = line.split(":");
                String pinyin = keyValue[0];

                PinyinInfo pinyinInfo = new PinyinInfo();
                pinyinInfo.setText(pinyin);

                id++;
                if(pinyin.length() == 1) {
                    pinyinInfo.setCode(pinyin.charAt(0));
                } else {
                    pinyinInfo.setCode(id);
                }

                builder.put(pinyin, pinyinInfo);
                pinyinMap.put(pinyinInfo.getCode(), pinyinInfo);
            }

            this.pinyinTable = pinyinMap;
            this.automaton = new PinyinAutomaton(builder.build());

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
    public Emit<PinyinInfo> parseFirstGreedyPinyin(CharSequence text) {
        if(text == null || text.length() == 0) {
            return null;
        }

        return this.automaton.parseFirstGreedyPinyin(text);
    }

    public PinyinInfo getInfoByCode(int id) {
        return pinyinTable.get(id);
    }

    public PinyinInfo getInfoByPinyin(String pinyin) {

        Emit<PinyinInfo> emit = parseFirstGreedyPinyin(pinyin);
        if(emit == null) {
            return null;
        }

        String text = emit.getValue().getText();

        if(text.length() == pinyin.length()) {
            return emit.getValue();
        }

        return null;
    }

    /**
     * Get a pinyin's code (or id)
     * @param pinyin
     * @return pinyin's code or 0 for error
     */
    public int getCodeByPinyin(String pinyin) {
        if(pinyin == null || pinyin.length() == 0) {
            return 0;
        }
        PinyinInfo info = getInfoByPinyin(pinyin);
        if(info != null) {
            return info.getCode();
        }
        if(pinyin.length() == 1) {
            return pinyin.charAt(0);
        }
        return 0;
    }

    public List<Emit<PinyinInfo>> parsePinyin(CharSequence text) {
        return this.automaton.parseText(text);
    }

    public void parsePinyin(CharSequence text, MatchHandler<PinyinInfo> handler) {
        this.automaton.parseText(text, handler);
    }

}


