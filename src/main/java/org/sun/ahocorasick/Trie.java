package org.sun.ahocorasick;

import java.util.*;
import java.util.function.Consumer;

public class Trie<V> implements Automaton<V> {

    private State<V> root;

    private int stateCount = 1;

    private int keywordCount;

    // whether constructed as an ac automaton
    private boolean linked;

    private BuildCallback<V> callback;

    public Trie() {
        this.root = new State();
    }

    public int getStateCount() {
        return stateCount;
    }

    public int getKeywordCount() {
        return keywordCount;
    }

    public void setCallback(BuildCallback<V> callback) {
        this.callback = callback;
    }

    public State addKeyword(final String keyword) {
        return putKeyword(keyword, null);
    }

    public State putKeyword(final String keyword, final V value) {

        State<V> currState = root;
        for (int i = 0, len = keyword.length(); i < len; i++) {

            char ch = keyword.charAt(i);

            State childState = currState.getSuccess().get(ch);
            if(childState == null) {
                childState = new State();
                currState.getSuccess().put(ch, childState);
                this.stateCount++;
                if(callback != null) {
                    callback.onStateCreated(childState, keyword, currState, ch);
                }
            }

            if(callback != null) {
                callback.onStateChecked(childState, keyword, currState, ch);
            }
            currState = childState;
        }
        if(currState.getKeyword() == null) {
            keywordCount += 1;
        }
        currState.setKeyword(keyword);
        currState.setPayload(value);
        if(callback != null) {
            callback.onWordAdded(currState, keyword);
        }
        return currState;
    }

    public State getRootState() {
        return this.root;
    }

    public boolean isLinked() {
        return linked;
    }

    public void constructFailureAndPrevWordPointer() {

        int ordinal = 1;
        root.setOrdinal(ordinal++);

        Queue<State> queue = new LinkedList<>();
        Collection<State<V>> directChildren = root.getSuccess().values();

        Set<State> visitedDirectChildren = new HashSet<>();
        for (State child : directChildren) {
            if(!visitedDirectChildren.contains(child)) {
                child.setFailure(root);
                queue.offer(child);
                visitedDirectChildren.add(child);
            }
        }
        visitedDirectChildren = null;

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

            Set<State> visited = new HashSet<>();
            for (State childState : children.values()) {
                if(childState.getFailure() == null) {
                    childState.setFailure(root);
                }
                if(!visited.contains(childState)) {
                    visited.add(childState);
                    queue.offer(childState);
                }
            }
        }

        this.linked = true;
    }

    @Override
    public void parseText(CharSequence text, MatchHandler<V> handler) {

        if(text == null || handler == null) {
            return;
        }

        State<V> currState = root;
        for (int i = 0, length = text.length(); i < length; i++) {
            char ch = text.charAt(i);
            if(ch == 0) {
                continue;
            }

            currState = nextState(currState, ch);
            List<State<V>> outputs = collectWords(currState);
            if(outputs != null) {
                for (State<V> outputState : outputs) {
                    String keyword = outputState.getKeyword();
                    V value = outputState.getPayload();
                    handler.onMatch(i + 1 - keyword.length(), i + 1, keyword, value);
                }
            }
        }
    }

    private List<State<V>> collectWords(final State<V> state) {

        List<State<V>> result = new ArrayList<>();
        if(state.getKeyword() != null) {
            result.add(state);
        }

        State<V> currState = state.getPrevWordState();
        while (currState != null) {
            result.add(currState);
            currState = currState.getPrevWordState();
        }

        if(result.size() != 0) {
            return result;
        }
        return null;
    }

    private State<V> nextState(final State<V> state, char ch) {
        State<V> currState = state;

        State<V> childState = null;
        while (currState != null) {
            if((childState = currState.getSuccess().get(ch)) != null) {
                return childState;
            }
            currState = currState.getFailure();
        }
        return root;
    }

    public void traverse(Consumer<State<V>> consumer) {

        Queue<State<V>> queue = new LinkedList<>();

        queue.offer(this.root);

        while (!queue.isEmpty()) {
            State<V> item = queue.poll();

            try {
                consumer.accept(item);
            } catch (Exception e) {
                throw e;
            }

            Set<State> visited = new HashSet<>();
            for (State<V> childState : item.getSuccess().values()) {
                if(!visited.contains(childState)) {
                    queue.offer(childState);
                    visited.add(childState);
                }
            }
        }
    }

}
