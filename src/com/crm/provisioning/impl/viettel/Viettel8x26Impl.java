/**
 * 
 */
package com.crm.provisioning.impl.viettel;

import java.util.Date;

import com.crm.kernel.message.Constants;
import com.crm.provisioning.cache.ProvisioningCommand;
import com.crm.provisioning.impl.CommandImpl;
import com.crm.provisioning.message.CommandMessage;
import com.crm.provisioning.thread.CommandInstance;
import com.crm.subscriber.impl.SubscriberOrderImpl;
import com.crm.subscriber.impl.SubscriberProductImpl;
import com.fss.util.AppException;

/**
 * @author Hung
 * 
 */
public class Viettel8x26Impl extends CommandImpl
{
	public CommandMessage sendSMS(CommandInstance instance, ProvisioningCommand provisioningCommand, CommandMessage request) throws Exception
	{
		Viettel8x26Connection connection = null;
		try
		{
			connection = (Viettel8x26Connection) instance.getProvisioningConnection();
			
			String cmdCode = request.getParameters().getString("vt8x26.commandcode", "");
			int isCdr = request.getParameters().getInteger("vt8x26.iscdr", 0);
			
			instance.logMonitor("submit request (RequestId: " + request.getRequestId() + ") (MoId: " + 0 + ") (CommandCode: " + cmdCode + ") (SrcNum: "
					+ request.getServiceAddress() + ")" +
					" (DstNum: " + request.getIsdn() + ") (MsgContent: " + request.getNextQuestion() + ")");

			

			String resultStr = connection.sendSMS(0, request.getServiceAddress(), request.getIsdn(), cmdCode, "",
					1, request.getNextQuestion(), 1, 0, isCdr);

			instance.logMonitor("response received (MTResult: " + resultStr + ")");

			instance.debugMonitor(request.toLogString());

			SubscriberOrderImpl.updateMTCouter(request.getOrderId());

		}
		catch (Exception e)
		{
			instance.debugMonitor(e.toString());
			processError(instance, provisioningCommand, request, e);
		}
		finally
		{
			instance.closeProvisioningConnection(connection);
		}

		return request;
	}

}
