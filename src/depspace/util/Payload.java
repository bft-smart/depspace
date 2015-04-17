package depspace.util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


public class Payload {

	public final byte[] payload;


	public Payload(byte[] payload) {
		this.payload = payload;
	}


	public boolean matches(byte[] data) {
		return matches(payload, data);
	}
	
	@Override
	public boolean equals(Object object) {
		if(object == null) return false;
		if(!(object instanceof Payload)) return false;
		Payload other = (Payload) object;
		return matches(other.payload);
	}

	@Override
	public int hashCode() {
		return payload.length;
	}

	public static boolean matches(byte[] chunkA, byte[] chunkB) {
		// Simple checks first
		if(chunkA == null) return false;
		if(chunkB == null) return false;
		
		// Compare chunk lengths
		if(chunkA.length != chunkB.length) return false;
		
		// Compare chunk contents
		for(int i = 0; i < chunkA.length; i++) {
			if(chunkA[i] != chunkB[i]) return false;
		}
		return true;
	}

	public static String toString(byte[] data) {
		String s = "";
		for(byte b: data) s += String.format("%04d", b);
		return s;
	}
	
	
	public static void writeChunk(byte[] chunk, ObjectOutput stream) throws IOException {
		if(chunk == null) {
			stream.writeInt(-1);
		} else {
			stream.writeInt(chunk.length);
			stream.write(chunk);
		}
	}

	public static byte[] readChunk(ObjectInput stream) throws IOException {
		int chunkSize = stream.readInt();
		if(chunkSize < 0) return null;
		byte[] chunk = new byte[chunkSize];
		int bytesRead, offset = 0;
		while((bytesRead = stream.read(chunk, offset, chunk.length - offset)) > 0) offset += bytesRead;
		return chunk;
	}
	
}
