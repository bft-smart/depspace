package depspace.recipes;


public interface LeaderElectionService {

	/**
	 * Start the election process.
	 */
	public void start();

	/**
	 * Stops all election services.
	 */
	public void stop();
	
	/**
	 * Adds {@code listener} to the list of listeners who will receive events.
	 * 
	 * @param listener
	 */
	public void addListener(LeaderElectionListener listener);

	/**
	 * Remove {@code listener} from the list of listeners who receive events.
	 * 
	 * @param listener
	 */
	public void removeListener(LeaderElectionListener listener);

	/**
	 * The type of event.
	 */
	public static enum EventType {
		START, OFFER_START, OFFER_COMPLETE, DETERMINE_START, DETERMINE_COMPLETE, ELECTED_START, ELECTED_COMPLETE, READY_START, READY_COMPLETE, FAILED, STOP_START, STOP_COMPLETE
	}

	/**
	 * An interface to be implemented by clients that want to receive election
	 * events.
	 */
	public interface LeaderElectionListener {

		/**
		 * Called during each state transition. Current, low level events are provided
		 * at the beginning and end of each state. For instance, START may be followed
		 * by OFFER_START, OFFER_COMPLETE, DETERMINE_START, DETERMINE_COMPLETE, and so
		 * on.
		 * 
		 * @param eventType
		 */
		public void onElectionEvent(EventType eventType);

	}

}
