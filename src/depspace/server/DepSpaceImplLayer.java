package depspace.server;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import depspace.general.Context;
import depspace.general.DepSpace;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceOperation;
import depspace.general.DepTuple;


public class DepSpaceImplLayer extends DepSpaceServerLayer {

	// The tuple list
	private DepSpace impl;
	
    // Flag to show whether it is possible to renew an expired tuple

	
	public DepSpaceImplLayer(DepSpaceEventHandler eventHandler, DepSpace impl) {
		super(null);
		this.impl = impl;
		this.eventHandler = eventHandler;
		this.blockedOperations = new LinkedList<DepSpaceBlockedOperation>();
	}
	
	
	@Override
	public DepSpaceServerLayer getUpperLayer() {
		return null;
	}

	
	@Override
	public void out(DepTuple tuple, Context ctx) throws DepSpaceException {
		impl.out(tuple, ctx);
		checkBlockedOperations();
	}

	@Override
	public DepTuple renew(DepTuple template, Context ctx) throws DepSpaceException {
		return impl.renew(template, ctx);
	}

	@Override
	public DepTuple rdp(DepTuple template, Context ctx) throws DepSpaceException {
		return impl.rdp(template, ctx);
	}

	@Override
	public DepTuple inp(DepTuple template, Context ctx) throws DepSpaceException {
		return impl.inp(template, ctx);
	}

	@Override
	public DepTuple cas(DepTuple template, DepTuple tuple, Context ctx) throws DepSpaceException {
		DepTuple result =  impl.cas(template, tuple, ctx);
		checkBlockedOperations();
		return result;
	}

	@Override
	public DepTuple replace(DepTuple template, DepTuple tuple, Context ctx) throws DepSpaceException {
		DepTuple result = impl.replace(template, tuple, ctx);
		checkBlockedOperations();
		return result;
	}

	@Override
	public void outAll(List<DepTuple> tuplesBag, Context ctx) throws DepSpaceException {
		impl.outAll(tuplesBag, ctx);
		checkBlockedOperations();
	}

	@Override
	public Collection<DepTuple> rdAll() throws DepSpaceException {
		return impl.rdAll();
	}

	@Override
	public Collection<DepTuple> rdAll(DepTuple template, Context ctx) throws DepSpaceException {
		return impl.rdAll(template, ctx);
	}

	@Override
	public Collection<DepTuple> inAll(DepTuple template, Context ctx) throws DepSpaceException {
		return impl.inAll(template, ctx);
	}
	
	@Override
	public Object signedRD(DepTuple template, Context ctx) throws DepSpaceException {
		throw new UnsupportedOperationException("Not implemented at this layer");
	}
	
	@Override
	public void clean(DepTuple proof, Context ctx) throws DepSpaceException {
		throw new UnsupportedOperationException("Not implemented at this layer");
	}

	
	/***********************
	 * BLOCKING OPERATIONS *
	 ***********************/

	// Event handler for blocked operations
    private final DepSpaceEventHandler eventHandler;
    
    private final Collection<DepSpaceBlockedOperation> blockedOperations;
    
    
	@Override
	public DepTuple rd(DepTuple template, Context ctx) throws DepSpaceException {
		// Try to perform non-blocking operation
		DepTuple result = rdp(template, ctx);
		if(result != null) return result;
		
		// Switch to blocking operation
		DepSpaceBlockedOperation blockedOperation = new DepSpaceBlockedOperation(DepSpaceOperation.RD, template, ctx);
		blockedOperations.add(blockedOperation);
		return null;
	}

	@Override
	public DepTuple in(DepTuple template, Context ctx) throws DepSpaceException {
		// Try to perform non-blocking operation
		DepTuple result = inp(template, ctx);
		if(result != null) return result;
		
		// Switch to blocking operation
		DepSpaceBlockedOperation blockedOperation = new DepSpaceBlockedOperation(DepSpaceOperation.IN, template, ctx);
		blockedOperations.add(blockedOperation);
		return null;
	}
	

	private void checkBlockedOperations() throws DepSpaceException {
		for(Iterator<DepSpaceBlockedOperation> iterator = blockedOperations.iterator(); iterator.hasNext(); ) {
			// Try to get a result
			DepSpaceBlockedOperation blockedOperation = iterator.next();
			DepTuple result;
			switch(blockedOperation.operation) {
			case RD:
				result = rdp(blockedOperation.template, blockedOperation.ctx);
				break;
			case IN:
				result = inp(blockedOperation.template, blockedOperation.ctx);
				break;
			default:
				System.err.println("Ignoring blocked operation of type " + blockedOperation.operation);
				iterator.remove();
				continue;
			}
			if(result == null) continue;
			
			// Forward reply
			eventHandler.handleEvent(blockedOperation.operation, result, blockedOperation.ctx);
			iterator.remove();
		}
	}
	
	
	private static class DepSpaceBlockedOperation {

		public final DepSpaceOperation operation;
		public final DepTuple template;
		public final Context ctx;
		
		
		public DepSpaceBlockedOperation(DepSpaceOperation operation, DepTuple template, Context ctx) {
			this.operation = operation;
			this.template = template;
			this.ctx = ctx;
		}
		
	}
	
}
