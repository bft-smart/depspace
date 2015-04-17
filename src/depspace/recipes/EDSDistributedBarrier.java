package depspace.recipes;

import depspace.extension.EDSExtensionRegistration;
import depspace.general.DepSpaceException;
import depspace.general.DepTuple;


public class EDSDistributedBarrier extends DepSpaceDistributedBarrier {

	private final DepTuple template;
	
	
	public EDSDistributedBarrier(int id, String tsName, int threshold, boolean createSpace, String basePath) throws DepSpaceException {
		super(id, tsName, threshold, createSpace);
		this.template = DepTuple.createTuple(EDSDistributedBarrierExtension.BARRIER_OPERATION, threshold);
		EDSExtensionRegistration.registerExtension(admin, EDSDistributedBarrierExtension.class, basePath);
	}

	
	@Override
	public void await() throws Exception {
		depSpace.in(template);
	}

}
