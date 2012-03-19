/*
interface to erlang.
1. call ofbiz service
2. invoking monitor
3. forward log data to xmpp server for logging into entity engine,  reduce the parallel insert operations into data base.

need start xmpp ejabberd server and add users as defined in ofbiz-containers.xml
user = erlangnode, monitorlogger, password = ofbiz
  */

package com.dragonflow.erlangecc.ofbiz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javolution.util.FastList;

import org.apache.vysper.mina.TCPEndpoint;
import org.apache.vysper.storage.StorageProviderRegistry;
import org.apache.vysper.storage.inmemory.MemoryStorageProviderRegistry;
import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.apache.vysper.xmpp.authorization.AccountCreationException;
import org.apache.vysper.xmpp.authorization.AccountManagement;
import org.apache.vysper.xmpp.modules.Module;
import org.apache.vysper.xmpp.modules.extension.xep0049_privatedata.PrivateDataModule;
import org.apache.vysper.xmpp.modules.extension.xep0050_adhoc_commands.AdhocCommandsModule;
import org.apache.vysper.xmpp.modules.extension.xep0054_vcardtemp.VcardTempModule;
import org.apache.vysper.xmpp.modules.extension.xep0077_inbandreg.InBandRegistrationModule;
import org.apache.vysper.xmpp.modules.extension.xep0092_software_version.SoftwareVersionModule;
import org.apache.vysper.xmpp.modules.extension.xep0119_xmppping.XmppPingModule;
import org.apache.vysper.xmpp.modules.extension.xep0133_service_administration.ServiceAdministrationModule;
import org.apache.vysper.xmpp.modules.extension.xep0202_entity_time.EntityTimeModule;
import org.apache.vysper.xmpp.modules.roster.RosterException;
import org.apache.vysper.xmpp.modules.roster.RosterItem;
import org.apache.vysper.xmpp.modules.roster.SubscriptionType;
import org.apache.vysper.xmpp.modules.roster.persistence.RosterManager;
import org.apache.vysper.xmpp.server.XMPPServer;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.ofbiz.base.container.Container;
import org.ofbiz.base.container.ContainerConfig;
import org.ofbiz.base.container.ContainerException;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.FileUtil;
import org.ofbiz.base.util.StringUtil;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactory;
import org.ofbiz.service.GenericDispatcher;
import org.ofbiz.service.LocalDispatcher;

import com.dragonflow.erlangecc.monitor.BaseMonitor;
import com.dragonflow.erlangecc.monitor.OfbizService;
import com.dragonflow.erlangecc.util.ObjectTransformation;
import com.dragonflow.erlangecc.util.SvMessage;
import com.dragonflow.siteview.ErlangUtils;
import com.ericsson.otp.erlang.OtpConverter;
import com.ericsson.otp.erlang.OtpErlangExit;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpMsg;
import com.ericsson.otp.erlang.OtpNode;


public class ErlangGateway implements Container, MessageListener ,Serializable {
	protected String configFileLocation = null;
	public static final String module = ErlangGateway.class.getName();
    
	protected static Delegator delegator;
	protected static LocalDispatcher dispatcher;
	protected static Connection xmppconnection ;
	protected static List<Chat> loggerChat = FastList.newInstance();
	protected static List<Chat> topologyChat = FastList.newInstance();
	protected static List<Chat> debugChat = FastList.newInstance();
	
	public static  String XMPP_SERVER = "localhost";
	public static  Boolean XMPP_DEBUG = false;
    public static  Integer XMPP_SERVER_PORT = 5222;
	public static  String XMPP_USERNAME ;
	public static  String XMPP_PASSWORD;
	public static  String MONITOR_LOG_USERNAMES ;
	public static  String MONITOR_LOG_PASSWORDS ;
	public static  String DEBUG_USERNAMES ;
	public static  String TOPOLOGY_USERNAMES;
    private static List<String> MONITOR_LOG_USERNAME_LIST; 
    private static List<String> DEBUG_USERNAME_LIST;        
    private static List<String> TOPOLOGY_USERNAME_LIST;


