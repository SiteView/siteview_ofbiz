package com.dragonflow.erlangecc.ofbiz;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CreateEntityXmlService {

	/**
	 * 获取两段时间内的所有日期
	 * 
	 * @param Date
	 *            dBegin
	 * @param Date
	 *            dEnd
	 * @return List lDate
	 */
	public static List<Date> findDates(Date dBegin, Date dEnd) {
		List listDate = new ArrayList();
		listDate.add(dBegin);
		Calendar calBegin = Calendar.getInstance();
		// 使用给定的 Date 设置此 Calendar 的时间
		calBegin.setTime(dBegin);
		Calendar calEnd = Calendar.getInstance();
		// 使用给定的 Date 设置此 Calendar 的时间
		calEnd.setTime(dEnd);
		// 测试此日期是否在指定日期之后
		while (dEnd.after(calBegin.getTime())) {
			// 根据日历的规则，为给定的日历字段添加或减去指定的时间量
			calBegin.add(Calendar.DAY_OF_MONTH, 1);
			listDate.add(calBegin.getTime());
		}
		return listDate;
	}

	/**
	 * 自动创建ofbiz的Log数据库表生成配置文件logentitymodel.xml
	 * 
	 * @return boolean isok
	 */
	public static boolean createEntityModelXml() {
		boolean isok = false;
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		Calendar calendar = new GregorianCalendar(year, month, day);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd ");
		// String start = "2012-03-06";
		// String end = "2012-03-08";
		String start = format.format(calendar.getTime());
		System.out.println("数据库表开始日期(今天的日期)：" + start);
		calendar.add(GregorianCalendar.YEAR, 1);
		String end = format.format(calendar.getTime());
		System.out.println("数据库表结束日期(1年后日期)：" + end);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date dBegin = null;
		Date dEnd = null;
		try {
			dBegin = sdf.parse(start);
			dEnd = sdf.parse(end);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Date> lDate = findDates(dBegin, dEnd);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder db = factory.newDocumentBuilder();
			Document xmldoc = db.newDocument();
			xmldoc.setXmlVersion("1.0");
			Element root = null;
			factory.setIgnoringElementContentWhitespace(true);
			root = xmldoc.createElement("entitymodel");
			root.setAttribute("xmlns:xsi",
					"http://www.w3.org/2001/XMLSchema-instance");
			root.setAttribute("xsi:noNamespaceSchemaLocation",
					"http://ofbiz.apache.org/dtds/entitymodel.xsd");
//			List tablelist = new ArrayList();
			for (Date date : lDate) {
				Element theentity = null, theElem = null, therelation = null;
				String tableindex = sdf.format(date);
				tableindex = "_" + tableindex.replaceAll("-", "_");
//				tablelist.add("`operation_attribute_log" + tableindex + "`");
				// System.out.println("转换后:"+tableindex);
				try {

					// --- 新建entity开始 ----
					theentity = xmldoc.createElement("entity");
					theentity.setAttribute("entity-name",
							"OperationAttributeLog" + tableindex);
					theentity.setAttribute("package-name",
							"com.siteview.ecc.operation");
					theentity.setAttribute("default-resource-name",
							"EccEntityLabels");
					theentity.setAttribute("title",
							"Operation Attribute Time Series Log Entity");

					theElem = xmldoc.createElement("description");
					theElem.setTextContent("Used to store the info for each measurement, the actual value is stored in related OperationAttributeLogValues entity.");
					theentity.appendChild(theElem);

					theElem = xmldoc.createElement("field");
					theElem.setAttribute("name", "logId");
					theElem.setAttribute("type", "id");
					theentity.appendChild(theElem);

					theElem = xmldoc.createElement("field");
					theElem.setAttribute("name", "operationId");
					theElem.setAttribute("type", "id");
					theentity.appendChild(theElem);

					theElem = xmldoc.createElement("field");
					theElem.setAttribute("name", "name");
					theElem.setAttribute("type", "object");
					theentity.appendChild(theElem);

					theElem = xmldoc.createElement("field");
					theElem.setAttribute("name", "logTime");
					theElem.setAttribute("type", "date-time");
					theentity.appendChild(theElem);

					theElem = xmldoc.createElement("field");
					theElem.setAttribute("name", "category");
					theElem.setAttribute("type", "very-short");
					theentity.appendChild(theElem);

					theElem = xmldoc.createElement("field");
					theElem.setAttribute("name", "description");
					theElem.setAttribute("type", "object");
					theentity.appendChild(theElem);

					theElem = xmldoc.createElement("field");
					theElem.setAttribute("name", "measurement");
					theElem.setAttribute("type", "object");
					theentity.appendChild(theElem);

					theElem = xmldoc.createElement("prim-key");
					theElem.setAttribute("field", "logId");
					theentity.appendChild(theElem);

					theElem = xmldoc.createElement("key-map");
					theElem.setAttribute("field-name", "logId");
					therelation = xmldoc.createElement("relation");
					therelation.setAttribute("type", "many");
					therelation.setAttribute("fk-name", "MONITOR_LOG_VALUE");
					therelation.setAttribute("rel-entity-name",
							"OperationAttributeLogValues");

					therelation.appendChild(theElem);

					theentity.appendChild(therelation);

					root.appendChild(theentity);
					// output(xmldoc);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			xmldoc.appendChild(root);
			saveXml("newlogentitymodel.xml", xmldoc);
			System.out.println(".............logentitymodel.xml创建完成,共创建 "
					+ lDate.size() + " 个Entity.............>>>>>>>>>>>>");
			isok = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return isok;
	}

	/**
	 * 将Document输出到文件
	 * 
	 * @param fileName
	 * @param doc
	 */

	public static void saveXml(String fileName, Document doc) {
		TransformerFactory transFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = transFactory.newTransformer();
			transformer.setOutputProperty("indent", "yes");
			DOMSource source = new DOMSource();
			source.setNode(doc);
			StreamResult result = new StreamResult();
			File directory = new File(".");
			String filenamePath = "";
			try {
				filenamePath = directory.getCanonicalPath()
						+ "\\hot-deploy\\erlangnode\\entitydef\\" + fileName;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			result.setOutputStream(new FileOutputStream(filenamePath));

			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Main Test Method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		createEntityModelXml();
	}
}
