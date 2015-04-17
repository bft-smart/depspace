package depspace.recipes;

import java.util.List;

import depspace.extension.EDSBaseExtension;
import depspace.general.Context;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceOperation;
import depspace.general.DepTuple;


public class EDSLeaderElectionExtension extends EDSBaseExtension {

	public static final String LEADER_TOKEN_NAME = "I_AM_LEADER";
	public static final DepTuple LEADER_TOKEN_TEMPLATE = DepTuple.createTuple(LEADER_TOKEN_NAME, DepTuple.WILDCARD);
	public static final int INITIAL_LEADER_LEASE = 2000;
	
	private static final DepTuple MODIFICATION_TRIGGER = DepTuple.createTuple("MODIFICATION_TRIGGER");
	
	
	// ################
	// # SUBSCRIPTION #
	// ################

	@Override
	public boolean matchesOperation(String tsName, DepSpaceOperation operation, Object arg) {
		if((operation == DepSpaceOperation.IN) || (operation == DepSpaceOperation.INP)) {
			Object[] templateFields = ((DepTuple) arg).getFields();
			if(templateFields.length != 2) return false;
			if(!(templateFields[0] instanceof String)) return false;
			return LEADER_TOKEN_NAME.equals(templateFields[0]);
		} else if(operation == DepSpaceOperation.OUTALL) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean matchesEvent(String tsName, DepSpaceOperation operation, DepTuple tuple) {
		if(operation != DepSpaceOperation.IN) return false;
		return (MODIFICATION_TRIGGER.equals(tuple));
	}

	
	// #############
	// # EXECUTION #
	// #############
	
	private void changeLeader() throws DepSpaceException {
		// Install modification trigger --> Unblocks a pending operation, if there is any.
		extensionGate.out(MODIFICATION_TRIGGER);
		
		// There may not be any pending operations --> Ensure modification-trigger cleanup.
		extensionGate.inp(MODIFICATION_TRIGGER);
	}
	
	@Override
	protected DepTuple in(DepTuple tuple, Context ctx) throws DepSpaceException {
		// Try to become leader
		DepTuple result = extensionGate.cas(LEADER_TOKEN_TEMPLATE, tuple);
		if(result == null) return DepTuple.createTuple(); // Return an empty tuple as 'null' indicates that the operation blocks
		
		// Resort to blocking operation
		return super.in(MODIFICATION_TRIGGER, ctx);
	}
	
	@Override
	protected DepTuple inp(DepTuple template, Context ctx) throws DepSpaceException {
		// Remove the token of the current leader
		DepTuple token = super.inp(template, ctx);
		changeLeader();
		return token;
	}

	@Override
	public boolean handleEvent(String tsName, DepSpaceOperation operation, DepTuple tuple, Context ctx) throws DepSpaceException {
		// Register the leader's token
		DepTuple token = DepTuple.createTimedTuple(ctx.time + INITIAL_LEADER_LEASE, EDSLeaderElectionExtension.LEADER_TOKEN_NAME, ctx.invokerID);
		extensionGate.out(token);
		return false;
	}
	
	@Override
	protected void outAll(List<DepTuple> tuplesBag, Context ctx) throws DepSpaceException {
		// Watch dog event: Invoke leader change if the token of the current leader timed out
		DepTuple token = extensionGate.rdp(LEADER_TOKEN_TEMPLATE);
		if(token == null) changeLeader();
	}
	
}
