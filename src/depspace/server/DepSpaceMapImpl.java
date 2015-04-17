package depspace.server;


import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import depspace.general.Context;
import depspace.general.DepSpace;
import depspace.general.DepSpaceException;
import depspace.general.DepTuple;
import depspace.util.ListMap;

/**
 * Tuple Space layer. This layer implements a Tuple Space with a combination of lists and hashMaps.
 * 
 * @author Ricardo Mendes (rmendes@lasige.di.fc.ul.pt)
 * @version DepSpace 2.1
 * @date
 */

public class DepSpaceMapImpl implements DepSpace {

	// If it is possible to renew an expired tuple
	// true - it is possible
	// false - it is not possible
	private boolean realTimeRenew;
	private DepSpaceListImpl listImpl;

	// the Depth (number of jumps until get the list of tuples)
	private int depth;

	private Map<Object, ListMap> tupleBag; 

	public DepSpaceMapImpl(boolean realTimeRenew, int depth) {
		System.out.println("nova instancia de HashMapImpl");
		this.realTimeRenew = realTimeRenew;
		this.depth = depth;
		listImpl = new DepSpaceListImpl(this.realTimeRenew);
		tupleBag = new TreeMap<Object, ListMap>();
	}

	@Override
	public void out(DepTuple tuple, Context ctx) {
		if(this.depth==0 || this.depth > tuple.getFields().length){
			listImpl.out(tuple, ctx);
			return;
		}

		//at this point, numFields(tuple) >= depth;

		Map<Object, ListMap> map = tupleBag;
		Object[] fields = tuple.getFields();
		for(int i=0; i<depth-1 ; i++){
			if(map.get(fields[i]) == null){
				map.put(fields[i], new ListMap(null, new TreeMap<Object, ListMap>()));
			}
			map = map.get(fields[i]).getMap();
		}

		if(map.get(fields[depth-1]) == null){
			map.put(fields[depth-1], new ListMap(new DepSpaceListImpl(realTimeRenew), null));
		}
		map.get(fields[depth-1]).getList().out(tuple, ctx);

	}

	@Override
	public void outAll(List<DepTuple> tuplesBag, Context ctx) {
		if(this.depth==0){
			listImpl.outAll(tuplesBag, ctx);
			return;
		}

		for(DepTuple tuple : tuplesBag)
			out(tuple, null);
	}

	@Override
	public DepTuple renew(DepTuple template, Context ctx)
			throws DepSpaceException {
		if(this.depth==0 || this.depth > template.getFields().length){
			return listImpl.renew(template, ctx);
		}
		DepTuple tup;
		Collection<DepSpaceListImpl> list = getListImpl(template);
		for(DepSpaceListImpl impl : list){
			tup = impl.renew(template, ctx);
			if(tup != null)
				return tup;
		}

		return null;

	}

	@Override
	public DepTuple rdp(DepTuple template, Context ctx)
			throws DepSpaceException {

		if(this.depth==0 || this.depth > template.getFields().length){
			return listImpl.rdp(template, ctx);
		}

		DepTuple tup;
		Collection<DepSpaceListImpl> list = getListImpl(template);
		for(DepSpaceListImpl impl : list){
			tup = impl.rdp(template, ctx);
			if(tup != null)
				return tup;
		}

		return null;

	}

	@Override
	public DepTuple inp(DepTuple template, Context ctx)
			throws DepSpaceException {

		if(this.depth==0 || this.depth > template.getFields().length){
			return listImpl.inp(template, ctx);
		}

		DepTuple tup;
		Collection<DepSpaceListImpl> list = getListImpl(template);
		for(DepSpaceListImpl impl : list){
			tup = impl.inp(template, ctx);
			if(tup != null){
				if(impl.rdAll().isEmpty()){
					Map<Object, ListMap> map = tupleBag;
					for(int i=0; i<depth ; i++){
						if(i==depth-1)
							map.remove(tup.getFields()[i]);
						else
							map=map.get(tup.getFields()[i]).getMap();
					}
				}

				return tup;
			}

		}

		return null;
	}

	@Override
	public DepTuple rd(DepTuple template, Context ctx) throws DepSpaceException {
		throw new UnsupportedOperationException("Not implemented at this level");
	}

	@Override
	public DepTuple in(DepTuple template, Context ctx) throws DepSpaceException {
		throw new UnsupportedOperationException("Not implemented at this level");
	}

	@Override
	public DepTuple cas(DepTuple template, DepTuple tuple, Context ctx)
			throws DepSpaceException {


		if(this.depth==0 || (this.depth > template.getFields().length && this.depth > tuple.getFields().length))
			return listImpl.cas(template, tuple, ctx);

		DepTuple tup = null;
		if(this.depth > template.getFields().length){
			tup = listImpl.rdp(template, ctx);
		}else{
			tup = rdp(template, ctx);
		}

		if(tup == null){
			out(tuple, ctx);
		}

		return tup;

	}

