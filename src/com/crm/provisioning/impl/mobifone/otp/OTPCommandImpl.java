/**
 * ----------------------------------------------------------------- 
 * @ Copyright(c) 2013 Vietnamobile. JSC. All Rights Reserved.
 * ----------------------------------------------------------------- 
 * Date 	Author 		Version
 * ------------------------------------- 
 * Oct 6, 2013 hungdt  v1.0
 * -------------------------------------
 */
package com.crm.provisioning.impl.mobifone.otp;

import com.crm.kernel.message.Constants;
import com.crm.product.cache.ProductEntry;
import com.crm.product.cache.ProductFactory;
import com.crm.provisioning.cache.ProvisioningCommand;
import com.crm.provisioning.impl.CommandImpl;
import com.crm.provisioning.message.CommandMessage;
import com.crm.provisioning.message.VNMMessage;
import com.crm.provisioning.thread.CommandInstance;
import com.crm.provisioning.util.CommandUtil;
import com.crm.subscriber.bean.SubscriberOrder;
import com.crm.subscriber.impl.SubscriberOrderImpl;
import com.fss.util.AppException;

/**
 * @author hungdt
 * 
 */
public class OTPCommandImpl extends CommandImpl {
	public CommandMessage OTPRequest(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		OTPConnection connection = null;
		CommandMessage result = request;
		ProductEntry product = ProductFactory.getCache().getProduct(
				request.getProductId());
		try {
			String productCode = product.getParameters().getString(
					"otp.service.productcode", "");
			String isdn = request.getIsdn();
			String action = request.getParameters().getString("request.action",
					"unsub");

			connection = (OTPConnection) instance.getProvisioningConnection();

			String requestStr = "com.crm.provisioning.impl.mobifone.otp.OTPCommandImpl.OTPRequest{spid= "
					+ result.getMerchantId()
					+ ",productid= "
					+ result.getProductId()
					+ ""
					+ ",isdn= "
					+ result.getIsdn()
					+ ",action= " + action + "}";

			long sessionId = setRequest(instance, result, requestStr);
			int response = connection.createOTPRequest(isdn,
					request.getRequestId(), productCode, action);

			if (response == Constants.OK_200) {
				setResponse(instance, request, "success", sessionId);
				result.setCause("success");
			} else if (response == Constants.BAD_400) {
				setResponse(instance, request, "bad-request", sessionId);
				throw new AppException("bad-request");
			} else if (response == Constants.UNAUTHORIZED_401) {
				setResponse(instance, request, "Unauthorized", sessionId);
				throw new AppException("Unauthorized");
			} else if (response == Constants.INVALID_402) {
				setResponse(instance, request, "Invalid-isdn", sessionId);
				throw new AppException("Invalid-isdn");
			} else if (response == Constants.FORBIDDEN_403) {
				setResponse(instance, request, "Forbidden", sessionId);
				throw new AppException("Forbidden");
			} else if (response == Constants.METHOD_NOT_ALLOWED_405) {
				setResponse(instance, request, "Method-not-allowed", sessionId);
				throw new AppException("Method-not-allowed");
			} else if (response == Constants.NOT_ACCEPTABLE_406) {
				setResponse(instance, request, "Not-acceptable", sessionId);
				throw new AppException("Not-acceptable");
			}

		} catch (Exception e) {
			processError(instance, provisioningCommand, result, e);
		} finally {
			instance.closeProvisioningConnection(connection);
		}

		return result;
	}

	public CommandMessage SubRequest(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		OTPConnection connection = null;
		CommandMessage result = request;
		try {
			String isdn = request.getIsdn();
			String otp = request.getParameters().getString("request.otp", "");

			connection = (OTPConnection) instance.getProvisioningConnection();

			String requestStr = "com.crm.provisioning.impl.mobifone.otp.OTPCommandImpl.SubRequest{spid= "
					+ result.getMerchantId()
					+ ",productid= "
					+ result.getProductId()
					+ ""
					+ ",isdn= "
					+ result.getIsdn()
					+ ",otp= " + otp + "}";

			long sessionId = setRequest(instance, result, requestStr);

			// insert thue bao vao bang tam
			SubscriberOrderImpl.insertOTPQueue(result.getIsdn(),
					result.getProductId());

			int response = connection.createSUBRequest(isdn,
					request.getRequestId(), otp);

			if (response == Constants.OK_200) {
				setResponse(instance, request, "success", sessionId);
				result.setCause("success");
			} else if (response == Constants.BAD_400) {
				setResponse(instance, request, "bad-request", sessionId);
				SubscriberOrderImpl.deleteOTPQueue(result.getIsdn(),
						result.getProductId());
				throw new AppException("bad-request");
			} else if (response == Constants.UNAUTHORIZED_401) {
				setResponse(instance, request, "Unauthorized", sessionId);
				SubscriberOrderImpl.deleteOTPQueue(result.getIsdn(),
						result.getProductId());
				throw new AppException("Unauthorized");
			} else if (response == Constants.INVALID_402) {
				setResponse(instance, request, "Invalid-isdn", sessionId);
				SubscriberOrderImpl.deleteOTPQueue(result.getIsdn(),
						result.getProductId());
				throw new AppException("Invalid-isdn");
			} else if (response == Constants.FORBIDDEN_403) {
				setResponse(instance, request, "Forbidden", sessionId);
				SubscriberOrderImpl.deleteOTPQueue(result.getIsdn(),
						result.getProductId());
				throw new AppException("Forbidden");
			} else if (response == Constants.METHOD_NOT_ALLOWED_405) {
				setResponse(instance, request, "Method-not-allowed", sessionId);
				SubscriberOrderImpl.deleteOTPQueue(result.getIsdn(),
						result.getProductId());
				throw new AppException("Method-not-allowed");
			} else if (response == Constants.NOT_ACCEPTABLE_406) {
				setResponse(instance, request, "Not-acceptable", sessionId);
				SubscriberOrderImpl.deleteOTPQueue(result.getIsdn(),
						result.getProductId());
				throw new AppException("Not-acceptable");
			} else if (response == Constants.INVALID_REQUEST_ID_407) {
				setResponse(instance, request, "requestId-not-exist", sessionId);
				SubscriberOrderImpl.deleteOTPQueue(result.getIsdn(),
						result.getProductId());
				throw new AppException("requestId-not-exist");
			}

		} catch (Exception e) {
			processError(instance, provisioningCommand, result, e);
		} finally {
			instance.closeProvisioningConnection(connection);
		}

		return result;
	}
}
