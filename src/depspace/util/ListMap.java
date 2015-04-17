package depspace.util;

import java.util.Map;

import depspace.server.DepSpaceListImpl;

public class ListMap {

	private DepSpaceListImpl list;
	private Map<Object, ListMap> map;

	public ListMap(DepSpaceListImpl list, Map<Object, ListMap> map) {
		this.map = map;
		this.list = list;
	}
	
	public boolean isList(){
		return list!=null;
	}
	
	public boolean isMap(){
		return map != null;
	}
	
	/**
	 * 
	 * @return the list, or null if !isList();
	 */
	public DepSpaceListImpl getList(){
		return list;
	}
	
	public Map<Object, ListMap> getMap() {
		return map;
	}

}
