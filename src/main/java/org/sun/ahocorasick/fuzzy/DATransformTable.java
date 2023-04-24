package org.sun.ahocorasick.fuzzy;

import java.util.*;

/**
 * Use double array to implement a transform table:
 * (state, character) -> [c1, c2, c3, ...]
 */
public class DATransformTable implements TransformTable {
    
    private final int[] base;
    private final int[] check;

    DATransformTable(int[] base, int[] check) {
        this.base = base;
        this.check = check;
    }

    public CharSequence getTransformedChars(int state, int originChar) {

        if(check[state] != state) {
            return null;
        }

        int firstCharTransIndex = base[state] + originChar;
        if(firstCharTransIndex < 1 || firstCharTransIndex >= base.length || check[firstCharTransIndex] != state) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        int firstValue = base[firstCharTransIndex];

        int offset = firstValue >> 16;
        char firstTransformedChar = (char) firstValue;

        sb.append(firstTransformedChar);

        int currIndex = firstCharTransIndex;

        while (offset != 0) {
            currIndex = currIndex + offset;

            char ch1 = (char) base[currIndex];
            char ch2 = (char) (base[currIndex] >>> 16);
            char ch3 = (char) check[currIndex];

            sb.append(ch1);
            if(ch2 != 0) {
                sb.append(ch2);
            }

            if(ch3 != 0) {
                sb.append(ch3);
            }

            offset = offsetInCheckValue(check[currIndex]);
        }

        return sb.toString();
    }

