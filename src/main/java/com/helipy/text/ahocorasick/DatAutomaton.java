package com.helipy.text.ahocorasick;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @param <V> The related object type
 * @author nuclear-sun
 */
public final class DatAutomaton<V> implements Automaton<V> {

    private static final int MAX_KEYWORD_LENGTH = 1000;
    private static final int ANCHOR_DEQUE_CLEAN_THRESHOLD = 20;

    private final int[] base;

    private final int[] check;

    private final Tuple<String, V>[] data;

    private final int stateCount;

    /**
     * redundant data
     */
    private final int reserveLength;

    /**
     * controls
     */
    private final boolean interruptable;

    private DatAutomaton(int[] base, int[] check, Tuple<String, V>[] data, int stateCount, boolean interruptable) {
        this.base = base;
        this.check = check;
        this.data = data;
        this.stateCount = stateCount;
        this.reserveLength = (stateCount << 1) + 1;

        this.interruptable = interruptable;
    }

    static int calcStart(final Deque<Tuple<Integer, Integer>> anchorQueue, final int end, final int wordLength) {

        int ignoreChars = 0;

        Iterator<Tuple<Integer, Integer>> tupleIterator = anchorQueue.descendingIterator();

        while (tupleIterator.hasNext()) {
            Tuple<Integer, Integer> anchor = tupleIterator.next();

            if (end - anchor.first - ignoreChars >= wordLength) {
                break;
            }
            ignoreChars += anchor.second;
        }

        return end - wordLength - ignoreChars + 1;
    }

    public static Builder builder() {
        return new Builder();
    }

    private int nextState(int currState, char ch) {

        int nextState = 0;
        int index;

        do {
            index = base[currState] + ch;
            if (index >= reserveLength && index < base.length && check[index] == currState) {
                nextState = base[index];
                return nextState;
            }

            currState = base[currState + stateCount];
            if (currState == 0) {
                return 1;
            }

        } while (nextState == 0);

        // this line should never be reached
        return 1;
    }

    /**
     * @param state the state to study
     * @return index for outer resource
     */
    private List<Integer> collectWords(final int state) {

        List<Integer> collector = null;

        int curr = state;
        while (curr > 1) {
            int outerIndex = check[curr];

            if (outerIndex > 0) {
                if (collector == null) {
                    collector = new LinkedList<>();
                }
                collector.add(outerIndex);
            }
            curr = check[curr + stateCount];
        }

        return collector;
    }

    /**
     * // CHECKSTYLE:OFF
     *
     * @param text    text to scan
     * @param handler how to handle matched keyword
     */
    @SuppressWarnings("checkstyle:ReturnCount")
    private void parseTextInner(CharSequence text, MatchHandler<V> handler) {

        if (text == null || handler == null) {
            return;
        }

        int currState = 1;

        // used to calculate left edge when some special chars are ignored
        final Deque<Tuple<Integer, Integer>> anchorDeque = new LinkedList<>();
        int totalIgnoreChars = 0;


        for (int i = 0, length = text.length(); i < length; i++) {

            final char ch = text.charAt(i);

            if (ch == 0) {
                // treat '\0' as the only special case to ignore
                totalIgnoreChars += 1;

                Tuple<Integer, Integer> last = anchorDeque.peekLast();
                if (anchorDeque.isEmpty() || last.first != i - 1) {
                    Tuple<Integer, Integer> newItem = new Tuple<>(i, 1);
                    anchorDeque.offerLast(newItem);

                    // clean anchorDeque when new item added
                    if (anchorDeque.size() > ANCHOR_DEQUE_CLEAN_THRESHOLD) {
                        Tuple<Integer, Integer> firstItem = anchorDeque.peekFirst();
                        while (anchorDeque.size() > 1
                                && i - firstItem.first - (totalIgnoreChars - firstItem.second) >= MAX_KEYWORD_LENGTH) {
                            totalIgnoreChars -= firstItem.second;
                            anchorDeque.removeFirst();
                        }
                    }

                } else {
                    // last != null && i == last.getFirst() + 1
                    last.first = i;
                    last.second += 1;
                }

                continue;
            }


            currState = nextState(currState, ch);

            List<Integer> outputs = collectWords(currState);

            if (outputs != null) {
                for (Integer index : outputs) {

                    Tuple<String, V> datum = this.data[index];
                    int start = calcStart(anchorDeque, i, datum.first.length());
                    boolean isContinue = handler.onMatch(start, i + 1, datum.first, datum.second);
                    if (!isContinue) {
                        return;
                    }
                }
            }

            if (this.interruptable && Thread.currentThread().isInterrupted()) {
                return;
            }
        }
    }
    // CHECKSTYLE:ON

