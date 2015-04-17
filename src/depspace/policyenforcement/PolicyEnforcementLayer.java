package depspace.policyenforcement;

import groovy.lang.GroovyClassLoader;

import java.util.Collection;
import java.util.List;

import depspace.general.Context;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceOperation;
import depspace.general.DepTuple;
import depspace.server.DepSpaceServerLayer;


public class PolicyEnforcementLayer extends DepSpaceServerLayer {

	// Policy enforcer
	private PolicyEnforcer policyEnforcer;

	
	/**
	 * Creates a new instance of PolicyEnforcementLayer.
	 * @param upperLayer The upper layer (normally Tuple Space layer)
	 * @param policy The policy to be enforced
	 * @throws DepSpaceException If policy enforcer can not be instantiated
	 */
	public PolicyEnforcementLayer(DepSpaceServerLayer upperLayer, String policy) throws DepSpaceException {
		super(upperLayer);
		try {
			@SuppressWarnings("resource")
			GroovyClassLoader classLoader = new GroovyClassLoader(getClass().getClassLoader());
			Class<?> policyClass = classLoader.parseClass(policy);
			this.policyEnforcer = (PolicyEnforcer) policyClass.newInstance();
			this.policyEnforcer.setLayer(this);
		} catch(Exception e) {
			throw new DepSpaceException("Cannot instantiate policy enforcer: " + e);
		}
	}

    
    private void checkPermission(DepSpaceOperation operation, Context ctx, DepTuple... args) throws DepSpaceOperationNotAllowed {
    	boolean allowed = true;
    	switch(operation) {
    	case OUT:
    	case OUTALL:
    		allowed = policyEnforcer.canExecuteOut(ctx.invokerID, args[0], ctx);
    		break;
    	case RENEW:
    		allowed = policyEnforcer.canExecuteRenew(ctx.invokerID, args[0], ctx);
    		break;
    	case RDP:
    	case RD:
    		allowed = policyEnforcer.canExecuteRdp(ctx.invokerID, args[0], ctx);
    		break;
    	case INP:
    	case IN:
    		allowed = policyEnforcer.canExecuteInp(ctx.invokerID, args[0], ctx);
    		break;
    	case CAS:
    		allowed = policyEnforcer.canExecuteCas(ctx.invokerID, args[0], args[1], ctx);
    		break;
    	case REPLACE:
    		allowed = policyEnforcer.canExecuteReplace(ctx.invokerID, args[0], args[1], ctx);
    		break;
    	case RDALL:
    		allowed = policyEnforcer.canExecuteRdAll(ctx.invokerID, args[0], ctx);
    		break;
    	case INALL:
    		allowed = policyEnforcer.canExecuteInAll(ctx.invokerID, args[0], ctx);
    		break;
    	case CLEAN:
    		allowed = true;
    		break;
		default:
			System.err.println("Policy enforcer: Unhandled operation type " + operation);
    		allowed = false;
    	}
    	if(!allowed) throw new DepSpaceOperationNotAllowed(operation, ctx.invokerID, args);
    }
    

	@Override
	public void out(DepTuple tuple, Context ctx) throws DepSpaceException {
		checkPermission(DepSpaceOperation.OUT, ctx, tuple);
		upperLayer.out(tuple, ctx);
	}

	@Override
	public DepTuple renew(DepTuple template, Context ctx) throws DepSpaceException {
		checkPermission(DepSpaceOperation.RENEW, ctx, template);
		return upperLayer.renew(template, ctx);
	}

	@Override
	public DepTuple rdp(DepTuple template, Context ctx) throws DepSpaceException {
		checkPermission(DepSpaceOperation.RDP, ctx, template);
		return upperLayer.renew(template, ctx);
	}

	@Override
	public DepTuple inp(DepTuple template, Context ctx) throws DepSpaceException {
		checkPermission(DepSpaceOperation.INP, ctx, template);
		return upperLayer.renew(template, ctx);
	}

	@Override
	public DepTuple rd(DepTuple template, Context ctx) throws DepSpaceException {
		checkPermission(DepSpaceOperation.RD, ctx, template);
		return upperLayer.renew(template, ctx);
	}

	@Override
	public DepTuple in(DepTuple template, Context ctx) throws DepSpaceException {
		checkPermission(DepSpaceOperation.IN, ctx, template);
		return upperLayer.renew(template, ctx);
	}

	@Override
	public DepTuple cas(DepTuple template, DepTuple tuple, Context ctx) throws DepSpaceException {
		checkPermission(DepSpaceOperation.CAS, ctx, template, tuple);
		return upperLayer.cas(template, tuple, ctx);
	}

	@Override
	public DepTuple replace(DepTuple template, DepTuple tuple, Context ctx) throws DepSpaceException {
		checkPermission(DepSpaceOperation.REPLACE, ctx, template, tuple);
		return upperLayer.cas(template, tuple, ctx);
	}

	@Override
	public void outAll(List<DepTuple> tuplesBag, Context ctx) throws DepSpaceException {
		for(DepTuple tuple: tuplesBag) checkPermission(DepSpaceOperation.OUTALL, ctx, tuple);
		upperLayer.outAll(tuplesBag, ctx);
	}

	@Override
	public Collection<DepTuple> rdAll(DepTuple template, Context ctx) throws DepSpaceException {
		checkPermission(DepSpaceOperation.RDALL, ctx, template);
		return upperLayer.rdAll(template, ctx);
	}

	@Override
	public Collection<DepTuple> rdAll() throws DepSpaceException {
		throw new UnsupportedOperationException("Not implemented at this layer");
	}

	@Override
	public Collection<DepTuple> inAll(DepTuple template, Context ctx) throws DepSpaceException {
		checkPermission(DepSpaceOperation.INALL, ctx, template);
		return upperLayer.inAll(template, ctx);
	}

	@Override
	public Object signedRD(DepTuple template, Context ctx) throws DepSpaceException {
		throw new UnsupportedOperationException("Not implemented at this layer");
	}

	@Override
	public void clean(DepTuple proof, Context ctx) throws DepSpaceException {
		checkPermission(DepSpaceOperation.CLEAN, ctx, proof);
		upperLayer.clean(proof, ctx);
	}

}
