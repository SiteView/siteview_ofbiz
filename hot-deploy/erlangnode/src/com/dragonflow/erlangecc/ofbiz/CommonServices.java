package com.dragonflow.erlangecc.ofbiz;

import java.util.Collections;
import java.util.Comparator;
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
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;


/**
 * Common Services
 */
public class CommonServices {

    public final static String module = CommonServices.class.getName();
    
    public static class ComparatorId implements Comparator{

    	 public int compare(Object arg0, Object arg1) {
//    	  String str1=(String)arg0;
//    	  String str2=(String)arg1;
//    	  
//    	  if(Integer.parseInt(str1) >= Integer.parseInt(str2))
//    		  return 1;
//    	  else
//    		  return 0;
       	  int n1=(Integer)arg0;
       	  int n2=(Integer)arg1;
       	  
       	  if(n1 >= n2)
       		  return 1;
       	  else
       		  return 0;
    	 }
    }  
    
    public static Map<String, Object> NextSeqId(DispatchContext dctx, Map<String, ?> context)
    {
    	//CIType
    	String strTable = (String)context.get("type");

    	Map<String, Object> response = ServiceUtil.returnSuccess();
//        response.put("resp", "ok");
        Delegator delegator = dctx.getDelegator();
        
        try
        {	
        	//get all CI based on CIType
        	List<GenericValue> listCI = delegator.findByAnd("CI", UtilMisc.toMap("ciTypeId", strTable));        	
        	if(listCI.size() > 0)
        	{
        		
        		//get all id of the same CiType
        		List<EntityCondition> entityConditionList = FastList.newInstance();
        		List<String> ciIdList = EntityUtil.getFieldListFromEntityList(listCI, "ciId", true);
        		entityConditionList.add(EntityCondition.makeCondition("ciId", EntityOperator.IN, ciIdList));        		
        		entityConditionList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("attrName", EntityOperator.EQUALS, "id")));        				
        	   	EntityCondition attributecondition = EntityCondition.makeCondition(entityConditionList, EntityOperator.AND);
               	
//               Debug.logInfo("---- SVC-CONTEXT QueryConditon -----------------attributecondition: " + attributecondition.toString(),module);
               	List<GenericValue> listValue = delegator.findList("CIAttribute", attributecondition, UtilMisc.toSet("attrObjValue"), null,  null, false);
               	List<String> listId = EntityUtil.getFieldListFromEntityList(listValue, "attrObjValue", true);
               	
               	if(listId.size() > 0)
               	{
               		//ci_0... -> 0...
               		//Collections.replaceAll(listId, strTable + "_", "");
               		List<Integer> listInt = FastList.newInstance();
               		for(String stId:listId)
               		{
               			listInt.add(Integer.parseInt(stId.replaceAll(strTable + "_", "")));
               		}
               		
               		Collections.sort(listInt, new ComparatorId());
               		
               	    int nStep = listInt.get(0);
               	    int nMaxId = 0; 
               	    boolean bMax = true;
               		
               	    //get the middle value of the id, e.g. NextSeqId: 1,3,4... --> 2
               		for(int nId : listInt)
               		{
               			if((nId - nStep) > 1)
               			{
               				bMax = false;
               				nMaxId = nId-1;
               				break;
               			}
               			else
               			{
               				nStep = nId;
               			}
               		}	                
               		
               		//get the max value of the id
               		if(bMax)
               		{
//	              		Collections.max(listId,  new ComparatorId());               		
		               	String strMaxId = listInt.get(listInt.size()-1).toString();
		               	nMaxId = (Integer.parseInt(strMaxId) + 1);		               	
               		}               		
               		
               		response.put("nextSeqId", strTable + "_" + String.format("%d", nMaxId));
               	}
               	else
               	{
               		//initial value
               		response.put("nextSeqId", strTable + "_0");
               	}
        	}
        	else
        	{        		
        		//initial value
        		response.put("nextSeqId", strTable + "_0");
        	}        
		}
	    catch (GenericEntityException e) 
	    {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
    
	    return response;
    }
    

    public static Map<String, Object> DbcsCreate(DispatchContext dctx, Map<String, ?> context) 
    {
//    	System.out.println("---- SV-CONTEXT -----------------: DbcsCreate!!!!" );
    	
    	String strTable = (String)context.get("table");
        FastList mapInData = (FastList) context.get("inData");
        
        if (context.size() > 0) 
        {
            for (Map.Entry<String, ?> entry: context.entrySet()) 
            {
                Object cKey = entry.getKey();
                Object value = entry.getValue();

//                System.out.println("---- SV-CONTEXT -----------------: " + cKey + " => " + value + " => " + value.getClass().toString());
//                Debug.logInfo("DbcsCreate parameter: " + cKey + " => " + value,module);
            }
        }
        
        Map<String, Object> response = ServiceUtil.returnSuccess();
        response.put("resp", "ok");
        Delegator delegator = dctx.getDelegator();
        
        try
        {        
			//add CIType is not exist
        	if(delegator.findByPrimaryKey("CIType", UtilMisc.toMap("ciTypeId", strTable)) == null)
			{
				GenericValue CiType = delegator.makeValue("CIType", UtilMisc.toMap("ciTypeId", strTable));
				delegator.create(CiType);
			}
	        	
        	//add data to CI entity
    	    String strNextId = delegator.getNextSeqId("CI");
    	    
            //add data to CIAttribute entity	    	    
    	    for(Object obj : mapInData)
    	    {
    	    	Map<String, Object> obj1 = (Map<String, Object>) obj; 
        		{        			
        			if(strTable.equals("group") || strTable.equals("monitor"))
        			{
                		if(obj1.containsKey("id"))
                		{
	        				strNextId = obj1.get("id").toString();
		        			GenericValue Ci = delegator.makeValue("CI", UtilMisc.toMap("ciId", strNextId, "ciTypeId", strTable));
		            	    GenericValue retCi = delegator.create(Ci);	    	    
		
		            	    retCi.set("ciName", obj1.get("id").toString());
		        			retCi.store();
	
		        			retCi.set("ciId", obj1.get("id").toString());
		        			retCi.store();
                		}
                		else
                		{
    	        			GenericValue Ci = delegator.makeValue("CI", UtilMisc.toMap("ciId", strNextId, "ciTypeId", strTable));
    	            	    GenericValue retCi = delegator.create(Ci);	
    	            	    
                			retCi.set("ciName", strNextId);
                			retCi.store();	                			
                		}
        			}
        			else
        			{
	        			//strNextId = obj1.get("id").toString();
	        			GenericValue Ci = delegator.makeValue("CI", UtilMisc.toMap("ciId", strNextId, "ciTypeId", strTable));
	            	    GenericValue retCi = delegator.create(Ci);	
	            	    
                		if(obj1.containsKey("id"))
                		{
                			retCi.set("ciName", obj1.get("id").toString());
                			retCi.store();	                			
                		}
        			}
        		}

        		
                for (Map.Entry<String, ?> entry : obj1.entrySet()) 
                {
                    Object cKey = entry.getKey();
                    Object value = entry.getValue();
            		GenericValue CiAttribute;
            		
            		if(cKey.toString().contains(".ini") && !(value instanceof FastList))
            		{
            			FastList listIni = FastList.newInstance();
            			listIni.add(value);
            			CiAttribute = delegator.makeValue("CIAttribute", UtilMisc.toMap("ciId",strNextId, "attrName", cKey.toString(), "attrObjValue", listIni));
        			}
            		else
            		{	            		
//	            		if(value instanceof String)	            			
//	            			CiAttribute = delegator.makeValue("CIAttribute", UtilMisc.toMap("ciId",strNextId, "attrName", cKey.toString(), "attrValue", value.toString()));
//	            		else
	            			CiAttribute = delegator.makeValue("CIAttribute", UtilMisc.toMap("ciId",strNextId, "attrName", cKey.toString(), "attrObjValue", value));
            		}
            		
            		delegator.create(CiAttribute);
                }
    	   }
    	    
    	    //return the value if id == ciId
    	    FastMap<Object, Object> OutData = FastMap.newInstance();
    		if(strTable.equals("machine") || strTable.equals("schedule"))
    		{
    			((Map<String, Object>) mapInData.get(0)).put("id", strNextId);
    		}
    		else
    		{
    			((Map<String, Object>) mapInData.get(0)).put("ciId", strNextId);
    		}
    		
    	    response.put("outData", mapInData);
    	    Debug.logInfo("DbcsCreate finsh", module);
		}
        catch (GenericEntityException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
        
        return response;
    }
    
    public static Map<String, Object> DbcsRead(DispatchContext dctx, Map<String, ?> context) 
    {
//    	System.out.println("---- SV-CONTEXT -----------------: DbcsRead!!!!" );
    	
        String strTable = (String)context.get("table");
        FastList mapInData = (FastList) context.get("inData");
        
        if (context.size() > 0) 
        {
            for (Map.Entry<String, ?> entry: context.entrySet()) 
            {
                Object cKey = entry.getKey();
                Object value = entry.getValue();

//                System.out.println("---- SV-CONTEXT -----------------: " + cKey + " => " + value + " => " + value.getClass().toString());
//                Debug.logInfo("DbcsRead parameter: " + cKey + " => " + value,module);
            }
        }
        
        Map<String, Object> response = ServiceUtil.returnSuccess();
        response.put("resp", "ok");
        Delegator delegator = dctx.getDelegator();
        
        try
        {        
        	
        	String strQuery = "";
        	if(strTable.equals("group") || strTable.equals("monitor"))
        		strQuery = mapInData.get(0).toString().replace("'", "").replace("<", "").replace(">", "").replace("type", "class");
        	else
        		strQuery = mapInData.get(0).toString().replace("'", "").replace("<", "").replace(">", "");
        	
        	//.replace("id", "ciId")
        	
        	//parse from=i&t0=j&order=x condition
        	Map<String, Object> mapOtherCondition = null;
        	if(mapInData.size() > 1)
        	{
        		//parse from=i&t0=j&order=x condition
        		String strQueryOther = mapInData.get(1).toString().replace("'", "").replace("<", "").replace(">", "");	        		
        		
        		mapOtherCondition = AnalysisOtherCondition(strQueryOther);
        	}	        	
        	
        	//query CI record
        	List<GenericValue> listCiId = QueryCiByCondition(delegator, strQuery, strTable, mapOtherCondition);
        	
        	//query entity based on ciId and prepare the return value
        	FastList OutDataList = FastList.newInstance();
        	FastMap<Object, Object> OutData = FastMap.newInstance();

        	if(listCiId != null)
        	{
		    	for(GenericValue obj : listCiId)
		    	{
		    		List<GenericValue> CiObjs = obj.getRelatedOne("CI").getRelated("CIAttribute");			
		    		
				   // println("Read CIAttribute From eee Db ---> CiObjs:" + CiObjs);
				    //OutData.ciId = CIAttribute.ciId;
			    	if(CiObjs.size() > 0)
			    	{
			    		if(strTable.equals("machine") || strTable.equals("schedule"))
			    			OutData.put("id", obj.get("ciId"));
			    	
		    		    for(GenericValue ciObj : CiObjs)
		    		    {	
//		                    if(strTable.equals("elecc_perferences") && ciObj.get("attrName").toString().equals("id"))
//		    		    		continue;
		                    
		    		    	//
			    			if(ciObj.get("attrValue") != null && ciObj.get("attrObjValue") == null)
			    				OutData.put(ciObj.get("attrName"), ciObj.get("attrValue"));
			    			else if(ciObj.get("attrObjValue") != null && ciObj.get("attrValue") == null)
			    				OutData.put(ciObj.get("attrName"), ciObj.get("attrObjValue"));
			    			else
			    				OutData.put(ciObj.get("attrName"), "");
		    		    }
		
		    		    OutDataList.add(OutData);
			    	}
			    	
				    OutData = FastMap.newInstance();			    		
		    	}
        	}
        	
        	response.put("outData", OutDataList);            	
//        	Debug.logInfo("DbcsRead finsh", module);
		}
        catch (GenericEntityException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
        
        return response;        
    }      

    public static Map<String, Object> DbcsUpdate(DispatchContext dctx, Map<String, ?> context) 
    {
//    	System.out.println("---- SV-CONTEXT -----------------: DbcsUpdate!!!!" );
    	
        String strTable = (String)context.get("table");
        FastList mapInData = (FastList) context.get("inData");
        
        if (context.size() > 0) 
        {
            for (Map.Entry<String, ?> entry: context.entrySet()) 
            {
                Object cKey = entry.getKey();
                Object value = entry.getValue();

//                System.out.println("---- SV-CONTEXT -----------------: " + cKey + " => " + value + " => " + value.getClass().toString());
//                Debug.logInfo("DbcsUpdate parameter: " + cKey + " => " + value,module);
            }
        }
        
        Map<String, Object> response = ServiceUtil.returnSuccess();
        response.put("resp", "ok");
        Delegator delegator = dctx.getDelegator();
        
        try
        {        
        	//clone a new set of data for update (prepare for new data type field)
        	Map<String, Object> inData = FastMap.newInstance();
//        	inData.putAll((Map<String, Object>) mapInData.get(0));	        	
        	Map<String, Object> objTmp  = (Map<String, Object>) mapInData.get(0);
            for (Map.Entry<String, ?> entry : objTmp.entrySet()) 
            {
                Object cKey = entry.getKey();
                Object value = entry.getValue();
		    	
                if(strTable.equals("elecc_perferences") && cKey.toString().equals("id"))
		    		continue;
                
                inData.put(cKey.toString(), value);
            }
            
        	//update data
            if(inData.containsKey("ciId"))
        	{	
        		//1. id ciId existed, then update based on ciId.
	        	String strciId = inData.get("ciId").toString();
	        	
	        	//Old Attribute update
	        	List<GenericValue> listCIAttribute = delegator.findByAnd("CIAttribute", UtilMisc.toMap("ciId", strciId));
        		for(GenericValue attrObj : listCIAttribute)
        		{       			
        			String strKey = attrObj.get("attrName").toString();
        			if(inData.containsKey(strKey))
        			{
//        				 if(inData.get(strKey) instanceof String)
//        					 attrObj.set("attrValue", inData.get(strKey));
//        				 else
        					 attrObj.set("attrObjValue", inData.get(strKey));
        				 attrObj.store();        				 
        				 inData.remove(strKey);
        			}
        		}
        		
        		//if new Attribute, add to CIAttribute
        		for (Map.Entry<String, ?> entry : inData.entrySet())
        		{
                    Object cKey = entry.getKey();
                    Object value = entry.getValue();
            		GenericValue CiAttribute;
//            		if(value instanceof String)
//            			CiAttribute = delegator.makeValue("CIAttribute", UtilMisc.toMap("ciId",strciId, "attrName", cKey.toString(), "attrValue", value.toString()));
//            		else
            			CiAttribute = delegator.makeValue("CIAttribute", UtilMisc.toMap("ciId",strciId, "attrName", cKey.toString(), "attrObjValue", value));            			
            		delegator.create(CiAttribute);
        		}
        	}
        	else
        	{
              //2. if no ciId, first query for CI based on where condition, then update
	        	String strQuery = "";
	        	if(strTable.equals("group") || strTable.equals("monitor"))
	        		strQuery = mapInData.get(1).toString().replace("'", "").replace("<", "").replace(">", "").replace("type", "class");
	        	else
	        		strQuery = mapInData.get(1).toString().replace("'", "").replace("<", "").replace(">", "");
	        	//.replace("id", "ciId")		        	

	        	List<GenericValue> listCiId = QueryCiByCondition(delegator, strQuery, strTable, null);
	        	if(listCiId != null)
	        	{		            
		            //updated based on ciId
			    	for(GenericValue obj : listCiId)
			    	{
			    		String strciId = obj.get("ciId").toString();
				        	
			        	//Old Attribute update
			        	List<GenericValue> listCIAttribute = delegator.findByAnd("CIAttribute", UtilMisc.toMap("ciId", strciId));
		        		for(GenericValue attrObj : listCIAttribute)
		        		{
		        			String strKey = attrObj.get("attrName").toString();
		        			if(inData.containsKey(strKey))
		        			{	        				
//		        				 if(inData.get(strKey) instanceof String)
//		        					 attrObj.set("attrValue", inData.get(strKey));
//		        				 else
		        					 attrObj.set("attrObjValue", inData.get(strKey));
			            		
		        				attrObj.store();        				 
		        				inData.remove(strKey);
		        			}
		        		}
		        		
		        		//if New Attribute, then add a CIAttribute
		        		for (Map.Entry<String, ?> entry : inData.entrySet())
		        		{
		                    Object cKey = entry.getKey();
		                    Object value = entry.getValue();
		            		GenericValue CiAttribute;

//		            		if(value instanceof String)
//		            			CiAttribute = delegator.makeValue("CIAttribute", UtilMisc.toMap("ciId",strciId, "attrName", cKey.toString(), "attrValue", value.toString()));
//		            		else
		            			CiAttribute = delegator.makeValue("CIAttribute", UtilMisc.toMap("ciId",strciId, "attrName", cKey.toString(), "attrObjValue", value));
		            		
		            		delegator.create(CiAttribute);
		        		}
			    	}
	        	}
	        	else
	        	{
		        	//add new data to CI entity
		    	    String strNextId = delegator.getNextSeqId("CI");
		    	    GenericValue Ci = delegator.makeValue("CI", UtilMisc.toMap("ciId", strNextId, "ciTypeId", strTable));
		    	    GenericValue retCi = delegator.create(Ci);	    	    
		    	    
		        	//add new data to CIAttribute entity	    	    
//		    	    for(Object obj : mapInData)
		    	    {
		    	    	Map<String, Object> obj1 = inData;//(Map<String, Object>) obj; 
	            		if(obj1.containsKey("id"))
	            		{
	            			retCi.set("ciName", obj1.get("id").toString());
	            			retCi.store();	            		
	            		}
	            		
		                for (Map.Entry<String, ?> entry : obj1.entrySet()) 
		                {
		                    Object cKey = entry.getKey();
		                    Object value = entry.getValue();
		            		GenericValue CiAttribute;
		            		
		            		if(cKey.toString().contains(".ini") && !(value instanceof FastList))
		            		{
		            			FastList listIni = FastList.newInstance();
		            			listIni.add(value);
		            			CiAttribute = delegator.makeValue("CIAttribute", UtilMisc.toMap("ciId",strNextId, "attrName", cKey.toString(), "attrObjValue", listIni));
	            			}
		            		else
		            		{	            		
//			            		if(value instanceof String)	            			
//			            			CiAttribute = delegator.makeValue("CIAttribute", UtilMisc.toMap("ciId",strNextId, "attrName", cKey.toString(), "attrValue", value.toString()));
//			            		else
			            			CiAttribute = delegator.makeValue("CIAttribute", UtilMisc.toMap("ciId",strNextId, "attrName", cKey.toString(), "attrObjValue", value));
		            		}
		            		
		            		delegator.create(CiAttribute);
		                }
		    	   }
		    	    
		    	    //return valueif id == ciId
		    	    FastMap<Object, Object> OutData = FastMap.newInstance();
		    		if(strTable.equals("machine") || strTable.equals("schedule"))
		    		{
		    			((Map<String, Object>) mapInData.get(0)).put("id", strNextId);
		    		}
		    		else
		    		{
		    			((Map<String, Object>) mapInData.get(0)).put("ciId", strNextId);
		    		}
		    		
//		    	    response.put("outData", mapInData);
	        		
	        	}
        	}
        	
        	//ciId have problems ?
            FastList OutDataList = FastList.newInstance();
            OutDataList.add(mapInData.get(0));
            Debug.logInfo("DbcsUpdate finsh", module);
            response.put("outData", OutDataList);

		}
        catch (GenericEntityException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
        
        return response;           
    }
    
    public static Map<String, Object> DbcsDelete(DispatchContext dctx, Map<String, ?> context) 
    {
//    	System.out.println("---- SV-CONTEXT -----------------: DbcsDelete!!!!" );
    	
        String strTable = (String)context.get("table");
        FastList mapInData = (FastList) context.get("inData");
        
        if (context.size() > 0) 
        {
            for (Map.Entry<String, ?> entry: context.entrySet()) 
            {
                Object cKey = entry.getKey();
                Object value = entry.getValue();
//                Debug.logInfo("DbcsDelete parameter: " + cKey + " => " + value,module);

//                System.out.println("---- SV-CONTEXT -----------------: " + cKey + " => " + value );
            }
        }
        Map<String, Object> response = ServiceUtil.returnSuccess();
        response.put("resp", "ok");
        Delegator delegator = dctx.getDelegator();
        
        try
        {        
        	//1. query CI record for delete,  based on the where condition
        	String strQuery = "";
        	if(strTable.equals("group") || strTable.equals("monitor"))
        		strQuery = mapInData.get(0).toString().replace("'", "").replace("<", "").replace(">", "").replace("type", "class");
        	else
        		strQuery = mapInData.get(0).toString().replace("'", "").replace("<", "").replace(">", "");

        	//.replace("id", "ciId")	        	
        	List<GenericValue> listObj = QueryCiByCondition(delegator, strQuery, strTable, null);
        	if(listObj != null)
        	{
	            //delete record based on ciId, (single or batch)
		    	for(GenericValue obj : listObj)
		    	{
	        		//1. if ciId is not empty, delete
		        	String strciId = obj.get("ciId").toString();
		        	
		        	//delete related CIAttribute record
		        	List<GenericValue> listCIAttribute = delegator.findByAnd("CIAttribute", UtilMisc.toMap("ciId", strciId));		        	
		        	delegator.removeAll(listCIAttribute);
		        	
		        	//delete CIs
		        	List<GenericValue> listCI = delegator.findByAnd("CI", UtilMisc.toMap("ciId", strciId));		        	
		        	delegator.removeAll(listCI);
	        	}
        	}
        	Debug.logInfo("DbcsDelete finsh", module);
        	response.put("outData", context.get("inData"));
		}
        catch (GenericEntityException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
        
        return response;
    }
    
    //query for CI entity
    public static List<GenericValue> QueryCiByCondition(Delegator delegator, String strQuery, String strTable, Map<String, Object> mapOther) throws GenericEntityException
    {
    	List<GenericValue> listCiId;
    	//1. parse condition
        java.util.StringTokenizer queryTokens = new java.util.StringTokenizer(strQuery, "&");
        
        Map<String, Object> paramMapEqual = FastMap.newInstance();
        Map<String, Object> paramMapNoEqual = FastMap.newInstance();
        Map<String, Object> paramMapLike = FastMap.newInstance();
        Map<String, Object> paramMapIn = FastMap.newInstance();
        
        while (queryTokens.hasMoreTokens())
        {
            String token = queryTokens.nextToken();
            
            String name = token;
            int noequalsIndex = token.indexOf("!=");
            int likeIndex = token.indexOf(" like ");
            int inIndex = token.indexOf(" in ");
            
            if (likeIndex > 0)
            {
                name = token.substring(0, likeIndex).trim();
//                if(!token.substring(likeIndex + 6).trim().equals("''") && !token.substring(likeIndex + 6).trim().equals(""))
                	paramMapLike.put(name.replace("my.", "").trim(), token.substring(likeIndex + 6).trim());
//                	paramMapLike.put(name.replace("my.", "").trim(), "atom_" + token.substring(likeIndex + 6).trim());
            }	                	
            else if (noequalsIndex > 0) 
            {
                name = token.substring(0, noequalsIndex).trim();
//                if(!token.substring(noequalsIndex + 2).trim().equals("''") && !token.substring(noequalsIndex + 2).trim().equals(""))
                	paramMapNoEqual.put(name.replace("my.", "").trim(), token.substring(noequalsIndex + 2).trim());
//                	paramMapNoEqual.put(name.replace("my.", "").trim(), "atom_" + token.substring(noequalsIndex + 2).trim());
                	
            }
            else if (inIndex > 0) 
            {
                name = token.substring(0, inIndex).trim();
//                if(!token.substring(inIndex + 4).trim().equals("''") && !token.substring(inIndex + 4).trim().equals(""))
                	paramMapIn.put(name.replace("my.", "").trim(), token.substring(inIndex + 4).trim());
//                	paramMapIn.put(name.replace("my.", "").trim(), "atom_" + token.substring(inIndex + 4).trim());
                	
            }            
            else
            {
                int equalsIndex = token.indexOf("=");
                
                if (equalsIndex > 0) {
                    name = token.substring(0, equalsIndex).trim();
//                    if(!token.substring(equalsIndex + 1).trim().equals("''") && !token.substring(equalsIndex + 1).trim().equals(""))
                    	paramMapEqual.put(name.replace("my.", "").trim(), token.substring(equalsIndex + 1).trim());
//                    	paramMapEqual.put(name.replace("my.", "").trim(), "atom_" + token.substring(equalsIndex + 1).trim());                    	
                }
            }
        }
    	
        //2. filter record based on query string
        
        //2.1 create query condition
    	List<EntityCondition> entityConditionList = FastList.newInstance();

        //simple and test, default should be the query ciIdList based on strTypeId (first, avoid Or case)
        //condition priority is from left to right, not considering priority of ()  
//        	entityConditionList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("attrName", EntityOperator.EQUALS, "host"), 
//            		EntityOperator.AND, EntityCondition.makeCondition("attrObjValue", EntityOperator.EQUALS, "\\\\192.168.0.49")));

        for (Map.Entry<String, ?> entry : paramMapEqual.entrySet()) 
        {
              String cKey = entry.getKey().toString();
              String value = entry.getValue().toString();
              if(cKey.equals("id") && (strTable.equals("machine") || strTable.equals("schedule")))
              {
            	  entityConditionList.add(EntityCondition.makeCondition("ciId", EntityOperator.EQUALS, value));
//            	  entityConditionList.add(EntityCondition.makeCondition("ciId", EntityOperator.EQUALS, "atom_" + value));
              }
              else		            	  
              {
            	  entityConditionList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("attrName", EntityOperator.EQUALS, cKey), 
	            		  EntityOperator.AND, EntityCondition.makeCondition("attrObjValue", EntityOperator.EQUALS, value)));

//            	  entityConditionList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("attrName", EntityOperator.EQUALS, cKey), 
//	            		  EntityOperator.AND, EntityCondition.makeCondition("attrObjValue", EntityOperator.EQUALS, "atom_" + value)));            	  
              }
        }

        for (Map.Entry<String, ?> entry : paramMapNoEqual.entrySet()) 
        {
              String cKey = entry.getKey().toString();
              String value = entry.getValue().toString();
              if(cKey.equals("id") && (strTable.equals("machine") || strTable.equals("schedule")))
              {
            	  entityConditionList.add(EntityCondition.makeCondition("ciId", EntityOperator.NOT_EQUAL, value));
//            	  entityConditionList.add(EntityCondition.makeCondition("ciId", EntityOperator.NOT_EQUAL, "atom_" + value));
              }
              else
              {
            	  entityConditionList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("attrName", EntityOperator.EQUALS, cKey), 
            			  EntityOperator.AND, EntityCondition.makeCondition("attrObjValue", EntityOperator.NOT_EQUAL, value)));
            	  
//            	  entityConditionList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("attrName", EntityOperator.EQUALS, cKey), 
//            			  EntityOperator.AND, EntityCondition.makeCondition("attrObjValue", EntityOperator.NOT_EQUAL, "atom_" + value)));            	  
              }
        }

        for (Map.Entry<String, ?> entry : paramMapLike.entrySet()) 
        {
              String cKey = entry.getKey().toString();
              String value = entry.getValue().toString();
              if(cKey.equals("id") && (strTable.equals("machine") || strTable.equals("schedule")))
              {
            	  entityConditionList.add(EntityCondition.makeCondition("ciId", EntityOperator.LIKE, value));
//            	  entityConditionList.add(EntityCondition.makeCondition("ciId", EntityOperator.LIKE, "atom_" + value));
              }
              else
              {
            	  entityConditionList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("attrName", EntityOperator.EQUALS, cKey), 
            			EntityOperator.AND, EntityCondition.makeCondition("attrObjValue", EntityOperator.LIKE, value)));
            	  
//            	  entityConditionList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("attrName", EntityOperator.EQUALS, cKey), 
//              			EntityOperator.AND, EntityCondition.makeCondition("attrObjValue", EntityOperator.LIKE, "atom_" + value)));            	  
              }
        }
        
        for (Map.Entry<String, ?> entry : paramMapIn.entrySet()) 
        {
              String cKey = entry.getKey().toString();
              String value = entry.getValue().toString();//[SWITCH, ROUTER_SWITCH, ...]
              List<String> listIn = FastList.newInstance();
              List<String> listAtomIn = FastList.newInstance();
              
//              for(String strTmp : value.substring(1, value.length() -1).split(","))
//              {
//            	  listIn.add(strTmp);
//            	  listAtomIn.add("atom_" + strTmp);
//              }
               
              if(cKey.equals("id") && (strTable.equals("machine") || strTable.equals("schedule")))
              {
            	  entityConditionList.add(EntityCondition.makeCondition("ciId", EntityOperator.IN, listIn));
//            	  entityConditionList.add(EntityCondition.makeCondition("ciId", EntityOperator.IN, listAtomIn));
              }
              else
              {
            	  entityConditionList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("attrName", EntityOperator.EQUALS, cKey), 
            			EntityOperator.AND, EntityCondition.makeCondition("attrObjValue", EntityOperator.IN, listIn)));
            	  
