package org.sun.ahocorasick.fuzzy;

import java.util.*;

/**
 * Use double array to implement a transform table:
 * (state, character) -> [c1, c2, c3, ...]
 */
public class DATransformTable implements TransformTable {
    
    private int[] base;
    private int[] check;

    DATransformTable(int[] base, int[] check) {
        this.base = base;
        this.check = check;
    }

    public CharSequence getTransformedChars(int state, int originChar) {

        if(check[state] != state) {
            return null;
        }

        int firstCharTransIndex = base[state] + originChar;
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

    private int offsetInCheckValue(int value) {
        return (value & 0x7fff0000) >> 16;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Map<Integer, Map<Integer, CharSequence>> transTable;

        private int[] base;
        private int[] check;

        private int maxState = 1;

        // 空闲列表，用于空间分配
        private TreeMap<Integer, Integer> freeList;

        private Builder() {
            transTable = new TreeMap<>();
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

            initDoubleArray();
            placeAccessableChars();
            placeAllLeftTransformChars();

            DATransformTable result = new DATransformTable(base, check);

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

            this.base = new int[initCapacity];
            this.check = new int[initCapacity];

            for (Integer state : transTable.keySet()) {
                check[state] = state;
            }


            freeList = new TreeMap<>();
            freeList.put(1, initCapacity - 1);
        }

        private void occupy(int index) {

            Map.Entry<Integer, Integer> leEntry = freeList.lowerEntry(index + 1);

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

            for (Map.Entry<Integer, Map<Integer, CharSequence>> entry : transTable.entrySet()) {
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
                occupy(state);


                // fill first char transform
                transformMap.forEach((originChar, transformedChars) -> {

                    int targetIndex = base[state] + originChar;
                    base[targetIndex] = transformedChars.charAt(0); // only fill the first char
                    check[targetIndex] = state;
                    occupy(targetIndex);
                });
            }

        }

        private void placeAllLeftTransformChars() {

            // fill all left char transform
            for (Map.Entry<Integer, Map<Integer, CharSequence>> entry : transTable.entrySet()) {
                Integer state = entry.getKey();
                Map<Integer, CharSequence> transformMap = entry.getValue();

                int baseAddress = base[state];

                transformMap.forEach((originChar, transformedChars) -> {

                    if(transformedChars.length() > 1) {
                        placeLeftTransformChars(state, originChar, transformedChars);
                    }
                });
            }

        }


        private final static int CHECK_VALUE_LINK_TAIL = 1 << 31;


        private void placeLeftTransformChars(int state, int originChar, CharSequence targetChars) {
            final int firstCharIndex = base[state] + originChar;

            Integer currFreeIndex = freeList.higherKey(firstCharIndex + Short.MIN_VALUE);

            if(currFreeIndex == null) {
                int newCapacity = base.length + 1 + (targetChars.length() >> 1);
                boolean resized = resize(newCapacity);
                if(!resized) {
                    throw new RuntimeException("Unable to allocate proper slot.");
                }
                currFreeIndex = freeList.higherKey(firstCharIndex);
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

                        nextFreeIndex = freeList.higherKey(currFreeIndex);
                        if(nextFreeIndex == null) {
                            int newCapacity = base.length + 1 + ((length - ci) >> 1);
                            boolean resized = resize(newCapacity);
                            if(!resized) {
                                throw new RuntimeException("Unable to allocate next free slot.");
                            }
                            nextFreeIndex = freeList.higherKey(currFreeIndex);
                        }
                        if(nextFreeIndex - currFreeIndex > Short.MAX_VALUE) {
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
            int charRange = chars[chars.length - 1] - chars[0];


            Map.Entry<Integer, Integer> firstEntry = freeList.firstEntry();
            int rangeStart = firstEntry.getKey();
            int rangeEnd = firstEntry.getValue();
            int baseIndex = rangeStart;


            if(baseIndex + charRange >= base.length) {
                boolean resized = resize(baseIndex + charRange + 1);
                if(!resized) {
                    return Integer.MAX_VALUE;
                }
            }

            while (true) {

                for (i = 0; i < chars.length; i++) {
                    int detectIndex = baseIndex + chars[i] - chars[0];
                    if (check[detectIndex] != 0) {
                        break;
                    }
                }

                if(i >= chars.length) {
                    return baseIndex - chars[0];
                }

                baseIndex += 1;
                if(baseIndex > rangeEnd) {
                    Map.Entry<Integer, Integer> nextFreeRange = freeList.higherEntry(rangeEnd);
                    rangeStart = nextFreeRange.getKey();
                    rangeEnd = nextFreeRange.getValue();
                    baseIndex = rangeStart;
                }

                int nextMaxIndex = baseIndex + charRange;


                if(nextMaxIndex >= base.length) {
                    if(!resize(nextMaxIndex + 1)) {
                        break;
                    }
                }
            }

            return Integer.MAX_VALUE;
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