	public static final String ERLANG_NODE = "master";
	public static final int TIME_OUT = 5000;
	public static String DEFAULT_MAIL_BOX = "eccadmin";
	public static String JAVA_NODE = "server1";
	public static String COOKIE = "3ren";
	public static final long RECEIVE_TIME_OUT = 1000 * 60 * 60;
	public static final String UNKOWN_REASONS = "unkown reasons. ";
	public static final String ERROR = "java_node_error";
	public static final String DEFAULT_STATE_KEY = "ok";
	public static final String DEFAULT_STATE_VALUE = "ok";
	public static final String CONTINUE_STATE_KEY = "ok";
	public static final String CONTINE_STATE = "continue";
	
	public static  XMPPConnection connection;
    public static  Roster roster;
    public static InetAddress ipaddr=null;

    
    public void xmppLogin(String userName, String password) throws XMPPException {
        ConnectionConfiguration config = new ConnectionConfiguration(XMPP_SERVER, XMPP_SERVER_PORT, XMPP_SERVER);
        connection = new XMPPConnection(config);
 
        //TODO: show reconnect progress
        config.setReconnectionAllowed(true);
        
        int i=0;
        while (!connection.isConnected()) {
        	try {
            	Thread.sleep(i*2000);
            	connection.connect();
            } catch (Exception ex) {
            	i++; i=Math.min(i,20);
            	Debug.logInfo("Connect to XMPP server("+XMPP_SERVER+") failed, wait for "+i+" seconds to retry.", module);            	            	
            }
        }
        
        roster = connection.getRoster();
//        SASLAuthentication.supportSASLMechanism("PLAIN", 0);
        connection.login(userName, password);
        Presence presence = new Presence(Presence.Type.available);
        presence.setStatus("Running ...");
        connection.sendPacket(presence);
        
        loggerChat.clear();
        if (MONITOR_LOG_USERNAME_LIST != null) {
	        for (String username:MONITOR_LOG_USERNAME_LIST){
	          loggerChat.add(connection.getChatManager().createChat(username + "@" + XMPP_SERVER, this));
	        }
        } else {
        	Debug.logError("No monitor loggers configured in ofbiz-containers.xml. Log data will NOT be inserted into database !", module);
        }
//        if (DEBUG_USERNAME_LIST.size()>0) {
//	        for (String username:DEBUG_USERNAME_LIST){
//	          debugChat.add(connection.getChatManager().createChat(username + "@" + XMPP_SERVER, this));
//	        }
//    	}        
        
        debugChat.clear();
        if (DEBUG_USERNAME_LIST != null) {
	        for (String username:DEBUG_USERNAME_LIST){
	          debugChat.add(connection.getChatManager().createChat(username + "@" + XMPP_SERVER, this));
	        }
    	}        
        
        topologyChat.clear();
        if (TOPOLOGY_USERNAME_LIST != null) {
	        for (String username:TOPOLOGY_USERNAME_LIST){
	            topologyChat.add(connection.getChatManager().createChat(username + "@" + XMPP_SERVER , this));
	        }
        } else {
        	Debug.logError("No topology loggers configured in ofbiz-containers.xml. NNM data will NOT be forwarded to topology chart!", module);
        }
    }
 
 

    public void disconnect() {
        connection.disconnect();
    }
 
    public void processMessage(Chat chat, Message message) {
        if (message.getType() == Message.Type.chat) {
            System.out.println(chat.getParticipant() + " says: " + message.getBody());
            try {
                chat.sendMessage(message.getBody() + " echo");
            } catch (XMPPException ex) {
              Debug.logError(ex.toString(), module);
            }
        }
    }
	
	public static String getLibPath(){
		String strCur = System.getProperty("user.dir");
		if (strCur.endsWith(";"))
			strCur.replaceAll(";", "");
		return strCur + File.separator + "lib";
	}
	
	public static String getCurrentDir(){
		String strCur = System.getProperty("user.dir");
		if (strCur.endsWith(";"))
			strCur.replaceAll(";", "");
		return strCur;
	}
	
