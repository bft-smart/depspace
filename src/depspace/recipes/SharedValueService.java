package depspace.recipes;


public interface SharedValueService {

	public void start() throws Exception;
	public void close() throws Exception;
	
	// Shared value
	public byte[] getValue();
	public void setValue(byte[] newValue) throws Exception;
	public boolean trySetValue(byte[] newValue) throws Exception;
	
	// Shared count
	public int getCount();
	public void setCount(int newCount) throws Exception;
	public boolean trySetCount(int newCount) throws Exception;
	
	// Shared counter
	public int getAndIncrement() throws Exception;
	public int incrementAndGet() throws Exception;
	
}
