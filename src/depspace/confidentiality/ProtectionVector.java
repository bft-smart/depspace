package depspace.confidentiality;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * ProtectionVector. 
 *
 * @author		Alysson Bessani
 * @author2		Eduardo Alchieri
 * @author3		Rui Posse (ruiposse@gmail.com)
 * @version		DepSpace 2.0
 * @date		
 */
public class ProtectionVector implements Externalizable{
	
	// TODO comment
    public transient final static int PU = 0;
    public transient final static int CO = 1;
    public transient final static int PR = 2;
    
    // TODO comment
    private int[] pTypes;
    
    // TODO comment
    public ProtectionVector(){}
    
    
    /**
     * Creates a new instance of ProtectionVector.
     * @param types TODO comment
     */
    public ProtectionVector(int[] types) {
        this.pTypes = types;
    }
    
    
    /**************************************************
	 *					 ACCESSORS					  *
	 **************************************************/
    public final int getLength() { return pTypes.length; }
    public final int getType(int i) { return pTypes[i]; }
    
    
    /**************************************************
     *				  Externalizable				  *
     **************************************************/
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	int tam = in.readInt();
    	pTypes = new int[tam];
    	for(int i = 0; i < tam; i++){
    		pTypes[i] = in.readInt();
    	}
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
    	int tam = pTypes.length;
    	out.writeInt(tam);
    	for(int i = 0; i < tam; i++){
    		out.writeInt(pTypes[i]);
    	}
    }

}