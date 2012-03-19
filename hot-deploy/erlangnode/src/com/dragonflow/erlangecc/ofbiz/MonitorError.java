package com.dragonflow.erlangecc.ofbiz;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

public class MonitorError {
	public final static String module = MonitorError.class.getName();
	
	public static Map<String, Object> monitorError(DispatchContext dctx,
			Map<String, ?> context) throws Exception {
		Map<String, Object> response = ServiceUtil.returnSuccess();
		//insert master data
		Delegator delegator = dctx.getDelegator();		
		String errorlogId = delegator.getNextSeqId("monitorerror");		
    	//erlang Params set in monitor.erl
        Map<String, String> fields = UtilMisc.toMap("errorlogId", errorlogId,
          "monitorerrorid",errorlogId,
          "errortype",context.get("errortype").toString(),
          "errordate",context.get("errordate").toString(),
          "errortitle",context.get("errortitle").toString()
          );
		GenericValue log = delegator.makeValue("monitorerror", fields);
		delegator.create(log);
		return response;
	}
	public static Map<String, Object> smonitorError(DispatchContext dctx,
			Map<String, ?> context) throws Exception {
	    Map<String, Object> response = ServiceUtil.returnSuccess();
	   //获取参数
	    FastList OutDataList = FastList.newInstance();
        String tablename=(String)context.get("tablename");
        int fromn=(Integer)context.get("from");
        int ton=(Integer)context.get("to");
        //过滤条件
        List<String> orderlist=null;
        List<EntityCondition> entityConditionList = FastList.newInstance();
        EntityCondition condition = EntityCondition.makeCondition(entityConditionList, EntityOperator.AND);  	
	    DynamicViewEntity dve = new DynamicViewEntity();
	    Collection<String> fieldsToSelect = FastList.newInstance();
	    fieldsToSelect.add("monitorerrorid");
	    fieldsToSelect.add("errortype");
	    fieldsToSelect.add("errordate");
	    fieldsToSelect.add("errortitle");
	    dve.addMemberEntity("OperLog", "monitorerror");
	    dve.addAliasAll("OperLog", "");
	    Delegator delegator = dctx.getDelegator();
	    EntityListIterator resultiterator= delegator.findListIteratorByCondition(dve,condition, null, fieldsToSelect, null, null);
	    List<GenericValue> result= resultiterator.getPartialList(fromn,ton-fromn);
	    for (GenericValue genericValue : result) {
	    	FastList childlist = FastList.newInstance();
	        childlist.add(0, genericValue.get("monitorerrorid"));
	        childlist.add(1, genericValue.get("errortype"));
	        childlist.add(2, genericValue.get("errordate"));
	        childlist.add(3, genericValue.get("errortitle") );
	        OutDataList.add(childlist);
		}
	    response.put("loggervalues",OutDataList );
		return response;

}
}
