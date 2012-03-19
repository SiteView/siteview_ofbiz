package svtype;

import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangByte;
import com.ericsson.otp.erlang.OtpErlangObject;


public class SvByte implements BaseData ,OtpConvert,Serializable{
	private static final long serialVersionUID = 5841123563469294570L;
	private byte value;
	public byte getValue() {
		return value;
	}

	public void setValue(byte value) {
		this.value = value;
	}

	public SvByte(byte byteValue) {
		this.setValue(byteValue);
	}
	public SvByte() {
	}
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(this.getValue());
		return  sb.toString();
	}
	@Override
	public OtpErlangObject convert() throws Exception {
		return new OtpErlangByte(this.getValue());
	}

	@Override
	public String toTrimmedString() {
		return toString();
	}


}
