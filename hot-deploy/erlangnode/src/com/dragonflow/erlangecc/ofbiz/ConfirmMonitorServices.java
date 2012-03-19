package com.dragonflow.erlangecc.ofbiz;

import java.util.Map;

import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

public class ConfirmMonitorServices {

	
	public static Map<String, Object> confirmMonitor(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		String tablename = (String) context.get("tablename");
		String mid = (String) context.get("mid");
		String monitorname = (String) context.get("monitorname");
		String groupname = (String) context.get("groupname");
		String monitordescripe = (String) context.get("monitordescripe");
		String monitorlogdescripe = (String) context.get("monitorlogdescripe");
		String category = (String) context.get("category");
		
		//insert master data
				Delegator delegator = dctx.getDelegator();		
				String Id = delegator.getNextSeqId(tablename);		
				
		    	//erlang Params set in monitor.erl
		        Map<String, String> fields = UtilMisc.toMap("id", Id,
		          "mid",mid,
		          "monitorname",monitorname,
		          "confirmtime",UtilDateTime.nowTimestamp(),
		          "groupname",groupname,
		          "category",category,
		          "monitordescripe",monitordescripe,
		          "monitorlogdescripe",monitorlogdescripe
		          );
		        
				GenericValue log = delegator.makeValue(tablename, fields);
				delegator.create(log);
		
		
		
		return response;
	}
}
