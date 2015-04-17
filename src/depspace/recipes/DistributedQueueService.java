package depspace.recipes;


public interface DistributedQueueService {

    /**
     * Return the head of the queue without modifying the queue.
     * @return the data at the head of the queue.
     * @throws Exception
     */
	public byte[] element() throws Exception;

    /**
     * Attempts to remove the head of the queue and return it.
     * @return The former head of the queue
     * @throws Exception
     */
	public byte[] remove() throws Exception;
	
    /**
     * Removes the head of the queue and returns it, blocks until it succeeds.
     * @return The former head of the queue
     * @throws Exception
     */
	public byte[] take() throws Exception;

    /**
     * Inserts data into queue.
     * @param data
     * @return true if data was successfully added
     */
	public boolean offer(byte[] data) throws Exception;

}
