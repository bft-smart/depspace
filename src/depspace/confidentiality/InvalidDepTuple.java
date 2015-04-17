package depspace.confidentiality;

import depspace.general.DepSpaceReply;
import depspace.general.DepTuple;

/**
 *
 * @author alysson
 */
public class InvalidDepTuple extends DepTuple {

	private DepSpaceReply[] responses;
	private ProtectionVector vector;
	private int[] from;

	/** Creates a new instance of InvalidDepTuple */
	public InvalidDepTuple(DepSpaceReply[] responses, int[] from, ProtectionVector vector) {
		this.responses = responses;
		this.vector = vector;
		this.from = from;
	}

	public DepSpaceReply[] getResponses(){
		return this.responses;
	}

	public ProtectionVector getProtectionVector(){
		return this.vector;
	}

	public int[] getFrom(){
		return from;
	}

}