package depspace.client;

import java.util.Collection;
import java.util.List;

import depspace.general.Context;
import depspace.general.DepSpace;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceOperation;
import depspace.general.DepTuple;


/**
* Proxy/Accessor layer for DepSpace clients. 
*
* @author		Alysson Bessani
* @author2		Eduardo Alchieri
* @author3		Rui Posse (ruiposse@gmail.com)
* @version		DepSpace 2.0
* @date		
*/
public class DepSpaceAccessor implements DepSpace {

	// The tuple space
    private final DepSpaceClientLayer tupleSpace;

    // The name of the tuple space
    private final String tsName;

    // Confidentiality layer switch
	private final boolean useConfidentiality;
	
	
	public DepSpaceAccessor(DepSpaceClientLayer tupleSpace, String tsName, boolean useConfidentiality) {
		this.tupleSpace = tupleSpace;
		this.tsName = tsName;
		this.useConfidentiality = useConfidentiality;
	}

	
	public DepSpaceClientLayer getTupleSpace() {
		return tupleSpace;
	}
	
	public String getTSName() {
		return tsName;
	}
	
	
    /***********************
	 * DEPSPACE METHODS *
	 ***********************/
    
    @Override
    public void out(DepTuple tuple, Context ctx) throws DepSpaceException {
        tupleSpace.out(tuple, ctx);
    }
    
	@Override
	public DepTuple rdp(DepTuple template, Context ctx) throws DepSpaceException {
		return tupleSpace.rdp(template, ctx);
	}

	@Override
	public DepTuple inp(DepTuple template, Context ctx) throws DepSpaceException {
		return tupleSpace.inp(template, ctx);
	}

	@Override
	public DepTuple rd(DepTuple template, Context ctx) throws DepSpaceException {
		return tupleSpace.rd(template, ctx);
	}

	@Override
	public DepTuple in(DepTuple template, Context ctx) throws DepSpaceException {
		return tupleSpace.in(template, ctx);
	}

	@Override
	public void outAll(List<DepTuple> tuplesBag, Context ctx) throws DepSpaceException {
		tupleSpace.outAll(tuplesBag, ctx);
	}

	@Override
	public DepTuple renew(DepTuple template, Context ctx) throws DepSpaceException {
		return tupleSpace.renew(template, ctx);
	}

	@Override
	public DepTuple cas(DepTuple template, DepTuple tuple, Context ctx) throws DepSpaceException {
		return tupleSpace.cas(template, tuple, ctx);
	}

	@Override
	public DepTuple replace(DepTuple template, DepTuple tuple, Context ctx) throws DepSpaceException {
		return tupleSpace.replace(template, tuple, ctx);
	}

	@Override
	public Collection<DepTuple> rdAll(DepTuple template, Context ctx) throws DepSpaceException {
		return tupleSpace.rdAll(template, ctx);
	}

	@Override
	public Collection<DepTuple> inAll(DepTuple template, Context ctx) throws DepSpaceException {
		return tupleSpace.inAll(template, ctx);
	}

    
    /***********************
	 * CONVENIENCE METHODS *
	 ***********************/

    public void out(DepTuple tuple) throws DepSpaceException {
        out(tuple, createDefaultContext(DepSpaceOperation.OUT, tuple));
    }
    
	public DepTuple rdp(DepTuple template) throws DepSpaceException {
        return rdp(template, createDefaultContext(DepSpaceOperation.RDP, template));
    }

	public DepTuple inp(DepTuple template) throws DepSpaceException {
		return inp(template, createDefaultContext(DepSpaceOperation.INP, template));
	}

	public DepTuple rd(DepTuple template) throws DepSpaceException {
		return rd(template, createDefaultContext(DepSpaceOperation.RD, template));
	}

	public DepTuple in(DepTuple template) throws DepSpaceException {
		return in(template, createDefaultContext(DepSpaceOperation.IN, template));
	}

	public void outAll(List<DepTuple> tuplesBag) throws DepSpaceException {
		outAll(tuplesBag, createDefaultContext(DepSpaceOperation.OUTALL, tuplesBag.toArray(new DepTuple[tuplesBag.size()])));
	}

	public DepTuple renew(DepTuple template) throws DepSpaceException {
		return renew(template, createDefaultContext(DepSpaceOperation.RENEW, template));
	}

	public DepTuple cas(DepTuple template, DepTuple tuple) throws DepSpaceException {
		return cas(template, tuple, createDefaultContext(DepSpaceOperation.CAS, template));
	}

	public DepTuple replace(DepTuple template, DepTuple tuple) throws DepSpaceException {
		return replace(template, tuple, createDefaultContext(DepSpaceOperation.REPLACE, template));
	}

	@Override
	public Collection<DepTuple> rdAll() throws DepSpaceException {
		return rdAll(null, createDefaultContext(DepSpaceOperation.RDALL, (DepTuple[]) null));
	}

	public Collection<DepTuple> rdAll(DepTuple template, int n_matches) throws DepSpaceException {
		template.setN_Matches(n_matches);
		return rdAll(template, createDefaultContext(DepSpaceOperation.RDALL, template));
	}

	public Collection<DepTuple> inAll(DepTuple template) throws DepSpaceException {
		return inAll(template, createDefaultContext(DepSpaceOperation.INALL, template));
	}

	private Context createDefaultContext(DepSpaceOperation operation, DepTuple... tuples) {
		return Context.createDefaultContext(tsName, operation, useConfidentiality, tuples);
	}

}
