package com.dragonflow.erlangecc.ofbiz;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.ofbiz.service.ServiceUtil;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpGateway;

/**
 * Monitor Browse Services
 */
public class MonitorBrowseServices {
	public final static String module = MonitorBrowseServices.class.getName();
	static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * query the most error monitors
	 * 
	 * @author zhongping.wang
	 * @param dctx
	 * @param context
	 * @return response
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> queryMostError(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		// Get params
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String tableindex = sdf.format(new Date());
		tableindex = "_" + tableindex.replaceAll("-", "_");//获取表索引
		String tablename = (String) context.get("tablename")+tableindex;
		int from = (Integer) context.get("from");
		int to = (Integer) context.get("to");
		Date start = null;
		Date end = null;
		String startTime = (String) context.get("starttime");
		String endTime = (String) context.get("endtime");
		Timestamp startdate = Timestamp.valueOf(startTime);
		Timestamp enddate = Timestamp.valueOf(endTime);
		String orderby = (String) context.get("orderby");
		String category = (String) context.get("category");
		List<EntityCondition> entityConditionList = FastList.newInstance();
		entityConditionList.add(EntityCondition.makeCondition("category",
				EntityOperator.EQUALS, category));
		entityConditionList.add(EntityCondition.makeCondition("logTime",
				EntityOperator.BETWEEN, UtilMisc.toList(startdate, enddate)));

		EntityCondition condition = EntityCondition.makeCondition(
				entityConditionList, EntityOperator.AND);

		DynamicViewEntity dve = new DynamicViewEntity();
		Collection<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("operationId");
		fieldsToSelect.add("name");
		fieldsToSelect.add("logTime");
		fieldsToSelect.add("category");
		fieldsToSelect.add("description");
		dve.addMemberEntity("OperLog", tablename);
		dve.addAliasAll("OperLog", "");
		List<String> orderBy = UtilMisc.toList("operationId");
		Delegator delegator = dctx.getDelegator();
		EntityListIterator resultiterator = delegator
				.findListIteratorByCondition(dve, condition, null,
						fieldsToSelect, orderBy, null);
		List<GenericValue> result = resultiterator.getPartialList(from, to
				- from);
		String operationid = "";
		String monitorname = "";
		String latestStatus = "";
		Object description = null;

		FastList datalist = FastList.newInstance();
		String groupname = "";
		String lastid = "";
		FastList<String> operationidlist = FastList.newInstance();
		for (GenericValue genericValue : result) {
			FastMap partmap = FastMap.newInstance();
			if (!lastid.equals(operationid) || operationidlist.size() > 2)
				operationidlist.clear();
			lastid = operationid;
			if (!lastid.equals(""))
				operationidlist.add(lastid);
			operationid = genericValue.get("operationId").toString();
			if (operationidlist.contains(operationid)
					&& operationidlist.size() > 2) {
				partmap.put("id", operationid);
				groupname = getMonitorGroupName(operationid);
				if (groupname.equals("Monitor_not_found")) {
					continue;
				}
				try {
					groupname = new String(groupname.getBytes("iso8859-1"),
							"UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				partmap.put("groupname", groupname);
				monitorname = getMonitorName(operationid);
				partmap.put("name", monitorname);
				partmap.put("latestStatus", latestStatus);
				description = genericValue.get("description");
				String desc = description.toString();
				desc = desc.replace("decription=", "");
				partmap.put("description", desc);
				Date newdate = (Date) genericValue.get("logTime");
				latestStatus = genericValue.get("category").toString();
				partmap.put("logTime", newdate);
				datalist.add(partmap);

			}

		}
		response.put("most_error_data", datalist);
		return response;
	}

	/**
	 * query the most browse monitors
	 * 
	 * @author zhongping.wang
	 * @param dctx
	 * @param context
	 * @return response
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> queryMostBrowse(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		// Get params
		String tablename = (String) context.get("tablename");
		int from = (Integer) context.get("from");
		int to = (Integer) context.get("to");
		Date start = null;
		Date end = null;
		String startTime = (String) context.get("starttime");
		String endTime = (String) context.get("endtime");
		Timestamp startdate = Timestamp.valueOf(startTime);
		Timestamp enddate = Timestamp.valueOf(endTime);
		String orderby = (String) context.get("orderby");
		List<EntityCondition> entityConditionList = FastList.newInstance();
		entityConditionList.add(EntityCondition.makeCondition("logTime",
				EntityOperator.BETWEEN, UtilMisc.toList(startdate, enddate)));

		EntityCondition condition = EntityCondition.makeCondition(
				entityConditionList, EntityOperator.AND);

		DynamicViewEntity dve = new DynamicViewEntity();
		Collection<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("operationId");
		fieldsToSelect.add("name");
		fieldsToSelect.add("logTime");
		fieldsToSelect.add("category");
		fieldsToSelect.add("description");
		dve.addMemberEntity("OperLog", tablename);
		dve.addAliasAll("OperLog", "");
		List<String> orderBy = UtilMisc.toList("operationId");
		Delegator delegator = dctx.getDelegator();
		EntityListIterator resultiterator = delegator
				.findListIteratorByCondition(dve, condition, null,
						fieldsToSelect, orderBy, null);
		List<GenericValue> result = resultiterator.getPartialList(from, to
				- from);
		String operationid = "";
		String monitorname = "";
		String latestStatus = "";
		Object description = null;

		FastList datalist = FastList.newInstance();
		String groupname = "";
		String lastid = "";
		FastList<String> operationidlist = FastList.newInstance();
		for (GenericValue genericValue : result) {
			FastMap partmap = FastMap.newInstance();
			if (!lastid.equals(operationid) || operationidlist.size() > 2)
				operationidlist.clear();
			lastid = operationid;
			if (!lastid.equals(""))
				operationidlist.add(lastid);
			operationid = genericValue.get("operationId").toString();
			if (operationidlist.contains(operationid)
					&& operationidlist.size() > 2) {
				partmap.put("id", operationid);
				groupname = getMonitorGroupName(operationid);
				if (groupname.equals("Monitor_not_found")) {
					continue;
				}
				try {
					groupname = new String(groupname.getBytes("iso8859-1"),
							"UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				partmap.put("groupname", groupname);
				monitorname = getMonitorName(operationid);
				partmap.put("name", monitorname);
				partmap.put("latestStatus", latestStatus);
				description = genericValue.get("description");
				String desc = description.toString();
				desc = desc.replace("decription=", "");
				partmap.put("description", desc);
				Date newdate = (Date) genericValue.get("logTime");
				latestStatus = genericValue.get("category").toString();
				partmap.put("logTime", newdate);
				datalist.add(partmap);
			}
		}
		response.put("most_browse_data", datalist);
		return response;
	}

	/**
	 * query data from table monitorfilter by mfid
	 * 
	 * @author zhongping.wang
	 * @param dctx
	 * @param context
	 * @return response
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> queryMonitorFilter(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		
		// Get params
		String tablename = (String) context.get("tablename");
		String mfid = (String) context.get("mfid");
		int from = (Integer) context.get("from");
		int to = (Integer) context.get("to");
		List<EntityCondition> entityConditionList = FastList.newInstance();
		entityConditionList.add(EntityCondition.makeCondition("mfid",
				EntityOperator.EQUALS, mfid));

		EntityCondition condition = EntityCondition.makeCondition(
				entityConditionList, EntityOperator.AND);

		DynamicViewEntity dve = new DynamicViewEntity();
		Collection<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("mfid");
		fieldsToSelect.add("entityname");
		fieldsToSelect.add("groupname");
		fieldsToSelect.add("monitordescripe");
		fieldsToSelect.add("monitorname");
		fieldsToSelect.add("monitorstate");
		fieldsToSelect.add("monitortype");
		fieldsToSelect.add("monitortypename");
		fieldsToSelect.add("nodeid");
		fieldsToSelect.add("refreshfre");
		fieldsToSelect.add("showhidename");
		fieldsToSelect.add("sort");
		fieldsToSelect.add("sortname");
		fieldsToSelect.add("title");
		fieldsToSelect.add("tagid");
		fieldsToSelect.add("tagname");
		fieldsToSelect.add("starttime");
		fieldsToSelect.add("endtime");
		dve.addMemberEntity("MonitorFilterData", tablename);
		dve.addAliasAll("MonitorFilterData", "");
		Delegator delegator = dctx.getDelegator();
		EntityListIterator resultiterator = delegator
				.findListIteratorByCondition(dve, condition, null,
						fieldsToSelect, null, null);
		List<GenericValue> result = resultiterator.getPartialList(from, to
				- from);
		String mid = "";
		String entityname = "";
		String groupname = "";
		String monitordescripe = "";
		String monitorname = "";
		String monitorstate = "";
		String monitortype = "";
		String monitortypename = "";
		String nodeid = "";
		String refreshfre = "";
		String showhidename = "";
		String sort = "";
		String sortname = "";
		String title = "";
		String tagid = "";
		String tagname = "";

		FastList data = FastList.newInstance();
		for (GenericValue genericValue : result) {
			FastMap partmap = FastMap.newInstance();
			mid = genericValue.get("mfid").toString();
			partmap.put("mid", mid);
			entityname = genericValue.get("entityname").toString();
			partmap.put("entityname", entityname);
			groupname = genericValue.get("groupname").toString();
			partmap.put("groupname", groupname);
			monitordescripe = genericValue.get("monitordescripe").toString();
			partmap.put("monitordescripe", monitordescripe);
			monitorname = genericValue.get("monitorname").toString();
			partmap.put("monitorname", monitorname);
			monitorstate = genericValue.get("monitorstate").toString();
			partmap.put("monitorstate", monitorstate);
			monitortype = genericValue.get("monitortype").toString();
			partmap.put("monitortype", monitortype);
			monitortypename = genericValue.get("monitortypename").toString();
			partmap.put("monitortypename", monitortypename);
			nodeid = genericValue.get("nodeid").toString();
			partmap.put("nodeid", nodeid);
			refreshfre = genericValue.get("refreshfre").toString();
			partmap.put("refreshfre", refreshfre);
			showhidename = genericValue.get("showhidename").toString();
			partmap.put("showhidename", showhidename);
			sort = genericValue.get("sort").toString();
			partmap.put("sort", sort);
			sortname = genericValue.get("sortname").toString();
			partmap.put("sortname", sortname);
			title = genericValue.get("title").toString();
			partmap.put("title", title);
			tagid = genericValue.get("tagid").toString();
			partmap.put("tagid", tagid);
			tagname = genericValue.get("tagname").toString();
			partmap.put("tagname", tagname);
			Date startdate = (Date) genericValue.get("starttime");
			partmap.put("startdate", startdate);
			Date enddate = (Date) genericValue.get("endtime");
			partmap.put("enddate", enddate);
			data.add(partmap);
		}
		response.put("monitor_filter_data", data);
		return response;
	}

	/**
	 * query the new error monitors
	 * 
	 * @author zhongping.wang
	 * @param dctx
	 * @param context
	 * @return response
	 * @throws GenericEntityException
	 *             SQL Example : SELECT
	 *             LOG_ID,OPERATION_ID,NAME,LOG_TIME,CATEGORY
	 *             ,DESCRIPTION,MEASUREMENT FROM operation_attribute_log WHERE
	 *             LOG_TIME BETWEEN '?' AND '?' AND CATEGORY = 'error' ORDER BY
	 *             LOG_TIME DESC
	 */
	public static Map<String, Object> queryNewestError(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		// queryOperationIds(dctx, context);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String tableindex = sdf.format(new Date());
		tableindex = "_" + tableindex.replaceAll("-", "_");//获取表索引
		// Get params
		String tablename = (String) context.get("tablename")+tableindex;
		int from = (Integer) context.get("from");
		int to = (Integer) context.get("to");
		Date start = null;
		Date end = null;
		String startTime = (String) context.get("starttime");
		String endTime = (String) context.get("endtime");
		// String orderby = (String) context.get("orderby");
		String category = (String) context.get("category");
		List<EntityCondition> entityConditionList = FastList.newInstance();

		entityConditionList.add(EntityCondition.makeCondition("category",
				EntityOperator.EQUALS, category));
		entityConditionList.add(EntityCondition.makeCondition("logTime",
				EntityOperator.BETWEEN, UtilMisc.toList(Timestamp
						.valueOf(startTime), Timestamp.valueOf(endTime))));

		EntityCondition condition = EntityCondition.makeCondition(
				entityConditionList, EntityOperator.AND);

		DynamicViewEntity dve = new DynamicViewEntity();
		Collection<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("operationId");
		fieldsToSelect.add("name");
		fieldsToSelect.add("logTime");
		fieldsToSelect.add("category");
		fieldsToSelect.add("description");
		dve.addMemberEntity("OperLog", tablename);
		dve.addAliasAll("OperLog", "");
		List<String> orderBy = UtilMisc.toList("logTime DESC");
		Delegator delegator = dctx.getDelegator();
		EntityListIterator resultiterator = delegator
				.findListIteratorByCondition(dve, condition, null,
						fieldsToSelect, orderBy, null);
		List<GenericValue> result = resultiterator.getPartialList(from, to
				- from);
		String operationid = "";
		String monitorname = "";
		String latestStatus = "";
		String groupname = "";
		Object description = null;
		FastList datalist = FastList.newInstance();
		List<String> newlist = FastList.newInstance();
		String lastid = "";
		for (GenericValue genericValue : result) {
			FastMap partmap = FastMap.newInstance();
			lastid = operationid;
			operationid = genericValue.get("operationId").toString();
			if (!lastid.equals("") || newlist.contains(lastid))
				newlist.add(lastid);
			if (!newlist.contains(operationid) || lastid.equals("")) {
				partmap.put("id", operationid);
				groupname = getMonitorGroupName(operationid);
				if (groupname.equals("Monitor_not_found")) {
					continue;
				}
				try {
					groupname = new String(groupname.getBytes("iso8859-1"),
							"UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				partmap.put("groupname", groupname);
				monitorname = getMonitorName(operationid);
				partmap.put("name", monitorname);
				latestStatus = genericValue.get("category").toString();
				partmap.put("latestStatus", latestStatus);
				description = genericValue.get("description");
				String desc = description.toString();
				desc = desc.replace("decription=", "");
				partmap.put("description", desc);
				Date newdate = (Date) genericValue.get("logTime");
				partmap.put("logTime", newdate);
				datalist.add(partmap);
			}
		}
		response.put("new_error_data", datalist);
		newlist.clear();
		return response;
	}

	/**
	 * query monitor Logs
	 * 
	 * @author zhongping.wang
	 * @param dctx
	 * @param context
	 * @return response
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> queryMonitorLog(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String tableindex = sdf.format(new Date());
		tableindex = "_" + tableindex.replaceAll("-", "_");//获取表索引
		// Get params
		String tablename = (String) context.get("tablename")+tableindex;
		int from = (Integer) context.get("from");
		int to = (Integer) context.get("to");
		String regroupname = (String) context.get("groupname");
		String remonitorname = (String) context.get("monitorname");
		String remonitorstate = (String) context.get("monitorstate");
		String remonitortype = (String) context.get("monitortype");
		String restarttime = (String) context.get("startdate");
		String reendtime = (String) context.get("enddate");
		List<EntityCondition> entityConditionList = FastList.newInstance();
		List<EntityCondition> entityConditionListcategory = FastList
				.newInstance();
		List<EntityCondition> monitornameList = FastList.newInstance();
		if (!remonitorname.equals("all_type") && !remonitorname.equals("")
				&& remonitorname != null) {
			String[] monitorname = remonitorname.split(",");
			for (Object indexstr : monitorname) {
				monitornameList.add(EntityCondition.makeCondition("name",
						EntityOperator.EQUALS, indexstr));
			}
			EntityCondition monitornamecondition = EntityCondition
					.makeCondition(monitornameList, EntityOperator.OR);
			entityConditionList.add(monitornamecondition);
		}
		if (remonitorstate.equals("show_error")) {
			entityConditionList.add(EntityCondition.makeCondition("category",
					EntityOperator.EQUALS, "error"));
		}
		if (remonitorstate.equals("show_ok")) {
			entityConditionList.add(EntityCondition.makeCondition("category",
					EntityOperator.EQUALS, "good"));
		}
		if (remonitorstate.equals("hide_error_or_warning")) {
			entityConditionList.add(EntityCondition.makeCondition("category",
					EntityOperator.EQUALS, "good"));
		}
		if (remonitorstate.equals("show_warning")) {
			entityConditionList.add(EntityCondition.makeCondition("category",
					EntityOperator.EQUALS, "warning"));
		}
		if (remonitorstate.equals("show_nodata")) {
			entityConditionList.add(EntityCondition.makeCondition("category",
					EntityOperator.EQUALS, "nodata"));
		}
		if (remonitorstate.equals("hide_error")) {
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "good"));
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "warning"));
		}
		if (remonitorstate.equals("hide_warning")) {
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "good"));
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "error"));
		}
		if (remonitorstate.equals("hide_nodata")) {
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "good"));
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "error"));
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "warning"));
		}
		if (remonitorstate.equals("show_error_or_warning")
				|| remonitorstate.equals("hide_ok")) {
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "error"));
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "warning"));
		}
		EntityCondition conditioncategory = EntityCondition.makeCondition(
				entityConditionListcategory, EntityOperator.OR);
		if (!conditioncategory.isEmpty()) {
			entityConditionList.add(conditioncategory);
		}
		if (restarttime != null && !restarttime.equals("") && reendtime != null
				&& !reendtime.equals("")) {
			entityConditionList.add(EntityCondition.makeCondition("logTime",
					EntityOperator.BETWEEN, UtilMisc
							.toList(Timestamp.valueOf(restarttime), Timestamp
									.valueOf(reendtime))));
		}

		EntityCondition condition = EntityCondition.makeCondition(
				entityConditionList, EntityOperator.AND);


		DynamicViewEntity dve = new DynamicViewEntity();
		Collection<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("operationId");
		fieldsToSelect.add("name");
		fieldsToSelect.add("logTime");
		fieldsToSelect.add("category");
		fieldsToSelect.add("description");
		dve.addMemberEntity("OperLog", tablename);
		dve.addAliasAll("OperLog", "");
		List<String> orderBy = UtilMisc.toList("operationId DESC");
		Delegator delegator = dctx.getDelegator();
		EntityListIterator resultiterator = delegator
				.findListIteratorByCondition(dve, condition, null,
						fieldsToSelect, orderBy, null);
		List<GenericValue> result = resultiterator.getPartialList(from, to
				- from);
		String operationid = "";
		String monitorname = "";
		String latestStatus = "";
		String groupname = "";
		Object description = null;

		FastList data = FastList.newInstance();
		for (GenericValue genericValue : result) {
			FastMap partmap = FastMap.newInstance();
			operationid = genericValue.get("operationId").toString();

			// Show Data by monitor groupname
			if (remonitorstate.equals("show_disabled")) {
				boolean flag = getMonitorDisabled(operationid);
				if (flag) {
					FastMap runinfo = getMonitorRuninfo(operationid);
					partmap.put("id", operationid);
					groupname = getMonitorGroupName(operationid);
					if (groupname.equals("Monitor_not_found")) {
						continue;
					}
					try {
						groupname = new String(groupname.getBytes("iso8859-1"),
								"UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					partmap.put("groupname", groupname);
					partmap.put("name", runinfo.get("name"));
					partmap.put("latestStatus", "disable");
					partmap.put("description", "nodata");
					partmap.put("logTime", "nodata");
					data.add(partmap);
				}
			} else if (!regroupname.equals("") && regroupname != null) {
				String group = getMonitorGroupName(operationid);
				if (group.equals("Monitor_not_found")) {
					continue;
				}
				try {
					group = new String(group.getBytes("iso8859-1"), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				List regroupnamestr = Arrays.asList(regroupname.split(","));
				if (regroupnamestr.contains(group)) {
					partmap.put("id", operationid);
					partmap.put("groupname", group);
					monitorname = getMonitorName(operationid);
					partmap.put("name", monitorname);
					latestStatus = genericValue.get("category").toString();
					partmap.put("latestStatus", latestStatus);
					description = genericValue.get("description");
					String desc = description.toString();
					desc = desc.replace("decription=", "");
					partmap.put("description", desc);
					Date newdate = (Date) genericValue.get("logTime");
					partmap.put("logTime", newdate);
					data.add(partmap);
				} else {
					continue;
				}
				// Show Data by monitortype
			} else if (!remonitortype.equals("all_type")
					&& !remonitortype.equals("")) {
				String monitortype = getMonitorType(operationid);
				if (monitortype.equals("MonitorType_not_found")) {
					continue;
				}
				if (monitortype.equals(remonitortype)) {
					partmap.put("id", operationid);
					groupname = getMonitorGroupName(operationid);
					if (groupname.equals("Monitor_not_found")) {
						continue;
					}
					try {
						groupname = new String(groupname.getBytes("iso8859-1"),
								"UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					partmap.put("groupname", groupname);
					monitorname = getMonitorName(operationid);
					partmap.put("name", monitorname);
					latestStatus = genericValue.get("category").toString();
					partmap.put("latestStatus", latestStatus);
					description = genericValue.get("description");
					String desc = description.toString();
					desc = desc.replace("decription=", "");
					partmap.put("description", desc);
					Date newdate = (Date) genericValue.get("logTime");
					partmap.put("logTime", newdate);
					data.add(partmap);
				}
			} else {
				// Show All
				partmap.put("id", operationid);
				groupname = getMonitorGroupName(operationid);
				if (groupname.equals("Monitor_not_found")) {
					continue;
				}
				try {
					groupname = new String(groupname.getBytes("iso8859-1"),
							"UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				partmap.put("groupname", groupname);
				monitorname = getMonitorName(operationid);
				partmap.put("name", monitorname);
				latestStatus = genericValue.get("category").toString();
				partmap.put("latestStatus", latestStatus);
				description = genericValue.get("description");
				String desc = description.toString();
				desc = desc.replace("decription=", "");
				partmap.put("description", desc);
				Date newdate = (Date) genericValue.get("logTime");
				partmap.put("logTime", newdate);
				data.add(partmap);
			}
		}
		response.put("monitor_log_data", data);
		return response;
	}

	/**
	 * query data from table operation_attribute_log by monitorfilter
	 * 
	 * @author zhongping.wang
	 * @param dctx
	 * @param context
	 * @return response
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> queryCustomBroswe(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String tableindex = sdf.format(new Date());
		tableindex = "_" + tableindex.replaceAll("-", "_");//获取表索引
		// Get params
		String tablename = (String) context.get("tablename")+tableindex;
		int from = (Integer) context.get("from");
		int to = (Integer) context.get("to");
		String regroupname = (String) context.get("groupname");
		String remonitorname = (String) context.get("monitorname");
		String remonitorstate = (String) context.get("monitorstate");
		String remonitortype = (String) context.get("monitortype");
		String restarttime = (String) context.get("startdate");
		String reendtime = (String) context.get("enddate");
		List<EntityCondition> entityConditionList = FastList.newInstance();
		List<EntityCondition> entityConditionListcategory = FastList
				.newInstance();
		List<EntityCondition> monitornameList = FastList.newInstance();
		if (!remonitorname.equals("all_type") && !remonitorname.equals("")
				&& remonitorname != null) {
			String[] monitorname = remonitorname.split(",");
			for (Object indexstr : monitorname) {
				monitornameList.add(EntityCondition.makeCondition("name",
						EntityOperator.EQUALS, indexstr));
			}
			EntityCondition monitornamecondition = EntityCondition
					.makeCondition(monitornameList, EntityOperator.OR);
			entityConditionList.add(monitornamecondition);
		}
		if (remonitorstate.equals("show_error")) {
			entityConditionList.add(EntityCondition.makeCondition("category",
					EntityOperator.EQUALS, "error"));
		}
		if (remonitorstate.equals("show_ok")) {
			entityConditionList.add(EntityCondition.makeCondition("category",
					EntityOperator.EQUALS, "good"));
		}
		if (remonitorstate.equals("hide_error_or_warning")) {
			entityConditionList.add(EntityCondition.makeCondition("category",
					EntityOperator.EQUALS, "good"));
		}
		if (remonitorstate.equals("show_warning")) {
			entityConditionList.add(EntityCondition.makeCondition("category",
					EntityOperator.EQUALS, "warning"));
		}
		if (remonitorstate.equals("show_nodata")) {
			entityConditionList.add(EntityCondition.makeCondition("category",
					EntityOperator.EQUALS, "nodata"));
		}
		if (remonitorstate.equals("hide_error")) {
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "good"));
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "warning"));
		}
		if (remonitorstate.equals("hide_warning")) {
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "good"));
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "error"));
		}
		if (remonitorstate.equals("hide_nodata")) {
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "good"));
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "error"));
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "warning"));
		}
		if (remonitorstate.equals("show_error_or_warning")
				|| remonitorstate.equals("hide_ok")) {
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "error"));
			entityConditionListcategory.add(EntityCondition.makeCondition(
					"category", EntityOperator.EQUALS, "warning"));
		}
		EntityCondition conditioncategory = EntityCondition.makeCondition(
				entityConditionListcategory, EntityOperator.OR);
		if (!conditioncategory.isEmpty()) {
			entityConditionList.add(conditioncategory);
		}
		if (restarttime != null && !restarttime.equals("") && reendtime != null
				&& !reendtime.equals("")) {
			entityConditionList.add(EntityCondition.makeCondition("logTime",
					EntityOperator.BETWEEN, UtilMisc
							.toList(Timestamp.valueOf(restarttime), Timestamp
									.valueOf(reendtime))));
		}

		EntityCondition condition = EntityCondition.makeCondition(
				entityConditionList, EntityOperator.AND);


		DynamicViewEntity dve = new DynamicViewEntity();
		Collection<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("operationId");
		fieldsToSelect.add("name");
		fieldsToSelect.add("logTime");
		fieldsToSelect.add("category");
		fieldsToSelect.add("description");
		dve.addMemberEntity("OperLog", tablename);
		dve.addAliasAll("OperLog", "");
		List<String> orderBy = UtilMisc.toList("logTime DESC");
		Delegator delegator = dctx.getDelegator();
		EntityListIterator resultiterator = delegator
				.findListIteratorByCondition(dve, condition, null,
						fieldsToSelect, orderBy, null);
		List<GenericValue> result = resultiterator.getPartialList(from, to
				- from);
		String operationid = "";
		String monitorname = "";
		String latestStatus = "";
		String groupname = "";
		Object description = null;

		FastList data = FastList.newInstance();
		List<String> newlist = FastList.newInstance();
		String lastid = "";
		for (GenericValue genericValue : result) {
			FastMap partmap = FastMap.newInstance();
			lastid = operationid;
			operationid = genericValue.get("operationId").toString();
			if (!lastid.equals("") || newlist.contains(lastid))
				newlist.add(lastid);
			// Show Data by monitor groupname
			if (remonitorstate.equals("show_disabled")) {
				boolean flag = getMonitorDisabled(operationid);
				if (flag) {
					if (!newlist.contains(operationid) || lastid.equals("")) {
						FastMap runinfo = getMonitorRuninfo(operationid);
						partmap.put("id", operationid);
						groupname = getMonitorGroupName(operationid);
						if (groupname.equals("Monitor_not_found")) {
							continue;
						}
						try {
							groupname = new String(groupname
									.getBytes("iso8859-1"), "UTF-8");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						partmap.put("groupname", groupname);
						partmap.put("name", runinfo.get("name"));
						partmap.put("latestStatus", "disable");
						partmap.put("description", "nodata");
						partmap.put("logTime", "nodata");
						data.add(partmap);
					}
				}
			} else if (!regroupname.equals("") && regroupname != null) {
				String group = getMonitorGroupName(operationid);
				if (group.equals("Monitor_not_found")) {
					continue;
				}
				try {
					group = new String(group.getBytes("iso8859-1"), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				List regroupnamestr = Arrays.asList(regroupname.split(","));
				if (regroupnamestr.contains(group)) {
					if (!newlist.contains(operationid) || lastid.equals("")) {
						partmap.put("id", operationid);
						partmap.put("groupname", group);
						monitorname = getMonitorName(operationid);
						partmap.put("name", monitorname);
						latestStatus = genericValue.get("category").toString();
						partmap.put("latestStatus", latestStatus);
						description = genericValue.get("description");
						String desc = description.toString();
						desc = desc.replace("decription=", "");
						partmap.put("description", desc);
						Date newdate = (Date) genericValue.get("logTime");
						partmap.put("logTime", newdate);
						data.add(partmap);
					}
				} else {
					continue;
				}
				// Show Data by monitortype
			} else if (!remonitortype.equals("all_type")
					&& !remonitortype.equals("")) {
				String monitortype = getMonitorType(operationid);
				if (monitortype.equals("MonitorType_not_found")) {
					continue;
				}
				if (monitortype.equals(remonitortype)) {
					if (!newlist.contains(operationid) || lastid.equals("")) {
						partmap.put("id", operationid);
						groupname = getMonitorGroupName(operationid);
						if (groupname.equals("Monitor_not_found")) {
							continue;
						}
						try {
							groupname = new String(groupname
									.getBytes("iso8859-1"), "UTF-8");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						partmap.put("groupname", groupname);
						monitorname = getMonitorName(operationid);
						partmap.put("name", monitorname);
						latestStatus = genericValue.get("category").toString();
						partmap.put("latestStatus", latestStatus);
						description = genericValue.get("description");
						String desc = description.toString();
						desc = desc.replace("decription=", "");
						partmap.put("description", desc);
						Date newdate = (Date) genericValue.get("logTime");
						partmap.put("logTime", newdate);
						data.add(partmap);
					}
				}
			} else if (!newlist.contains(operationid) || lastid.equals("")) {
				// Show All
				partmap.put("id", operationid);
				groupname = getMonitorGroupName(operationid);
				if (groupname.equals("Monitor_not_found")) {
					continue;
				}
				try {
					groupname = new String(groupname.getBytes("iso8859-1"),
							"UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				partmap.put("groupname", groupname);
				monitorname = getMonitorName(operationid);
				partmap.put("name", monitorname);
				latestStatus = genericValue.get("category").toString();
				partmap.put("latestStatus", latestStatus);
				description = genericValue.get("description");
				String desc = description.toString();
				desc = desc.replace("decription=", "");
				partmap.put("description", desc);
				Date newdate = (Date) genericValue.get("logTime");
				partmap.put("logTime", newdate);
				data.add(partmap);
			}
		}
		response.put("custom_filter_data", data);
		newlist.clear();
		return response;
	}

	/**
	 * Get Monitor GroupName
	 * 
	 * @param id
	 * @return gorupname
	 */
	private static String getMonitorGroupName(String id) {
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
		String newnodesstr = nodes.toString();
		if (newnodesstr.equals("{error=monitor_not_found}")) {
			classname = "Monitor_not_found";
			return classname;
		} else {
			classname = nodes.get("class").toString();
			if (!classname.equals("group")) {
				return getMonitorGroupName(nodes.get("parent").toString());
			} else {
				String gorupname = nodes.get("name").toString();
				return gorupname;
			}
		}
	}

	public static String getMonitorName(String monitorID) {
		String retdata = null;
		List<Object> list = new ArrayList<Object>();
		list.add(new OtpErlangAtom(monitorID));
		try {
			retdata = (String) OtpGateway.getOtpInterface().call(
					"api_siteview", "get_object_name", list);
			retdata = new String(retdata.getBytes("iso8859-1"), "UTF-8");
		} catch (Exception ex) {
			ex.printStackTrace();
			try {

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return retdata;
	}

	/**
	 * Get Monitor Type
	 * 
	 * @param id
	 * @return classname
	 */
	private static String getMonitorType(String id) {
		String classname = "";
		List<Object> list = new ArrayList<Object>();
		FastMap nodes = null;
		list.add(new OtpErlangAtom(id));
		try {
			nodes = (FastMap) OtpGateway.getOtpInterface().call("api_monitor",
					"info", list);
			String newnodesstr = nodes.toString();
			if (newnodesstr.equals("{error=monitor_not_found}")) {
				classname = "MonitorType_not_found";
				return classname;
			} else {
				classname = nodes.get("class").toString();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return classname;
	}

	/**
	 * Get Monitor Type
	 * 
	 * @param id
	 * @return classname
	 */
	private static boolean getMonitorDisabled(String monitorid) {
		boolean flag = false;
		String disabled = "";
		List<Object> list = new ArrayList<Object>();
		FastMap nodes = null;
		list.add(new OtpErlangAtom(monitorid));
		try {
			nodes = (FastMap) OtpGateway.getOtpInterface().call("api_monitor",
					"info", list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		disabled = nodes.get("disabled").toString();
		if (disabled.equals("true")) {
			flag = true;
		}
		return flag;
	}

	/**
	 * Get Monitor RunInfo
	 * 
	 * @param id
	 * @return classname
	 */
	private static FastMap getMonitorRuninfo(String monitorid) {
		boolean flag = false;
		String disabled = "";
		List<Object> list = new ArrayList<Object>();
		FastMap nodes = null;
		list.add(new OtpErlangAtom(monitorid));
		try {
			nodes = (FastMap) OtpGateway.getOtpInterface().call("api_monitor",
					"info", list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nodes;
	}

	public static void main(String[] args) {

		FastMap returnvalue = getMonitorRuninfo("0.2.1");

	}

	/**
	 * query all filter
	 * 
	 * @author xia.liu
	 * @param dctx
	 *            The DispatchContext that this service is operating in
	 *@param context
	 *            Map containing the input parameters
	 *@return Map with the result of the service, the output parameters
	 * @throws GenericEntityException
	 *             SQL Example : SELECT
	 *             MFID,ENTITYNAME,GROUPNAME,MONITORDESCRIPE
	 *             ,MONITORNAME,MONITORSTATE,MONITORTYPE,
	 *             MONITORTYPENAME,NODEID,
	 *             REFRESHFRE,SHOWHIDENAME,SORT,SORTNAME,TITLE,TAGID,TAGNAME
	 *             FROM MONITORFILTER
	 * */
	public static Map<String, Object> allFilter(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		String tablename = (String) context.get("tablename");
		int fromn = (Integer) context.get("from");
		int ton = (Integer) context.get("to");
		DynamicViewEntity dve = new DynamicViewEntity();
		Collection<String> fieldsToSelect = FastList.newInstance();

		fieldsToSelect.add("mfid");
		fieldsToSelect.add("entityname");
		fieldsToSelect.add("groupname");
		fieldsToSelect.add("monitordescripe");
		fieldsToSelect.add("monitorname");
		fieldsToSelect.add("monitorstate");
		fieldsToSelect.add("monitortype");
		fieldsToSelect.add("monitortypename");
		fieldsToSelect.add("nodeid");
		fieldsToSelect.add("refreshfre");
		fieldsToSelect.add("showhidename");
		fieldsToSelect.add("sort");
		fieldsToSelect.add("sortname");
		fieldsToSelect.add("title");
		fieldsToSelect.add("tagid");
		fieldsToSelect.add("tagname");

		fieldsToSelect.add("starttime");
		fieldsToSelect.add("endtime");
		dve.addMemberEntity("OperLog", tablename);
		dve.addAliasAll("OperLog", "");
		Delegator delegator = dctx.getDelegator();
		EntityListIterator resultiterator = delegator
				.findListIteratorByCondition(dve, null, null, fieldsToSelect,
						null, null);
		List<GenericValue> result = resultiterator.getPartialList(fromn, ton
				- fromn);
		FastMap OutDataList = FastMap.newInstance();
		FastMap partmap = FastMap.newInstance();
		String mfid = "", Sort = "", MonitorTypeName = "", GroupName = "", SortName = "", Titile = "", MonitorDescripe = "", ShowHideName = "", MonitorName = "", EntityName = "", RefreshFre = "", TagId = "", TagName = "", NodeId = "", MonitorType = "", MonitorState = "";
		Date StartTime = new Date();
		Date EndTime = new Date();
		for (GenericValue genericValue : result) {

			FastMap partmapchildren = FastMap.newInstance();
			mfid = genericValue.get("mfid").toString();
			// FastMap partmapchildren = FastMap.newInstance();
			try {

				Sort = genericValue.get("sort").toString();
				partmapchildren.put("Sort", Sort);
			} catch (Exception e) {
				partmapchildren.put("Sort", Sort);
			}
			try {
				MonitorTypeName = genericValue.get("monitortypename")
						.toString();
				partmapchildren.put("MonitorTypeName", MonitorTypeName);
			} catch (Exception e) {
				partmapchildren.put("MonitorTypeName", MonitorTypeName);
			}
			try {
				GroupName = genericValue.get("groupname").toString();
				partmapchildren.put("GroupName", GroupName);
			} catch (Exception e) {
				partmapchildren.put("GroupName", GroupName);
			}
			try {
				SortName = genericValue.get("sortname").toString();
				partmapchildren.put("SortName", SortName);
			} catch (Exception e) {
				partmapchildren.put("SortName", SortName);
			}
			try {
				Titile = genericValue.get("title").toString();
				partmapchildren.put("Titile", Titile);

			} catch (Exception e) {
				partmapchildren.put("Titile", Titile);
			}
			try {
				MonitorDescripe = genericValue.get("monitordescripe")
						.toString();
				partmapchildren.put("MonitorDescripe", MonitorDescripe);
			} catch (Exception e) {
				partmapchildren.put("MonitorDescripe", MonitorDescripe);
			}
			try {
				ShowHideName = genericValue.get("showhidename").toString();
				partmapchildren.put("ShowHideName", ShowHideName);
			} catch (Exception e) {
				partmapchildren.put("ShowHideName", ShowHideName);
			}
			try {
				MonitorName = genericValue.get("monitorname").toString();
				partmapchildren.put("MonitorName", MonitorName);
			} catch (Exception e) {
				partmapchildren.put("MonitorName", MonitorName);
			}
			try {
				EntityName = genericValue.get("entityname").toString();
				partmapchildren.put("EntityName", EntityName);
			} catch (Exception e) {
				partmapchildren.put("EntityName", EntityName);
			}
			try {
				RefreshFre = genericValue.get("refreshfre").toString();
				partmapchildren.put("RefreshFre", RefreshFre);
			} catch (Exception e) {
				partmapchildren.put("RefreshFre", RefreshFre);
			}
			try {
				TagId = genericValue.get("tagid").toString();
				partmapchildren.put("TagId", TagId);
			} catch (Exception e) {
				partmapchildren.put("TagId", TagId);
			}
			try {
				TagName = genericValue.get("tagname").toString();
				partmapchildren.put("TagName", TagName);
			} catch (Exception e) {
				partmapchildren.put("TagName", TagName);
			}
			try {
				NodeId = genericValue.get("nodeid").toString();
				partmapchildren.put("NodeId", NodeId);
			} catch (Exception e) {
				partmapchildren.put("NodeId", NodeId);
			}
			try {
				MonitorType = genericValue.get("monitortype").toString();
				partmapchildren.put("MonitorType", MonitorType);

			} catch (Exception e) {
				partmapchildren.put("MonitorType", MonitorType);
			}
			try {
				MonitorState = genericValue.get("monitorstate").toString();
				partmapchildren.put("MonitorState", MonitorState);

			} catch (Exception e) {
				partmapchildren.put("MonitorState", MonitorState);
			}
			try {
				StartTime = (Date) genericValue.get("starttime");
				partmapchildren.put("StartTime", StartTime.toString());

			} catch (Exception e) {
				partmapchildren.put("StartTime", StartTime.toString());
			}
			try {
				EndTime = (Date) genericValue.get("endtime");
				partmapchildren.put("EndTime", EndTime.toString());

			} catch (Exception e) {
				partmapchildren.put("EndTime", EndTime.toString());
			}
			// partmap.put(mfid, partmapchildren);
			// OutDataList.add(partmap);
			OutDataList.put(mfid, partmapchildren);
		}

		response.put("filter_datas", OutDataList);
		return response;
	}

	/**
	 * add filter to database
	 * 
	 * @author xia.liu
	 * @param dctx
	 *            The DispatchContext that this service is operating in
	 *@param context
	 *            Map containing the input parameters
	 *@return excute result
	 * @throws GenericEntityException
	 *             SQL Example : INSERT TO
	 *             MONITORFILTER(MFID,ENTITYNAME,GROUPNAME
	 *             ,MONITORDESCRIPE,MONITORNAME,MONITORSTATE,MONITORTYPE,
	 *             MONITORTYPENAME
	 *             ,NODEID,REFRESHFRE,SHOWHIDENAME,SORT,SORTNAME,TITLE
	 *             ,TAGID,TAGNAME)
	 *             VALUES(mfid,ENTITYNAME,GROUPNAME,MONITORDESCRIPE
	 *             ,MONITORNAME,MONITORSTATE,MONITORTYPE,
	 *             MONITORTYPENAME,NODEID,
	 *             REFRESHFRE,SHOWHIDENAME,SORT,SORTNAME,TITLE,TAGID,TAGNAME)
	 * 
	 *             *
	 */
	public Map<String, Object> addFilter(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> response = ServiceUtil.returnSuccess();
		// Get params
		String tablename = (String) context.get("tablename");
		String mfid = "", Sort = "", GroupName = "", Titile = "", MonitorDescripe = "", MonitorName = "", EntityName = "", RefreshFre = "", TagId = "", TagName = "", NodeId = "", MonitorType = "", MonitorState = "", StartTime = "", EndTime = "";
		StartTime = context.get("starttime").toString();
		EndTime = context.get("endtime").toString();
		mfid = (String) context.get("mfid");
		// Date starttime=(Date)context.get("starttime");
		// Date endtime=(Date)context.get("endtime");
		Sort = (String) context.get("sort");
		GroupName = (String) context.get("groupname");
		Titile = (String) context.get("title");
		MonitorDescripe = (String) context.get("monitordescripe");
		MonitorName = (String) context.get("monitorname");
		EntityName = (String) context.get("entityname");
		RefreshFre = (String) context.get("refreshfre");
		TagId = (String) context.get("tagid");
		TagName = (String) context.get("tagname");
		NodeId = (String) context.get("nodeid");
		MonitorType = (String) context.get("monitortype");
		MonitorState = (String) context.get("monitorstate");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Map<String, String> fields = UtilMisc.toMap("id", delegator
				.getNextSeqId(tablename), "mfid", mfid, "starttime", Timestamp
				.valueOf(StartTime), "endtime", Timestamp.valueOf(EndTime),
				"sort", Sort, "monitortypename",
				context.get("monitortypename"), "groupname", GroupName,
				"sortname", context.get("sortname"), "title", context
						.get("title"), "monitordescripe", MonitorDescripe,
				"showhidename", context.get("showhidename"), "monitorname",
				MonitorName, "entityname", EntityName, "refreshfre",
				RefreshFre, "tagid", TagId, "tagname", TagName, "nodeid",
				NodeId, "monitortype", MonitorType, "monitorstate",
				MonitorState);

		GenericValue log = delegator.makeValue(tablename, fields);
		delegator.create(log);
		// delegator.create(tablename,)
		return response;
	}

	/**
	 * add filter to database
	 * 
	 * @author xia.liu
	 * @param dctx
	 *            The DispatchContext that this service is operating in
	 *@param context
	 *            Map containing the input parameters
	 *@return excute result
	 * @throws GenericEntityException
	 *             SQL Example : INSERT TO
	 *             MONITORFILTER(MFID,ENTITYNAME,GROUPNAME
	 *             ,MONITORDESCRIPE,MONITORNAME,MONITORSTATE,MONITORTYPE,
	 *             MONITORTYPENAME
	 *             ,NODEID,REFRESHFRE,SHOWHIDENAME,SORT,SORTNAME,TITLE
	 *             ,TAGID,TAGNAME)
	 *             VALUES(mfid,ENTITYNAME,GROUPNAME,MONITORDESCRIPE
	 *             ,MONITORNAME,MONITORSTATE,MONITORTYPE,
	 *             MONITORTYPENAME,NODEID,
	 *             REFRESHFRE,SHOWHIDENAME,SORT,SORTNAME,TITLE,TAGID,TAGNAME)
	 * 
	 *             *
	 */
	public Map<String, Object> modifyFilter(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> response = ServiceUtil.returnSuccess();
		this.deleteFilter(dctx, context);
		this.addFilter(dctx, context);
		return response;
	}

	/**
	 * delete filter by mfid
	 * 
	 * @author xia.liu
	 * @param dctx
	 *            The DispatchContext that this service is operating in
	 *@param context
	 *            Map containing the input parameters
	 *@return excute result
	 * @throws GenericEntityException
	 *             SQL Example : DELETE FROM monitorfilter WHERE MFID=mfid
	 * 
	 *             *
	 */
	public Map<String, Object> deleteFilter(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> response = ServiceUtil.returnSuccess();
		// Get params
		String tablename = (String) context.get("tablename");
		String mfid = "";
		mfid = (String) context.get("mfid");
		// List<GenericValue> listMFID = delegator.findByAnd("mfid",
		// UtilMisc.toMap("mfid", mfid));
		// delegator.removeAll(listMFID);
		Collection<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("mfid");
		List<EntityCondition> entityConditionList = FastList.newInstance();
		// entityConditionList.add(EntityCondition.makeCondition("logTime",
		// EntityOperator.BETWEEN,UtilMisc.toList(startTime,endTime)));
		entityConditionList.add(EntityCondition.makeCondition("mfid",
				EntityOperator.EQUALS, mfid));
		EntityCondition condition = EntityCondition.makeCondition(
				entityConditionList, EntityOperator.AND);
		delegator.removeByCondition(tablename, condition);
		return response;
	}

	/*
	 * mobile
	 */
	public static Map<String, Object> queryCVactive(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		// queryOperationIds(dctx, context);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String tableindex = sdf.format(new Date());
		tableindex = "_" + tableindex.replaceAll("-", "_");//获取表索引
		// Get params
		String tablename = (String) context.get("tablename")+tableindex;
		int from = (Integer) context.get("from");
		int to = (Integer) context.get("to");
		Date start = null;
		Date end = null;
		String startTime = (String) context.get("starttime");
		String endTime = (String) context.get("endtime");
		// String orderby = (String) context.get("orderby");
		Set ids = (Set) context.get("ids");
		List<EntityCondition> entityConditionList = FastList.newInstance();
		Iterator<String> iterator = ids.iterator();
		List<EntityCondition> entityConditionListid = FastList.newInstance();
		while (iterator.hasNext()) {
			String id = iterator.next();
			entityConditionListid.add(EntityCondition.makeCondition(
					"operationId", EntityOperator.EQUALS, id));
		}
		EntityCondition conditionid = EntityCondition.makeCondition(
				entityConditionListid, EntityOperator.OR);
		entityConditionList.add(conditionid);
		entityConditionList.add(EntityCondition.makeCondition("logTime",
				EntityOperator.BETWEEN, UtilMisc.toList(Timestamp
						.valueOf(startTime), Timestamp.valueOf(endTime))));

		EntityCondition condition = EntityCondition.makeCondition(
				entityConditionList, EntityOperator.AND);
		DynamicViewEntity dve = new DynamicViewEntity();
		Collection<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("operationId");
		fieldsToSelect.add("name");
		fieldsToSelect.add("logTime");
		fieldsToSelect.add("category");
		fieldsToSelect.add("description");
		dve.addMemberEntity("OperLog", tablename);
		dve.addAliasAll("OperLog", "");
		List<String> orderBy = UtilMisc.toList("logTime DESC");
		Delegator delegator = dctx.getDelegator();
		EntityListIterator resultiterator = delegator
				.findListIteratorByCondition(dve, condition, null,
						fieldsToSelect, orderBy, null);
		List<GenericValue> result = resultiterator.getPartialList(from, to
				- from);
		String operationid = "";
		String monitorname = "";
		String latestStatus = "";
		String groupname = "";
		Object description = null;
		FastList datalist = FastList.newInstance();
		List<String> newlist = FastList.newInstance();
		String lastid = "";
		for (GenericValue genericValue : result) {
			FastMap partmap = FastMap.newInstance();
			lastid = operationid;
			operationid = genericValue.get("operationId").toString();
			if (!lastid.equals("") || newlist.contains(lastid))
				newlist.add(lastid);
			if (!newlist.contains(operationid) || lastid.equals("")) {
				partmap.put("id", operationid);
				groupname = getMonitorGroupName(operationid);
				if (groupname.equals("Monitor_not_found")) {
					continue;
				}
				partmap.put("groupname", groupname);
				monitorname = getMonitorName(operationid);
				partmap.put("name", monitorname);
				latestStatus = genericValue.get("category").toString();
				partmap.put("latestStatus", latestStatus);
				description = genericValue.get("description");
				String desc = description.toString();
				desc = desc.replace("decription=", "");
				partmap.put("description", desc);
				Date newdate = (Date) genericValue.get("logTime");
				partmap.put("logTime", newdate);
				datalist.add(partmap);
			}
		}
		response.put("new_error_data", datalist);
		newlist.clear();
		return response;
	}

	public static Map<String, Object> queryCVhistory(DispatchContext dctx,
			Map<String, ?> context) throws GenericEntityException {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		// queryOperationIds(dctx, context);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String tableindex = sdf.format(new Date());
		tableindex = "_" + tableindex.replaceAll("-", "_");//获取表索引
		// Get params
		String tablename = (String) context.get("tablename")+tableindex;
		int from = (Integer) context.get("from");
		int to = (Integer) context.get("to");
		// Date start = null;
		// Date end = null;
		// String startTime = (String) context.get("starttime");
		// String endTime = (String) context.get("endtime");
		// // String orderby = (String) context.get("orderby");
		// String category = (String) context.get("category");
		List<EntityCondition> entityConditionList = FastList.newInstance();

		// entityConditionList.add(EntityCondition.makeCondition("category",
		// EntityOperator.EQUALS, category));
		// entityConditionList.add(EntityCondition.makeCondition("logTime",
		// EntityOperator.BETWEEN, UtilMisc.toList(Timestamp
		// .valueOf(startTime), Timestamp.valueOf(endTime))));

		EntityCondition condition = EntityCondition.makeCondition(
				entityConditionList, EntityOperator.AND);

		DynamicViewEntity dve = new DynamicViewEntity();
		Collection<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("id");
		fieldsToSelect.add("mid");
		fieldsToSelect.add("monitorname");
		fieldsToSelect.add("confirmtime");
		fieldsToSelect.add("groupname");
		fieldsToSelect.add("category");
		fieldsToSelect.add("monitordescripe");
		fieldsToSelect.add("monitorlogdescripe");
		dve.addMemberEntity("OperLog", tablename);
		dve.addAliasAll("OperLog", "");
		List<String> orderBy = UtilMisc.toList("confirmtime DESC");
		Delegator delegator = dctx.getDelegator();
		EntityListIterator resultiterator = delegator
				.findListIteratorByCondition(dve, condition, null,
						fieldsToSelect, orderBy, null);
		List<GenericValue> result = resultiterator.getPartialList(from, to
				- from);
		String operationid = "";
		String monitorname = "";
		String latestStatus = "";
		String groupname = "";
		Object description = null;
		FastList datalist = FastList.newInstance();
		List<String> newlist = FastList.newInstance();
		String lastid = "";
		for (GenericValue genericValue : result) {
			FastMap partmap = FastMap.newInstance();
			lastid = operationid;
			operationid = genericValue.get("mid").toString();
			if (!lastid.equals("") || newlist.contains(lastid))
				newlist.add(lastid);
			if (!newlist.contains(operationid) || lastid.equals("")) {
				partmap.put("id", operationid);
				groupname = genericValue.get("groupname").toString();
				if (groupname.equals("Monitor_not_found")) {
					continue;
				}
				partmap.put("groupname", groupname);
				monitorname = genericValue.get("monitorname").toString();
				partmap.put("name", monitorname);
				latestStatus = genericValue.get("category").toString();
				partmap.put("latestStatus", latestStatus);
				description = genericValue.get("monitorlogdescripe");
				String desc = description.toString();
				desc = desc.replace("decription=", "");
				partmap.put("description", desc);
				Date newdate = (Date) genericValue.get("confirmtime");
				partmap.put("logTime", newdate);
				datalist.add(partmap);
			}
		}
		response.put("new_error_data", datalist);
		newlist.clear();
		return response;
	}

}
