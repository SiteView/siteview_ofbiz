package com.dragonflow.siteview.informix;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.commons.dbcp.BasicDataSource;

import com.dragonflow.siteview.websphere.util.JMXObject;

public class DbInformix {

	String dbUser = "";
	String dbUrl = "";
	String dbpass = "";
	String driverName = "";
	int connectionTimeout = 60;
	int queryTimeout = 60;
	BasicDataSource datasource = new BasicDataSource();
	Connection con = null;
	StringBuffer error = new StringBuffer();
	HashMap<String, String> countersMap = null;

	public DbInformix() {
	}

	public DbInformix(String Url, String User, String PassWord, String driverName) {
		this.dbUrl = Url;
		this.dbUser = User;
		this.dbpass = PassWord;
		this.driverName = driverName;
		countersMap = new HashMap<String, String>();
	}

	// ����
	public Connection getConnection() {
		datasource.setUrl(this.dbUrl);
		datasource.setDriverClassName(this.driverName);
		datasource.setUsername(this.dbUser);
		datasource.setPassword(this.dbpass);
		datasource.setMaxWait(this.connectionTimeout);
		datasource.setMaxActive(2);

		try {
			return datasource.getConnection();
		} catch (SQLException e) {
			this.error.append(e.getMessage());
			// TODO Auto-generated catch block
//			System.out.println(e.getMessage());
		}
		return null;

	}

	// ��д���ܼ��
	// reads �������� pagesread ��ҳ�� writesд������ pageswritten дҳ��
	public void getChunkIO() {
		con = getConnection();
		String strsql = "select * from syschkio";
		PreparedStatement statement = null;
		ResultSet results = null;
		long reads = 0;// ������
		long pagesread = 0;// ��ҳ��
		long writes = 0;// д����
		long pageswritten = 0;// дҳ��

		long Totalreads = 0;// ������
		long Totalpagesread = 0;// ��ҳ��
		long Totalwrites = 0;// д����
		long Totalpageswritten = 0;// дҳ��
		if (con == null) {
			error.append("database connection fail!");
		} else {
			try {
				statement = con.prepareStatement(strsql);
				statement.setQueryTimeout(this.queryTimeout);
				results = statement.executeQuery();
				// 1. set columns of the queried result.
				while (results.next()) {
					Totalreads += Integer.parseInt(results.getString("reads"));
					Totalpagesread += Integer.parseInt(results
							.getString("pagesread"));
					Totalwrites += Integer
							.parseInt(results.getString("writes"));
					Totalpageswritten += Integer.parseInt(results
							.getString("pageswritten"));

				}
				reads = Totalreads;// ������
				pagesread = Totalpagesread;// ��ҳ��
				writes = Totalwrites;// д����
				pageswritten = Totalpageswritten;// дҳ��

				//
				this.countersMap.put("reads", reads + "");
				this.countersMap.put("pagesread", pagesread + "");
				this.countersMap.put("writes", writes + "");
				this.countersMap.put("pageswritten", pageswritten + "");

				results.close();
				con.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				error.append(e.getMessage());
				e.printStackTrace();
			}
		}

	}

