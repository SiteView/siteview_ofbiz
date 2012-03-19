/*
interface to a generic object.
receive the call from erlangnode and forward the call to client
*/

package com.dragonflow.erlangecc.ofbiz;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
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

import com.dragonflow.erlangecc.util.ObjectTransformation;
import com.ericsson.otp.erlang.OtpErlangList;


public class XMPPChannel implements Runnable, MessageListener  {
	
	protected static  String self;

	static String peer="topo1";
    
    private static  XMPPConnection connection;
    public Chat chat;
    public static   String XMPP_SERVER = "localhost";
    public   Integer XMPP_SERVER_PORT = 5222;
    private static  XMPPChannel x_instance = null;
    public static   String XMPP_USERNAME ="erlangnode";
	public static  String XMPP_PASSWORD ="siteview";
    synchronized public static XMPPChannel getInstance(String erlangnode2) {
		if (x_instance == null) {
			System.err.println("......new XMPPChannel intance.......");
			try {
			  x_instance = new XMPPChannel(erlangnode2);
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
		return x_instance;
	}
    
    public XMPPChannel(XMPPConnection conn, String self, String peer) throws XMPPException
    {
        this.connection = conn;
        this.self = self;
        this.peer = peer;
        this.chat = connection.getChatManager().createChat(peer+"@"+XMPP_SERVER, this);
    }

        
    public XMPPChannel(String erlangnode2) throws XMPPException
    {
    	ConnectionConfiguration config = new ConnectionConfiguration(XMPP_SERVER, XMPP_SERVER_PORT, XMPP_SERVER);
		connection = new XMPPConnection(config);
		connection.connect();
		System.out.println("XMPP SERVER OK...............");
		connection.login(XMPP_USERNAME, XMPP_PASSWORD);
		System.out.println("User Login OK...............");
		self = erlangnode2;
		peer = "topo1@"+XMPP_SERVER;
		this.chat = connection.getChatManager().createChat(peer, this);
    }
   
    public Object call(String className, String method, List otpErlangList,int timeout) {
    	List list = FastList.newInstance();
    	Object rtnObj = null;
    	list.add(className);
    	list.add(method);
    	list.add(otpErlangList);
    	try {
    		Message xmppmsg = new Message();
    		xmppmsg.setBody(ObjectTransformation.OToS(list));
    		xmppmsg.setFrom(self);
    		xmppmsg.setTo(peer);
    		System.out.println("Send Message to XML："+xmppmsg.toXML());
            chat.sendMessage(xmppmsg);
    	
	        PacketFilter filter = new AndFilter(new PacketTypeFilter(Message.class),new FromContainsFilter(peer));
	
	        //Collect these messages
	        PacketCollector collector = connection.createPacketCollector(filter);
	        Packet packet;
	        
			//wait for reply message
	        packet = collector.nextResult(timeout);
	        
	        if (packet != null) {//data arrived
	            if (packet instanceof Message) {
	            	System.out.println("Received Message to XML");
	              rtnObj = ObjectTransformation.SToO( ((Message) packet).getBody());
	            }
	        }
		} catch (XMPPException e) {
			e.printStackTrace();
		}
    	
    	return rtnObj;
    }
    
    public void castcall(String className, String method, Map<String,Object> Params) {
    	List<Object> list = FastList.newInstance();
    	list.add(className);
    	list.add(method);
    	list.add(Params);
    	try {
    		Message xmppmsg = new Message();
    		xmppmsg.setBody(ObjectTransformation.OToS(list));
    		xmppmsg.setFrom(self);
    		xmppmsg.setTo(peer);
            chat.sendMessage(xmppmsg);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
    	
    }

    @Override
    public void run()
    {
      //Accept only messages from the server
      PacketFilter filter = new AndFilter(new PacketTypeFilter(Message.class),new FromContainsFilter(peer));

      //Collect these messages
      PacketCollector collector = connection.createPacketCollector(filter);
      Packet packet;
      System.err.println("XMPPChannel is Running.............");
      while (true) {
    	  //try to reconnect if lost connection
    	  while (!connection.isConnected()) {
          	try {
              	Thread.sleep(1000);
              	connection.connect();
                SASLAuthentication.supportSASLMechanism("PLAIN", 0);
                connection.login(XMPP_USERNAME, XMPP_PASSWORD);
                
             // Create a new presence. Pass in false to indicate we're unavailable.
                Presence presence = new Presence(Presence.Type.available);
                presence.setStatus("logging data ...");
                connection.sendPacket(presence);
              } catch (Exception ex) {
            	  ex.printStackTrace();
              }
          }
    	  //wait for message
          packet = collector.nextResult();

          String methodName = null ;
          String className = null ;
          if (packet != null) {//data arrived
            if (packet instanceof Message) {
            	System.out.println(packet.toXML());
              List<Object> inList = (List<Object>) ObjectTransformation.SToO( ((Message) packet).getBody());
              System.err.println("inList: "+inList);
              className = (String) inList.get(0);
              methodName = (String) inList.get(1);
              Map<String,Object> params = (Map<String,Object>) inList.get(2);
              
              try {
  				//call a generic function locally, using RMI call, call ofbiz service, call webservice, or set a value
            	  Class cls = Class.forName(className);
            	  Method m = cls.getDeclaredMethod(methodName, Map.class);            	  
            	  Object obj = cls.newInstance();
            	  m.invoke(obj, params);
              }
              catch (Exception e) {
            	  e.printStackTrace();
              }
              }
          } 
      }
    }

	@Override
	public void processMessage(Chat chat, Message message) {
        if (message.getType() == Message.Type.chat) {
//            System.out.println(chat.getParticipant() + " says: " + message.getBody());
            try {
                chat.sendMessage(message.getBody() + " echo");
            } catch (XMPPException ex) {
          	  ex.printStackTrace();
            }
        }
		
	}
	
//	public static void main(String[] args) {
////		    ConnectionConfiguration config = new ConnectionConfiguration(XMPP_SERVER, XMPP_SERVER_PORT, XMPP_SERVER);
////		    XMPPConnection conn = new XMPPConnection(config);
//			XMPPChannel channel = XMPPChannel.getInstance(String erlangnode2);
//	        Roster roster= channel.connection.getRoster();
//	        Presence presence = new Presence(Presence.Type.available);
//	        presence = roster.getPresence(XMPP_USERNAME+"@"+XMPP_SERVER);
//	        System.out.println("self Type:"+presence.getType());
//	        Collection<RosterEntry> entries = roster.getEntries();
//	        for (RosterEntry entry : entries) {
//	            System.out.println("entry:"+entry);
//	            presence = roster.getPresence(entry.getName()+"@SITEVIEWWZP");
//		        System.out.println("entry Type:"+presence.getType());
//	        }
//	        String self = "debugger3@"+XMPP_SERVER+"/Smack";
//	        String peer = "logger3@"+XMPP_SERVER+"/Smack";
////	        XMPPChannel channel = new XMPPChannel(conn,self, peer); 
//	        
//	        channel.run();
//	        	        
////	        XMPPChannel channel = XMPPChannel.getInstance();
//
////			Map<String,Object> map = new HashMap();
////			map.put("1", "1");
////			map.put("2", "2");
////			map.put("3", "3");
////			channel.call("com.siteview.base.data.JDBC.TestXmppChannel", "test", map, 1000);
//
//	}

}