package com.dragonflow.erlangecc.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;

import org.ofbiz.base.util.Debug;
import org.ofbiz.entity.GenericValue;

import com.dragonflow.erlangecc.util.SvMessage;
import com.dragonflow.siteview.ErlangUtils;
import com.ericsson.otp.erlang.OtpConverter;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

//public abstract class BaseMonitor extends Thread {
public abstract class BaseMonitor implements Runnable {
	private SvMessage message;

	public static final String module = BaseMonitor.class.getName();
	
	public abstract int handleMessage();
	
	public BaseMonitor(){
		
	}

	public BaseMonitor(SvMessage message) {
		super();
		this.setMessage(message);
	}

	@Override
	public void run() {
		this.handleMessage();
	}

	public void setMessage(SvMessage message) {
		this.message = message;
	}
	
	public SvMessage getMessage() {
		return message;
	}

	public int sendResponse(String status,Map<String,Object> Result){
		OtpErlangObject[] response = new OtpErlangObject[3];
		OtpErlangList list = ErlangUtils.map2ErlangList(Result);
		response[0] = new OtpErlangAtom(status);
		response[1] = message.getMailbox().self();	
		response[2] = list;				

		OtpErlangTuple msg = new OtpErlangTuple(response);

		message.getMailbox().send(message.getRemotePid(), msg);
		
		return 0;
	}
	
	public int sendOfbizResponse(String status,Object rtnObj) {
		//including status in the responce to erlang
		OtpErlangObject erlResultObj = null; //list of tuple(map)
		try {
//			erlResultList = (OtpErlangList)ObjectType.simpleTypeConvert(rtnMapList, "com.ericsson.otp.erlang.OtpErlangList", null, null,false);
		    if (rtnObj instanceof GenericValue) {
		      erlResultObj = OtpConverter.Object2OtpErlangObject(((GenericValue)rtnObj).getAllFields());
		    }
			erlResultObj = OtpConverter.Object2OtpErlangObject(rtnObj);
		} catch (Exception e) {
			Debug.logError("convert Erlang message to service OUT parameter error",module);
		}
			
		try {
           	List<Object> tupleList = FastList.newInstance();
            tupleList.add(message.getMailbox().self());
            tupleList.add(erlResultObj);
            OtpErlangTuple erlMap = new OtpErlangTuple(tupleList.toArray(new OtpErlangObject[tupleList.size()]));
//            Debug.logInfo("Return message to erlang: " + erlMap.toString(),module);
            message.getMailbox().send(message.getRemotePid(), erlMap);
		} catch (Exception e) {
			Debug.logError("return to Erlang error: "+e,module);
		}
		
		return 0;
	}
	
	public int sendResponse2(String status,Map<String,Object> Result){
		OtpErlangObject[] response = new OtpErlangObject[3];
		OtpErlangList list = ErlangUtils.map2ErlangList2(Result);
		response[0] = new OtpErlangAtom(status);
		response[1] = message.getMailbox().self();	
		response[2] = list;				

		OtpErlangTuple msg = new OtpErlangTuple(response);

		message.getMailbox().send(message.getRemotePid(), msg);
		
		return 0;
	}
	public int sendResponse3(String status,ArrayList<String> Result){
		OtpErlangObject[] response = new OtpErlangObject[3];
		OtpErlangList list = ErlangUtils.arrayList2ErlangList(Result);
		response[0] = new OtpErlangAtom(status);
		response[1] = message.getMailbox().self();	
		response[2] = list;				

		OtpErlangTuple msg = new OtpErlangTuple(response);

		message.getMailbox().send(message.getRemotePid(), msg);
		
		return 0;
	}
	
	public int sendResponse(String status,String Desc){
		OtpErlangObject[] response = new OtpErlangObject[3];
		response[0] = new OtpErlangAtom(status);
		response[1] = message.getMailbox().self();	
		response[2] = new OtpErlangList(Desc);				

		OtpErlangTuple msg = new OtpErlangTuple(response);

		message.getMailbox().send(message.getRemotePid(), msg);
		
		return 0;
	}
}