	// ����Ựͳ����Ϣ
	public void getSession() {
		con = getConnection();
		String strsql = "select * from syssessions";
		PreparedStatement statement = null;
		ResultSet results = null;
		// long sid;//�Ի���
		// String username = "";//�û���
		// long uid;//�û�unix��
		// long pid;//�û����̺�
		// String hostname;//������
		// String tty;//tty�˿�
		// long connected = 0;//�û�����ʱ��
		// String feprogram;//������
		// long pooladdr = 0;//ר�жԻ���ָ��
		//
		// long is_wlatch = 0;//��־1=YES��0=NO���ȴ�����
		// long is_wlock = 0;//��־1=YES��0=NO���ȴ���
		// long is_wbuff = 0;//��־1=YES��0=NO���ȴ�������
		// long is_wckpt = 0;//��־1=YES��0=NO���ȴ�У���
		// long is_wlogbuf = 0;//��־1=YES��0=NO���ȴ���־������
		// long is_wtrans = 0;//��־1=YES��0=NO���ȴ�����
		// long is_monitor = 0;//��־1=YES��0=NO�����ӽ���
		// long is_incrit = 0;//��־1=YES��0=NO���ڹؼ�����

		long state = 0;// ��־

		// ����ͳ��
		int nPerNormal = 0;// ͨ����
		int nPerWait = 0;// �ȴ���

		int nconnect = 0;// ������
		int nwlatch = 0;// �ȴ�������
		int nwlock = 0;// �ȴ�����
		int nwbuff = 0;// �ȴ���������
		int nwckpt = 0;// �ȴ�У�����
		int nwlogbuf = 0;// �ȴ���־��������
		int nwtrans = 0;// �ȴ�������
		int nmonitor = 0;// ���ӽ�����
		int nincrit = 0;// �ڹؼ�������
		if (con == null) {
			error.append("database connection fail!");
		} else {
			try {
				statement = con.prepareStatement(strsql);
				statement.setQueryTimeout(this.queryTimeout);
				results = statement.executeQuery();
				// 1. set columns of the queried result.
				while (results.next()) {
					// ͳ����
					// �ȴ�������
					if (results.getString("is_wlatch").equals("1"))
						nwlatch++;
					// �ȴ�����
					if (results.getString("is_wlock").equals("1"))
						nwlock++;
					// �ȴ�������
					if (results.getString("is_wbuff").equals("1"))
						nwbuff++;
					// �ȴ�У���
					if (results.getString("is_wckpt").equals("1"))
						nwckpt++;
					// �ȴ���־������
					if (results.getString("is_wlogbuf").equals("1"))
						nwlogbuf++;
					// �ȴ�����
					if (results.getString("is_wtrans").equals("1"))
						nwtrans++;
					// ���ӽ���
					if (results.getString("is_monitor").equals("1"))
						nmonitor++;
					// �ڹؼ�����
					if (results.getString("is_incrit").equals("1"))
						nincrit++;

					// ������
					nconnect++;

				}
				// ����ͨ���ʺ͵ȴ���
				if (nconnect > 0) {
					// �ȴ���
					nPerWait = (nwlatch + nwlock + nwbuff + nwckpt + nwlogbuf + nwtrans);
					nPerWait = (nPerWait * 100) / nconnect;

					// ͨ����
					nPerNormal = 100 - nPerWait;
				} else {
					nPerWait = 0;
					nPerNormal = 100;
				}

				//
				this.countersMap.put("nwlatch", nwlatch + "");
				this.countersMap.put("nwlock", nwlock + "");
				this.countersMap.put("nwbuff", nwbuff + "");
				this.countersMap.put("nwckpt", nwckpt + "");
				this.countersMap.put("nwlogbuf", nwlogbuf + "");
				this.countersMap.put("nwtrans", nwtrans + "");
				this.countersMap.put("nmonitor", nmonitor + "");
				this.countersMap.put("nincrit", nincrit + "");
				this.countersMap.put("nconnect", nconnect + "");
				this.countersMap.put("nPerWait", nPerWait + "");
				this.countersMap.put("nPerNormal", nPerNormal + "");

				results.close();
				con.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				error.append(e.getMessage());
				e.printStackTrace();
			}
		}

	}

	// �����(�����)

