/*
interface to entity.
receive the monitor log from erlangnode,  reduce the large number insert operation into database
*/

package com.dragonflow.erlangecc.ofbiz;

import java.util.List;
import java.util.Map;

import javolution.util.FastList;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromContainsFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactory;
import org.ofbiz.service.GenericDispatcher;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;

import com.dragonflow.erlangecc.util.ObjectTransformation;


public class XMPPBufferredChannel implements Runnable  {
	protected String configFileLocation = null;
	public static final String module = XMPPBufferredChannel.class.getName();
	protected static Delegator delegator;
	protected static LocalDispatcher dispatcher;
	
	protected  String from,userName,pwd;
    protected  String server;
    protected int bufferSize,delay,port;
    
    private XMPPConnection connection;
    private Presence presence ;

    
    public XMPPBufferredChannel(String server, Integer port, String userName, String pwd, String from, int bufferSize, int delay ) throws XMPPException
    {
      this.from = from;
      this.userName= userName;
      this.pwd = pwd;
      this.server = server;
      this.port = port;
      this.bufferSize = bufferSize;
      this.delay = delay;
      ConnectionConfiguration config = new ConnectionConfiguration(server, port, server);
      connection = new XMPPConnection(config);
      delegator = DelegatorFactory.getDelegator("default");        
      // create the LocalDispatcher
      dispatcher = GenericDispatcher.getLocalDispatcher("ErlangDispatcher", delegator);

      connection.connect();
      SASLAuthentication.supportSASLMechanism("PLAIN", 0);
      Debug.logInfo("Monitor logger's userName/pwd="+userName+"@"+server+"/"+pwd, module);
      connection.login(userName, pwd);
      
   // Create a new presence. Pass in false to indicate we're unavailable.
      presence = new Presence(Presence.Type.available);
      presence.setStatus("logging data ...");
      connection.sendPacket(presence);
    }
    @Override
    public void run()
    {
      //Accept only messages from erlangnode
      PacketFilter filter = new AndFilter(new PacketTypeFilter(Message.class),new FromContainsFilter(from +"@"+server));

      //Collect these messages
      PacketCollector collector = connection.createPacketCollector(filter);
      List<Object> paramsList = FastList.newInstance();
      int i=0;
      Packet packet;
      Boolean commit = false;
      while (true) {
    	  
          //try to reconnect if lost connection
    	  while (!connection.isConnected()) {
          	try {
              	Thread.sleep(1000);
              	connection.connect();
                SASLAuthentication.supportSASLMechanism("PLAIN", 0);
                Debug.logInfo("Monitor logger's userName/pwd="+userName+"@"+server+"/"+pwd, module);
                connection.login(userName, pwd);
                
             // Create a new presence. Pass in false to indicate we're unavailable.
                presence = new Presence(Presence.Type.available);
                presence.setStatus("logging data ...");
                connection.sendPacket(presence);
              } catch (Exception ex) {
              	Debug.logInfo("Connect to XMPP server("+server+") failed, wait for 1 seconds to retry.", module);            	            	
              }
          }
    	  
    	  //timed buffer: wait for xx seconds or a number of messages to arrive
          packet = collector.nextResult(delay);//wait for the next result, blocking here or time out

          String func = null ;
          if (packet != null) {//data arrived
            if (packet instanceof Message) {
              List<Object> inList = (List<Object>)  ObjectTransformation.SToO( ((Message) packet).getBody());
              func = (String) inList.get(0);
              Map<String,Object> params = (Map<String,Object>) inList.get(1); 
              paramsList.add(params);
              i++;
              commit = (i==bufferSize);
            }
          } else //time out, no data for delayed seconds, go ahead to commit anyway
              commit = (paramsList.size()>0);
          
          //write to entity when timeout or reach buffer size
          if ((commit && (paramsList != null) && (func !=null))) {
              try
              {
                  dispatcher.runSync(func,UtilMisc.toMap("paramsList", paramsList));
                  paramsList.clear();
                  i=0;
                  commit=false;
              }
              catch (GenericServiceException e)
              {
                  Debug.logError(e, module);
              }        	  
          }
      }
    }

}