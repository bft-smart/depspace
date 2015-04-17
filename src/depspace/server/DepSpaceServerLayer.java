package depspace.server;

import depspace.general.Context;
import depspace.general.DepSpace;
import depspace.general.DepSpaceException;
import depspace.general.DepTuple;


public abstract class DepSpaceServerLayer implements DepSpace {

	protected final DepSpaceServerLayer upperLayer;
	
	
	public DepSpaceServerLayer(DepSpaceServerLayer upperLayer) {
		this.upperLayer = upperLayer;
	}
	
	
	public DepSpaceServerLayer getUpperLayer() {
		return upperLayer;
	}
	
    public abstract Object signedRD(DepTuple template, Context ctx) throws DepSpaceException;
    public abstract void clean(DepTuple proof, Context ctx) throws DepSpaceException;

}
