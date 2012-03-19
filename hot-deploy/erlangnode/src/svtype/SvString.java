package svtype;

import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;


public class SvString implements BaseData,OtpConvert,Serializable {
	private static final long serialVersionUID = -3499399982631043588L;
	private String value = null;
	public SvString() {
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public SvString(String stringValue) {
		this.setValue(stringValue);
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("\"");
		sb.append(this.getValue());
		sb.append("\"");
		return  sb.toString();
	}

	@Override
	public OtpErlangObject convert() throws Exception {
		if (value==null || value.isEmpty()) {
			return new OtpErlangList();
		}
		else
			return new OtpErlangString(OtpErlangString.newString(this.getValue().getBytes("UTF-8")));
	}

	@Override
	public String toTrimmedString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getValue());
		return  sb.toString();
	}


}
