package depspace.general;

import bftsmart.tom.core.messages.TOMMessageType;


public enum DepSpaceOperation {

	OUT,
	RENEW,
	RDP,
	INP,
	RD,
	IN,
	SIGNED_RD,
	CAS,
	REPLACE,
	OUTALL,
	RDALL,
	INALL,
	CREATE,
	DELETE,
	CLEAN,
	EXCEPTION;
	

	public static DepSpaceOperation[] values = values();

	
	public static DepSpaceOperation getOperation(int ordinal) {
		return values[ordinal];
	}
	
	public TOMMessageType getRequestType() {
		switch(this) {
		case RD:
		case RDALL:
		case RDP:
			return TOMMessageType.UNORDERED_REQUEST;
		default:
			return TOMMessageType.ORDERED_REQUEST;
		}
	}

	public boolean isBlocking() {
		return ((this == RD) || (this == IN));
	}
	
}
