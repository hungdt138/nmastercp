/**
 * 
 */
package com.crm.provisioning.thread;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import com.crm.thread.DispatcherInstance;

/**
 * @author hungdt
 *
 */
public class SendMOToCPInstance extends DispatcherInstance {

	public SendMOToCPInstance() throws Exception {
		super();
	}
	
	public SendMOToCPThread getDispatcher()
	{
		return (SendMOToCPThread) dispatcher;
	}
	
	public String getUrl(ResultSet rsQueue) throws Exception {
		String url = "";
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
		cpUrl.append(dateFormat.format(rsQueue.getString("REQUESTDATE")));
		cpUrl.append("&productCode=");
		cpUrl.append(rsQueue.getString("productCode"));
		cpUrl.append("&cmdcode=");
		cpUrl.append(rsQueue.getString("cmdcode"));
		cpUrl.append("&msgbody=");
		cpUrl.append(rsQueue.getString("msgBody"));
		cpUrl.append("&opid=");
		cpUrl.append(rsQueue.getString("opid"));

		return url;
	}
	
	

}
