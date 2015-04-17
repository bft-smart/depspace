package depspace.recipes;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import depspace.client.DepSpaceAccessor;
import depspace.client.DepSpaceAdmin;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceProperties;
import depspace.general.DepTuple;


public class DepSpaceLeaderElection implements LeaderElectionService {

	protected final DepSpaceAdmin admin;
	protected final DepSpaceAccessor depSpace;
	
	private final Set<LeaderElectionListener> listeners;
	private final Thread thread;
	private final Semaphore startLock;
	private final Semaphore stopLock;
	private final long retryTimeout;
	private final long leaderRenewTimeout;
	
	protected final DepTuple token;

	
	public DepSpaceLeaderElection(int id, String tsName, long retryTimeout, long leaderRenewTimeout, boolean createSpace) throws DepSpaceException {
		this.admin = new DepSpaceAdmin(id);
		this.depSpace = admin.createAccessor(DepSpaceProperties.createDefaultProperties(tsName), createSpace);
		this.listeners = new HashSet<LeaderElectionListener>();
		this.thread = new DepSpaceLeaderElectionThread();
		this.startLock = new Semaphore(0);
		this.stopLock = new Semaphore(0);
		this.retryTimeout = retryTimeout;
		this.leaderRenewTimeout = leaderRenewTimeout;
		this.token = DepTuple.createTimedTuple(leaderRenewTimeout * 2, EDSLeaderElectionExtension.LEADER_TOKEN_NAME, id);
	}


	@Override
	public synchronized void start() {
		if(!thread.isAlive()) thread.start();
		startLock.release();
	}

	@Override
	public void stop() {
		stopLock.release();
	}
	
	public void shutdown() {
		thread.interrupt();
	}

	@Override
	public void addListener(LeaderElectionListener listener) {
		synchronized(listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeListener(LeaderElectionListener listener) {
		synchronized(listeners) {
			listeners.remove(listener);
		}
	}
	
	protected void waitForLeadership() throws Exception {
		while(depSpace.cas(EDSLeaderElectionExtension.LEADER_TOKEN_TEMPLATE, token) != null) Thread.sleep(retryTimeout);
	}
	

	private class DepSpaceLeaderElectionThread extends Thread {
		
		@Override
		public void run() {
			try {
				while(true) {
					// Wait until someone starts the leader election
					startLock.acquire();
	
					// Wait until I am the leader
					waitForLeadership();

					// Tell listeners that I am the leader
					synchronized(listeners) {
						for(LeaderElectionListener listener: listeners) listener.onElectionEvent(EventType.ELECTED_COMPLETE);
					}
					
					// Periodically renew my leader token
					while(!stopLock.tryAcquire(leaderRenewTimeout, TimeUnit.MILLISECONDS)) depSpace.renew(token);
					
					// Remove my token
					depSpace.inp(token);
				}
			} catch(InterruptedException ie) {
				// Do nothing
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
