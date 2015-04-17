package depspace.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import pvss.InvalidVSSScheme;
import pvss.PVSSEngine;
import pvss.PublicInfo;
import depspace.confidentiality.ConfidentialityScheme;
import depspace.confidentiality.ServerConfidentialityLayer;
import depspace.general.Context;
import depspace.general.DepSpace;
import depspace.general.DepSpaceConfiguration;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceOperation;
import depspace.general.DepSpaceProperties;
import depspace.general.DepTuple;
import depspace.policyenforcement.PolicyEnforcementLayer;


public class DepSpaceManager {

	private final int processID;
	private final Map<String, DepSpaceServerLayer> tupleSpaces;
	private DepSpaceEventHandler eventHandler;


	public DepSpaceManager(int processID, DepSpaceEventHandler eventHandler) {
		this.processID = processID;
		this.tupleSpaces = new HashMap<String, DepSpaceServerLayer>();
		this.eventHandler = eventHandler;
	}


	protected void setEventHandler(DepSpaceEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	public synchronized Object invokeOperation(String tsName, DepSpaceOperation operation, Object arg, Context ctx) throws DepSpaceException {
		//		System.out.println(System.currentTimeMillis() + " INVOKE[" + ctx.invokerID + "]: " + operation + " on " + tsName + " with " + arg);
		switch(operation) {
		case CREATE:
			// Create tuple space if it not yet exists
			DepSpaceServerLayer tupleSpace = tupleSpaces.get(tsName);
			if(tupleSpace == null) {
				tupleSpace = createTupleSpace((Properties) arg);
				tupleSpaces.put(tsName, tupleSpace);
			}
			return new DepTuple();
		case DELETE:
			tupleSpaces.remove(tsName);
			return new DepTuple();
		default:
			tupleSpace = tupleSpaces.get(tsName);
			if(tupleSpace != null) return invokeTupleSpaceOperation(tupleSpace, operation, arg, ctx);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private Object invokeTupleSpaceOperation(DepSpace tupleSpace, DepSpaceOperation operation, Object arg, Context ctx) throws DepSpaceException {
		Object result = null;
		switch(operation) {
		case OUT:
			tupleSpace.out((DepTuple) arg, ctx);
			break;
		case RENEW:
			result = tupleSpace.renew((DepTuple) arg, ctx);
			break;
		case RDP:
			result = tupleSpace.rdp((DepTuple) arg, ctx);
			break;
		case INP:
			result = tupleSpace.inp((DepTuple) arg, ctx);
			break;
		case RD:
			result = tupleSpace.rd((DepTuple) arg, ctx);
			break;
		case IN:
			result = tupleSpace.in((DepTuple) arg, ctx);
			break;
		case CAS:
			DepTuple[] tuples = (DepTuple[]) arg;
			result = tupleSpace.cas(tuples[0], tuples[1], ctx);
			break;
		case REPLACE:
			tuples = (DepTuple[]) arg;
			result = tupleSpace.replace(tuples[0], tuples[1], ctx);
			break;
		case OUTALL:
			tupleSpace.outAll((List<DepTuple>) arg, ctx);
			break;
		case RDALL:
			result = (arg == null) ? tupleSpace.rdAll() : tupleSpace.rdAll((DepTuple) arg, ctx);
			break;
		case INALL:
			result = tupleSpace.inAll((DepTuple) arg, ctx);
			break;
		case CLEAN:
			throw new UnsupportedOperationException("clean() not yet implemented");
			//			break;
		default:
			System.err.println("Unhandled operation type: " + operation);
		}
		return result;
	}

	private DepSpaceServerLayer createTupleSpace(Properties properties) {
		// Create implementation layer
		DepSpaceServerLayer tupleSpace;
		if(DepSpaceConfiguration.tupleSpaceImpl.equals("List"))
			tupleSpace = new DepSpaceImplLayer(eventHandler, new DepSpaceListImpl(DepSpaceConfiguration.realTimeNew));
		else if(DepSpaceConfiguration.tupleSpaceImpl.equals("Map"))
			tupleSpace = new DepSpaceImplLayer(eventHandler, new DepSpaceMapImpl(DepSpaceConfiguration.realTimeNew, DepSpaceConfiguration.depth));
		else
			tupleSpace = new DepSpaceImplLayer(eventHandler, new DepSpaceListImpl(DepSpaceConfiguration.realTimeNew));
		
		// Add replace-trigger layer
		if(DepSpaceConfiguration.replaceTrigger) tupleSpace = new ReplaceTriggerLayer(tupleSpace);

		// Add policy-enforcement layer
		String policy = DepSpaceProperties.getPolicy(properties);
		if(policy != null) {
			try {
				tupleSpace = new PolicyEnforcementLayer(tupleSpace, policy);
			} catch(DepSpaceException dse) {
				dse.printStackTrace();
			}
		}

		// Add confidentiality layer
		boolean useConfidentiality = DepSpaceProperties.getUseConfidentiality(properties);
		if(useConfidentiality) {
			try {
				PublicInfo publicInfo = DepSpaceConfiguration.createPublicInfo();
				PVSSEngine engine = PVSSEngine.getInstance(publicInfo);
				ConfidentialityScheme scheme = new ConfidentialityScheme(
						engine.getPublicInfo(),
						DepSpaceConfiguration.publicKeys,
						processID,
						DepSpaceConfiguration.secretKeys[processID]
						); 
				tupleSpace = new ServerConfidentialityLayer(tupleSpace, scheme);
			} catch(InvalidVSSScheme ivs) {
				ivs.printStackTrace();
				System.err.println("WARNING: Confidentiality layer is not available");
			}
		}

		return tupleSpace;
	}

}
