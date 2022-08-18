package org.sun.ahocorasick.fuzzy;

import org.sun.ahocorasick.fuzzy.TruncatableDeque;
import org.testng.annotations.Test;

import java.util.NoSuchElementException;

import static org.testng.Assert.*;

public class TruncatableDequeTest {

    @Test
    public void testOfferLast() {
        TruncatableDeque<Integer> deque = new TruncatableDeque<>();

        assertEquals(deque.size(), 0);

        deque.offerLast(1);
        deque.offerLast(2);
        deque.offerLast(3);

        assertEquals(deque.size(), 3);

        assertEquals((int) deque.peekLast(), 3);
        assertEquals((int) deque.peekFirst(), 1);

        assertEquals((int) deque.pollLast(), 3);
        assertEquals((int) deque.peekLast(), 2);
        assertEquals((int) deque.pollFirst(), 1);
        assertEquals((int) deque.peekFirst(), 2);

        assertEquals(deque.size(), 1);

        assertEquals((int) deque.pollLast(), 2);

        assertNull(deque.pollFirst());
        assertNull(deque.pollLast());
        assertNull(deque.pollFirst());

        assertEquals(deque.size(), 0);


        assertTrue(deque.offerFirst(5));
        assertTrue(deque.offerLast(6));
        assertEquals((int) deque.pollFirst(), 5);
        assertEquals((int) deque.pollLast(), 6);

        assertEquals(deque.size(), 0);
    }

    @Test
    public void testTruncate() {

        TruncatableDeque<Integer> deque = new TruncatableDeque<>();

        assertThrows(NoSuchElementException.class, () -> {
            deque.resetLast(20);
        });

        deque.offerLast(1);
        deque.offerLast(2);
        deque.offerLast(3);

        TruncatableDeque.Node<Integer> node = deque.peekLastNode();

        deque.offerLast(4);
        deque.offerLast(5);
        deque.offerLast(6);

        assertEquals((int) deque.peekLast(), 6);

        assertEquals(deque.size(), 6);

        deque.truncateAfter(node);
        assertEquals((int) deque.peekLast(), 3);
        deque.resetLast(9);
        assertEquals((int) deque.peekLast(), 9);

        assertEquals(deque.size(), 3);
    }

}