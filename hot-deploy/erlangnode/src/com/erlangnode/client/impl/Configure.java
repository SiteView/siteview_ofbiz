package com.erlangnode.client.impl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap.Builder;

public class Configure {
	private final static String CONFIG_FILENAME = "/config.properties";
	private static final Builder<String, Properties> propertiesBuilder = new Builder<String, Properties>();
	
	static
	{
		propertiesBuilder.maximumWeightedCapacity(2000);
	}
	
	private final static Map<String,Properties> mapcache = propertiesBuilder.build();
	

	
	public static String getConfigureValue(String key) throws Exception
	{
//		return (String)getConfigureProperties().get(key);
		
		if (key.equals("ErlangImplClassName"))
		{
			return "com.erlangnode.client.impl.ErlangIfImpl";
		}
		else if (key.equals("Connection_ClassName"))
		{
			return "com.erlangnode.client.impl.ErlangDirectEccServiceImpl";
		}
		else if (key.equals("NodeName"))
		{
			return "EccClient";
		}		
		else if (key.equals("NnmNodeName"))
		{
			return "NnmClient";
		}
		else if (key.equals("ReportNodeName"))
		{
			return "ReportClient";
		}
		else if (key.equals("PeerNode"))
		{
			return "debug@itsm";
		}
		else if (key.equals("PeerReportNode"))
		{
			return "reportproxy@itsm";
		}
		else if (key.equals("PeerNnmNode"))
		{
			return "nnm@itsm";
		}
		else if (key.equals("Cookie"))
		{
			return "3ren";
		}
		else
		{
			return "";
		}
		
	}
	public static int getConfigureIntValue(String key) throws Exception
	{
		String val = getConfigureValue(key);
		return Integer.parseInt(val);
	}

	public static Properties getProperties(String filename) throws Exception{
		Properties prop = mapcache.get(filename);
		if (prop == null){
			prop = new Properties();
			try{
//				String filenameWithPayh = EccStarter.getInstance().getWebDir() + (filename.startsWith("/") ? filename.substring(1) : filename);
				String filenameWithPayh = (filename.startsWith("/") ? filename.substring(1) : filename);
				InputStream in= new FileInputStream(filenameWithPayh);
				prop.load(in);
			}catch(Exception e){
				prop.load(Configure.class.getResourceAsStream(filename));
			}
			mapcache.put(filename, prop);
		}
		return prop;
	}
	public static String getConfigurePropertiesFilename()throws Exception{
		return CONFIG_FILENAME; 
	}
	private static Properties getConfigureProperties() throws Exception{
		return getProperties(getConfigurePropertiesFilename());
	}
	
	public static String getVersionTitle(){
		return "SiteView ECC";
	}
}
