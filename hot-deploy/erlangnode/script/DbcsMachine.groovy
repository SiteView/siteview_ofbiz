import org.ofbiz.service.ServiceUtil;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.*;
import org.ofbiz.entity.util.*;
import com.dragonflow.test.TestServices;
/*
def parse(s)
{
	s1=s.replace(" ", "");
	s2=s1.replace("my.", "");
	r = s2.tokenize('&|');
	def map2 = [:];
	for(r1 in r)
	{
	    ss = r1.tokenize('=');
	    map2.(ss[0]) = ss[1];.
	}
	map2;
};*/



Debug.logInfo("-=-=-=- Dbcs_Machine Start-=-=-=-", "");
result = ServiceUtil.returnSuccess();
return TestServices.testPing(dispatchContext,context);

if (context.action) 
{
    action = context.action;
    table = context.table;
    inData = context.inData[0];
    Debug.logInfo("-=-=-=- Dbcs_Machine =-=-=-action:" + action, "");
    Debug.logInfo("-=-=-=- Dbcs_Machine =-=-=-table:" + table, "");
    Debug.logInfo("-=-=-=- Dbcs_Machine =-=-=-inData:" + inData, "");
    result.successMessage = "Got action [" + action + "] and finished fine";

//    GenericValue V1 = delegator.findOne("CIType", [description:inData.type]);
//    println("-=-=-=- Dbcs_Machine Other -=-=-=  V1:" + V1);
    

//    OtherData = inData.other;
//    inData.other = ""; 
//    println("-=-=-=- Dbcs_Machine Other -=-=-=:" + inData.other);

    //create a new CIType is not created b4.
    if(delegator.findByPrimaryKey("CIType", [ciTypeId:table]) == null && action == "create")
    {
	    println("-=-=-=- Dbcs_Machine Add CiType -=-=-=:" + table);	    
	    GenericValue CiType = delegator.makeValue("CIType", [ciTypeId:table]);
	    delegator.create(CiType);

	    println("-=-=-=- Dbcs_Machine Add CiTypeAttribute -=-=-=:" + table);
	    inData.each{key,value->
	    	GenericValue CITypeAttr = delegator.makeValue("CITypeAttr", [ciTypeId:table, attrName:key]);
	    	delegator.create(CITypeAttr);
	    }
    }

    //
    if(action == "create")
    {
	    NextId = delegator.getNextSeqId("CI");
	    GenericValue Ci = delegator.makeValue("CI", [ciId:NextId, ciTypeId:table]);
	    delegator.create(Ci);

	    inData.each{key,value->

		//	println("CIAttribute key is: "+ key +" and value is: " + value);
			GenericValue CiAttribute;
			if(value instanceof String)
				CiAttribute = delegator.makeValue("CIAttribute", [ciId:NextId, attrName:key, attrValue:value]);
			else
				CiAttribute = delegator.makeValue("CIAttribute", [ciId:NextId, attrName:key, attrObjValue:value]);
			delegator.create(CiAttribute);
	    }

	    def OutData = [ciId:NextId];
	    CiObjs = delegator.findByAnd("CIAttribute", [ciId:NextId]);
	    for(ciObj in CiObjs)
	    {
		if(ciObj.attrValue != null)
			OutData.(ciObj.attrName) = (ciObj.attrValue);
		else
			OutData.(ciObj.attrName) = (ciObj.attrObjValue);

		//println("Read CIAttribute.OutData1 --- >>>>>>>>>>>>>>>>>" + ciObj.attrName + ":" + ciObj.attrValue);
	    }
	    
	    println("Read CIAttribute From Db ---> CIid:" + NextId + " : Data :" + OutData);

	//    GenericValue CiAttribute1 = delegator.makeValue("CIAttribute", [ciId:NextId, attrName:"otherobj", attrObjValue:OtherData]);
	//    delegator.create(CiAttribute1);

//	result.outData = [ciId:NextId];
	result.outData = OutData;
	
    }
    else if(action == "read")
    {
            Debug.logInfo("-=-=-=- Dbcs_Machine =-=-=readreadreadreadreadreadreadreadreadread:" + action, "");
	    CIAttributes = delegator.findByAnd("CIAttribute", [attrName:"os",attrValue:"nt", attrObjValue:null]);
/*
//	    queryCond = pharse(inData);
	    EntityCondition.makeCondition(["roleTypeIdFrom": "PARENT_ORGANIZATION", "partyIdTo": companyId]),
	    queryCond.each{key,value->
	    EntityCondition.makeCondition("attrName", EntityOperator.EQUALS, value);
	    }
//	    queryCond.each({

            r.collect { key, value ->
	    [attributeName :key, attributeValue:value]

*/
//	    CIAttributes = delegator.findByAnd("CIAttribute", [attrName:"name",attrValue:"d", attrObjValue:null]);
	    def OutDataList = [];
	    def OutData = [ciId:0];
	    for(CIAttribute in CIAttributes)
	    {
		    
		    CiObjs = delegator.findByAnd("CIAttribute", [ciId:CIAttribute.ciId]);
		   // println("Read CIAttribute From eee Db ---> CiObjs:" + CiObjs);
		    OutData.ciId = CIAttribute.ciId;
		    for(ciObj in CiObjs)
		    {
 		       //println("Read CIAttribute From ccc Db ---> ciObj.attrName:" + ciObj.attrName);
		       //println("Read CIAttribute From ccc Db ---> ciObj.attrValue:" + ciObj.attrValue);
		       //println("Read CIAttribute From ccc Db ---> ciObj.attrObjValue:" + ciObj.attrObjValue);
			
			if(ciObj.attrValue != null)
				OutData.(ciObj.attrName) = (ciObj.attrValue);
			else if(ciObj.attrObjValue != null)
				OutData.(ciObj.attrName) = (ciObj.attrObjValue);
			else
				OutData.(ciObj.attrName) = "null";

			//if(ciObj.attrValue == "")
			//	OutData.(ciObj.attrName) = "null";
		    }

		    //OutDataList<<OutData;
		    OutData.other = "null";
		    OutDataList.add(OutData);
		    OutData = [ciId:0];
	    }

	    result.outData = OutDataList;
//	    result.outData = inData;
    }
    else if(action == "update")
    {
	result.outData = inData;
    }
    else if(action == "delete")
    {
	    result.outData = inData;
    }
    else
    {
	    result.outData = inData;
    }

    //result.outData = inData;
    
}
else 
{
    Debug.logInfo("-=-=-=- Dbcs_Machine Error No Action -=-=-=-", "");
    result.successMessage = "Got no message but finished fine anyway";
    result.result = "[no message received]";
}

return result;
