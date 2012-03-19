package svtype;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;


public class SvTuple implements BaseData,OtpConvert,Serializable {
	private static final long serialVersionUID = -7192521213111410337L;
	private BaseData[] value = null;
	public SvTuple() {
	}

	public BaseData[] getValue() {
		return value;
	}

	public void setValue(BaseData[] value) {
		this.value = value;
	}

	public SvTuple(BaseData[] array) {
		this.setValue(array);
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for (BaseData val : this.getValue()){
			if (sb.length()>1) sb.append(",");
			sb.append(val.toString());
		}
		sb.append("}");
		return  sb.toString();
	}
	@Override
	public OtpErlangObject convert() throws Exception {
		List<OtpErlangObject> list = new LinkedList<OtpErlangObject>();
		for (BaseData data : this.getValue()){
			if (data instanceof OtpConvert){
				OtpConvert convert = (OtpConvert) data;
				list.add(convert.convert());
			}
		}
		return new OtpErlangTuple(list.toArray(new OtpErlangObject[list.size()]));
	}

	@Override
	public String toTrimmedString() {
		StringBuffer sb = new StringBuffer();
		for (BaseData val : this.getValue()){
			if (sb.length()>1) sb.append(",");
			sb.append(val.toString());
		}
		return  sb.toString();
	}



}
