package com.erlangnode.client.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;

import com.ericsson.otp.erlang.OtpConnection;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.erlangnode.client.itf.ErlangIf;

public class ErlangIfImpl implements ErlangIf {
	private final Log log = LogFactory.getLog(ErlangIfImpl.class);
	private final static ObjectPool pool = new StackObjectPool(new ErlangOtpPoolFactory());
	private final static ObjectPool poolReport = new StackObjectPool(new ErlangReportOtpPoolFactory());
	private final static ObjectPool poolNnm = new StackObjectPool(new ErlangNnmOtpPoolFactory());
	private final static Object syncObject = "Sync Object";
	
	@Override
	public OtpErlangObject callWithList(String module,String function, OtpErlangList param)
			throws Exception {
		long timer = System.currentTimeMillis();
		//synchronized(syncObject)
		{
			OtpConnection conn = null;
			try{
				conn = (OtpConnection) pool.borrowObject();
				conn.sendRPC(module, function, param);
				OtpErlangObject retobj = conn.receiveRPC();
				return retobj;
			}finally{
				pool.returnObject(conn);
				log.debug(module + ":" + function + "  --> " + param + "  timelong=" +  (System.currentTimeMillis() - timer) + "mms");
			}
		}
	}

	@Override
	public OtpErlangObject call(String module, String function,
			OtpErlangObject... params) throws Exception {
		if (params.length == 0){
			return this.callWithList(module, function, new OtpErlangList());
		}
		return this.callWithList(module, function, new OtpErlangList(params));
	}

	@Override
	public OtpErlangObject callWithList(String node, String module,
			String function, OtpErlangList param) throws Exception {
		long timer = System.currentTimeMillis();		
		OtpConnection conn = null;
		if ("nnm".equals(node))  {
			try{
				conn = (OtpConnection) poolNnm.borrowObject();
				conn.sendRPC(module, function, param);
				OtpErlangObject retobj = conn.receiveRPC();
				return retobj;
			}finally{
				poolNnm.returnObject(conn);
				log.debug(module + ":" + function + "  --> " + param + "  timelong=" +  (System.currentTimeMillis() - timer) + "mms");
			}
		}
		else {
			try{
				conn = (OtpConnection) poolReport.borrowObject();
				conn.sendRPC(module, function, param);
				OtpErlangObject retobj = conn.receiveRPC();
				return retobj;
			}finally{
				poolReport.returnObject(conn);
				log.debug(module + ":" + function + "  --> " + param + "  timelong=" +  (System.currentTimeMillis() - timer) + "mms");
			}
		}
		
	}

	@Override
	public OtpErlangObject call(String node, String module, String function,
			OtpErlangObject... params) throws Exception {
		if (params.length == 0){
			return this.callWithList("", module, function, new OtpErlangList());
		}
		return this.callWithList("", module, function, new OtpErlangList(params));
	}

}
