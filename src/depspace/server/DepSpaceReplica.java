package depspace.server;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import bftsmart.statemanagement.StateManager;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ReplicaContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;
import depspace.extension.EDSExtensionManager;
import depspace.general.Context;
import depspace.general.DepSpaceConfiguration;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceOperation;
import depspace.general.DepSpaceReply;
import depspace.general.DepSpaceRequest;
import depspace.general.DepTuple;


public class DepSpaceReplica extends DefaultRecoverable implements DepSpaceEventHandler {

	private final int replicaID;
	private final DepSpaceManager spacesManager;
	private final Lock stateLock;
	
    private StateManager stateManager;
    private ReplicaContext replicaContext;
    
    private final BlockingDeque<DepSpaceReplyContent> replyQueue;
    
	
	public DepSpaceReplica(int id, boolean join) {
		new ServiceReplica(id, DepSpaceConfiguration.configHome, join, this, this);
		this.replicaID = id;
		this.spacesManager = DepSpaceConfiguration.IS_EXTENSIBLE ? new EDSExtensionManager(id, this) : new DepSpaceManager(id, this);
		this.stateLock = new ReentrantLock();
		this.replyQueue = new LinkedBlockingDeque<DepSpaceReplyContent>();
		(new DepSpaceReplicaSendThread()).start();
	}

	
	/********************
	 * BATCH EXECUTABLE *
	 ********************/
	
	@Override
	public byte[] executeUnordered(byte[] command, MessageContext msgCtx) {
		execute(command, msgCtx, true);
		return null;
	}
	
	private void execute(byte[] command, MessageContext msgCtx, boolean priority) {
		DepSpaceRequest request = null;
		try {
			// Invoke operation
			request = new DepSpaceRequest(command, msgCtx);
			Object result = spacesManager.invokeOperation(request.context.tsName, request.operation, request.arg, request.context);
			
			// Handle result
			if((result == null) && request.operation.isBlocking()) sendReply(TOMMessage.BLOCKING_HINT, request.context);
			else handleResult(request.operation, result, request.context, priority);
		} catch(Exception e) {
			DepSpaceException dse = (e instanceof DepSpaceException) ? (DepSpaceException) e : new DepSpaceException("Server-side exception: " + e);
			if(request != null) handleResult(DepSpaceOperation.EXCEPTION, dse, request.context, priority);
			e.printStackTrace();
		}
	}

	@Override
	public byte[][] executeBatch(byte[][] commands, MessageContext[] msgCtxs) {
        stateLock.lock();
        for(int i = 0; i < commands.length; i++) execute(commands[i], msgCtxs[i], false);
        stateLock.unlock();
        return new byte[commands.length][];
	}

	public void handleResult(DepSpaceOperation operation, Object result, Context ctx, boolean sendSync) {
		if(sendSync) {
			DepSpaceReply reply = new DepSpaceReply(operation, result);
			byte[] content = reply.serialize();
			sendReply(content, ctx);
		} else {
			DepSpaceReplyContent replyContent = new DepSpaceReplyContent(operation, result, ctx);
			replyQueue.addLast(replyContent);
		}
	}

	public void sendReply(byte[] content, Context ctx) {
		TOMMessage reply = new TOMMessage(replicaID, ctx.session, ctx.sequence, ctx.operationID, content, ctx.view, ctx.requestType);
		replicaContext.getServerCommunicationSystem().send(new int[] { ctx.invokerID }, reply);
	}

	
	/**************************
	 * DEPSPACE EVENT HANDLER *
	 **************************/
	
	@Override
	public void handleEvent(DepSpaceOperation operation, DepTuple tuple, Context ctx) {
		handleResult(operation, tuple, ctx, true);
	}
	
	
	/***************
	 * RECOVERABLE *
	 ***************/

	@Override
	public void setReplicaContext(ReplicaContext replicaContext) {
		super.setReplicaContext(replicaContext);
		this.replicaContext = replicaContext;
	}


	@Override
	public StateManager getStateManager() {
		if(stateManager == null) stateManager = super.getStateManager();
    	return stateManager;
	}

	@Override
	public void noOp(int op) {
		//NO OP IMPLEMENTATION
	}	

	/************************
	 * ASYNCHRONOUS SENDING *
	 ************************/
	
	private static class DepSpaceReplyContent {
		
		public final DepSpaceOperation operation;
		public final Object result;
		public final Context ctx;
		
		
		public DepSpaceReplyContent(DepSpaceOperation operation, Object result, Context ctx) {
			this.operation = operation;
			this.result = result;
			this.ctx = ctx;
		}
		
	}
	
	
	private class DepSpaceReplicaSendThread extends Thread {
		
		@Override
		public void run() {
			try {
				while(!isInterrupted()) {
					DepSpaceReplyContent replyContent = replyQueue.take();
					DepSpaceReply reply = new DepSpaceReply(replyContent.operation, replyContent.result);
					byte[] content = reply.serialize();
					sendReply(content, replyContent.ctx);
				}
			} catch(InterruptedException ie) {
				return;
			}
		}
	}
	
	
	/********
	 * MAIN *
	 ********/

	public static void main(String[] args) {
		if(args.length < 2) {
			System.out.println("Use: java DepSpaceServer <processId> <config-home> <join option (optional)>");
			System.exit(-1);
		}

		DepSpaceConfiguration.init(args[1]);
		boolean join = (args.length > 2) ? Boolean.valueOf(args[2]) : false;
		new DepSpaceReplica(Integer.parseInt(args[0]), join);
	}


	@Override
	public void installSnapshot(byte[] state) {
		
	}


	@Override
	public byte[] getSnapshot() {
		return new byte[] {1};
	}


	@Override
	public byte[][] appExecuteBatch(byte[][] commands, MessageContext[] msgCtxs) {
		return executeBatch(commands, msgCtxs);
	}

}
