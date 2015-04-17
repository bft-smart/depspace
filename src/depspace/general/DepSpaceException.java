package depspace.general;

/**
 * General exception to be thrown if abnormal tuple space conditions are reached.
 *
 * @author		Alysson Bessani
 * @author2		Eduardo Alchieri
 * @author3		Rui Posse (ruiposse@gmail.com)
 * @version		DepSpace 2.0
 * @date		
 */
public class DepSpaceException extends Exception {
	
	// Serializable
	private static final long serialVersionUID = 3568964777851391148L;
	
	
	/**
	 * Creates a new instance of DepSpaceException.
	 * @param message The message
	 */
    public DepSpaceException(String message) {
        super(message);
    }
    
    
    /**
     * Redefines equals. 
     */
    public boolean equals(Object o){
        if(o instanceof DepSpaceException){
            DepSpaceException ex = (DepSpaceException)o;
            if(this.getMessage().equals(ex.getMessage())){
                return true;
            }
        }
        return false;
    }
    
}
