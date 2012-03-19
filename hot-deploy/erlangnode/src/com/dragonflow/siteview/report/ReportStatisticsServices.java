package com.dragonflow.siteview.report;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericDispatcher;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceAuthException;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.service.ServiceValidationException;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpGateway;

public class ReportStatisticsServices {
	public final static String module = ReportStatisticsServices.class
			.getName();
	static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * 获取两段时间内的所有日期
	 * 
	 * @param Date
	 *            dBegin
	 * @param Date
	 *            dEnd
	 * @return List lDate
	 */
	public static List<Date> findDates(Date dBegin, Date dEnd) {
		List listDate = new ArrayList();
		listDate.add(dBegin);
		Calendar calBegin = Calendar.getInstance();
		// 使用给定的 Date 设置此 Calendar 的时间
		calBegin.setTime(dBegin);
		Calendar calEnd = Calendar.getInstance();
		// 使用给定的 Date 设置此 Calendar 的时间
		calEnd.setTime(dEnd);
		// 测试此日期是否在指定日期之后
		while (dEnd.after(calBegin.getTime())) {
			// 根据日历的规则，为给定的日历字段添加或减去指定的时间量
			calBegin.add(Calendar.DAY_OF_MONTH, 1);
			listDate.add(calBegin.getTime());
		}
		return listDate;
	}

