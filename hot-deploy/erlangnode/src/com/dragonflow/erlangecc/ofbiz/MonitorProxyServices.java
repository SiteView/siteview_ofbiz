/*
 * Manage the mapping between the monitor proxy and monitors and machines.  Which monitors can be run on which proxy.
 * Can also allocate machines to proxy, all monitors related to a machine can be run from the proxy.
 * 
 * */

package com.dragonflow.erlangecc.ofbiz;

import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;


public class MonitorProxyServices 
{
    public final static String module = MonitorProxyServices.class.getName();

    public static Map<String, Object> SetProxyAndMonitorId(DispatchContext dctx, Map<String, ?> context)
    {       
        //1、解析出[{{proxyName=...}, {monitorId=...}} ...]
        String strProxyNode = (String)context.get("proxyNode");
        String strMonitorId = (String)context.get("monitorId");
        
        Map<String, Object> response = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        String strProxyId = "";
        Map<String, Object> rtnMapList;
        
        try
        {
        	GenericValue proxyValue = null;
//        	GenericValue proxyValue = delegator.findOne("Proxy", UtilMisc.toMap("erlangnode", strProxyNode), false);
        	List<GenericValue> listProxyValue = delegator.findList("Proxy", EntityCondition.makeCondition("erlangnode", EntityOperator.EQUALS, strProxyNode),null, null, null, false);
        	
            //2、proxyName是否存在？未存在则 Add Proxy ... [{proxyId, NextSeqId("Proxy"),{erlangnode,strProxyNode}, ... ] --> Proxy
//        	if(proxyValue == null)
        	if(listProxyValue.size() == 0)
			{        		
//				try 
//				{
//					rtnMapList = dispatcher.runSync("getNextId", UtilMisc.toMap("type", "Proxy"));
//	        		strProxyId = rtnMapList.get("nextSeqId").toString();
					strProxyId = delegator.getNextSeqId("Proxy"); 
					GenericValue CiType = delegator.makeValue("Proxy", UtilMisc.toMap("proxyId",strProxyId, "erlangnode", strProxyNode));
					delegator.create(CiType);
					proxyValue = CiType;
//				}
//				catch (GenericServiceException e) 
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
        	else
        	{        	
        		proxyValue = listProxyValue.get(0);
        		//存在则获取proxyId 
        		strProxyId = proxyValue.get("proxyId").toString();        		
        	}
        	
            //3、Add Or Update ?
//        	GenericValue operationProxyValue = delegator.findOne("OperationProxy", UtilMisc.toMap("proxyId", strProxyId, "ciId", strMonitorId), false);
        	List<EntityCondition> entityConditionList = FastList.newInstance();
        	entityConditionList.add(EntityCondition.makeCondition("proxyId", EntityOperator.EQUALS, strProxyId));
        	entityConditionList.add(EntityCondition.makeCondition("ciId", EntityOperator.EQUALS, strMonitorId));
        	List<GenericValue> listOperationProxyValue = delegator.findList("OperationProxy", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND),null, null, null, false);
        	if(listOperationProxyValue.size() == 0)        	
			{
        		//4、[{operationproxyId, NextSeqId("OperationProxy"), {{proxyId, ...}, {monitorId, ...}} ...] --> OperationProxy

//				try 
//				{
//					rtnMapList = dispatcher.runSync("getNextId", UtilMisc.toMap("type", "OperationProxy"));
//	        		String strOperationId = rtnMapList.get("nextSeqId").toString();
					String strOperationId = delegator.getNextSeqId("OperationProxy");
					GenericValue  operationProxy = delegator.makeValue("OperationProxy", UtilMisc.toMap("operationproxyId", strOperationId, "proxyId", strProxyId, "ciId", strMonitorId));
					delegator.create(operationProxy);					
//				}
//				catch (GenericServiceException e) 
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
        		
			}
		}
        catch (GenericEntityException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
        
        return response;
    }

    public static Map<String, Object> GetAllProxyAndMonitorId(DispatchContext dctx, Map<String, ?> context) 
    {   
        Map<String, Object> response = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        
        try
        {
        	//1、All OperationProxy
        	List<EntityCondition> entityConditionList = FastList.newInstance();        	
        	List<GenericValue> listOperationProxyValue = delegator.findList("OperationProxy", null,null, null, null, false);    		

        	 //2、 {ok,[{'0.2.3.1',master@itsm}, ...]
        	FastList OutDataList = FastList.newInstance();        	        	
        	for(GenericValue operationProxyValue : listOperationProxyValue)
        	{
        		FastMap<Object, Object> OutData = FastMap.newInstance();
        		OutData.put("monitorId" ,operationProxyValue.get("ciId"));
        		OutData.put("proxyId" ,operationProxyValue.get("proxyId"));
        		OutData.put("proxyName" ,operationProxyValue.getRelatedOne("Proxy").get("erlangnode").toString());
        		OutDataList.add(OutData);
        	}
        	
        	response.put("outData", OutDataList);
        }
        catch (GenericEntityException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
        
        return response;
    }
    
    public static Map<String, Object> GetMonitorIdByProxy(DispatchContext dctx, Map<String, ?> context) 
    {
        Map<String, Object> response = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        
        try
        {        
            //1、解析 {proxyId, ...}并查询OperationProxy
            String strProxyNode = (String)context.get("proxyNode");

            //2、  返回值 {ok,[{'0.2.3.1',master@itsm}, ...]
            List<EntityCondition> entityConditionList = FastList.newInstance();
        	entityConditionList.add(EntityCondition.makeCondition("proxyId", EntityOperator.EQUALS, strProxyNode));
        	List<GenericValue> listOperationProxyValue = delegator.findList("OperationProxy", EntityCondition.makeCondition(
        			entityConditionList, EntityOperator.AND),null, null, null, false);    		

        	FastList OutDataList = FastList.newInstance();
        	        	
        	for(GenericValue operationProxyValue : listOperationProxyValue)
        	{
        		FastMap<Object, Object> OutData = FastMap.newInstance();
        		OutData.put("monitorId" ,operationProxyValue.get("ciId"));
        		OutData.put("proxyId" ,operationProxyValue.get("proxyId"));
        		OutData.put("proxyName" ,operationProxyValue.getRelatedOne("Proxy").get("erlangnode").toString());
        		OutDataList.add(OutData);
        	}
        	
    	    response.put("outData", OutDataList);
		}
        catch (GenericEntityException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
        
        return response;
    }
    
    public static Map<String, Object> GetProxyByMonitorId(DispatchContext dctx, Map<String, ?> context) 
    {
        Map<String, Object> response = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        
        try
        {        
            //1、  解析 {ciId, MonitorId}并查询OperationProxy 
            String strMonitorId = (String)context.get("monitorId");

            //2、  返回值 {ok,[{'0.2.3.1',master@itsm}, ...]
            List<EntityCondition> entityConditionList = FastList.newInstance();
        	entityConditionList.add(EntityCondition.makeCondition("ciId", EntityOperator.EQUALS, strMonitorId));
        	List<GenericValue> listOperationProxyValue = delegator.findList("OperationProxy", EntityCondition.makeCondition(
        			entityConditionList, EntityOperator.AND),null, null, null, false);    		

        	FastList OutDataList = FastList.newInstance();
        	        	
        	for(GenericValue operationProxyValue : listOperationProxyValue)
        	{
        		FastMap<Object, Object> OutData = FastMap.newInstance();
        		OutData.put("monitorId" ,operationProxyValue.get("ciId"));
        		OutData.put("proxyId" ,operationProxyValue.get("proxyId"));
        		OutData.put("proxyName" ,operationProxyValue.getRelatedOne("Proxy").get("erlangnode").toString());
        		OutDataList.add(OutData);
        	}
        	
    	    response.put("outData", OutDataList);
		}
        catch (GenericEntityException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
        
        return response;
    }

    public static Map<String, Object> DelProxyAndMonitorId(DispatchContext dctx, Map<String, ?> context) 
    {
        Map<String, Object> response = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        
        try
        {        
            //1、解析并查询出[{{proxyId=...}, {monitorId=...}} ...]
            String strProxyNode = (String)context.get("proxyNode");
            String strMonitorId = (String)context.get("monitorId");
            
            //2、Delete
        	List<EntityCondition> entityConditionList = FastList.newInstance();
        	entityConditionList.add(EntityCondition.makeCondition("proxyId", EntityOperator.EQUALS, strProxyNode));
        	entityConditionList.add(EntityCondition.makeCondition("ciId", EntityOperator.EQUALS, strMonitorId));
        	List<GenericValue> listOperationProxyValue = delegator.findList("OperationProxy", EntityCondition.makeCondition(
        			entityConditionList, EntityOperator.AND),null, null, null, false);    		
        	delegator.removeAll(listOperationProxyValue);
		}
        catch (GenericEntityException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
        
        return response;
    }
    
    
    

    
}

