package com.dragonflow.erlangecc.ofbiz;

import java.sql.Timestamp;
import java.util.Collection;
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
import org.ofbiz.service.ServiceUtil;


/**
 * Common Services
 */
public class AlertServices {

	//报警日志查询实现方法
	 public static Map<String, Object> queryAlertLogger(DispatchContext dctx,
				Map<String, ?> context) throws GenericEntityException {	
			Map<String, Object> response = ServiceUtil.returnSuccess();
		 //获取参数
			String tablename = (String) context.get("tablename");
			int fromcount = (Integer) context.get("from");
			int tocount = (Integer) context.get("to");
			String orderby = (String) context.get("orderby");
			String starttime=(String) context.get("starttime");
			String endtime=(String) context.get("endtime");
			
			String name = context.get("name")==null?"":context.get("name").toString();
			String receiver = context.get("receiver")==null?"":context.get("receiver").toString();
			String type = context.get("type")==null?"":context.get("type").toString();
			String alert_level = context.get("alert_level")==null?"":context.get("alert_level").toString();
			String alertid = context.get("alertId")==null?"":context.get("alertId").toString();
			
			List<EntityCondition> entityConditionList = FastList.newInstance();
			List<EntityCondition> entityConditionListNames = FastList.newInstance();
			//报警规则id 只有一个id传过来
			if(!alertid.isEmpty()){
				entityConditionList.add(EntityCondition.makeCondition(
						"alertId", EntityOperator.EQUALS,alertid));
			}
			if(!name.isEmpty()){
				String[] names=name.split(";");
				for(int i=0;i<names.length;i++){
					entityConditionListNames.add(EntityCondition.makeCondition(
							"name", EntityOperator.EQUALS, names[i]));
				}
			}
			//报警名称
			EntityCondition conditionnames = EntityCondition.makeCondition(
					entityConditionListNames, EntityOperator.OR);
		
			entityConditionList.add(conditionnames);
			
			//报警接收人
			if(!receiver.isEmpty()){
				entityConditionList.add(EntityCondition.makeCondition("receiver",EntityOperator.EQUALS,receiver));
			}
			//报警类型
			if(!type.isEmpty()){
			   entityConditionList.add(EntityCondition.makeCondition("alerttype",EntityOperator.EQUALS,type));
			}
			//报警级别
			if(!alert_level.isEmpty()){
				   entityConditionList.add(EntityCondition.makeCondition("alert_level",EntityOperator.EQUALS,alert_level));
			}
			
			//查询时间段
			Timestamp startTime=Timestamp.valueOf(starttime);
		    Timestamp endTime=Timestamp.valueOf(endtime);
		    entityConditionList.add(EntityCondition.makeCondition("alerttime", EntityOperator.BETWEEN,UtilMisc.toList(startTime,endTime)));
		    
		    //总过滤条件
		    EntityCondition condition = EntityCondition.makeCondition(
					entityConditionList, EntityOperator.AND);
		    
			DynamicViewEntity dve = new DynamicViewEntity();
			Collection<String> fieldsToSelect = FastList.newInstance();
			fieldsToSelect.add("id");
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
			fieldsToSelect.add("cleartime");
			fieldsToSelect.add("times");
			dve.addMemberEntity("ALog", "AlertLog");
			dve.addAliasAll("ALog", "");
			Delegator delegator = dctx.getDelegator();
			List<String> orderBy=UtilMisc.toList("-alerttime");
			EntityListIterator resultiterator = delegator
			.findListIteratorByCondition(dve, condition, null,
					fieldsToSelect, orderBy, null);
			List<GenericValue> result = resultiterator.getPartialList(fromcount,tocount);
			resultiterator.last();
			int totalsiz=resultiterator.currentIndex();
			int i=-1;
			 FastMap resultList = FastMap.newInstance();
			for(GenericValue genericValue:result){
				i++;
				 FastMap childlist = FastMap.newInstance();
			        childlist.put("alertId", genericValue.get("alertId"));
			        childlist.put("alerttype", genericValue.get("alerttype"));
			        childlist.put("name", genericValue.get("name"));
			        childlist.put("monitor", genericValue.get("monitor") );
			        childlist.put("receiver", genericValue.get("receiver") );
			        childlist.put("title", genericValue.get("title") );
			        childlist.put("alerttime", genericValue.get("alerttime") );
			        childlist.put("result", genericValue.get("result") );
			        childlist.put("alert_level", genericValue.get("alert_level") );
			        childlist.put("content", genericValue.get("content") );
			        childlist.put("groupid", genericValue.get("groupid") );
			        childlist.put("responsetime", genericValue.get("responsetime") );
			        childlist.put("responder", genericValue.get("responder") );
			        childlist.put("responsecontent", genericValue.get("responsecontent") );
			        childlist.put("cleartime", genericValue.get("cleartime") );
			        childlist.put("times", genericValue.get("times") );
			        childlist.put("id", genericValue.get("id"));
			        resultList.put(i, childlist);
			}
	    response.put("loggervalues",resultList );
	    response.put("totalsize", totalsiz);
		 return response;
	 }
}