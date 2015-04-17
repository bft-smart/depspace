package depspace.recipes;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import depspace.client.DepSpaceAccessor;
import depspace.client.DepSpaceAdmin;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceProperties;
import depspace.general.DepTuple;


public class DepSpaceQueue implements DistributedQueueService {
	
	protected final DepSpaceAdmin admin;
	protected final DepSpaceAccessor depSpace;
	protected final DepTuple headTemplate;
	
	private final DepSpaceQueueSorter sorter;
	private final long takeRetryTimeout;
	
	
	public DepSpaceQueue(int id, String tsName, long takeRetryTimeout, boolean createSpace) throws DepSpaceException {
		this.admin = new DepSpaceAdmin(id);
		this.depSpace = admin.createAccessor(DepSpaceProperties.createDefaultProperties(tsName), createSpace);
		this.headTemplate = createHeadTemplate();
		this.sorter = new DepSpaceQueueSorter();
		this.takeRetryTimeout = takeRetryTimeout;
	}
	
	
	@Override
	public byte[] element() throws DepSpaceException {
		List<DepTuple> sortedQueue = getOrderedElements();
		if(sortedQueue.isEmpty()) return null;
		return (byte[]) sortedQueue.get(0).getFields()[1];
	}

	@Override
	public byte[] remove() throws DepSpaceException {
		DepTuple head = null;
		do {
			// Get sorted queue
			List<DepTuple> sortedQueue = getOrderedElements();
	
			// Return if the queue is empty
			if(sortedQueue.isEmpty()) return null;
			
			// Remove head element from the queue, possibly retry
			for(DepTuple element: sortedQueue) {
				headTemplate.getFields()[0] = element.getFields()[0];
				headTemplate.modified();
				head = depSpace.inp(headTemplate);
				if(head != null) break;
			}
		} while(head == null);
		return (byte[]) head.getFields()[1];
	}


	@Override
	public byte[] take() throws DepSpaceException, InterruptedException {
		byte[] head;
		while((head = remove()) == null) Thread.sleep(takeRetryTimeout);
		return head;
	}


	@Override
	public boolean offer(byte[] data) throws DepSpaceException {
		DepTuple tuple = DepTuple.createTuple(System.currentTimeMillis(), data);
		depSpace.out(tuple);
		return true;
	}

	
	// ###########
	// # HELPERS #
	// ###########

	protected DepTuple createHeadTemplate() {
		return DepTuple.createTuple(-1, DepTuple.WILDCARD);
	}
	
	private List<DepTuple> getOrderedElements() throws DepSpaceException {
		List<DepTuple> sortedQueue = new LinkedList<DepTuple>(depSpace.rdAll());
		Collections.sort(sortedQueue, sorter);
		return sortedQueue;
	}
	
	private static class DepSpaceQueueSorter implements Comparator<DepTuple> {

		@Override
		public int compare(DepTuple tupleA, DepTuple tupleB) {
			return ((Long) tupleA.getFields()[0]).compareTo((Long) tupleB.getFields()[0]);
		}
		
	}

}
