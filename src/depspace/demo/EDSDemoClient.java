package depspace.demo;

import depspace.general.DepSpaceConfiguration;
import depspace.recipes.EDSSharedValue;
import depspace.recipes.SharedValueService;


public class EDSDemoClient {

	public static void main(String[] args) throws Exception {
		// Parse arguments
		if(args.length < 3) {
			System.err.println("usage: java " + EDSDemoClient.class + " <client-id> <config-dir> <extension-code-dir>");
			System.exit(1);
		}
		int clientID = Integer.parseInt(args[0]);
		String configDir = args[1];
		String extensionCodeDir = args[2];
		
		// Preparations
		DepSpaceConfiguration.init(configDir);

		// Create counter
		EDSSharedValue counter = new EDSSharedValue(clientID, "SHARED_VALUE", true, extensionCodeDir);
		counter.start();
		
		// Use counter
		while(true) {
			int value = counter.incrementAndGet();
			System.out.println(value);
		}
	}
	
}
