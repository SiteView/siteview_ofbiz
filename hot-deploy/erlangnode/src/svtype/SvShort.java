package svtype;

import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangShort;


public class SvShort implements BaseData,OtpConvert,Serializable {
	private static final long serialVersionUID = 5788542263695312808L;
	private short value;
	public SvShort() {
	}

	public short getValue() {
		return value;
	}

	public void setValue(short value) {
		this.value = value;
	}

	public SvShort(short shortValue) {
		this.setValue(shortValue);
	}
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(this.getValue());
		return  sb.toString();
	}
	@Override
	public OtpErlangObject convert() throws Exception {
		return new OtpErlangShort(this.getValue());
	}

	@Override
	public String toTrimmedString() {
		return toString();
	}


}
