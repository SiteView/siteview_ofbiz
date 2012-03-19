package com.dragonflow.erlangecc.ofbiz;

import java.util.List;


public interface XmppInterface {	
	public Object call(String className, String method, List Params,int timeout) throws Exception;
	public Object call() throws Exception;
}
