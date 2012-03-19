package com.ericsson.otp.erlang;

import java.io.IOException;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.WSDLException;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.container.Container;
import org.ofbiz.base.container.ContainerConfig;
import org.ofbiz.base.container.ContainerException;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.FileUtil;
import org.ofbiz.base.util.ObjectType;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactory;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.GenericDispatcher;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelParam;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.service.GenericServiceException;
import com.ericsson.otp.erlang.*;


/**
 * MonitorContainer - Container implementation for Erlang Ofbiz Node
  Receive the msg from erlang, convert the data to ofbiz and then call ofbiz service
  OtpErlangMap and OtpErlangList are convert to Ofbiz FastMap and FastList.  OtpErlangTuple is treat the same as list.
 */
public class ErlangContainer implements Container {

    public static final String module = ErlangContainer.class.getName();

    protected String configFileLocation = null;
    protected static String node;
    protected static String cookie;
	protected static String mBox;
    protected OtpNode server;
    
	public static final String ERLANG_NODE = "debug@test-52";
	public static final int TIME_OUT = 2000;
	public static final String DEFAULT_MAIL_BOX = "java_mail_box";
	public static final long RECEIVE_TIME_OUT = 1000 * 60 * 60;
	public static final String UNKOWN_REASONS = "unkown reasons. ";
	public static final String ERROR = "java_node_error";
	public static final String DEFAULT_STATE_KEY = "ok";
	public static final String DEFAULT_STATE_VALUE = "ok";
	public static final String CONTINUE_STATE_KEY = "ok";
	public static final String CONTINE_STATE = "continue";
	
	protected static Delegator delegator;
	protected static LocalDispatcher dispatcher;
	

    //protected int port;

    /**
     * @see org.ofbiz.base.container.Container#init(java.lang.String[], java.lang.String)
     */
    public void init(String[] args, String configFile) {
        this.configFileLocation = configFile;
		String v = System.getProperties().getProperty("OtpConnection.trace");
		Debug.logInfo("OtpConnection.trace=" + v, module);
    }

    /**
     * @see org.ofbiz.base.container.Container#init(java.lang.String[], java.lang.String)
     */
    public boolean start() throws ContainerException {
        Debug.logInfo("Start Erlang container ", module);
        // get the container config
        ContainerConfig.Container cfg = ContainerConfig.getContainer("erlang-container", configFileLocation);
        
        // get the container properties
        ContainerConfig.Container.Property erlangNode = cfg.getProperty("node");
        if (erlangNode == null || UtilValidate.isEmpty(erlangNode.value)) {
            throw new ContainerException("Invalid erlangNode defined in container configuration");
        } else {
            this.node = erlangNode.value;
        }
		
        ContainerConfig.Container.Property erlangCookie = cfg.getProperty("cookie");
        if (erlangCookie == null || UtilValidate.isEmpty(erlangCookie.value)) {
            throw new ContainerException("Invalid erlangCookie defined in container configuration");
        } else {
            this.cookie = erlangCookie.value;
        }

		ContainerConfig.Container.Property erlangMBox = cfg.getProperty("mailBox");
        if (erlangMBox == null || UtilValidate.isEmpty(erlangMBox.value)) {
            throw new ContainerException("Invalid erlangMBox defined in container configuration");
        } else {
            this.mBox = erlangMBox.value;
        }
        
        // get the delegator for this container
        delegator = DelegatorFactory.getDelegator("default");

        // create the LocalDispatcher
        dispatcher = GenericDispatcher.getLocalDispatcher("ErlangDispatcher", delegator);

        Runnable ofbizNode = new Runnable() {
            public void run() {
              serve(delegator,dispatcher, node,cookie,mBox);
            }
          };
          
        new Thread(ofbizNode).start();
        
        return true;

	}
    
