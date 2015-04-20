/**
 * 
 */
package com.crm.provisioning.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.crm.kernel.message.AlarmMessage;
import com.crm.kernel.message.Constants;
import com.crm.kernel.queue.QueueFactory;
import com.crm.kernel.sql.Database;
import com.crm.provisioning.message.CommandMessage;
import com.crm.provisioning.util.CommandUtil;
import com.crm.thread.DispatcherThread;
import com.crm.thread.util.ThreadUtil;
import com.crm.util.GeneratorSeq;
import com.crm.util.HttpRequest;
import com.fss.util.AppException;

/**
 * @author hungdt
 * 
 */
public class SendMOToCPThread extends DispatcherThread {

	public PreparedStatement stmtQueue = null;
	public PreparedStatement stmtQueueUpdate = null;
	public PreparedStatement stmtQueueUpdateRetry = null;
	public ResultSet rsQueue = null;
	private Connection _conn = null;

	public String selectSQL = "";
	public String updateSQL = "";
	public boolean expire = false;
	public long orderId = 0;
	public int timeRetry = 0;
	public int alarmError = 0;
	public int timeDelay = 0;

	private int errorCouter = 0;
	protected StringBuilder systemDump = null;

	// //////////////////////////////////////////////////////
	// Override
	// //////////////////////////////////////////////////////
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Vector getDispatcherDefinition() {
		Vector vtReturn = new Vector();

		vtReturn.addElement(ThreadUtil.createTextParameter("selectSQL", 4000,
				""));
		vtReturn.addElement(ThreadUtil.createTextParameter("updateSQL", 4000,
				""));

		vtReturn.addElement(ThreadUtil.createBooleanParameter("neverExpire",
				"Loop or not"));
		vtReturn.addElement(ThreadUtil.createIntegerParameter("timeRetry",
				"Time retry (Minutes)"));
		vtReturn.addElement(ThreadUtil.createIntegerParameter("alarmError", ""));

		vtReturn.addElement(ThreadUtil.createIntegerParameter("timeDelay",
				"Time delay vinaphone (s)"));

		vtReturn.addAll(ThreadUtil.createInstanceParameter(this));

		vtReturn.addAll(ThreadUtil.createLogParameter(this));
		// vtReturn.addALl(super.getDispatcherDefinition());
		// super.getDispatcherDefinition();
		return vtReturn;
	}

	// //////////////////////////////////////////////////////
	// Override
	// //////////////////////////////////////////////////////
	public void fillDispatcherParameter() throws AppException {
		try {
			super.fillDispatcherParameter();

			selectSQL = ThreadUtil.getString(this, "selectSQL", true, "");
			updateSQL = ThreadUtil.getString(this, "updateSQL", true, "");

			expire = ThreadUtil.getBoolean(this, "neverExpire", false);
			timeRetry = ThreadUtil.getInt(this, "timeRetry", 5);
			alarmError = ThreadUtil.getInt(this, "alarmError", 20);
			timeDelay = ThreadUtil.getInt(this, "timeDelay", 15);
		} catch (AppException e) {
			logMonitor(e);

			throw e;
		} catch (Exception e) {
			logMonitor(e);

			throw new AppException(e.getMessage());
		} finally {
		}
	}

	public void beforeProcessSession() throws Exception {
		super.beforeProcessSession();

		try {
			_conn = Database.getConnection();
			stmtQueue = _conn.prepareStatement(selectSQL);
			stmtQueueUpdate = _conn.prepareStatement(updateSQL);

			String sql = "update CPQUEUE set retry = retry + 1, lastruntime = sysdate where orderId = ?";

			stmtQueueUpdateRetry = _conn.prepareStatement(sql);

		} catch (Exception e) {
			throw e;
		}
	}

	public void afterProcessSession() throws Exception {
		try {
			Database.closeObject(stmtQueue);
			Database.closeObject(stmtQueueUpdate);
			Database.closeObject(stmtQueueUpdateRetry);
			Database.closeObject(rsQueue);
			Database.closeObject(_conn);
		} catch (Exception e) {
			throw e;
		} finally {
			super.afterProcessSession();
		}
	}

	public String getUrl() throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		StringBuffer cpUrl = new StringBuffer();

		cpUrl.append(rsQueue.getString("CPURL"));
		cpUrl.append("?username=");
		cpUrl.append(rsQueue.getString("username"));
		cpUrl.append("&password=");
		cpUrl.append(rsQueue.getString("password"));
		cpUrl.append("&dest=");
		cpUrl.append(rsQueue.getString("serviceAddress"));
		cpUrl.append("&isdn=");
		cpUrl.append(rsQueue.getString("isdn"));
		cpUrl.append("&reqid=");
		cpUrl.append(rsQueue.getString("orderId"));
		cpUrl.append("&requestDate=");
		cpUrl.append(dateFormat.format(rsQueue.getTimestamp("REQUESTDATE")));
		cpUrl.append("&productCode=");
		cpUrl.append(rsQueue.getString("productCode"));
		cpUrl.append("&cmdcode=");
		cpUrl.append(rsQueue.getString("cmdcode"));
		cpUrl.append("&msgbody=");
		cpUrl.append(rsQueue.getString("msgBody"));
		cpUrl.append("&opid=");
		cpUrl.append(rsQueue.getString("opid"));

		if (rsQueue.getString("opid").equals("2")) {
			Thread.sleep(timeDelay);
		}

		return cpUrl.toString();
	}

	public void doProcessSession() throws Exception {
		try {
			rsQueue = stmtQueue.executeQuery();

			while (rsQueue.next() && isAvailable()) {
				String url = getUrl();
				if (!"".equals(url)) {
					long sessionId = GeneratorSeq.getNextSeq();
					orderId = rsQueue.getLong("orderId");
					int retry = rsQueue.getInt("retry");
					int status = 1;
					debugMonitor("SEND: ID = " + sessionId + ", " + url);
					Date startTime = Calendar.getInstance().getTime();
					String response = HttpRequest.callURL(url);
					// String response = "error";
					Date endTime = Calendar.getInstance().getTime();
					if (response.equalsIgnoreCase("200")) {
						debugMonitor("RECEIVER: ID = "
								+ sessionId
								+ ", STATUS = "
								+ response
								+ ", "
								+ "costTime = "
								+ CommandUtil.calculateCostTime(startTime,
										endTime));

						status = 0;
					} else if (response.equalsIgnoreCase("202")) {
						debugMonitor("RECEIVER: ID = "
								+ sessionId
								+ ", STATUS = "
								+ response
								+ ", "
								+ "costTime = "
								+ CommandUtil.calculateCostTime(startTime,
										endTime));
						status = 0;
					} else {
						debugMonitor("RECEIVER: ID = "
								+ sessionId
								+ ", STATUS = "
								+ response
								+ ", "
								+ "costTime = "
								+ CommandUtil.calculateCostTime(startTime,
										endTime));
						status = 1;
						retry = retry + 1;
						// errorCouter += 1;
						throw new AppException("not-success");
					}

					stmtQueueUpdate.setInt(1, status);
					stmtQueueUpdate.setInt(2, retry);
					stmtQueueUpdate.setLong(3, orderId);

					stmtQueueUpdate.execute();

				}

			}

		} catch (Exception e) {
			debugMonitor(e.toString());
		} finally {

			orderId = 0;
			_conn.commit();
			Database.closeObject(rsQueue);
		}
	}

}
