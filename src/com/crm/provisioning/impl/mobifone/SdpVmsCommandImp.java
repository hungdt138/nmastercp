package com.crm.provisioning.impl.mobifone;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import vms.org.csapi.www.wsdl.parlayx.sms.send.v2_2.service.SendSmsServiceStub.SendSmsE;
import vms.org.csapi.www.wsdl.parlayx.sms.send.v2_2.service.SendSmsServiceStub.SendSmsResponseE;
import vms.org.csapi.www.wsdl.parlayx.wap_push.send.v1_0.service.SendPushMessageServiceStub.SendPushMessageE;
import vms.org.csapi.www.wsdl.parlayx.wap_push.send.v1_0.service.SendPushMessageServiceStub.SendPushMessageResponseE;
import vms.cn.com.huawei.www.schema.common.v2_1.RequestSOAPHeaderE;

import com.crm.kernel.message.Constants;
import com.crm.product.cache.ProductEntry;
import com.crm.product.cache.ProductFactory;
import com.crm.provisioning.cache.CommandEntry;
import com.crm.provisioning.cache.ProvisioningCommand;
import com.crm.provisioning.cache.ProvisioningFactory;
import com.crm.provisioning.impl.CommandImpl;
import com.crm.provisioning.message.CommandMessage;
import com.crm.provisioning.thread.CommandInstance;
import com.crm.util.AppProperties;
import com.fss.util.AppException;

