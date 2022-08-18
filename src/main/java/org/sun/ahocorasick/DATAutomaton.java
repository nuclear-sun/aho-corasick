package org.sun.ahocorasick;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DATAutomaton<V> implements Automaton<V> {

    protected static final int MAX_KEYWORD_LENGTH = 1000;
    protected static final int ANCHOR_DEQUE_CLEAN_THRESHOLD = 20;

    protected final int[] base;

    protected final int[] check;

    protected final WordEntry<V>[] data;

    protected final int stateCount;

    // redundant data
    protected final int reserveLength;

    // controls
    protected final boolean interruptable;

    private DATAutomaton(int[] base, int[] check, WordEntry<V>[] data, int stateCount, boolean interruptable) {
        this.base = base;
        this.check = check;
        this.data = data;
        this.stateCount = stateCount;
        this.reserveLength = (stateCount << 1) + 1;

        this.interruptable = interruptable;
    }

    protected DATAutomaton(DATAutomaton that) {
        this.base = that.base;
        this.check = that.check;
        this.data = that.data;
        this.stateCount = that.stateCount;
        this.reserveLength = that.reserveLength;
        this.interruptable = that.interruptable;
    }

    /**
     * next success state when matching
     * @return next success state or -1 when fail
     */
    public int childState(int currState, char ch) {
        int index = base[currState] + ch;
        if(index >= reserveLength && index < base.length && check[index] == currState) {
            return base[index];
        }
        return -1;
    }

    protected int nextState(int currState, char ch) {

        int nextState = 0, index;

        do {
            index = base[currState] + ch;
            if(index >= reserveLength && index < base.length && check[index] == currState) {
                nextState = base[index];
                return nextState;
            }

            currState = base[currState + stateCount];
            if(currState == 0) {
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
    protected List<Integer> collectWords(final int state) {

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

    // open for test
    public static int calcStart(final Deque<Tuple<Integer, Integer>> anchorQueue, final int end, final int wordLength) {

        int ignoreChars = 0;

        Iterator<Tuple<Integer, Integer>> tupleIterator = anchorQueue.descendingIterator();

        while (tupleIterator.hasNext()) {
            Tuple<Integer, Integer> anchor = tupleIterator.next();

            if(end - anchor.first - ignoreChars >= wordLength) {
                break;
            }
            ignoreChars += anchor.second;
        }

        return end - wordLength - ignoreChars + 1;
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

                    WordEntry<V> datum = this.data[index];
                    int start = calcStart(anchorDeque, i, datum.getKeyword().length());
                    boolean isContinue = handler.onMatch(start, i + 1, datum.getKeyword(), datum.getPayload());
                    if(!isContinue) return;
                }
            }

            if(this.interruptable && Thread.currentThread().isInterrupted()) return;
        }
    }

    protected WordEntry<V> getWordEntryByState(int state) {
        return data[state];
    }

    public static Builder builder() {
        return new Builder();
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

    public void printStats(boolean detail) {

        System.out.println("words: " + data.length);
        System.out.println("stateCount: " + stateCount);
        System.out.println("capacity: " + base.length);
        System.out.println("freeSlots: " + countFreeSlots());

        if(detail) {
            System.out.println();

            System.out.println("index & base & check");
            for (int i = 0; i < base.length; i++) {
                System.out.printf("%5d %5d %5d\n", i, base[i], check[i]);
            }
        }
    }

    public static class WordEntry<V> {

        private final String keyword;
        private final V payload;
        private int wordMetaFlags;
        // may contain extended meta info
        private Object extMetaInfo;


        public WordEntry(String keyword, V value, int wordMetaFlags) {
            this.keyword = keyword;
            this.payload = value;
            this.wordMetaFlags = wordMetaFlags;
        }

        public WordEntry(String keyword, V value) {
            this.keyword = keyword;
            this.payload = value;
        }

        public String getKeyword() {
            return keyword;
        }

        public int getWordMetaFlags() {
            return wordMetaFlags;
        }

        public void setWordMetaFlags(int wordMetaFlags) {
            this.wordMetaFlags = wordMetaFlags;
        }

        public Object getExtMetaInfo() {
            return extMetaInfo;
        }

        public void setExtMetaInfo(Object extMetaInfo) {
            this.extMetaInfo = extMetaInfo;
        }

        public V getPayload() {
            return payload;
        }

    }

    public static class Builder<V> {

        private int[] base;

        private int[] check;

        // start from index 1
        private WordEntry<V>[] data;

        private int stateCount;

        // controls
        private boolean interruptable = false;

        // callback when word entry placed
        private BiConsumer<State<V>, WordEntry<V>> callback;

        // temp
        private Trie<V> trie;
        private Map<String, V> dataMap;

        private Builder() {
            this.dataMap = new HashMap<>();
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

        public void setWordInfoCallback(BiConsumer<State<V>, WordEntry<V>> callback) {
            this.callback = callback;
        }

        public Builder<V> setInterruptable(boolean interruptable) {
            this.interruptable = interruptable;
            return this;
        }

        public DATAutomaton<V> build() {

            buildTrie();
            this.stateCount = trie.getStateCount();
            initDoubleArray();
            placeAllStateInfo();

            return new DATAutomaton<V>(base, check, data, stateCount, interruptable);
        }

        public DATAutomaton<V> buildFromTrie(Trie<V> trie) {

            this.trie = trie;
            this.stateCount = trie.getStateCount();
            initDoubleArray();
            placeAllStateInfo();

            this.trie = null;

            return new DATAutomaton<V>(base, check, data, stateCount, interruptable);
        }

        private void buildTrie() {

            Trie<V> trie = new Trie<>();
            dataMap.forEach((key, value) -> {
                trie.putKeyword(key, value);
            });
            trie.constructFailureAndPrevWordPointer();
            this.trie = trie;
        }


        public Trie<V> getTrie() {
            return this.trie;
        }

        private void initDoubleArray() {

            int initCapacity = this.stateCount << 2;

            this.base = new int[initCapacity];
            this.check = new int[initCapacity];

            // build free list
            final int firstFreeIndex = (stateCount << 1) + 1;
            final int lastFreeIndex = initCapacity - 1;

            check[0] = - firstFreeIndex;
            base[0] = - lastFreeIndex;
            for (int i = firstFreeIndex; i < initCapacity; i++) {
                check[i] = -(i + 1);
                base[i] = - (i - 1);
            }
            check[lastFreeIndex] = 0;
            base[firstFreeIndex] = 0;

        }

        private void resize(int newCapacity) {

            if(newCapacity <= this.base.length) {
                return;
            }

            int[] newBase = new int[newCapacity];
            int[] newCheck = new int[newCapacity];

            System.arraycopy(base, 0, newBase, 0, base.length);
            System.arraycopy(check, 0, newCheck, 0, check.length);

            // build free list
            for (int i = check.length; i < newCapacity; i++) {
                newCheck[i] = -(i + 1);
                newBase[i] = - (i - 1);
            }
            int prevLastFreeIndex = -base[0];
            newCheck[prevLastFreeIndex] = - check.length;
            newCheck[newCapacity - 1] = 0;

            newBase[base.length] = - prevLastFreeIndex;
            newBase[0] = -(newCapacity - 1);

            this.check = newCheck;
            this.base = newBase;
        }


        /**
         * find free indexes for characters of state
         * @param state
         * @return the appropriate base address value, may be negative. Use Integer.MAX_VALUE to indicate failure.
         */
        private int findBaseValue(final State state) {

            Map<Character, State> childrenMap = state.getSuccess();
            if(childrenMap.size() == 0) {
                return 0;
            }

            char[] characters = new char[childrenMap.size()];
            int i = 0;
            for (Character ch : childrenMap.keySet()) {
                characters[i++] = ch;
            }

            Arrays.sort(characters);

            int[] diffs = new int[characters.length];

            for(i = 1; i < characters.length; i++) {
                diffs[i] = characters[i] - characters[0];
            }

            int freeIndex = - check[0];
            while (freeIndex > 0) {

                int maxNeededIndex = freeIndex + diffs[characters.length - 1];
                if(maxNeededIndex >= check.length) {
                    resize(maxNeededIndex + 1);
                } else if(maxNeededIndex < 0) { // overflow
                    throw new OutOfMemoryError();
                }

                for (i = characters.length - 1; i > 0; i--) {
                    int detectIndex = freeIndex + diffs[i];
                    if(check[detectIndex] > 0) break;
                }

                if(i == 0) {
                    return freeIndex - characters[0];
                }

                freeIndex = - check[freeIndex];
            }

            return Integer.MAX_VALUE;
        }


        private void placeAllStateInfo() {

            class IntHolder {
                int i = 1;
            }
            final IntHolder holder = new IntHolder();
            this.data = new WordEntry[trie.getKeywordCount() + 1];

            trie.traverse(state -> {
                int ordinal = state.getOrdinal();

                // 1. place outer index
                final String keyword  = state.getKeyword();
                if(keyword != null) {
                    WordEntry<V> wordEntry = new WordEntry<>(keyword, state.getPayload());
                    this.data[holder.i] = wordEntry;
                    check[ordinal] = holder.i;
                    holder.i++;

                    if(callback != null) {
                        callback.accept(state, wordEntry);
                    }
                }

                // 2. place failure pointer
                State failure = state.getFailure();
                if(failure != null) {
                    base[ordinal + stateCount] = failure.getOrdinal();
                }

                // 3. place previous word pointer
                State prevWordState = state.getPrevWordState();
                if(prevWordState != null) {
                    check[ordinal + stateCount] = prevWordState.getOrdinal();
                }

                // 4. place success table
                placeSuccess(state);
            });
        }

        private void placeSuccess(State state) {
            int baseValue = findBaseValue(state);
            if(baseValue == Integer.MAX_VALUE) {
                throw new RuntimeException("No base value found for state " + state.getOrdinal());
            }
            // place base address
            base[state.getOrdinal()] = baseValue;

            // place goto table
            Map<Character, State> success = state.getSuccess();
            for (Map.Entry<Character, State> entry : success.entrySet()) {
                int freeIndex = baseValue + entry.getKey();
                int prevFreeIndex = - base[freeIndex];
                int nextFreeIndex = - check[freeIndex];

                check[prevFreeIndex] = - nextFreeIndex;
                base[nextFreeIndex] = - prevFreeIndex;

                base[freeIndex] = entry.getValue().getOrdinal();
                check[freeIndex] = state.getOrdinal();
            }

        }

    }
}
