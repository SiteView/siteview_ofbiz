package com.dragonflow.erlangecc.util;

import java.io.Serializable;
import java.util.Map;
import com.dragonflow.siteview.ErlangUtils;
import com.ericsson.otp.erlang.*;
import org.ofbiz.base.util.Debug;

public class SvMessage implements Serializable{
    public static final String module = SvMessage.class.getName();
	private String remoteNode;
	private OtpErlangPid remotePid;
	private String	className, action;
	private long	messageId;
	private Map<String,Object> params;
//	private Map<String,Object> ofbizParams = FastMap.newInstance();
	private Object ofbizParams;
	private OtpMbox		mailbox;
	private String	requestType;
	private String MonitorClassName;
	private OtpErlangObject erlObj;
	private OtpErlangList tuple;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public SvMessage(OtpMbox mailbox) {
		super();
		this.setMailbox(mailbox);
	}
	
	//receive msg and convert the OtpErlang data type to Java native type
	public int fromOtpMsg(OtpMsg otpmsg){

//		ModelParam modelParam = null;
		
		OtpErlangObject erlobject;
		try {
			erlobject = otpmsg.getMsg();
		
			if (erlobject instanceof OtpErlangTuple) {
				OtpErlangTuple t = (OtpErlangTuple) erlobject;
				remotePid = (OtpErlangPid)t.elementAt(0);
				requestType = ((OtpErlangString)  t.elementAt(1)).stringValue();
				if(t.elementAt(2) instanceof OtpErlangString)
					className =  ((OtpErlangString)  t.elementAt(2)).stringValue();
				else className =  ((OtpErlangAtom)  t.elementAt(2)).atomValue();
//				action =  ((OtpErlangString)  t.elementAt(3)).stringValue();
				if(t.elementAt(3) instanceof OtpErlangString){
					action =  ((OtpErlangString)  t.elementAt(3)).stringValue();
				}else {
					action =  ((OtpErlangList)  t.elementAt(3)).toString();
					tuple =  ((OtpErlangList)  t.elementAt(3));
				}
				
				if("UpdateMonitor".equalsIgnoreCase(requestType))
				{
					MonitorClassName  = ((OtpErlangString)  t.elementAt(2)).stringValue();
				} 
//				else if ("LogMonitor2".equalsIgnoreCase(requestType)){
//					erlObj = t.elementAt(t.arity()-1);
//					return 0;
//				}
				erlObj = t.elementAt(t.arity()-1);
				
//		        ofbizParams = (Map<String,Object>)OtpConverter.OtpErlangObject2Object(Obj);
				ofbizParams = OtpConverter.OtpErlangObject2Object(erlObj);
		      //old way for data conversion
		        if (erlObj instanceof OtpErlangList) {	
					params = ErlangUtils.erlangListToMap((OtpErlangList)erlObj);
				}else if(erlObj instanceof OtpErlangTuple){
					OtpErlangTuple t1 = (OtpErlangTuple) erlObj;
					params = ErlangUtils.erlangListToMap((OtpErlangList)t1.elementAt(2));
				}
			}
		} catch (OtpErlangDecodeException e) {
				e.printStackTrace();
		} catch (Exception e) {
				Debug.logError("convert Erlang message to service In parameter error"+e,module);
		}
		return 0;
	}
	
	public OtpErlangObject getErlObj(){
		return erlObj;
	}

	/**
	 * @return the remoteNode
	 */
	public String getRemoteNode() {
		return remoteNode;
	}
	/**
	 * @param remoteNode the remoteNode to set
	 */
	public void setRemoteNode(String remoteNode) {
		this.remoteNode = remoteNode;
	}
	/**
	 * @return the remotePid
	 */
	public OtpErlangPid getRemotePid() {
		return remotePid;
	}
	
	public OtpErlangList getTuple() {
		return tuple;
	}

	public void setTuple(OtpErlangList tuple) {
		this.tuple = tuple;
	}

	/**
	 * @param remotePid the remotePid to set
	 */
	public void setRemotePid(OtpErlangPid remotePid) {
		this.remotePid = remotePid;
	}
	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}
	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}
	/**
	 * @return the messageId
	 */
	public long getMessageId() {
		return messageId;
	}
	/**
	 * @param messageId the messageId to set
	 */
	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}
	/**
	 * @return the params
	 */
	public Map<String, Object> getParams() {
		return params;
	}
	/**
	 * @param params the params to set
	 */
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public Object getOfbizParams() {
		return ofbizParams;
	}

	public void setOfbizParams(Object ofbizParams) {
		this.ofbizParams = ofbizParams;
	}

	public String getMonitorClassName() {
		return MonitorClassName;
	}

	public void setMonitorClassName(String monitorClassName) {
		MonitorClassName = monitorClassName;
	}
	
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setMailbox(OtpMbox mailbox) {
		this.mailbox = mailbox;
	}

	public OtpMbox getMailbox() {
		return mailbox;
	}
	
	public String toString(String type){
	  
	  if("TopologyDispatch".equalsIgnoreCase(type))return "TuoPu("+action+"):"+erlObj;
	  if ("UpdateMonitor".equalsIgnoreCase(type)) return "Monitor("+ MonitorClassName + "):" +"Action(" + action + "): "+ ofbizParams.toString();
	  if ("ofbizService".equalsIgnoreCase(type)) return "Service("+action + "): "+ofbizParams.toString();
	  if ("LogMonitor".equalsIgnoreCase(type)) return  "Log monitors: "+ofbizParams.toString();
	  
	  return null;
	  
	}
      
	
	
}
