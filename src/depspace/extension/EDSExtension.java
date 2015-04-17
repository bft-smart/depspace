package depspace.extension;

import depspace.general.Context;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceOperation;
import depspace.general.DepTuple;


public interface EDSExtension {

	public void setExtensionGate(EDSExtensionGate extensionGate);

	public boolean matchesOperation(String tsName, DepSpaceOperation operation, Object arg);
	
	public boolean matchesEvent(String tsName, DepSpaceOperation operation, DepTuple tuple);

	public void init() throws DepSpaceException;
	
	public Object handleOperation(String tsName, DepSpaceOperation operation, Object arg, Context ctx) throws DepSpaceException;
	
	public boolean handleEvent(String tsName, DepSpaceOperation operation, DepTuple tuple, Context ctx) throws DepSpaceException;

}
