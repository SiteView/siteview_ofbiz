package com.dragonflow.test;


import java.util.Map;
import java.util.concurrent.*;

import org.ofbiz.base.util.Debug;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

/**
 * Common Services
 */
public class TestServices {

    public final static String module = TestServices.class.getName();

    /**
     * Generic Test Service
     *@param dctx The DispatchContext that this service is operating in
     *@param context Map containing the input parameters
     *@return Map with the result of the service, the output parameters
     */
    public static Map<String, Object> testListMap(DispatchContext dctx, Map<String, ?> context) {
        Map<String, Object> response = ServiceUtil.returnSuccess();

        if (context.size() > 0) {
            for (Map.Entry<String, ?> entry: context.entrySet()) {
                Object cKey = entry.getKey();
                Object value = entry.getValue();

                Debug.logInfo("In CONTEXTvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv: " + cKey + " => " + value,module);
            }
        }
        if (!context.containsKey("message")) {
            response.put("resp", "no message found"); 
        } else {
            response.put("outListMap", context.get("listMap"));
            response.put("outList", context.get("listValue"));
            response.put("outMap", context.get("mapField"));
            response.put("resp", "service done");
        }

        System.out.println("----- SVCvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv: " + dctx.getName() + " -----"); 
        return response;
    }

    /**
     * Generic Test Ping Service
     *@param dctx The DispatchContext that this service is operating in
     *@param context Map containing the input parameters
     *@return Map with the result of the service, the output parameters
     */
    public static Map<String, Object> testPing(DispatchContext dctx, Map<String, ?> context) {
        Map<String, Object> response = ServiceUtil.returnSuccess();

        if (context.size() > 0) {
            for (Map.Entry<String, ?> entry: context.entrySet()) {
                Object cKey = entry.getKey();
                Object value = entry.getValue();

                System.out.println("---- SVC-CONTEXTvvvccccccccccccccccccccsfsfcccccccccccc: " + cKey + " => " + value);
            }
        }
        if (!context.containsKey("message")) {
            response.put("result", "no message found");
        } else {        	
            Debug.logInfo("-----SERVICE TEST PING vvvcccccccccccccccccsdssdsssffsdfdfsdccccc----- : " + (String) context.get("message"),module);
           /* Long delay = (Long)(context.get("delay"));
            try {
            	Thread.sleep(delay*1000);
            } catch (Exception e) {
    			e.printStackTrace();
    		}
            
            response.put("result", "delay="+context.get("delay") + "s, message="+context.get("message"));*/
			response.put("result",  "message=" + context.get("message"));
        }

        return response;
    }

   
}            	
