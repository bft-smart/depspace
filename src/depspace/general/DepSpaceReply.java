package depspace.general;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import depspace.util.Payload;



public class DepSpaceReply {

	public final DepSpaceOperation operation;
	public final Object arg;

	
	public DepSpaceReply(DepSpaceOperation operation, Object arg) {
		this.operation = operation;
		this.arg = arg;
	}
	
	
	@Override
	public String toString() {
		return "{ operation=" + operation + " | arg=" + arg + " }" ;
	}

	@Override
	public boolean equals(Object object) {
		if(!(object instanceof DepSpaceReply)) return false;
		DepSpaceReply other = (DepSpaceReply) object;
		if(operation != other.operation) return false;
		return arg.equals(other.arg);
	}
	
	
	// #####################################
	// # SERIALIZATION AND DESERIALIZATION #
	// #####################################

	public byte[] serialize() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			
			// Serialize operation
			oos.write((byte) operation.ordinal());
			
			// Serialize argument
			switch(operation) {
			case OUT:
			case OUTALL:
			case CREATE:
			case DELETE:
			case CLEAN:
				break;
			case RENEW:
			case RDP:
			case INP:
			case RD:
			case IN:
			case SIGNED_RD:
			case CAS:
			case REPLACE:
				byte[] chunk = (arg != null) ? ((DepTuple) arg).serialize() : null;
				Payload.writeChunk(chunk, oos);
				break;
			case RDALL:
			case INALL:
				@SuppressWarnings("unchecked")
				List<DepTuple> tuples = (List<DepTuple>) arg;
				oos.writeInt(tuples.size());
				for(DepTuple tuple: tuples) {
					chunk = tuple.serialize();
					Payload.writeChunk(chunk, oos);
				}
				break;
			case EXCEPTION:
				oos.writeObject(arg);
				break;
			default:
				System.err.println(DepSpaceReply.class.getSimpleName() + ".serialize(): Unhandled operation type " + operation);
			}
			
			oos.close();
			
			return baos.toByteArray();
		} catch(IOException ioe) {
			ioe.printStackTrace();
			return null;
		}
	}
	
	public DepSpaceReply(byte[] buffer) throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(buffer);
		ObjectInputStream ois = new ObjectInputStream(in);

		// Deserialize operation
		this.operation = DepSpaceOperation.getOperation(ois.read());
		
		// Deserialize argument
		switch(operation) {
		case OUT:
		case OUTALL:
		case CREATE:
		case DELETE:
		case CLEAN:
			this.arg = null;
			break;
		case RENEW:
		case RDP:
		case INP:
		case RD:
		case IN:
		case SIGNED_RD:
		case CAS:
		case REPLACE:
			byte[] chunk = Payload.readChunk(ois);
			this.arg = (chunk != null) ? new DepTuple(chunk) : null;
			break;
		case RDALL:
		case INALL:
			int listSize = ois.readInt();
			List<DepTuple> list = new ArrayList<DepTuple>(listSize);
			for(int i = 0; i < listSize; i++) {
				chunk = Payload.readChunk(ois);
				list.add(new DepTuple(chunk));
			}
			this.arg = list;
			break;
		case EXCEPTION:
			this.arg = ois.readObject();
			break;
		default:
			System.err.println(DepSpaceReply.class.getSimpleName() + "(): Unhandled operation type " + operation);
			this.arg = null;
		}
		ois.close();
	}
	
}
