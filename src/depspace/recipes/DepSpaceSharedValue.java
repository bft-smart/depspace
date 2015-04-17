package depspace.recipes;

import java.util.Arrays;

import depspace.client.DepSpaceAccessor;
import depspace.client.DepSpaceAdmin;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceProperties;
import depspace.general.DepTuple;


public class DepSpaceSharedValue implements SharedValueService {

	protected final DepSpaceAdmin admin;
	protected final DepSpaceAccessor depSpace;
	
	
	public DepSpaceSharedValue(int id, String tsName, boolean createSpace) throws DepSpaceException {
		this.admin = new DepSpaceAdmin(id);
		this.depSpace = admin.createAccessor(DepSpaceProperties.createDefaultProperties(tsName), createSpace);
		this.value = null;
		this.count = -1;
	}
	
	
	@Override
    public void start() throws Exception {
		readValue();
		if(count < 0) setCount(0);
    }

	@Override
    public void close() throws Exception {
		admin.finalizeAccessor(depSpace);
    }

	private void readValue() throws DepSpaceException {
		DepTuple tuple = depSpace.rdp(DepTuple.createTuple(DepTuple.WILDCARD));
		if(tuple == null) return;
		Object result = tuple.getFields()[0];
		if(result instanceof byte[]) value = (byte[]) result;
		else count = (Integer) result;
	}
	

    // ###########################
    // # SHARED-VALUE OPERATIONS #
    // ###########################

	private byte[] value;
	

	@Override
	public byte[] getValue() {
		return Arrays.copyOf(value, value.length);
	}

	@Override
	public void setValue(byte[] newValue) throws Exception {
        depSpace.out(DepTuple.createTuple(newValue));
        value = Arrays.copyOf(newValue, newValue.length);
	}

	@Override
	public boolean trySetValue(byte[] newValue) throws Exception {
        DepTuple result = depSpace.cas(DepTuple.createTuple(value), DepTuple.createTuple(newValue));
        if(result != null) {
        	value = Arrays.copyOf(newValue, newValue.length);
        	return true;
        }
        readValue();
        return false;
	}

	
    // ###########################
    // # SHARED-COUNT OPERATIONS #
    // ###########################

	protected int count;
	

	@Override
    public int getCount() {
        return count;
    }
    
    @Override
    public void setCount(int newCount) throws Exception {
        depSpace.out(DepTuple.createTuple(newCount));
        count = newCount;
    }

    @Override
    public boolean trySetCount(int newCount) throws Exception {
        DepTuple result = depSpace.replace(DepTuple.createTuple(count), DepTuple.createTuple(newCount));
        if(result != null) {
        	count = newCount;
        	return true;
        }
    	readValue();
    	return false;
    }

    
    // #############################
    // # SHARED-COUNTER OPERATIONS #
    // #############################
    
    @Override
    public int getAndIncrement() throws Exception {
    	while(true) {
	    	int currentValue = getCount();
	    	boolean success = trySetCount(currentValue + 1);
	    	if(success) return currentValue;
    	}
    }
    
    @Override
    public int incrementAndGet() throws Exception {
    	return getAndIncrement() + 1;
    }
    
}
