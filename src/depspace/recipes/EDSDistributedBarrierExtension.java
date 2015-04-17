package depspace.recipes;

import java.util.Collection;

import depspace.extension.EDSBaseExtension;
import depspace.general.Context;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceOperation;
import depspace.general.DepTuple;


public class EDSDistributedBarrierExtension extends EDSBaseExtension {

	public static final String BARRIER_OPERATION = "barrier";
	
	private static final DepTuple READY_TUPLE = DepTuple.createTuple("ready");
	private static final DepTuple RESET_TEMPLATE = DepTuple.createTuple(DepTuple.WILDCARD);
	
	
	// ################
	// # SUBSCRIPTION #
	// ################

	@Override
	public boolean matchesOperation(String tsName, DepSpaceOperation operation, Object arg) {
		if(operation != DepSpaceOperation.IN) return false;
		Object[] templateFields = ((DepTuple) arg).getFields();
		if(templateFields.length != 2) return false;
		return BARRIER_OPERATION.equals(templateFields[0]);
	}
	
	@Override
	public boolean matchesEvent(String tsName, DepSpaceOperation operation, DepTuple tuple) {
		return false;
	}


	// #############
	// # EXECUTION #
	// #############

	@Override
	protected DepTuple in(DepTuple template, Context ctx) throws DepSpaceException {
		// Check whether barrier threshold is reached
		Collection<DepTuple> tuples = extensionGate.rdAll();
		int threshold = (Integer) template.getFields()[1] - 1;
		if(tuples.size() < threshold) {
			// Create node
			extensionGate.out(DepTuple.createTuple(ctx.invokerID));

			// Block operation
			extensionGate.rd(READY_TUPLE);
			return null;
		}

		// Create ready node to unblock the other members
		extensionGate.out(READY_TUPLE);
		
		// Reset barrier for next round
		extensionGate.inAll(RESET_TEMPLATE);
		return READY_TUPLE;
	}
	
}