	public static Map<String, Object> queryStatisticsLog(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		// Get params
		String tablename = (String) context.get("tablename");
		int from = Integer.parseInt(context.get("from").toString());
		int to = Integer.parseInt(context.get("to").toString());
		String startTime = context.get("starttime").toString();
		String endTime = context.get("endtime").toString();
		String mid = context.get("monitorid").toString();

		List<EntityCondition> entityConditionList = FastList.newInstance();
		List<EntityCondition> entityConditionListid = FastList.newInstance();
		if (mid != null && !mid.isEmpty()) {
			String[] ids = mid.split(",");
			for (int idssize = 0; idssize < ids.length; idssize++) {
				if (!(ids[idssize].equals("")) && ids[idssize].length() > 0)
					entityConditionListid
							.add(EntityCondition.makeCondition("operationId",
									EntityOperator.EQUALS, ids[idssize]));
			}
		}

		EntityCondition conditionid = EntityCondition.makeCondition(
				entityConditionListid, EntityOperator.OR);
		entityConditionList.add(conditionid);

		//根据时间判断读取对应数据
		Date beginTime = null;
		Date enddTime = null;
		try {
			beginTime = format.parse(startTime);
			enddTime = format.parse(endTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Date> lDate = findDates(beginTime, enddTime);
		List<EntityCondition> entityConditionListDate = FastList.newInstance();
		for(Date date : lDate){
			String stime = sdf.format(date)+" 23:59:59";
			entityConditionListDate.add(EntityCondition.makeCondition("logTime",
					EntityOperator.EQUALS,Timestamp.valueOf(stime)));
		}
		System.out.println(entityConditionListDate);
		EntityCondition conditionDate = EntityCondition.makeCondition(
				entityConditionListid, EntityOperator.OR);
		entityConditionList.add(conditionDate);
		Timestamp startdate = Timestamp.valueOf(startTime);
		Timestamp enddate = Timestamp.valueOf(endTime);
//		entityConditionList.add(EntityCondition.makeCondition("logTime",
//				EntityOperator.BETWEEN, UtilMisc.toList(startdate, enddate)));

		EntityCondition condition = EntityCondition.makeCondition(
				entityConditionList, EntityOperator.AND);

		DynamicViewEntity dve = new DynamicViewEntity();
		Collection<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("operationId");
		fieldsToSelect.add("logTime");
		fieldsToSelect.add("statisticsData");
		fieldsToSelect.add("mark");
		dve.addMemberEntity("OperLog", tablename);
		dve.addAliasAll("OperLog", "");
		List<String> orderBy = UtilMisc.toList("operationId");
		Delegator delegator = dctx.getDelegator();
		EntityListIterator resultiterator = delegator
				.findListIteratorByCondition(dve, condition, null,
						fieldsToSelect, orderBy, null);
		List<GenericValue> result = resultiterator.getPartialList(from, to
				- from);
		// String operationid = "";
		// String mark = "";
		Object statisticsData = null;
		FastList datalist = FastList.newInstance();
		for (GenericValue genericValue : result) {
			FastMap partmap = FastMap.newInstance();
			// operationid = genericValue.get("operationId").toString();
			// partmap.put("operationId", operationid);
			// Date newdate = (Date) genericValue.get("logTime");
			// partmap.put("logTime", newdate);
			// mark = genericValue.get("mark").toString();
			// partmap.put("mark", mark);
			statisticsData = genericValue.get("statisticsData");
			datalist.add(statisticsData);
		}
		response.put("statisticsData", datalist);
		return response;
	}

	public static Map<String, Object> queryOperationIdList(
			DispatchContext dctx, Map<String, ?> context)
			throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date d = new Date();
		String tableindex = sdf.format(new Date(d.getTime() - 1 * 24 * 60 * 60
				* 1000));
		tableindex = "_" + tableindex.replaceAll("-", "_");// 获取表索引
		String tablename = "OperationAttributeLog" + tableindex;
		int fromn = 1;
		int ton = 999999;
		String startTime = sdf.format(new Date(d.getTime() - 1 * 24 * 60 * 60
				* 1000))
				+ " 00:00:00";
		String endTime = sdf.format(new Date(d.getTime() - 1 * 24 * 60 * 60
				* 1000))
				+ " 23:59:59";

		Timestamp startdate = Timestamp.valueOf(startTime);
		Timestamp enddate = Timestamp.valueOf(endTime);
		List<EntityCondition> entityConditionList = FastList.newInstance();
		entityConditionList.add(EntityCondition.makeCondition("logTime",
				EntityOperator.BETWEEN, UtilMisc.toList(startdate, enddate)));

		EntityCondition condition = EntityCondition.makeCondition(
				entityConditionList, EntityOperator.AND);

		DynamicViewEntity dve = new DynamicViewEntity();
		Collection<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("operationId");
		dve.addMemberEntity("OperLog", tablename);
		dve.addAliasAll("OperLog", "");
		List<String> orderBy = UtilMisc.toList("operationId");
		Delegator delegator = dctx.getDelegator();
		EntityListIterator resultiterator = delegator
				.findListIteratorByCondition(dve, condition, null,
						fieldsToSelect, orderBy, null);
		List<GenericValue> result = resultiterator.getPartialList(fromn, ton
				- fromn);
		String operationid = "";
		List operationIdList = new ArrayList();
		for (GenericValue genericValue : result) {
			operationid = genericValue.get("operationId").toString();
			if (!operationIdList.contains(operationid)) {// 去除重复operationId
				operationIdList.add(operationid);
			}
		}
		System.err.println("监测器数据统计服务开始：一共有 " + operationIdList.size()
				+ "个监测器需要统计数据！");
		LocalDispatcher rd = GenericDispatcher.getLocalDispatcher(
				delegator.getDelegatorName(), delegator);
		for (int i = 0; i < operationIdList.size(); i++) {
			Map<String, Object> contextd = FastMap.newInstance();
			contextd.put("mid", operationIdList.get(i));
			try {
				rd.runAsync("ReportStatisticsService", contextd);
			} catch (ServiceAuthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServiceValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GenericServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		response.put("OperationIdList", operationIdList);
		return response;
	}

	/**
	 * report statistics service
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */

	public static Map<String, Object> statisticReport(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		System.out.println("*********开始执行监测器ID为 "
				+ context.get("mid").toString() + " 前一天报表数据统计********");
		Map<String, Object> response = ServiceUtil.returnSuccess();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date d = new Date();
		String tableindex = sdf.format(new Date(d.getTime() - 1 * 24 * 60 * 60
				* 1000));
		tableindex = "_" + tableindex.replaceAll("-", "_");// 获取表索引
		String tablename = "OperationAttributeLog" + tableindex;
		int fromn = 1;
		int ton = 9999;
		String starttime = sdf.format(new Date(d.getTime() - 1 * 24 * 60 * 60
				* 1000))
				+ " 00:00:00";
		String endtime = sdf.format(new Date(d.getTime() - 1 * 24 * 60 * 60
				* 1000))
				+ " 23:59:59";
		// String mid = "0.2.1";
		String mid = context.get("mid").toString();
		System.out.println("统计数据从 " + starttime + " 开始到 " + endtime
				+ " 结束,统计的表是 " + tablename + " ,监测器ID是 " + mid + " ！");

		// 过滤条件
		List<EntityCondition> entityConditionList = FastList.newInstance();
		List<EntityCondition> entityConditionListid = FastList.newInstance();
		if (mid != null && !mid.isEmpty()) {
			String[] ids = mid.split(",");
			for (int idssize = 0; idssize < ids.length; idssize++) {
				if (!(ids[idssize].equals("")) && ids[idssize].length() > 0)
					entityConditionListid
							.add(EntityCondition.makeCondition("operationId",
									EntityOperator.EQUALS, ids[idssize]));
			}
		}

		EntityCondition conditionid = EntityCondition.makeCondition(
				entityConditionListid, EntityOperator.OR);
		entityConditionList.add(conditionid);
		Timestamp startTime2 = Timestamp.valueOf(starttime);
		Timestamp endTime2 = Timestamp.valueOf(endtime);
		entityConditionList.add(EntityCondition.makeCondition("logTime",
				EntityOperator.BETWEEN, UtilMisc.toList(startTime2, endTime2)));

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
		List<String> orderBy = UtilMisc.toList("-logTime");
		EntityListIterator resultiterator = delegator
				.findListIteratorByCondition(dve, condition, null,
						fieldsToSelect, orderBy, null);
		List<GenericValue> result = resultiterator.getPartialList(fromn, ton
				- fromn);
		int i = 0;
		String maxvaluetime = "";
		String MonitorName = "";
		FastMap detaildstr = new FastMap();
		// String latestCreateTime="";
		int nodata = 0;
		int good = 0;
		int warm = 0;
		String id = "";
		int error = 0;

		List<Object> list = new ArrayList<Object>();
		// query monitor type
		String monitortype = "";
		List<String> monitortypes = new ArrayList<String>();

		monitortype = getMonitorType(mid);
		List nodes = null;

		try {
			if (mid.split(",").length > 1) {
				for (int idsize = 0; idsize < monitortypes.size(); idsize++) {
					list.add(new OtpErlangAtom(monitortypes.get(idsize)));
					nodes = (List) OtpGateway.getOtpInterface().call(
							"api_monitor_template", "get_template", list);
				}
			} else {
				list.add(new OtpErlangAtom(monitortype));
				nodes = (List) OtpGateway.getOtpInterface().call(
						"api_monitor_template", "get_template", list);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LinkedHashSet<Map<String, String>> m_return_items = new LinkedHashSet<Map<String, String>>();
		m_return_items = get_Return_Items(monitortype, mid, nodes);

		String[] ReturnName = new String[m_return_items.size()];
		double latest_value = 0.0;
		String lasestcreatedtime = "";
		StringBuffer[] detail = new StringBuffer[m_return_items.size()];
		double[] max = new double[m_return_items.size()];
		double[] min = new double[m_return_items.size()];
		double[] avg = new double[m_return_items.size()];
		double[] latest = new double[m_return_items.size()];
		String[] nid = new String[m_return_items.size()];
		double[] totleValue = new double[m_return_items.size()];
		Date logtime = new Date();
		Date lastcratedtime = new Date();
		String latestStatus = "";
		String latestDstr = "";
		for (GenericValue genericValue : result) {
			StringBuffer detailds = new StringBuffer();
			if (genericValue.get("category").toString().equals("nodata")) {
				nodata++;
			} else if (genericValue.get("category").toString().equals("good")) {
				good++;
			} else if (genericValue.get("category").toString()
					.equals("warning")) {
				warm++;
			} else {
				error++;
			}
			id = genericValue.get("operationId").toString();
			Map map = (FastMap) genericValue.get("measurement");
			MonitorName = genericValue.get("name").toString();

			Date newdate = (Date) genericValue.get("logTime");
			Iterator<Map<String, String>> iterator = m_return_items.iterator();
			int mapsize = 0;
			while (iterator.hasNext()) {
				StringBuffer detail2 = new StringBuffer();
				Object value;
				String ReturnNameval = iterator.next().get("sv_name");
				value = map.get(ReturnNameval.trim());
				ReturnName[mapsize] = ReturnNameval;
				nid[mapsize] = (mapsize + ")" + genericValue.get("operationId"))
						.toString();
				if (value == null) {
					value = 0.0;
				}
				if (value instanceof Double) {
					value = (Double) value;
					totleValue[mapsize] = ((Double) value)
							+ totleValue[mapsize];
					if ((Double) value < max[mapsize]) {
						max[mapsize] = max[mapsize];
						maxvaluetime = genericValue.get("logTime").toString();
					} else {
						max[mapsize] = (Double) value;
					}
					if ((Double) value > min[mapsize] && min[mapsize] != 0) {
						min[mapsize] = min[mapsize];
					} else {
						min[mapsize] = (Double) value;
					}
					if (latest[mapsize] == 0.0) {
						latest[mapsize] = (Double) value;
						try {
							latestStatus = genericValue.get("category")
									.toString();
							latestDstr = genericValue.get("description")
									.toString();

						} catch (Exception e) {
							latestStatus = "nodata";
							latestDstr = "no data";
						}
					}
					// if (newdate.after(logtime)) {
					// logtime = newdate;
					// latest[mapsize] = (Double) value;
					// latestStatus = genericValue.get("category").toString();
					// latestDstr = genericValue.get("description").toString();
					// }
				} else if (value instanceof Long) {
					value = (Long) value;
					if ((Long) value < max[mapsize]) {
						max[mapsize] = max[mapsize];
						maxvaluetime = genericValue.get("logTime").toString();
					} else {
						max[mapsize] = (Long) value;
					}
					if ((Long) value > min[mapsize] && min[mapsize] != 0.0) {
						min[mapsize] = min[mapsize];
					} else {
						min[mapsize] = (Long) value;
					}
					if (latest[mapsize] == 0.0) {
						latest[mapsize] = (Long) value;
						try {
							latestStatus = genericValue.get("category")
									.toString();
							latestDstr = genericValue.get("description")
									.toString();

						} catch (Exception e) {
							latestStatus = "nodata";
							latestDstr = "no data";
						}
					}
					if (newdate.after(logtime)) {
						// logtime = newdate;
						// latest[mapsize] = (Long) value;
						// latestStatus =
						// genericValue.get("category").toString();
						// latestDstr =
						// genericValue.get("description").toString();
					} else {
						value = (Long) value;
					}
					totleValue[mapsize] = ((Long) value) + totleValue[mapsize];
				}
				if (newdate.after(lastcratedtime)) {
					lastcratedtime = newdate;
				}
				if (detail[mapsize] != null) {
					detail[mapsize] = detail[mapsize]
							.append(genericValue.get("logTime")).append("=")
							.append(value).append(",");
				} else {
					detail[mapsize] = detail2
							.append(genericValue.get("logTime")).append("=")
							.append(value).append(",");
				}
				mapsize++;
			}
			String statemessage = genericValue.get("category").toString();
			if (statemessage.contains("good")) {
				statemessage = "ok";
			} else if (statemessage.contains("error")) {
				statemessage = "error";
			} else if (statemessage.contains("disable")) {
				statemessage = "disable";
			} else if (statemessage.contains("warning")) {
				statemessage = "warning";
			} else {
				statemessage = "nodata";
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
									genericValue.get("measurement").toString()
											.length() - 1));
			String timekey = format.format(genericValue.get("logTime"));
			detaildstr.put(timekey, detailds.toString().replaceAll("\"", ""));
			i++;
		}
		// -------part 1--------
		resultiterator.close();
		NumberFormat number = NumberFormat.getNumberInstance();
		number.setMaximumFractionDigits(2);
		for (int outdatamapsize = 0; outdatamapsize < min.length; outdatamapsize++) {
			FastMap OutDataMap = FastMap.newInstance();
			FastMap OutDataMap2 = FastMap.newInstance();
			OutDataMap.put("min",
					String.valueOf(number.format(min[outdatamapsize]))
							.replaceAll(",", ""));
			OutDataMap.put("max",
					String.valueOf(number.format(max[outdatamapsize]))
							.replaceAll(",", ""));
			OutDataMap.put(
					"average",
					String.valueOf(
							number.format(totleValue[outdatamapsize] / i))
							.replaceAll(",", ""));
			OutDataMap.put("sv_drawimage", "1");
			OutDataMap.put("sv_drawtable", "1");
			/**
			 * time max
			 */
			OutDataMap.put("when_max", maxvaluetime);
			OutDataMap.put("MonitorName", MonitorName.replaceAll("\"", ""));
			OutDataMap.put("sv_type", "numeric");
			OutDataMap.put("ReturnTitle", ReturnName[outdatamapsize].toString()
					.replaceAll("\"", ""));
			OutDataMap.put("ReturnName", ReturnName[outdatamapsize].toString()
					.replaceAll("\"", ""));
			OutDataMap.put("sv_drawmeasure", "1");
			OutDataMap.put("sv_primary", "1");
			OutDataMap.put("sv_baseline", "1");
			// OutDataMap.put("latest",String.valueOf(number.format(latest[outdatamapsize])).replaceAll(",",
			// ""));
			String numstring = detail[outdatamapsize].substring(
					detail[outdatamapsize].indexOf("=") + 1,
					detail[outdatamapsize].indexOf(",")).replaceAll(",", "");
			Double newnumber = 0.0;
			if (numstring.equals("not_found")) {
				newnumber = 0.0;
			} else {
				newnumber = Double.valueOf(numstring.equals("n/a") ? "0"
						: numstring);
			}
			OutDataMap.put("latest", String.valueOf(number.format(newnumber)));
			OutDataMap.put("detail", detail[outdatamapsize].toString());
			OutDataList.put("(Return_" + nid[outdatamapsize], OutDataMap);
			// OutDataList.put("",OutDataMap2);
		}
		// -------part 2--------
		OutDataList.put("(dstr)" + id, detaildstr);
		// -------part 3--------
		FastMap part = FastMap.newInstance();
		String lastcratedtime1 = format.format(lastcratedtime);
		part.put("latestCreateTime", lastcratedtime1);
		part.put("MonitorName", MonitorName.replaceAll("\"", ""));
		part.put("errorPercent", String.valueOf(error / result.size())
				.replaceAll(",", ""));
		part.put("warnPercent", String.valueOf(warm / result.size())
				.replaceAll(",", ""));
		part.put("okPercent",
				String.valueOf(good / result.size()).replaceAll(",", ""));
		part.put("latestStatus", latestStatus);
		part.put("latestDstr", latestDstr);
		for (int ReturnNamesize = 0; ReturnNamesize < ReturnName.length; ReturnNamesize++) {
			part.put("(Return_" + ReturnNamesize + ")" + id, "ReturnValue");
		}
		OutDataList.put(id, part);
		String logId = delegator.getNextSeqId("OperationStatisticsLog");
		String statisticsendtime = sdf.format(new Date(d.getTime() - 1 * 24
				* 60 * 60 * 1000))
				+ " 23:59:59";
		Map<String, Object> fields = UtilMisc.toMap("logId", logId,
				"operationId", id, "logTime",
				Timestamp.valueOf(statisticsendtime), "statisticsData",
				OutDataList, "mark", "1");
		GenericValue data = delegator.makeValue("OperationStatisticsLog",
				fields);
		delegator.create(data);
		System.out.println("*********执行监测器ID为 " + context.get("mid").toString()
				+ " 前一天报表数据统计完毕********");
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

	public final static LinkedHashSet<Map<String, String>> get_Return_Items(
			String monitortype, String id, List nodes) {

		Object o = null;
		LinkedHashSet<Map<String, String>> a_list = new LinkedHashSet<Map<String, String>>();

		for (int nodessize = 0; nodessize < nodes.size(); nodessize++) {
			o = nodes.get(nodessize);
			if (o instanceof Object[]) {
				if (!monitortype.equals("browsa_cpu_utilization")) {
					Object[] temv = (Object[]) o;
					for (int temvsize = 0; temvsize < temv.length; temvsize++) {
						if (temv[9].toString().equals("true")) {
							Map<String, String> a_map = new LinkedHashMap<String, String>();
							a_map.put("sv_name", temv[1].toString());
							a_map.put(
									"sv_label",
									temv[2].toString().substring(1,
											temv[2].toString().length() - 1));
							a_list.add(a_map);
						}
					}
				} else {
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
			nodees = (FastMap) OtpGateway.getOtpInterface().call("api_monitor",
					"info", list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FastMap defaultst = (FastMap) nodees.get("browse");
		if (defaultst == null) {
			Map<String, String> map = new LinkedHashMap<String, String>();
			LinkedHashSet<Map<String, String>> a_list2 = new LinkedHashSet<Map<String, String>>();
			Iterator<Map<String, String>> iterator = a_list.iterator();
			while (iterator.hasNext()) {
				Map<String, String> map2 = iterator.next();
				String ReturnNameval = map2.get("sv_name");
				if (ReturnNameval.equals("status")) {
					map = map2;
				} else {
					a_list2.add(map2);
				}
			}
			return a_list2;
		} else {
			List<String> browsablesid_list = new LinkedList<String>();
			// 计数器名称列表
			List<String> browsablesname_list = new LinkedList<String>();
			// 计数器显示值
			List<String> n_list = new ArrayList<String>();
			String svdefault = defaultst.toString();
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
					if (monitortype.equals("interface_monitor")) {
						a_map.put(
								"sv_name",
								("\"" + str[1].substring(0, str[1].length()) + "\""));
						a_map.put(
								"sv_label",
								("\"" + str[1].substring(0, str[1].length()) + "\""));
						a_list.add(a_map);
					} else if (monitortype.contains("snmp")) {
						a_map.put("sv_name", ("\"" + str[1]).toString() + "\"");
						a_map.put("sv_label", (str[1].toString()));
						a_list.add(a_map);
					} else {
						a_map.put("sv_name", (str[0]).toString());
						a_map.put("sv_label", (str[1].toString()));
						a_list.add(a_map);

					}
					// }
				}
			}

			LinkedHashSet<Map<String, String>> a_list2 = new LinkedHashSet<Map<String, String>>();
			Map<String, String> map = new LinkedHashMap<String, String>();
			Iterator<Map<String, String>> iterator = a_list.iterator();
			while (iterator.hasNext()) {
				Map<String, String> map2 = iterator.next();
				String ReturnNameval = map2.get("sv_name");
				if (ReturnNameval.equals("countersInError")) {
					map = map2;
				} else {
					if (monitortype.equals("interface_monitor")) {
						Map<String, String> map3 = new LinkedHashMap<String, String>();
						map3.put(
								"sv_name",
								ReturnNameval.substring(1,
										ReturnNameval.length() - 1)
										+ ":inDiscards");
						map3.put(
								"sv_label",
								ReturnNameval.substring(1,
										ReturnNameval.length() - 1)
										+ ":inDiscards");
						Map<String, String> map4 = new LinkedHashMap<String, String>();
						map4.put(
								"sv_name",
								ReturnNameval.substring(1,
										ReturnNameval.length() - 1)
										+ ":outDiscards");
						map4.put(
								"sv_label",
								ReturnNameval.substring(1,
										ReturnNameval.length() - 1)
										+ ":outDiscards");
						Map<String, String> map5 = new LinkedHashMap<String, String>();
						map5.put(
								"sv_name",
								ReturnNameval.substring(1,
										ReturnNameval.length() - 1)
										+ ":operStatus");
						map5.put(
								"sv_label",
								ReturnNameval.substring(1,
										ReturnNameval.length() - 1)
										+ ":operStatus");
						Map<String, String> map6 = new LinkedHashMap<String, String>();
						map6.put(
								"sv_name",
								ReturnNameval.substring(1,
										ReturnNameval.length() - 1)
										+ ":inOctets");
						map6.put(
								"sv_label",
								ReturnNameval.substring(1,
										ReturnNameval.length() - 1)
										+ ":inOctets");
						Map<String, String> map7 = new LinkedHashMap<String, String>();
						map7.put(
								"sv_name",
								ReturnNameval.substring(1,
										ReturnNameval.length() - 1)
										+ ":outOctets");
						map7.put(
								"sv_label",
								ReturnNameval.substring(1,
										ReturnNameval.length() - 1)
										+ ":outOctets");
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
}
