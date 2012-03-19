package com.erlangnode.client.impl;


import svtype.BaseData;

public interface EccService {
	public BaseData call(String module,String function,BaseData...params)throws Exception;
	public BaseData call(String node,String module,String function,BaseData...params)throws Exception;
}
