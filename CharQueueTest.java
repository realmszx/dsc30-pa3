import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Random;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CharQueueTest {
    @Test
    void defaultConstructorCreatesUsableEmptyQueue() {
        CharQueue queue = new CharQueue();
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertThrows(NoSuchElementException.class, queue::peek);
        for (char value : "abcdef".toCharArray()) {
            queue.enqueue(value);
        }
        assertEquals(6, queue.size());
        assertEquals("abcdef", drain(queue));
    }

    @Test
    void capacityConstructorSupportsSmallAndLargeCapacities() {
        for (int capacity : new int[] {1, 2, 5, 23}) {
            CharQueue queue = new CharQueue(capacity);
            assertTrue(queue.isEmpty());
            assertEquals(0, queue.size());
            queue.enqueue('x');
            assertEquals('x', queue.peek());
            assertEquals('x', queue.dequeue());
        }
    }

    @Test
    void capacityConstructorRejectsEveryInvalidCapacityCategory() {
        assertThrows(IllegalArgumentException.class, () -> new CharQueue(0));
        assertThrows(IllegalArgumentException.class, () -> new CharQueue(-1));
        assertThrows(IllegalArgumentException.class, () -> new CharQueue(Integer.MIN_VALUE));
    }

    @Test
    void isEmptyTracksQueueTransitions() {
        CharQueue queue = new CharQueue();
        assertTrue(queue.isEmpty());
        queue.enqueue('a');
        assertFalse(queue.isEmpty());
        queue.dequeue();
        assertTrue(queue.isEmpty());
        queue.enqueue('b');
        queue.clear();
        assertTrue(queue.isEmpty());
    }

    @Test
    void sizeTracksElementsIncludingDuplicateAndNullCharacters() {
        CharQueue queue = new CharQueue(1);
        assertEquals(0, queue.size());
        queue.enqueue('\0');
        assertEquals(1, queue.size());
        queue.enqueue('\0');
        assertEquals(2, queue.size());
        queue.dequeue();
        assertEquals(1, queue.size());
        queue.clear();
        assertEquals(0, queue.size());
    }

    @Test
    void clearWorksOnEmptyFullAndWrappedQueues() {
        CharQueue queue = new CharQueue(3);
        queue.clear();
        assertTrue(queue.isEmpty());
        queue.enqueue('a');
        queue.enqueue('b');
        queue.enqueue('c');
        queue.dequeue();
        queue.enqueue('d');
        queue.clear();
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
        assertThrows(NoSuchElementException.class, queue::peek);
        assertThrows(NoSuchElementException.class, queue::dequeue);
        queue.enqueue('z');
        assertEquals('z', queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    void enqueuePreservesOrderWhenGrowingAWrappedFullQueue() {
        CharQueue queue = new CharQueue(3);
        queue.enqueue('a');
        queue.enqueue('b');
        queue.enqueue('c');
        assertEquals('a', queue.dequeue());
        assertEquals('b', queue.dequeue());
        queue.enqueue('d');
        queue.enqueue('e');
        assertEquals(3, queue.size());
        queue.enqueue('f');
        assertEquals(4, queue.size());
        assertEquals('c', queue.peek());
        assertEquals("cdef", drain(queue));
    }

    @Test
    void enqueueSupportsCapacityOneRepeatedGrowthAndEveryCharValue() {
        CharQueue queue = new CharQueue(1);
        char[] expected = {'\0', 'a', 'a', '\uffff', '\n', 'Z'};
        for (char value : expected) {
            queue.enqueue(value);
        }
        assertEquals(expected.length, queue.size());
        assertEquals('\0', queue.peek());
        for (char value : expected) {
            assertEquals(value, queue.dequeue());
        }
        assertTrue(queue.isEmpty());
    }

    @Test
    void peekReturnsFrontWithoutRemovingIt() {
        CharQueue queue = new CharQueue(2);
        queue.enqueue('a');
        queue.enqueue('b');
        assertEquals('a', queue.peek());
        assertEquals('a', queue.peek());
        assertEquals(2, queue.size());
        queue.dequeue();
        queue.enqueue('c');
        assertEquals('b', queue.peek());
        assertEquals(2, queue.size());
    }

    @Test
    void peekThrowsOnNewDrainedAndClearedQueues() {
        CharQueue queue = new CharQueue();
        assertThrows(NoSuchElementException.class, queue::peek);
        queue.enqueue('a');
        queue.dequeue();
        assertThrows(NoSuchElementException.class, queue::peek);
        queue.enqueue('b');
        queue.clear();
        assertThrows(NoSuchElementException.class, queue::peek);
    }

    @Test
    void dequeueReturnsOldestAndAllowsReuseAfterDraining() {
        CharQueue queue = new CharQueue(2);
        queue.enqueue('a');
        queue.enqueue('b');
        assertEquals('a', queue.dequeue());
        assertEquals(1, queue.size());
        queue.enqueue('c');
        assertEquals('b', queue.dequeue());
        assertEquals('c', queue.dequeue());
        assertTrue(queue.isEmpty());
        queue.enqueue('d');
        assertEquals('d', queue.dequeue());
    }

    @Test
    void dequeueThrowsOnNewDrainedAndClearedQueues() {
        CharQueue queue = new CharQueue();
        assertThrows(NoSuchElementException.class, queue::dequeue);
        queue.enqueue('a');
        queue.dequeue();
        assertThrows(NoSuchElementException.class, queue::dequeue);
        queue.enqueue('b');
        queue.clear();
        assertThrows(NoSuchElementException.class, queue::dequeue);
    }

    @Test
    void mixedOperationsMatchIndependentFifoReference() {
        Random random = new Random(30);
        CharQueue actual = new CharQueue(1);
        ArrayDeque<Character> expected = new ArrayDeque<>();
        for (int step = 0; step < 20000; step++) {
            int operation = random.nextInt(100);
            if (operation < 55) {
                char value = (char) random.nextInt(65536);
                expected.addLast(value);
                actual.enqueue(value);
            } else if (operation < 80) {
                if (expected.isEmpty()) {
                    assertThrows(NoSuchElementException.class, actual::dequeue);
                } else {
                    assertEquals(expected.removeFirst().charValue(), actual.dequeue());
                }
            } else if (operation < 98) {
                if (expected.isEmpty()) {
                    assertThrows(NoSuchElementException.class, actual::peek);
                } else {
                    assertEquals(expected.peekFirst().charValue(), actual.peek());
                }
            } else {
                expected.clear();
                actual.clear();
            }
            assertEquals(expected.size(), actual.size());
            assertEquals(expected.isEmpty(), actual.isEmpty());
        }
        while (!expected.isEmpty()) {
            assertEquals(expected.removeFirst().charValue(), actual.dequeue());
        }
        assertTrue(actual.isEmpty());
    }

    private String drain(CharQueue queue) {
        StringBuilder result = new StringBuilder();
        while (!queue.isEmpty()) {
            result.append(queue.dequeue());
        }
        return result.toString();
    }
}