public class SdpVmsCommandImp extends CommandImpl {
	/**
	 * 
	 * hungdt
	 * 
	 * @param instance
	 * @param provisioningCommand
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public CommandMessage sendSms(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		SdpVmsConnection connection = null;
		CommandMessage result = request;

		try {
			CommandEntry command = ProvisioningFactory.getCache().getCommand(
					request.getCommandId());

			ProductEntry product = ProductFactory.getCache().getProduct(
					request.getProductId());

			command.setMaxRetry(1);

			String serviceId = product.getParameters().getString(
					"sdp.service.serviceid", "");
			String productId = product.getParameters().getString(
					"sdp.service.productcode", "");

			result.getParameters().setString("serviceid", serviceId);
			result.getParameters().setString("productid", productId);
			String spId = product.getParameter("sdp.service.spid", "");

			String descKey = "sdp.description." + result.getActionType();

			String description = product.getParameter(descKey, "");

			description = description.replace("<ALIAS>", product.getIndexKey());

			connection = (SdpVmsConnection) instance
					.getProvisioningConnection();

			RequestSOAPHeaderE header = connection.createHeader(result);

			SendSmsE body = connection.createBodySmS(result);

			String requestStr = "com.crm.provisioning.impl.mobifone.sendSms{spid= "
					+ spId
					+ ",sid= "
					+ serviceId
					+ ",productid= "
					+ productId
					+ ""
					+ ",isdn= "
					+ result.getIsdn()
					+ ",description= "
					+ description + ",content= " + result.getContent() + "}";

			long sessionId = setRequest(instance, result, requestStr);

			SendSmsResponseE req = connection.sendSMS(header, body);
			if (req == null)
				throw new AppException(Constants.ERROR_TIMEOUT);

			String identifier = req.getSendSmsResponse().getResult();

			result.setIdentifier(identifier);

			request.setResponseTime(new Date());

			setResponse(instance, request, "identifier= " + identifier
					+ "status= success", sessionId);

		} catch (Exception e) {
			processError(instance, provisioningCommand, result, e);
		}

		finally {
			instance.closeProvisioningConnection(connection);
		}

		return result;
	}

	// public VNMMessage getDeliverySms(CommandInstance instance,
	// ProvisioningCommand provisioningCommand, CommandMessage request)
	// throws Exception
	// {
	// SdpVmsConnection connection = null;
	// VNMMessage result = CommandUtil.createVNMMessage(request);
	// try
	// {
	// CommandEntry command = ProvisioningFactory.getCache()
	// .getCommand(request.getCommandId());
	//
	// ProductEntry product = ProductFactory.getCache()
	// .getProduct(request.getProductId());
	//
	// command.setMaxRetry(1);
	//
	// String serviceId =
	// product.getParameters().getString("sdp.service.serviceid", "");
	// String productId =
	// product.getParameters().getString("sdp.service.productcode", "");
	//
	// AppProperties app = new AppProperties();
	// app.setString("serviceid", serviceId);
	// app.setString("productid", productId);
	// result.setParameters(app);
	// String spId = product.getParameter("sdp.service.spid", "");
	//
	// String descKey = "sdp.description." + result.getActionType();
	//
	// String description = product.getParameter(descKey, "");
	//
	// description = description.replace("<ALIAS>", product.getIndexKey());
	//
	// connection = (SdpVmsConnection) instance.getProvisioningConnection();
	//
	// long sessionid = result.getSessionId();
	// RequestSOAPHeaderE header = connection.createHeader(result);
	//
	// GetSmsDeliveryStatusE bodyDelivery = connection.createBody(result
	// .getIdentifier());
	//
	// GetSmsDeliveryStatusResponseE resp = connection
	// .getDeliverySMS(header, bodyDelivery);
	//
	// DeliveryInformation[] d = resp.getGetSmsDeliveryStatusResponse()
	// .getResult();
	//
	// String deliveryS = "";
	//
	// if (d.length > 0)
	// {
	// for (DeliveryInformation a : d)
	// {
	// deliveryS = a.getDeliveryStatus().toString();
	// }
	// }
	//
	// String respStr = "ID=" + sessionid + ", " + "isdn=" + result
	// .getIsdn() + ", status=" + deliveryS;
	// instance.logMonitor("GetDelivery Status: " + respStr);
	// }
	// catch (Exception e)
	// {
	// processError(instance, provisioningCommand, result, e);
	// }
	//
	// finally
	// {
	// instance.closeProvisioningConnection(connection);
	// }
	// return result;
	// }

	/**
	 * 
	 * hungdt
	 * 
	 * @param instance
	 * @param provisioningCommand
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public CommandMessage sendWappush(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		SdpVmsConnection connection = null;
		CommandMessage result = request;
		try {
			ProductEntry product = ProductFactory.getCache().getProduct(
					request.getProductId());

			String serviceId = product.getParameters().getString(
					"sdp.service.serviceid", "");
			String productId = product.getParameters().getString(
					"sdp.service.productcode", "");

			AppProperties app = new AppProperties();
			app.setString("serviceid", serviceId);
			app.setString("productid", productId);
			result.setParameters(app);
			String spId = product.getParameter("sdp.service.spid", "");

			String descKey = "sdp.description." + result.getActionType();

			String description = product.getParameter(descKey, "");

			description = description.replace("<ALIAS>", product.getIndexKey());

			connection = (SdpVmsConnection) instance
					.getProvisioningConnection();

			RequestSOAPHeaderE header = connection.createHeader(result);

			SendPushMessageE body = connection.createBodyWap(result);

			String requestStr = "com.crm.provisioning.impl.mobifone.sendWappush{spid = "
					+ spId
					+ ",sid= "
					+ serviceId
					+ ",productid= "
					+ productId
					+ ""
					+ ",isdn= "
					+ result.getIsdn()
					+ ",description= "
					+ description
					+ ",subject= "
					+ result.getDeliveryWapTitle()
					+ ",url= " + result.getDeliveryWapHref() + "}";

			long sessionId = setRequest(instance, result, requestStr);

			SendPushMessageResponseE req = connection.sendWap(header, body);
			if (req == null)
				throw new AppException(Constants.ERROR_TIMEOUT);

			String identifier = req.getSendPushMessageResponse()
					.getRequestIdentifier();

			result.setIdentifier(identifier);

			request.setResponseTime(new Date());

			setResponse(instance, request, "identifier= " + identifier
					+ "status= success", sessionId);
		} catch (Exception e) {
			processError(instance, provisioningCommand, result, e);
		}

		finally {
			instance.closeProvisioningConnection(connection);
		}

		return result;
	}

	// public VNMMessage getDeliveryWap(CommandInstance instance,
	// ProvisioningCommand provisioningCommand, CommandMessage request)
	// throws Exception
	// {
	// SdpVmsConnection connection = null;
	// VNMMessage result = CommandUtil.createVNMMessage(request);
	// try
	// {
	// CommandEntry command = ProvisioningFactory.getCache()
	// .getCommand(request.getCommandId());
	//
	// ProductEntry product = ProductFactory.getCache()
	// .getProduct(request.getProductId());
	//
	// command.setMaxRetry(1);
	//
	// String serviceId = product.getParameters()
	// .getString("sdp.service.serviceid", "");
	// String productId = product.getParameters()
	// .getString("sdp.service.productcode", "");
	//
	// AppProperties app = new AppProperties();
	// app.setString("serviceid", serviceId);
	// app.setString("productid", productId);
	// result.setParameters(app);
	// String spId = product.getParameter("sdp.service.spid", "");
	//
	// String descKey = "sdp.description." + result.getActionType();
	//
	// String description = product.getParameter(descKey, "");
	//
	// description = description.replace("<ALIAS>", product.getIndexKey());
	//
	// connection = (SdpVmsConnection) instance.getProvisioningConnection();
	// long sessionid = result.getSessionId();
	//
	// RequestSOAPHeaderE header = connection.createHeader(result);
	//
	// GetPushMessageDeliveryStatusE bodyDelivery =
	// connection.getPushDelivery(result.getIdentifier());
	//
	// GetPushMessageDeliveryStatusResponseE resp =
	// connection.getDeliveryWap(header, bodyDelivery);
	//
	// vms.org.csapi.www.wsdl.parlayx.wap_push.send.v1_0.service.SendPushMessageServiceStub.DeliveryInformation[]
	// d = resp
	// .getGetPushMessageDeliveryStatusResponse()
	// .getDeliveryStatus();
	//
	// String deliveryS = "";
	//
	// if (d.length > 0)
	// {
	// for
	// (vms.org.csapi.www.wsdl.parlayx.wap_push.send.v1_0.service.SendPushMessageServiceStub.DeliveryInformation
	// a : d)
	// {
	// deliveryS = a.getStatus().toString();
	// }
	// }
	//
	// String respStr = "ID=" + sessionid + ", " + "isdn=" + result
	// .getIsdn() + ", status=" + deliveryS;
	// instance.logMonitor("GetDelivery Status: " + respStr);
	// }
	// catch (Exception e)
	// {
	// processError(instance, provisioningCommand, result, e);
	// }
	//
	// finally
	// {
	// instance.closeProvisioningConnection(connection);
	// }
	//
	// return result;
	// }

	public void sendMMS(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request) {

	}

	/**
	 * @author HungDT
	 * @param instance
	 * @param provisioningCommand
	 * @param request
	 * @return result
	 * @throws Exception
	 *             Send MT sms or wappush
	 */
	public CommandMessage sendMT(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		SdpVmsConnection connection = null;
		CommandMessage result = request;
		String contentAppend = "";
		try {
			ProductEntry product = ProductFactory.getCache().getProduct(
					request.getProductId());

			String serviceId = product.getParameters()
					.getString("sdp.service.serviceid", "").trim();
			String productId = product.getParameters()
					.getString("sdp.service.productcode", "").trim();

			String type = result.getParameters().getString("mttype", "sms");

			result.getParameters().setString("serviceid", serviceId);
			result.getParameters().setString("productid", productId);

			String spId = product.getParameter("sdp.service.spid", "").trim();

			String descKey = "sdp.description." + result.getActionType();

			String description = product.getParameter(descKey, "");

			description = description.replace("<ALIAS>", product.getIndexKey());

			connection = (SdpVmsConnection) instance
					.getProvisioningConnection();

			RequestSOAPHeaderE header = connection.createHeader(result);

			if (type.equalsIgnoreCase("sms")) {
				contentAppend = product.getParameters().getString(
						"sdp.appendmt", "");

				String content = result.getContent();

				SimpleDateFormat sdf1 = new SimpleDateFormat("HH");
				Date cur = Calendar.getInstance().getTime();

				if (Integer.parseInt(sdf1.format(cur)) >= 23
						|| Integer.parseInt(sdf1.format(cur)) <= 4) {
					if (!contentAppend.equals("")) {
						if (result.getContent().contains("goo.gl")) {

							content = content.substring(0,
									content.indexOf("http://goo.gl"))
									+ contentAppend;
						}
					}

				}

				result.setContent(content);

				SendSmsE body = connection.createBodySmS(result);

				String requestStr = "com.crm.provisioning.impl.mobifone.sendMT SMS{spid= "
						+ spId
						+ ",sid= "
						+ serviceId
						+ ",productid= "
						+ productId
						+ ""
						+ ",isdn= "
						+ result.getIsdn()
						+ ",description= "
						+ description
						+ ",content= "
						+ result.getContent() + "}";

				long sessionId = setRequest(instance, result, requestStr);
				
				SendSmsResponseE req = connection.sendSMS(header, body);
				if (req == null)
					throw new AppException(Constants.ERROR_TIMEOUT);

				String identifier = req.getSendSmsResponse().getResult();

				// SubscriberOrderImpl.updateDeliveryStatus(result.getOrderId(),
				// identifier, serviceId);
				result.setIdentifier(identifier);

				setResponse(instance, request, "identifier= " + identifier
						+ ",status= success", sessionId);
			} else if (type.equalsIgnoreCase("wappush")) {
				SendPushMessageE body = connection.createBodyWap(result);

				String requestStr = "com.crm.provisioning.impl.mobifone.sendMT WAPPUSH{spid = "
						+ spId
						+ ",sid= "
						+ serviceId
						+ ",productid= "
						+ productId
						+ ""
						+ ",isdn= "
						+ result.getIsdn()
						+ ",description= "
						+ description
						+ ",subject= "
						+ result.getDeliveryWapTitle()
						+ ",url= "
						+ result.getDeliveryWapHref() + "}";

				long sessionId = setRequest(instance, result, requestStr);

				SendPushMessageResponseE req = connection.sendWap(header, body);
				if (req == null)
					throw new AppException(Constants.ERROR_TIMEOUT);

				String identifier = req.getSendPushMessageResponse()
						.getRequestIdentifier();

				// SubscriberOrderImpl.updateDeliveryStatus(result.getOrderId(),
				// identifier, serviceId);

				result.setIdentifier(identifier);

				setResponse(instance, request, "identifier= " + identifier
						+ ",status= success", sessionId);
			} else {
				throw new AppException(Constants.ERROR_INVALID_DELIVER);
			}
		} catch (Exception e) {
			processError(instance, provisioningCommand, result, e);
		}

		finally {
			instance.closeProvisioningConnection(connection);
		}

		return result;
	}

}