	public void getLock() {
		con = getConnection();
		String strsql = "select * from syslocks";
		PreparedStatement statement = null;
		ResultSet results = null;
		// ������
		int nLock = 0;
		// ������
		int nDeadLock = 0;
		// ������
		int nPerDeadLock = 0;

		int B_Lock = 0;// ������������>byte lock(�ֽ���)
		int IS_Lock = 0;// ������������>intent shared lock(��ͼ������)
		int S_Lock = 0;// ������������>shared lock(������)
		int XS_Lock = 0;// ������������>repeatable read shared key(���ظ���������)

		int U_Lock = 0;// ������������>update lock(������)

		int IX_Lock = 0;// ������������>intent exclusive lock(��ͼ��ռ��)
		int SIX_Lock = 0;// ������������>shared intent exclusive(������ͼ��ռ��)
		int X_Lock = 0;// ������������>exclusive lock(��ռ��)
		int XR_Lock = 0;// ������������>repreatable read exclusive(���ظ�����ռ��)

		String dbsname = "";// ���ݿ�
		String tabname = "";// ����
		long rowidlk = 0;// �����ؼ��������к�
		long keynum = 0;// �����ؼ������Ĺؼ��ֺ�
		String type = "";// ������
		long owner = 0;// ��ӵ���߶Ի�ID
		String waiter = "";// ��һ���ȴ��߶Ի�ID
		if (con == null) {
			error.append("database connection fail!");
		} else {
			try {
				statement = con.prepareStatement(strsql);
				statement.setQueryTimeout(this.queryTimeout);
				results = statement.executeQuery();
				// 1. set columns of the queried result.
				while (results.next()) {
					nLock++;

					// ������
					waiter = results.getString("waiter");
					if (waiter != "")
						nDeadLock++;

					// ������
					type = results.getString("type");
					type.replace(" ", "");
					if (type == "B")
						B_Lock++;
					else if (type == "IS")
						IS_Lock++;
					else if (type == "S")
						S_Lock++;
					else if (type == "XS")
						XS_Lock++;

					// ������
					else if (type == "U")
						U_Lock++;

					// ��ռ��
					else if (type == "X")
						X_Lock++;
					else if (type == "IX")
						IX_Lock++;
					else if (type == "SIX")
						SIX_Lock++;
					else if (type == "XR")
						XR_Lock++;

				}

				// ���ֹ������Ͷ�ռ�����
				B_Lock = B_Lock + IS_Lock + S_Lock + XS_Lock;
				X_Lock = X_Lock + IX_Lock + SIX_Lock + XR_Lock;

				// ������
				if (nLock > 0)
					nPerDeadLock = (100 * nDeadLock) / nLock;
				//
				this.countersMap.put("nPerDeadLock", nPerDeadLock + "");
				this.countersMap.put("nLock", nLock + "");
				this.countersMap.put("B_Lock", B_Lock + "");
				this.countersMap.put("U_Lock", U_Lock + "");
				this.countersMap.put("X_Lock", X_Lock + "");
				this.countersMap.put("nDeadLock", nDeadLock + "");

				results.close();
				con.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				error.append(e.getMessage());
				e.printStackTrace();
			}
		}

	}

	// ������ȴ�

	public void GetWait() {
		con = getConnection();
		String strsql = "select * from sysseswts";
		PreparedStatement statement = null;
		ResultSet results = null;
		long sid = 0;// �Ի�ID
		String reason = "";// �Ի�ID
		long numwaits = 0;// �ȴ�ԭ��˵��
		long cumtime = 0;// ���ԭ��ĵȴ��ۼ�ʱ��
		long maxtime = 0;// ���ԭ��ĵȴ��ʱ��
		if (con == null) {
			error.append("database connection fail!");
		} else {
			try {
				statement = con.prepareStatement(strsql);
				statement.setQueryTimeout(this.queryTimeout);
				results = statement.executeQuery();
				// 1. set columns of the queried result.
				while (results.next()) {
					sid = Integer.parseInt(results.getString("sid"));
					reason = results.getString("reason");
					numwaits = Integer.parseInt(results.getString("numwaits"));
					cumtime = Integer.parseInt(results.getString("cumtime"));
					maxtime = Integer.parseInt(results.getString("maxtime"));

				}

				//
				this.countersMap.put("sid", sid + "");
				this.countersMap.put("reason", reason);
				this.countersMap.put("numwaits", numwaits + "");
				this.countersMap.put("cumtime", cumtime + "");
				this.countersMap.put("maxtime", maxtime + "");
				results.close();
				con.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				error.append(e.getMessage());
				e.printStackTrace();
			}
		}

	}

