package com.dragonflow.siteview.report;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpGateway;

public class ReportDataServices1 {
    public final static String module = ReportDataServices1.class.getName();
    static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * contrast
     * @param dctx
     * @param context
     * @return
     * @throws GenericEntityException
     */
    
    /*
     * manbo.qu 2011-11-23
	 * @param ids,beginDate,endDate
	 * @throws RemoteException
	 * @throws GenericServiceException
     */
    
    public static Map<String, Object> statisticReportLog(DispatchContext dctx,
		Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		// 鑾峰彇鍙傛暟
		String tablename = (String) context.get("tablename");
		tablename = "OperationAttributeLog";
		int fromn = (Integer) context.get("from");
		int ton = (Integer) context.get("to");
		String mid = (String) context.get("id");
		String orderby = (String) context.get("orderby");
		String starttime=(String) context.get("starttime");
		String endtime=(String) context.get("endtime");
		
		
		// 杩囨护鏉′欢
		List<String> orderlist = null;
		List<EntityCondition> entityConditionList = FastList.newInstance();
		List<EntityCondition> entityConditionListid = FastList.newInstance();
		if (mid != null && !mid.isEmpty()) {
			String[] ids =mid.split(",");
			for(int idssize=0;idssize<ids.length;idssize++){
				if(!(ids[idssize].equals(""))&&ids[idssize].length()>0)
					entityConditionListid.add(EntityCondition.makeCondition(
						"operationId", EntityOperator.EQUALS, ids[idssize]));
			}
		}
		
		if (orderby != null && !orderby.isEmpty()) {
			orderlist = UtilMisc.toList(orderby);
		}
		EntityCondition conditionid = EntityCondition.makeCondition(
				entityConditionListid, EntityOperator.OR);
		entityConditionList.add(conditionid);
		Timestamp startTime2=Timestamp.valueOf(starttime);
	    Timestamp endTime2=Timestamp.valueOf(endtime);
		entityConditionList.add(EntityCondition.makeCondition("logTime", EntityOperator.BETWEEN,UtilMisc.toList(startTime2,endTime2)));
		EntityCondition condition = EntityCondition.makeCondition(
				entityConditionList, EntityOperator.AND);
		Map<String,Object> OutDataList = FastMap.newInstance();
		DynamicViewEntity dve = new DynamicViewEntity();
		Collection<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("operationId");
		fieldsToSelect.add("logTime");
		fieldsToSelect.add("category");
		fieldsToSelect.add("description");
		fieldsToSelect.add("measurement");
		fieldsToSelect.add("name");
		dve.addMemberEntity("OperLog", "OperationAttributeLog");
		dve.addAliasAll("OperLog", "");
		Delegator delegator = dctx.getDelegator();
		EntityListIterator resultiterator = delegator
				.findListIteratorByCondition(dve, condition, null,
						fieldsToSelect, orderlist, null);
		List<GenericValue> result = resultiterator.getPartialList(fromn, ton
				- fromn);
		int i = 0;
		String[] nid = new String[3];
		double[] totalValue = new double[3];
		String maxvaluetime="";
		String MonitorName="";
		Map<String,String> detaildstr=FastMap.newInstance();
//		String latestCreateTime="";
		int nodata=0;
		int good=0;
		int warning=0;
		String id="";
		int error=0;
		
		List<Object> list = new ArrayList<Object>();
		//query monitor type
		String monitortype="";
		List<String> monitorTypeList=FastList.newInstance();
		try {
			if(mid.split(",").length>1){
			  monitorTypeList = getMonitorTypeList(delegator, mid);
			}else {
				monitortype = getMonitorType(delegator, mid);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
//		list.add(new OtpErlangList());
		List<Object> templateList=null;
		
		try {
			if(mid.split(",").length>1) {
				for(String monitorType: monitorTypeList){
					list.add(new OtpErlangAtom(monitorType));
					templateList=(List<Object>) OtpGateway.getOtpInterface().call("api_monitor_template", "get_template", list);
				}
			}else{
				list.add(new OtpErlangAtom(monitortype));
				templateList = (List) OtpGateway.getOtpInterface().call("api_monitor_template", "get_template", list);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		Object[] temv =(Object[])nodes;
//		for(Object o:temv){
//			System.out.println(o);
//		}
		LinkedHashSet<Map<String, String>> m_return_items =new  LinkedHashSet<Map<String,String>>();
		
		m_return_items =get_Return_Items(templateList);
		
		String[]  ReturnName=new String[m_return_items.size()];
		double latest_value = 0.0;
		String lasestcreatedtime = "";
//		GenericValue gv = result.get(0);
//		MonitorName=gv.get("name").toString(); 
//		if (MonitorName.contains("Memory")) {
//			Map map = (FastMap) genericValue.get("measurement");
//		}
		StringBuffer[] detail=new StringBuffer[m_return_items.size()];
		double[] max = new double[m_return_items.size()];
		double[] min = new double[m_return_items.size()];
		double[] avg = new double[m_return_items.size()];
		double[] latest=new double[m_return_items.size()];
		Date logtime = new Date();
		Date lastcreatedtime = new Date();
		String latestStatus ="";
		String latestDstr="";
		for (GenericValue genericValue : result) {
			StringBuffer detailds=new StringBuffer();
			if (genericValue.get("category").toString().equals("nodata")){
				nodata++;
			}else if(genericValue.get("category").toString().equals("good")){
				good++;
			}else if(genericValue.get("category").toString().equals("warning")){
				warning++;
			}else {
				error++;
			}
			id=genericValue.get("operationId").toString();
			Map map = (FastMap) genericValue.get("measurement");
		 	MonitorName=genericValue.get("name").toString();

			Date newdate=(Date)genericValue.get("logTime");
			
			Iterator<Map<String, String>> iterator=m_return_items.iterator();
			int mapsize=0;
			while (iterator.hasNext()) {
				StringBuffer detail2=new StringBuffer();
				Object value ;
				String ReturnNameval=iterator.next().get("sv_name");
				value =  map.get(ReturnNameval);
				ReturnName[mapsize] = ReturnNameval;
				nid[mapsize] = (mapsize + ")" + genericValue.get("operationId"))
						.toString();
				if (value == null) {
					value = 0.0;
				}
				if(value instanceof Double){
					value=(Double)value;
					totalValue[mapsize] = ((Double) value) + totalValue[mapsize];
					if ((Double) value < max[mapsize]) {
						max[mapsize] = max[mapsize];
						maxvaluetime = genericValue.get("logTime").toString();
					} else {
						max[mapsize] = (Double) value;
					}
					if ((Double) value > min[mapsize]) {
						min[mapsize] = min[mapsize];
					} else {
						min[mapsize] = (Double) value;
					}
					if (newdate.after(logtime)) {
						logtime = newdate;
						latest[mapsize] = (Double) value;
						latestStatus = genericValue.get("category").toString();
						latestDstr = genericValue.get("description").toString();
					}
				}else if(value instanceof Long){
					value=(Long)value;
					if ((Long) value < max[mapsize]) {
						max[mapsize] = max[mapsize];
						maxvaluetime = genericValue.get("logTime").toString();
					} else {
						max[mapsize] = (Long) value;
					}
					if ((Long) value > min[mapsize]) {
						min[mapsize] = min[mapsize];
					} else {
						min[mapsize] = (Long) value;
					}
					if (newdate.after(logtime)) {
						logtime = newdate;
						latest[mapsize] = (Long) value;
						latestStatus = genericValue.get("category").toString();
						latestDstr = genericValue.get("description").toString();
					}else{
						value=(Long) value;
					}
					totalValue[mapsize] = ((Long) value) + totalValue[mapsize];
				}
				if (newdate.after(lastcreatedtime)) {
					lastcreatedtime = newdate;
				}
				if(detail[mapsize]!=null){
					detail[mapsize]=detail[mapsize].append(genericValue.get("logTime")).append("=")
							.append(value).append(",");
				}else {
					detail[mapsize]=detail2.append(genericValue.get("logTime")).append("=")
							.append(value).append(",");
				}
				mapsize++;
			}
			String statemessage=genericValue.get("category").toString();
			if(statemessage.contains("good")){
				statemessage= "ok";
			}else if(statemessage.contains("good")){
				statemessage= "error";
			}else if(statemessage.contains("good")){
				statemessage= "disable";
			}else if(statemessage.contains("good")){
				statemessage= "warning";
			}else{
				statemessage= "nodata";
			}
			
			detailds.append(statemessage)
			.append(" ")
			.append(genericValue.get("description"))
			.append(" ")
			.append("@@")
			.append(genericValue
					.get("measurement")
					.toString()
					.substring(
							1,
							genericValue.get("measurement")
							.toString().length() - 1));
			String timekey = format.format(genericValue.get("logTime"));
			detaildstr.put(timekey, detailds.toString());
			i++;
		}
		//-------part 1--------
		
	
		for(int outdatamapsize=0;outdatamapsize<min.length;outdatamapsize++){
			Map<String,String> OutDataMap = FastMap.newInstance();
			OutDataMap.put("min", String.valueOf(min[outdatamapsize]));
			OutDataMap.put("max", String.valueOf(max[outdatamapsize]));
			OutDataMap.put("average", String.valueOf(totalValue[outdatamapsize]/i));
			OutDataMap.put("sv_drawimage", "1");
			OutDataMap.put("sv_drawtable", "1");
			/**
			 * time max
			 */
			OutDataMap.put("when_max",maxvaluetime);
			OutDataMap.put("MonitorName", MonitorName);
			OutDataMap.put("sv_type", "numeric");
			OutDataMap.put("ReturnTitle", ReturnName[outdatamapsize].toString());
			OutDataMap.put("ReturnName", ReturnName[outdatamapsize].toString());
			OutDataMap.put("sv_drawmeasure", "1");
			OutDataMap.put("sv_primary", "1");
			OutDataMap.put("sv_baseline", "1");
			OutDataMap.put("latest", String.valueOf(latest[outdatamapsize]));
			OutDataMap.put("detail", detail[outdatamapsize].toString());
			OutDataList.put("(Return_"+nid[outdatamapsize], OutDataMap);
//			OutDataList.put("",OutDataMap2);
		}
	//-------part 2--------
		OutDataList.put("(dstr)"+id, detaildstr);
	//-------part 3--------
		Map<String,String> part = FastMap.newInstance();
		String lastcreatedtime1 =format.format(lastcreatedtime);
		part.put("latestCreateTime", lastcreatedtime1);
		part.put("MonitorName", MonitorName);
		part.put("errorPercent",String.valueOf( error/result.size()));
		part.put("warningPercent", String.valueOf( warning/result.size()));
		part.put("okPercent", String.valueOf( good/result.size()));
		part.put("latestStatus", latestStatus);
		part.put("latestDstr", latestDstr);
		for (int ReturnNamesize=0;ReturnNamesize<ReturnName.length;ReturnNamesize++){
			part.put("(Return_"+ReturnNamesize+")"+id, "ReturnValue");
		}
		OutDataList.put(id, part);
		response.put("loggervalues", OutDataList);
		return response;

	} 
    
    
    public static Map<String, Object> contrastReportLog(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
    	return null;
    }
    
    private static void reportlog() {

  		
  	}
   
    /**
	 * Log a single monitor value into entity
	 * 
	 * @param dctx
	 *            The DispatchContext that this service is operating in
	 *@param context
	 *            Map containing the input parameters
	 *@return Map with the result of the service, the output parameters
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> monitorLogger(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();

//		if (context.size() > 0) {
//			for (Map.Entry<String, ?> entry : context.entrySet()) {
//				Object cKey = entry.getKey();
//				Object value = entry.getValue();
//
//				Debug.logInfo(
//						"In CONTEXTvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv: "
//								+ cKey + " => " + value, module);
//			}
//		}
		
		//insert master data
		Delegator delegator = dctx.getDelegator();
		FastList listValues = (FastList) context.get("monitor");
		
		
		String logId = delegator.getNextSeqId("OperationAttributeLog");		
		String operationId=listValues.get(0).toString();
//		String name=listValues.get(2).toString();
		Object name=listValues.get(2);
        Timestamp logTime=(Timestamp)listValues.get(3);
		String category=listValues.get(4).toString();
		Object description=listValues.get(5).toString();
		Object measurement=listValues.get(6);
		Map<String, String> fields = UtilMisc.toMap("logId", logId,
				"operationId",operationId,
				"logTime",logTime,
				"name",name,
				"category",category,
				"description",description,
				"measurement",measurement);
		GenericValue log = delegator.makeValue("OperationAttributeLog", fields);
		delegator.create(log);
		
//		response.put("listValue", listValues);
		return response;
	}

    /**
	 * Log a list of monitors into entity
	 * 
	 * @param dctx
	 *            The DispatchContext that this service is operating in
	 *@param context
	 *            Map containing the input parameters
	 *@return Map with the result of the service, the output parameters
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> monitorListLogger(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response;

		//insert master data
		Delegator delegator = dctx.getDelegator();
//		FastList monitorList = (FastList) context.get("monitorList");
		List<Object>  monitorList = FastList.newInstance();
		monitorList=(List<Object>) context.get("monitorList");
		List<GenericValue> list = FastList.newInstance();
		
		for(Object tmonitor: monitorList)
		{
			List<Object> monitor=(List<Object>)tmonitor;
	        String logId = delegator.getNextSeqId("OperationAttributeLog");
	        String operationId=monitor.get(0).toString();
	        Object name=monitor.get(2);
	        Timestamp logTime=(Timestamp)monitor.get(3);
	        String category=monitor.get(4).toString();
	        Object description=monitor.get(5).toString();
	        Object measurement=monitor.get(6);
	        Map<String, String> fields = UtilMisc.toMap("logId", logId,
	                "operationId",operationId,
	                "logTime",logTime,
	                "name",name,
	                "category",category,
	                "description",description,
	                "measurement",measurement);
	        list.add(delegator.makeValue("OperationAttributeLog", fields));
		}
		
		response = (delegator.storeAll(list)>0) ? ServiceUtil.returnSuccess():ServiceUtil.returnFailure();

		return response;
	}
	/**
	 * Generic Test Service
	 * 
	 * @param dctx
	 *            The DispatchContext that this service is operating in
	 *@param context
	 *            Map containing the input parameters
	 *@return Map with the result of the service, the output parameters
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> loggerquery(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();

		if (context.size() > 0) {
			for (Map.Entry<String, ?> entry : context.entrySet()) {
				Object cKey = entry.getKey();
				Object value = entry.getValue();

				Debug.logInfo(
						"In CONTEXTvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv: "
								+ cKey + " => " + value, module);
			}
		}
		//get param
		FastList listValues = (FastList) context.get("listValue");	
		EntityFindOptions findOptions=null;
		String monitorid=listValues.get(0).toString();
		
		String counts= listValues.get(3).toString();
		Delegator delegator = dctx.getDelegator();
		List<EntityCondition> entityConditionList = FastList.newInstance();
		if (counts.isEmpty()||counts.equals(""))
		{
		//List listids=Arrays.asList(monitorids.split(","));
        Timestamp startTime=(Timestamp)listValues.get(1);
        Timestamp endTime=(Timestamp)listValues.get(2);
        entityConditionList.add(EntityCondition.makeCondition("logTime", EntityOperator.BETWEEN,UtilMisc.toList(startTime,endTime)));
		}else
		{
			findOptions=new EntityFindOptions();
			findOptions.setMaxRows(Integer.parseInt(counts));
		}
		if(monitorid.isEmpty()||monitorid.equals(""))
		{
			
		}else
		{
			List monitorids=(List)listValues.get(0);
			entityConditionList.add(EntityCondition.makeCondition("operationId", EntityOperator.IN, monitorids));	
		}
        
		
	    EntityCondition condition = EntityCondition.makeCondition(entityConditionList, EntityOperator.AND); 
	    List<GenericValue> result= delegator.findList("OperationAttributeLog", condition, null,UtilMisc.toList("logTime DESC"), findOptions, false);
	    List<Object> OutDataList = FastList.newInstance();
	    
	    for(int index=result.size()-1;index>=0;index--)
	    {   GenericValue genericValue=result.get(index);
	    	    List<Object> childlist = FastList.newInstance();
		        childlist.add(0, genericValue.get("operationId"));
		        childlist.add(1, genericValue.get("logTime"));
		        childlist.add(2, genericValue.get("category"));
		        childlist.add(3, genericValue.get("description") );
		        childlist.add(4, genericValue.get("measurement") );
		        childlist.add(5, genericValue.get("name") );
		        OutDataList.add(childlist);
	    }
	    response.put("loggervalues",OutDataList );
		//("", entityCondition, fieldsToSelect, orderBy, findOptions, useCache); 
		
//		System.out.println("----- SVCvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv: "
//				+ dctx.getName() + " -----");
		return response;
	}
	//
	//rmi 鏀寔澶ф暟鎹噺鐨勬姤鍛婏紝鍒嗛〉
	// where 娌″疄鐜�
	@SuppressWarnings("unused")
	public static Map<String, Object> queryReportLog(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		// 鑾峰彇鍙傛暟
		String tablename = (String) context.get("tablename");
		tablename = "OperationAttributeLog";
		int fromn = (Integer) context.get("from");
		int ton = (Integer) context.get("to");
		String mid = (String) context.get("id");
		String orderby = (String) context.get("orderby");
		String where = (String) context.get("where");
		// 杩囨护鏉′欢
		List<String> orderlist = null;
		List<EntityCondition> entityConditionList = FastList.newInstance();
		if (mid != null && !mid.isEmpty()) {
			entityConditionList.add(EntityCondition.makeCondition(
					"operationId", EntityOperator.EQUALS, mid));
		}
		if (orderby != null && !orderby.isEmpty()) {
			orderlist = UtilMisc.toList(orderby);
		}
		EntityCondition condition = EntityCondition.makeCondition(
				entityConditionList, EntityOperator.AND);
		Map<String,Object> OutDataList = FastMap.newInstance();
		DynamicViewEntity dve = new DynamicViewEntity();
		Collection<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("operationId");
		fieldsToSelect.add("logTime");
		fieldsToSelect.add("category");
		fieldsToSelect.add("description");
		fieldsToSelect.add("measurement");
		fieldsToSelect.add("name");
		dve.addMemberEntity("OperLog", "OperationAttributeLog");
		dve.addAliasAll("OperLog", "");
		Delegator delegator = dctx.getDelegator();
		EntityListIterator resultiterator = delegator
				.findListIteratorByCondition(dve, condition, null,
						fieldsToSelect, null, null);
		List<GenericValue> result = resultiterator.getPartialList(fromn, ton
				- fromn);
		int i = 0;
		String[] nid = new String[3];
		double[] totalValue = new double[3];
		String maxvaluetime="";
		String MonitorName="";
		Map<String,String> detaildstr=FastMap.newInstance();
//		String latestCreateTime="";
		int nodata=0;
		int good=0;
		int warning=0;
		String id="";
		int error=0;
		
		List<Object> list = new ArrayList<Object>();
		//query monitor type
		String monitortype="";
		try {
			monitortype = getMonitorType(delegator, mid);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		list.add(new OtpErlangAtom(monitortype));
//		list.add(new OtpErlangList());
		List<Object> templateList=null;
		try {
		  templateList = (List) OtpGateway.getOtpInterface().call("api_monitor_template", "get_template", list);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		Object[] temv =(Object[])nodes;
//		for(Object o:temv){
//			System.out.println(o);
//		}
		LinkedHashSet<Map<String, String>> m_return_items =new  LinkedHashSet<Map<String,String>>();
		m_return_items =get_Return_Items(templateList);
			
		String[]  ReturnName=new String[m_return_items.size()];
		double latest_value = 0.0;
		String lasestcreatedtime = "";
//		GenericValue gv = result.get(0);
//		MonitorName=gv.get("name").toString(); 
//		if (MonitorName.contains("Memory")) {
//			Map map = (FastMap) genericValue.get("measurement");
//		}
		StringBuffer[] detail=new StringBuffer[m_return_items.size()];
		double[] max = new double[m_return_items.size()];
		double[] min = new double[m_return_items.size()];
		double[] avg = new double[m_return_items.size()];
		double[] latest=new double[m_return_items.size()];
		Date logtime = new Date();
		Date lastcreatedtime = new Date();
		String latestStatus ="";
		String latestDstr="";
		for (GenericValue genericValue : result) {
			StringBuffer detailds=new StringBuffer();
			if (genericValue.get("category").toString().equals("nodata")){
				nodata++;
			}else if(genericValue.get("category").toString().equals("good")){
				good++;
			}else if(genericValue.get("category").toString().equals("warninging")){
				warning++;
			}else {
				error++;
			}
			id=genericValue.get("operationId").toString();
			//TODO: measure is map ?  need conversion ?
			Map map = (FastMap) genericValue.get("measurement");
		 	MonitorName=genericValue.get("name").toString();

			Date newdate=(Date)genericValue.get("logTime");
			
			Iterator<Map<String, String>> iterator=m_return_items.iterator();
			int mapsize=0;
			while (iterator.hasNext()) {
				StringBuffer detail2=new StringBuffer();
				Object value ;
				String ReturnNameval=iterator.next().get("sv_name");
				value =  map.get(ReturnNameval);
				ReturnName[mapsize] = ReturnNameval;
				nid[mapsize] = (mapsize + ")" + genericValue.get("operationId"))
						.toString();
				if (value == null) {
					value = 0.0;
				}
				if(value instanceof Double){
					value=(Double)value;
					totalValue[mapsize] = ((Double) value) + totalValue[mapsize];
					if ((Double) value < max[mapsize]) {
						max[mapsize] = max[mapsize];
						maxvaluetime = genericValue.get("logTime").toString();
					} else {
						max[mapsize] = (Double) value;
					}
					if ((Double) value > min[mapsize]) {
						min[mapsize] = min[mapsize];
					} else {
						min[mapsize] = (Double) value;
					}
					if (newdate.after(logtime)) {
						logtime = newdate;
						latest[mapsize] = (Double) value;
						latestStatus = genericValue.get("category").toString();
						latestDstr = genericValue.get("description").toString();
					}
				}else if(value instanceof Long){
					value=(Long)value;
					if ((Long) value < max[mapsize]) {
						max[mapsize] = max[mapsize];
						maxvaluetime = genericValue.get("logTime").toString();
					} else {
						max[mapsize] = (Long) value;
					}
					if ((Long) value > min[mapsize]) {
						min[mapsize] = min[mapsize];
					} else {
						min[mapsize] = (Long) value;
					}
					if (newdate.after(logtime)) {
						logtime = newdate;
						latest[mapsize] = (Long) value;
						latestStatus = genericValue.get("category").toString();
						latestDstr = genericValue.get("description").toString();
					}else{
						value=(long)0;
					}
					totalValue[mapsize] = ((Long) value) + totalValue[mapsize];
				}
				if (newdate.after(lastcreatedtime)) {
					lastcreatedtime = newdate;
				}
				if(detail[mapsize]!=null){
					detail[mapsize]=detail[mapsize].append(genericValue.get("logTime")).append("=")
							.append(value).append(",");
				}else {
					detail[mapsize]=detail2.append(genericValue.get("logTime")).append("=")
							.append(value).append(",");
				}
				mapsize++;
			}
			String statemessage=genericValue.get("category").toString();
			if(statemessage.contains("good")){
				statemessage= "ok";
			}else if(statemessage.contains("good")){
				statemessage= "error";
			}else if(statemessage.contains("good")){
				statemessage= "disable";
			}else if(statemessage.contains("good")){
				statemessage= "warning";
			}else{
				statemessage= "nodata";
			}
			
			detailds.append(statemessage)
			.append(" ")
			.append(genericValue.get("description"))
			.append(" ")
			.append("@@")
			.append(genericValue
					.get("measurement")
					.toString()
					.substring(
							1,
							genericValue.get("measurement")
							.toString().length() - 1));
			String timekey = format.format(genericValue.get("logTime"));
			detaildstr.put(timekey, detailds.toString());
			i++;
		}
		//-------part 1--------
		
	
		for(int outdatamapsize=0;outdatamapsize<min.length;outdatamapsize++){
			Map<String,String> OutDataMap = FastMap.newInstance();
			OutDataMap.put("min", String.valueOf(min[outdatamapsize]));
			OutDataMap.put("max", String.valueOf(max[outdatamapsize]));
			OutDataMap.put("average", String.valueOf(totalValue[outdatamapsize]/i));
			OutDataMap.put("sv_drawimage", "1");
			OutDataMap.put("sv_drawtable", "1");
			/**
			 * time max
			 */
			OutDataMap.put("when_max",maxvaluetime);
			OutDataMap.put("MonitorName", MonitorName);
			OutDataMap.put("sv_type", "numeric");
			OutDataMap.put("ReturnTitle", ReturnName[outdatamapsize].toString());
			OutDataMap.put("ReturnName", ReturnName[outdatamapsize].toString());
			OutDataMap.put("sv_drawmeasure", "1");
			OutDataMap.put("sv_primary", "1");
			OutDataMap.put("sv_baseline", "1");
			OutDataMap.put("latest", String.valueOf(latest[outdatamapsize]));
			OutDataMap.put("detail", detail[outdatamapsize].toString());
			OutDataList.put("(Return_"+nid[outdatamapsize], OutDataMap);
//			OutDataList.put("",OutDataMap2);
		}
	//-------part 2--------
		OutDataList.put("(dstr)"+id, detaildstr);
	//-------part 3--------
		Map<String,String> part = FastMap.newInstance();
		String lastcreatedtime1 =format.format(lastcreatedtime);
		part.put("latestCreateTime", lastcreatedtime1);
		part.put("MonitorName", MonitorName);
		part.put("errorPercent",String.valueOf( error/result.size()));
		part.put("warningPercent", String.valueOf( warning/result.size()));
		part.put("okPercent", String.valueOf( good/result.size()));
		part.put("latestStatus", latestStatus);
		part.put("latestDstr", latestDstr);
		for (int ReturnNamesize=0;ReturnNamesize<ReturnName.length;ReturnNamesize++){
			part.put("(Return_"+ReturnNamesize+")"+id, "ReturnValue");
		}
		OutDataList.put(id, part);
		response.put("loggervalues", OutDataList);
		return response;

	} 
	/**
	 * Created by zhongping.wang
	 * Get Monitor Type
	 * @param DispatchContext dctx,Map<String, ?> context,String id
	 * @return String monitorType
	 **/
	
	private static String getMonitorType(Delegator delegator,String mid) throws Exception{

	 String monitorType ="";
     if(mid == null || mid.isEmpty()) return null;
     
     Set<String> fieldsToSelect = FastSet.newInstance();
     fieldsToSelect.add("ciId");
     fieldsToSelect.add("attrName");
     fieldsToSelect.add("attrValue");
     fieldsToSelect.add("attrObjValue");

     EntityCondition AttrNameCondition = EntityCondition.makeCondition("attrName", EntityOperator.EQUALS,"class");
     EntityCondition IdCondition = EntityCondition.makeCondition("ciId", EntityOperator.EQUALS,mid);
     EntityCondition condition = EntityCondition.makeCondition(AttrNameCondition , EntityOperator.AND, IdCondition);

     List<GenericValue> result= delegator.findList("CIAttribute", condition, fieldsToSelect, null, null, true);
     monitorType = result.get(0).get("attrObjValue").toString();
     if (monitorType == null) monitorType = result.get(0).get("attrValue").toString();
     return monitorType;
 }
	
	/*
	 * many monitors
	 * 
	 */
	private static List<String> getMonitorTypeList(Delegator delegator,String mid) throws Exception{
     List<EntityCondition> entityConditionList = FastList.newInstance();
     List<String> monitorTypeList =new ArrayList<String>();
     if(mid!=null&& !mid.isEmpty())
     {
         if (mid != null && !mid.isEmpty()) {
             String[] ids =mid.split(",");
             for(int idssize=0;idssize<ids.length;idssize++){
                 if(!(ids[idssize].equals(""))&&ids[idssize].length()>0)
                     entityConditionList.add(EntityCondition.makeCondition(
                         "ciId", EntityOperator.EQUALS, ids[idssize]));
             }
         }
     }
     DynamicViewEntity dve = new DynamicViewEntity();
     Collection<String> fieldsToSelect = FastList.newInstance();
     fieldsToSelect.add("ciId");
     fieldsToSelect.add("attrName");
     fieldsToSelect.add("attrValue");
     fieldsToSelect.add("attrObjValue");
     dve.addMemberEntity("ciattribute", "CIAttribute");
     dve.addAliasAll("ciattribute", "");
     EntityCondition AttrNameCondition = EntityCondition.makeCondition("attrName", EntityOperator.EQUALS,"class");
     EntityCondition IdCondition = EntityCondition.makeCondition(entityConditionList, EntityOperator.OR);
     EntityCondition condition = EntityCondition.makeCondition(AttrNameCondition , EntityOperator.AND, IdCondition);

     EntityListIterator resultiterator= delegator.findListIteratorByCondition(dve,condition, null, fieldsToSelect, null, null);
     List<GenericValue> result= resultiterator.getPartialList(1,1000);
     for (GenericValue genericValue : result) {
           monitorTypeList.add(genericValue.get("attrObjValue").toString()==null ?
             genericValue.get("Value").toString():genericValue.get("attrObjValue").toString() );
     }
     //return Monitor type list
     return monitorTypeList;
 }
	
    public void	setdata(List<GenericValue> result)
	{
    	GenericValue gv1=result.get(0);
    	Map map = (FastMap) gv1.get("measurement");

		Double min[]=new Double[map.size()];
		Date lastdate=new Date();
		Double lastst[]=new Double[map.size()];
    	for(int i=0;i<map.size();i++)
    	{ 
    		String 	id=gv1.get("operationId").toString();
    		String MonitorName=gv1.get("name").toString();
    		Map mapi=(FastMap)map.get(i);
    		for(int j=0;j<result.size();j++)
    		{
    			GenericValue onegv=result.get(j);
    			Double value;
    		    if(i==0)
    		    {
    		    	
    		    	value =	(Double) mapi.get("pPercentFull");
    		    	if(min[i]<value)
    		    	{
    		    		min[i]=value;
    		    	}
    		    	Date date=(Date)mapi.get("logTime");
    		    	if(lastdate.before(date))
    		    	{
    		    		lastdate=date;
    		    		lastst[i]=value;
    		    	}
    		    }
    		    if(i==1)
    		    {
    		    	
    		    	value =	(Double) mapi.get(".......");
    		    	if(min[i]<value)
    		    	{
    		    		min[i]=value;
    		    	}
    		    	
    		    	
    		    }
    		}
    	}
	
		
	}
	
	/**
	 * 杩斿洖闃��琛ㄨ揪寮�
	 */
	public final static LinkedHashSet<Map<String, String>> get_Return_Items(List<Object> templateList) {

		Object o =null;
		LinkedHashSet<Map<String, String>> a_list = new LinkedHashSet<Map<String,String>>();
		for (Object template: templateList){
//			o=nodes.get(nodessize);
			if (o instanceof Object[]){
				Object[] temv =(Object[])template;
				for (int temvsize=0;temvsize<temv.length;temvsize++){
					if (temv[9].toString().equals("true")) {
						Map<String, String> a_map = new LinkedHashMap<String, String>();
						a_map.put("sv_name", temv[1].toString());
						a_map.put("sv_label", temv[2].toString().substring(1,
								temv[2].toString().length() - 1));
						a_list.add(a_map);
					}
				}
			}
		}
		return a_list;
	}
	
	/**
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */
	
	public static Map<String, Object> queryReportLog1(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		String tablename = (String) context.get("tablename");
		tablename = "OperationAttributeLog";
		Map<String, String>  OutDataMap = FastMap.newInstance();
		Map<String, String>  OutDataMap1= FastMap.newInstance();
		Map<String, Object>  OutDataMap2 = FastMap.newInstance();
		Map<String, String>  OutDataMap3 = FastMap.newInstance();
		Map<String, String>  OutDataMap4 = FastMap.newInstance();
		//-------part 1--------
		OutDataMap.put("min", "1");
		OutDataMap.put("detail", "2011-11-21 11:49:30=1,2011-11-21 11:59:30=2,2011-11-21 12:09:30=1,2011-11-21 12:19:30=2,2011-11-21 12:29:30=1,2011-11-21 12:39:30=2,2011-11-21 12:49:30=1,2011-11-21 12:59:30=2,2011-11-21 13:09:30=1,2011-11-21 13:19:30=2,2011-11-21 13:29:30=1,2011-11-21 13:39:30=2,2011-11-21 13:49:30=1,2011-11-21 13:59:30=2,2011-11-21 14:09:30=1,2011-11-21 14:19:30=2,2011-11-21 14:29:30=1,2011-11-21 14:39:30=2,2011-11-21 14:49:30=1,2011-11-21 14:59:30=2");
		OutDataMap.put("max", "2");
		OutDataMap.put("when_max", "2011-11-21 11:49:30");
		OutDataMap.put("MonitorName", "Disk Space:192.168.0.68(/dev/hd1)");
		OutDataMap.put("ReturnTitle", "percentFull");
		OutDataMap.put("ReturnName","percentFull(%)");
		OutDataMap.put("average", "55.00");
		OutDataMap.put("latest", "100.00");
		OutDataMap.put("sv_type", "numeric");
		OutDataMap.put("sv_drawimage", "1");
		OutDataMap.put("sv_drawtable", "1");
		OutDataMap.put("sv_drawmeasure", "1");
		OutDataMap.put("sv_primary", "1");
		OutDataMap.put("sv_baseline", "1");
		OutDataMap2.put("(Return_0)0.2.1", OutDataMap);
		//---------part 2------------
		OutDataMap1.put("min", "0");
		OutDataMap1.put("detail", "2011-11-21 11:49:30=64,2011-11-21 11:59:30=64,2011-11-21 12:09:30=64,2011-11-21 12:19:30=64,2011-11-21 12:29:30=64,2011-11-21 12:39:30=64,2011-11-21 12:49:30=64,2011-11-21 12:59:30=64,2011-11-21 13:09:30=64,2011-11-21 13:19:30=64,2011-11-21 13:29:30=64,2011-11-21 13:39:30=64,2011-11-21 13:49:30=64,2011-11-21 13:59:30=64,2011-11-21 14:09:30=64,2011-11-21 14:19:30=64,2011-11-21 14:29:30=64,2011-11-21 14:39:30=64,2011-11-21 14:49:30=64,2011-11-21 14:59:30=64,");
		OutDataMap1.put("max", "64");
		OutDataMap1.put("when_max", "2011-11-21 11:49:30");
		OutDataMap1.put("MonitorName", "Disk Space:192.168.0.68(/dev/hd1)");
		OutDataMap1.put("ReturnTitle", "freeSpace");
		OutDataMap1.put("ReturnName", "MB free(MB)");
		OutDataMap1.put("average", "64");
		OutDataMap1.put("latest", "64");
		OutDataMap1.put("sv_type", "numeric");
		OutDataMap1.put("sv_drawimage", "1");
		OutDataMap1.put("sv_drawtable", "1");
		OutDataMap1.put("sv_drawmeasure", "1");
		OutDataMap1.put("sv_primary", "1");
		OutDataMap1.put("sv_baseline", "1");
		OutDataMap2.put("(Return_1)0.2.1", OutDataMap1);
		//----------part 3--------------
		OutDataMap3.put("latestCreateTime", "2011-11-21 15:06:39");
		OutDataMap3.put("MonitorName", "Disk Space:192.168.0.68(/dev/hd1)");
		OutDataMap3.put("errorPercent", "0.00");
		OutDataMap3.put("warningPercent", "0.00");
		OutDataMap3.put("okPercent", "100.00");
		OutDataMap3.put("latestStatus", "good");
		OutDataMap3.put("latestDstr", "1% full<br>64MB free<br>64MB total");
		OutDataMap3.put("(Return_0)0.2.1", "ReturnValue");
		OutDataMap3.put("errorCondition", "0");
		OutDataMap2.put("0.2.1", OutDataMap3);
		
		//----------part 4--------------
		String wstr="ok 1% full 64MB free 64MB total@@percentFull=1,freeSpace=64";
		String wstr2="ok 2% full 30MB free 64MB total@@percentFull=2,freeSpace=30";
		OutDataMap4.put("2011-11-21 11:49:30",wstr);
		OutDataMap4.put("2011-11-21 11:59:30",wstr2);
		OutDataMap4.put("2011-11-16 15:03:38", wstr);
		OutDataMap4.put("2011-11-16 15:13:38", wstr2);
		OutDataMap4.put("2011-11-16 15:23:38", wstr);
		OutDataMap4.put("2011-11-16 15:33:38", wstr2);
		OutDataMap4.put("2011-11-16 15:43:38", wstr);
		OutDataMap4.put("2011-11-16 15:53:38", wstr2);
		OutDataMap4.put("2011-11-16 16:03:38", wstr);
		OutDataMap4.put("2011-11-16 16:13:38", wstr2);
		OutDataMap4.put("2011-11-16 16:23:38", wstr);
		OutDataMap4.put("2011-11-16 16:33:38", wstr2);
		OutDataMap4.put("2011-11-16 16:43:38", wstr);
		OutDataMap4.put("2011-11-16 16:53:38", wstr2);
		OutDataMap4.put("2011-11-16 17:03:38", wstr);
		
		OutDataMap4.put("2011-11-16 17:13:38", wstr2);
		OutDataMap4.put("2011-11-16 17:23:38", wstr);
		OutDataMap4.put("2011-11-16 17:33:38", wstr2);
		OutDataMap4.put("2011-11-16 17:43:38", wstr);
		OutDataMap4.put("2011-11-16 17:53:38", wstr2);
		OutDataMap2.put("(dstr)0.2.1", OutDataMap4);
		//----------part 5--------------
		OutDataMap2.put("error_classifier", "{0.8.2=[percentFull>98]}");
		OutDataMap2.put("warning_classifier", "{0.8.2=[percentFull>=90]}");
		OutDataMap2.put("ok_classifier", "{0.8.2=[percentFull>=0]}");
		
//		OutDataList.add(OutDataMap2);
//		OutDataList.add(OutDataMap3);
//		String id = "0.1.2";
		// Object o ;
		// try {
		// o=getMonitorTemplate(dctx, context, id);
		// } catch (Exception e) {

		// e.printStackTrace();
		// }
//		Set<Integer> set = getNumsFromStr("good, 51% used,2554MB free,");
//		for (Iterator iter = set.iterator(); iter.hasNext();) {
//			Integer element = (Integer) iter.next();
//			System.out.print("element------------------------------>" + element
//					+ "   ");
//		}
//		// OutDataList.add(6,"max=11111,min=11111,vag=5555,error=74444,ok=4144444,dragon=4555");
//		response.put("loggervalues", OutDataList);
		return OutDataMap2;

	}
	
	public static Map<String, Object> LoggerServices(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		    Map<String, Object> response = ServiceUtil.returnSuccess();
		   //鑾峰彇鍙傛暟
	        String tablename=(String)context.get("tablename");
	        tablename="OperationAttributeLog";
	        int fromn=(Integer)context.get("from");
	        int ton=(Integer)context.get("to");
	        String mid=(String)context.get("id");
	        String orderby=(String)context.get("orderby");
	        String where=(String)context.get("where");
	        //杩囨护鏉′欢
	        List<String> orderlist=null;
	        List<EntityCondition> entityConditionList = FastList.newInstance();
	        if(mid!=null&& !mid.isEmpty())
	        {
	        	entityConditionList.add(EntityCondition.makeCondition("operationId", EntityOperator.EQUALS,mid ));	
	        }
	        if(orderby!=null&&!orderby.isEmpty())
	        {
	        	orderlist=UtilMisc.toList(orderby);
	        }
	        EntityCondition condition = EntityCondition.makeCondition(entityConditionList, EntityOperator.AND);  	
	        FastList OutDataList = FastList.newInstance();
		    DynamicViewEntity dve = new DynamicViewEntity();
		    Collection<String> fieldsToSelect = FastList.newInstance();
		    fieldsToSelect.add("operationId");
		    fieldsToSelect.add("logTime");
		    fieldsToSelect.add("category");
		    fieldsToSelect.add("description");
		    fieldsToSelect.add("measurement");
		    fieldsToSelect.add("name");
		    dve.addMemberEntity("OperLog", "OperationAttributeLog");
		    dve.addAliasAll("OperLog", "");
		    Delegator delegator = dctx.getDelegator();
		    EntityListIterator resultiterator= delegator.findListIteratorByCondition(dve,condition, null, fieldsToSelect, orderlist, null);
		    List<GenericValue> result= resultiterator.getPartialList(fromn,ton-fromn);
		    for (GenericValue genericValue : result) {
		    	FastList childlist = FastList.newInstance();
		        childlist.add(0, genericValue.get("operationId"));
		        childlist.add(1, genericValue.get("logTime"));
		        childlist.add(2, genericValue.get("category"));
		        childlist.add(3, genericValue.get("description") );
		        childlist.add(4, genericValue.get("measurement") );
		        childlist.add(5, genericValue.get("name") );
		        Set<Integer> set = getNumsFromStr(genericValue.get("measurement").toString());
		        for (Iterator iter = set.iterator(); iter.hasNext();) {
		        	Integer element = (Integer) iter.next();
		        	System.out.print("element------------------------------>"+element + "   ");
		        }
		        OutDataList.add(childlist);
			}
//		    String id="0.1.2";
//		    Object o ;
//		    try {
//				o=getMonitorTemplate(dctx, context, id);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			OutDataList.add(6,"max=11111,min=11111,vag=5555,error=74444,ok=4144444,dragon=4555");
		    response.put("loggervalues",OutDataList );
			return response;
	
	}
		private static Set<Integer> getNumsFromStr(String str) {

		String[] ary = str.replaceAll("[^\\d]", " ").split("\\s+");

		Set<Integer> set = new TreeSet<Integer>();

		for (String num : ary) {
			if (!num.trim().equals("")) {
				set.add(new Integer(num.trim()));
			}
		}

		return set;
	}
	 /**
	 * Generic Test Service
	 * 
	 * @param dctx
	 *            The DispatchContext that this service is operating in
	 *@param context
	 *            Map containing the input parameters
	 *@return Map with the result of the service, the output parameters
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> alertlogger(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();

		if (context.size() > 0) {
			for (Map.Entry<String, ?> entry : context.entrySet()) {
				Object cKey = entry.getKey();
				Object value = entry.getValue();

				Debug.logInfo(
						"In CONTEXTvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv: "
								+ cKey + " => " + value, module);
			}
		}
		//insert master data
		Delegator delegator = dctx.getDelegator();
		FastList listValues = (FastList) context.get("listValue");
		
		String id = delegator.getNextSeqId("AlertLog");
		String alertId=listValues.get(0).toString();
		//listValues.get(2) App
		String alerttype=listValues.get(2).toString();
		String name=listValues.get(3).toString();
		String monitor=listValues.get(4).toString();
		String receiver=listValues.get(5).toString();
		String title=listValues.get(6).toString();
		Timestamp alerttime=(Timestamp)listValues.get(7);
		String result=listValues.get(8).toString();
		String content=listValues.get(9).toString();
		String alert_level=listValues.get(10).toString();
		String groupid=listValues.get(11).toString();
		Timestamp responsetime;
		try
		{
		responsetime=(Timestamp)listValues.get(12);
		}
		catch(Exception ex)
		{
			responsetime=Timestamp.valueOf("1970-01-01 0:0:0.1") ;
		}
		String responder=listValues.get(13).toString();
		String responsecontent=listValues.get(14).toString();
		Timestamp cleartime;
		try
		{
		cleartime=(Timestamp)listValues.get(15);
		}catch(Exception ex1)
		{
			cleartime=Timestamp.valueOf("1970-01-01 0:0:0.1") ;
		}
		String times=listValues.get(16).toString();
		Map<String, String> fields = UtilMisc.toMap("id", id,
				"alertId",alertId,
				"alerttype",alerttype,
				"name",name,
				"monitor",monitor,
				"receiver",receiver,
				"title",title,
				"alerttime",alerttime,
				"result",result,
				"alert_level",alert_level,
				"content",content,
				"groupid",groupid,
				"responsetime",responsetime,
				"responder",responder,
				"responsecontent",responsecontent,
				"cleartime",cleartime,
				"times",times
				);
		GenericValue log = delegator.makeValue("AlertLog", fields);
		delegator.create(log);
		response.put("listValue", listValues);
		return response;
	}
	/**
	 * Generic Test Service
	 * 
	 * @param dctx
	 *            The DispatchContext that this service is operating in
	 *@param context
	 *            Map containing the input parameters
	 *@return Map with the result of the service, the output parameters
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> alertloggerquerylarge(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();

		if (context.size() > 0) {
			for (Map.Entry<String, ?> entry : context.entrySet()) {
				Object cKey = entry.getKey();
				Object value = entry.getValue();

				Debug.logInfo(
						"In CONTEXTvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv: "
								+ cKey + " => " + value, module);
			}
		}
		//get param
		FastList listValues = (FastList) context.get("listValue");	
		EntityFindOptions findOptions=null;
		Timestamp startTime=(Timestamp)listValues.get(0);
	    Timestamp endTime=(Timestamp)listValues.get(1);
	    FastList whereCondition=new FastList();
	    try
	    {
	    whereCondition=(FastList)listValues.get(2);
	    }catch(Exception e1)
	    {
	    	
	    }
	    List<EntityCondition> entityConditionList = FastList.newInstance();
	    for (Object cond : whereCondition) {
	    	Object[] temv =(Object[])cond;
	    	String name=temv[0].toString().trim();
	    	String operation=temv[1].toString().trim();
	    	String v1=temv[2].toString().trim();
	    	if(name.equals("id"))
	    	{
	    		entityConditionList.add(EntityCondition.makeCondition("alertId", EntityOperator.EQUALS,v1 ));	
	    	}else if(name.equals("name"))
	    	{
	    		entityConditionList.add(EntityCondition.makeCondition("name", EntityOperator.EQUALS,v1 ));	
	    	}else if(name.equals("receiver"))
	    	{
	    		entityConditionList.add(EntityCondition.makeCondition("receiver", EntityOperator.EQUALS,v1 ));
	    	}else if(name.equals("type"))
	    	{
	    		entityConditionList.add(EntityCondition.makeCondition("alerttype", EntityOperator.EQUALS,v1 ));
	    	}
	    	else if(name.equals("alert_level"))
		    	{
		    		entityConditionList.add(EntityCondition.makeCondition("alert_level", EntityOperator.EQUALS,v1 ));
		    	}else
		    	{}
			
		}
	    int start=Integer.valueOf(listValues.get(3).toString()) ;
	    int number=Integer.valueOf(listValues.get(4).toString());
		Delegator delegator = dctx.getDelegator();
		
		
		entityConditionList.add(EntityCondition.makeCondition("alerttime", EntityOperator.BETWEEN,UtilMisc.toList(startTime,endTime)));
		
		
	    EntityCondition condition = EntityCondition.makeCondition(entityConditionList, EntityOperator.AND); 
	   // List<GenericValue> result= delegator.findList("AlertLog", condition, null,null, findOptions, false);
	    FastList OutDataList = FastList.newInstance();
	    DynamicViewEntity dve = new DynamicViewEntity();
	    Collection<String> fieldsToSelect = FastList.newInstance();
	    fieldsToSelect.add("alertId");
	    fieldsToSelect.add("alerttype");
	    fieldsToSelect.add("name");
	    fieldsToSelect.add("monitor");
	    fieldsToSelect.add("receiver");
	    fieldsToSelect.add("title");
	    fieldsToSelect.add("alerttime");
	    fieldsToSelect.add("result");
	    fieldsToSelect.add("alert_level");
	    fieldsToSelect.add("content");
	    fieldsToSelect.add("groupid");
	    fieldsToSelect.add("responsetime");
	    fieldsToSelect.add("responder");
	    fieldsToSelect.add("responsecontent");
	    fieldsToSelect.add("times");
	    fieldsToSelect.add("cleartime");
//	    List fieldsToSelect = UtilMisc.toList("alertId", "alerttype",
//	    "name","monitor","receiver","title");
	    dve.addMemberEntity("ALog", "AlertLog");
	    dve.addAliasAll("ALog", "");
	    EntityListIterator resultiterator= delegator.findListIteratorByCondition(dve,condition, null, fieldsToSelect, null, findOptions);
	    
	    
	    List<GenericValue> result= resultiterator.getPartialList(start, number);
	    for (GenericValue genericValue : result) {
	    	 FastList childlist = FastList.newInstance();
	        childlist.add(0, genericValue.get("alertId"));
	        childlist.add(1, genericValue.get("alerttype"));
	        childlist.add(2, genericValue.get("name"));
	        childlist.add(3, genericValue.get("monitor") );
	        childlist.add(4, genericValue.get("receiver") );
	        childlist.add(5, genericValue.get("title") );
	        childlist.add(6, genericValue.get("alerttime") );
	        childlist.add(7, genericValue.get("result") );
	        childlist.add(8, genericValue.get("alert_level") );
	        childlist.add(9, genericValue.get("content") );
	        childlist.add(10, genericValue.get("groupid") );
	        childlist.add(11, genericValue.get("responsetime") );
	        childlist.add(12, genericValue.get("responder") );
	        childlist.add(13, genericValue.get("responsecontent") );
	        childlist.add(14, genericValue.get("cleartime") );
	        childlist.add(15, genericValue.get("times") );
	        OutDataList.add(childlist);
		}
	    response.put("loggervalues",OutDataList );
		//("", entityCondition, fieldsToSelect, orderBy, findOptions, useCache); 
		
//		System.out.println("----- SVCvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv: "
//				+ dctx.getName() + " -----");
		return response;
	}
	/**
	 * Generic Test Service
	 * 
	 * @param dctx
	 *            The DispatchContext that this service is operating in
	 *@param context
	 *            Map containing the input parameters
	 *@return Map with the result of the service, the output parameters
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> alertloggerquery(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();

		if (context.size() > 0) {
			for (Map.Entry<String, ?> entry : context.entrySet()) {
				Object cKey = entry.getKey();
				Object value = entry.getValue();

				Debug.logInfo(
						"In CONTEXTvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv: "
								+ cKey + " => " + value, module);
			}
		}
		//get param
		FastList listValues = (FastList) context.get("listValue");	
		EntityFindOptions findOptions=null;
		String alertid=listValues.get(0).toString();
		
		Object Condition= listValues.get(3);
		Delegator delegator = dctx.getDelegator();
		Timestamp startTime=(Timestamp)listValues.get(1);
	    Timestamp endTime=(Timestamp)listValues.get(2);
		List<EntityCondition> entityConditionList = FastList.newInstance();
		if(!alertid.equals(""))
		{
			entityConditionList.add(EntityCondition.makeCondition("alertId", EntityOperator.EQUALS,alertid ));	
		}
		entityConditionList.add(EntityCondition.makeCondition("alerttime", EntityOperator.BETWEEN,UtilMisc.toList(startTime,endTime)));
		
		
	    EntityCondition condition = EntityCondition.makeCondition(entityConditionList, EntityOperator.AND); 
	    List<GenericValue> result= delegator.findList("OperationAttributeLog", condition, null,UtilMisc.toList("logTime DESC"), findOptions, false);
	    FastList OutDataList = FastList.newInstance();
	   
	    for (GenericValue genericValue : result) {
	    	 FastList childlist = FastList.newInstance();
	        childlist.add(0, genericValue.get("operationId"));
	        childlist.add(1, genericValue.get("logTime"));
	        childlist.add(2, genericValue.get("category"));
	        childlist.add(3, genericValue.get("description") );
	        childlist.add(4, genericValue.get("measurement") );
	        childlist.add(5, genericValue.get("name") );
	        OutDataList.add(childlist);
		}
	    response.put("loggervalues",OutDataList );
		//("", entityCondition, fieldsToSelect, orderBy, findOptions, useCache); 
		
//		System.out.println("----- SVCvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv: "
//				+ dctx.getName() + " -----");
		return response;
	}
	
    /**
	 * Generic Test Service
	 * 
	 * @param dctx
	 *            The DispatchContext that this service is operating in
	 *@param context
	 *            Map containing the input parameters
	 *@return Map with the result of the service, the output parameters
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> maketestdata(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();

		if (context.size() > 0) {
			for (Map.Entry<String, ?> entry : context.entrySet()) {
				Object cKey = entry.getKey();
				Object value = entry.getValue();

				Debug.logInfo(
						"In CONTEXTvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv: "
								+ cKey + " => " + value, module);
			}
		}
		//insert master data
		Delegator delegator = dctx.getDelegator();
		FastList listValues = (FastList) context.get("listValue");				
		String strCount =listValues.get(0).toString();
		int nCount = Integer.parseInt(strCount);
//		List<String> lstIds = StringUtil.split(operationIds, ",");
		
		List<EntityCondition> entityConditionList1 = FastList.newInstance();
		entityConditionList1.add(EntityCondition.makeCondition("ciTypeId", EntityOperator.EQUALS, "monitor"));        
	
	    EntityCondition condition1 = EntityCondition.makeCondition(entityConditionList1, EntityOperator.AND);
	    List<GenericValue> result1 = delegator.findList("CI", condition1, null, null, null, false);
	    List<GenericValue> values = FastList.newInstance(); 
	    for(GenericValue monitor :result1)
		{
			List<EntityCondition> entityConditionList = FastList.newInstance();
			entityConditionList.add(EntityCondition.makeCondition("operationId", EntityOperator.EQUALS, monitor.get("ciId")));        
		
		    EntityCondition condition = EntityCondition.makeCondition(entityConditionList, EntityOperator.AND); 
		    List<GenericValue> result= delegator.findList("OperationAttributeLog", condition, null,UtilMisc.toList("logTime ASC"), null, false);
		    FastList OutDataList = FastList.newInstance();
		   
		    if(result.size() > 0)
		    {
		    	GenericValue src = result.get(0);
//		    for (GenericValue src : result) 
//		    {
				String operationId=src.get("operationId").toString();
				Object name=src.get("name");
		        Timestamp logTime=(Timestamp)src.get("logTime");
				String category=src.get("category").toString();
				Object description=src.get("description");
				Object measurement=src.get("measurement");
				for(int i=0; i < nCount; i++)
				{
					logTime.setTime(logTime.getTime() - 1000*60*5);
					String logId = delegator.getNextSeqId("OperationAttributeLog");
					Map<String, String> fields = UtilMisc.toMap("logId", logId,
							"operationId",operationId,
							"logTime",logTime,
							"name",name,
							"category",category,
							"description",description,
							"measurement",measurement);
					
					GenericValue log = delegator.makeValue("OperationAttributeLog", fields);
					
					delegator.create(log);
//					values.add(log);
					
				}
//				delegator.storeAll(values);
//				values.clear();
//		    }
		    }
		}
		
		
		response.put("listValue", listValues);
//		System.out.println("----- SVCvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv: "
//				+ dctx.getName() + " -----");
		return response;
	}

	
}