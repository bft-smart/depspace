package depspace.server;

import java.util.Collection;
import java.util.List;

import depspace.general.Context;
import depspace.general.DepSpaceException;
import depspace.general.DepTuple;

public class ReplaceTriggerLayer extends DepSpaceServerLayer {

	public ReplaceTriggerLayer(DepSpaceServerLayer upperLayer) {
		super(upperLayer);
	}

	
	@Override
	public void out(DepTuple tuple, Context ctx) throws DepSpaceException {
		upperLayer.out(tuple, ctx);
	}

	@Override
	public DepTuple renew(DepTuple template, Context ctx) throws DepSpaceException {
		return upperLayer.renew(template, ctx);
	}

	@Override
	public DepTuple rdp(DepTuple template, Context ctx) throws DepSpaceException {
		return upperLayer.rdp(template, ctx);
	}

	@Override
	public DepTuple inp(DepTuple template, Context ctx) throws DepSpaceException {
		return upperLayer.inp(template, ctx);
	}

	@Override
	public DepTuple rd(DepTuple template, Context ctx) throws DepSpaceException {
		return upperLayer.rd(template, ctx);
	}

	@Override
	public DepTuple in(DepTuple template, Context ctx) throws DepSpaceException {
		return upperLayer.in(template, ctx);
	}

	@Override
	public DepTuple cas(DepTuple template, DepTuple tuple, Context ctx) throws DepSpaceException {
		return upperLayer.cas(template, tuple, ctx);
	}

	// @Override
	// public DepTuple replace(DepTuple template, DepTuple tuple, Context ctx)
	// throws DepSpaceException {
	// System.out.println("\n\nREPLACE - ReplaceTriggerLayer : template = " +
	// template + "tuple = " + tuple);
	//
	// if(!getTotalName(template.getFields()).equals(getTotalName(tuple.getFields()))){
	// Object [] vec = new Object[tuple.getFields().length];
	// for(int i=0;i<vec.length;i++)
	// vec[i] = i<3 ? tuple.getFields()[i] : "*";
	// DepTuple dp = DepTuple.createTuple(vec);
	// if(upperLayer.rdp(dp, ctx)!=null)
	// System.out.println("REMOVI : " + upperLayer.inp(dp, ctx) + "\n\n");
	// }
	// return replaceDirAndChildrens(template, tuple, ctx);
	//
	// }

	@Override
	public DepTuple replace(DepTuple template, DepTuple tuple, Context ctx) throws DepSpaceException {
		if (getTotalName(template.getFields())!=null && getTotalName(tuple.getFields()) != null && !getTotalName(template.getFields()).equals(getTotalName(tuple.getFields()))) {
			Object[] vec = new Object[tuple.getFields().length];
			upperLayer.inp(createDirectoryServiceTuple(tuple.getFields()[1], tuple.getFields()[2], vec.length), ctx);
			return replaceDirAndChildrens(template, tuple, ctx);
		}
		return upperLayer.replace(template, tuple, ctx);
	}

	private DepTuple replaceDirAndChildrens(DepTuple template, DepTuple tuple, Context ctx) throws DepSpaceException {
		System.out.println("\n\n -- ReplaceDirAndChildren : template = " + template + "tuple = " + tuple);
		DepTuple res = upperLayer.replace(template, tuple, ctx);

		Object[] fieldsTupTempl = template.getFields();

		String newParent = getTotalName(fieldsTupTempl);
		DepTuple tup = createDirectoryServiceTuple(newParent, "*", fieldsTupTempl.length);
		tup.setN_Matches(0);
		Collection<DepTuple> list = upperLayer.rdAll(tup, ctx);
		for (DepTuple t : list) {
			Object[] tupleNew = t.getFields();
			tupleNew[1] = getTotalName(tuple.getFields());
			replaceDirAndChildrens(createDirectoryServiceTuple(newParent, t.getFields()[2], fieldsTupTempl.length),
					DepTuple.createAccessControledTuple(t.getC_rd(), t.getC_in(), tupleNew), ctx);
		}
		return res;
	}

	@Override
	public void outAll(List<DepTuple> tuplesBag, Context ctx) throws DepSpaceException {
		upperLayer.outAll(tuplesBag, ctx);
	}

	@Override
	public Collection<DepTuple> rdAll() throws DepSpaceException {
		return upperLayer.rdAll();
	}

	@Override
	public Collection<DepTuple> rdAll(DepTuple template, Context ctx) throws DepSpaceException {
		return upperLayer.rdAll(template, ctx);
	}

	@Override
	public Collection<DepTuple> inAll(DepTuple template, Context ctx) throws DepSpaceException {
		return upperLayer.inAll(template, ctx);
	}

	@Override
	public Object signedRD(DepTuple template, Context ctx) throws DepSpaceException {
		return upperLayer.signedRD(template, ctx);
	}

	@Override
	public void clean(DepTuple proof, Context ctx) throws DepSpaceException {
		upperLayer.clean(proof, ctx);
	}

	// private DepTuple replaceDirAndChildrens(DepTuple template, DepTuple
	// tuple, Context ctx) throws DepSpaceException{
	// //TODO: generalizar para qualquer tipo de tuplos.
	//
	// DepTuple tup = upperLayer.rdp(template, ctx);
	// if(tup == null || upperLayer.rdp(tuple, ctx) != null)
	// return null;
	//
	// Object[] fieldsTupTempl = tup.getFields();
	// Object[] fieldsTuple = tuple.getFields();
	//
	// for(int i = 0 ; i < fieldsTuple.length && fieldsTuple.length ==
	// fieldsTupTempl.length ; i++)
	// if(fieldsTuple[i].equals("*"))
	// fieldsTuple[i] = fieldsTupTempl[i];
	//
	// DepTuple res = upperLayer.replace(template,
	// DepTuple.createAccessControledTuple(tuple.getC_rd(), tuple.getC_in(),
	// fieldsTuple), ctx);
	// if(fieldsTupTempl[0].equals("DIR")){
	// String newParent = getTotalName(fieldsTupTempl);
	// tup = createDirectoryServiceTuple(newParent, "*",fieldsTupTempl.length);
	// tup.setN_Matches(0);
	// Collection<DepTuple> list = upperLayer.rdAll(tup, ctx);
	// for(DepTuple t : list)
	// replaceDirAndChildrens(createDirectoryServiceTuple(getTotalName(fieldsTupTempl),
	// t.getFields()[2], fieldsTupTempl.length),
	// createDirectoryServiceTuple(getTotalName(tuple.getFields()),
	// t.getFields()[2], fieldsTupTempl.length), ctx);
	// }
	// return res;
	// }

	private DepTuple createDirectoryServiceTuple(Object newParent, Object newName, int length) {
		Object[] fields = new Object[length];
		for(int i = 0; i < fields.length; i++) fields[i] = "*";
		fields[1] = newParent;
		fields[2] = newName;
		return DepTuple.createTuple(fields);
	}

	private String getTotalName(Object[] fields) {
		if (fields.length >= 3)
			return ((String) fields[1]).concat((fields[1].equals("/") ? "" : "/") + (String) fields[2]);
		else
			return null;
	}
	
}
