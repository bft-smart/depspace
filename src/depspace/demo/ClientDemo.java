/*
 * ClientTest.java
 *
 * Created on 15 de Maio de 2006, 15:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package depspace.demo;

import java.util.Collection;
import java.util.Properties;
import java.util.Scanner;

import depspace.client.DepSpaceAccessor;
import depspace.client.DepSpaceAdmin;
import depspace.general.DepSpaceConfiguration;
import depspace.general.DepSpaceException;
import depspace.general.DepSpaceProperties;
import depspace.general.DepTuple;

/**
 * 
 * @author edualchieri
 */
public class ClientDemo {

	private int executions;
	private boolean createSpace;
	private int id;

	/** Creates a new instance of ClientTest */
	public ClientDemo(int clientId, int exec, boolean createSpace) {
		this.id = clientId;
		this.executions = exec;
		this.createSpace = createSpace;
	}

	public void run() {
		try {



			/*
			 * if(this.createSpace){ //this will delete the DepSpace
			 * accessor.finalize(); }
			 */
			System.out.println("THE END!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DepTuple get(int i) {
		return DepTuple.createTuple(i, "confidentiality", "I'm the client",
				"BUMMMM!!!");
	}

	private void access(DepSpaceAccessor accessor) throws Exception {
		System.out.println("Using the tuple: " + get(0));

		for (int i = 0; i < executions; i++) {
			System.out.println("Sending " + i);
			// OUT
			//			DepTuple dt = get(i);
			//			accessor.out(dt);
			//System.out.println("OUT ready.");
			//pause();
			// RDP
			DepTuple template = DepTuple.createTuple(i, "*", "*", "*");
			System.out.println("RDP: " + accessor.rdp(template));
			//pause();
			// CAS READ
			//System.out.println("CAS READ: " + accessor.cas(template, dt));
			//pause();
			// INP
			//System.out.println("INP: " + accessor.inp(template));
			//pause();
			// CAS INSERT
			//System.out.println("CAS INSERT: " + accessor.cas(template, dt));
			//pause();

		}

		// INALL TEST
		/* 	DepTuple template = DepTuple.createTuple("*", "*", "*", "*");
		Collection<DepTuple> tupleBag = accessor.inAll(template, 2);
		System.out.println("INALL:");
		for (DepTuple tuple : tupleBag)
			System.out.println("\t IN: " + tuple);
		pause();*/
		// accessor.renewTransactionTimeout(transId,9999);
		synchronized (this) {
			try {
				this.wait(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// accessor.commitTransaction(transId);
	}

	public static void main(String[] args) throws DepSpaceException {

		int exec = 1;
		boolean create = true;

		String name = "Demo Space";
		
		DepSpaceConfiguration.init(null);
		
		// the DepSpace name
		Properties prop = DepSpaceProperties.createDefaultProperties(name);

		// use confidentiality?
		prop.put(DepSpaceProperties.DPS_CONFIDEALITY, "false");

		int clientID = 4;

		// the DepSpace Accessor, who will access the DepSpace.
		DepSpaceAccessor accessor = null;
		if (create) {
			accessor = new DepSpaceAdmin(clientID).createSpace(prop);
		} else {
			accessor = new DepSpaceAdmin(clientID).createAccessor(prop, create);
		}

		int cont = 50;

		Scanner sc = new Scanner(System.in);
		Command command = Command.rdp;
		DepTuple template, tuple;
		while(command!=Command.exit){
			String[] commStr; 
			if(cont<50){
				commStr = new String [] {"out", "a,"+cont};
				System.out.println("> " + commStr[0] + " " + commStr[1]);
				cont++;
			} else {
				System.out.print("> ");
				commStr = sc.nextLine().split(" ");
			}
			if(commStr.length > 3){
				System.out.println("Invalid number of args. ");
				continue;
			}

			try{
				command=Command.valueOf(commStr[0]);
			}catch(java.lang.IllegalArgumentException e){
				System.out.println("Invalid Command.");
				continue;
			}

			// === OPERATIONS THREATMENT ====
			switch(command){
			case rdp:
				if(commStr.length<2){
					System.out.println("Invalid number of args. ");
					continue;
				}
				template = DepTuple.createTuple(commStr[1].split(","));
				DepTuple dt = accessor.rdp(template);
				if(dt==null)
					System.out.println("null");
				else
					System.out.println(dt.toStringTuple());
				break;
			case cas:
				if(commStr.length<3){
					System.out.println("Invalid number of args. ");
					continue;
				}
				template = DepTuple.createTuple(commStr[1].split(","));
				tuple = DepTuple.createTuple(commStr[2].split(","));
				DepTuple res = accessor.cas(template, tuple);
				if(res==null)
					System.out.println("null");
				else
					System.out.println(res.toStringTuple());
				break;
			case inall:
				if(commStr.length<2){
					System.out.println("Invalid number of args. ");
					continue;
				}
				template = DepTuple.createTuple(commStr[1].split(","));
				Collection<DepTuple> l = accessor.inAll(template);
				for(DepTuple t : l)
					System.out.println(t.toStringTuple());
				break;
			case inp:
				if(commStr.length<2){
					System.out.println("Invalid number of args. ");
					continue;
				}
				template = DepTuple.createTuple(commStr[1].split(","));
				DepTuple t = accessor.inp(template);
				if(t==null)
					System.out.println("null");
				else
					System.out.println(t.toStringTuple());
				break;
			case out:
				if(commStr.length<2){
					System.out.println("Invalid number of args. ");
					continue;
				}
				tuple = DepTuple.createTuple(commStr[1].split(","));
				accessor.out(tuple);
				break;
			case rdall:
				Collection<DepTuple> list=null;
				if(commStr.length==2){
					template = DepTuple.createTuple(commStr[1].split(","));
					list = accessor.rdAll(template, 0);
				}else{
					System.out.println("Invalid number of args. ");
					continue;
				}
				for(DepTuple tup : list)
					System.out.println(tup.toStringTuple());
				break;
			case replace:
				if(commStr.length<3){
					System.out.println("Invalid number of args. ");
					continue;
				}
				template = DepTuple.createTuple(commStr[1].split(","));
				tuple = DepTuple.createTuple(commStr[2].split(","));
				DepTuple r = accessor.replace(template, tuple);
				if(r==null)
					System.out.println("null");
				else
					System.out.println(r.toStringTuple());
				break;
			case exit:
				break;
			}
		}
	}
}

enum Command{
	rdp, cas, inp, rdall, inall, out, replace,exit;
}