	@Override
	public DepTuple replace(DepTuple template, DepTuple tuple, Context ctx)
			throws DepSpaceException {

		if(this.depth==0 || (this.depth > template.getFields().length && this.depth > tuple.getFields().length)){
			return listImpl.replace(template, tuple, ctx);
		}

		if(!tuple.canIn(ctx.invokerID)){
			return null;
		}

		DepTuple tup = null;
		if(this.depth > template.getFields().length){
			tup = listImpl.rdp(template, ctx);
		}else{
			tup = rdp(template, ctx);
		}

		if(tup != null){
			inp(template, ctx);
			out(tuple, ctx);
		}

		return tup;

	}

	@Override
	public Collection<DepTuple> rdAll(DepTuple template, Context ctx)
			throws DepSpaceException {

		if(this.depth==0 || this.depth > template.getFields().length){
			return listImpl.rdAll(template, ctx);
		}

		Collection<DepSpaceListImpl> lists = getListImpl(template);
		Collection<DepTuple> res = new LinkedList<DepTuple>();
		for(DepSpaceListImpl impl : lists)
			res.addAll(impl.rdAll(template, ctx));

		return res;
	}

	@Override
	public Collection<DepTuple> rdAll() {

		if(this.depth==0){
			return listImpl.rdAll();
		}

		Collection<DepSpaceListImpl> lists = getAllLists(tupleBag, 0);
		Collection<DepTuple> res = new LinkedList<DepTuple>();
		for(DepSpaceListImpl impl : lists)
			res.addAll(impl.rdAll());

		return res;
	}

	@Override
	public Collection<DepTuple> inAll(DepTuple template, Context ctx)
			throws DepSpaceException {

		if(this.depth==0 || this.depth > template.getFields().length){
			return listImpl.inAll(template, ctx);
		}

		Collection<DepSpaceListImpl> lists = getListImpl(template);
		Collection<DepTuple> res = new LinkedList<DepTuple>();
		for(DepSpaceListImpl impl : lists){
			Collection<DepTuple> list = impl.inAll(template, ctx);
			res.addAll(list);
			DepTuple tup = list.iterator().next();
			if(impl.rdAll().isEmpty()){
				Map<Object, ListMap> map = tupleBag;
				for(int i=0; i<depth ; i++){
					if(i==depth-1)
						map.remove(tup.getFields()[i]);
					else
						map=map.get(tup.getFields()[i]).getMap();
				}
			}
		}
		return res;
	}

	/*
	 * @requires depth > 0;
	 */
	private Collection<DepSpaceListImpl> getListImpl(DepTuple template) throws DepSpaceException{
		Map<Object, ListMap> map = tupleBag;
		Object[] fields = template.getFields();
		Collection<DepSpaceListImpl> res = new ArrayList<DepSpaceListImpl>();

		for(int i=0; i<depth ; i++){
			if(map.get(fields[i]) == null){
				if(fields[i].equals(DepTuple.WILDCARD))
					break;
				else
					return res;
			}else{
				if(i!=depth-1){
					map = map.get(fields[i]).getMap();
				}else{
					res.add(map.get(fields[i]).getList());
					return res;
				}
			}
		}

		//solve Asterisk
		return solveAsterisk(tupleBag, 0, template);

	}

	private Collection<DepSpaceListImpl> solveAsterisk(Map<Object,ListMap> map, int index, DepTuple template){
		printTree();
		Object[] fields = template.getFields();
		Collection<DepSpaceListImpl> res = new ArrayList<DepSpaceListImpl>();
		if(index == depth-1){
			//paragem
			if(fields[index].equals(DepTuple.WILDCARD)){
				for(ListMap lm : map.values()){
					res.add(lm.getList());
				}
			}else{
				if(map.get(fields[index]) != null)
					res.add(map.get(fields[index]).getList());
			}
			return res;
		}else{
			//passo
			//			for(int i = index ; i<depth ; i++){
			if(fields[index].equals(DepTuple.WILDCARD)){
				for(ListMap lm : map.values()){
					res.addAll(solveAsterisk(lm.getMap(), index+1, template));
				}
			}else{
				res.addAll(solveAsterisk(map.get(fields[index]).getMap(), index+1, template));
			}
			//			}
		}
		return res;
	}

	private void printTree(){
		for(Object o : tupleBag.keySet()){
			System.out.println(o.toString());
			ListMap m = tupleBag.get(o);
			printTree(m, "\t");
		}
	}