    @Override
    public List<Emit<V>> parseText(CharSequence text) {

        List<Emit<V>> results = new LinkedList<>();

        MatchHandler<V> listener = new MatchHandler<V>() {
            @Override
            public boolean onMatch(int start, int end, String key, V value) {
                Emit<V> emit = new Emit<>(key, start, end, value);
                results.add(emit);
                return true;
            }
        };

        parseTextInner(text, listener);

        return results;
    }

    @Override
    public List<Emit<V>> parseText(CharSequence text, MatchHandler<V> handler) {
        List<Emit<V>> results = new LinkedList<>();
        MatchHandler<V> listener = new MatchHandler<V>() {
            @Override
            public boolean onMatch(int start, int end, String key, V value) {
                Emit<V> emit = new Emit<>(key, start, end, value);
                results.add(emit);
                return handler.onMatch(start, end, key, value);
            }
        };
        parseTextInner(text, listener);

        return results;
    }

    // methods for statistics

    public int getStateCount() {
        return this.stateCount;
    }

    public int getArrayLength() {
        return base.length;
    }

    public int getKeywordSize() {
        return data.length;
    }

    public boolean getInterruptable() {
        return this.interruptable;
    }

    private int countFreeSlots() {
        int count = 0;
        int ind = -check[0];

        while (ind != 0) {
            count += 1;
            ind = -check[ind];
        }
        return count;
    }

    public void printStats(boolean detail, PrintStream out) {

        out.println("words: " + data.length);
        out.println("stateCount: " + stateCount);
        out.println("capacity: " + base.length);
        out.println("freeSlots: " + countFreeSlots());

        if (detail) {
            out.println();

            out.println("index & base & check");
            for (int i = 0; i < base.length; i++) {
                out.printf("%5d %5d %5d%n", i, base[i], check[i]);
            }
        }
    }

    private static class Tuple<V1, V2> {

        private V1 first;
        private V2 second;

        Tuple(V1 first, V2 second) {
            this.first = first;
            this.second = second;
        }
    }


    public static final class Builder<V> {

        private int[] base;

        private int[] check;

        // start from index 1
        private Tuple<String, V>[] data;

        private int stateCount;

        // controls
        private boolean interruptable = false;

        // temp
        private Trie trie;
        private Map<String, V> dataMap;
        private Map<String, Integer> invertedIndex;

        private Builder() {
            this.dataMap = new HashMap<>();
            this.invertedIndex = new HashMap<>();
        }


        public Builder<V> put(String key, V value) {
            this.dataMap.put(key, value);
            return this;
        }

        public Builder<V> putAll(Map<String, ? extends V> data) {
            this.dataMap.putAll(data);
            return this;
        }

        public Builder<V> add(String key) {
            this.dataMap.put(key, null);
            return this;
        }

        public Builder<V> addAll(Collection<String> keys) {
            for (String key : keys) {
                this.dataMap.put(key, null);
            }
            return this;
        }

        public Builder<V> setInterruptable(boolean interruptable) {
            this.interruptable = interruptable;
            return this;
        }

        public DatAutomaton<V> build() {

            prepareData();
            buildTrie();
            this.stateCount = trie.getStateCount();
            initDoubleArray();
            placeAllStateInfo();

            return new DatAutomaton<V>(base, check, data, stateCount, interruptable);
        }


        private void prepareData() {

            this.data = new Tuple[dataMap.size() + 1];
            int i = 1;
            for (Map.Entry<String, V> mapEntry : dataMap.entrySet()) {
                Tuple<String, V> dataEntry = new Tuple<>(mapEntry.getKey(), mapEntry.getValue());
                this.data[i] = dataEntry;
                invertedIndex.put(mapEntry.getKey(), i);
                i++;
            }

        }

        private void buildTrie() {

            Trie trieTmp = new Trie();

            for (int i = 1; i < data.length; i++) {
                trieTmp.addKeyword(data[i].first);
            }

            trieTmp.constructFailureAndPrevWordPointer();
            this.trie = trieTmp;
        }