    static int offsetInCheckValue(int value) {
        return (value << 1) >> 17;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Map<Integer, Map<Integer, CharSequence>> transTable;

        private int[] base;
        private int[] check;

        private int maxState = 1;

        private int initCapacity;

        // 空闲列表，用于空间分配
        private TreeMap<Integer, Integer> freeList;


        // statistics

        private int rc_firstExtendLink = 0;
        private int rc_extendLink = 0;
        private int rc_placeFirstJump_fail = 0;
        private int rc_placeFirstJump_base = 0;
        private int rc_searchBaseAddressFail = 0;

        private int resizeCount = 0;
        public void printStatistics() {
            int length = base.length;
            int emptyCount = 0;
            for (int i = 1; i < length; i++) {
                if(base[i] == 0) {
                    emptyCount ++;
                }
            }

            System.out.println("DATransformTable: Length: " + length + ", EmptyCount: " + emptyCount +
                    ", ResizeCount: " + resizeCount + ", rc_placeFirstJump_base: " + rc_placeFirstJump_base +
                    ", rc_placeFirstJump_fail: " + rc_placeFirstJump_fail +
                    ", rc_firstExtendLink: " + rc_firstExtendLink + ", rc_extendLink： " + rc_extendLink +
                    ", rc_searchBaseAddressFail: " + rc_searchBaseAddressFail);
        }

        private Builder() {
            transTable = new TreeMap<>();
        }

        public void setInitCapacity(int capacity) {
            initCapacity = capacity;
        }

        public void putTransforms(int state, int source, CharSequence targets) {

            Map<Integer, CharSequence> transformMap = transTable.get(state);
            if(transformMap == null) {
                transformMap = new HashMap<>();
                transTable.put(state, transformMap);
            }
            transformMap.put(source, targets);

            if(maxState < state) {
                maxState = state;
            }
        }

        public void putTransforms(int state, Map<Integer, ? extends CharSequence> transMapForState) {

            Map<Integer, CharSequence> transformMap = transTable.get(state);

            if(transformMap == null) {
                transformMap = new HashMap<>();
                transTable.put(state, transformMap);
            }
            transformMap.putAll(transMapForState);

            if(maxState < state) {
                maxState = state;
            }
        }

        public DATransformTable build() {

            long t0 = System.currentTimeMillis();
            initDoubleArray();
            long t1 = System.currentTimeMillis();
            placeAccessableChars();
            long t2 = System.currentTimeMillis();
            placeAllLeftTransformChars();
            long t3 = System.currentTimeMillis();

            DATransformTable result = new DATransformTable(base, check);

            printStatistics();
            System.out.println("initDoubleArray: " + (t1 - t0) +
                    ", placeAccessableChars: " + (t2 - t1) +
                    ", placeAllLeftTransformChars: " + (t3 - t2));

            this.base = null;
            this.check = null;
            this.freeList = null;

            return result;
        }

        private void initDoubleArray() {

            // overflow?
            int initCapacity = this.transTable.size() << 3;
            if(initCapacity < 0) {
                initCapacity = Integer.MAX_VALUE;
            }
            if(initCapacity < maxState) {
                initCapacity = maxState + 1;
            }
            if(initCapacity < this.initCapacity) {
                initCapacity = this.initCapacity;
            } else {
                this.initCapacity = initCapacity;
            }

            this.base = new int[initCapacity];
            this.check = new int[initCapacity];

            freeList = new TreeMap<>();
            freeList.put(1, initCapacity - 1);

            for (Integer state : transTable.keySet()) {
                occupy(state);
                check[state] = state;
            }

        }

        /**
         * Occupy an index and update free list
         */
        private void occupy(int index) {

            Map.Entry<Integer, Integer> leEntry = freeList.lowerEntry(index + 1);

            assert leEntry != null;
            assert leEntry.getValue() >= index;

            if(leEntry.getValue() < index) {
                throw new RuntimeException("Try to occupy an occupied position: " + index);
            }

            int rangeStart = leEntry.getKey();
            int rangeEnd = leEntry.getValue();

            if(rangeStart == rangeEnd) {
                assert rangeStart == index;
                freeList.remove(rangeStart);
            } else {
                if(index == rangeStart) {
                    freeList.remove(rangeStart);
                    freeList.put(index + 1, rangeEnd);
                } else if(index == rangeEnd) {
                    freeList.put(rangeStart, rangeEnd - 1);
                } else {
                    freeList.put(rangeStart, index - 1);
                    freeList.put(index + 1, rangeEnd);
                }
            }
        }

        private void placeAccessableChars() {


            for (Map.Entry<Integer, Map<Integer, CharSequence>> entry : ((TreeMap<Integer, Map<Integer, CharSequence>>) transTable).descendingMap().entrySet()) {
                Integer state = entry.getKey();
                Map<Integer, CharSequence> transformMap = entry.getValue();

                if(transformMap == null || transformMap.isEmpty()) {
                    continue;
                }

                int baseAddress = findBaseAddress(state);
                if(baseAddress == Integer.MAX_VALUE) {
                    throw new RuntimeException("Unable to place state.");
                }

                // fill base address
                base[state] = baseAddress;

                // fill first char transform
                transformMap.forEach((originChar, transformedChars) -> {

                    int targetIndex = base[state] + originChar;
                    occupy(targetIndex);
                    base[targetIndex] = transformedChars.charAt(0); // only fill the first char
                    check[targetIndex] = state;
                });
            }

        }

        private void placeAllLeftTransformChars() {

            // fill all left char transform
            for (Map.Entry<Integer, Map<Integer, CharSequence>> entry : transTable.entrySet()) {
                Integer state = entry.getKey();
                Map<Integer, CharSequence> transformMap = entry.getValue();

                transformMap.forEach((originChar, transformedChars) -> {

                    if(transformedChars.length() > 1) {
                        placeLeftTransformChars(state, originChar, transformedChars);
                    }
                });
            }

        }


        private final static int CHECK_VALUE_LINK_TAIL = 1 << 31;

        private final static int CHECK_OFFSET_MIN_VALUE = - (1 << 14);    // -2^14
        private final static int CHECK_OFFSET_MAX_VALUE = (1 << 14) - 1;


        private void placeLeftTransformChars(int state, int originChar, CharSequence targetChars) {
            final int firstCharIndex = base[state] + originChar;

            Integer currFreeIndex = freeList.higherKey(firstCharIndex + (Short.MIN_VALUE >> 4));

            if(currFreeIndex == null) {
                currFreeIndex = base.length;
                int newCapacity = base.length + 1 + (targetChars.length() >> 1);
                boolean resized = resize(newCapacity);
                rc_firstExtendLink ++;
                if(!resized) {
                    throw new RuntimeException("Unable to allocate proper slot.");
                }
            }

            if(currFreeIndex - firstCharIndex > Short.MAX_VALUE) {
                throw new RuntimeException("Unable to find proper slot.");
            }
            base[firstCharIndex] = ((currFreeIndex - firstCharIndex) << 16) | base[firstCharIndex];

            int ci = 1;

            final int length = targetChars.length();

            while (ci < length) {
                int checkValue = CHECK_VALUE_LINK_TAIL;
                int baseValue = targetChars.charAt(ci++);
                occupy(currFreeIndex);

                Integer nextFreeIndex = -1;
                do {

                    if(ci < length) { // 存在第二个字符？
                        baseValue = (targetChars.charAt(ci++) << 16) | baseValue;
                    } else {
                        break;
                    }

                    if(ci < length) { // 存在第三个字符？
                        checkValue = checkValue | targetChars.charAt(ci++);
                    } else {
                        break;
                    }

                    if(ci < length) { // 存在更多字符？

                        nextFreeIndex = freeList.higherKey(currFreeIndex + random.nextInt(10000));
                        if(nextFreeIndex == null) {
                            nextFreeIndex = base.length;  // 准备分配新空间
                            int newCapacity = base.length + 1 + ((length - ci) >> 1);
                            boolean resized = resize(newCapacity);
                            rc_extendLink++;
                            if(!resized) {
                                throw new RuntimeException("Unable to allocate next free slot.");
                            }
                        }
                        if(nextFreeIndex - currFreeIndex > CHECK_OFFSET_MAX_VALUE) {
                            throw new RuntimeException("Unable to find next free slot.");
                        }
                        checkValue = ((nextFreeIndex - currFreeIndex) << 16) | checkValue;
                    }

                } while (false);

                base[currFreeIndex] = baseValue;
                check[currFreeIndex] = checkValue;

                if(nextFreeIndex > 0) {
                    currFreeIndex = nextFreeIndex;
                }
            }
        }



        private final Random random = new Random();
        /**
         * find base address for state
         * @param state
         * @return a valid base address, or Integer.MAX_VALUE indicating failure
         */
        private int findBaseAddress(int state) {
            Map<Integer, CharSequence> childrenMap = transTable.get(state);
            Set<Integer> childrenChars = childrenMap.keySet();

            int[] chars = new int[childrenChars.size()];
            int i = 0;
            for (Integer ch : childrenChars) {
                chars[i++] = ch;
            }
            Arrays.sort(chars);

            int[] diffs = new int[chars.length];
            for (int j = 0; j < chars.length; j++) {
                diffs[j] = chars[j] - chars[0];
            }

            Map.Entry<Integer, Integer> firstEntry = freeList.firstEntry();
            Map.Entry<Integer, Integer> lastEntry = freeList.lastEntry();

            if(firstEntry == null || lastEntry == null) {
                int result = base.length;
                int newCapacity = Math.max(base.length + (base.length >> 1), base.length + chars[chars.length - 1]);
                resize(newCapacity);
                return result;
            }

            int startPosition = (firstEntry.getKey() + lastEntry.getKey()) >> 1;



            //int randomLowBound = random.nextInt(30000);

            //Map.Entry<Integer, Integer> startEntry = freeList.startEntry();
            Map.Entry<Integer, Integer> startEntry = freeList.higherEntry(startPosition);

            if(startEntry == null) {
                int newCapacity = Math.max(base.length + (base.length >> 1), base.length + chars[chars.length - 1]);
                resize(newCapacity);
                startEntry = freeList.higherEntry(startPosition);
            }

            int rangeStart = startEntry.getKey();
            int rangeEnd = startEntry.getValue();
            int baseIndex = rangeStart;

            while (baseIndex < base.length) {

                int j = 1;
                int detectIndex = baseIndex;
                while (j < diffs.length) {
                    detectIndex = baseIndex + diffs[j];
                    if(detectIndex < check.length && check[detectIndex] != 0) {
                        break;
                    }
                    if(detectIndex >= check.length) {
                        j = diffs.length;
                        detectIndex = baseIndex + diffs[diffs.length - 1];
                        break;
                    }
                    j++;
                }

                if(j >= diffs.length) { // ok
                    if(detectIndex >= check.length) {
                        int newCapacity = Math.max(base.length + (base.length >> 1), detectIndex + 1); // 至少扩为1.5倍，主要的扩容都发生在这里
                        System.out.println("resize: " + base.length + " -> " + newCapacity);
                        boolean resized = resize(newCapacity);
                        rc_placeFirstJump_base ++;
                        if(!resized) {
                            return Integer.MAX_VALUE;
                        }
                    }
                    return baseIndex - chars[0];
                }

                baseIndex ++;
                rc_searchBaseAddressFail ++;
                if(baseIndex > rangeEnd) {
                    Map.Entry<Integer, Integer> nextFreeRange = freeList.higherEntry(rangeEnd);
                    if(nextFreeRange == null) {
                        baseIndex = base.length;
                        break;
                    }
                    rangeStart = nextFreeRange.getKey();
                    rangeEnd = nextFreeRange.getValue();
                    baseIndex = rangeStart;
                }
            }

            // now we have:  baseIndex >= base.length
            int maxDetectIndex = baseIndex + diffs[diffs.length - 1];
            if(maxDetectIndex < 0 || !resize(maxDetectIndex + 1)) {
                return Integer.MAX_VALUE;
            }
            rc_placeFirstJump_fail ++;

            return baseIndex - chars[0];
        }


        /**
         * resize the two array
         * @param newCapacity
         * @return boolean value indicates whether array size changed
         */
        private boolean resize(int newCapacity) {

            if(newCapacity <= base.length) {
                return false;
            }

            resizeCount++;

            int[] newBase = Arrays.copyOf(base, newCapacity);
            int[] newCheck = Arrays.copyOf(check, newCapacity);

            Map.Entry<Integer, Integer> lastEntry = freeList.lastEntry();
            if(lastEntry == null) {
                freeList.put(base.length, newCapacity - 1);
            } else {
                Integer lastFreeRangeStart = lastEntry.getKey();
                Integer lastFreeRangeEnd = lastEntry.getValue();

                if (lastFreeRangeEnd == base.length - 1) {
                    freeList.put(lastFreeRangeStart, newCapacity - 1);
                } else {
                    freeList.put(base.length, newCapacity - 1);
                }
            }

            this.base = newBase;
            this.check = newCheck;

            return true;
        }

    }
}


