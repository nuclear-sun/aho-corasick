package org.sun.ahocorasick.dynamic;

import java.util.*;

public class DAWG {


    private static class Node {

        private static int idGenerator = 1;

        final int id;
        Map<Character, Edge> children;
        Node slink;
        boolean isTrunk;
        String word;

        public Node(int id) {
            this.id = id;
            children = new HashMap<>();
            isTrunk = false;
        }

        public Node() {
            this(idGenerator++);
        }

        public Node trans(char ch) {
            Edge edge = children.get(ch);
            return edge == null ? null : edge.target;
        }

        @Override
        public String toString() {
            return "{" +
                    "id='" + id + '\'' +
                    ", children=" + children +
                    ", slink=" + (slink == null ? slink : slink.id) +
                    ", isTrunk=" + isTrunk +
                    '}';
        }
    }
    private static class Edge {
        boolean isPrimary;
        Node target;
        public Edge(Node target, boolean isPrimary) {
            this.target = target;
            this.isPrimary = isPrimary;
        }

        @Override
        public String toString() {
            return "{" +
                    "isPrimary=" + isPrimary +
                    ", target=" + target.id +
                    '}';
        }
    }

    private final Node source;

    public DAWG() {
        source = new Node();
        source.isTrunk = true;
    }

    // dynamic insertion
    public void addWords(Collection<String> words) {
        for (String word : words) {
            addWord(word);
        }
    }

    public void addWord(String word) {
        Node activeNode = source;
        for (int i = 0; i < word.length(); i++) {
            activeNode = update(activeNode, word.charAt(i));
        }
        activeNode.word = word;
    }

    public void printAllNodes() {
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        queue.add(source);
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            if(!visited.contains(node)) {
                System.out.println(node);
                visited.add(node);
            }

            for (Edge value : node.children.values()) {
                Node child = value.target;
                queue.offer(child);
            }
        }
    }

    private Node update(Node activeNode, Character a) {
        Edge edge = activeNode.children.get(a);
        Node newActiveNode;
        if(edge != null) {
            newActiveNode = edge.target;
            if(edge.isPrimary) {
                return newActiveNode;
            } else {
                newActiveNode = split(activeNode, a, newActiveNode);
                newActiveNode.isTrunk = true;
                return newActiveNode;
            }
        } else {
            newActiveNode = new Node();
            newActiveNode.isTrunk = true;
            activeNode.children.put(a, new Edge(newActiveNode, true));
            Node currentNode = activeNode, suffixNode = null;

            while (currentNode != source && suffixNode == null) {
                currentNode = currentNode.slink;
                Edge sEdge = currentNode.children.get(a);
                if(sEdge != null && sEdge.isPrimary) {
                    suffixNode = sEdge.target;
                } else if(sEdge != null && !sEdge.isPrimary) {
                    Node childNode = sEdge.target;
                    suffixNode = split(currentNode, a, childNode);
                } else {
                    currentNode.children.put(a, new Edge(newActiveNode, false));
                }
            }

            if(suffixNode == null) {
                suffixNode = source;
            }
            newActiveNode.slink = suffixNode;
        }
        return newActiveNode;
    }

    private Node split(Node parentNode, Character a, Node childNode) {
        Node newChildNode = new Node();
        Edge parentToChild = parentNode.children.get(a);
        assert parentToChild.target == childNode && parentToChild.isPrimary == false;
        parentToChild.target = newChildNode;
        parentToChild.isPrimary = true;
        for(Map.Entry<Character, Edge> entry: childNode.children.entrySet()) {
            newChildNode.children.put(entry.getKey(), new Edge(entry.getValue().target, false));
        }
        newChildNode.slink = childNode.slink;
        childNode.slink = newChildNode;
        Node currentNode = parentNode;
        while (currentNode != null) {
            Edge edge = currentNode.children.get(a);
            if(edge != null && edge.target == childNode && !edge.isPrimary) {
                edge.target = newChildNode;
            } else {
                break;
            }
        }
        return newChildNode;
    }

    private Set<String> collect(Node node) {
        Set<String> results = new HashSet<>();
        Node s = node;
        while (s != source) {
            if(s.isTrunk && s.word != null) {
                results.add(s.word);
            }
            s = s.slink;
        }
        return results;
    }

    public void parse(String text) {
        Node node = source;
        for (int i = 0; i < text.length(); i++) {

            char ch = text.charAt(i);
            if (checkTransition(node, ch)) {
                node = node.trans(ch);
            } else {
                Node s = node.slink;
                while (s != null && !checkTransition(s, ch)) {
                    s = s.slink;
                }
                if(s == null) {
                    node = source;
                    continue; // abandon this char
                } else {
                    node = s.trans(ch);
                }
            }

            if(node != source) {
                Set<String> words = collect(node);
                if(!words.isEmpty()) {
                    System.out.println(i + ":" + words);
                }
            }
        }
    }

    public Set<Node> search(String text) {

        Set<Node> outputs = new HashSet<>();

        Node activeNode = source;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            while (checkTransition(activeNode, ch) && activeNode != source) {
                activeNode = activeNode.slink;
            }
            if(checkTransition(activeNode, ch)) {
                activeNode = activeNode.trans(ch);
            }
            Node outNode = activeNode;
            while (outNode != source) {
                if(outNode.word != null && outNode.isTrunk) {
                    outputs.add(outNode);
                }
                outNode = outNode.slink;
            }
        }

        return outputs;
    }

    private boolean checkTransition(Node node, Character c) {
        if(!node.isTrunk) return false;
        Edge edge = node.children.get(c);
        if(edge == null) return false;
        if(!edge.isPrimary) return false;
        if(!edge.target.isTrunk) return false;
        return true;
    }

    public static void main(String[] args) {
        DAWG dawg = new DAWG();
        dawg.addWord("abba");
        dawg.addWord("aca");
        dawg.addWord("cbb");

        dawg.parse("cbbabbaca");

    }
}