	/**
     * @see org.ofbiz.base.container.Container#init(java.lang.String[], java.lang.String)
     */
    public void init(String[] args, String configFile) {
        this.configFileLocation = configFile;
        try
        {
          ipaddr= InetAddress.getLocalHost();;
        }
        catch (UnknownHostException e1)
        {
          e1.printStackTrace();
        }
    }
	    /**
     * @see org.ofbiz.base.container.Container#init(java.lang.String[], java.lang.String)
     */
    public boolean start() throws ContainerException {
    	
    	final ContainerConfig.Container cfg = ContainerConfig.getContainer("erlanggateway-container", this.configFileLocation);
    	
    	XMPP_SERVER = cfg.getProperty("xmpp").value;
    	if (XMPP_SERVER==null)  XMPP_SERVER = ipaddr.getHostName();
    	
    	XMPP_DEBUG= Boolean.getBoolean(cfg.getProperty("xmpp_debug").value);
    	String server_port = cfg.getProperty("xmpp_port").value;
    	XMPP_SERVER_PORT = (server_port != null) ? Integer.parseInt(server_port):5222;
        
    	XMPP_USERNAME = cfg.getProperty("xmpp_username").value;
    	if (XMPP_USERNAME == null) XMPP_USERNAME = "erlangnode";
    	
    	XMPP_PASSWORD = cfg.getProperty("xmpp_password").value;
    	if (XMPP_PASSWORD == null) XMPP_PASSWORD = "ofbiz";
        
    	MONITOR_LOG_USERNAMES = cfg.getProperty("monitor_logger_usernames").value;
    	MONITOR_LOG_PASSWORDS = cfg.getProperty("monitor_logger_passwords").value;
        MONITOR_LOG_USERNAME_LIST = StringUtil.split(MONITOR_LOG_USERNAMES,",");
    	
    	DEBUG_USERNAMES = cfg.getProperty("debug_usernames").value;
        DEBUG_USERNAME_LIST = StringUtil.split(DEBUG_USERNAMES,",");        

        TOPOLOGY_USERNAMES = cfg.getProperty("topology_usernames").value;    	
        TOPOLOGY_USERNAME_LIST = StringUtil.split(TOPOLOGY_USERNAMES,",");

    	
    	//start the vysper xmpp server
    	startVysper("component://erlangnode/config/bogus_mina_tls.cert");

    	try
        {
    	  // turn on the enhanced debugger
          XMPPConnection.DEBUG_ENABLED = XMPP_DEBUG;
          
          //start the main client to receive the data into xmpp
          xmppLogin(XMPP_USERNAME, XMPP_PASSWORD);
          
          //starting the multiple clients to log the data into entity
          final List<String> pwdList = StringUtil.split(MONITOR_LOG_PASSWORDS, ",");
          int i=0;
          for (i=0;i<MONITOR_LOG_USERNAME_LIST.size();i++) {
              MonitorLoggers logger = new MonitorLoggers(XMPP_SERVER, XMPP_SERVER_PORT,
            		  MONITOR_LOG_USERNAME_LIST.get(i),pwdList.get(i),XMPP_USERNAME,
                                                  Integer.parseInt(cfg.getProperty("monitor_logger_buffer").value),
                                                  Integer.parseInt(cfg.getProperty("monitor_logger_delay").value));
              new Thread(logger).start();      
          }
        }
        catch (XMPPException e1)
        {
          Debug.logError(e1, module);
        }
    	
    	
        Runnable ofbizNode = new Runnable() {
            public void run() { serve(cfg); }
        };
        new Thread(ofbizNode).start();
        //update by zhongping.wang
//        XMPPChannel xc = XMPPChannel.getInstance();
//        xc.run();
        //end
        return true;

	}
    