	// ���ݿ�ռ���

	public void GetSpace() {

		con = getConnection();
		String strsql = "select count(number), sum(used) from syslogs";
		// syschunks chunks���� chunks�ܴ�С chunksʣ���ܴ�
		// strSql.Format("select count(chknum),sum(chksize),sum(nfree) from syschunks;");
		// sysextents ���ݿ������ ҳ����
		// strSql.Format("select count(tabname),sum(size) from sysextents;");
		PreparedStatement statement = null;
		ResultSet results = null;
		long nPerDbspace = 0;
		long nLogFileSum = 0;
		long nLogFileSize = 0;
		long nChunks = 0;
		long nChunkTotalSize = 0;
		long nChunkFreeSize = 0;
		long nTabSum = 0;
		long nTabPageSum = 0;

		if (con == null) {
			error.append("database connection fail!");
		} else {
			try {
				statement = con.prepareStatement(strsql);
				statement.setQueryTimeout(this.queryTimeout);
				results = statement.executeQuery();
				// 1. set columns of the queried result.
				while (results.next()) {
					nLogFileSum = Integer.parseInt(results.getString(1));
					nLogFileSize = Integer.parseInt(results.getString(2));

				}
				strsql = "select count(chknum),sum(chksize),sum(nfree) from syschunks";

				statement = con.prepareStatement(strsql);
				statement.setQueryTimeout(this.queryTimeout);
				results = statement.executeQuery();
				while (results.next()) {
					nChunks = Integer.parseInt(results.getString(1));
					nChunkTotalSize = Integer.parseInt(results.getString(2));
					nChunkFreeSize = Integer.parseInt(results.getString(3));

				}
				strsql = "select count(tabname),sum(size) from sysextents";

				statement = con.prepareStatement(strsql);
				statement.setQueryTimeout(this.queryTimeout);
				results = statement.executeQuery();
				while (results.next()) {
					nTabSum = Integer.parseInt(results.getString(1));
					nTabPageSum = Integer.parseInt(results.getString(2));

				}
				if (nChunkTotalSize > 0) {
					nPerDbspace = (nChunkTotalSize - nChunkFreeSize) * 100;
					nPerDbspace = nPerDbspace / nChunkTotalSize;
				}

				this.countersMap.put("nPerDbspace", nPerDbspace + "");
				this.countersMap.put("nLogFileSum", nLogFileSum + "");
				this.countersMap.put("nLogFileSize", nLogFileSize + "");
				this.countersMap.put("nChunks", nChunks + "");
				this.countersMap.put("nChunkTotalSize", nChunkTotalSize + "");
				this.countersMap.put("nChunkFreeSize", nChunkFreeSize + "");
				this.countersMap.put("nTabSum", nTabSum + "");
				this.countersMap.put("nTabPageSum", nTabPageSum + "");
				results.close();
				con.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				error.append(e.getMessage());
				e.printStackTrace();
			}
		}

	}
	
