package depspace.extension;

import depspace.general.Context;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceOperation;
import depspace.general.DepTuple;


public class EDSBuiltInExtension extends EDSBaseExtension {

	public static final String EXTENSION_NAME = EDSBuiltInExtension.class.getName();

	
	@Override
	public String toString() {
		return EXTENSION_NAME;
	}
	

	// ################
	// # SUBSCRIPTION #
	// ################

	@Override
	public boolean matchesOperation(String tsName, DepSpaceOperation operation, Object arg) {
		if(operation != DepSpaceOperation.OUT) return false;
		return EDSExtensionManager.EXTENSION_MANAGER_TUPLE_SPACE_NAME.equals(tsName);
	}
	
	@Override
	public boolean matchesEvent(String tsName, DepSpaceOperation operation, DepTuple tuple) {
		return false;
	}

	
	// #############
	// # EXECUTION #
	// #############
	
	@Override
	protected void out(DepTuple tuple, Context ctx) throws DepSpaceException {
		// Create extension tuple
		DepTuple extensionTuple = DepTuple.createTuple(ctx.session, tuple.getFields());
		extensionGate.cas(extensionTuple, extensionTuple);

		// Register extension
		EDSExtension extension = ((EDSExtensionManager) extensionGate).registerExtension((String) tuple.getFields()[0], (String) tuple.getFields()[1], ctx.session);

		// Initialize extension
		extension.init();
	}

}
