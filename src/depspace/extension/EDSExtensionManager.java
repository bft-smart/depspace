package depspace.extension;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bftsmart.tom.core.messages.TOMMessageType;
import depspace.general.Context;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceOperation;
import depspace.general.DepTuple;
import depspace.server.DepSpaceEventHandler;
import depspace.server.DepSpaceManager;


public class EDSExtensionManager extends DepSpaceManager implements EDSExtensionGate, DepSpaceEventHandler {

	public static final String EXTENSION_MANAGER_TUPLE_SPACE_NAME = "EDS_SPACE";
	
	private final Map<Integer, EDSExtension> extensions;
	private final DepSpaceEventHandler defaultEventHandler;
	private final EDSBuiltInExtension builtInExtension;
	
	private Context currentContext;
	
	
	public EDSExtensionManager(int processID, DepSpaceEventHandler eventHandler) {
		super(processID, null);
		this.extensions = new HashMap<Integer, EDSExtension>();

		// Use the extension manager as a proxy event handler
		this.defaultEventHandler = eventHandler;
		setEventHandler(this);
		
		// Register built-in extension
		this.builtInExtension = new EDSBuiltInExtension();
		builtInExtension.setExtensionGate(this);
		extensions.put(0, builtInExtension);
	}

	
	@Override
	public Object invokeOperation(String tsName, DepSpaceOperation operation, Object arg, Context ctx) throws DepSpaceException {
		// Try to match subscriptions
		int[] ids = new int[] { 0, ctx.session };
		EDSExtension extension = null;
		for(int id: ids) {
			EDSExtension ext = extensions.get(id);
			if(ext == null) continue;
			if(ext.matchesOperation(tsName, operation, arg)) {
				extension = ext;
				break;
			}
		}
		if(extension == null) return super.invokeOperation(tsName, operation, arg, ctx);

		// Execute extension
		currentContext = ctx;
		try {
			return extension.handleOperation(tsName, operation, arg, ctx);
		} catch(NoClassDefFoundError ncdfe) {
			ncdfe.printStackTrace();
			throw new DepSpaceException(ncdfe.getCause().getMessage());
		}
	}
	
	
	// ##################
	// # EVENT HANDLING #
	// ##################

	@Override
	public void handleEvent(DepSpaceOperation operation, DepTuple tuple, Context ctx) {
		// Execute extension if it matches
		EDSExtension extension = extensions.get(ctx.session);
		if((extension != null) && extension.matchesEvent(ctx.tsName, operation, tuple)) {
			// Ensure that the current timestamp is used during event handling 
			ctx.updateTime(currentContext.time);
			try {
				Context savedContext = currentContext;
				currentContext = ctx;
				boolean handled = extension.handleEvent(ctx.tsName, operation, tuple, ctx);
				currentContext = savedContext;
				if(handled) return;
			} catch(DepSpaceException dse) {
				System.err.println(dse);
				// Fall back to default event handler
			} catch(NoClassDefFoundError ncdfe) {
				ncdfe.printStackTrace();
				// Fall back to default event handler
			}
		}
		
		// Use default event handler
		defaultEventHandler.handleEvent(operation, tuple, ctx);
	}

	
	// ##########################
	// # EXTENSION REGISTRATION #
	// ##########################

	public EDSExtension registerExtension(String extensionName, String extensionCode, int sessionID) throws DepSpaceException {
		// Compile extension
		EDSExtensionCompiler extensionCompiler = new EDSExtensionCompiler(extensionName, extensionCode);
		byte[] extensionBinary = extensionCompiler.compileExtension();
		
		// Load extension
		EDSExtensionLoader extensionLoader = new EDSExtensionLoader(extensionName, extensionBinary);
		EDSExtension extension = extensionLoader.loadExtension();
		extension.setExtensionGate(this);
		
		// Register extension
		extensions.put(sessionID, extension);
		return extension;
	}
	
	
	// ##################
	// # EXTENSION GATE #
	// ##################
	
	private Object extensionGateInvoke(DepSpaceOperation operation, Object arg) throws DepSpaceException {
		// Ensure that unordered requests do not perform modifying operations
		if((operation.getRequestType() == TOMMessageType.ORDERED_REQUEST) && (currentContext.requestType == TOMMessageType.UNORDERED_REQUEST)) {
			throw new DepSpaceException("Unordered requests must not perform modifying operations!");
		}
		
		/***** Implement additional extension policies here *****/
		
		// Invoke upper layer
		return super.invokeOperation(currentContext.tsName, operation, arg, currentContext);
	}
	
	
	@Override
	public void out(DepTuple tuple) throws DepSpaceException {
		extensionGateInvoke(DepSpaceOperation.OUT, tuple);
	}

	@Override
	public void outAll(List<DepTuple> tuplesBag) throws DepSpaceException {
		extensionGateInvoke(DepSpaceOperation.OUTALL, tuplesBag);
	}

	@Override
	public DepTuple renew(DepTuple template) throws DepSpaceException {
		return (DepTuple) extensionGateInvoke(DepSpaceOperation.RENEW, template);
	}

	@Override
	public DepTuple rdp(DepTuple template) throws DepSpaceException {
		return (DepTuple) extensionGateInvoke(DepSpaceOperation.RDP, template);
	}

	@Override
	public DepTuple inp(DepTuple template) throws DepSpaceException {
		return (DepTuple) extensionGateInvoke(DepSpaceOperation.INP, template);
	}

	@Override
	public DepTuple rd(DepTuple template) throws DepSpaceException {
		return (DepTuple) extensionGateInvoke(DepSpaceOperation.RD, template);
	}

	@Override
	public DepTuple in(DepTuple template) throws DepSpaceException {
		return (DepTuple) extensionGateInvoke(DepSpaceOperation.IN, template);
	}

	@Override
	public DepTuple cas(DepTuple template, DepTuple tuple) throws DepSpaceException {
		return (DepTuple) extensionGateInvoke(DepSpaceOperation.CAS, new DepTuple[] { template, tuple });
	}

	@Override
	public DepTuple replace(DepTuple template, DepTuple tuple) throws DepSpaceException {
		return (DepTuple) extensionGateInvoke(DepSpaceOperation.REPLACE, new DepTuple[] { template, tuple });
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<DepTuple> rdAll(DepTuple template) throws DepSpaceException {
		return (Collection<DepTuple>) extensionGateInvoke(DepSpaceOperation.RDALL, template);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<DepTuple> rdAll() throws DepSpaceException {
		return (Collection<DepTuple>) extensionGateInvoke(DepSpaceOperation.RDALL, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<DepTuple> inAll(DepTuple template) throws DepSpaceException {
		return (Collection<DepTuple>) extensionGateInvoke(DepSpaceOperation.INALL, template);
	}

	
	// #######################
	// # SAFE HELPER METHODS #
	// #######################

	@Override
	public void error(String message) throws DepSpaceException {
		throw new DepSpaceException(message);
	}

}
