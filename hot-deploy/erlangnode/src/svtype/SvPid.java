package svtype;

import java.io.Serializable;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangPid;


public class SvPid implements BaseData,OtpConvert,Serializable  {
	private static final long serialVersionUID = -4578774580223732738L;
	private String node;
	private int id;
	private int serial;
	private int creation;

	public SvPid(final String node, final int id, final int serial,
		    final int creation) {
		this.setId(id);
		this.setNode(node);
		this.setSerial(serial);
		this.setCreation(creation);
	}

	public SvPid() {
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

	public int getSerial() {
		return serial;
	}

	public void setSerial(int serial) {
		this.serial = serial;
	}

	public int getCreation() {
		return creation;
	}

	public void setCreation(int creation) {
		this.creation = creation;
	}
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("Pid(");
		sb.append("node=");
		sb.append(this.getNode());
		sb.append(",id=");
		sb.append(this.getId());
		sb.append(",serial=");
		sb.append(this.getSerial());
		sb.append(",creation=");
		sb.append(this.getCreation());
		sb.append(")");
		return  sb.toString();
	}

	@Override
	public OtpErlangObject convert() throws Exception {
		return new OtpErlangPid(this.node,this.id,this.serial,this.creation);
	}

	@Override
	public String toTrimmedString() {
		return toString();
	}


}
