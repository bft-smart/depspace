package depspace.general;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.Arrays;

import pvss.InvalidVSSScheme;
import pvss.PublicInfo;
import pvss.PublishedShares;
import pvss.Share;

/**
 * Replication layer for DepSpace clients. 
 *
 * @author		Alysson Bessani
 * @author2		Eduardo Alchieri
 * @author3		Rui Posse (ruiposse@gmail.com)
 * @version		DepSpace 2.0
 * @date		
 */
public class DepTuple implements Externalizable {
	
	public static final String WILDCARD = "*";
	
	// This tuple fields
    private Object[] fields;
    
    // TODO comment
    private  int[] c_rd;
    private  int[] c_in;
    
    // TODO comment
    private PublishedShares publishedShares;
    
    // TODO comment
    private Share share = null;
    
    // The expiration time of this tuple
    // If "expirationTime = 0", tuple is not timed.
    private long expirationTime;
    
    // If this tuple is a template for rdAll or inAll,
    // it must carry an n_matches
    private int n_matches;
    
    
	/**************************************************
	 *					CONSTRUCTORS				  *
	 *			 called by 'create' methods			  *
	 **************************************************/
    
    public DepTuple() {
        this.c_rd = null;
        this.c_in = null;
        this.fields = null;
        this.publishedShares = null;
        this.expirationTime = 0;
        this.n_matches = -1;
    }
    
    private DepTuple(Object[] fields, int[] c_rd, int[] c_in,
            PublishedShares publishedShares) {
        this.c_rd = c_rd;
        this.c_in = c_in;
        this.fields = fields;
        this.publishedShares = publishedShares;
        this.expirationTime = 0;
        this.n_matches = -1;
    }
    
    private DepTuple(Object[] fields, int[] c_rd, int[] c_in,
            PublishedShares publishedShares, Share share) {
        this.c_rd = c_rd;
        this.c_in = c_in;
        this.fields = fields;
        this.publishedShares = publishedShares;
        this.share = share;
        this.expirationTime = 0;
        this.n_matches = -1;
    }
    
    /**
     * Creates a new Timed Tuple.
     * @param expirationTime The expiration time in seconds
     */
    private DepTuple(Object[] fields, int[] c_rd, int[] c_in, long expirationTime) {
        this.c_rd = c_rd;
        this.c_in = c_in;
        this.fields = fields;
        this.expirationTime = expirationTime;
    	this.n_matches = -1;
    }
    
    
    /**************************************************
	 *					 CREATORS					  *
	 *	 create methods for several kinds of tuples	  *
	 **************************************************/
    
    public static final DepTuple createTuple(Object... fields) {
        return new DepTuple(fields,null,null,null);
    }
    
    public static final DepTuple createAccessControledTuple(int[] c_rd,
            int[] c_in, Object... fields) {
        return new DepTuple(fields, c_rd, c_in, null);
    }
    
    public static final DepTuple createAccessControledTimedTuple(int[] c_rd,
            int[] c_in, long expirationTime,Object... fields) {
        return new DepTuple(fields, c_rd, c_in, expirationTime);
    }
    
    public static final DepTuple internalCreateConfidentialTuple(
            int[] c_rd, int[] c_in, Object[] fields, PublishedShares publishedShares) {
        return new DepTuple(fields,c_rd,c_in,publishedShares);
    }
    
    public static final DepTuple internalCreateConfidentialTuple(
            int[] c_rd, int[] c_in, Object[] fields, PublishedShares publishedShares, Share share) {
        return new DepTuple(fields,c_rd,c_in,publishedShares,share);
    }
    
    // TimedTuple
    public static final DepTuple createTimedTuple(long expirationTime, Object... fields) {
    	return new DepTuple(fields, null, null, expirationTime);
    }
    
    
    /**************************************************
	 *					 ACCESSORS					  *
	 **************************************************/
    
    public final Object[] getFields() { return fields; }
    public final PublishedShares getPublishedShares() { return publishedShares; }
    public final Share getShare() { return share; }
    public int[] getC_rd() { return c_rd; }
    public int[] getC_in() { return c_in; }
    public long getExpirationTime() { return expirationTime; }
    public int getN_Matches() { return n_matches; }
    
