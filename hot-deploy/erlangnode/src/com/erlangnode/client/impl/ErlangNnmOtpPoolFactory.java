package com.erlangnode.client.impl;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;

import com.ericsson.otp.erlang.OtpConnection;
import com.ericsson.otp.erlang.OtpPeer;
import com.ericsson.otp.erlang.OtpSelf;
import com.erlangnode.client.impl.Configure;

public class ErlangNnmOtpPoolFactory extends BasePoolableObjectFactory {

	private static final Log loger = LogFactory.getLog(ErlangOtpPoolFactory.class);
	
	private static final String NODE_NAME = "NnmNodeName";
	private static final String COOKIE = "Cookie";
	public static final String PEER_NODE = "PeerNnmNode";
	
	private static OtpPeer other = null;
	private static final List<String> keys = new CopyOnWriteArrayList<String>();

	@Override
	public synchronized Object makeObject() throws Exception {
		String nodename = getNextSelfNodeName();

		OtpSelf self = new OtpSelf(nodename);
		self.setCookie(Configure.getConfigureValue(COOKIE));

		if (other == null )
			other = new OtpPeer(Configure.getConfigureValue(PEER_NODE));
		OtpConnection conn = self.connect(other);
		loger.info("makeObject nodename=" + nodename);
		keys.add(nodename);
		return conn;
	}
	
	private String getNextSelfNodeName() throws Exception{
		String nodeName = Configure.getConfigureValue(NODE_NAME);
		if (nodeName == null) nodeName = Configure.getVersionTitle();
		Random random = new Random(System.currentTimeMillis());
		while(true){
			String key =  nodeName + "_" + random.nextInt();
			if (keys.contains(key))continue;
			return key;
		}
	}

	@Override
	public boolean validateObject(Object obj) {
		if (obj == null) return false;
		OtpConnection conn = (OtpConnection) obj;
	    return conn.isConnected();
	}
	
	@Override
	public void destroyObject(Object obj) throws Exception {
		if (obj == null) return;
		OtpConnection conn = (OtpConnection) obj;
		if (conn.isConnected()){
			conn.close();
		}
	}
}
