package svtype;

import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangDouble;
import com.ericsson.otp.erlang.OtpErlangObject;


public class SvDouble implements BaseData,OtpConvert,Serializable {
	private static final long serialVersionUID = 5971516912600488697L;
	private double value;
	
	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public SvDouble(double doubleValue) {
		this.setValue(doubleValue);
	}
	public SvDouble() {
	}
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(this.getValue());
		return  sb.toString();
	}
	@Override
	public OtpErlangObject convert() throws Exception {
		return new OtpErlangDouble(this.getValue());
	}

	@Override
	public String toTrimmedString() {
		return toString();
	}

}
