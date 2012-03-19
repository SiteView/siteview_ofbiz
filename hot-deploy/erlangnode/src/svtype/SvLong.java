package svtype;

import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangObject;


public class SvLong implements BaseData,OtpConvert,Serializable {
	private static final long serialVersionUID = -5703076593993598775L;
	private long value;
	
	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public SvLong(long longValue) {
		this.setValue(longValue);
	}

	public SvLong() {
	}
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(this.getValue());
		return  sb.toString();
	}
	@Override
	public OtpErlangObject convert() throws Exception {
		return new OtpErlangLong(this.getValue());
	}

	@Override
	public String toTrimmedString() {
		return toString();
	}

}
