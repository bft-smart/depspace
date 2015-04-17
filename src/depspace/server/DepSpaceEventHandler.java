package depspace.server;

import depspace.general.Context;
import depspace.general.DepSpaceOperation;
import depspace.general.DepTuple;


public interface DepSpaceEventHandler {

	public void handleEvent(DepSpaceOperation operation, DepTuple tuple, Context ctx);
	
}
