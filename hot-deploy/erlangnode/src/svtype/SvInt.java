package svtype;

import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangInt;
import com.ericsson.otp.erlang.OtpErlangObject;


public class SvInt implements BaseData,OtpConvert,Serializable  {
	private static final long serialVersionUID = -1382234651153936463L;
	private int value;
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public SvInt(int intValue) {
		this.setValue(intValue);
	}
	public SvInt() {
	}
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(this.getValue());
		return  sb.toString();
	}
	@Override
	public OtpErlangObject convert() throws Exception {
		return new OtpErlangInt(this.getValue());
	}

	@Override
	public String toTrimmedString() {
		return toString();
	}


}
