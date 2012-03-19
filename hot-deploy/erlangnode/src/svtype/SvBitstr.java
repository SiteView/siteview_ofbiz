package svtype;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangBitstr;
import com.ericsson.otp.erlang.OtpErlangObject;


public class SvBitstr implements BaseData,OtpConvert,Serializable {
	private static final long serialVersionUID = -4446750677634846908L;
	private List<Integer> value = null;
	public List<Integer> getValue() {
		return value;
	}

	public void setValue(List<Integer> value) {
		this.value = value;
	}

	public SvBitstr(byte[] binaryValue) {
		List<Integer> list = new LinkedList<Integer>();
		for(byte b : binaryValue){
			list.add(new Integer(b));
		}
		this.setValue(list);
	}
	
	public SvBitstr(List<Integer> binaryValue) {
		this.setValue(binaryValue);
	}
	public SvBitstr() {
	}
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(this.getValue());
		return  sb.toString();
	}

	@Override
	public OtpErlangObject convert() throws Exception {
		List<Integer> list = getValue();
		byte[] bytes = new byte[list.size()];
		for (int i = 0 ; i<bytes.length ; i++){
			bytes[i] = list.get(i).byteValue(); 
		}
		return new OtpErlangBitstr(bytes);
	}

	@Override
	public String toTrimmedString() {
		return toString();
	}
}
