package org.sun.ahocorasick.fussyzh;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class TruncatableDeque<E> implements Deque<E> {

    public static class Node<E> {
        private E element;
        private Node<E> prev;
        private Node<E> next;

        public Node(E element) {
            this.element = element;
        }
    }

    private Node<E> first;
    private Node<E> last;
    private int size;

    public boolean offerLast(E element) {

        Node<E> newNode = new Node<>(element);
        newNode.prev = last;

        if(last != null) {
            last.next = newNode;
            last = newNode;
        } else {
            first = last = newNode;
        }

        size++;
        return true;
    }

    @Override
    public E removeFirst() {
        return null;
    }

    @Override
    public E removeLast() {
        return null;
    }

    @Override
    public void addFirst(E e) {

    }

    @Override
    public void addLast(E e) {

    }

    public boolean offerFirst(E element) {
        Node<E> newNode = new Node<>(element);
        newNode.next = first;

        if(first != null) {
            first.prev = newNode;
            first = newNode;
        } else {
            first = last = newNode;
        }

        size++;
        return true;
    }

    public E pollFirst() {
        if(first == null) {
            return null;
        }

        E result = first.element;

        Node<E> next = first.next;
        if(next != null) {
            next.prev = null;
        } else {
            last = null;
        }

        first.next = null;
        first = next;

        size--;
        return result;
    }

    public E pollLast() {

        if(last == null) {
            return null;
        }

        Node<E> prev = last.prev;
        if(prev != null) {
            prev.next = null;
        } else {
            first = null;
        }
        last.prev = null;
        E result = last.element;
        last = prev;

        size--;
        return result;
    }

    @Override
    public E getFirst() {
        return null;
    }

    @Override
    public E getLast() {
        return null;
    }


    public E peekLast() {
        return last == null ? null : last.element;
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return false;
    }

    @Override
    public boolean add(E e) {
        return false;
    }

    @Override
    public boolean offer(E e) {
        return false;
    }

    @Override
    public E remove() {
        return null;
    }

    @Override
    public E poll() {
        return null;
    }

    @Override
    public E element() {
        return null;
    }

    @Override
    public E peek() {
        return null;
    }

    @Override
    public void push(E e) {

    }

    @Override
    public E pop() {
        return null;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    public Node<E> peekLastNode() {
        return last;
    }

    public E peekFirst() {
        return first == null ? null : first.element;
    }

    public void truncateAfter(Node<E> node) {
        Node<E> next = node.next;
        if(next == null) {
            return;
        }

        Node<E> p = next;
        int count = 0;
        while (p != null) {
            count++;
            p = p.next;
        }
        size = size - count;

        next.prev = null;
        node.next = null;
        last = node;
    }

    public void resetLast(E element) {
        if(last == null) {
            throw new NoSuchElementException("last");
        }
        last.element = element;
    }

    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }




    @Override
    public Iterator<E> descendingIterator() {

        if(last == null) {
            return null;
        }

        class InnerIterator<E> implements Iterator<E> {

            private Node<E> p;

            private InnerIterator() {
                p = (Node<E>) TruncatableDeque.this.last;
            }

            @Override
            public boolean hasNext() {
                return p != null;
            }

            @Override
            public E next() {
                E result = p.element;
                p = p.prev;
                return result;
            }
        }

        return new InnerIterator<>();
    }

}
