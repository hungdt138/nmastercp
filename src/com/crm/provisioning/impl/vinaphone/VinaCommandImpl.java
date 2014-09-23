/**
 * ----------------------------------------------------------------- 
 * @ Copyright(c) 2013 Vietnamobile. JSC. All Rights Reserved.
 * ----------------------------------------------------------------- 
 * Date 	Author 		Version
 * ------------------------------------- 
 * Oct 6, 2013 hungdt  v1.0
 * -------------------------------------
 */
package com.crm.provisioning.impl.vinaphone;

import java.util.Date;

import com.crm.kernel.message.Constants;
import com.crm.product.cache.ProductEntry;
import com.crm.product.cache.ProductFactory;
import com.crm.provisioning.cache.CommandEntry;
import com.crm.provisioning.cache.ProvisioningCommand;
import com.crm.provisioning.cache.ProvisioningFactory;
import com.crm.provisioning.impl.CommandImpl;
import com.crm.provisioning.message.CommandMessage;
import com.crm.provisioning.thread.CommandInstance;
import com.crm.provisioning.util.CommandUtil;
import com.crm.subscriber.impl.SubscriberOrderImpl;
import com.crm.util.AppProperties;
import com.crm.util.GeneratorSeq;
import com.fss.util.AppException;

/**
 * @author hungdt
 * 
 */
public class VinaCommandImpl extends CommandImpl
{
	public CommandMessage sendSms(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception
	{
		VinaConnection connection = null;
		CommandMessage result = request;
		try
		{
			CommandEntry command = ProvisioningFactory.getCache().getCommand(request.getCommandId());

			ProductEntry product = ProductFactory.getCache().getProduct(request.getProductId());

			command.setMaxRetry(1);

			String serviceId = product.getParameters().getString("sdp.service.serviceid", "");
			String productId = product.getParameters().getString("sdp.service.productcode", "");

			AppProperties app = new AppProperties();
			app.setString("serviceid", serviceId);
			app.setString("productid", productId);
			result.setParameters(app);
			String spId = product.getParameter("sdp.service.spid", "");
			String descKey = "sdp.description." + result.getActionType();

			String description = product.getParameter(descKey, "");

			description = description.replace("<ALIAS>", product.getIndexKey());

			connection = (VinaConnection) instance.getProvisioningConnection();

			String isdn = CommandUtil.addCountryCode(request.getIsdn());

			// int sessionId = GeneratorSeq.getNextSeq();
			//
			// result.setSessionId(sessionId);
			// String requestStr = getRequestString(0, isdn, description, "",
			// serviceId, spId, productId, result.getMsgContent());

			// setRequest(instance, request, requestStr, sessionId);

			String identifier = connection.sendSms(instance, result);

			// SubscriberOrderImpl.updateDeliveryStatus(result.getOrderId(),
			// identifier, serviceId);

			result.setIdentifier(identifier);

			// String respStr = getResponseString("success", isdn, "SEND SMS",
			// identifier);
			//
			// setResponse(instance, request, respStr, sessionId);
		}
		catch (Exception e)
		{
			processError(instance, provisioningCommand, result, e);
		}

		finally
		{
			instance.closeProvisioningConnection(connection);
		}

		return result;
	}

	public CommandMessage sendWap(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception
	{
		VinaConnection connection = null;
		CommandMessage result = request;
		try
		{
			CommandEntry command = ProvisioningFactory.getCache().getCommand(request.getCommandId());

			ProductEntry product = ProductFactory.getCache().getProduct(request.getProductId());

			command.setMaxRetry(1);

			String serviceId = product.getParameters().getString("sdp.service.serviceid", "");
			String productId = product.getParameters().getString("sdp.service.productcode", "");

			AppProperties app = new AppProperties();
			app.setString("serviceid", serviceId);
			app.setString("productid", productId);
			result.setParameters(app);
			String spId = product.getParameter("sdp.service.spid", "");

			String descKey = "sdp.description." + result.getActionType();

			String description = product.getParameter(descKey, "");

			description = description.replace("<ALIAS>", product.getIndexKey());

			connection = (VinaConnection) instance.getProvisioningConnection();

			String isdn = CommandUtil.addCountryCode(request.getIsdn());

			// int sessionId = GeneratorSeq.getNextSeq();
			//
			// result.setSessionId(sessionId);
			//
			// String requestStr = getWapRequestString(0, isdn, description, "",
			// serviceId, spId, productId, result.getSubject(),
			// result.getUrl());

			// setRequest(instance, request, requestStr, sessionId);

			String identifier = connection.sendWap(instance, request);

			result.setIdentifier(identifier);

			// String respStr = getResponseString("success", isdn, "SEND WAP",
			// identifier);
			//
			// setResponse(instance, request, respStr, sessionId);

		}
		catch (Exception e)
		{
			processError(instance, provisioningCommand, result, e);
		}

		finally
		{
			instance.closeProvisioningConnection(connection);
		}

		return result;
	}

	public CommandMessage sendMT(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception
	{
		VinaConnection connection = null;
		CommandMessage result = request;
		try
		{
			String type = result.getParameters().getString("mttype", "sms");
			ProductEntry product = ProductFactory.getCache().getProduct(request.getProductId());

			String serviceId = product.getParameters().getString("sdp.service.serviceid", "").trim();
			String productId = product.getParameters().getString("sdp.service.productcode", "").trim();

			result.getParameters().setString("serviceid", serviceId);
			result.getParameters().setString("productid", productId);
			String spId = product.getParameter("sdp.service.spid", "").trim();
			String descKey = "sdp.description." + result.getActionType();

			String description = product.getParameter(descKey, "");

			description = description.replace("<ALIAS>", product.getIndexKey());

			connection = (VinaConnection) instance.getProvisioningConnection();

			if (type.equalsIgnoreCase("sms"))
			{
				String requestStr = "com.crm.provisioning.impl.vinaphone.sendMT SMS{spid= " + spId + ",sid= " + serviceId + ",productid= "
						+ productId + "" +
						",isdn= " + result.getIsdn() + ",description= " + description + ",content= " + result.getContent() + "}";

				long sessionId = setRequest(instance, result, requestStr);

				String identifier = connection.sendSms(instance, result);

				// SubscriberOrderImpl.updateDeliveryStatus(result.getOrderId(),
				// identifier, serviceId);

				result.setIdentifier(identifier);

				setResponse(instance, request, "identifier= " + identifier + ",status= success", sessionId);
			}
			else if (type.equalsIgnoreCase("wappush"))
			{
				String requestStr = "com.crm.provisioning.impl.mobifone.sendMT WAPPUSH{spid = " + spId + ",sid= " + serviceId + ",productid= "
						+ productId + "" +
						",isdn= " + result.getIsdn() + ",description= " + description + ",subject= " + result.getDeliveryWapTitle() + ",url= "
						+ result.getDeliveryWapHref() + "}";

				long sessionId = setRequest(instance, result, requestStr);
				String identifier = connection.sendWap(instance, request);

				// SubscriberOrderImpl.updateDeliveryStatus(result.getOrderId(),
				// identifier, serviceId);

				result.setIdentifier(identifier);

				setResponse(instance, request, "identifier= " + identifier + ",status= success", sessionId);
			}
			else
			{
				throw new AppException(Constants.ERROR_INVALID_DELIVER);
			}

		}
		catch (Exception e)
		{
			processError(instance, provisioningCommand, result, e);
		}

		finally
		{
			instance.closeProvisioningConnection(connection);
		}

		return result;
	}

}
