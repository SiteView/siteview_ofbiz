package com.dragonflow.siteview.report;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javolution.util.FastList;
import javolution.util.FastMap;

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

public class ReportDataServices {
    public final static String module = ReportDataServices.class.getName();
    static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 时段对比报告查询
     * @param dctx
     * @param context
     * @return
     * @throws GenericEntityException
     */
    public static Map<String, Object> queryPeriodReport(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String datetime = context.get("starttime").toString();
		Date d = null;
		try {
			d = format.parse(datetime);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String tableindex = sdf.format(d);
		tableindex = "_" + tableindex.replaceAll("-", "_");//获取表索引
		// 获取参数
		String tablename = (String) context.get("tablename");
		tablename = tablename+tableindex;
		int fromn = (Integer) context.get("from");
		int ton = (Integer) context.get("to");
		String mid = (String) context.get("id");
		String starttime=(String) context.get("starttime");
		String endtime=(String) context.get("endtime");
		
		
		// 过滤条件
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
		
		EntityCondition conditionid = EntityCondition.makeCondition(
				entityConditionListid, EntityOperator.OR);
		entityConditionList.add(conditionid);
		Timestamp startTime2=Timestamp.valueOf(starttime);
	    Timestamp endTime2=Timestamp.valueOf(endtime);
		entityConditionList.add(EntityCondition.makeCondition("logTime", EntityOperator.BETWEEN,UtilMisc.toList(startTime2,endTime2)));
		
		EntityCondition condition = EntityCondition.makeCondition(
				entityConditionList, EntityOperator.AND);
		FastMap OutDataList = FastMap.newInstance();
		DynamicViewEntity dve = new DynamicViewEntity();
		Collection<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("operationId");
		fieldsToSelect.add("logTime");
		fieldsToSelect.add("category");
		fieldsToSelect.add("description");
		fieldsToSelect.add("measurement");
		fieldsToSelect.add("name");
		dve.addMemberEntity("OperLog", "OperationAttributeLog"+tableindex);
		dve.addAliasAll("OperLog", "");
		Delegator delegator = dctx.getDelegator();
		List<String> orderBy=UtilMisc.toList("-logTime");
		EntityListIterator resultiterator = delegator
				.findListIteratorByCondition(dve, condition, null,
						fieldsToSelect, orderBy, null);
		List<GenericValue> result = resultiterator.getPartialList(fromn, ton
				- fromn);
		int i = 0;
		String maxvaluetime="";
		String MonitorName="";
		FastMap detaildstr=new FastMap();
//		String latestCreateTime="";
		int nodata=0;
		int good=0;
		int warm=0;
		String id="";
		int error=0;
		
		List<Object> list = new ArrayList<Object>();
		//query monitor type
		String monitortype="";
		List<String> monitortypes=new ArrayList<String>();
		
		monitortype = getMonitorType(mid);
		List nodes=null;
		
		
		try {
			if(mid.split(",").length>1) {
				for(int idsize=0;idsize<monitortypes.size();idsize++){
					list.add(new OtpErlangAtom(monitortypes.get(idsize)));
					nodes=(List) OtpGateway.getOtpInterface().call("api_monitor_template", "get_template", list);
				}
			}else{
				list.add(new OtpErlangAtom(monitortype));
				nodes = (List) OtpGateway.getOtpInterface().call("api_monitor_template", "get_template", list);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LinkedHashSet<Map<String, String>> m_return_items =new  LinkedHashSet<Map<String,String>>();
		m_return_items =get_Return_Items(monitortype,mid,nodes);
			
		String[]  ReturnName=new String[m_return_items.size()];
		double latest_value = 0.0;
		String lasestcreatedtime = "";
		StringBuffer[] detail=new StringBuffer[m_return_items.size()];
		double[] max = new double[m_return_items.size()];
		double[] min = new double[m_return_items.size()];
		double[] avg = new double[m_return_items.size()];
		double[] latest=new double[m_return_items.size()];
		String[] nid = new String[m_return_items.size()];
		double[] totleValue = new double[m_return_items.size()];
		Date logtime = new Date();
		Date lastcratedtime = new Date();
		String latestStatus ="";
		String latestDstr="";
		for (GenericValue genericValue : result) {
			StringBuffer detailds=new StringBuffer();
			if (genericValue.get("category").toString().equals("nodata")){
				nodata++;
			}else if(genericValue.get("category").toString().equals("good")){
				good++;
			}else if(genericValue.get("category").toString().equals("warning")){
				warm++;
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
				value =  map.get(ReturnNameval.trim());
				ReturnName[mapsize] = ReturnNameval;
				nid[mapsize] = (mapsize + ")" + genericValue.get("operationId"))
						.toString();
				if (value == null) {
					value = 0.0;
				}
				if(value instanceof Double){
					value=(Double)value;
					totleValue[mapsize] = ((Double) value) + totleValue[mapsize];
					if ((Double) value < max[mapsize]) {
						max[mapsize] = max[mapsize];
						maxvaluetime = genericValue.get("logTime").toString();
					} else {
						max[mapsize] = (Double) value;
					}
					if ((Double) value > min[mapsize] && min[mapsize]!=0) {
						min[mapsize] = min[mapsize];
					} else {
						min[mapsize] = (Double) value;
					}
					if(latest[mapsize]==0.0){
						latest[mapsize]=(Double) value;
						try {
							latestStatus = genericValue.get("category").toString();
							latestDstr = genericValue.get("description").toString();
							
						} catch (Exception e) {
							latestStatus = "nodata";
							latestDstr = "no data";
						}
					}
//					if (newdate.after(logtime)) {
//						logtime = newdate;
//						latest[mapsize] = (Double) value;
//						latestStatus = genericValue.get("category").toString();
//						latestDstr = genericValue.get("description").toString();
//					}
				}else if(value instanceof Long){
					value=(Long)value;
					if ((Long) value < max[mapsize]) {
						max[mapsize] = max[mapsize];
						maxvaluetime = genericValue.get("logTime").toString();
					} else {
						max[mapsize] = (Long) value;
					}
					if ((Long) value > min[mapsize] && min[mapsize]!=0.0) {
						min[mapsize] = min[mapsize];
					} else {
						min[mapsize] = (Long) value;
					}
					if(latest[mapsize]==0.0){
						latest[mapsize]=(Long) value;
						try {
							latestStatus = genericValue.get("category").toString();
							latestDstr = genericValue.get("description").toString();
							
						} catch (Exception e) {
							latestStatus = "nodata";
							latestDstr = "no data";
						}
					}
					if (newdate.after(logtime)) {
//						logtime = newdate;
//						latest[mapsize] = (Long) value;
//						latestStatus = genericValue.get("category").toString();
//						latestDstr = genericValue.get("description").toString();
					}else{
						value=(Long) value;
					}
					totleValue[mapsize] = ((Long) value) + totleValue[mapsize];
				}
				if (newdate.after(lastcratedtime)) {
					lastcratedtime = newdate;
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
			}else if(statemessage.contains("error")){
				statemessage= "error";
			}else if(statemessage.contains("disable")){
				statemessage= "disable";
			}else if(statemessage.contains("warning")){
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
			detaildstr.put(timekey, detailds.toString().replaceAll("\"", ""));
			i++;
		}
		//-------part 1--------
		resultiterator.close();
	NumberFormat number=NumberFormat.getNumberInstance();
	number.setMaximumFractionDigits(2);
		for(int outdatamapsize=0;outdatamapsize<min.length;outdatamapsize++){
			FastMap OutDataMap = FastMap.newInstance();
			FastMap OutDataMap2 = FastMap.newInstance(); 
			OutDataMap.put("min", String.valueOf(number.format(min[outdatamapsize])).replaceAll(",", ""));
			OutDataMap.put("max", String.valueOf(number.format(max[outdatamapsize])).replaceAll(",", ""));
			OutDataMap.put("average", String.valueOf(number.format(totleValue[outdatamapsize]/i)).replaceAll(",", ""));
			OutDataMap.put("sv_drawimage", "1");
			OutDataMap.put("sv_drawtable", "1");
			/**
			 * time max
			 */
			OutDataMap.put("when_max",maxvaluetime);
			OutDataMap.put("MonitorName", MonitorName.replaceAll("\"", ""));
			OutDataMap.put("sv_type", "numeric");
			OutDataMap.put("ReturnTitle", ReturnName[outdatamapsize].toString().replaceAll("\"", ""));
			OutDataMap.put("ReturnName", ReturnName[outdatamapsize].toString().replaceAll("\"", ""));
			OutDataMap.put("sv_drawmeasure", "1");
			OutDataMap.put("sv_primary", "1");
			OutDataMap.put("sv_baseline", "1");
//			OutDataMap.put("latest",String.valueOf(number.format(latest[outdatamapsize])).replaceAll(",", ""));
			String numstring=detail[outdatamapsize].substring(detail[outdatamapsize].indexOf("=")+1,detail[outdatamapsize].indexOf(",")).replaceAll(",", "");
			Double newnumber = 0.0;
			if (numstring.equals("not_found")) {
				newnumber = 0.0;
			}else{
			   newnumber=Double.valueOf(numstring.equals("n/a") ? "0" : numstring);
			}
			OutDataMap.put("latest",String.valueOf(number.format(newnumber)));
			OutDataMap.put("detail",detail[outdatamapsize].toString());
			OutDataList.put("(Return_"+nid[outdatamapsize], OutDataMap);
//			OutDataList.put("",OutDataMap2);
		}
	//-------part 2--------
		OutDataList.put("(dstr)"+id, detaildstr);
	//-------part 3--------
		FastMap part = FastMap.newInstance();
		String lastcratedtime1 =format.format(lastcratedtime);
		part.put("latestCreateTime", lastcratedtime1);
		part.put("MonitorName", MonitorName.replaceAll("\"", ""));
		part.put("errorPercent",String.valueOf( error/result.size()).replaceAll(",", ""));
		part.put("warnPercent", String.valueOf( warm/result.size()).replaceAll(",", ""));
		part.put("okPercent", String.valueOf( good/result.size()).replaceAll(",", ""));
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
     * contrast
     * @param dctx
     * @param context
     * @return
     * @throws GenericEntityException
     */
    
    public static Map<String, Object> statisticReportLog(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String tableindex = sdf.format(new Date());
		tableindex = "_" + tableindex.replaceAll("-", "_");//获取表索引
		// 获取参数
		String tablename = "OperationAttributeLog"+tableindex;
		int fromn = (Integer) context.get("from");
		int ton = (Integer) context.get("to");
		String mid = (String) context.get("id");
		String starttime=(String) context.get("starttime");
		String endtime=(String) context.get("endtime");
		
		
		// 过滤条件
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
		
		EntityCondition conditionid = EntityCondition.makeCondition(
				entityConditionListid, EntityOperator.OR);
		entityConditionList.add(conditionid);
		Timestamp startTime2=Timestamp.valueOf(starttime);
	    Timestamp endTime2=Timestamp.valueOf(endtime);
		entityConditionList.add(EntityCondition.makeCondition("logTime", EntityOperator.BETWEEN,UtilMisc.toList(startTime2,endTime2)));
		
		EntityCondition condition = EntityCondition.makeCondition(
				entityConditionList, EntityOperator.AND);
		FastMap OutDataList = FastMap.newInstance();
		DynamicViewEntity dve = new DynamicViewEntity();
		Collection<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("operationId");
		fieldsToSelect.add("logTime");
		fieldsToSelect.add("category");
		fieldsToSelect.add("description");
		fieldsToSelect.add("measurement");
		fieldsToSelect.add("name");
		dve.addMemberEntity("OperLog", tablename);
		dve.addAliasAll("OperLog", "");
		Delegator delegator = dctx.getDelegator();
		List<String> orderBy=UtilMisc.toList("-logTime");
		EntityListIterator resultiterator = delegator
				.findListIteratorByCondition(dve, condition, null,
						fieldsToSelect, orderBy, null);
		List<GenericValue> result = resultiterator.getPartialList(fromn, ton
				- fromn);
		int i = 0;
		String maxvaluetime="";
		String MonitorName="";
		FastMap detaildstr=new FastMap();
//		String latestCreateTime="";
		int nodata=0;
		int good=0;
		int warm=0;
		String id="";
		int error=0;
		
		List<Object> list = new ArrayList<Object>();
		//query monitor type
		String monitortype="";
		List<String> monitortypes=new ArrayList<String>();
		
		monitortype = getMonitorType(mid);
		List nodes=null;
		
		
		try {
			if(mid.split(",").length>1) {
				for(int idsize=0;idsize<monitortypes.size();idsize++){
					list.add(new OtpErlangAtom(monitortypes.get(idsize)));
					nodes=(List) OtpGateway.getOtpInterface().call("api_monitor_template", "get_template", list);
				}
			}else{
				list.add(new OtpErlangAtom(monitortype));
				nodes = (List) OtpGateway.getOtpInterface().call("api_monitor_template", "get_template", list);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LinkedHashSet<Map<String, String>> m_return_items =new  LinkedHashSet<Map<String,String>>();
		m_return_items =get_Return_Items(monitortype,mid,nodes);
			
		String[]  ReturnName=new String[m_return_items.size()];
		double latest_value = 0.0;
		String lasestcreatedtime = "";
		StringBuffer[] detail=new StringBuffer[m_return_items.size()];
		double[] max = new double[m_return_items.size()];
		double[] min = new double[m_return_items.size()];
		double[] avg = new double[m_return_items.size()];
		double[] latest=new double[m_return_items.size()];
		String[] nid = new String[m_return_items.size()];
		double[] totleValue = new double[m_return_items.size()];
		Date logtime = new Date();
		Date lastcratedtime = new Date();
		String latestStatus ="";
		String latestDstr="";
		for (GenericValue genericValue : result) {
			StringBuffer detailds=new StringBuffer();
			if (genericValue.get("category").toString().equals("nodata")){
				nodata++;
			}else if(genericValue.get("category").toString().equals("good")){
				good++;
			}else if(genericValue.get("category").toString().equals("warning")){
				warm++;
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
				value =  map.get(ReturnNameval.trim());
				ReturnName[mapsize] = ReturnNameval;
				nid[mapsize] = (mapsize + ")" + genericValue.get("operationId"))
						.toString();
				if (value == null) {
					value = 0.0;
				}
				if(value instanceof Double){
					value=(Double)value;
					totleValue[mapsize] = ((Double) value) + totleValue[mapsize];
					if ((Double) value < max[mapsize]) {
						max[mapsize] = max[mapsize];
						maxvaluetime = genericValue.get("logTime").toString();
					} else {
						max[mapsize] = (Double) value;
					}
					if ((Double) value > min[mapsize] && min[mapsize]!=0) {
						min[mapsize] = min[mapsize];
					} else {
						min[mapsize] = (Double) value;
					}
					if(latest[mapsize]==0.0){
						latest[mapsize]=(Double) value;
						try {
							latestStatus = genericValue.get("category").toString();
							latestDstr = genericValue.get("description").toString();
							
						} catch (Exception e) {
							latestStatus = "nodata";
							latestDstr = "no data";
						}
					}
//					if (newdate.after(logtime)) {
//						logtime = newdate;
//						latest[mapsize] = (Double) value;
//						latestStatus = genericValue.get("category").toString();
//						latestDstr = genericValue.get("description").toString();
//					}
				}else if(value instanceof Long){
					value=(Long)value;
					if ((Long) value < max[mapsize]) {
						max[mapsize] = max[mapsize];
						maxvaluetime = genericValue.get("logTime").toString();
					} else {
						max[mapsize] = (Long) value;
					}
					if ((Long) value > min[mapsize] && min[mapsize]!=0.0) {
						min[mapsize] = min[mapsize];
					} else {
						min[mapsize] = (Long) value;
					}
					if(latest[mapsize]==0.0){
						latest[mapsize]=(Long) value;
						try {
							latestStatus = genericValue.get("category").toString();
							latestDstr = genericValue.get("description").toString();
							
						} catch (Exception e) {
							latestStatus = "nodata";
							latestDstr = "no data";
						}
					}
					if (newdate.after(logtime)) {
//						logtime = newdate;
//						latest[mapsize] = (Long) value;
//						latestStatus = genericValue.get("category").toString();
//						latestDstr = genericValue.get("description").toString();
					}else{
						value=(Long) value;
					}
					totleValue[mapsize] = ((Long) value) + totleValue[mapsize];
				}
				if (newdate.after(lastcratedtime)) {
					lastcratedtime = newdate;
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
			}else if(statemessage.contains("error")){
				statemessage= "error";
			}else if(statemessage.contains("disable")){
				statemessage= "disable";
			}else if(statemessage.contains("warning")){
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
			detaildstr.put(timekey, detailds.toString().replaceAll("\"", ""));
			i++;
		}
		//-------part 1--------
		resultiterator.close();
	NumberFormat number=NumberFormat.getNumberInstance();
	number.setMaximumFractionDigits(2);
		for(int outdatamapsize=0;outdatamapsize<min.length;outdatamapsize++){
			FastMap OutDataMap = FastMap.newInstance();
			FastMap OutDataMap2 = FastMap.newInstance(); 
			OutDataMap.put("min", String.valueOf(number.format(min[outdatamapsize])).replaceAll(",", ""));
			OutDataMap.put("max", String.valueOf(number.format(max[outdatamapsize])).replaceAll(",", ""));
			OutDataMap.put("average", String.valueOf(number.format(totleValue[outdatamapsize]/i)).replaceAll(",", ""));
			OutDataMap.put("sv_drawimage", "1");
			OutDataMap.put("sv_drawtable", "1");
			/**
			 * time max
			 */
			OutDataMap.put("when_max",maxvaluetime);
			OutDataMap.put("MonitorName", MonitorName.replaceAll("\"", ""));
			OutDataMap.put("sv_type", "numeric");
			OutDataMap.put("ReturnTitle", ReturnName[outdatamapsize].toString().replaceAll("\"", ""));
			OutDataMap.put("ReturnName", ReturnName[outdatamapsize].toString().replaceAll("\"", ""));
			OutDataMap.put("sv_drawmeasure", "1");
			OutDataMap.put("sv_primary", "1");
			OutDataMap.put("sv_baseline", "1");
//			OutDataMap.put("latest",String.valueOf(number.format(latest[outdatamapsize])).replaceAll(",", ""));
			String numstring=detail[outdatamapsize].substring(detail[outdatamapsize].indexOf("=")+1,detail[outdatamapsize].indexOf(",")).replaceAll(",", "");
			Double newnumber = 0.0;
			if (numstring.equals("not_found")) {
				newnumber = 0.0;
			}else{
			   newnumber=Double.valueOf(numstring.equals("n/a") ? "0" : numstring);
			}
			OutDataMap.put("latest",String.valueOf(number.format(newnumber)));
			OutDataMap.put("detail",detail[outdatamapsize].toString());
			OutDataList.put("(Return_"+nid[outdatamapsize], OutDataMap);
//			OutDataList.put("",OutDataMap2);
		}
	//-------part 2--------
		OutDataList.put("(dstr)"+id, detaildstr);
	//-------part 3--------
		FastMap part = FastMap.newInstance();
		String lastcratedtime1 =format.format(lastcratedtime);
		part.put("latestCreateTime", lastcratedtime1);
		part.put("MonitorName", MonitorName.replaceAll("\"", ""));
		part.put("errorPercent",String.valueOf( error/result.size()).replaceAll(",", ""));
		part.put("warnPercent", String.valueOf( warm/result.size()).replaceAll(",", ""));
		part.put("okPercent", String.valueOf( good/result.size()).replaceAll(",", ""));
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
     * contrast
     * @param dctx
     * @param context
     * @return
     * @throws GenericEntityException
     */
    
    public static Map<String, Object> queryFishEyeService(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String starttime=(String) context.get("starttime");
		String endtime=(String) context.get("endtime");
		Date d = null;
		try {
			 d = format.parse(endtime);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String tableindex = sdf.format(d.getTime());
		tableindex = "_" + tableindex.replaceAll("-", "_");//获取表索引
		// 获取参数
		String tablename = "OperationAttributeLog"+tableindex;
		int fromn = (Integer) context.get("from");
		int ton = (Integer) context.get("to");
		String mid = (String) context.get("id");
		
		
		// 过滤条件
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
		
		EntityCondition conditionid = EntityCondition.makeCondition(
				entityConditionListid, EntityOperator.OR);
		entityConditionList.add(conditionid);
		Timestamp startTime2=Timestamp.valueOf(starttime);
	    Timestamp endTime2=Timestamp.valueOf(endtime);
		entityConditionList.add(EntityCondition.makeCondition("logTime", EntityOperator.BETWEEN,UtilMisc.toList(startTime2,endTime2)));
		
		EntityCondition condition = EntityCondition.makeCondition(
				entityConditionList, EntityOperator.AND);
		FastMap OutDataList = FastMap.newInstance();
		DynamicViewEntity dve = new DynamicViewEntity();
		Collection<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("operationId");
		fieldsToSelect.add("logTime");
		fieldsToSelect.add("category");
		fieldsToSelect.add("description");
		fieldsToSelect.add("measurement");
		fieldsToSelect.add("name");
		dve.addMemberEntity("OperLog", tablename);
		System.out.println("鱼眼视图查询表名："+tablename);
		dve.addAliasAll("OperLog", "");
		Delegator delegator = dctx.getDelegator();
		List<String> orderBy=UtilMisc.toList("-logTime");
		EntityListIterator resultiterator = delegator
				.findListIteratorByCondition(dve, condition, null,
						fieldsToSelect, orderBy, null);
		List<GenericValue> result = resultiterator.getPartialList(fromn, ton
				- fromn);
		int i = 0;
		String maxvaluetime="";
		String MonitorName="";
		FastMap detaildstr=new FastMap();
//		String latestCreateTime="";
		int nodata=0;
		int good=0;
		int warm=0;
		String id="";
		int error=0;
		
		List<Object> list = new ArrayList<Object>();
		//query monitor type
		String monitortype="";
		List<String> monitortypes=new ArrayList<String>();
		
		monitortype = getMonitorType(mid);
		List nodes=null;
		
		
		try {
			if(mid.split(",").length>1) {
				for(int idsize=0;idsize<monitortypes.size();idsize++){
					list.add(new OtpErlangAtom(monitortypes.get(idsize)));
					nodes=(List) OtpGateway.getOtpInterface().call("api_monitor_template", "get_template", list);
				}
			}else{
				list.add(new OtpErlangAtom(monitortype));
				nodes = (List) OtpGateway.getOtpInterface().call("api_monitor_template", "get_template", list);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LinkedHashSet<Map<String, String>> m_return_items =new  LinkedHashSet<Map<String,String>>();
		m_return_items =get_Return_Items(monitortype,mid,nodes);
			
		String[]  ReturnName=new String[m_return_items.size()];
		double latest_value = 0.0;
		String lasestcreatedtime = "";
		StringBuffer[] detail=new StringBuffer[m_return_items.size()];
		double[] max = new double[m_return_items.size()];
		double[] min = new double[m_return_items.size()];
		double[] avg = new double[m_return_items.size()];
		double[] latest=new double[m_return_items.size()];
		String[] nid = new String[m_return_items.size()];
		double[] totleValue = new double[m_return_items.size()];
		Date logtime = new Date();
		Date lastcratedtime = new Date();
		String latestStatus ="";
		String latestDstr="";
		for (GenericValue genericValue : result) {
			StringBuffer detailds=new StringBuffer();
			if (genericValue.get("category").toString().equals("nodata")){
				nodata++;
			}else if(genericValue.get("category").toString().equals("good")){
				good++;
			}else if(genericValue.get("category").toString().equals("warning")){
				warm++;
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
				value =  map.get(ReturnNameval.trim());
				ReturnName[mapsize] = ReturnNameval;
				nid[mapsize] = (mapsize + ")" + genericValue.get("operationId"))
						.toString();
				if (value == null) {
					value = 0.0;
				}
				if(value instanceof Double){
					value=(Double)value;
					totleValue[mapsize] = ((Double) value) + totleValue[mapsize];
					if ((Double) value < max[mapsize]) {
						max[mapsize] = max[mapsize];
						maxvaluetime = genericValue.get("logTime").toString();
					} else {
						max[mapsize] = (Double) value;
					}
					if ((Double) value > min[mapsize] && min[mapsize]!=0) {
						min[mapsize] = min[mapsize];
					} else {
						min[mapsize] = (Double) value;
					}
					if(latest[mapsize]==0.0){
						latest[mapsize]=(Double) value;
						try {
							latestStatus = genericValue.get("category").toString();
							latestDstr = genericValue.get("description").toString();
							
						} catch (Exception e) {
							latestStatus = "nodata";
							latestDstr = "no data";
						}
					}
//					if (newdate.after(logtime)) {
//						logtime = newdate;
//						latest[mapsize] = (Double) value;
//						latestStatus = genericValue.get("category").toString();
//						latestDstr = genericValue.get("description").toString();
//					}
				}else if(value instanceof Long){
					value=(Long)value;
					if ((Long) value < max[mapsize]) {
						max[mapsize] = max[mapsize];
						maxvaluetime = genericValue.get("logTime").toString();
					} else {
						max[mapsize] = (Long) value;
					}
					if ((Long) value > min[mapsize] && min[mapsize]!=0.0) {
						min[mapsize] = min[mapsize];
					} else {
						min[mapsize] = (Long) value;
					}
					if(latest[mapsize]==0.0){
						latest[mapsize]=(Long) value;
						try {
							latestStatus = genericValue.get("category").toString();
							latestDstr = genericValue.get("description").toString();
							
						} catch (Exception e) {
							latestStatus = "nodata";
							latestDstr = "no data";
						}
					}
					if (newdate.after(logtime)) {
//						logtime = newdate;
//						latest[mapsize] = (Long) value;
//						latestStatus = genericValue.get("category").toString();
//						latestDstr = genericValue.get("description").toString();
					}else{
						value=(Long) value;
					}
					totleValue[mapsize] = ((Long) value) + totleValue[mapsize];
				}
				if (newdate.after(lastcratedtime)) {
					lastcratedtime = newdate;
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
			}else if(statemessage.contains("error")){
				statemessage= "error";
			}else if(statemessage.contains("disable")){
				statemessage= "disable";
			}else if(statemessage.contains("warning")){
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
			detaildstr.put(timekey, detailds.toString().replaceAll("\"", ""));
			i++;
		}
		//-------part 1--------
		resultiterator.close();
	NumberFormat number=NumberFormat.getNumberInstance();
	number.setMaximumFractionDigits(2);
		for(int outdatamapsize=0;outdatamapsize<min.length;outdatamapsize++){
			FastMap OutDataMap = FastMap.newInstance();
			FastMap OutDataMap2 = FastMap.newInstance(); 
			OutDataMap.put("min", String.valueOf(number.format(min[outdatamapsize])).replaceAll(",", ""));
			OutDataMap.put("max", String.valueOf(number.format(max[outdatamapsize])).replaceAll(",", ""));
			OutDataMap.put("average", String.valueOf(number.format(totleValue[outdatamapsize]/i)).replaceAll(",", ""));
			OutDataMap.put("sv_drawimage", "1");
			OutDataMap.put("sv_drawtable", "1");
			/**
			 * time max
			 */
			OutDataMap.put("when_max",maxvaluetime);
			OutDataMap.put("MonitorName", MonitorName.replaceAll("\"", ""));
			OutDataMap.put("sv_type", "numeric");
			OutDataMap.put("ReturnTitle", ReturnName[outdatamapsize].toString().replaceAll("\"", ""));
			OutDataMap.put("ReturnName", ReturnName[outdatamapsize].toString().replaceAll("\"", ""));
			OutDataMap.put("sv_drawmeasure", "1");
			OutDataMap.put("sv_primary", "1");
			OutDataMap.put("sv_baseline", "1");
//			OutDataMap.put("latest",String.valueOf(number.format(latest[outdatamapsize])).replaceAll(",", ""));
			String numstring=detail[outdatamapsize].substring(detail[outdatamapsize].indexOf("=")+1,detail[outdatamapsize].indexOf(",")).replaceAll(",", "");
			Double newnumber = 0.0;
			if (numstring.equals("not_found")) {
				newnumber = 0.0;
			}else{
			   newnumber=Double.valueOf(numstring.equals("n/a") ? "0" : numstring);
			}
			OutDataMap.put("latest",String.valueOf(number.format(newnumber)));
			OutDataMap.put("detail",detail[outdatamapsize].toString());
			OutDataList.put("(Return_"+nid[outdatamapsize], OutDataMap);
//			OutDataList.put("",OutDataMap2);
		}
	//-------part 2--------
		OutDataList.put("(dstr)"+id, detaildstr);
	//-------part 3--------
		FastMap part = FastMap.newInstance();
		String lastcratedtime1 =format.format(lastcratedtime);
		part.put("latestCreateTime", lastcratedtime1);
		part.put("MonitorName", MonitorName.replaceAll("\"", ""));
		part.put("errorPercent",String.valueOf( error/result.size()).replaceAll(",", ""));
		part.put("warnPercent", String.valueOf( warm/result.size()).replaceAll(",", ""));
		part.put("okPercent", String.valueOf( good/result.size()).replaceAll(",", ""));
		part.put("latestStatus", latestStatus);
		part.put("latestDstr", latestDstr);
		for (int ReturnNamesize=0;ReturnNamesize<ReturnName.length;ReturnNamesize++){
			part.put("(Return_"+ReturnNamesize+")"+id, "ReturnValue");
		}
		OutDataList.put(id, part);
		response.put("loggervalues", OutDataList);
		return response;

	} 
    
    
    
	private static String getMonitorType(String id) {
		String classname = "";
		List<Object> list = new ArrayList<Object>();
		FastMap nodes = null;
		list.add(new OtpErlangAtom(id));
		try {
			nodes = (FastMap) OtpGateway.getOtpInterface().call("api_monitor",
					"info", list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		classname = nodes.get("class").toString();
		return classname;
	}


	public static Map<String, Object> contrastReportLog(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
    	return null;
    }
    
    private static void reportlog() {
  		// TODO Auto-generated method stub
  		
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
	    FastList OutDataList = FastList.newInstance();
	    
	    for(int index=result.size()-1;index>=0;index--)
	    {   GenericValue genericValue=result.get(index);
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
	//
	//rmi 支持大数据量的报告，分页
	// where 没实现
	@SuppressWarnings("unused")
	public static Map<String, Object> queryReportLog(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		// 获取参数
		String tablename = (String) context.get("tablename");
		tablename = "OperationAttributeLog";
		int fromn = (Integer) context.get("from");
		int ton = (Integer) context.get("to");
		String mid = (String) context.get("id");
		String orderby = (String) context.get("orderby");
		String where = (String) context.get("where");
		// 过滤条件
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
		FastMap OutDataList = FastMap.newInstance();
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
		double[] totleValue = new double[3];
		String maxvaluetime="";
		String MonitorName="";
		FastMap detaildstr=new FastMap();
//		String latestCreateTime="";
		int nodata=0;
		int good=0;
		int warm=0;
		String id="";
		int error=0;
		
		List<Object> list = new ArrayList<Object>();
		//query monitor type
		String monitortype="";
		try {
			monitortype = getMonitorType(dctx, context, mid);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		list.add(new OtpErlangAtom(monitortype));
//		list.add(new OtpErlangList());
		List nodes=null;
		try {
			nodes = (List) OtpGateway.getOtpInterface().call("api_monitor_template", "get_template", list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		Object[] temv =(Object[])nodes;
//		for(Object o:temv){
//			System.out.println(o);
//		}
		LinkedHashSet<Map<String, String>> m_return_items =new  LinkedHashSet<Map<String,String>>();
		m_return_items =get_Return_Items(nodes);
			
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
		Date lastcratedtime = new Date();
		String latestStatus ="";
		String latestDstr="";
		for (GenericValue genericValue : result) {
			StringBuffer detailds=new StringBuffer();
			if (genericValue.get("category").toString().equals("nodata")){
				nodata++;
			}else if(genericValue.get("category").toString().equals("good")){
				good++;
			}else if(genericValue.get("category").toString().equals("warning")){
				warm++;
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
					totleValue[mapsize] = ((Double) value) + totleValue[mapsize];
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
						try {
							latestStatus = genericValue.get("category").toString();
							latestDstr = genericValue.get("description").toString();
							
						} catch (Exception e) {
							latestStatus = "nodata";
							latestDstr = "no data";
						}
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
						try {
							latestStatus = genericValue.get("category").toString();
							latestDstr = genericValue.get("description").toString();
							
						} catch (Exception e) {
							latestStatus = "nodata";
							latestDstr = "no data";
						}
					}else{
						value=(long)0;
					}
					totleValue[mapsize] = ((Long) value) + totleValue[mapsize];
				}
				if (newdate.after(lastcratedtime)) {
					lastcratedtime = newdate;
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
			FastMap OutDataMap = FastMap.newInstance();
			FastMap OutDataMap2 = FastMap.newInstance();
			OutDataMap.put("min", String.valueOf(min[outdatamapsize]));
			OutDataMap.put("max", String.valueOf(max[outdatamapsize]));
			OutDataMap.put("average", String.valueOf(totleValue[outdatamapsize]/i));
			OutDataMap.put("sv_drawimage", "1");
			OutDataMap.put("sv_drawtable", "1");
			/**
			 * time max
			 */
			OutDataMap.put("when_max",maxvaluetime);
			OutDataMap.put("MonitorName", MonitorName.replaceAll("\"", ""));
			OutDataMap.put("sv_type", "numeric");
			OutDataMap.put("ReturnTitle", ReturnName[outdatamapsize].toString().replaceAll("\"", ""));
			OutDataMap.put("ReturnName", ReturnName[outdatamapsize].toString().replaceAll("\"", ""));
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
		FastMap part = FastMap.newInstance();
		String lastcratedtime1 =format.format(lastcratedtime);
		part.put("latestCreateTime", lastcratedtime1);
		part.put("MonitorName", MonitorName.replaceAll("\"", ""));
		part.put("errorPercent",String.valueOf( error/result.size()));
		part.put("warnPercent", String.valueOf( warm/result.size()));
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
	
	private static String getMonitorType(DispatchContext dctx,
			Map<String, ?> context,String mid) throws Exception{
		List<EntityCondition> entityConditionList = FastList.newInstance();
	    String monitorType ="";
		Map<String, Object> response = ServiceUtil.returnSuccess();
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
		EntityCondition condition = EntityCondition.makeCondition(entityConditionList, EntityOperator.OR);
		Delegator delegator = dctx.getDelegator();
	    EntityListIterator resultiterator= delegator.findListIteratorByCondition(dve,condition, null, fieldsToSelect, null, null);
	    List<GenericValue> result= resultiterator.getPartialList(1,100);
	    for (GenericValue genericValue : result) {
	    	if(genericValue.get("attrName").equals("class")){
	    		monitorType=genericValue.get("attrObjValue").toString();
	    	}
		}
	    //return Monitor type
		return monitorType;
	}
	
	/*
	 * many monitor
	 * 
	 */
	
	private static List<String> getMonitorTypes(DispatchContext dctx,
			Map<String, ?> context,String mid) throws Exception{
		List<EntityCondition> entityConditionList = FastList.newInstance();
	    List<String> monitorType =new ArrayList<String>();
		Map<String, Object> response = ServiceUtil.returnSuccess();
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
		EntityCondition condition = EntityCondition.makeCondition(entityConditionList, EntityOperator.OR);
		Delegator delegator = dctx.getDelegator();
	    EntityListIterator resultiterator= delegator.findListIteratorByCondition(dve,condition, null, fieldsToSelect, null, null);
	    List<GenericValue> result= resultiterator.getPartialList(1,100);
	    for (GenericValue genericValue : result) {
	    	if(genericValue.get("attrName").equals("class")){
	    		monitorType.add(genericValue.get("attrObjValue").toString());
	    	}
		}
	    //return Monitor type
		return monitorType;
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
	 * 返回阀值表达式
	 */
	public final static LinkedHashSet<Map<String, String>> get_Return_Items(List nodes) {

		Object o =null;
		LinkedHashSet<Map<String, String>> a_list = new LinkedHashSet<Map<String,String>>();
		for (int nodessize=0;nodessize<nodes.size();nodessize++){
			o=nodes.get(nodessize);
			if (o instanceof Object[]){
				Object[] temv =(Object[])o;
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
	
	
	public final static LinkedHashSet<Map<String, String>> get_Return_Items(String monitortype,String id,List nodes) {
		
		Object o =null;
		LinkedHashSet<Map<String, String>> a_list = new LinkedHashSet<Map<String,String>>();

		for (int nodessize=0;nodessize<nodes.size();nodessize++){
			o=nodes.get(nodessize);
			if (o instanceof Object[]){
				if(!monitortype.equals("browsa_cpu_utilization")){
				Object[] temv =(Object[])o;
				for (int temvsize=0;temvsize<temv.length;temvsize++){
					if (temv[9].toString().equals("true")) {
						Map<String, String> a_map = new LinkedHashMap<String, String>();
							a_map.put("sv_name", temv[1].toString());
							a_map.put("sv_label", temv[2].toString().substring(1,
									temv[2].toString().length() - 1));
						a_list.add(a_map);
					}
				}
				}else{
					Map<String, String> a_map = new LinkedHashMap<String, String>();
					a_map.put("sv_name", "countersInError");
					a_map.put("sv_label", "countersInError");
					a_list.add(a_map);
				}
			}
		}
		List<Object> list = new ArrayList<Object>();
		list.add(new OtpErlangAtom(id));
		FastMap nodees = null;
		try {
			nodees=(FastMap) OtpGateway.getOtpInterface().call("api_monitor", "info", list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FastMap defaultst = (FastMap) nodees.get("browse");
		if(defaultst==null){
			Map<String, String> map = new LinkedHashMap<String, String>();
			LinkedHashSet<Map<String, String>> a_list2 = new LinkedHashSet<Map<String,String>>();
			Iterator<Map<String, String>> iterator=a_list.iterator();
			while(iterator.hasNext()){
				Map<String, String> map2=iterator.next();
				String ReturnNameval=map2.get("sv_name");
				if(ReturnNameval.equals("status")){
					map=map2;
				}else{
					a_list2.add(map2);
				}
			}
			return a_list2;
		}else{
			List<String> browsablesid_list = new LinkedList<String>();
			// 计数器名称列表
			List<String> browsablesname_list = new LinkedList<String>();
			// 计数器显示值
			List<String> n_list = new ArrayList<String>();
			String svdefault=defaultst.toString();
			String tmp_Value = null;
			// 计数器传输值
			String c_list = "";
			tmp_Value = svdefault.substring(1, svdefault.length() - 1);
			String[] list1 = tmp_Value.split("\\},\\{");
			for (int k = 0; k < list1.length; k++) {
				String tmp_Value2 = list1[k];
				String[] list2 = tmp_Value2.split(",");
				for (int list2size = 0; list2size < list2.length; list2size++) {
					String[] str = list2[list2size].split("=");
					Map<String, String> a_map = new LinkedHashMap<String, String>();
					if(monitortype.equals("interface_monitor")){
						a_map.put("sv_name", ("\""+str[1].substring(0, str[1].length())+"\""));
						a_map.put("sv_label",("\""+str[1].substring(0, str[1].length())+"\""));
						a_list.add(a_map);
					}else if(monitortype.contains("snmp")){
						a_map.put("sv_name",("\""+str[1]).toString()+"\"");
						a_map.put("sv_label",(str[1].toString()));
						a_list.add(a_map);
					}
					else if(monitortype.equals("network_bandwidth_monitor"))
					{
						a_map.put("sv_name",(str[1].toString()));
						a_map.put("sv_label",(str[1].toString()));
						a_list.add(a_map);
					}
					else {
						a_map.put("sv_name",(str[0]).toString());
						a_map.put("sv_label",(str[1].toString()));
						a_list.add(a_map);

					}
					// }
				}
			}
	
			LinkedHashSet<Map<String, String>> a_list2 = new LinkedHashSet<Map<String,String>>();
			Map<String, String> map = new LinkedHashMap<String, String>();
			Iterator<Map<String, String>> iterator=a_list.iterator();
			while(iterator.hasNext()){
				Map<String, String> map2=iterator.next();
				String ReturnNameval=map2.get("sv_name");
				if(ReturnNameval.equals("countersInError")){
					map=map2;
				}else{
					if(monitortype.equals("interface_monitor")){
						Map<String, String> map3 = new LinkedHashMap<String, String>();
						map3.put("sv_name", ReturnNameval.substring(1, ReturnNameval.length()-1)+":inDiscards");
						map3.put("sv_label", ReturnNameval.substring(1, ReturnNameval.length()-1)+":inDiscards");
						Map<String, String> map4 = new LinkedHashMap<String, String>();
						map4.put("sv_name", ReturnNameval.substring(1, ReturnNameval.length()-1)+":outDiscards");
						map4.put("sv_label",ReturnNameval.substring(1, ReturnNameval.length()-1)+":outDiscards");
						Map<String, String> map5 = new LinkedHashMap<String, String>();
						map5.put("sv_name", ReturnNameval.substring(1, ReturnNameval.length()-1)+":operStatus");
						map5.put("sv_label",ReturnNameval.substring(1, ReturnNameval.length()-1)+":operStatus");
						Map<String, String> map6 = new LinkedHashMap<String, String>();
						map6.put("sv_name", ReturnNameval.substring(1, ReturnNameval.length()-1)+":inOctets");
						map6.put("sv_label",ReturnNameval.substring(1, ReturnNameval.length()-1)+":inOctets");
						Map<String, String> map7 = new LinkedHashMap<String, String>();
						map7.put("sv_name", ReturnNameval.substring(1, ReturnNameval.length()-1)+":outOctets");
						map7.put("sv_label",ReturnNameval.substring(1, ReturnNameval.length()-1)+":outOctets");
						a_list2.add(map6);
						a_list2.add(map7);
						a_list2.add(map5);
						a_list2.add(map3);
						a_list2.add(map4);
					}
					a_list2.add(map2);
				}
			}
			a_list2.add(map);
			return a_list2;
		}
	}
	

	
	public static Map<String, Object> LoggerServices(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		    Map<String, Object> response = ServiceUtil.returnSuccess();
		   //获取参数
	        String tablename=(String)context.get("tablename");
	        tablename="OperationAttributeLog";
	        int fromn=(Integer)context.get("from");
	        int ton=(Integer)context.get("to");
	        String mid=(String)context.get("id");
	        String orderby=(String)context.get("orderby");
	        String where=(String)context.get("where");
	        //过滤条件
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
		    EntityListIterator resultiterator= delegator.findListIteratorByCondition(dve,condition, null, fieldsToSelect, null, null);
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
		        }
		        OutDataList.add(childlist);
			}
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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String tableindex = sdf.format(new Date());
		tableindex = "_" + tableindex.replaceAll("-", "_");//获取表索引

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
	    List<GenericValue> result= delegator.findList("OperationAttributeLog"+tableindex, condition, null,UtilMisc.toList("logTime DESC"), findOptions, false);
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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String tableindex = sdf.format(new Date());
		tableindex = "_" + tableindex.replaceAll("-", "_");//获取表索引
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
		    List<GenericValue> result= delegator.findList("OperationAttributeLog"+tableindex, condition, null,UtilMisc.toList("logTime ASC"), null, false);
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