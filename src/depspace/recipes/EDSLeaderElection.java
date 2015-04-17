package depspace.recipes;

import java.util.Collections;

import depspace.client.DepSpaceAccessor;
import depspace.client.DepSpaceAdmin;
import depspace.extension.EDSExtensionRegistration;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceProperties;
import depspace.general.DepTuple;


public class EDSLeaderElection extends DepSpaceLeaderElection {

	public EDSLeaderElection(int id, String tsName, long leaderRenewTimeout, boolean createSpace, String basePath, int watchDogID) throws DepSpaceException {
		super(id, tsName, -1L, leaderRenewTimeout, createSpace);
		
		// Register extension
		EDSExtensionRegistration.registerExtension(admin, EDSLeaderElectionExtension.class, basePath);
		if(watchDogID < 0) return;
		
		// Start watch dog
		watchDog = new EDSLeaderElectionWatchDog(watchDogID, tsName, leaderRenewTimeout, basePath);
		watchDog.start();
	}

	
	@Override
	public void shutdown() {
		synchronized(watchDog) {
			if(watchDog.isAlive()) watchDog.interrupt();
		}
		super.shutdown();
	}
	
	@Override
	protected void waitForLeadership() throws Exception {
		depSpace.in(token);
	}

	
	// #############
	// # WATCH DOG #
	// #############
	
	private static Thread watchDog;
			

	/*
	 * Unsatisfying workaround: By periodically sending a request,
	 * the watch dog ensures that DepSpace removes the timed tuple of
	 * the current leader in cases in which this leader has crashed.
	 */
	private static class EDSLeaderElectionWatchDog extends Thread {
		
		private final DepSpaceAdmin admin;
		private final DepSpaceAccessor depSpace;
		private final long interval;

		
		public EDSLeaderElectionWatchDog(int id, String tsName, long interval, String basePath) throws DepSpaceException {
			this.admin = new DepSpaceAdmin(id);
			this.depSpace = admin.createAccessor(DepSpaceProperties.createDefaultProperties(tsName), false);
			EDSExtensionRegistration.registerExtension(admin, EDSLeaderElectionExtension.class, basePath);
			this.interval = interval;
		}
		
		
		@Override
		public void run() {
			try {
				while(true) {
					depSpace.outAll(Collections.<DepTuple> emptyList());
					sleep(interval);
				}
			} catch(InterruptedException ie) {
				// Do nothing
			} catch(DepSpaceException dse) {
				dse.printStackTrace();
			}
		}
		
	}
	
}
