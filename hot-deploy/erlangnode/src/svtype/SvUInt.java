package svtype;

import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangUInt;


public class SvUInt implements BaseData ,OtpConvert,Serializable  {
	private static final long serialVersionUID = 5895588510537257666L;
	private int value;
	public SvUInt() {
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public SvUInt(int uIntValue) {
		this.setValue(uIntValue);
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(this.getValue());
		return  sb.toString();
	}


	@Override
	public OtpErlangObject convert() throws Exception {
		return new OtpErlangUInt(this.getValue());
	}

	@Override
	public String toTrimmedString() {
		return toString();
	}

}
