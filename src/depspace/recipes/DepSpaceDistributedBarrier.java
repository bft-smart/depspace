package depspace.recipes;

import java.util.Collection;

import depspace.client.DepSpaceAccessor;
import depspace.client.DepSpaceAdmin;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceProperties;
import depspace.general.DepTuple;


public class DepSpaceDistributedBarrier implements DistributedBarrierService {

	private static final String READY_TUPLE_NAME = "ready";
	
	protected final int id;
	protected final int threshold;
	protected final DepSpaceAdmin admin;
	protected final DepSpaceAccessor depSpace;
	
	private int epoch;
	
	
	public DepSpaceDistributedBarrier(int id, String tsName, int threshold, boolean createSpace) throws DepSpaceException {
		this.id = id;
		this.threshold = threshold;
		this.admin = new DepSpaceAdmin(id);
		this.depSpace = admin.createAccessor(DepSpaceProperties.createDefaultProperties(tsName), createSpace);
		this.epoch = -1;
	}


	@Override
	public void await() throws Exception {
		// Create own node
		epoch++;
		if(epoch == 0) depSpace.out(DepTuple.createTuple(id, epoch));
		else depSpace.replace(DepTuple.createTuple(id, epoch - 1), DepTuple.createTuple(id, epoch));
		
		// Check whether barrier threshold is reached
		Collection<DepTuple> tuples = depSpace.rdAll();
		int memberCount = 0;
		for(DepTuple tuple: tuples) {
			Object[] fields = tuple.getFields();
			if(epoch != (Integer) fields[1]) continue;
			if(READY_TUPLE_NAME.equals(fields[0])) return;
			memberCount++;
		}
		
		DepTuple readyTuple = DepTuple.createTuple(READY_TUPLE_NAME, epoch);
		if(memberCount < threshold) {
			// Wait for barrier to be broken
			depSpace.rd(readyTuple);
		} else {
			// Create ready node
			if(epoch == 0) depSpace.cas(readyTuple, readyTuple);
			else depSpace.replace(DepTuple.createTuple(READY_TUPLE_NAME, epoch - 1), readyTuple);
		}
	}
	
}