//            	  entityConditionList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("attrName", EntityOperator.EQUALS, cKey), 
//              			EntityOperator.AND, EntityCondition.makeCondition("attrObjValue", EntityOperator.IN, listAtomIn)));            	  
              }
        }
        
        //2.2 first query based on the EntityCondition, then use And/Or to filter the ciId
        EntityFindOptions options = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
    	options.setDistinct(true);
    		
   	    List<List<String>> idList = FastList.newInstance();            	 
    	for(EntityCondition c : entityConditionList)
        {	            	
//    		System.out.println("---- SVC-CONTEXT QueryConditon -----------------c: " + c.toString());
//    		Debug.logInfo("---- SVC-CONTEXT QueryConditon -----------------c: " + c.toString(),module);
    		List<GenericValue> r = delegator.findList("CIAttribute", c, UtilMisc.toSet("ciId"), null, options, false);
//        		System.out.println("---- SVC-CONTEXT QueryConditon -----------------r: " + r.toString());
        	List<String> idChildList = EntityUtil.getFieldListFromEntityList(r, "ciId", true);
//            	System.out.println("---- SVC-CONTEXT QueryConditon -----------------idChildList: " + idChildList.toString());
        	if(idChildList.size() > 0)
        		idList.add(idChildList);
        }
    	
        //if record not empty, evaluate if a type in a list or query for all record for the type
    	boolean bAllRecord = (paramMapEqual.isEmpty() && paramMapLike.isEmpty() && paramMapNoEqual.isEmpty() && paramMapIn.isEmpty());
    	if(idList.size() > 0 || bAllRecord)
    	{
        	List<GenericValue> listCI = delegator.findByAnd("CI", UtilMisc.toMap("ciTypeId", strTable));
        	List<String> idChildList = EntityUtil.getFieldListFromEntityList(listCI, "ciId", true);
        	idList.add(idChildList);
    	}
    	
    	entityConditionList.clear();
    	for(List<String> list : idList)
    	{
    		entityConditionList.add(EntityCondition.makeCondition("ciId", EntityOperator.IN, list));
    	}
    	
        //construct the return value based on from=i&t0=j&order=x condition
    	EntityFindOptions optionsAll = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
        optionsAll.setDistinct(true);
        List<String> lstOrder =  UtilMisc.toList("+ciId");
    	if(mapOther != null)
    	{
    		if(mapOther.containsKey("from"))
    		{
    			optionsAll.setFetchSize(0);
    		}
    		
    		if(mapOther.containsKey("to"))
    		{
    			optionsAll.setMaxRows(Integer.parseInt(mapOther.get("to").toString()));
    		}
    		
//    		if(mapOther.containsKey("order"))
//    		{
//    			lstOrder =  UtilMisc.toList(mapOther.get("order").toString());
//    		}
    	}
    	
    	// query CIAttribute based on ciId condition, then construct the FastMap to return to erlang
    	if(entityConditionList.size() > 0)
    	{
        	EntityCondition attributecondition = EntityCondition.makeCondition(entityConditionList, EntityOperator.AND);        	
//        	System.out.println("---- SVC-CONTEXT QueryConditon -----------------attributecondition: " + attributecondition.toString());
//        	Debug.logInfo("---- SVC-CONTEXT QueryConditon -----------------attributecondition: " + attributecondition.toString(),module);
        	listCiId = delegator.findList("CIAttribute", attributecondition, UtilMisc.toSet("ciId"), lstOrder,  
        			optionsAll, false);
        	
//            	System.out.println("---- SVC-CONTEXT QueryConditon -----------------listCiId: " + UtilMisc.toList(listCiId).toString());	
        	return listCiId;
    	}
    	else
    	{    	
    		return null;
    	}
    }
    
    //parse from=i&t0=j&order=x condition
    public static Map<String, Object> AnalysisOtherCondition(String strQuery)
    {
    	Map<String, Object> mapOther = FastMap.newInstance();
        java.util.StringTokenizer queryTokens = new java.util.StringTokenizer(strQuery, "&");
        
        while (queryTokens.hasMoreTokens())
        {
            String token = queryTokens.nextToken();            
            String name = token;
            int equalsIndex = token.indexOf("=");
            
            if (equalsIndex > 0) 
            {
                name = token.substring(0, equalsIndex).trim();
                if(!token.substring(equalsIndex + 1).trim().equals("''") && !token.substring(equalsIndex + 1).trim().equals(""))
                	mapOther.put(name.replace("my.", "").trim(), token.substring(equalsIndex + 1).replace("my.", "").trim());
            }
        }
    	
    	return mapOther;
    }
    
    
}

            	  