	//��־��ϸ��Ϣ
	public void GetSysLog() {

		con = getConnection();
		String strsql = "select number,uniqid,is_used, is_current,is_backed_up,is_new,is_archived,is_temp from syslogs";

		PreparedStatement statement = null;
		ResultSet results = null;
		int number = 0;//��־�ļ���
		int uniqid = 0;//��־�ļ�ΨһID
		int is_used = 0;//����״̬
		int is_current = 0;//��ǰʹ��
		int is_backed_up = 0;//����״̬
		int is_new = 0;//�½�״̬
		int is_archived = 0;//�浵״̬
		int is_temp = 0;//��ʱ״̬

		if (con == null) {
			error.append("database connection fail!");
		} else {
			try {
				statement = con.prepareStatement(strsql);
				statement.setQueryTimeout(this.queryTimeout);
				results = statement.executeQuery();
				String n="";
				// 1. set columns of the queried result.
				while (results.next()) {
					number = Integer.parseInt(results.getString(1));
					uniqid = Integer.parseInt(results.getString(2));
					is_used=Integer.parseInt(results.getString(3));
					is_current=Integer.parseInt(results.getString(4));
					is_backed_up=Integer.parseInt(results.getString(5));
					is_new=Integer.parseInt(results.getString(6));
					is_archived=Integer.parseInt(results.getString(7));
					is_temp=Integer.parseInt(results.getString(8));
				    n=results.getString(1);
				    boolean tmp=false;
					this.countersMap.put("number", number + "");
					this.countersMap.put("uniqid", uniqid + "");
					tmp=is_used==1?true:false;
					this.countersMap.put("is_used of "+n,  tmp+ "");
					tmp=is_current==1?true:false;
					this.countersMap.put("is_current of "+n, tmp + "");
					tmp=is_backed_up==1?true:false;
					this.countersMap.put("is_backed_up of "+n, tmp + "");
					tmp=is_new==1?true:false;
					this.countersMap.put("is_new of "+n, tmp + "");
					tmp=is_archived==1?true:false;
					this.countersMap.put("is_archived of "+n, tmp + "");
					tmp=is_temp==1?true:false;
					this.countersMap.put("is_temp of "+n, tmp + "");
				}
				results.close();
				con.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				error.append(e.getMessage());
				e.printStackTrace();
			}
		}

	}
	