    public static void serve(Delegator delegator, LocalDispatcher dispatcher, String node, String cookie, String mBox) {
    	
		OtpNode ofbizNode = null;
		OtpMbox mailBox = null;
		DispatchContext ctx = dispatcher.getDispatchContext();
		OtpErlangPid from = null;

		int try_count = 1;
		//creating the erlang node
		try {
			//Debug.logInfo("start erlang node:" + node,module);
			//FIXME: failes if no erlang started, need fix, erlang node has to started first, or the EMPD must started
			ofbizNode = new OtpNode(node,cookie);
			mailBox = ofbizNode.createMbox(mBox);
			
			Debug.logInfo("Start erlang node:" + ofbizNode.node(),module);
			String[] nodeNames = ofbizNode.getNames(); // equivalent to nodes() in Erlang.
			for (int i = 0; i < nodeNames.length; i++) { Debug.logInfo("Available node:" + nodeNames[i],module);	}
		}  catch (IOException e) {
			e.printStackTrace();
		}
		
		//wait to receive msg from erlang and processing and return to erlang
		while(true) {
			//empty for each new service call
			ModelService service = null;
			List<ModelParam> serviceInParamList=null, serviceOutParamList=null;
			ModelParam serviceParam=null;
			String serviceName = null;
			String paramType = null;
			GenericValue paramValue = null;
			Map<String, Object> inCtx = FastMap.newInstance();
			OtpErlangObject receivedMsg = null;
			OtpErlangList inParamErlList=null;

			try {
				receivedMsg = mailBox.receive();
				OtpErlangTuple t=(OtpErlangTuple) receivedMsg;
				from = (OtpErlangPid )t.elementAt(0);
				serviceName = ((OtpErlangString) t.elementAt(1)).stringValue();
				inParamErlList = (OtpErlangList) t.elementAt(2);
			}  catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("---------------------------        SERVICE: " + serviceName +"        --------------------");	
			try {
				service = ctx.getModelService(serviceName);
			}  catch (Exception e) {
				Debug.logError("retrieve the related service IN and OUT parameter info error",module);
			}
			
			//Debug.logInfo("erlang(key,value) + service type define -> ofbiz(key, value)",module);
			
			try {	
				inCtx =(Map<String, Object>) ObjectType.simpleTypeConvert(inParamErlList, inCtx.getClass().getName(), null, null,false);
				Debug.logInfo(serviceName + " input is " +inCtx.toString(), module);				 
			} catch (Exception e) {
				Debug.logError("convert Erlang message to service IN parameter error",module);
			}
			
			//CommunicationFactory.getOfbizservice().runSync(mailBox,receivedMsg);

//			String msgStr = ((OtpErlangString) t.elementAt(2)).stringValue();				 
			Map<String, Object> rtnMapList=null;
				
			try {
				rtnMapList = dispatcher.runSync(serviceName, inCtx);
			} catch (GenericServiceException e) {
				Debug.logError(e, module);
//		            return ServiceUtil.returnError(e.getMessage()); 
			}
			
			OtpErlangList erlResultList = null; //list of tuple(map)
//			List<OtpErlangTuple> ResultList=FastList.newInstance();
//			rtnMapList = (Map<String, Object>) UtilMisc.toMap("message", "cool");

//			Debug.logInfo(serviceName + " returns " + rtnMapList.toString(), module); 
			try {
				erlResultList = (OtpErlangList)ObjectType.simpleTypeConvert(rtnMapList, "com.ericsson.otp.erlang.OtpErlangList", null, null,false);
//				erlResultList = (OtpErlangList)ObjectType.simpleTypeConvert(rtnMapList, erlResultList.getClass().getName(), null, null,false);
			} catch (Exception e) {
				Debug.logError("convert Erlang message to service OUT parameter error",module);
			}
			
			try {
            	List<Object> tupleList = FastList.newInstance();
            	OtpErlangTuple tempTuple = (OtpErlangTuple) receivedMsg;
                tupleList.add(tempTuple.elementAt(1));
                tupleList.add(erlResultList);
                OtpErlangTuple erlMap = new OtpErlangTuple(tupleList.toArray(new OtpErlangObject[tupleList.size()]));
                mailBox.send(from, (OtpErlangObject)erlMap);

			} catch (Exception e) {
				Debug.logError("return to Erlang error",module);
			}
//			Debug.logInfo(serviceName + " returns to erlang: " + erlResultList.toString() + " from "+ from.toString(), module);
			

		  }//while
	}//end of serve()
	
	public void stop() throws ContainerException {
       
    }
}