/*
interface to erlang.
1. call ofbiz service
2. invoking monitor
3. forward log data to xmpp server for logging into entity engine,  reduce the parallel insert operations into data base.

need start xmpp ejabberd server and add users as defined in ofbiz-containers.xml
user = erlangnode, monitorlogger, password = 3ren
  */

package com.dragonflow.erlangecc.ofbiz;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javolution.util.FastList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.ofbiz.base.container.Container;
import org.ofbiz.base.container.ContainerConfig;
import org.ofbiz.base.container.ContainerException;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.StringUtil;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactory;
import org.ofbiz.service.GenericDispatcher;
import org.ofbiz.service.LocalDispatcher;

import com.dragonflow.erlangecc.monitor.BaseMonitor;
import com.dragonflow.erlangecc.monitor.OfbizService;
import com.dragonflow.erlangecc.util.SvMessage;
import com.ericsson.otp.erlang.OtpConverter;
import com.ericsson.otp.erlang.OtpErlangExit;
import com.ericsson.otp.erlang.OtpMbox;
import com.ericsson.otp.erlang.OtpMsg;
import com.ericsson.otp.erlang.OtpNode;


public class ErlangNode implements Container, MessageListener ,Serializable {
	protected String configFileLocation = null;
	public static final String module = ErlangNode.class.getName();
    
	
	/**
     * @see org.ofbiz.base.container.Container#init(java.lang.String[], java.lang.String)
     */
    public void init(String[] args, String configFile) {
        this.configFileLocation = configFile;
        Debug.logInfo("erlangnode container init args: " + args, module);
    }

  
	    /**
     * @see org.ofbiz.base.container.Container#init(java.lang.String[], java.lang.String)
     */
    public boolean start() throws ContainerException {
    	
    	final ContainerConfig.Container cfg = ContainerConfig.getContainer("erlangnode-container", this.configFileLocation);
    	
    	String erlangRoot = cfg.getProperty("erlang_root").value;
    	String kernelRoot = cfg.getProperty("kernel_root").value;
    	String cmdline = "svecc";

    	Debug.logInfo("Starting erlang kernel: "+ kernelRoot +"\\"+ cmdline, module);
//    	startKernel(erlangRoot ,kernelRoot);
    	
	   	 String[] command = {"CMD", kernelRoot, cmdline};
	     ProcessBuilder probuilder = new ProcessBuilder( command );
	     //You can set up your work directory
	//     probuilder.directory(new File("c:\\xyzwsdemo"));
	     
	     try {
			Process process = probuilder.start();
			
	        InputStream is = process.getInputStream();
	        InputStreamReader isr = new InputStreamReader(is);
	        BufferedReader br = new BufferedReader(isr);
	        String line;

	        while ((line = br.readLine()) != null) {
	            System.out.println(line);
	        }

		} catch (IOException e) {
			e.printStackTrace();
		}
	     
	     return true;
	}

	public  void  stop() throws ContainerException {
	    
    }
	
	public static void main(String args[]) throws Exception {
		System.out.println("hello");
		
//		startKernel("d:\\src\\eccng\\trunk\\server2","svecc.bat");
		
		String kernel_root =  "d:\\src\\eccng\\trunk\\server2";
	
//		 String[] command = {"CMD", kernel_root, "svecc.bat"};
//	        ProcessBuilder probuilder = new ProcessBuilder( command );
//	        probuilder.directory(new File(kernel_root));
	        
	        String[] command = {"CMD", "/C", "svecc"};
	        ProcessBuilder probuilder = new ProcessBuilder( command );
	        probuilder.directory(new File(kernel_root));
	        Process process = probuilder.start();
	        
	        //Read out dir output
	        InputStream is = process.getInputStream();
	        InputStreamReader isr = new InputStreamReader(is);
	        BufferedReader br = new BufferedReader(isr);
	        String line;
	        System.out.printf("Output of running %s is:\n",
	                Arrays.toString(command));
	        while ((line = br.readLine()) != null) {
	            System.out.println(line);
	        }
	        
	        //Wait to get exit value
	        try {
	            int exitValue = process.waitFor();
	            System.out.println("\n\nExit Value is " + exitValue);
	        } catch (InterruptedException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
		System.exit(0);
	}


	@Override
	public void processMessage(Chat arg0, Message arg1) {
		// TODO Auto-generated method stub
		
	}
}