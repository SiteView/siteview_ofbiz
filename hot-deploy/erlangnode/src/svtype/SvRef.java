package svtype;

import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangRef;


public class SvRef implements BaseData,OtpConvert,Serializable {
	private static final long serialVersionUID = -5198826816830845769L;
	private String node;
	private int[] ids;
	private int creation;
	public SvRef() {
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public int[] getIds() {
		return ids;
	}

	public void setIds(int[] ids) {
		this.ids = ids;
	}

	public int getCreation() {
		return creation;
	}

	public void setCreation(int creation) {
		this.creation = creation;
	}

	public SvRef(String node, int[] ids, int creation) {
		this.setNode(node);
		this.setIds(ids);
		this.setCreation(creation);
	}
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("Ref(");
		sb.append("node=");
		sb.append(this.getNode());
		sb.append(",ids=[");
		boolean b = false;
		for (int id : this.getIds()){
			if (b) sb.append(",");
			sb.append(id);
			b=true;
		}
		sb.append("],creation=");
		sb.append(this.getCreation());
		sb.append(")");
		return  sb.toString();
	}
	@Override
	public OtpErlangObject convert() throws Exception {
		return new OtpErlangRef(this.getNode(),this.getIds(),this.getCreation());
	}

	@Override
	public String toTrimmedString() {
		return toString();
	}

}
