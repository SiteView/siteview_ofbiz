package com.dragonflow.erlangecc.ofbiz;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;
import javolution.util.FastList;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.ofbiz.base.container.Container;
import org.ofbiz.base.container.ContainerConfig;
import org.ofbiz.base.container.ContainerException;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.StringUtil;

public class XmppPoolFactory extends BasePoolableObjectFactory implements
		Container, MessageListener {
	public static final String module = XmppPoolFactory.class.getName();
	protected String configFileLocation = null;
	public static String XMPPRPC_USERNAMES;
	public static String XMPPRPC_USERNAME;
	public static String XMPPRPC_USER_PASSWORD;

	public static String XMPP_SERVER;
	public static Integer XMPP_SERVER_PORT = 5222;
	private static XMPPConnection connection;
	protected static List<Chat> rpcChat = FastList.newInstance();
	public static InetAddress ipaddr = null;

	public XmppPoolFactory() {
		ConnectionConfiguration config = new ConnectionConfiguration(
				XMPP_SERVER, XMPP_SERVER_PORT, XMPP_SERVER);
		connection = new XMPPConnection(config);
		try {
			connection.connect();
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public synchronized Object makeObject() throws Exception {
		int i = 0;
		while (!connection.isConnected()) {
			try {
				Thread.sleep(i * 2000);
				connection.connect();
			} catch (Exception ex) {
				i++;
				i = Math.min(i, 20);
				Debug.log("Connect to XMPP server(" + XMPP_SERVER
						+ ") failed, wait for " + i + " seconds to retry.");
			}
		}
		String username = getNextSelfNodeName();// Get a free user
		XMPPChannel channel = new XMPPChannel(connection, username, XMPPRPC_USERNAME);
		connection.login(username, XMPPRPC_USER_PASSWORD);
		return channel;
	}

	private String getNextSelfNodeName() throws Exception {
		final List<String> XmppRpcUserList = StringUtil.split(
				XMPPRPC_USERNAMES, ",");
		String freeusername = null;
		rpcChat.clear();
		Random r = new Random();
		if (XmppRpcUserList != null) {
			// out:for (int i = 0; i < XmppRpcUserList.size(); i++) {
			// rpcChat.add(connection.getChatManager().createChat(
			// XmppRpcUserList.get(i) + "@" + XMPP_SERVER + "/Smack",
			// this));
			// // Presence presence = roster.getPresence(XmppRpcUserList.get(i)
			// // + "@" + XMPP_SERVER);
			// // if (presence.getType() != Presence.Type.available){
			// // freeusername = XmppRpcUserList.get(i);
			// // break out;
			// // }
			// }
			/**
			 * 这里需要返回一个可用的用户，需要判断从池中返回用户是否占用
			 * 
			 */
			int index = r.nextInt(XmppRpcUserList.size());
			freeusername = XmppRpcUserList.get(index);
			// Roster roster = connection.getRoster();
			System.out.println("Send By User: " + freeusername);
		} else {
			Debug.logError("No rpc users configured in ofbiz-containers.xml!",
					module);
		}

		return freeusername;
	}

	@Override
	public void processMessage(Chat arg0, Message arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(String[] args, String configFile)
			throws ContainerException {
		// TODO Auto-generated method stub
		this.configFileLocation = configFile;
		try {
			ipaddr = InetAddress.getLocalHost();
			;
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public boolean start() throws ContainerException {
		// TODO Auto-generated method stub
		final ContainerConfig.Container cfg = ContainerConfig.getContainer(
				"xmpprpc-container", this.configFileLocation);
		XMPP_SERVER = cfg.getProperty("xmpp_server").value;
		if (XMPP_SERVER == null)
			XMPP_SERVER = ipaddr.getHostName();
		XMPPRPC_USERNAME = cfg.getProperty("xmpprpc_username").value;
		if (XMPPRPC_USERNAME == null)
			XMPPRPC_USERNAME = "rpcnode";
		XMPPRPC_USER_PASSWORD = cfg.getProperty("xmpprpc_password").value;
		if (XMPPRPC_USER_PASSWORD == null)
			XMPPRPC_USER_PASSWORD = "ofbiz";

		XMPPRPC_USERNAMES = cfg.getProperty("xmpprpc_usernames").value;

		return true;
	}

	@Override
	public void stop() throws ContainerException {
		// TODO Auto-generated method stub

	}
}
