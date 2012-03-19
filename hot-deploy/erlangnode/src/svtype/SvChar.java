package svtype;

import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangChar;
import com.ericsson.otp.erlang.OtpErlangObject;


public class SvChar implements BaseData,OtpConvert,Serializable {
	private static final long serialVersionUID = 8152304391212241916L;
	private char value;
	
	public char getValue() {
		return value;
	}

	public void setValue(char value) {
		this.value = value;
	}

	public SvChar(char charValue) {
		this.setValue(charValue);
	}
	public SvChar() {
	}
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(this.getValue());
		return  sb.toString();
	}

	@Override
	public OtpErlangObject convert() throws Exception {
		return new OtpErlangChar(this.getValue());
	}

	@Override
	public String toTrimmedString() {
		return toString();
	}


}
