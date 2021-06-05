/**
 * Name: Yuval Doron
 * ID: 207331554
 */

/**
 * A synchronized bounded-size queue for multithreaded producer-consumer applications.
 *
 * @param <T> Type of data items
 */
public class SynchronizedQueue<T> {

    private T[] buffer;
    private int producers;
    private int size;   // actual number of items in the list
    private int front;  // index of the front element in the queue
    private int end;    // index to the last element in the queue
    private int counter; // counts the overall items that was in the queue, so we won't pass the thresholds.

    /**
     * Constructor. Allocates a buffer (an array) with the given capacity and
     * resets pointers and counters.
     * @param capacity Buffer capacity
     */
    @SuppressWarnings("unchecked")
    public SynchronizedQueue(int capacity) {
        this.buffer = (T[])(new Object[capacity]);
        this.producers = 0;
        this.size = 0;
        this.front = -1;
        this.end = -1;
        this.counter = 0;
    }

    /**
     * Dequeues the first item from the queue and returns it.
     * If the queue is empty but producers are still registered to this queue,
     * this method blocks until some item is available.
     * If the queue is empty and no more items are planned to be added to this
     * queue (because no producers are registered), this method returns null.
     *
     * @return The first item, or null if there are no more items
     * @see #registerProducer()
     * @see #unregisterProducer()
     */
    public T dequeue() {
        synchronized (this) {
            // if queue is currently empty cut producers are still working, then go to sleep
            while (size == 0 && producers > 0) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            // if queue is empty and no more producers are expected to produce
            if (size == 0 && producers == 0) {
                return null;
            }

            T item = this.buffer[front];

            // if we are returning the last element left in the queue, reset indexes
            if (this.front == this.end) {
                this.front = -1;
                this.end = -1;
            }

            // more than one element was in the queue
            else {
                front = (front + 1) % buffer.length;
            }
            size--;

            // awake all producers that might be have been waiting to the queue to have space
            this.notifyAll();

            return item;
        }
    }

    /**
     * Enqueues an item to the end of this queue. If the queue is full, this
     * method blocks until some space becomes available.
     *
     * @param item Item to enqueue
     */
    public void enqueue(T item) {
        synchronized (this) {
            // if the queue is full, wait until it will have space
            while (size == this.buffer.length) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // if queue is empty before the enqueue
            if (front == -1) {
                front = 0;
            }

            end = (end + 1) % buffer.length;
            this.buffer[end] = item;
            size++;

            // awake consumer threads that might have been waiting for products
            this.notifyAll();
        }
    }

    /**
     * Returns the capacity of this queue
     * @return queue capacity
     */
    public int getCapacity() {
        return this.buffer.length;
    }

    /**
     * Returns the current size of the queue (number of elements in it)
     * @return queue size
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Registers a producer to this queue. This method actually increases the
     * internal producers counter of this queue by 1. This counter is used to
     * determine whether the queue is still active and to avoid blocking of
     * consumer threads that try to dequeue elements from an empty queue, when
     * no producer is expected to add any more items.
     * Every producer of this queue must call this method before starting to
     * enqueue items, and must also call <see>{@link #unregisterProducer()}</see> when
     * finishes to enqueue all items.
     *
     * @see #dequeue()
     * @see #unregisterProducer()
     */
    public void registerProducer() {
        synchronized (this) {
            this.producers++;
        }
    }

    /**
     * Unregisters a producer from this queue. See <see>{@link #registerProducer()}</see>.
     *
     * @see #dequeue()
     * @see #registerProducer()
     */
    public void unregisterProducer() {
        synchronized (this) {
            this.producers--;

            // we want to notify all threads who still waits for the producer by this point.
            // we now know that the producer finishes his job and we don't want to have sleeping consumers forever if producers will be 0 now.
            // notify all from enqueue method is not enough to prevent all the race situations, so that's why we are notify from here as well.
            this.notifyAll();
        }
    }

    /**
     * Printing the queue object.
     * We want to allow a queue to print itself, mostly for the mileStones queue.
     */
    public void printQueue(){
        for (int i = 0; i < size; i++){
            System.out.println(this.buffer[front]);
            front = (front + 1) % buffer.length;
        }
    }

    /**
     * get the current value of the counter, and than increase it's value by 1.
     * we want to be able to count the products so we won't pass the overall threshold.
     *
     * @return the previous value of the counter, before the increase.
     */
    public synchronized int getAndIncrement (){
        return this.counter++;
    }
}