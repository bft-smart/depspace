package depspace.confidentiality;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;

import pvss.InvalidVSSScheme;
import pvss.PVSSEngine;
import pvss.PublicInfo;
import pvss.PublishedShares;
import pvss.Share;
import depspace.general.DepTuple;

/**
 * ConfidentialityScheme. 
 *
 * @author		Alysson Bessani
 * @author2		Eduardo Alchieri
 * @author3		Rui Posse (ruiposse@gmail.com)
 * @version		DepSpace 2.0
 * @date		
 */
public class ConfidentialityScheme {
	
    public static final String PRIVATE = "PR";
    //public static final String DELIM = "|";
    
    private PVSSEngine engine;
    private PublicInfo info;
    private BigInteger[] publicKeys;
    
    private int id;
    private BigInteger secretKey;
    
    
    /**
     * Creates a new instance of the Confidentiality Scheme. This constructor must
     * be called at client side.
     */
    public ConfidentialityScheme(PublicInfo publicInfo, BigInteger[] publicKeys) throws InvalidVSSScheme {
        this.engine = PVSSEngine.getInstance(publicInfo);
        this.info = publicInfo;
        this.publicKeys = publicKeys;
    }
    
    /** Creates a new instance of the Confidentiality Scheme. This constructor must
     * be called at server side.
     */
    public ConfidentialityScheme(PublicInfo publicInfo, BigInteger[] publicKeys,
            int id, BigInteger secretKey) throws InvalidVSSScheme {
        this(publicInfo,publicKeys);
        this.id = id;
        this.secretKey = secretKey;
    }
    
    
    public PublicInfo getPublicInfo() { return info; }
    public BigInteger[] getPublicKeys() { return publicKeys; }
    public BigInteger getPublicKey(int index) { return publicKeys[index]; }
    
    public DepTuple mask(ProtectionVector protectionVector, DepTuple tuple) throws InvalidVSSScheme {
    	
        Object[] fingerprint = fingerprint(protectionVector,tuple.getFields());
        
        PublishedShares shares =
                engine.generalPublishShares(tupleToBytes(tuple.getFields()), publicKeys, 1);
        
        return DepTuple.internalCreateConfidentialTuple(tuple.getC_rd(),
                tuple.getC_in(),fingerprint,shares);
    }
    
    public DepTuple unmask(DepTuple tuple) throws InvalidVSSScheme {
//        DepTuple unmaskedTuple = DepTuple.internalCreateConfidentialTuple(
//          tuple.getC_rd(),tuple.getC_in(),tuple.getFields(), tuple.getPublishedShares());
        
        tuple.extractShare(id,secretKey,info,publicKeys);
        
        return tuple;
    }
    
    @SuppressWarnings("static-access")
	public boolean verifyTuple(ProtectionVector protectionVector,
            Object[] fingerprint,
            Object[] fields) throws InvalidVSSScheme {
        
        if((fingerprint.length != fields.length) ||
                (protectionVector.getLength() != fields.length)) {
            return false;
        }
        
        for(int i=0; i<protectionVector.getLength(); i++) {
           // if(!fingerprint[i].equals(WILDCARD)){
                switch(protectionVector.getType(i)){
                    case ProtectionVector.PU:{
                        if(!fingerprint[i].equals(fields[i])){
                            return false;
                        }
                    }break;
                    case ProtectionVector.CO:{
                        if(!fingerprint[i].equals(engine.hash(engine.getPublicInfo(),
                                fields[i].toString().getBytes()).toString()))
                            return false;
                    }break;
                    case ProtectionVector.PR:{
                        if(!PRIVATE.equals(fingerprint[i]))
                            return false;
                    }break;
                    default:{
                        throw new RuntimeException("Invalid field type specification");
                    }
                }
                // }
        }

        return true;
    }

    public Object[] extractTuple(Share[] shares) throws InvalidVSSScheme {
    	byte[] tupleBytes = engine.generalCombineShares(shares);
    	try {
    		return (Object[])new ObjectInputStream(
    				new ByteArrayInputStream(tupleBytes)).readObject();
    	} catch(Exception e) {
    		throw new RuntimeException("cannot read tuple fields: "+e);
    	}
    }

    @SuppressWarnings("static-access")
	public Object[] fingerprint(ProtectionVector protectionVector, Object[] fields)
    throws InvalidVSSScheme {

    	if(protectionVector.getLength() != fields.length) {
    		throw new RuntimeException("Invalid field type specification");
    	}

        Object[] fingerprint = new Object[fields.length];
        
        for(int i=0; i < protectionVector.getLength(); i++) {
            if(DepTuple.WILDCARD.equals(fields[i])){
                fingerprint[i] = DepTuple.WILDCARD;
            }else{
                switch(protectionVector.getType(i)){
                    case ProtectionVector.PU:{
                        fingerprint[i] = fields[i];
                    }break;
                    case ProtectionVector.CO:{
                        fingerprint[i] = engine.hash(engine.getPublicInfo(),
                                fields[i].toString().getBytes()).toString();
                    }break;
                    case ProtectionVector.PR:{
                        fingerprint[i] = PRIVATE;
                    }break;
                    default:{
                        throw new RuntimeException("Invalid field type specification");
                    }
                }
            }
        }
        
        return fingerprint;
    }
    
    private byte[] tupleToBytes(Object[] fields) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        
        try{
            new ObjectOutputStream(bos).writeObject(fields);
            
            return bos.toByteArray();
        }catch(Exception e){
            throw new RuntimeException("cannot write tuple fields: "+e);
        }
    }
    
}