	//��ռ���ϸ��Ϣ
	public void GetDBSpace() {

		con = getConnection();
		//select name[1,8] dbspace,sum(chksize) Pages_size,sum(chksize) - sum(nfree) Pages_used,sum(nfree) Pages_free, round ((sum(nfree)) / (sum(chksize)) * 100, 2) percent_free from sysdbspaces d, syschunks c where d.dbsnum = c.dbsnum group by 1 order by 1;
		String strsql = "select d.name dbspace,sum(c.chksize) Pages_size,sum(c.chksize) - sum(c.nfree) Pages_used,sum(c.nfree) Pages_free, round ((sum(c.nfree)) / (sum(c.chksize)) * 100, 2) percent_free from sysdbspaces d, syschunks c where d.dbsnum = c.dbsnum group by name order by name;";
		//String strsql = "select name[1,8] dbspace,sum(chksize) Pages_size,sum(chksize) - sum(nfree) Pages_used,sum(nfree) Pages_free, round ((sum(nfree)) / (sum(chksize)) * 100, 2) percent_free from sysdbspaces d, syschunks c where d.dbsnum = c.dbsnum group by 1 order by 1;";
		PreparedStatement statement = null;
		ResultSet results = null;
		String dbspace="";//��ռ�����
		String Pages_size = "";//ʵ�ʴ�С
		String Pages_used=""; //���ô�С
		String Pages_free = "";//ʣ���С
		String percent_free = "";//ʣ��ٷ���
		

		if (con == null) {
			error.append("database connection fail!");
		} else {
			try {
				statement = con.prepareStatement(strsql);
				statement.setQueryTimeout(this.queryTimeout);
				results = statement.executeQuery();
				String n="";
				// 1. set columns of the queried result.
				while (results.next()) {
					dbspace = results.getString(1).trim();
					Pages_size = results.getString(2);
					Pages_used=results.getString(3);
					Pages_free=results.getString(4);
					percent_free=results.getString(5);
				   
					this.countersMap.put("dbspace",dbspace);
					this.countersMap.put("Pages_size of "+dbspace,Pages_size);
					this.countersMap.put("Pages_used of "+dbspace,Pages_used);
					this.countersMap.put("Pages_free of "+dbspace,Pages_free);
					this.countersMap.put("percent_free of "+dbspace,percent_free);
					
				}
				results.close();
				con.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				try {
					con.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				error.append(e.getMessage());
				e.printStackTrace();
			}
		}

	}
 
	public HashMap<String, String> getUpdate() {
		this.countersMap.clear();
		
		
			this.getChunkIO();
			if (this.error.length()==0)
			{
			this.getLock();
			this.getSession();
			this.GetSpace();
			this.GetWait();
			this.GetSysLog();
			this.GetDBSpace();
			}
		
		if (this.error.length()>0)
		{
//			System.out.println(this.error.toString());
			this.countersMap.put("error", error.toString());
		}
		return this.countersMap;
	}

	// reads �������� pagesread ��ҳ�� writesд������ pageswritten дҳ��
	//
	public StringBuffer xmlBrowseData(String serverName) {
		StringBuffer xml = new StringBuffer();
		xml.append("<browse_data>");
		xml.append("<object name=\"").append(serverName).append("\"");
		xml.append(" id=\"").append("0").append("\"");
		xml.append(">");

		xml.append("<object name=\"").append("Informix��д����").append("\"");
		xml.append(" id=\"").append("1").append("\"");
		xml.append(">");
		// counter

		xml.append("<counter name=\"").append("��������").append("\"");
		xml.append(" id=\"").append("reads").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("��ҳ��").append("\"");
		xml.append(" id=\"").append("pagesread").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("д������").append("\"");
		xml.append(" id=\"").append("writes").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("дҳ��").append("\"");
		xml.append(" id=\"").append("pageswritten").append("\"");
		xml.append(">");
		xml.append("</counter>");
		//
		xml.append("</object>");

		xml.append("<object name=\"").append("Informix�������").append("\"");
		xml.append(" id=\"").append("2").append("\"");
		xml.append(">");
		// counter

		xml.append("<counter name=\"").append("ͨ����").append("\"");
		xml.append(" id=\"").append("nPerNormal").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("�ȴ���").append("\"");
		xml.append(" id=\"").append("nPerWait").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("������").append("\"");
		xml.append(" id=\"").append("nconnect").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("�ȴ�������").append("\"");
		xml.append(" id=\"").append("nwlatch").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("�ȴ�����").append("\"");
		xml.append(" id=\"").append("nwlock").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("�ȴ���������").append("\"");
		xml.append(" id=\"").append("nwbuff").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("�ȴ�У�����").append("\"");
		xml.append(" id=\"").append("nwckpt").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("�ȴ���־��������").append("\"");
		xml.append(" id=\"").append("nwlogbuf").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("�ȴ�������").append("\"");
		xml.append(" id=\"").append("nwtrans").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("���ӽ�����").append("\"");
		xml.append(" id=\"").append("nmonitor").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("�ڹؼ�������").append("\"");
		xml.append(" id=\"").append("nincrit").append("\"");
		xml.append(">");
		xml.append("</counter>");

		//
		xml.append("</object>");

		xml.append("<object name=\"").append("Informix�����").append("\"");
		xml.append(" id=\"").append("3").append("\"");
		xml.append(">");
		// counter
		xml.append("<counter name=\"").append("������").append("\"");
		xml.append(" id=\"").append("nLock").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("������").append("\"");
		xml.append(" id=\"").append("nDeadLock").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("������").append("\"");
		xml.append(" id=\"").append("nPerDeadLock").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("byte lock(�ֽ���)").append("\"");
		xml.append(" id=\"").append("B_Lock").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("update lock(������)").append("\"");
		xml.append(" id=\"").append("U_Lock").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("exclusive lock(��ռ��)").append(
				"\"");
		xml.append(" id=\"").append("X_Lock").append("\"");
		xml.append(">");
		xml.append("</counter>");

		//
		xml.append("</object>");

		xml.append("<object name=\"").append("Informix������ȴ�").append("\"");
		xml.append(" id=\"").append("4").append("\"");
		xml.append(">");
		// counter

		xml.append("<counter name=\"").append("�ỰID").append("\"");
		xml.append(" id=\"").append("sid").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("ԭ��").append("\"");
		xml.append(" id=\"").append("reason").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("�ȴ�������").append("\"");
		xml.append(" id=\"").append("numwaits").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("ԭ��ĵȴ��ۼ�ʱ��").append("\"");
		xml.append(" id=\"").append("cumtime").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("ԭ��ĵȴ��ʱ��").append("\"");
		xml.append(" id=\"").append("maxtime").append("\"");
		xml.append(">");
		xml.append("</counter>");
		//
		xml.append("</object>");

		xml.append("<object name=\"").append("Informix���ݿ�ռ�").append("\"");
		xml.append(" id=\"").append("5").append("\"");
		xml.append(">");
		// counter

		xml.append("<counter name=\"").append("�߼���־��ռ�ÿռ�").append("\"");
		xml.append(" id=\"").append("nPerDbspace").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("�ļ�����").append("\"");
		xml.append(" id=\"").append("nLogFileSum").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("�ļ���С").append("\"");
		xml.append(" id=\"").append("nLogFileSize").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("chunks����").append("\"");
		xml.append(" id=\"").append("nChunks").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("chunks�ܴ�С").append("\"");
		xml.append(" id=\"").append("nChunkTotalSize").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("���ݿ������").append("\"");
		xml.append(" id=\"").append("nTabSum").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("ҳ����").append("\"");
		xml.append(" id=\"").append("nTabPageSum").append("\"");
		xml.append(">");
		xml.append("</counter>");

		xml.append("<counter name=\"").append("chunksʣ���ܴ�С").append("\"");
		xml.append(" id=\"").append("nChunkFreeSize").append("\"");
		xml.append(">");
		xml.append("</counter>");
		//
		xml.append("</object>");
		
		//============��־��ϸ��Ϣ=========
		xml.append("<object name=\"").append("Informix��־��ϸ��Ϣ").append("\"");
		xml.append(" id=\"").append("6").append("\"");
		xml.append(">");
		// counter
		con = getConnection();
		String strsql = "select number,uniqid,is_used, is_current,is_backed_up,is_new,is_archived,is_temp from syslogs";
		PreparedStatement statement = null;
		ResultSet results = null;
		int number = 0;//��־�ļ���
		int uniqid = 0;//��־�ļ�ΨһID
		int is_used = 0;//����״̬
		int is_current = 0;//��ǰʹ��
		int is_backed_up = 0;//����״̬
		int is_new = 0;//�½�״̬
		int is_archived = 0;//�浵״̬
		int is_temp = 0;//��ʱ״̬

		if (con == null) {
			error.append("database connection fail!");
		} else {
			try {
				statement = con.prepareStatement(strsql);
				statement.setQueryTimeout(this.queryTimeout);
				results = statement.executeQuery();
				String n="";
				// 1. set columns of the queried result.
				while (results.next()) {
					number = Integer.parseInt(results.getString(1));
					uniqid = Integer.parseInt(results.getString(2));
					is_used=Integer.parseInt(results.getString(3));
					is_current=Integer.parseInt(results.getString(4));
					is_backed_up=Integer.parseInt(results.getString(5));
					is_new=Integer.parseInt(results.getString(6));
					is_archived=Integer.parseInt(results.getString(7));
					is_temp=Integer.parseInt(results.getString(8));
				    n=results.getString(1);
				    xml.append("<object name=\"").append(number+"").append("\"");
					xml.append(" id=\"").append(number+""+uniqid).append("\"");
					xml.append(">");
					xml.append("<counter name=\"").append("����״̬").append("\"");
					xml.append(" id=\"").append("is_used of "+n).append("\"");
					xml.append(">");
					xml.append("</counter>");
					xml.append("<counter name=\"").append("��ǰʹ��").append("\"");
					xml.append(" id=\"").append("is_current of "+n).append("\"");
					xml.append(">");
					xml.append("</counter>");
					xml.append("<counter name=\"").append("����״̬").append("\"");
					xml.append(" id=\"").append("is_backed_up of "+n).append("\"");
					xml.append(">");
					xml.append("</counter>");
					xml.append("<counter name=\"").append("�½�״̬").append("\"");
					xml.append(" id=\"").append("is_new of "+n).append("\"");
					xml.append(">");
					xml.append("</counter>");
					xml.append("<counter name=\"").append("�浵״̬").append("\"");
					xml.append(" id=\"").append("is_archived of "+n).append("\"");
					xml.append(">");
					xml.append("</counter>");
					xml.append("<counter name=\"").append("��ʱ״̬").append("\"");
					xml.append(" id=\"").append("is_temp of "+n).append("\"");
					xml.append(">");
					xml.append("</counter>");
					xml.append("</object>");
				}
				results.close();
				con.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				error.append(e.getMessage());
				e.printStackTrace();
			}
		}


		//
		xml.append("</object>");
		//============��־��ϸ��Ϣ=========

		//============��ռ���Ϣ================
		xml.append("<object name=\"").append("Informix��ռ���Ϣ").append("\"");
		xml.append(" id=\"").append("7").append("\"");
		xml.append(">");
		// counter
		con = getConnection();
		//select name[1,8] dbspace,sum(chksize) Pages_size,sum(chksize) - sum(nfree) Pages_used,sum(nfree) Pages_free, round ((sum(nfree)) / (sum(chksize)) * 100, 2) percent_free from sysdbspaces d, syschunks c where d.dbsnum = c.dbsnum group by 1 order by 1;
		 strsql = "select d.name dbspace,sum(c.chksize) Pages_size,sum(c.chksize) - sum(c.nfree) Pages_used,sum(c.nfree) Pages_free, round ((sum(c.nfree)) / (sum(c.chksize)) * 100, 2) percent_free from sysdbspaces d, syschunks c where d.dbsnum = c.dbsnum group by name order by name;";

		 statement = null;
		 results = null;
		String dbspace="";//��ռ�����
		String Pages_size = "";//ʵ�ʴ�С
		String Pages_used=""; //���ô�С
		String Pages_free = "";//ʣ���С
		String percent_free = "";//ʣ��ٷ���
		

		if (con == null) {
			error.append("database connection fail!");
		} else {
			try {
				statement = con.prepareStatement(strsql);
				statement.setQueryTimeout(this.queryTimeout);
				results = statement.executeQuery();
				String n="";
				// 1. set columns of the queried result.
				while (results.next()) {
					dbspace = results.getString(1).trim();
					Pages_size = results.getString(2);
					Pages_used=results.getString(3);
					Pages_free=results.getString(4);
					percent_free=results.getString(5);
				   
					xml.append("<object name=\"").append(dbspace).append("\"");
					xml.append(" id=\"").append(dbspace).append("\"");
					xml.append(">");
					xml.append("<counter name=\"").append("ʵ�ʴ�С").append("\"");
					xml.append(" id=\"").append("Pages_size of "+dbspace).append("\"");
					xml.append(">");
					xml.append("</counter>");
					xml.append("<counter name=\"").append("���ô�С").append("\"");
					xml.append(" id=\"").append("Pages_used of "+dbspace).append("\"");
					xml.append(">");
					xml.append("</counter>");
					xml.append("<counter name=\"").append("ʣ���С").append("\"");
					xml.append(" id=\"").append("Pages_free of "+dbspace).append("\"");
					xml.append(">");
					xml.append("</counter>");
					xml.append("<counter name=\"").append("ʣ��ٷ���").append("\"");
					xml.append(" id=\"").append("percent_free of "+dbspace).append("\"");
					xml.append(">");
					xml.append("</counter>");
					xml.append("</object>");
					
				}
				results.close();
				con.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				try {
					con.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				error.append(e.getMessage());
				e.printStackTrace();
			}
		}



		//
		xml.append("</object>");
		//============��ռ���Ϣ=========
		xml.append("</object>");
		xml.append("</browse_data>");
		return xml;

	}

}
