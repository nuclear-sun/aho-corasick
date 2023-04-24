package org.sun.ahocorasick.dynamic;

import java.util.*;

public class DAWG {

    private static class Node {

        private static int idGenerator = 0;

        private String id;
        private Map<Character, Edge> children;
        private Node slink;

        public Node(String id) {
            this.id = id;
            children = new HashMap<>();
        }

        public Node() {
            this(String.valueOf(idGenerator++));
        }

        @Override
        public String toString() {
            return "{" +
                    "id='" + id + '\'' +
                    ", children=" + children +
                    ", slink=" + slink +
                    '}';
        }
    }
    private static class Edge {
        private boolean isPrimary;
        private Node target;
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

    private final Node source = new Node();

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
    }

    public void printAllNodes() {
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        visited.add(source);
        queue.add(source);
        while (!queue.isEmpty()) {
            Node node = queue.poll();

            System.out.println(node);

            for (Edge value : node.children.values()) {
                Node child = value.target;
                if(!visited.contains(child)) {
                    queue.offer(child);
                    visited.add(child);
                }
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
                return split(activeNode, a, newActiveNode);
            }
        } else {
            newActiveNode = new Node();
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
        parentNode.children.get(a).target = newChildNode;
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

    public static void main(String[] args) {
        DAWG dawg = new DAWG();
        dawg.addWord("abba");

        dawg.printAllNodes();
    }
}