        private void initDoubleArray() {

            int initCapacity = this.stateCount << 2;

            this.base = new int[initCapacity];
            this.check = new int[initCapacity];

            // build free list
            final int firstFreeIndex = (stateCount << 1) + 1;
            final int lastFreeIndex = initCapacity - 1;

            check[0] = -firstFreeIndex;
            base[0] = -lastFreeIndex;
            for (int i = firstFreeIndex; i < initCapacity; i++) {
                check[i] = -(i + 1);
                base[i] = -(i - 1);
            }
            check[lastFreeIndex] = 0;
            base[firstFreeIndex] = 0;

        }

        private void resize(int newCapacity) {

            if (newCapacity <= this.base.length) {
                return;
            }

            int[] newBase = new int[newCapacity];
            int[] newCheck = new int[newCapacity];

            System.arraycopy(base, 0, newBase, 0, base.length);
            System.arraycopy(check, 0, newCheck, 0, check.length);

            // build free list
            for (int i = check.length; i < newCapacity; i++) {
                newCheck[i] = -(i + 1);
                newBase[i] = -(i - 1);
            }
            int prevLastFreeIndex = -base[0];
            newCheck[prevLastFreeIndex] = -check.length;
            newCheck[newCapacity - 1] = 0;

            newBase[base.length] = -prevLastFreeIndex;
            newBase[0] = -(newCapacity - 1);

            this.check = newCheck;
            this.base = newBase;
        }


        /**
         * find free indexes for characters of state
         *
         * @param state
         * @return the appropriate base address value, may be negative. Use Integer.MAX_VALUE to indicate failure.
         */
        private int findBaseValue(final State state) {

            Map<Character, State> childrenMap = state.getSuccess();
            if (childrenMap.size() == 0) {
                return 0;
            }

            char[] characters = new char[childrenMap.size()];
            int i = 0;
            for (Character ch : childrenMap.keySet()) {
                characters[i++] = ch;
            }

            Arrays.sort(characters);

            int[] diffs = new int[characters.length];

            for (i = 1; i < characters.length; i++) {
                diffs[i] = characters[i] - characters[0];
            }

            int freeIndex = -check[0];
            while (freeIndex > 0) {

                int maxNeededIndex = freeIndex + diffs[characters.length - 1];
                if (maxNeededIndex >= check.length) {
                    resize(maxNeededIndex + 1);
                } else if (maxNeededIndex < 0) { // overflow
                    throw new OutOfMemoryError();
                }

                for (i = characters.length - 1; i > 0; i--) {
                    int detectIndex = freeIndex + diffs[i];
                    if (check[detectIndex] > 0) {
                        break;
                    }
                }

                if (i == 0) {
                    return freeIndex - characters[0];
                }

                freeIndex = -check[freeIndex];
            }

            return Integer.MAX_VALUE;
        }

        private void placeAllStateInfo() {

            Queue<State> queue = new LinkedList<>();

            queue.offer(trie.getRootState());

            while (!queue.isEmpty()) {

                State state = queue.poll();
                int ordinal = state.getOrdinal();

                // 1. place outer index
                final String keyword = state.getKeyword();
                if (keyword != null) {
                    check[ordinal] = invertedIndex.get(keyword);
                }

                // 2. place failure pointer
                State failure = state.getFailure();
                if (failure != null) {
                    base[ordinal + stateCount] = failure.getOrdinal();
                }

                // 3. place previous word pointer
                State prevWordState = state.getPrevWordState();
                if (prevWordState != null) {
                    check[ordinal + stateCount] = prevWordState.getOrdinal();
                }

                // 4. place success table
                placeSuccess(state);

                for (State child : state.getSuccess().values()) {
                    queue.offer(child);
                }
            }
        }


        private void placeSuccess(State state) {
            int baseValue = findBaseValue(state);
            if (baseValue == Integer.MAX_VALUE) {
                throw new RuntimeException("No base value found for state " + state.getOrdinal());
            }
            // place base address
            base[state.getOrdinal()] = baseValue;

            // place goto table
            Map<Character, State> success = state.getSuccess();
            for (Map.Entry<Character, State> entry : success.entrySet()) {
                int freeIndex = baseValue + entry.getKey();
                int prevFreeIndex = -base[freeIndex];
                int nextFreeIndex = -check[freeIndex];

                check[prevFreeIndex] = -nextFreeIndex;
                base[nextFreeIndex] = -prevFreeIndex;

                base[freeIndex] = entry.getValue().getOrdinal();
                check[freeIndex] = state.getOrdinal();
            }

        }

    }
}
