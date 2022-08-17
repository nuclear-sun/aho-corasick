package org.sun.ahocorasick.fuzzy;

import org.sun.ahocorasick.DATAutomaton;
import org.sun.ahocorasick.Emit;
import org.sun.ahocorasick.MatchHandler;
import org.sun.ahocorasick.Tuple;
import org.sun.ahocorasick.fussyzh.TruncatableDeque;

import java.util.*;

public class FuzzyDATAutomaton<V> extends DATAutomaton<V> implements FuzzyAutomaton<V>{

    private Transformer transformer;

    public FuzzyDATAutomaton(DATAutomaton<V> that, Transformer transformer) {
        super(that);
        this.transformer = transformer;
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

    boolean tryCollectAndHandle(int state, int i, MatchHandler<V> handler, TruncatableDeque<Tuple<Integer, Integer>> anchorDeque) {
        List<Integer> outputs = collectWords(state);

        if(outputs != null) {
            for (Integer index : outputs) {

                Tuple<String, V> datum = this.data[index];
                String word = datum.first;
                int start = calcStart(anchorDeque, i, word.length());
                boolean isContinue = handler.onMatch(start, i + 1, word, datum.second);
                if(!isContinue) return false;
            }
        }
        return true;
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


    private static class AnchorRecoverInfo {
        final TruncatableDeque.Node node;
        final int anchorValue1;
        final int anchorValue2;
        final int accumulatedIgnoredChars;

        public AnchorRecoverInfo(TruncatableDeque.Node node, int anchorValue1, int anchorValue2, int accumulatedIgnoredChars) {
            this.node = node;
            this.anchorValue1 = anchorValue1;
            this.anchorValue2 = anchorValue2;
            this.accumulatedIgnoredChars = accumulatedIgnoredChars;
        }
    }


    public void fussyParseText(CharSequence text, MatchHandler<V> handler) {

        if(text == null || handler == null) {
            return;
        }

        final TruncatableDeque<Tuple<Integer, Integer>> anchorDeque = new TruncatableDeque<>();
        int totalIgnoreChars = 0;
        anchorDeque.offerLast(new Tuple<>(0, 0));

        int state = 1;
        int i = 0, length = text.length();
        char ch = text.charAt(0);

        // use these stacks to recover above five states
        Deque<AnchorRecoverInfo> anchorStack = new ArrayDeque<>(); // use this stack to recover anchorDeque and totalIgnoreChars
        Deque<Tuple<Integer, RuleBuffer>> stateStack = new ArrayDeque<>();
        Deque<Tuple<Character, Integer>> charStack = new ArrayDeque<>();
        stateStack.push(new Tuple<>(0, null));  // 哨兵数据
        charStack.push(new Tuple<>('\0', -1));
        anchorStack.push(new AnchorRecoverInfo(null, 0, 0, 0));


        while (i < length || stateStack.size() > 1) {

            if(ch == 0) {  // treat '\0' as the only special case to ignore
                totalIgnoreChars += 1;

                Tuple<Integer, Integer> last;
                if(anchorDeque.size() == 0 || (last = anchorDeque.peekLast()).first != i - 1) {
                    Tuple<Integer, Integer> newItem = new Tuple<>(i, 1);
                    anchorDeque.offerLast(newItem);

                    // clean anchorDeque when new item added
                    if(anchorDeque.size() > ANCHOR_DEQUE_CLEAN_THRESHOLD) {
                        Tuple<Integer, Integer> firstItem;
                        while (anchorDeque.size() > 1 &&
                                i - (firstItem = anchorDeque.peekFirst()).first -
                                        (totalIgnoreChars - firstItem.second) >= MAX_KEYWORD_LENGTH) {
                            totalIgnoreChars -= firstItem.second;
                            anchorDeque.pollFirst();
                        }
                    }

                } else {                 //   last != null && i == last.getFirst() + 1
                    last.first = i;
                    last.second += 1;
                }

                ch = text.charAt(++i);
                continue;
            }

            if (stateStack.peek().first < state) {  // 第一次来到这个状态

                RuleBuffer ruleBuffer;

                int child = childState(state, ch);

                if(child > 0) {                 // 成功跳转
                    i++;
                    if(i < length) {
                        ch = text.charAt(i);
                    }
                    state = child;
                    if(!tryCollectAndHandle(state, i - 1, handler, anchorDeque)) {
                        return;
                    }

                } else if(!canFussyMatch(state, ch, stateStack) ||
                        (ruleBuffer = transformer.getTransformRules(this, state, text, i, ch)) == null) { // 不支持模糊转换

                    if(stateStack.size() == 1) {          // 模糊状态栈为空，进行失配跳转
                        state = nextState(state, ch);
                        i++;
                        if(i < length) {
                            ch = text.charAt(i);
                        }
                        if(!tryCollectAndHandle(state, i - 1, handler, anchorDeque)) {
                            return;
                        }
                    } else {                              // 恢复上个模糊转换状态
                        state = stateStack.peek().first;
                        ch = charStack.peek().first;
                        i = charStack.peek().second;

                        // recover anchor info
                        AnchorRecoverInfo anchorRecoverInfo = anchorStack.peek();
                        anchorDeque.truncateAfter(anchorRecoverInfo.node);
                        anchorDeque.resetLast(new Tuple<>(anchorRecoverInfo.anchorValue1, anchorRecoverInfo.anchorValue2));
                        totalIgnoreChars = anchorRecoverInfo.accumulatedIgnoredChars;
                    }
                } else {  // 发现是一个模糊状态，进行现场保存
                    stateStack.push(new Tuple<>(state, ruleBuffer));
                    charStack.push(new Tuple<>(ch, i));

                    AnchorRecoverInfo anchorRecoverInfo = new AnchorRecoverInfo(
                            anchorDeque.peekLastNode(),
                            anchorDeque.peekLast().first,
                            anchorDeque.peekLast().second,
                            totalIgnoreChars);
                    anchorStack.push(anchorRecoverInfo);
                }

            } else {                                          // 再次来到这个状态
                assert stateStack.peek().first == state;

                RuleBuffer ruleBuffer = stateStack.peek().second;

                if(!ruleBuffer.hasNextRule()) {
                    stateStack.pop();
                    charStack.pop();
                    anchorStack.pop();

                    if(stateStack.size() == 1) {  // 栈空
                        state = nextState(state, ch);
                        i++;
                        if(i < length) {
                            ch = text.charAt(i);
                        }
                    } else {
                        state = stateStack.peek().first;
                        ch = charStack.peek().first;
                        i = charStack.peek().second;

                        // recover anchor info
                        AnchorRecoverInfo anchorRecoverInfo = anchorStack.peek();
                        anchorDeque.truncateAfter(anchorRecoverInfo.node);
                        anchorDeque.resetLast(new Tuple<>(anchorRecoverInfo.anchorValue1, anchorRecoverInfo.anchorValue2));
                        totalIgnoreChars = anchorRecoverInfo.accumulatedIgnoredChars;
                    }

                } else {

                    ruleBuffer.nextRule();
                    int consumedChars = ruleBuffer.getConsumedCharNum();

                    char newChar;
                    int currState = state;
                    // 这个循环是因为可能存在转换为多个字符的场景，此时期望转换后的字符每个都能匹配上，如果匹配不上，说明规则有问题应跳过
                    while ((newChar = ruleBuffer.getNextChar()) != 0 && currState > 0) {
                        currState = childState(currState, newChar);
                    }

                    if(currState > 0) {
                        state = currState;
                        i += consumedChars;
                        if (i < length) {
                            ch = text.charAt(i);
                        }

                        // 如果是该规则拓展出多个字符，对于位置统计是有影响的，只需把多拓展出的位置视为特殊字符忽略即可
                        if(consumedChars > 1) {
                            anchorDeque.offerLast(new Tuple<>(i - 1, consumedChars - 1));
                            totalIgnoreChars += (consumedChars - 1);
                        }

                        if(!tryCollectAndHandle(state, i - consumedChars, handler, anchorDeque)) {
                            return;
                        }
                    }
                }

            }

        }

    }

    boolean canFussyMatch(int state, char ch, Deque<Tuple<Integer, RuleBuffer>> stateStack) {
        if(stateStack.size() < 3) return true;
        return false;
    }

}
