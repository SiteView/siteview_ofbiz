package com.dragonflow.erlangecc.ofbiz;

import java.util.List;

import javolution.util.FastList;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.jivesoftware.smack.Chat;


public class XmppInterfaceImpl implements XmppInterface {
	private final static ObjectPool pool = new StackObjectPool(
			new XmppPoolFactory());
	private Chat chat=null;
	int timeout = 2000;
	
	public Object call(String className, String method, List Params,int timeout)
			throws Exception {
		List<Object> list = FastList.newInstance();
    	Object rtnObj = null;
    	list.add(className);
    	list.add(method);
    	list.add(Params);
		XMPPChannel channel = (XMPPChannel) pool.borrowObject();
    	try {
    		 rtnObj = channel.call(className, method, Params, timeout);
		} catch (Exception e) {
			e.printStackTrace();
		}
		pool.returnObject(chat);
    	return rtnObj;
	}

	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		XMPPChannel channel = (XMPPChannel) pool.borrowObject();
		pool.returnObject(channel);
		return channel;
	}

}
