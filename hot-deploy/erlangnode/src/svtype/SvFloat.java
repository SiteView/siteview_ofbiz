package svtype;

import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangFloat;
import com.ericsson.otp.erlang.OtpErlangObject;


public class SvFloat implements BaseData,OtpConvert,Serializable {
	private static final long serialVersionUID = 6964751623374735603L;
	private float value;
	
	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public SvFloat(float floatValue) {
		this.setValue(floatValue);
	}
	public SvFloat() {
	}
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(this.getValue());
		return  sb.toString();
	}
	@Override
	public OtpErlangObject convert() throws Exception {
		return new OtpErlangFloat(this.getValue());
	}

	@Override
	public String toTrimmedString() {
		return toString();
	}

}
