package depspace.policyenforcement;

import depspace.general.Context;
import depspace.general.DepTuple;

/**
 * Base class for all tuple space policies.
 *
 * @author		Alysson Bessani
 * @author2		Eduardo Alchieri
 * @author3		Rui Posse (ruiposse@gmail.com)
 * @version		DepSpace 2.0
 * @date		
 */
public abstract class PolicyEnforcer {
    
	// The layer
    private PolicyEnforcementLayer layer;
    
    
    /**Sets the policy enforcer layer*/
    public void setLayer(PolicyEnforcementLayer layer){
        this.layer = layer;
    }
    
    
    /**Executed in OUT operations to define the access permission*/
    public boolean canExecuteOut(int invoker, DepTuple tuple, Context ctx){
        return false;
    }
    
    
    /**Executed in RENEW operations to define the access permission*/
    public boolean canExecuteRenew(int invoker, DepTuple template, Context ctx){
    	return false;
    }
    
    /**Executed in READ operations (rdp or rd) to define the access permission*/
    public boolean canExecuteRdp(int invoker, DepTuple template, Context ctx){
        return false;
    }
    
    /**Executed in IN operations (in or inp) to define the access permission*/
    public boolean canExecuteInp(int invoker, DepTuple template, Context ctx){
        return false;
    }
    
    /**Executed in CAS operations to define the access permission*/
    public boolean canExecuteCas(int invoker, DepTuple template, DepTuple tuple, Context ctx){
        return false;
    }

    /**Executed in REPLACE operations to define the access permission*/
    public boolean canExecuteReplace(int invokerId, DepTuple template,
    		DepTuple tuple, Context ctx) {
    	return false;
    }
    
    /**Executed in RDALL operations to define the access permission*/
	public boolean canExecuteRdAll(int invokerId, DepTuple template, Context ctx) {
		return false;
	}
	
	/**Executed in INALL operations to define the access permission*/
	public boolean canExecuteInAll(int invokerId, DepTuple template, Context ctx) {
		return false;
	}
    
    
    public DepTuple rdp(DepTuple template, Context ctx){
        try{
            return layer.getUpperLayer().rdp(template,ctx);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
