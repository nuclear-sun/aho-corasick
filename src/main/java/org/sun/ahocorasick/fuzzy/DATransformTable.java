package org.sun.ahocorasick.fuzzy;

import java.nio.CharBuffer;
import java.util.*;

public class DATransformTable {
    
    private int[] base;
    private int[] check;

    public CharBuffer getTransformedChars(int state, char ch) {
        return null;
    }


    public static void main(String[] args) {


        String s = "\uD863\uDC47";

        System.out.println((int)s.charAt(1));

        System.out.println(s.length());

        StringBuilder sb = new StringBuilder();

        char[] chars = Character.toChars(166983);

        int i = Character.codePointAt(s, 0);
        System.out.println(i);
        System.out.println(Integer.toHexString(i));
    }





}

class DATTBuilder {

    private Map<Integer, Map<Integer, String>> transTable;

    private int[] base;
    private int[] check;

    private int maxState = 1;

    // 空闲列表，用于空间分配
    private TreeMap<Integer, Integer> freeList;


    public void putTransforms(int state, int source, String target) {

        Map<Integer, String> transformMap = transTable.get(state);
        if(transformMap == null) {
            transformMap = new HashMap<>();
            transTable.put(state, transformMap);
        }
        transformMap.put(source, target);

        if(maxState < state) {
            maxState = state;
        }
    }

    public DATransformTable build() {
        return null;
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

        for (Map.Entry<Integer, Map<Integer, String>> entry : transTable.entrySet()) {
            Integer state = entry.getKey();
            Map<Integer, String> transformMap = entry.getValue();

            int baseValue = findBaseValue(state);
            if(baseValue == Integer.MIN_VALUE) {
                throw new RuntimeException("Unable to place state.");
            }

            // fill base address
            base[state] = baseValue;
            occupy(state);


            // fill first char transform
            transformMap.forEach((originChar, transformedChars) -> {

                int targetIndex = base[state] + originChar;
                base[targetIndex] = transformedChars.charAt(0); // only fill the first char
                check[targetIndex] = state;
                occupy(targetIndex);

            });
        }

        // fill all left char transform
        for (Map.Entry<Integer, Map<Integer, String>> entry : transTable.entrySet()) {
            Integer state = entry.getKey();
            Map<Integer, String> transformMap = entry.getValue();

            int baseAddress = base[state];

            transformMap.forEach((originChar, transformedChars) -> {

                if(transformedChars.length() > 1) {
                    placeLeftTransformChars(state, originChar, transformedChars);
                }
            });
        }


    }


    private final static int CHECK_VALUE_LINK_TAIL = 1 << 31;

    private int offsetInCheckValue(int value) {
        return (value & 0x7fff0000) >> 16;
    }

    private void placeLeftTransformChars(int state, int originChar, String targetChars) {
        final int firstCharIndex = base[state] + originChar;

        int currFreeIndex = freeList.higherKey(firstCharIndex);

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

            int nextFreeIndex = -1;
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

    private String getTargetChars(int state, int originChar) {

        if(base[state] != state) {
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


    /**
     * find base address for state
     * @param state
     * @return a valid base address, or Integer.MAX_VALUE indicating failure
     */
    private int findBaseValue(int state) {
        Map<Integer, String> childrenMap = transTable.get(state);
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
            boolean resized = resize(baseIndex + charRange);
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
                if(!resize(nextMaxIndex)) {
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

        Integer lastFreeRangeStart = freeList.lastKey();
        Integer lastFreeRangeEnd = freeList.get(lastFreeRangeStart);

        if(lastFreeRangeEnd == base.length - 1) {
            freeList.put(lastFreeRangeStart, newCapacity - 1);
        } else {
            freeList.put(base.length, newCapacity - 1);
        }

        this.base = newBase;
        this.check = newCheck;

        return true;
    }



    public static void main(String[] args) {
        DATTBuilder builder = new DATTBuilder();
        System.out.println(builder.base[2]);

    }

}
