package depspace.general;

import java.util.Properties;


public class DepSpaceProperties extends Properties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7729585489041504228L;
	public static final int DPS_NAME = 14;// "depspace.name";
	public static final int DPS_CONFIDEALITY = 15;// "depspace.confidentiality";
	//private static final int DPS_ACCESS_CONTROL = 16;// "depspace.accesscontrol";
	public static final int DPS_POLICY_ENFORCEMENT = 17;// "depspace.policyenforcement";
	
	
	public static Properties createDefaultProperties(String tsName) {
		Properties properties = new Properties();
		properties.put(DPS_NAME, tsName);
		return properties;
	}
	
	public static String getTSName(Properties properties) {
		return (String) properties.get(DPS_NAME);
	}
	
	public static String getPolicy(Properties properties) {
		return (String) properties.get(DPS_POLICY_ENFORCEMENT);
	}
	
	public static boolean getUseConfidentiality(Properties properties) {
		return Boolean.parseBoolean((String) properties.get(DPS_CONFIDEALITY));
	}
	
}
