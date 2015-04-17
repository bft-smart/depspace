package depspace.recipes;

import depspace.extension.EDSBaseExtension;
import depspace.general.Context;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceOperation;
import depspace.general.DepTuple;


public class EDSSharedValueExtension extends EDSBaseExtension {

	public static final String INCREMENT_OPERATION = "increment";
	
	private static final DepTuple TEMPLATE = DepTuple.createTuple(DepTuple.WILDCARD);
	
	
	// ################
	// # SUBSCRIPTION #
	// ################

	@Override
	public boolean matchesOperation(String tsName, DepSpaceOperation operation, Object arg) {
		if(operation != DepSpaceOperation.INP) return false;
		Object[] templateFields = ((DepTuple) arg).getFields();
		if(templateFields.length != 1) return false;
		return INCREMENT_OPERATION.equals(templateFields[0]);
	}
	
	@Override
	public boolean matchesEvent(String tsName, DepSpaceOperation operation, DepTuple tuple) {
		return false;
	}


	// #############
	// # EXECUTION #
	// #############

	@Override
	protected DepTuple inp(DepTuple template, Context ctx) throws DepSpaceException {
		// Get and increment
		DepTuple tuple = extensionGate.inp(TEMPLATE);
		if(tuple == null) extensionGate.error("There is no shared-counter tuple to increment");
		int counterValue = (Integer) tuple.getFields()[0];
		extensionGate.out(DepTuple.createTuple(counterValue + 1));
		return tuple;
	}
	
}
