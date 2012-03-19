package svtype;

import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangPort;


public class SvPort implements BaseData ,OtpConvert,Serializable{
	private static final long serialVersionUID = 9213809571474844023L;
	private String node;
	private int id;
	private int creation;
	public SvPort() {
	}
	
	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCreation() {
		return creation;
	}

	public void setCreation(int creation) {
		this.creation = creation;
	}

	public SvPort(String node, int id, int creation) {
		this.setNode(node);
		this.setId(id);
		this.setCreation(creation);
	}
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("Port(");
		sb.append("node=");
		sb.append(this.getNode());
		sb.append(",id=");
		sb.append(this.getId());
		sb.append(",creation=");
		sb.append(this.getCreation());
		sb.append(")");
		return  sb.toString();
	}
	@Override
	public OtpErlangObject convert() throws Exception {
		return new OtpErlangPort(this.getNode(),this.getId(),this.getCreation());
	}

	@Override
	public String toTrimmedString() {
		return toString();
	}


}
