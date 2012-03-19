package svtype;

import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangBoolean;
import com.ericsson.otp.erlang.OtpErlangObject;


public class SvBoolean implements BaseData,OtpConvert,Serializable {
	private static final long serialVersionUID = 4504342409077082647L;
	private boolean value = false;
	
	public boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	public SvBoolean(boolean booleanValue) {
		this.setValue(booleanValue);
	}
	public SvBoolean() {
	}
	public String toString(){
		return this.getValue() ? "true" : "false";
	}


	@Override
	public OtpErlangObject convert() throws Exception {
		return new OtpErlangBoolean(this.getValue());
	}

	@Override
	public String toTrimmedString() {
		return toString();
	}

}
