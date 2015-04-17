package depspace.recipes;

import depspace.extension.EDSExtensionRegistration;
import depspace.general.DepSpaceException;
import depspace.general.DepTuple;


public class EDSSharedValue extends DepSpaceSharedValue {
	
	private static final DepTuple GET_AND_INCREMENT_TEMPLATE = DepTuple.createTuple(EDSSharedValueExtension.INCREMENT_OPERATION);
	

	public EDSSharedValue(int id, String tsName, boolean createSpace, String basePath) throws DepSpaceException {
		super(id, tsName, createSpace);
		EDSExtensionRegistration.registerExtension(admin, EDSSharedValueExtension.class, basePath);
	}

	
	@Override
	public int getAndIncrement() throws Exception {
		DepTuple result = depSpace.inp(GET_AND_INCREMENT_TEMPLATE);
		count = (Integer) result.getFields()[0];
		return count;
	}
	
	@Override
	public int incrementAndGet() throws Exception {
		return getAndIncrement() + 1;
	}
	
}
