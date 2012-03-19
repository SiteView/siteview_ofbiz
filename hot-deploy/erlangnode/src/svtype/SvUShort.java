package svtype;

import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangUShort;


public class SvUShort implements BaseData,OtpConvert,Serializable {
	private static final long serialVersionUID = 2576845401372507110L;
	private short value;
	public SvUShort() {
	}

	public short getValue() {
		return value;
	}

	public void setValue(short value) {
		this.value = value;
	}

	public SvUShort(short uShortValue) {
		this.setValue(uShortValue);
	}
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(this.getValue());
		return  sb.toString();
	}
	@Override
	public OtpErlangObject convert() throws Exception {
		return new OtpErlangUShort(this.getValue());
	}

	@Override
	public String toTrimmedString() {
		return toString();
	}

}
