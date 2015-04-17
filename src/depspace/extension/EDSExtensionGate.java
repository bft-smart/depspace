package depspace.extension;

import java.util.Collection;
import java.util.List;

import depspace.general.DepSpaceException;
import depspace.general.DepTuple;


public interface EDSExtensionGate {

	// ################
	// # MAIN METHODS #
	// ################

    public void out(DepTuple tuple) throws DepSpaceException;
    public void outAll(List<DepTuple> tuplesBag) throws DepSpaceException;
    public DepTuple renew(DepTuple template) throws DepSpaceException;
    public DepTuple rdp(DepTuple template) throws DepSpaceException;
    public DepTuple inp(DepTuple template) throws DepSpaceException;
    public DepTuple rd(DepTuple template) throws DepSpaceException;
    public DepTuple in(DepTuple template) throws DepSpaceException;
    public DepTuple cas(DepTuple template, DepTuple tuple) throws DepSpaceException;
    public DepTuple replace(DepTuple template, DepTuple tuple) throws DepSpaceException;
    public Collection<DepTuple> rdAll(DepTuple template) throws DepSpaceException;
    public Collection<DepTuple> rdAll() throws DepSpaceException;
    public Collection<DepTuple> inAll(DepTuple template) throws DepSpaceException;

    
	// #######################
	// # SAFE HELPER METHODS #
	// #######################

	public void error(String message) throws DepSpaceException;

}
