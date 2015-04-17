package depspace.client;

import java.util.Properties;

import depspace.general.Context;
import depspace.general.DepSpace;
import depspace.general.DepSpaceException;
import depspace.general.DepTuple;

/**
 * General interface for a DepSpace client.
 *
 * @author		Alysson Bessani
 * @author2		Eduardo Alchieri
 * @author3		Rui Posse (ruiposse@gmail.com)
 * @version		DepSpace 2.0
 * @date		
 */
public interface DepSpaceClientLayer extends DepSpace {
	
	// Creates a new tuple space
    public void createSpace(Properties prop) throws DepSpaceException;
    
    // Deletes an existing tuple space
    public void deleteSpace(String name) throws DepSpaceException;
    
	// TODO comment
    public DepTuple signedRD(DepTuple template, Context ctx) throws DepSpaceException;
    
	// TODO comment
    public void clean(DepTuple proof, Context ctx) throws DepSpaceException;
    
}