    public void setShare(Share share) { this.share = share; }
    public void setC_rd(int[] c_rd) { this.c_rd = c_rd; };
    public void setC_in(int[] c_in) { this.c_in = c_in; };
    public void setExpirationTime(long expirationTime) { this.expirationTime = expirationTime; }
    public void setN_Matches(int n_matches) { this.n_matches = n_matches; }
    public void setFields(Object[] fields) { this.fields = fields; }
    
    /**************************************************
	 *					  OTHERS					  *
	 **************************************************/
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	c_rd = (int[])in.readObject();
    	c_in = (int[])in.readObject();
    	publishedShares = (PublishedShares)in.readObject();
    	share = (Share)in.readObject();
 
    	int tam = in.readInt();
    	if(tam < 0){
    		fields = null;
    	}else{
    		fields = new Object[tam];
    		for(int i = 0; i < tam; i++){
    			fields[i] = in.readObject();
    		}
    	}
    	
    	expirationTime = in.readLong();
    	n_matches = in.readInt();
    }
    
    public void writeExternal(ObjectOutput out) throws IOException{
        out.writeObject(c_rd);
        out.writeObject(c_in);
        out.writeObject(publishedShares);
        out.writeObject(share);
        
        if(fields != null){
            //out.writeInt(1);
            int tam = fields.length;
            out.writeInt(tam);
            for(int i = 0; i < tam; i++){
                out.writeObject(fields[i]);
            }
        }else{
            out.writeInt(-1);
        }
        
        out.writeLong(expirationTime);
        out.writeInt(n_matches);
    }
    
    
    /**************************************************
	 *				  Business Methods				  *
	 **************************************************/
    
    public void extractShare(int id, BigInteger secretKey, PublicInfo info, BigInteger[] publicKeys) {
        if(publishedShares == null) {
            System.err.println("Not a confidential tuple!");
        } else {
            try {
                this.share = publishedShares.getShare(id,secretKey,info,publicKeys);
            } catch (InvalidVSSScheme ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public boolean canRd(int id) {
        if(c_rd == null) {
        	return true;
        }
        
        for(int e : c_rd) {
            if(e == id){
                return true;
            }
        }
        return false;
    }
    
    public boolean canIn(int id) {
        if(c_in == null) {
            return true;
        }
        
        for(int e : c_in) {
            if(e == id){
                return true;
            }
        }
        return false;
    }
    
    public boolean equals(Object object) {
        if(!(object instanceof DepTuple)) return false;
        DepTuple other = (DepTuple) object;
        if(publishedShares == null) return (other.publishedShares == null);
        if(other.publishedShares == null) return false;
        if(!publishedShares.equals(other.publishedShares)) return false; // <--- Does the PublishedShares class implement equals()? 
        // Shouldn't the 'share' attribute also be compared?
        // Shouldn't the 'expiration time' attribute also be compared?
        // Shouldn't the 'n_matches' attribute also be compared?
        if(!Arrays.equals(c_rd, other.getC_rd())) return false;
        if(!Arrays.equals(c_in, other.getC_in())) return false;
        return Arrays.equals(fields, other.fields);
    }
    
    
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("[<");
        if(fields != null && fields.length > 0){
        	buff.append(fields[0]);
            for(int i=1; i<fields.length; i++) {
                buff.append(",").append(fields[i]);
            }
        }
//        buff.append(">");
        buff.append(">,C_rd=(");
        if(c_rd != null) {
            buff.append(c_rd[0]);
            for(int i=1; i<c_rd.length; i++) {
                buff.append(",").append(c_rd[i]);
            }
        } else {
            buff.append("all");
        }
        buff.append("),C_in=(");
        if(c_in != null) {
            buff.append(c_in[0]);
            for(int i=1; i<c_in.length; i++) {
                buff.append(",").append(c_in[i]);
            }
        } else {
            buff.append("all");
        }
        buff.append("),confidential=").append(publishedShares != null).append("]");
        
        return buff.toString();
    }
    
    public String toStringTuple(){
    	StringBuffer buff = new StringBuffer();
        buff.append("[<");
        if(fields != null && fields.length > 0){
        	buff.append(fields[0]);
            for(int i=1; i<fields.length; i++) {
                buff.append(",").append(fields[i]);
            }
        }
//        buff.append(">");
        buff.append(">");
		return buff.toString();
    }

    public boolean matches(DepTuple template) {
    	int n = fields.length;
    	if(n != template.fields.length) return false;
    	for(int i = 0; i < n; i++) {
    		if(WILDCARD.equals(template.fields[i])) continue;
    		if(!fields[i].equals(template.fields[i])) return false;
    	}
    	return true;
    }

    public boolean isExpired(long time) {
    	return ((expirationTime != 0) && (time != 0) && (expirationTime < time));
    }

    
	// #####################################
	// # SERIALIZATION AND DESERIALIZATION #
	// #####################################

    private byte[] buffer;
    

    public void modified() {
    	buffer = null;
    }
    
	public byte[] serialize() {
		// Return serialization buffer if the tuple has already been serialized 
		if(buffer != null) return buffer;
		
		// Serialize tuple
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			if(fields == null) {
				oos.writeInt(-1);
			} else {
				oos.writeInt(fields.length);
				for(Object field: fields) {
					if(field instanceof Integer) {
						oos.write(1);
						oos.writeInt((Integer) field);
					} else if(field instanceof Long) {
						oos.write(2);
						oos.writeLong((Long) field);
					} else if(field instanceof Short) {
						oos.write(3);
						oos.writeShort((Short) field);
					} else if(field instanceof Boolean) {
						oos.write(4);
						oos.writeBoolean((Boolean) field);
					} else if(field instanceof Byte) {
						oos.write(5);
						oos.writeByte((Byte) field);
					} else if(field instanceof Character) {
						oos.write(6);
						oos.writeChar((Character) field);
					} else if(field instanceof Float) {
						oos.write(7);
						oos.writeFloat((Float) field);
					} else if(field instanceof Double) {
						oos.write(8);
						oos.writeDouble((Double) field);
					/* Add more special cases if needed. */
					} else {
						oos.write(0);
						oos.writeObject(field);
					}
				}
			}
			oos.writeObject(c_rd);
			oos.writeObject(c_in);
			oos.writeObject(publishedShares);
			oos.writeObject(share);
			oos.writeLong(expirationTime);
			oos.writeInt(n_matches);
			oos.close();
			buffer = baos.toByteArray();
		} catch(Exception ioe) {
			ioe.printStackTrace();
		}
		return buffer;
	}
	
	public DepTuple(byte[] buffer) throws Exception {
		this.buffer = buffer;
		ByteArrayInputStream in = new ByteArrayInputStream(buffer);
		ObjectInputStream ois = new ObjectInputStream(in);
		int nrOfFields = ois.readInt();
		if(nrOfFields >= 0) {
			this.fields = new Object[nrOfFields];
			for(int i = 0; i < nrOfFields; i++) {
				switch(ois.read()) {
				case 0:
					fields[i] = ois.readObject();
					break;
				case 1:
					fields[i] = ois.readInt();
					break;
				case 2:
					fields[i] = ois.readLong();
					break;
				case 3:
					fields[i] = ois.readShort();
					break;
				case 4:
					fields[i] = ois.readBoolean();
					break;
				case 5:
					fields[i] = ois.readByte();
					break;
				case 6:
					fields[i] = ois.readChar();
					break;
				case 7:
					fields[i] = ois.readFloat();
					break;
				case 8:
					fields[i] = ois.readDouble();
					break;
				/* Add more special cases if needed. */
				default:
					System.err.println("Error while deserializing tuple field");
				}
			}
		}
    	this.c_rd = (int[]) ois.readObject();
    	this.c_in = (int[]) ois.readObject();
    	this.publishedShares = (PublishedShares) ois.readObject();
    	this.share = (Share) ois.readObject();
    	this.expirationTime = ois.readLong();
    	this.n_matches = ois.readInt();
		ois.close();
	}
	
}
