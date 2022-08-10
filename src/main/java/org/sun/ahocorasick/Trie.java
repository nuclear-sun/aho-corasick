package org.sun.ahocorasick;

import java.util.*;

class Trie<V> {

    private State<V> root;

    private int stateCount;

    Trie() {
        this.root = new State();
    }

    int getStateCount() {
        return stateCount;
    }

    State addKeyword(final String keyword) {

        State<V> currState = root;
        for (int i = 0, len = keyword.length(); i < len; i++) {

            char ch = keyword.charAt(i);

            State childState = currState.getSuccess().get(ch);
            if(childState == null) {
                childState = new State();
                currState.getSuccess().put(ch, childState);

            }
            currState = childState;
        }
        currState.setKeyword(keyword);
        return currState;
    }

    State getRootState() {
        return this.root;
    }

    void constructFailureAndPrevWordPointer() {

        int ordinal = 1;
        root.setOrdinal(ordinal++);

        Queue<State> queue = new LinkedList<>();
        Collection<State<V>> directChildren = root.getSuccess().values();

        for (State child : directChildren) {
            child.setFailure(root);
            queue.offer(child);
        }

        while (!queue.isEmpty()) {

            State parentState = queue.poll();
            parentState.setOrdinal(ordinal++);

            Map<Character, State> children = parentState.getSuccess();

            int resolvedChildCnt = 0;
            boolean foundPrevWord = false;
            State parentFailure = parentState;

            while ((parentFailure = parentFailure.getFailure()) != null) {

                // indicate whether either foundPrevWord or resolvedChildCnt changed
                boolean modified = false;

                // set previous word pointer
                if(!foundPrevWord && parentFailure.getKeyword() != null) {
                    parentState.setPrevWordState(parentFailure);
                    foundPrevWord = true;
                    modified = true;
                }

                // set failure pointer
                Map<Character, State> failureChildren = parentFailure.getSuccess();
                if(failureChildren == null || failureChildren.size() == 0) continue;

                for (Map.Entry<Character, State> entry : children.entrySet()) {
                    Character character = entry.getKey();
                    State childState = entry.getValue();

                    State childFailure;

                    if(childState.getFailure() == null && (childFailure = failureChildren.get(character)) != null) {
                        childState.setFailure(childFailure);
                        resolvedChildCnt += 1;
                        modified = true;
                    }

                }

                // break loop if both tasks are done
                if(modified && foundPrevWord && resolvedChildCnt == children.size()) {
                    break;
                }

            }

            for (State childState : children.values()) {
                if(childState.getFailure() == null) {
                    childState.setFailure(root);
                }
                queue.offer(childState);
            }

        }

        this.stateCount = ordinal - 1;
    }

}
