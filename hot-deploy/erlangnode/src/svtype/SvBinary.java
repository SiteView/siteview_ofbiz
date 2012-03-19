package svtype;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangObject;


public class SvBinary implements BaseData,OtpConvert,Serializable  {
	private static final long serialVersionUID = -1025044331678090399L;
	private byte[] value = null;
	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	public SvBinary(byte[] binaryValue) {
		this.setValue(binaryValue);
	}
	
	public List<Integer> getIntegerValues(){
		List<Integer> list = new LinkedList<Integer>();
		for(byte b : this.getValue()){
			list.add(new Integer(b));
		}
		return list;
	}
	
	public SvBinary() {
	}
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(byte b : this.getValue()){
			if (sb.length()>0) sb.append(",");
			sb.append(b);
		}
		return "<<" + sb.toString() + ">>";
	}

	@Override
	public OtpErlangObject convert() throws Exception {
		return new OtpErlangBinary(getValue());
	}

	@Override
	public String toTrimmedString() {
		return toString();
	}
}
