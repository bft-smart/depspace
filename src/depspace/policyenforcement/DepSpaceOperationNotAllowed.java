package depspace.policyenforcement;

import depspace.general.DepSpaceException;
import depspace.general.DepSpaceOperation;
import depspace.general.DepTuple;

/**
 * Access denied exception.
 *
 * @author		Alysson Bessani
 * @author2		Eduardo Alchieri
 * @author3		Rui Posse (ruiposse@gmail.com)
 * @version		DepSpace 2.0
 * @date		
 */
public class DepSpaceOperationNotAllowed extends DepSpaceException {

    // Serializable
	private static final long serialVersionUID = 6877087799124281933L;
	
	
	/**
	 * Creates a new instance of DepSpaceOperationNotAllowed.
	 * @param operation TODO comment
	 * @param invoker TODO comment
	 * @param transId TODO comment
	 */
    public DepSpaceOperationNotAllowed(DepSpaceOperation operation, int invoker, int transId) {
        super("Operation not allowed: invoke("+invoker+" , "+operation+" : "+transId+")");
    }
    
    
    /**
     * Creates a new instance of DepSpaceOperationNotAllowed.
     * @param operation TODO comment
     * @param invoker TODO comment
     * @param args TODO comment
     */
    public DepSpaceOperationNotAllowed(DepSpaceOperation operation, int invoker, DepTuple... args) {
        super("Operation not allowed: invoke("+invoker+","+operation+"("+args[0]+
                ((args.length > 1)?","+args[1]:"")+"))");
    }
    
}