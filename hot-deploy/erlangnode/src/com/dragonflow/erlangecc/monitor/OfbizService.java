package com.dragonflow.erlangecc.monitor;

import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.ofbiz.base.conversion.CollectionConverters;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.ObjectType;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactory;
import org.ofbiz.service.GenericDispatcher;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;


public class OfbizService extends BaseMonitor {
	public static final String OK = "ok";
	public static final String ERROR = "error";
	protected static Delegator delegator;
	protected static LocalDispatcher dispatcher;
	private Chat chat;

	
	private Map<String,String> getParams() throws Exception{
		return null;
	}
	
	public void setXmppChat(Chat chat) {
		this.chat = chat;
	}
	
	@Override
	public int handleMessage() {
	
		
		// get the delegator for this container
        delegator = DelegatorFactory.getDelegator("default");        

        
        ModelService service = null;
        String[] states = { "ok", "ok" };

        // create the LocalDispatcher
        dispatcher = GenericDispatcher.getLocalDispatcher("ErlangDispatcher", delegator);
        try {
        	System.out.println(service);
        	service = dispatcher.getDispatchContext().getModelService(this.getMessage().getAction());
		} catch (GenericServiceException e) {
			Debug.logError(e, module);
		}

		//action = servicename
		Object rtnObj=null,rtnMapObj=null;
		List<Object> rtnListObj=FastList.newInstance();
		Map<String, Object> inMapList=FastMap.newInstance();
		Class<?>[] signature = null; 
		
		try {
			Map<String, Object> inJavaMapList = (Map<String, Object>)this.getMessage().getOfbizParams();
			//make sure the input type is the same as the defined in services.xml
			//iterator thru the inParam and convert the type to the defined in services.xml
	        if (inJavaMapList.size() > 0) {
	            for (Map.Entry<String, ?> entry: inJavaMapList.entrySet()) {	  
	            	//Debug.logWarning("inJavaMapList is " + entry.getKey(), module);
	            	if(service.getParam(entry.getKey())!= null)
	            	{
		                String ofbizType = service.getParam(entry.getKey()).getType();
		                if(!(entry.getValue().getClass().getName().contains(ofbizType))) {
		                	Debug.logWarning("Expected type in services.xml is "+ ofbizType +", but input type is " + entry.getValue().getClass().getName() +", will try to convert.", module);
		                }
		                inMapList.put(entry.getKey(), ObjectType.simpleTypeConvert(entry.getValue(), ofbizType, null, null));
	            	}
	            }
	        }
	        
	        //call service
	        rtnObj = dispatcher.runSync(this.getMessage().getAction(),inMapList );
	        
	        this.sendOfbizResponse("success",rtnObj);	  
	        
		} catch (GenericServiceException e) {
			Debug.logError(e, module);
		} catch (GeneralException e) {
			Debug.logError(e, module);
		} catch (Exception e) {
			// other eroors
			states[0] = ERROR;
			states[1] = "unknow reason" + e.getMessage();
			e.printStackTrace();
		}
		return 0;
		
	}

}