	private void printTree(ListMap m, String ident){
		if(m.isMap()){
			for(Object key : m.getMap().keySet()){
				System.out.println(ident+key.toString());
				printTree(m.getMap().get(key), ident.concat("\t"));
			}
		}else{
			Collection <DepTuple> l = m.getList().rdAll();
			for(DepTuple t : l){
				System.out.println(ident + t.toStringTuple());
			}
		}
	}


	private Collection<DepSpaceListImpl> getAllLists(Map<Object,ListMap> map, int index){
		Collection<DepSpaceListImpl> res = new ArrayList<DepSpaceListImpl>();
		if(index == depth-1){
			for(ListMap lm : map.values())
				res.add(lm.getList());
		}else{
			for(ListMap lm : map.values())
				res.addAll(getAllLists(lm.getMap(), index+1));
		}
		return res;
	}

//	public byte[] getSnapshot() {
//		try {
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			ObjectOutputStream oos = new ObjectOutputStream(baos);
//			Collection<DepSpaceListImpl> lists = getAllLists(tupleBag, 0);
//
//			oos.writeBoolean(realTimeRenew);
//			oos.writeInt(depth);
//			listImpl.writeExternal(oos);
//			oos.writeInt(lists.size());
//			for(DepSpaceListImpl impl : lists){
//				impl.writeExternal(oos);
//			}
//			oos.close();
//			baos.close();
//			return baos.toByteArray();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

//	public void installSnapshot(byte[] state) {
//		try {
//			ByteArrayInputStream bais = new ByteArrayInputStream(state);
//			ObjectInputStream ois = new ObjectInputStream(bais);
//
//			realTimeRenew = ois.readBoolean();
//			depth = ois.readInt();
//			listImpl = new DepSpaceListImpl(realTimeRenew);
//			listImpl.readExternal(ois);
//
//			DepSpaceListImpl impl;
//			int size = ois.readInt();
//			Collection<DepSpaceListImpl> lists = new ArrayList<DepSpaceListImpl>(size);
//			for(int i = 0 ; i < size ; i++ ){
//				impl = new DepSpaceListImpl(realTimeRenew);
//				impl.readExternal(ois);
//				lists.add(impl);
//			}
//
//			ois.close();
//			bais.close();
//
//			Object[] fields;
//			for(DepSpaceListImpl list : lists){
//				Map<Object, ListMap> map = tupleBag;
//				fields = list.getKeys(depth);
//				if(fields == null)
//					continue;
//
//				for(int i=0; i<depth-1 ; i++){
//					if(map.get(fields[i]) == null){
//						map.put(fields[i], new ListMap(null, new ConcurrentHashMap<Object, ListMap>()));
//					}
//					map = map.get(fields[i]).getMap();
//				}
//
//				if(map.get(fields[depth-1]) == null){
//					map.put(fields[depth-1], new ListMap(list, null));
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//
//
//	}

//	@Override
//	public void readExternal(ObjectInput in) throws IOException,
//	ClassNotFoundException {
//		realTimeRenew = in.readBoolean();
//		depth = in.readInt();
//		listImpl = new DepSpaceListImpl(realTimeRenew);
//		listImpl.readExternal(in);
//
//		DepSpaceListImpl impl;
//		int size = in.readInt();
//		Collection<DepSpaceListImpl> lists = new ArrayList<DepSpaceListImpl>(size);
//		for(int i = 0 ; i < size ; i++ ){
//			impl = new DepSpaceListImpl(realTimeRenew);
//			impl.readExternal(in);
//			lists.add(impl);
//		}
//
//		in.close();
//		in.close();
//
//		Object[] fields;
//		for(DepSpaceListImpl list : lists){
//			Map<Object, ListMap> map = tupleBag;
//			fields = list.getKeys(depth);
//			if(fields == null)
//				continue;
//
//			for(int i=0; i<depth-1 ; i++){
//				if(map.get(fields[i]) == null){
//					map.put(fields[i], new ListMap(null, new ConcurrentHashMap<Object, ListMap>()));
//				}
//				map = map.get(fields[i]).getMap();
//			}
//
//			if(map.get(fields[depth-1]) == null){
//				map.put(fields[depth-1], new ListMap(list, null));
//			}
//		}
//
//
//	}

//	@Override
//	public void writeExternal(ObjectOutput out) throws IOException {
//		Collection<DepSpaceListImpl> lists = getAllLists(tupleBag, 0);
//
//		out.writeBoolean(realTimeRenew);
//		out.writeInt(depth);
//		listImpl.writeExternal(out);
//		out.writeInt(lists.size());
//		for(DepSpaceListImpl impl : lists){
//			impl.writeExternal(out);
//		}
//
//	}

}
