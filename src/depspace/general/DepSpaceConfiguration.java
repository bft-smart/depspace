package depspace.general;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Properties;

import pvss.PublicInfo;


public class DepSpaceConfiguration {

	public static final boolean IS_EXTENSIBLE = true;
	public static final boolean ENABLE_SANDBOX = true;
	public static final int MAX_EXTENSION_CODE_SIZE = 8192;

	public static String configHome;
	public static int clientRetransmissionTimeout;
	public static boolean realTimeNew;
	public static boolean replaceTrigger;

	public static String tupleSpaceImpl;

	public static int n;
	public static int f;

	public static BigInteger groupPrimeOrder;
	public static BigInteger generatorg;
	public static BigInteger generatorG;
	public static BigInteger[] publicKeys;
	public static BigInteger[] secretKeys;
	public static int depth;


	public static void init(String configHome) {
		if((configHome == null) || (configHome.isEmpty())) configHome = "config";
		DepSpaceConfiguration.configHome = configHome;

		// Load configuration
		Properties config = new Properties();
		try {
			InputStream stream = new FileInputStream(configHome + System.getProperty("file.separator") + "depspace.config");
			config.load(stream);
			stream.close();
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}

		// General info
		clientRetransmissionTimeout = Integer.parseInt(config.getProperty("system.general.retryTime", "10000"));
		realTimeNew = Boolean.parseBoolean(config.getProperty("system.general.realTimeRenew", "true"));
		replaceTrigger = Boolean.parseBoolean(config.getProperty("system.general.replaceTrigger", "false"));
		tupleSpaceImpl = config.getProperty("system.general.tupleSpaceImpl", "List");
		depth = Integer.parseInt(config.getProperty("system.general.depth", "2"));

		// Info necessary for confidentiality layer
		groupPrimeOrder = new BigInteger(config.getProperty("system.confidentiality.groupPrimeOrder", "3813369993959833155649376866168717993247966019469968618851"));
		generatorg = new BigInteger(config.getProperty("system.confidentiality.generatorg", "1713411152149572460609939359168479341022990105326255541669"));
		generatorG = new BigInteger(config.getProperty("system.confidentiality.generatorG", "2532677346424118791910518448076602467150725367353857847463"));

		publicKeys = new BigInteger[n];
		secretKeys = new BigInteger[n];
		for(int i = 0; i < n; i++) {
			publicKeys[i] = new BigInteger(config.getProperty("system.confidentiality.publicKey." + i, "3444452163413827351943839248528410269180092202949006540287"));
			secretKeys[i] = new BigInteger(config.getProperty("system.confidentiality.secretKey." + i, "970240587165602351282744419152364373924879082324066857233"));
		}


		// Load configuration
		config = new Properties();
		try {
			InputStream stream = new FileInputStream(configHome + System.getProperty("file.separator") + "system.config");
			config.load(stream);
			stream.close();
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}

		n = Integer.parseInt(config.getProperty("system.servers.num", "4"));
		String fValue = config.getProperty("system.servers.f", "1");
		f = (fValue != null) ? Integer.parseInt(fValue) : (int) Math.ceil((n - 1) / 3);

	}


	public static PublicInfo createPublicInfo() {
		return new PublicInfo(n, f + 1, groupPrimeOrder, generatorg, generatorG);
	}

}
