package depspace.client;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import depspace.general.DepSpaceException;
import depspace.general.DepSpaceProperties;


public class DepSpaceAdmin {

	private final Map<String, DepSpaceAccessor> accessors;
	private final DepSpaceServiceProxy service;


	public DepSpaceAdmin(int clientID) {
		this.accessors = new TreeMap<String, DepSpaceAccessor>();
		this.service = new DepSpaceServiceProxy(clientID);
	}


	/**
	 * Creates a new logical tuple space and a new accessor for this space.
	 */
	public DepSpaceAccessor createSpace(Properties properties) throws DepSpaceException {
		return createAccessor(properties, true);
	}

	/** Creates a new accessor for a logical tuple space specified by the properties*/
	public synchronized DepSpaceAccessor createAccessor(Properties properties, boolean createSpace) throws DepSpaceException {
		// Get tuple-space name
		String tsName = DepSpaceProperties.getTSName(properties);
		if(tsName == null) throw new DepSpaceException("The DepSpace name must be specified");

		// Return accessor if it already exists
		DepSpaceAccessor accessor = accessors.get(tsName);
		if(accessor != null) return accessor;

		// Create replication layer
		DepSpaceClientLayer depSpaceClient = new ClientReplicationLayer(service);
		
		// Create confidentiality layer
		boolean useConfidentiality = DepSpaceProperties.getUseConfidentiality(properties);
		if(useConfidentiality) {
			System.err.println("TODO: Implement confidentiality layer");
//			ConfidentialityScheme scheme = null;
//			try {
//				PublicInfo publicInfo = new PublicInfo(config.getN(), config.getF() + 1,
//						config.getGroupPrimeOrder(),config.getGeneratorg(),config.getGeneratorG());
//				PVSSEngine engine = PVSSEngine.getInstance(publicInfo);
//				scheme = new ConfidentialityScheme(engine.getPublicInfo(), config.getPublicKeys());
//			} catch(Exception e) {
//				e.printStackTrace();
//			}
//			depSpaceClient = new ClientConfidentialityLayer(depSpaceClient, scheme));
		}
		
		// Create accessor
		accessor = new DepSpaceAccessor(depSpaceClient, tsName, useConfidentiality);
		accessors.put(tsName, accessor);

		// Create tuple space
		if(createSpace) depSpaceClient.createSpace(properties);
		return accessor;
	}
	
	/**
	 * Delete the logical tuple space identified by name
	 * @param name The logical Tuple Space name
	 * @throws DepSpaceException if specified Tuple Space wasn't created by this admin
	 */
	public synchronized void deleteSpace(String name) throws DepSpaceException {
		DepSpaceAccessor accessor = accessors.remove(name);
		if(accessor == null) throw new DepSpaceException("The specified DepSpace wasn't created by this admin");
		accessor.getTupleSpace().deleteSpace(name);
	}

	/** Finalize the accessor and delete the logical tuple space related with this accessor*/
	public synchronized void finalizeAccessor(DepSpaceAccessor accessor) throws DepSpaceException {
		accessors.remove(accessor.getTSName());
		accessor.getTupleSpace().deleteSpace(accessor.getTSName());
	}

}
