package depspace.client;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import bftsmart.tom.core.messages.TOMMessageType;
import depspace.general.Context;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceOperation;
import depspace.general.DepSpaceProperties;
import depspace.general.DepSpaceReply;
import depspace.general.DepSpaceRequest;
import depspace.general.DepTuple;


public class ClientReplicationLayer implements DepSpaceClientLayer {

	// Counter for request sequence numbers
	private final AtomicInteger sequenceNumber;

	// BFT-SMaRt
	private final DepSpaceServiceProxy service;
	
	
	public ClientReplicationLayer(DepSpaceServiceProxy service) {
		this.sequenceNumber = new AtomicInteger();
		this.service = service;
	}


	private Object executeOperation(DepSpaceOperation operation, Object arg, Context ctx) throws DepSpaceException {
		return executeOperation(operation, arg, ctx, operation.getRequestType());
	}
	
	private synchronized Object executeOperation(DepSpaceOperation operation, Object arg, Context ctx, TOMMessageType type) throws DepSpaceException {
		// Prepare request
		DepSpaceRequest request = new DepSpaceRequest(sequenceNumber.getAndIncrement(), operation, arg, ctx);
		byte[] requestBytes = request.serialize();

		// Invoke operation
		byte[] replyBuffer = service.invoke(requestBytes, type);
		
		// Deserialize result
		switch(request.operation) {
		case OUT:
		case OUTALL:
		case CREATE:
		case DELETE:
		case CLEAN:
			return null;
		default:
			DepSpaceReply reply;
			try {
				reply = new DepSpaceReply(replyBuffer);
			} catch(Exception e) {
				e.printStackTrace();
				throw new DepSpaceException("Error while deserializing reply");
			}
			if(reply.operation == DepSpaceOperation.EXCEPTION) throw (DepSpaceException) reply.arg;
			return reply.arg;
		}
	}
	
	
	/*************************************
	 * DEPSPACE INTERFACE IMPLEMENTATION *
	 *************************************/

	@Override
	public void out(DepTuple tuple, Context ctx) throws DepSpaceException {
		executeOperation(DepSpaceOperation.OUT, tuple, ctx);
	}

	@Override
	public void outAll(List<DepTuple> tuplesBag, Context ctx) throws DepSpaceException {
		executeOperation(DepSpaceOperation.OUTALL, tuplesBag, ctx);		
	}

	@Override
	public DepTuple renew(DepTuple template, Context ctx) throws DepSpaceException {
		return (DepTuple) executeOperation(DepSpaceOperation.RENEW, template, ctx);
	}

	@Override
	public DepTuple rdp(DepTuple template, Context ctx) throws DepSpaceException {
		// Try to obtain a result
		DepTuple result = (DepTuple) executeOperation(DepSpaceOperation.RDP, template, ctx);
		if(result == null) return result;
		
		// Validation check <--- Why is such a check performed in rdp(), but not in rd() or rdAll()? 
		if(!result.isExpired(System.currentTimeMillis())) return result;
		// System.out.println("RDP :: TUPLE VALIDATION IS EXPIRED, WE NEED TO SEND A NON READONLY REQUEST");
		return (DepTuple) executeOperation(DepSpaceOperation.RDP, template, ctx, TOMMessageType.ORDERED_REQUEST);
	}

	@Override
	public DepTuple inp(DepTuple template, Context ctx) throws DepSpaceException {
		return (DepTuple) executeOperation(DepSpaceOperation.INP, template, ctx);
	}

	@Override
	public DepTuple rd(DepTuple template, Context ctx) throws DepSpaceException {
		return (DepTuple) executeOperation(DepSpaceOperation.RD, template, ctx);
	}

	@Override
	public DepTuple in(DepTuple template, Context ctx) throws DepSpaceException {
		return (DepTuple) executeOperation(DepSpaceOperation.IN, template, ctx);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<DepTuple> rdAll(DepTuple template, Context ctx) throws DepSpaceException {
		return (Collection<DepTuple>) executeOperation(DepSpaceOperation.RDALL, template, ctx);
	}

	@Override
	public Collection<DepTuple> rdAll() {
		throw new UnsupportedOperationException("rdAll() is not implemented by the client replication layer");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<DepTuple> inAll(DepTuple template, Context ctx) throws DepSpaceException {
		return (Collection<DepTuple>) executeOperation(DepSpaceOperation.INALL, template, ctx);
	}
	
	@Override
	public DepTuple cas(DepTuple template, DepTuple tuple, Context ctx) throws DepSpaceException {
		return (DepTuple) executeOperation(DepSpaceOperation.CAS, new DepTuple[] { template, tuple }, ctx);
	}

	@Override
	public DepTuple replace(DepTuple template, DepTuple tuple, Context ctx) throws DepSpaceException {
		return (DepTuple) executeOperation(DepSpaceOperation.REPLACE, new DepTuple[] { template, tuple }, ctx);
	}

	
	/********************************************
	 * DEPSPACE CLIENT INTERFACE IMPLEMENTATION *
	 ********************************************/

	@Override
	public void createSpace(Properties properties) throws DepSpaceException {
		Context createContext = Context.createDefaultContext(DepSpaceProperties.getTSName(properties), DepSpaceOperation.CREATE, false, (DepTuple[]) null);
		executeOperation(DepSpaceOperation.CREATE, properties, createContext);
	}

	@Override
	public void deleteSpace(String name) throws DepSpaceException {
		Context deleteContext = Context.createDefaultContext(name, DepSpaceOperation.DELETE, false, (DepTuple[]) null);
		executeOperation(DepSpaceOperation.DELETE, name, deleteContext);
	}

	@Override
	public DepTuple signedRD(DepTuple template, Context ctx) throws DepSpaceException {
		return (DepTuple) executeOperation(DepSpaceOperation.SIGNED_RD, template, ctx);
	}

	@Override
	public void clean(DepTuple proof, Context ctx) throws DepSpaceException {
		executeOperation(DepSpaceOperation.CLEAN, proof, ctx);
	}

}
