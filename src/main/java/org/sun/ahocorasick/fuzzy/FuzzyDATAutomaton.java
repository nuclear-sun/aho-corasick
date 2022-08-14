package org.sun.ahocorasick.fuzzy;

import org.sun.ahocorasick.DATAutomaton;
import org.sun.ahocorasick.Emit;
import org.sun.ahocorasick.MatchHandler;
import org.sun.ahocorasick.Tuple;

import java.util.*;

public class FuzzyDATAutomaton<V> extends DATAutomaton<V> implements FuzzyAutomaton<V>{

    private Transformer transformer = new C2CTransformer();

    private PreProcessor preProcessor;

    public FuzzyDATAutomaton(DATAutomaton<V> that, Transformer transformer, PreProcessor processor) {
        super(that);
        this.transformer = transformer;
        this.preProcessor = processor;
    }

    @Override
    public void parseText(CharSequence text, MatchHandler<V> handler) {

        if(text == null || handler == null) {
            return;
        }

        int currState = 1;

        // used to calculate left edge when some special chars are ignored
        final Deque<Tuple<Integer, Integer>> anchorDeque = new LinkedList<>();
        int totalIgnoreChars = 0;


        for (int i = 0, length = text.length(); i < length; i++) {

            final char ch = text.charAt(i);

            if(ch == 0) {  // treat '\0' as the only special case to ignore
                totalIgnoreChars += 1;

                Tuple<Integer, Integer> last;
                if(anchorDeque.size() == 0 || (last = anchorDeque.getLast()).first != i - 1) {
                    Tuple<Integer, Integer> newItem = new Tuple<>(i, 1);
                    anchorDeque.offerLast(newItem);

                    // clean anchorDeque when new item added
                    if(anchorDeque.size() > ANCHOR_DEQUE_CLEAN_THRESHOLD) {
                        Tuple<Integer, Integer> firstItem;
                        while (anchorDeque.size() > 1 &&
                                i - (firstItem = anchorDeque.getFirst()).first -
                                        (totalIgnoreChars - firstItem.second) >= MAX_KEYWORD_LENGTH) {
                            totalIgnoreChars -= firstItem.second;
                            anchorDeque.removeFirst();
                        }
                    }

                } else {                 //   last != null && i == last.getFirst() + 1
                    last.first = i;
                    last.second += 1;
                }

                continue;
            }


            currState = nextState(currState, ch);

            List<Integer> outputs = collectWords(currState);

            if(outputs != null) {
                for (Integer index : outputs) {

                    Tuple<String, V> datum = this.data[index];
                    int start = calcStart(anchorDeque, i, datum.first.length());
                    boolean isContinue = handler.onMatch(start, i + 1, datum.first, datum.second);
                    if(!isContinue) return;
                }
            }

            if(this.interruptable && Thread.currentThread().isInterrupted()) return;
        }
    }

    void tryCollect(int state, int i, MatchHandler<V> handler) {
        List<Integer> outputs = collectWords(state);

        if(outputs != null) {
            for (Integer index : outputs) {

                Tuple<String, V> datum = this.data[index];
                int start = i - datum.first.length();
                boolean isContinue = handler.onMatch(start, i + 1, datum.first, datum.second);
                if(!isContinue) return;
            }
        }
    }

    public List<Emit<V>> fussyParseText(CharSequence text) {

        final List<Emit<V>> list = new LinkedList<>();

        MatchHandler<V> handler = new MatchHandler<V>() {
            @Override
            public boolean onMatch(int start, int end, String key, V value) {
                list.add(new Emit<V>(key, start, end, value));
                return true;
            }
        };

        fussyParseText(text, handler);

        return list;
    }


    public void fussyParseText(CharSequence text, MatchHandler<V> handler) {

        if(text == null || handler == null) {
            return;
        }


        //Deque<Tuple<Integer, Integer>> stateStack = new ArrayDeque<>();

        Deque<Tuple<Integer, RuleBuffer>> stateStack = new ArrayDeque<>();

        Deque<Tuple<Character, Integer>> charStack = new ArrayDeque<>();
        stateStack.push(new Tuple<>(0, null));  // 哨兵数据
        charStack.push(new Tuple<>('\0', -1));

        int state = 1;
        int i = 0, length = text.length();
        char ch = text.charAt(0);

        while (i < length || stateStack.size() > 1) {

            if (stateStack.peek().first < state) {

                RuleBuffer ruleBuffer;

                int child = childState(state, preProcessor.process(ch));

                if(child > 0) {                 // 成功跳转
                    i++;
                    if(i < length) {
                        ch = text.charAt(i);
                    }
                    state = child;
                    tryCollect(state, i, handler);

                } else if(!canFussyMatch(state, ch, stateStack) ||
                        (ruleBuffer = transformer.getTransformRules(this, state, text, i, ch)) == null) { // 不支持模糊转换

                    if(stateStack.size() == 1) {          // 模糊状态栈为空，进行失配跳转
                        state = nextState(state, ch);
                        i++;
                        if(i < length) {
                            ch = text.charAt(i);
                        }
                        tryCollect(state, i, handler);
                    } else {                              // 恢复上个模糊转换状态
                        state = stateStack.peek().first;
                        ch = charStack.peek().first;
                        i = charStack.peek().second;
                    }
                } else {
                    stateStack.push(new Tuple<>(state, ruleBuffer));
                    charStack.push(new Tuple<>(ch, i));
                }


            } else {
                assert stateStack.peek().first == state;
                //int ruleIndex = stateStack.peek().second;

                RuleBuffer ruleBuffer = stateStack.peek().second;

                if(!ruleBuffer.hasNextRule()) {
                    stateStack.pop();
                    charStack.pop();

                    if(stateStack.size() == 1) {
                        state = nextState(state, ch);
                        i++;
                        if(i < length) {
                            ch = text.charAt(i);
                        }
                    } else {
                        state = stateStack.peek().first;
                        ch = charStack.peek().first;
                        i = charStack.peek().second;
                    }

                } else {

                    ruleBuffer.nextRule();

                    int consumedChars = ruleBuffer.getConsumedCharNum();

                    // 这个循环是因为可能存在转换为多个字符的场景，此时期望转换后的字符每个都能匹配上
                    char newChar;
                    while ((newChar = ruleBuffer.getNextChar()) != 0) {
                        int child = childState(state, newChar);
                        assert child > 0;
                        state = child;
                    }

                    i += consumedChars;
                    if(i<length) {
                        ch = text.charAt(i);
                    }

                    tryCollect(state, i, handler);
                }

            }

        }

    }

    boolean canFussyMatch(int state, char ch, Deque<Tuple<Integer, RuleBuffer>> stateStack) {
        if(stateStack.size() < 3) return true;
        return false;
    }

//    public static void main(String[] args) {
//
//        Builder builder = DATAutomaton.builder();
//        builder.add("资本主义")
//                .add("资本主×鹏")
//                .add("资本汪")
//                .add("王义鹏")
//                .add("王×鹏");
//        DATAutomaton automaton = builder.build();
//
//        FuzzyDATAutomaton fuzzyAutomaton = new FuzzyDATAutomaton<>(automaton);
//
//        String text = "资本王仪鹏";
//
//        List list = fuzzyAutomaton.fussyParseText(text);
//        System.out.println(list);
//    }

}
