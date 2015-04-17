package depspace.confidentiality;

import java.security.Signature;
import java.util.Collection;
import java.util.List;

import pvss.InvalidVSSScheme;
import depspace.general.Context;
import depspace.general.DepSpaceException;
import depspace.general.DepTuple;
import depspace.server.DepSpaceServerLayer;

/**
 * Confidentiality layer for DepSpace servers. 
 *
 * @author		Alysson Bessani
 * @author2		Eduardo Alchieri
 * @author3		Rui Posse (ruiposse@gmail.com)
 * @version		DepSpace 2.0
 * @date		
 */
public class ServerConfidentialityLayer extends DepSpaceServerLayer {
    
    // TODO comment
    private final ConfidentialityScheme scheme;
    
    // TODO comment
    private Signature engine = null;
    
    
    /**
     * Creates a new instance of ServerConfidentialityLayer.
     * @param upperLayer The upper layer (Policy Enforcement or Tuple Space layer)
     * @param scheme TODO comment
     * @param conf The configuration
     */
    public ServerConfidentialityLayer(DepSpaceServerLayer upperLayer, ConfidentialityScheme scheme) {
    	super(upperLayer);
        this.scheme = scheme;
        try {
            engine = Signature.getInstance("SHA1withRSA");
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.err.println("ERROR: confidentiality layer is not fully implemented");
    }
    
    
	/**************************************************
	 *		 DEPSPACE INTERFACE IMPLEMENTATION		  *
	 **************************************************/
    
    public void out(DepTuple tuple, Context ctx) throws DepSpaceException {
        upperLayer.out(tuple,ctx);
    }
    
    public DepTuple renew(DepTuple template, Context ctx) throws DepSpaceException {
        DepTuple dt = upperLayer.renew(template,ctx);
        if(dt != null && dt.getShare() == null){
            try{
                dt = scheme.unmask(dt);
            } catch(InvalidVSSScheme e) {
                throw new DepSpaceException("Cannot unmask tuple: "+e.getMessage());
            }
        }
    	return dt;
    }
    
    public DepTuple rdp(DepTuple template, Context ctx) throws DepSpaceException {
        DepTuple dt = upperLayer.rdp(template,ctx);
        if(dt != null && dt.getShare() == null){
            try{
                dt = scheme.unmask(dt);
            } catch(InvalidVSSScheme e) {
                throw new DepSpaceException("Cannot unmask tuple: "+e.getMessage());
            }
        }
        return dt;
    }
    
    public DepTuple inp(DepTuple template, Context ctx) throws DepSpaceException {
        DepTuple dt = upperLayer.inp(template,ctx);
        if(dt != null && dt.getShare() == null){
            try{
                dt = scheme.unmask(dt);
            } catch(InvalidVSSScheme e) {
                throw new DepSpaceException("Cannot unmask tuple: "+e.getMessage());
            }
        }
        return dt;
    }
    
    // Must never be called!!!
    public DepTuple rd(DepTuple template, Context ctx) throws DepSpaceException {
        throw new UnsupportedOperationException("Not to be implemented");
    }
    
    // Must never be called!!!
    public DepTuple in(DepTuple template, Context ctx) throws DepSpaceException {
        throw new UnsupportedOperationException("Not to be implemented");
    }
    
    public DepTuple cas(DepTuple template, DepTuple tuple, Context ctx) throws DepSpaceException {
        DepTuple dt = upperLayer.cas(template,tuple,ctx);
        if(dt != null && dt.getShare() == null){
            try{
                dt = scheme.unmask(dt);
            } catch(InvalidVSSScheme e) {
                throw new DepSpaceException("Cannot unmask tuple: "+e.getMessage());
            }
        }
        return dt;
    }
    
    @Override
	public DepTuple replace(DepTuple template, DepTuple tuple, Context ctx)
			throws DepSpaceException {
        DepTuple dt = upperLayer.replace(template,tuple,ctx);
        if(dt != null && dt.getShare() == null){
            try{
                dt = scheme.unmask(dt);
            } catch(InvalidVSSScheme e) {
                throw new DepSpaceException("Cannot unmask tuple: "+e.getMessage());
            }
        }
        return dt;
	}

	public Collection<DepTuple> rdAll(DepTuple template, Context ctx) throws DepSpaceException {
		return upperLayer.rdAll(template, ctx);

	}

	public Collection<DepTuple> inAll(DepTuple template, Context ctx) throws DepSpaceException {
		return upperLayer.inAll(template, ctx);
	}
	
    
    /**************************************************
	 *	  DEPSPACE SERVER INTERFACE IMPLEMENTATION	  *
	 **************************************************/
    
    public Object signedRD(DepTuple template, Context ctx) throws DepSpaceException{
//        DepTuple result = upperLayer.rdp(template,ctx);
//        if(result != null){
//            try{
//                return new SignedObject((java.io.Serializable)result,
//                        conf.getRSAPrivateKey(),engine);
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//        }
        return null;
    }
    
    public void clean(DepTuple proof, Context ctx)throws DepSpaceException{
//        if(proof instanceof InvalidDepTuple){
//            InvalidDepTuple inv = (InvalidDepTuple)proof;
//            DepSpaceReply[] msgs = inv.getResponses();
//            int [] from = inv.getFrom();
//            SignedObject so = null;
//            if(from.length!=msgs.length) return;
//            
//            for(int i = 0; i < msgs.length;i++){
//                if(msgs[i] != null && msgs[i].arg instanceof SignedObject){
//                    so = (SignedObject)msgs[i].arg;
//                    try{
//                        //if(so.verify(conf.getRSAPublicKeys()[msgs[i].getFrom()],engine)){
//                        if(so.verify(conf.getRSAPublicKey(from[i]),engine)){
//                        	msgs[i].setArg(so.getObject());
//                        }else{
//                        	msgs[i] = null;
//                        }
//                    }catch(Exception e){
//                    	e.printStackTrace();
//                    }
//                }else{
//                	msgs[i] = null;
//                }
//            }
//            DepTuple rem = this.checkRemove(msgs,inv.getProtectionVector());
//            if(rem != null){
//            	//System.out.println("Vai remover a tupla "+rem);
//            	this.upperLayer.clean(rem,ctx);
//            }
//        }
    }


    /**************************************************
     *					  OTHER					  	  *
     **************************************************/

//    private DepTuple checkRemove(DepSpaceReply[] msgs, ProtectionVector protectionVector) {
//    	if(!this.checkMessages(msgs)){
//    		return null;
//    	}
//    	List<DepTuple> candidateShares = extractCandidateResponse(msgs);
//    	List<DepTuple> validCandidateShares = new LinkedList<DepTuple>();
//    	for(ListIterator<DepTuple> li = candidateShares.listIterator(); li.hasNext(); ) {
//    		DepTuple tuple = li.next();
//    		Share share = tuple.getShare();
//    		try {
//    			if(share != null && share.verify(scheme.getPublicInfo(),
//    					scheme.getPublicKey(share.getIndex()))) {
//    				validCandidateShares.add(tuple);
//    			}
//    		}catch(InvalidVSSScheme e) {
//    			e.printStackTrace();
//    		}
//    	}
//    	if(validCandidateShares.size() > conf.getF()) {
//    		DepTuple tuple = validCandidateShares.get(0);
//    		Share[] shares = new Share[conf.getN()];
//    		for(ListIterator<DepTuple> li = validCandidateShares.listIterator(); li.hasNext(); ) {
//    			Share share = li.next().getShare();
//    			shares[share.getIndex()] = share;
//    		}
//    		try {
//    			Object[] tupleContents = scheme.extractTuple(shares);
//    			if(scheme.verifyTuple(protectionVector,tuple.getFields(),tupleContents)) {
//    				return null;
//    			}else{
//    				return tuple;
//    			}
//    		}catch(InvalidVSSScheme e) {
//    			return null;
//    		}
//    	}
//    	return null;
//    }
//
//
//    private final List<DepTuple> extractCandidateResponse(DepSpaceReply[] msgs) {
//    	List<DepTuple> candidateShares = new LinkedList<DepTuple>();
//    	for(int i=0; i<msgs.length; i++) {
//    		if(msgs[i] != null && msgs[i].getArg() instanceof DepTuple) {
//    			DepTuple tuple1 = (DepTuple)msgs[i].getArg();
//    			candidateShares.add(tuple1);
//    			for(int j=i+1; j<msgs.length; j++) {
//    				if(msgs[j] != null && msgs[j].getArg() instanceof DepTuple) {
//    					DepTuple tuple2 = (DepTuple)msgs[j].getArg();
//    					if(theSame(tuple1,tuple2)) {
//    						candidateShares.add(tuple2);
//    					}
//    				}
//    			}
//    			if(candidateShares.size() > conf.getF()) {
//    				return candidateShares;
//    			}else {
//    				candidateShares.clear();
//    			}
//    		}
//    	}
//    	candidateShares.clear();
//    	return candidateShares;
//    }
//
//    private final boolean theSame(DepTuple tuple1, DepTuple tuple2) {
//    	return tuple1.equalFields(tuple2) && //same fingerprint
//    	(tuple1.getPublishedShares().hashCode() ==
//    		tuple2.getPublishedShares().hashCode()); //same published shares
//    }
//
//    private boolean checkMessages(DepSpaceReply[] msgs){
//    	int c = 0;
//    	for(int i = 0; i < msgs.length; i++){
//    		if(msgs[i] != null && msgs[i].arg != null){
//    			c++;
//    		}else{
//    			c = 0;
//    		}
//    		if( c > conf.getF()){
//    			return true;
//    		}
//    	}
//    	return false;
//    }


    @Override
    public void outAll(List<DepTuple> tuplesBag, Context ctx) throws DepSpaceException {
    	// TODO Auto-generated method stub
    	
    }

	@Override
	public Collection<DepTuple> rdAll() {
		return null;
	}

}
