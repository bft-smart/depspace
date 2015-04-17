package depspace.extension;

import java.util.Collection;
import java.util.List;

import depspace.general.Context;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceOperation;
import depspace.general.DepTuple;


public abstract class EDSBaseExtension implements EDSExtension {

	protected EDSExtensionGate extensionGate;
	
	
	@Override
	public void setExtensionGate(EDSExtensionGate extensionGate) {
		this.extensionGate = extensionGate;
	}
	
	@Override
	public void init() throws DepSpaceException {
		// Override in sub classes if necessary
	}

	@SuppressWarnings("unchecked")
	@Override
	public final Object handleOperation(String tsName, DepSpaceOperation operation, Object arg, Context ctx) throws DepSpaceException {
		Object result = null;
		switch(operation) {
		case OUT:
			out((DepTuple) arg, ctx);
			break;
		case OUTALL:
			outAll((List<DepTuple>) arg, ctx);
			break;
		case RENEW:
			result = renew((DepTuple) arg, ctx);
			break;
		case RDP:
			result = rdp((DepTuple) arg, ctx);
			break;
		case INP:
			result = inp((DepTuple) arg, ctx);
			break;
		case RD:
			result = rd((DepTuple) arg, ctx);
			break;
		case IN:
			result = in((DepTuple) arg, ctx);
			break;
		case CAS:
			DepTuple[] tuples = (DepTuple[]) arg;
			result = cas(tuples[0], tuples[1], ctx);
			break;
		case REPLACE:
			tuples = (DepTuple[]) arg;
			result = replace(tuples[0], tuples[1], ctx);
			break;
		case RDALL:
			result = (arg == null) ? rdAll() : rdAll((DepTuple) arg, ctx);
			break;
		case INALL:
			result = inAll((DepTuple) arg, ctx);
			break;
		default:
			System.err.println(this + " EDSBaseExtension -- Unhandled operation type: " + operation);
		}
		return result;
	}
	
	@Override
	public boolean handleEvent(String tsName, DepSpaceOperation operation, DepTuple tuple, Context ctx) throws DepSpaceException {
		// Override in sub classes if necessary
		return false;
	}

	
	protected void out(DepTuple tuple, Context ctx) throws DepSpaceException {
		extensionGate.out(tuple);
	}

	protected void outAll(List<DepTuple> tuplesBag, Context ctx) throws DepSpaceException {
		extensionGate.outAll(tuplesBag);
	}

	protected DepTuple renew(DepTuple template, Context ctx) throws DepSpaceException {
		return extensionGate.renew(template);
	}

	protected DepTuple rdp(DepTuple template, Context ctx) throws DepSpaceException {
		return extensionGate.rdp(template);
	}

	protected DepTuple inp(DepTuple template, Context ctx) throws DepSpaceException {
		return extensionGate.inp(template);
	}

	protected DepTuple rd(DepTuple template, Context ctx) throws DepSpaceException {
		return extensionGate.rd(template);
	}

	protected DepTuple in(DepTuple template, Context ctx) throws DepSpaceException {
		return extensionGate.in(template);
	}

	protected DepTuple cas(DepTuple template, DepTuple tuple, Context ctx) throws DepSpaceException {
		return extensionGate.cas(template, tuple);
	}

	protected DepTuple replace(DepTuple template, DepTuple tuple, Context ctx) throws DepSpaceException {
		return extensionGate.replace(template, tuple);
	}

	protected Collection<DepTuple> rdAll(DepTuple template, Context ctx) throws DepSpaceException {
		return extensionGate.rdAll(template);
	}

	protected Collection<DepTuple> rdAll() throws DepSpaceException {
		return extensionGate.rdAll();
	}

	protected Collection<DepTuple> inAll(DepTuple template, Context ctx) throws DepSpaceException {
		return extensionGate.inAll(template);
	}

}
