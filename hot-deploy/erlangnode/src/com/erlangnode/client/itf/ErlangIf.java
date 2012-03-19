package com.erlangnode.client.itf;

import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;

public interface ErlangIf {	
	public OtpErlangObject call(String module,String function,OtpErlangObject...params) throws Exception;
	public OtpErlangObject call(String node, String module,String function,OtpErlangObject...params) throws Exception;
	public OtpErlangObject callWithList(String module,String function,OtpErlangList param) throws Exception;
	public OtpErlangObject callWithList(String node, String module,String function,OtpErlangList param) throws Exception;
}
