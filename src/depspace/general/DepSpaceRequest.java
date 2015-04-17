package depspace.general;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import depspace.util.Payload;

import bftsmart.tom.MessageContext;


public class DepSpaceRequest {

	public final int id;
	public final DepSpaceOperation operation;
	public final Object arg;
	public final Context context;

	
	public DepSpaceRequest(int id, DepSpaceOperation operation, Object arg, Context context) {
		this.id = id;
		this.operation = operation;
		this.arg = arg;
		this.context = context;
	}
	
	
	@Override
	public String toString() {
		return "{ id=" + id + " | operation=" + operation + " | arg=" + arg + " | tsname=" + context.tsName + " }" ;
	}

	@Override
	public boolean equals(Object object) {
		if(!(object instanceof DepSpaceRequest)) return false;
		DepSpaceRequest other = (DepSpaceRequest) object;
		if(id != other.id) return false;
		if(operation != other.operation) return false;
		if(!context.equals(other.context)) return false;
		if(arg == null) return (other.arg == null);
		if(other.arg == null) return false;
		switch(operation) {
		case CAS:
		case REPLACE:
			return Arrays.equals((DepTuple[]) arg, (DepTuple[]) other.arg);
		default:
			return arg.equals(other.arg);
		}
	}
	
	
	// #####################################
	// # SERIALIZATION AND DESERIALIZATION #
	// #####################################

	public byte[] serialize() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			
			// Serialize standard attributes
			oos.writeInt(id);
			oos.write((byte) operation.ordinal());
			context.serialize(oos);
			
			// Serialize argument
			switch(operation) {
			case OUT:
			case RENEW:
			case RDP:
			case INP:
			case RD:
			case IN:
			case SIGNED_RD:
			case RDALL:
			case INALL:
			case CLEAN:
				byte[] chunk = (arg != null) ? ((DepTuple) arg).serialize() : null;
				Payload.writeChunk(chunk, oos);
				break;
			case CAS:
			case REPLACE:
				for(DepTuple tuple: (DepTuple[]) arg) {
					chunk = tuple.serialize();
					Payload.writeChunk(chunk, oos);
				}
				break;
			case OUTALL:
				@SuppressWarnings("unchecked")
				List<DepTuple> tuples = (List<DepTuple>) arg;
				oos.writeInt(tuples.size());
				for(DepTuple tuple: tuples) {
					chunk = tuple.serialize();
					Payload.writeChunk(chunk, oos);
				}
				break;
			case CREATE:
			case DELETE:
				oos.writeObject(arg);
				break;
			default:
				System.err.println(DepSpaceRequest.class.getSimpleName() + ".serialize(): Unhandled operation type " + operation);
			}
			
			oos.close();
			return baos.toByteArray();
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	@SuppressWarnings("unchecked")
	public DepSpaceRequest(byte[] buffer, MessageContext tomMessageContext) throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(buffer);
		ObjectInputStream ois = new ObjectInputStream(in);
		
		// Deserialize standard attributes
		this.id = ois.readInt();
		this.operation = DepSpaceOperation.getOperation(ois.read());
		this.context = new Context(ois, operation, tomMessageContext);
		
		// Deserialize protection vectors and arguments
		switch(operation) {
		case OUT:
		case RENEW:
		case RDP:
		case INP:
		case RD:
		case IN:
		case SIGNED_RD:
		case RDALL:
		case INALL:
		case CLEAN:
			byte[] chunk = Payload.readChunk(ois);
			this.arg = (chunk != null) ? new DepTuple(chunk) : null;
			break;
		case CAS:
		case REPLACE:
			this.arg = new DepTuple[2];
			for(int i = 0; i < 2; i++) {
				chunk = Payload.readChunk(ois);
				((DepTuple[]) arg)[i] = new DepTuple(chunk);
			}
			break;
		case OUTALL:
			int size = ois.readInt();
			List<DepTuple> list = new ArrayList<DepTuple>(size);
			for(int i = 0; i < size; i++) {
				chunk = Payload.readChunk(ois);
				list.add(new DepTuple(chunk));
			}
			this.arg = list;
			break;
		case CREATE:
		case DELETE:
			this.arg = ois.readObject();
			break;
		default:
			System.err.println(DepSpaceRequest.class.getSimpleName() + "(): Unhandled operation type " + operation);
			this.arg = null;
		}
		ois.close();

		// Update expiration time
		if(arg == null) return;
		if(operation == DepSpaceOperation.CREATE) return;
		if(operation == DepSpaceOperation.OUTALL) {
			for(DepTuple tuple: (List<DepTuple>) arg) {
				long expirationTimeOffset = tuple.getExpirationTime();
				if(expirationTimeOffset != 0) tuple.setExpirationTime(context.time + expirationTimeOffset);
			}
		} else {
			DepTuple tuple;
			switch(operation) {
			case CAS:
			case REPLACE:
				tuple = ((DepTuple[]) arg)[1];
				break;
			default:
				tuple = (DepTuple) arg;
			}
			long expirationTimeOffset = tuple.getExpirationTime();
			if(expirationTimeOffset != 0) tuple.setExpirationTime(context.time + expirationTimeOffset);
		}
	}

}
