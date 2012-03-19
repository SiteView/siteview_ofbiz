package com.erlangnode.client.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import svtype.BaseData;
import svtype.OtpConvert;

import com.ericsson.otp.erlang.OtpConnection;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpPeer;
import com.ericsson.otp.erlang.OtpSelf;
import com.erlangnode.client.impl.CommunicationFactory;
import com.erlangnode.client.impl.EccService;
import com.erlangnode.client.itf.ErlangIf;
//import com.erlangnode.client.utils.ErlangDebugLog;
import com.erlangnode.client.impl.Tools;


public class ErlangDirectEccServiceImpl implements EccService {
	protected static final Random rand = new Random(System.currentTimeMillis());
	@Override
	public BaseData call(String module, String function, BaseData... params) throws Exception {
		List<OtpErlangObject> list = new LinkedList<OtpErlangObject>();
		for(BaseData data : params){
			if (data instanceof OtpConvert){
				OtpConvert convert = (OtpConvert) data;
				list.add(convert.convert());
			}
		}
		OtpErlangObject retobject = null;
		long timeLong = System.currentTimeMillis();
		String token = "" + rand.nextLong();
//		ErlangDebugLog.writeBeginLog(token,Thread.currentThread().getStackTrace()[2],module, function,  params);
		if (list.isEmpty()){
			retobject = CommunicationFactory.getErlangIf().call(module, function);
		}else{
			retobject = CommunicationFactory.getErlangIf().callWithList(module, function, new OtpErlangList(list.toArray(new OtpErlangObject[list.size()])));
		}
		BaseData data = Tools.convert(retobject);
		//ConvertFactory.checkError(data);
//		ErlangDebugLog.writeComLog(token,Thread.currentThread().getStackTrace()[2],System.currentTimeMillis() - timeLong,module, function, data, params);
		return data;
	}

	public static void main(String[] args) throws Exception{
		Object o = new ErlangDirectEccServiceImpl().call("erlang", "date");
		System.out.print(o);
	}
	public static void main2(String[] args) throws Exception{
		OtpSelf self = new OtpSelf("aaa");//, Configure.getConfigureValue(COOKIE));
		self.setCookie("3ren");
		OtpPeer other  = new OtpPeer("debug@itsm");
		OtpConnection con = self.connect(other);
		OtpErlangList params = new OtpErlangList(new OtpErlangObject[0]);
		con.sendRPC("erlang", "date", params);
		Object o = con.receive();
		System.out.print(o);
	}

	@Override
	public BaseData call(String node, String module, String function,
			BaseData... params) throws Exception {
		List<OtpErlangObject> list = new LinkedList<OtpErlangObject>();
		for(BaseData data : params){
			if (data instanceof OtpConvert){
				OtpConvert convert = (OtpConvert) data;
				list.add(convert.convert());
			}
		}
		OtpErlangObject retobject = null;
		long timeLong = System.currentTimeMillis();
		String token = "" + rand.nextLong();
//		ErlangDebugLog.writeBeginLog(token,Thread.currentThread().getStackTrace()[2],node,module, function,  params);
		if (list.isEmpty()){
			retobject = CommunicationFactory.getErlangIf().call(node, module, function);
		}else{
			retobject = CommunicationFactory.getErlangIf().callWithList(node, module, function, new OtpErlangList(list.toArray(new OtpErlangObject[list.size()])));
		}
		BaseData data = Tools.convert(retobject);
		//ConvertFactory.checkError(data);
//		ErlangDebugLog.writeComLog(token,Thread.currentThread().getStackTrace()[2],System.currentTimeMillis() - timeLong,node,module, function, data, params);
		return data;
	}

}
