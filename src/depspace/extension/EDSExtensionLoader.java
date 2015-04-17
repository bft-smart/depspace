package depspace.extension;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import depspace.general.Context;
import depspace.general.DepSpaceConfiguration;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceOperation;
import depspace.general.DepTuple;


public class EDSExtensionLoader extends ClassLoader {

	// ###########
	// # HELPERS #
	// ###########

	private static final Class<?>[] CLASS_WHITE_LIST = {
		Object.class,

		Boolean.class,
		Byte.class,
		Character.class,
		Double.class,
		Integer.class,
		Float.class,
		Long.class,
		Short.class,
		
		Collection.class,
		Iterator.class,
		String.class,
		StringBuilder.class,

		DepTuple.class,
		DepSpaceOperation.class,
		Context.class,

		EDSExtension.class,
		EDSExtensionGate.class,
		EDSBaseExtension.class,
	};
	

	// ###############
	// # CONSTRUCTOR #
	// ###############
	
	private final Collection<String> whiteListClassNames;
	private final String extensionName;
	private final byte[] extensionBinary;
	
	
	public EDSExtensionLoader(String extensionName, byte[] extensionBinary) {
		// Store attributes
		this.extensionName = extensionName;
		this.extensionBinary = extensionBinary;

		if(DepSpaceConfiguration.ENABLE_SANDBOX) {
			// Process white list
			this.whiteListClassNames = new HashSet<String>();
			for(Class<?> c: CLASS_WHITE_LIST) whiteListClassNames.add(c.getName());
		} else {
			this.whiteListClassNames = null;
		}
	}
	

	// #####################
	// # EXTENSION LOADING #
	// #####################
	
	public EDSExtension loadExtension() throws DepSpaceException {
		try {
			// Load extension
			Class<?> extensionClass = loadClass(extensionName, true);
			EDSExtension extension = (EDSExtension) extensionClass.newInstance();
			return extension;
		} catch(Exception e) {
			e.printStackTrace();
			throw new DepSpaceException("Exception during extension loading: " + e);
		}
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if(!DepSpaceConfiguration.ENABLE_SANDBOX) return super.loadClass(name, resolve);
		return loadClassSandbox(name, resolve);
	}
	
	private synchronized Class<?> loadClassSandbox(String name, boolean resolve) throws ClassNotFoundException {
		// Check white list
		if(whiteListClassNames.contains(name)) return findSystemClass(name);

		// Only allow the extension to be loaded
		if(!extensionName.equals(name)) throw new ClassNotFoundException("An extension is not allowed to load class " + name);

		// Load extension
		Class<?> extensionClass = defineClass(extensionName, extensionBinary, 0, extensionBinary.length);
		
		// Check whether extension class really implements the extension interface
		if(!EDSExtension.class.isAssignableFrom(extensionClass)) throw new ClassNotFoundException("The extension does not implement the interface " + EDSExtension.class.getName());
		return extensionClass;
	}
	
}
