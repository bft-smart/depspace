package depspace.recipes;

import java.util.Collection;

import depspace.extension.EDSBaseExtension;
import depspace.general.Context;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceOperation;
import depspace.general.DepTuple;


public class EDSDistributedQueueExtension extends EDSBaseExtension {

	public static final String HEAD_ELEMENT = "head-element";
	
	private final DepTuple headTemplate;
	
	
	public EDSDistributedQueueExtension() {
		this.headTemplate = DepTuple.createTuple(-1, DepTuple.WILDCARD);
	}
	
	
	// ################
	// # SUBSCRIPTION #
	// ################

	@Override
	public boolean matchesOperation(String tsName, DepSpaceOperation operation, Object arg) {
		if((operation == DepSpaceOperation.RDP) || (operation == DepSpaceOperation.INP) || (operation == DepSpaceOperation.IN)) {
			Object[] templateFields = ((DepTuple) arg).getFields();
			if(templateFields.length != 1) return false;
			return HEAD_ELEMENT.equals(templateFields[0]);
		}
		return false;
	}
	
	@Override
	public boolean matchesEvent(String tsName, DepSpaceOperation operation, DepTuple tuple) {
		return false;
	}

	
	// #############
	// # EXECUTION #
	// #############

	private DepTuple getHeadElement() throws DepSpaceException {
		// Get queue elements
		Collection<DepTuple> tuples = extensionGate.rdAll();
		
		// Return if the queue is empty
		if(tuples.isEmpty()) return null;
		
		// Find the head element
		DepTuple head = null;
		long minTimestamp = Long.MAX_VALUE;
		for(DepTuple tuple: tuples) {
			long timestamp = (Long) tuple.getFields()[0];
			if(minTimestamp <= timestamp) continue;
			minTimestamp = timestamp;
			head = tuple;
		}
		return head;
	}

	@Override
	protected DepTuple rdp(DepTuple template, Context ctx) throws DepSpaceException {
		return getHeadElement();
	}
	
	@Override
	protected DepTuple inp(DepTuple template, Context ctx) throws DepSpaceException {
		// Get head element
		DepTuple head = getHeadElement();

		// Return if the queue is empty
		if(head == null) return null;
		
		// Remove an element with the smallest timestamp from the queue
		headTemplate.getFields()[0] = head.getFields()[0];
		headTemplate.modified();
		return extensionGate.inp(headTemplate);
	}
	
	@Override
	protected DepTuple in(DepTuple template, Context ctx) throws DepSpaceException {
		// Try a regular remove()
		DepTuple head = inp(template, ctx);
		if(head != null) return head;
		
		// Resort to blocking operation
		DepTuple blockingTemplate = DepTuple.createTuple(DepTuple.WILDCARD, DepTuple.WILDCARD);
		return super.in(blockingTemplate, ctx);
	}
	
}
