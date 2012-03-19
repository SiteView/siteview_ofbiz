package com.dragonflow.erlangecc.ofbiz;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

public class XmppNode {
	private static XmppInterface xmppIf = null;

	public static XmppInterface getOtpInterface() throws Exception {
		if (xmppIf == null) {
			xmppIf = (XmppInterface) (new XmppInterfaceImpl());
		}
		return xmppIf;
	}
	/**Main Test Method**/
	public static void main(String[] args) {
		try {
		XMPPChannel channel = (XMPPChannel) new XmppNode().getOtpInterface().call();
		Chat chat = channel.chat;
		Message msg = new Message();
		msg.setTo(chat.getParticipant());
		msg.setBody("Hi,Are you "+chat.getParticipant()+" ?");
		System.out.println(msg.toXML());
		chat.sendMessage(msg);
		Thread.sleep(1000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
