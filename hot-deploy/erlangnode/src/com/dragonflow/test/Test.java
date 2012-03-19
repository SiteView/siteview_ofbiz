package com.dragonflow.test;

import java.io.UnsupportedEncodingException;
import org.apache.commons.lang.CharSet;
import com.ericsson.otp.erlang.*;
import com.ericsson.otp.erlang.Foo;
import com.ericsson.otp.erlang.FooSon;

import java.io.PrintStream;
import java.util.List;
import javolution.util.FastList;
import java.util.Map;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.*;


// Referenced classes of package com.ericsson.otp.erlang:
//            OtpNode, OtpErlangAtom, OtpErlangTuple, OtpErlangPid, 
//            OtpErlangString, OtpErlangList, OtpErlangExit, OtpMbox, 
//            OtpMsg, UtilErlang

public class Test
{
	public final static String module = Test.class.getName();

    public Test()
    {
    }

    public static void main(String args[])
        throws Exception
    {

    	OtpNode myNode = new OtpNode("ofbiztest", "3ren");
        OtpMbox myMbox = myNode.createMbox("eccadmin");
        Integer counter = 0;
        OtpErlangAtom myAtom = new OtpErlangAtom("ok");
        OtpErlangObject erlobject;
        OtpErlangObject myMsg=null;
        OtpErlangPid from =null;
        
        Delegator delegator = DelegatorFactory.getDelegator("default");
//        List<GenericValue> geoList = FastList.newInstance();
//        EntityCondition condition = EntityCondition.makeCondition(EntityOperator.OR,
//                EntityCondition.makeCondition("geoTypeId", "STATE"), EntityCondition.makeCondition("geoTypeId", "PROVINCE"),
//                EntityCondition.makeCondition("geoTypeId", "TERRITORY"), EntityCondition.makeCondition("geoTypeId", "MUNICIPALITY"));
//        List<String> sortList = UtilMisc.toList("geoName");
//        try {
//            geoList = delegator.findList("Geo", condition, null, sortList, null, true);
//        } catch (GenericEntityException e) {
//            Debug.logError(e, "Cannot lookup State Geos: " + e.toString(), module);
//        }
        do
            try
            {
            	System.out.println("my node="+myNode.node()+",cookie="+myNode.cookie()+",mailbox="+myMbox.getName());
            	OtpMsg msg = myMbox.receiveMsg();
            	
            	
            	erlobject = msg.getMsg();
        		
//    			Debug.logInfo(erlobject.toString(), module);
    			if (erlobject instanceof OtpErlangTuple) {
    				OtpErlangTuple t = (OtpErlangTuple) erlobject;
    				from = (OtpErlangPid)t.elementAt(0);
    				   				
    				myMsg = t.elementAt(t.arity()-1);
    			}
    				
    			
                
                System.out.println(myMsg.toString());
                
                Map<String,Object> map = (Map<String,Object>)OtpConverter.OtpErlangObject2Object(myMsg);
                System.out.println(map.toString());
                
                List<Object> list = FastList.newInstance();
//                Foo bar = new Foo(100,"bar");
//                Foo bar2  = new Foo(200,"bar2");  
//                FooSon barson = new FooSon(300,"barson");
//                barson.setProp3("new barson3");
//                barson.setProp2("new bar son2");
//                barson.setProp1(2000000);
//                list.add(bar);
//                list.add(bar2);
//                list.add(barson);
//                
                //OtpErlangList erlList = (OtpErlangList)UtilErlang.Object2OtpErlangObject(map);
                list.add("any thing");
                OtpErlangList erlList = (OtpErlangList)OtpConverter.Object2OtpErlangObject(list);
                
                
                
                List<Object> tupleList = FastList.newInstance();
                tupleList.add(myMbox.self());
                tupleList.add(erlList);
                OtpErlangTuple erlMap = new OtpErlangTuple(tupleList.toArray(new OtpErlangObject[tupleList.size()]));
                System.out.println(erlMap.toString());
                
                myMbox.send(from, erlMap);
               
                counter++;
            }
            catch(OtpErlangExit e)
            {
                return;
            }
        while(true);
    }
}