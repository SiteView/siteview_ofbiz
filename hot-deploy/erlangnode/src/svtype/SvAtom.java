package svtype;

import java.io.InvalidObjectException;
import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangObject;

public class SvAtom implements BaseData, OtpConvert, Serializable {
	private static final long serialVersionUID = 7683487457697422893L;
	private String value = null;

	public String getValue() {
		return value;
	}

	public void setValue(String value) throws Exception {
		if (value == null)
			throw new InvalidObjectException("Atom should not have no space.");
		// update by liangqing.meng
		// if (value.contains(" "))
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			if (ch >= 127 || ch < 0)
				throw new InvalidObjectException("Atom must be ASSIIC code");
			if (ch >= '0' || ch <= '9')
				continue;
			if (ch >= 'A' || ch <= 'Z')
				continue;
			if (ch >= 'a' || ch <= 'z')
				continue;
			if (ch == '_')
				continue;
			throw new InvalidObjectException(
					"Atom must be 0-9,A-Z,a-z");
		}
		this.value = value;
	}

	public SvAtom(String atomValue) throws Exception {
		this.setValue(atomValue);
	}

	public SvAtom() {
	}

	public String toString() {
		return this.getValue();
	}
	
	public String toTrimmedString() {
		return this.toString();
	}

	@Override
	public OtpErlangObject convert() throws Exception {
		return new OtpErlangAtom(this.getValue());
	}
}
