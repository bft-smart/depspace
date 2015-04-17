package depspace.extension;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import depspace.client.DepSpaceAccessor;
import depspace.client.DepSpaceAdmin;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceProperties;
import depspace.general.DepTuple;


public class EDSExtensionRegistration {

	private static String loadExtensionCode(Class<? extends EDSExtension> extensionClass, String codeBasePath) throws DepSpaceException {
		// Check file
		String codePath = codeBasePath + File.separatorChar + extensionClass.getName().replace('.', File.separatorChar) + ".java";
		File file = new File(codePath);
		if(!file.isFile()) throw new DepSpaceException(codePath + " is not a file");
		
		// Get extension code
		try {
			BufferedReader stream = new BufferedReader(new FileReader(file));
			String extensionCode = "";
			for(String line = stream.readLine(); line != null; line = stream.readLine()) extensionCode += line + "\n";
			stream.close();
			return extensionCode;
		} catch(Exception e) {
			e.printStackTrace();
			throw new DepSpaceException(e.toString());
		}
	}
	
	public static boolean checkExtension(Class<? extends EDSExtension> extensionClass, String codeBasePath) throws DepSpaceException {
		// Compile extension
		String extensionCode = loadExtensionCode(extensionClass, codeBasePath);
		EDSExtensionCompiler compiler = new EDSExtensionCompiler(extensionClass.getName(), extensionCode);
		byte[] extensionBinary = compiler.compileExtension();
		
		// Load extension
		EDSExtensionLoader extensionLoader = new EDSExtensionLoader(extensionClass.getName(), extensionBinary);
		extensionLoader.loadExtension();
		return true;
	}
	
	public static boolean registerExtension(DepSpaceAdmin admin, Class<? extends EDSExtension> extensionClass, String codeBasePath) throws DepSpaceException {
		try {
			String extensionCode = loadExtensionCode(extensionClass, codeBasePath);
			Properties properties = DepSpaceProperties.createDefaultProperties(EDSExtensionManager.EXTENSION_MANAGER_TUPLE_SPACE_NAME);
			DepSpaceAccessor eds = admin.createAccessor(properties, false);
			DepTuple extension = DepTuple.createTuple(extensionClass.getName(), extensionCode);
			eds.out(extension);
			return true;
		} catch(DepSpaceException dse) {
			return false;
		}
	}
	
	public static boolean useExtension(DepSpaceAdmin admin, Class<? extends EDSExtension> extensionClass) throws DepSpaceException {
		throw new UnsupportedOperationException("TODO: implement");
	}
	
	public static boolean deleteExtension() throws DepSpaceException {
		throw new UnsupportedOperationException("TODO: implement");
	}

}
