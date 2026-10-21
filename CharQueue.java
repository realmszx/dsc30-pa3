/*
 * Name: YOUR NAME
 * PID: YOUR PID
 */

import java.util.NoSuchElementException;

/**
 * Stores characters in a resizable circular queue in first-in, first-out order.
 *
 * @author YOUR NAME
 * @since 2026-10-20
 */
public class CharQueue {
    private static final int DEFAULT_CAPACITY = 5;
    private static final int GROWTH_FACTOR = 2;

    private char[] circularArray;
    private int length;
    private int front;
    private int rear;

    /**
     * Creates an empty queue with the default capacity of five characters.
     */
    public CharQueue() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates an empty queue with the specified initial capacity.
     *
     * @param capacity initial number of characters the backing array can hold
     * @throws IllegalArgumentException if capacity is less than one
     */
    public CharQueue(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity must be at least one.");
        }
        circularArray = new char[capacity];
        length = 0;
        front = 0;
        rear = 0;
    }

    /**
     * Returns whether this queue contains no elements.
     *
     * @return true if the queue is empty, false otherwise
     */
    public boolean isEmpty() {
        return length == 0;
    }

    /**
     * Returns the number of characters currently in the queue.
     *
     * @return the number of queued characters
     */
    public int size() {
        return length;
    }

    /**
     * Removes all logical elements in constant time, retaining the capacity.
     */
    public void clear() {
        length = 0;
        front = 0;
        rear = 0;
    }

    /**
     * Adds a character at the rear, doubling the backing array when full.
     *
     * @param elem the character to add
     */
    public void enqueue(char elem) {
        if (length == circularArray.length) {
            expandCapacity();
        }
        circularArray[rear] = elem;
        rear = (rear + 1) % circularArray.length;
        length++;
    }

    /**
     * Returns the front character without removing it.
     *
     * @return the oldest character in the queue
     * @throws NoSuchElementException if the queue is empty
     */
    public char peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Cannot peek at an empty queue.");
        }
        return circularArray[front];
    }

    /**
     * Removes and returns the front character.
     *
     * @return the oldest character in the queue
     * @throws NoSuchElementException if the queue is empty
     */
    public char dequeue() {
        char value = peek();
        front = (front + 1) % circularArray.length;
        length--;
        return value;
    }

    /**
     * Doubles capacity and copies the logical queue into consecutive positions.
     */
    private void expandCapacity() {
        char[] expanded = new char[circularArray.length * GROWTH_FACTOR];
        for (int index = 0; index < length; index++) {
            // The oldest element may be in the middle of the old backing array.
            expanded[index] = circularArray[(front + index) % circularArray.length];
        }
        circularArray = expanded;
        front = 0;
        rear = length;
    }
}
