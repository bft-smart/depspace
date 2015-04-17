package depspace.recipes;

import depspace.extension.EDSExtensionRegistration;
import depspace.general.DepSpaceException;
import depspace.general.DepTuple;


public class EDSDistributedQueue extends DepSpaceQueue {

	public EDSDistributedQueue(int id, String tsName, boolean createSpace, String basePath) throws DepSpaceException {
		super(id, tsName, -1L, createSpace);
		EDSExtensionRegistration.registerExtension(admin, EDSDistributedQueueExtension.class, basePath);
	}
	
	
	@Override
	protected DepTuple createHeadTemplate() {
		return DepTuple.createTuple(EDSDistributedQueueExtension.HEAD_ELEMENT);
	}
	
	@Override
	public byte[] element() throws DepSpaceException {
		DepTuple head = depSpace.rdp(headTemplate);
		if(head == null) return null;
		return (byte[]) head.getFields()[1];
	}
	
	@Override
	public byte[] remove() throws DepSpaceException {
		DepTuple head = depSpace.inp(headTemplate);
		if(head == null) return null;
		return (byte[]) head.getFields()[1];
	}

	@Override
	public byte[] take() throws DepSpaceException, InterruptedException {
		DepTuple head = depSpace.in(headTemplate);
		return (byte[]) head.getFields()[1];
	}
	
}
