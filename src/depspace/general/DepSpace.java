package depspace.general;

import java.util.Collection;
import java.util.List;

/**
 * General interface for DepSpace and its layers.
 *
 * @author		Alysson Bessani
 * @author2		Eduardo Alchieri
 * @author3		Rui Posse (ruiposse@gmail.com)
 * @version		DepSpace 2.0
 * @date		
 */
public interface DepSpace {
	
	// Inserts DepTuple tuple in the space
    public void out(DepTuple tuple, Context ctx) throws DepSpaceException;
    
	// Inserts DepTuple tuple in the space
    public void outAll(List<DepTuple> tuplesBag, Context ctx) throws DepSpaceException;
    
    // Renews a tuple expirationTime that matches with template 
    public DepTuple renew(DepTuple template, Context ctx) throws DepSpaceException;
    
    // Reads a tuple that matches template from the space (returning true);
    // returns false if no tuple is found
    public DepTuple rdp(DepTuple template, Context ctx) throws DepSpaceException;
    
    // Reads and removes a tuple that matches template from the space
    // (returning true); returns false if no tuple is found
    public DepTuple inp(DepTuple template, Context ctx) throws DepSpaceException;
    
    // Reads a tuple that matches template from the space; stays blocked
    // until some matching tuple is found
    public DepTuple rd(DepTuple template, Context ctx) throws DepSpaceException;
    
    // Reads and removes a tuple that matches template from the space; stays
    // blocked until some matching tuple is found
    public DepTuple in(DepTuple template, Context ctx) throws DepSpaceException;
    
    // If there is no tuple that matches template on the space,
    // inserts tuple and returns true; otherwise returns false
    /**
     * If the returned value is null, then the tuple was inserted.
     */
    public DepTuple cas(DepTuple template, DepTuple tuple, Context ctx) throws DepSpaceException;
    
    // Replace the first tuple found in the Tuple Space that matches with the template,
    // returning true if some tuple was found, false otherwise. 
    public DepTuple replace(DepTuple template, DepTuple tuple, Context ctx) throws DepSpaceException;
    
    // Reads all tuples that match the template from the space;
    // If n_matches = 0, returns all matching tuples
    // Matching tuples are returned in a Collection
    public Collection<DepTuple> rdAll(DepTuple template, Context ctx) throws DepSpaceException;
    
	// Reads all tuples from the TS
    public Collection<DepTuple> rdAll() throws DepSpaceException;
    
    // Reads and removes all tuples that match the template from the space;
    // If n_matches = 0, returns all matching tuples
    // Matching tuples are returned in a Collection
    public Collection<DepTuple> inAll(DepTuple template, Context ctx) throws DepSpaceException;
    
}