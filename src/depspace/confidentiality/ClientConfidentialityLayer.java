package depspace.confidentiality;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import pvss.InvalidVSSScheme;
import depspace.client.DepSpaceClientLayer;
import depspace.general.Context;
import depspace.general.DepSpaceException;
import depspace.general.DepTuple;

/**
 * Confidentiality layer for DepSpace clients. 
 *
 * @author		Alysson Bessani
 * @author2		Eduardo Alchieri
 * @author3		Rui Posse (ruiposse@gmail.com)
 * @version		DepSpace 2.0
 * @date		
 */
public class ClientConfidentialityLayer implements DepSpaceClientLayer {
	
	// The bottom layer (normally Replication layer)
    private DepSpaceClientLayer bottomLayer;
    
    // TODO comment
    private ConfidentialityScheme scheme;
    
    
    /**
     * Creates a new instance of ClientConfidentialityLayer.
     * @param bottomLayer The bottom layer (normally Replication layer)
     * @param scheme TODO comment
     */
    public ClientConfidentialityLayer(DepSpaceClientLayer bottomLayer, ConfidentialityScheme scheme) {
        this.bottomLayer = bottomLayer;
        this.scheme = scheme;
    }
    
    
    /**************************************************
	 *		 DEPSPACE INTERFACE IMPLEMENTATION		  *
	 **************************************************/
    
    public void out(DepTuple tuple, Context ctx) throws DepSpaceException {
        try {
            bottomLayer.out(scheme.mask(ctx.protectionVectors[0],tuple),ctx);
        } catch(InvalidVSSScheme e) {
            throw new DepSpaceException("Cannot mask tuple: " + e.getMessage());
        }
    }
    
    public DepTuple renew(DepTuple template, Context ctx) throws DepSpaceException {
    	try {
            return bottomLayer.renew(scheme.mask(ctx.protectionVectors[0],template),ctx);
        } catch(InvalidVSSScheme e) {
            throw new DepSpaceException("Cannot mask tuple: " + e.getMessage());
        }
    }
    
    public DepTuple rdp(DepTuple template, Context ctx) throws DepSpaceException {
        DepTuple maskedTemplate = generateFingerprint(ctx.protectionVectors[0],
                template), r = null;
        do{
            r = bottomLayer.rdp(maskedTemplate,ctx);
            if(!valid(r)) {
                r = this.signedRD(maskedTemplate,ctx);
                if(valid(r)){
                   return r;
                }
                this.clean(r, ctx);
            }
        }while(!valid(r));
        return r;
    }

    public DepTuple inp(DepTuple template, Context ctx) throws DepSpaceException {
        DepTuple maskedTemplate = generateFingerprint(ctx.protectionVectors[0],
                template), r = null;
        do{
            r = bottomLayer.inp(maskedTemplate,ctx);
        } while (!valid(r));

        return r;
    }
    
    public DepTuple rd(DepTuple template, Context ctx) throws DepSpaceException {
        DepTuple maskedTemplate = generateFingerprint(ctx.protectionVectors[0],
        		template), r = null;
        do{
            r = bottomLayer.rd(maskedTemplate, ctx);
            if(!valid(r)) {
                r = this.signedRD(maskedTemplate, ctx);
                if(valid(r)){
                   return r;
                }
                this.clean(r, ctx);
            }
        } while(!valid(r));

        return r;
    }
    
    public DepTuple in(DepTuple template, Context ctx) throws DepSpaceException {
        DepTuple maskedTemplate = generateFingerprint(ctx.protectionVectors[0],
                template), r = null;
        do{
            r = bottomLayer.in(maskedTemplate,ctx);
        } while (! valid(r));

        return r;
    }
    
    public DepTuple cas(DepTuple template, DepTuple tuple, Context ctx) throws DepSpaceException {
        DepTuple maskedTemplate = generateFingerprint(ctx.protectionVectors[0],
                template);
        DepTuple maskedTuple = null, r = null;
        try {
            maskedTuple = scheme.mask(ctx.protectionVectors[1],tuple);
        } catch(InvalidVSSScheme e) {
            throw new DepSpaceException("Cannot mask tuple: "+e.getMessage());
        }
        do{
            r = bottomLayer.cas(maskedTemplate,maskedTuple, ctx);
            if(!valid(r)) {
                r = this.signedRD(maskedTemplate,ctx);
                if(valid(r)){
                   return r;
                }
                this.clean(r, ctx);
            }
        } while(!valid(r));

        return r;
    }

	public Collection<DepTuple> rdAll(DepTuple template, Context ctx) throws DepSpaceException {
        return bottomLayer.rdAll(template, ctx);
	}

	public Collection<DepTuple> inAll(DepTuple template, Context ctx) throws DepSpaceException {
		return bottomLayer.inAll(template, ctx);
	}
    
    
    /**************************************************
	 *	  DEPSPACE CLIENT INTERFACE IMPLEMENTATION	  *
	 **************************************************/
    
    public void createSpace(Properties prop) throws DepSpaceException {
        this.bottomLayer.createSpace(prop);
    }
    
    public void deleteSpace(String name) throws DepSpaceException {
        this.bottomLayer.deleteSpace(name);
    }
    
    public DepTuple signedRD(DepTuple template, Context ctx) throws DepSpaceException{
          return this.bottomLayer.signedRD(template,ctx);
    }

    public void clean(DepTuple proof, Context ctx)throws DepSpaceException{
            this.bottomLayer.clean(proof,ctx);
    }
    
    
	/**************************************************
	 *					 AUXILIAR					  *
	 **************************************************/
    
    private final DepTuple generateFingerprint(final ProtectionVector protectionVector,
            final DepTuple template) throws DepSpaceException {
        try{
            return DepTuple.createTuple(scheme.fingerprint(protectionVector,
                    template.getFields()));
        }catch(InvalidVSSScheme e) {
            throw new DepSpaceException("Cannot calculate fingerprint: "+
                    e.getMessage());
        }
    }

    private final boolean valid(DepTuple tuple) {
        if(tuple instanceof InvalidDepTuple) {
            return false;
        }
        return true;
    }


	@Override
	public DepTuple replace(DepTuple template, DepTuple tuple, Context ctx)
			throws DepSpaceException {
		DepTuple maskedTemplate = generateFingerprint(ctx.protectionVectors[0],
                template);
        DepTuple maskedTuple = null, r = null;
        try {
            maskedTuple = scheme.mask(ctx.protectionVectors[1],tuple);
        } catch(InvalidVSSScheme e) {
            throw new DepSpaceException("Cannot mask tuple: "+e.getMessage());
        }
        do{
            r = bottomLayer.replace(maskedTemplate,maskedTuple, ctx);
            if(!valid(r)) {
                r = this.signedRD(maskedTemplate,ctx);
                if(valid(r)){
                   return r;
                }
                this.clean(r, ctx);
            }
        } while(!valid(r));

        return r;
	}


	@Override
	public void outAll(List<DepTuple> tuplesBag, Context ctx) throws DepSpaceException {
		
	}

	@Override
	public Collection<DepTuple> rdAll() {
		return null;
	}

}