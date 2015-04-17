package depspace.general;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

import bftsmart.tom.MessageContext;
import bftsmart.tom.core.messages.TOMMessageType;
import depspace.confidentiality.ProtectionVector;

/**
 * Context. This class implements a Context for tuples.
 */
public class Context {
	
	public final String tsName;
    public final ProtectionVector[] protectionVectors;

    public transient final int invokerID;
	public transient final int consensusID;

	public transient long time;
	
	public transient final int session;
	public transient final int sequence;
	public transient final int operationID;
	public transient final int view;
	public transient final TOMMessageType requestType;
    
	
	private Context(String tsName, ProtectionVector[] protectionVectors) {
		this.tsName = tsName;
		this.protectionVectors = protectionVectors;
		this.invokerID = -1;
		this.consensusID = -1;
		this.time = -1;
		this.session = -1;
		this.sequence = -1;
		this.operationID = -1;
		this.view = -1;
		this.requestType = null;
	}
	
	
	public static Context createDefaultContext(String tsName, DepSpaceOperation operation, boolean useConfidentiality, DepTuple... tuples) {
		// Create protection vectors
		ProtectionVector[] vectors;
		switch(operation) {
		case CAS:
		case REPLACE:
			vectors = new ProtectionVector[2];
			break;
		default:
			vectors = new ProtectionVector[1];
		}
		
		// Handle confidentiality
    	if(useConfidentiality) {
	    	for(int i = 0; i < vectors.length; i++) {
	    		//ProtectionType[] types = new ProtectionType[tuple[i].getFields().length];
	    		int[] types = new int[tuples[i].getFields().length];
	    		Arrays.fill(types, ProtectionVector.CO);
	    		vectors[i] = new ProtectionVector(types);
	    	}
	    }
    	return new Context(tsName, vectors);
	}
	
    
	public String toString(){
		StringBuilder sb = new StringBuilder("{");
		sb.append("InvokerId = ").append(invokerID).append("| tsName = ").append(tsName).append("| ConsensusId = ").append(consensusID);
		return sb.toString();
	}

	@Override
	public boolean equals(Object object) {
		if(!(object instanceof Context)) return false;
		Context other = (Context) object;
		if(!tsName.equals(other.tsName)) return false;
		if(time != other.time) return false;
		return Arrays.equals(protectionVectors, other.protectionVectors);
	}
	
	public void updateTime(long time) {
		this.time = Math.max(this.time, time);
	}
	
	
	// #####################################
	// # SERIALIZATION AND DESERIALIZATION #
	// #####################################

	public void serialize(ObjectOutput stream) throws IOException {
		stream.writeObject(tsName);
		for(ProtectionVector protectionVector: protectionVectors) stream.writeObject(protectionVector);
	}

	public Context(ObjectInput stream, DepSpaceOperation operation, MessageContext tomMessageContext) throws Exception {
		// Deserialize non-transient attributes
		this.tsName = (String) stream.readObject();
		switch(operation) {
		case CAS:
		case REPLACE:
			this.protectionVectors = new ProtectionVector[] { (ProtectionVector) stream.readObject(), (ProtectionVector) stream.readObject() };
			break;
		default:
			this.protectionVectors = new ProtectionVector[] { (ProtectionVector) stream.readObject() };
		}
		
		// Create transient attributes
		this.invokerID = tomMessageContext.getSender();
		this.consensusID = tomMessageContext.getConsensusId();
		this.time = tomMessageContext.getTimestamp();
		this.session = tomMessageContext.request.getSession();
		this.sequence = tomMessageContext.request.getSequence();
		this.operationID = tomMessageContext.request.getOperationId();
		this.view = tomMessageContext.request.getViewID();
		this.requestType = tomMessageContext.request.getReqType();
	}
	
}