	private void startVysper(String fileName) {
        String domain = XMPP_SERVER;
        
        String addedModuleProperty = System.getProperty("vysper.add.module");
        List<Module> listOfModules = null;
        if (addedModuleProperty != null) {
            String[] moduleClassNames = addedModuleProperty.split(",");
            listOfModules = createModuleInstances(moduleClassNames);
        }

        // choose the storage you want to use
        //StorageProviderRegistry providerRegistry = new JcrStorageProviderRegistry();
        StorageProviderRegistry providerRegistry = new MemoryStorageProviderRegistry();
        
        final Entity adminJID = EntityImpl.parseUnchecked("admin@" + domain);
        final Entity erlangNodeJID = EntityImpl.parseUnchecked(XMPP_USERNAME+ "@" + domain);
        final AccountManagement accountManagement = (AccountManagement) providerRegistry
                .retrieve(AccountManagement.class);
        final RosterManager rosterManager = (RosterManager) providerRegistry
                .retrieve(RosterManager.class);

        String initialPassword = System.getProperty("vysper.admin.initial.password", "siteview");
        if (!accountManagement.verifyAccountExists(adminJID)) {
            try {
				accountManagement.addUser(adminJID, initialPassword);
				accountManagement.addUser(erlangNodeJID, initialPassword);
				
				for (String user: MONITOR_LOG_USERNAME_LIST) {
					final Entity userJID = EntityImpl.parseUnchecked(user+ "@" + domain);
					accountManagement.addUser(userJID, initialPassword);
					rosterManager.addContact(erlangNodeJID, new RosterItem(userJID, SubscriptionType.BOTH));
					rosterManager.addContact(userJID, new RosterItem(erlangNodeJID, SubscriptionType.BOTH));
				}
				
//				for (String user: DEBUG_USERNAME_LIST) {
//					accountManagement.addUser(EntityImpl.parseUnchecked(user+ "@" + domain), initialPassword);
//				}
				
				for (String user: TOPOLOGY_USERNAME_LIST) {
					final Entity userJID = EntityImpl.parseUnchecked(user+ "@" + domain);
					accountManagement.addUser(userJID, initialPassword);
					rosterManager.addContact(erlangNodeJID, new RosterItem(userJID, SubscriptionType.BOTH));
					rosterManager.addContact(userJID, new RosterItem(erlangNodeJID, SubscriptionType.BOTH));
				}
			} catch (AccountCreationException e) {
				e.printStackTrace();
			} catch (RosterException e) {
				e.printStackTrace();
			}
        }

        XMPPServer xmppserver = new XMPPServer(domain);
        xmppserver.addEndpoint(new TCPEndpoint());
        //server.addEndpoint(new StanzaSessionFactory());
        xmppserver.setStorageProviderRegistry(providerRegistry);
        
        try {
        	xmppserver.setTLSCertificateInfo(FileUtil.getFile(fileName), "boguspw");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

        try {
        	xmppserver.start();
            System.out.println("vysper server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }

        xmppserver.addModule(new SoftwareVersionModule());
        xmppserver.addModule(new EntityTimeModule());
        xmppserver.addModule(new VcardTempModule());
        xmppserver.addModule(new XmppPingModule());
        xmppserver.addModule(new PrivateDataModule());
        xmppserver.addModule(new InBandRegistrationModule());
        xmppserver.addModule(new AdhocCommandsModule());
        final ServiceAdministrationModule serviceAdministrationModule = new ServiceAdministrationModule();
        // unless admin user account with a secure password is added, this will be not become effective
        serviceAdministrationModule.setAddAdminJIDs(Arrays.asList(adminJID)); 
        
        xmppserver.addModule(serviceAdministrationModule);

        if (listOfModules != null) {
            for (Module module : listOfModules) {
            	xmppserver.addModule(module);
            }
        }

			
		}

    private static List<Module> createModuleInstances(String[] moduleClassNames) {
        List<Module> modules = new ArrayList<Module>();

        for (String moduleClassName : moduleClassNames) {
            Class<Module> moduleClass;
            try {
                moduleClass = (Class<Module>) Class.forName(moduleClassName);
            } catch (ClassCastException e) {
                System.err.println("not a Vysper module class: " + moduleClassName);
                continue;
            } catch (ClassNotFoundException e) {
                System.err.println("could not load module class " + moduleClassName);
                continue;
            }
            try {
                Module module = moduleClass.newInstance();
                modules.add(module);
            } catch (Exception e) {
                System.err.println("failed to instantiate module class " + moduleClassName);
                continue;
            }
        }
        return modules;
    }

	public void serve(ContainerConfig.Container cfg) {
		String v = System.getProperties().getProperty("OtpConnection.trace");
		System.out.println("OtpConnection.trace=" + v);
		OtpNode server = null;
		
		
		java.io.File f=new java.io.File("longname");
		
		JAVA_NODE = cfg.getProperty("node").value;
		COOKIE =  cfg.getProperty("cookie").value;
		DEFAULT_MAIL_BOX =  cfg.getProperty("mailBox").value;
		Message xmppMsg = new Message();
		
		try {
			if(f.exists())
			{					
				server = new OtpNode(JAVA_NODE + "@" + ipaddr.getHostName(), COOKIE);
				Debug.logInfo("Erlang node server is :  " + JAVA_NODE + "@" + ipaddr.getHostName(),module);
			}else
			{
				server = new OtpNode(JAVA_NODE, COOKIE);
				Debug.logInfo("Erlang node server is :  " + JAVA_NODE,module);
			}
		}  catch (IOException e) {
			e.printStackTrace();
		}

		Debug.logInfo("Erlang node container ip is :  " + server.node(),module);

		OtpMbox serverMbox = server.createMbox(DEFAULT_MAIL_BOX);
		
		delegator = DelegatorFactory.getDelegator("default");

        // create the LocalDispatcher
        dispatcher = GenericDispatcher.getLocalDispatcher("ErlangDispatcher", delegator);

		String[] nodeNames = server.getNames(); // equivalent to nodes() in .
		for (int i = 0; i < nodeNames.length; i++) {
			System.out.println("name:" + nodeNames[i]);
		}
		
		String node_name = ERLANG_NODE+"@"+ipaddr.getHostName();

		int i=1;
		while (!server.ping(node_name, TIME_OUT*i)){
		  Debug.logInfo("Ping erlang node (" + node_name+ "): pang, wait for "+TIME_OUT*i/1000+ " seconds to retry." ,module);
		  i++; i=Math.min(i,50);
		}
		
		//adjust this number based on the number of monitors and frequency, and the number of CPU cores
		ExecutorService tpes = Executors.newFixedThreadPool(Integer.parseInt(cfg.getProperty("NUMBER_OF_THREADS").value));
//		OfbizService ofbizService = new OfbizService();

        OtpConverter.setAtomName(Arrays.asList("id", "class", "parent", "monitorId", "groupId", 
        		"enabled", "category", "action"));
        int indexLogger = 0, indexTopology = 0;
		while (true) {
			
	    	//reconnect if lost connection to the server  
			while (!connection.isConnected()) {
	            	try {
	            		xmppLogin(XMPP_USERNAME, XMPP_PASSWORD);
	                } catch (Exception ex) {
	                	Debug.logError(ex, module);            	            	
	                }
	        }
			
			String[] states = { DEFAULT_STATE_KEY, DEFAULT_STATE_VALUE };
			try {
				//OtpMsg msg = serverMbox.receiveMsg(RECEIVE_TIME_OUT);
				OtpMsg msg = serverMbox.receiveMsg();
//				Debug.logInfo("-------------------------the " + try_count + "th receive messages : ----- " + try_count++, module);
								
//				Debug.logInfo("raw message from erlang: " + msg.getMsg(),module);
				SvMessage message = new SvMessage(serverMbox);
				
				
//				Debug.logInfo(service.getParam("message").getName(),module);
				message.fromOtpMsg(msg);
				
//				indexLogger = (loggerChat.size()==1) ? 0: getNextLoggerIndex(indexLogger,loggerChat.size());
				indexLogger = (indexLogger == loggerChat.size()) ? 0: indexLogger;
				indexTopology = (indexTopology == topologyChat.size()) ? 0: indexTopology;
//				for (Chat chat:debugChat) {
//					chat.sendMessage(message.toString(message.getRequestType())); }

//				System.out.println("message:"+message.toString(message.getRequestType()));
				if ("LogMonitor".equalsIgnoreCase(message.getRequestType())){
					List<Object> list = FastList.newInstance();
					list.add(message.getAction()); 
					list.add(message.getOfbizParams());							
				    loggerChat.get(indexLogger).sendMessage(ObjectTransformation.OToS(list));
				    indexLogger++;
				}
				else if ("TopologyDispatch".equalsIgnoreCase(message.getRequestType())){
//					List<Object> list = FastList.newInstance();
//					list.add(message.getClass());
//					list.add(message.getAction());
//					list.add(message.getOfbizParams());							
//					topologyChat.get(indexTopology).sendMessage(ObjectTransformation.OToS(list));
//					XMPPChannel xmppcall=XMPPChannel.getInstance("erlangnode@windy");
//					OtpErlangList xmpptuple=message.getTuple();
//					List ssi=new ArrayList();
//					ssi=ErlangUtils.erlangList2arrayList((OtpErlangList) xmpptuple);
//					xmppcall.call("topologychart.device.DeviceParserData", "refreshDeviceData", ssi, 1000);
//					indexTopology++;
					List list = FastList.newInstance();
					list.add(message.getClassName());
					list.add(message.getOfbizParams()); 
//					list.add(message.getOfbizParams());
					topologyChat.get(indexTopology).sendMessage(ObjectTransformation.OToS(list));
					indexTopology++;
				}
				else if ("OfbizService".equalsIgnoreCase(message.getRequestType())) {
					OfbizService ofbizService = new OfbizService();
					ofbizService.setMessage(message);
					tpes.execute(ofbizService);
//					ofbizService;
				} 
				else if ("UpdateMonitor".equalsIgnoreCase(message.getRequestType())) 
				{ //for monitor call
					Class<?> cls = Class.forName(message.getMonitorClassName());
					BaseMonitor monitor = (BaseMonitor)cls.newInstance();
					monitor.setMessage(message);
					tpes.execute(monitor);
				} 
				else {
					Debug.logError("Unknown action type from erlang, allowed types are LogMonitor, OfbizService or UpdateMonitor.", module);
				};
			} catch (OtpErlangExit e) {
				// receiveMsg error
				states[0] = ERROR;
				states[1] = "erlang node receive message error: " + e.getMessage();
				e.printStackTrace();
			}
			catch (ClassNotFoundException e) {
				// monitor_type can not be loaded.
				states[0] = ERROR;
				states[1] = "monitor type can not be loaded. " + e.getMessage();
				e.printStackTrace();
				//list = ErlangUtils.erlangListAdd(list, ErlangUtils.createTuple(new OtpErlangAtom(states[0]), new OtpErlangString(states[1])));
			} catch (InstantiationException e) {
				states[0] = ERROR;
				states[1] = "instantiate the monitor update object error. " + e.getMessage();
				e.printStackTrace();
				//list = ErlangUtils.erlangListAdd(list, ErlangUtils.createTuple(new OtpErlangAtom(states[0]), new OtpErlangString(states[1])));
			} 
			catch (Exception e) {
				// other eroors
				states[0] = ERROR;
				states[1] = UNKOWN_REASONS + e.getMessage();
				e.printStackTrace();
			}

		}

	}
	
	//not used for now,  should gather chat statics to allocate the routing
	private static int getNextLoggerIndex (int index,int max){
	  index = (index == loggerChat.size()-1) ? 0: index;
	  
      //check for the presence status             
      Presence presence = roster.getPresence(loggerChat.get(index).getParticipant());
//      Presence presence = roster.getPresence("logger1");
      
      if (max==0) {
        Debug.logError("No logger is online, the log data will not be able to write into entity.", module);
        return -1;
      }
 
      Debug.logInfo("User = "+loggerChat.get(index).getParticipant()
                  +" presence.getType= "+ presence.getType()
                  +" presence.getStatus= "+ presence.getStatus()
                  +" index="+ index
                  +" max="+ max ,module);
      if (presence.getType() != Presence.Type.available) return getNextLoggerIndex(index++,max--);
      else return index;

	}

	public  void  stop() throws ContainerException {
	    connection.disconnect();
    }
	
	public static void main(String args[]) throws Exception {
		System.out.println("hello");

		testChat(args);
		System.exit(0);
	}
	
	public static void testChat(String args[]) throws XMPPException, IOException {
        // declare variables
        ErlangGateway c = new ErlangGateway();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String msg;

        // turn on the enhanced debugger
        XMPPConnection.DEBUG_ENABLED = true;

        // Enter your login information here
        c.xmppLogin("logger1", "siteview");

        c.displayBuddyList();


        System.out.println("Who do you want to talk to? - Type contacts full email address:");
        String talkTo = br.readLine();

        System.out.println("-----");
        System.out.println("All messages will be sent to " + talkTo);
        System.out.println("Enter your message in the console:");
        System.out.println("-----\n");

        while (!(msg = br.readLine()).equals("bye")) {
            c.sendMessage(msg, talkTo);
        }

        c.disconnect();
        System.exit(0);
    }
	
    public void sendMessage(String message, String to) throws XMPPException {
        Chat chat = connection.getChatManager().createChat(to, this);
        chat.sendMessage(message);
    }

    public void displayBuddyList() {
        Roster roster = connection.getRoster();
        Collection<RosterEntry> entries = roster.getEntries();

        for (RosterEntry r : entries) {
        }
    }
}