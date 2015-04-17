package depspace.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import depspace.general.Context;
import depspace.general.DepSpace;
import depspace.general.DepTuple;

public class DepSpaceListImpl implements DepSpace {
	// The tuple list
	private final List<DepTuple> tuplesBag;

	// Flag to show whether it is possible to renew an expired tuple
	// true - it is possible
	// false - it is not possible
	private final boolean realTimeRenew;


	public DepSpaceListImpl(boolean realTimeRenew) {
		this.tuplesBag = new LinkedList<DepTuple>();
		this.realTimeRenew = realTimeRenew;
	}


	@Override
	public void out(DepTuple tuple, Context ctx) {
		tuplesBag.add(tuple);
	}

	@Override
	public DepTuple renew(DepTuple template, Context ctx) {
		for(Iterator<DepTuple> iterator = tuplesBag.iterator(); iterator.hasNext(); ) {
			DepTuple tuple = iterator.next();
			if(!tuple.canIn(ctx.invokerID)) continue;
			if(!tuple.matches(template)) continue;
			if(tuple.isExpired(ctx.time) && !realTimeRenew) return null;
			tuple.setExpirationTime(template.getExpirationTime());
			return tuple;
		}
		return null;
	}

	@Override
	public DepTuple rdp(DepTuple template, Context ctx) {
		for(Iterator<DepTuple> iterator = tuplesBag.iterator(); iterator.hasNext(); ) {
			DepTuple tuple = iterator.next();
			if(!tuple.canRd(ctx.invokerID)) continue;
			if(!tuple.matches(template)) continue;
			if(!tuple.isExpired(ctx.time)) return tuple;
			iterator.remove();
		}
		return null;
	}

	@Override
	public DepTuple inp(DepTuple template, Context ctx) {
		for(Iterator<DepTuple> iterator = tuplesBag.iterator(); iterator.hasNext(); ) {
			DepTuple tuple = iterator.next();
			if(!tuple.canIn(ctx.invokerID)) continue;
			if(!tuple.matches(template)) continue;
			iterator.remove();
			if(!tuple.isExpired(ctx.time)) return tuple;
		}
		return null;
	}

	@Override
	public DepTuple cas(DepTuple template, DepTuple tuple, Context ctx) {
		DepTuple result = rdp(template, ctx);
		if(result == null) out(tuple, ctx);
		return result;
	}

	@Override
	public DepTuple replace(DepTuple template, DepTuple tuple, Context ctx) {
		DepTuple result = inp(template, ctx);
		if(result == null) return result;
		out(tuple, ctx);
		return result;
	}

	@Override
	public void outAll(List<DepTuple> tuplesBag, Context ctx) {
		this.tuplesBag.addAll(tuplesBag);
	}

	@Override
	public Collection<DepTuple> rdAll() {
		return new ArrayList<DepTuple>(tuplesBag);
	}

	@Override
	public Collection<DepTuple> rdAll(DepTuple template, Context ctx) {
		ArrayList<DepTuple> result = new ArrayList<DepTuple>();
		int tuplesToRead = (template.getN_Matches() > 0) ? template.getN_Matches() : tuplesBag.size();
		for(Iterator<DepTuple> iterator = tuplesBag.iterator(); iterator.hasNext() && (tuplesToRead > 0); ) {
			DepTuple tuple = iterator.next();
			if(!tuple.canRd(ctx.invokerID)) continue;
			if(!tuple.matches(template)) continue;
			if(tuple.isExpired(ctx.time)) {
				iterator.remove();
				continue;
			}
			result.add(tuple);
			tuplesToRead--;
		}
		return result;
	}

	@Override
	public Collection<DepTuple> inAll(DepTuple template, Context ctx) {
		ArrayList<DepTuple> result = new ArrayList<DepTuple>();
		int tuplesToRead = (template.getN_Matches() > 0) ? template.getN_Matches() : tuplesBag.size();
		for(Iterator<DepTuple> iterator = tuplesBag.iterator(); iterator.hasNext() && (tuplesToRead > 0); ) {
			DepTuple tuple = iterator.next();
			if(!tuple.canIn(ctx.invokerID)) continue;
			if(!tuple.matches(template)) continue;
			iterator.remove();
			if(tuple.isExpired(ctx.time)) continue;
			result.add(tuple);
			tuplesToRead--;
		}
		return result;
	}


	/***********************
	 * BLOCKING OPERATIONS *
	 ***********************/
	@Override
	public DepTuple rd(DepTuple template, Context ctx) {
		throw new UnsupportedOperationException("Not implemented at this layer");
	}

	@Override
	public DepTuple in(DepTuple template, Context ctx) {
		throw new UnsupportedOperationException("Not implemented at this layer");
	}


}
