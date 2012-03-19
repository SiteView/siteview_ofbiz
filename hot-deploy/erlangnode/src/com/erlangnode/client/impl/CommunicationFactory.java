package com.erlangnode.client.impl;

//import com.caucho.hessian.client.HessianProxyFactory;
import com.erlangnode.client.impl.EccService;
//import com.erlangnode.client.impl.EccServiceImpl;
//import com.erlangnode.client.impl.JSonService;
//import com.erlangnode.client.impl.RestIf;
//import com.erlangnode.client.impl.RestIfImpl;
import com.erlangnode.client.impl.Configure;
import com.erlangnode.client.impl.ErlangIfImpl;
import com.erlangnode.client.itf.ErlangIf;

public class CommunicationFactory {

//	public static JSonService getJSonService() throws Exception {
//		String url = Configure.getConfigureValue("remoteurl");
//
//		HessianProxyFactory factory = new HessianProxyFactory();
//		JSonService basic = (JSonService) factory.create(JSonService.class, url);
//		return basic;
//	}
	private static ErlangIf eif = null;
	public static ErlangIf getErlangIf() throws Exception
	{
		if (eif == null){
			try{
				String classname = Configure.getConfigureValue("ErlangImplClassName");
				Class<?> classz = Class.forName(classname);
				eif = (ErlangIf) classz.newInstance();
			}catch(Exception e){
				eif = new ErlangIfImpl();
			}
			
		}
		return eif;
	}	
	
	private static EccService eccService = null;
	private static EccService nnmService = null;
	public static EccService getEccService()
	{
		if (eccService == null){
			try{
				String classname = Configure.getConfigureValue("Connection_ClassName");

				Class<?> classz = Class.forName(classname);
				eccService = (EccService) classz.newInstance();
			}catch(Exception e){
//				eccService = new EccServiceImpl();
				eccService = null;
				e.printStackTrace();
			}
		}
		return eccService;
	}
	
	public static EccService getNnmService()
	{
		if (nnmService == null){
			try{
				String classname = Configure.getConfigureValue("Connection_ClassName");
				Class<?> classz = Class.forName(classname);
				nnmService = (EccService) classz.newInstance();
			}catch(Exception e){
//				nnmService = new EccServiceImpl();
				nnmService = null;
				e.printStackTrace();
			}
		}
		return nnmService;
	}


//	private static RestIf restIf = null;
//	public static RestIf getRestIf()
//	{
//		if (restIf == null){
//			restIf = new RestIfImpl();
//		}
//		return restIf;
//	}
}
