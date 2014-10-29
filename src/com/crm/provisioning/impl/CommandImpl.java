/**
 * 
 */
package com.crm.provisioning.impl;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.axis.AxisFault;

import com.crm.product.cache.ProductFactory;
import com.crm.product.cache.ProductEntry;
import com.crm.product.cache.ProductMessage;
import com.crm.product.cache.ProductRoute;
import com.crm.provisioning.cache.CommandAction;
import com.crm.provisioning.cache.CommandEntry;
import com.crm.provisioning.cache.ProvisioningCommand;
import com.crm.provisioning.cache.ProvisioningConnection;
import com.crm.provisioning.cache.ProvisioningEntry;
import com.crm.provisioning.cache.ProvisioningFactory;
import com.crm.provisioning.cache.ProvisioningMessage;
import com.crm.provisioning.message.CommandMessage;
import com.crm.provisioning.message.VNMMessage;
import com.crm.provisioning.thread.CommandInstance;
import com.crm.provisioning.thread.ProvisioningThread;
import com.crm.provisioning.util.CommandUtil;
import com.crm.provisioning.util.ResponseUtil;
import com.crm.subscriber.bean.SubscriberGoService;
import com.crm.subscriber.bean.SubscriberProduct;
import com.crm.subscriber.impl.IDDServiceImpl;
import com.crm.subscriber.impl.MGMServiceImpl;
import com.crm.subscriber.impl.SubscriberGroupImpl;
import com.crm.subscriber.impl.SubscriberOrderImpl;
import com.crm.subscriber.impl.SubscriberProductImpl;
import com.crm.subscriber.impl.SubscriberRenewDailyImpl;
import com.crm.subscriber.impl.FunStoryServiceImpl;
import com.crm.util.GeneratorSeq;
import com.crm.util.HttpRequest;
import com.crm.util.StringPool;
import com.crm.util.StringUtil;
import com.crm.kernel.index.ExecuteImpl;
import com.crm.kernel.message.Constants;
import com.crm.kernel.sql.Database;
import com.crm.merchant.cache.MerchantEntry;
import com.crm.merchant.cache.MerchantFactory;
import com.fss.util.AppException;

/**
 * @author ThangPV
 * 
 */
public class CommandImpl extends ExecuteImpl {
	public ProvisioningConnection getProvisioningConnection(
			CommandInstance instance) throws Exception {
		return instance.getProvisioningConnection();
	}

	public String getErrorCode(CommandInstance instance,
			CommandMessage request, Exception error) {
		String errorCode = Constants.ERROR;
		try {
			instance.logMonitor(error);

			if (error != null) {
				if (error instanceof AppException) {
					errorCode = ((AppException) error).getMessage();
				} else {
					ProvisioningEntry provisioning = ProvisioningFactory
							.getCache().getProvisioning(
									request.getProvisioningId());

					ProvisioningMessage message = provisioning.getMessage(error
							.getMessage());

					if (message != null) {
						errorCode = message.getCause();
					}
				}
			}
		} catch (Exception e) {
			instance.getLog().error(e);
		}

		return errorCode;
	}

	public void processError(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request,
			Exception error) throws Exception {
		String errorCode = "";

		try {
			if (error instanceof AppException) {
				errorCode = ((AppException) error).getMessage();
			} else if (error instanceof AxisFault) {
				AxisFault axisFault = (AxisFault) error;

				if (axisFault.detail instanceof ConnectException) {
					errorCode = "connection-time-out";
				} else {
					errorCode = getErrorCode(instance, request, error);
				}
			} else if (error instanceof IOException) {
				throw new AppException(Constants.ERROR_CONNECTION);
			} else {
				errorCode = getErrorCode(instance, request, error);
			}

			request.setStatus(Constants.ORDER_STATUS_DENIED);
			request.setCause(errorCode);

			CommandUtil.processError(instance, request, errorCode);
		} finally {
			// rollback(instance, provisioningCommand, request);
		}
	}

	public CommandMessage simulation(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		try {
			long simulationExecuteTime = ((ProvisioningThread) instance
					.getDispatcher()).simulationTime;
			String cause = ((ProvisioningThread) instance.getDispatcher()).simulationCause;
			// instance.debugMonitor("Simulation execute time: " +
			// simulationExecuteTime + "ms");
			Thread.sleep(simulationExecuteTime);
			request.setCause(cause);
		} catch (Exception e) {
			throw e;
		}

		return request;
	}

	/**
	 * Edited by NamTA<br>
	 * Modified Date: 17/05/2012
	 * 
	 * @param instance
	 * @param provisioningCommand
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public CommandMessage register(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;

		try {
			ProductEntry product = ProductFactory.getCache().getProduct(
					request.getProductId());

			if (!product.isSubscription()) {
				throw new AppException(Constants.ERROR_INVALID_REQUEST);
			}

			boolean includeCurrentDay = result.getParameters().getBoolean(
					"includeCurrentDay");

			// if (result.getActionType().equals(Constants.ACTION_UPGRADE))
			// includeCurrentDay = false;

			int motype = 0;
			if (SubscriberOrderImpl.checkOTP(result.getIsdn(),
					result.getProductId())) {
				motype = 1;
			}

			SubscriberProduct subProduct = SubscriberProductImpl.register(
					result.getUserId(), result.getUserName(),
					result.getSubscriberId(), result.getIsdn(),
					result.getSubscriberType(), result.getProductId(),
					result.getCampaignId(), result.getLanguageId(),
					includeCurrentDay, result.getMerchantId(),
					product.getOpId(), motype);
			if (subProduct.getExpirationDate() != null) {
				result.setResponseValue(ResponseUtil.SERVICE_EXPIRE_DATE,
						StringUtil.format(subProduct.getExpirationDate(),
								"dd/MM/yyyy"));
			}
		} catch (Exception error) {
			processError(instance, provisioningCommand, request, error);
		}

		return result;
	}

	public CommandMessage unregister(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;

		try {
			ProductEntry product = ProductFactory.getCache().getProduct(
					request.getProductId());

			if (!product.isSubscription()) {
				throw new AppException(Constants.ERROR_INVALID_REQUEST);
			}

			SubscriberProductImpl.unregister(result.getUserId(),
					result.getUserName(), result.getSubProductId(),
					result.getProductId());
		} catch (Exception error) {
			processError(instance, provisioningCommand, request, error);
		}

		return result;
	}

	public CommandMessage disableRenew(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		int updated = 0;
		CommandMessage result = request;

		try {
			updated = SubscriberRenewDailyImpl.disableRenew(request.getIsdn(),
					request.getProductId(), Constants.ACTION_REGISTER,
					Constants.ACTION_AUTORENEW);
		} catch (Exception error) {
			processError(instance, provisioningCommand, request, error);
		}
		if (updated == Constants.SERVICE_STATUS_DENIED) {
			result.setCause(Constants.ERROR_UNREGISTERED);
		}
		return result;
	}

	public CommandMessage subscription(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;

		try {
			ProductEntry product = ProductFactory.getCache().getProduct(
					request.getProductId());

			if (!product.isSubscription()) {
				throw new AppException(Constants.ERROR_INVALID_REQUEST);
			}

			SubscriberProductImpl.subscription(result.getUserId(),
					result.getUserName(), result.getSubProductId(),
					result.isFullOfCharge(), result.getQuantity());
		} catch (Exception error) {
			processError(instance, provisioningCommand, request, error);
		}

		return result;
	}

	public CommandMessage barringBySupplier(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;

		try {
			ProductEntry product = ProductFactory.getCache().getProduct(
					request.getProductId());

			if (!product.isSubscription()) {
				throw new AppException(Constants.ERROR_INVALID_REQUEST);
			}

			SubscriberProductImpl.barringBySupplier(result.getUserId(),
					result.getUserName(), result.getSubProductId());
		} catch (Exception error) {
			processError(instance, provisioningCommand, request, error);
		}

		return result;
	}

	/**
	 * Created by NamTA<br>
	 * Created Date: 16/05/2012
	 * 
	 * @param instance
	 * @param provisioningCommand
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public CommandMessage extendExpirationDate(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;

		try {
			ProductEntry product = ProductFactory.getCache().getProduct(
					request.getProductId());

			if (!product.isSubscription()) {
				throw new AppException(Constants.ERROR_INVALID_REQUEST);
			}

			boolean includeCurrentDay = result.getParameters().getBoolean(
					"includeCurrentDay", false);
			SubscriberProduct subProduct = SubscriberProductImpl
					.extendExpirationDate(result.getUserId(),
							result.getUserName(), result.getSubProductId(),
							result.getCampaignId(), includeCurrentDay);

			/**
			 * Add response value
			 */
			result.setResponseValue(ResponseUtil.SERVICE_EXPIRE_DATE,
					StringUtil.format(subProduct.getExpirationDate(),
							"dd/MM/yyyy"));
		} catch (Exception error) {
			processError(instance, provisioningCommand, request, error);
		}

		return result;
	}

	public CommandMessage unbarringBySupplier(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;

		try {
			ProductEntry product = ProductFactory.getCache().getProduct(
					request.getProductId());

			if (!product.isSubscription()) {
				throw new AppException(Constants.ERROR_INVALID_REQUEST);
			}

			SubscriberProductImpl.unbarringBySupplier(result.getUserId(),
					result.getUserName(), result.getSubProductId());
		} catch (Exception error) {
			processError(instance, provisioningCommand, request, error);
		}

		return result;
	}

	public CommandMessage addMember(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		Connection connection = null;

		CommandMessage result = request;

		try {
			ProductEntry product = ProductFactory.getCache().getProduct(
					request.getProductId());

			connection = Database.getConnection();

			connection.setAutoCommit(false);

			// add member into group
			String verifyCode = result.getParameters().getString(
					"member.verifyCode", "");

			SubscriberGroupImpl.addMember(connection, result.getUserId(),
					result.getUserName(), result.getIsdn(), result.getShipTo(),
					result.getProductId(), product.getMemberType(), verifyCode,
					result.getOrderDate(), Constants.SUPPLIER_ACTIVE_STATUS);

			if (product.isSubscription()) {
				int totalMember = SubscriberGroupImpl.countMember(connection,
						result.getIsdn(), result.getProductId(),
						product.getMemberType(), true);

				if (totalMember == 1 && request.getProductId() != 12110) {
					boolean includeCurrentDay = result.getParameters()
							.getBoolean("includeCurrentDay", false);

					SubscriberProductImpl.register(connection,
							result.getUserId(), result.getUserName(),
							result.getSubscriberId(), result.getIsdn(),
							result.getSubscriberType(), result.getProductId(),
							result.getCampaignId(), result.getLanguageId(),
							includeCurrentDay, result.getMerchantId(), 0, 0);
				}
			}

			connection.commit();
		} catch (Exception error) {
			Database.rollback(connection);

			processError(instance, provisioningCommand, request, error);
		} finally {
			Database.closeObject(connection);
		}

		return result;
	}

	public CommandMessage addMemberF5(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		Connection connection = null;

		CommandMessage result = request;

		try {
			ProductEntry product = ProductFactory.getCache().getProduct(
					request.getProductId());

			connection = Database.getConnection();

			connection.setAutoCommit(false);

			// add member into group
			String verifyCode = result.getParameters().getString(
					"member.verifyCode", "");
			String[] phoneBookList = result.getRequestValue("f5-modify-member",
					"").split(",");

			for (int i = 0; i < phoneBookList.length; i++) {
				SubscriberGroupImpl
						.addMember(connection, result.getUserId(),
								result.getUserName(), result.getIsdn(),
								phoneBookList[i], result.getProductId(),
								product.getMemberType(), verifyCode,
								result.getOrderDate(),
								Constants.SUPPLIER_ACTIVE_STATUS);
			}
			connection.commit();
		} catch (Exception error) {
			Database.rollback(connection);

			processError(instance, provisioningCommand, request, error);
		} finally {
			Database.closeObject(connection);
		}

		return result;
	}

	public CommandMessage removeMember(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		Connection connection = null;

		CommandMessage result = request;

		try {
			ProductEntry product = ProductFactory.getCache().getProduct(
					request.getProductId());

			connection = Database.getConnection();

			connection.setAutoCommit(false);

			// add member into group
			SubscriberGroupImpl.removeMember(connection, result.getIsdn(),
					result.getShipTo(), result.getProductId(),
					product.getMemberType(), Constants.SUPPLIER_CANCEL_STATUS);

			if (product.isSubscription()) {
				int totalMember = SubscriberGroupImpl.countMember(connection,
						result.getIsdn(), result.getProductId(),
						product.getMemberType(), true);

				if (totalMember == 0) {
					SubscriberProductImpl.unregister(connection,
							result.getUserId(), result.getUserName(),
							result.getSubProductId(), result.getProductId());
				}
			}

			connection.commit();
		} catch (Exception error) {
			Database.rollback(connection);

			processError(instance, provisioningCommand, request, error);
		} finally {
			Database.closeObject(connection);
		}

		return result;
	}

	public CommandMessage removeMemberF5(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		Connection connection = null;

		CommandMessage result = request;

		try {
			ProductEntry product = ProductFactory.getCache().getProduct(
					request.getProductId());

			connection = Database.getConnection();

			connection.setAutoCommit(false);

			// add member into group
			String[] phoneBookList = result.getRequestValue("f5-modify-member",
					"").split(",");

			for (int i = 0; i < phoneBookList.length; i++) {
				SubscriberGroupImpl.removeMember(connection, result.getIsdn(),
						phoneBookList[i], result.getProductId(),
						product.getMemberType(),
						Constants.SUPPLIER_BARRING_STATUS);
			}

			connection.commit();
		} catch (Exception error) {
			Database.rollback(connection);

			processError(instance, provisioningCommand, request, error);
		} finally {
			Database.closeObject(connection);
		}

		return result;
	}

	public CommandMessage removeGroup(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		Connection connection = null;

		CommandMessage result = request;

		try {
			ProductEntry product = ProductFactory.getCache().getProduct(
					request.getProductId());

			connection = Database.getConnection();

			connection.setAutoCommit(false);

			// add member into group
			SubscriberGroupImpl.removeGroup(connection, result.getIsdn(),
					result.getProductId(), product.getMemberType());

			if (product.isSubscription()) {
				SubscriberProductImpl.unregister(connection,
						result.getUserId(), result.getUserName(),
						result.getSubProductId(), result.getProductId());
			}

			connection.commit();
		} catch (Exception error) {
			Database.rollback(connection);

			processError(instance, provisioningCommand, request, error);
		} finally {
			Database.closeObject(connection);
		}

		return result;
	}

	public CommandMessage rollback(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		instance.rollback(request);
		return request;
	}

	public CommandMessage nextCommand(CommandAction action,
			CommandMessage message) throws Exception {
		CommandMessage transform = ((CommandMessage) message).clone();

		transform.setCommandId(action.getNextCommandId());

		// transform.setCommandRequest(transform.getCommandResponse());
		transform.setResponse("");

		return transform;
	}

	/**
	 * withdraw money for loyalty balance.
	 * 
	 * @param instance
	 * @param provisioningCommand
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public CommandMessage withDraw(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;
		try {

			SubscriberProductImpl.withdraw(request.getUserId(),
					request.getUserName(), request.getSubscriberId(),
					request.getIsdn(), "LOYALTY", request.getQuantity());

			result.setCause(Constants.SUCCESS);
		} catch (Exception e) {
			result.setCause("error-withdraw-loyalty");
			processError(instance, provisioningCommand, result, e);
		}
		return result;
	}

	public VNMMessage registerLuckySimProduct(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		long userId = request.getUserId();
		String userName = request.getUserName();
		Date createDate = new Date();
		Date modifiedDate = new Date();
		Date unregisterDate = new Date();
		Date expirationDate = new Date();
		Date graceDate = new Date();
		String subscriberId = "0";
		long productId = request.getProductId();
		String isdn = request.getIsdn();
		int subscriberType = Constants.PREPAID_SUB_TYPE;
		int supplierStatus = Constants.SUPPLIER_CANCEL_STATUS;
		Date registerDate = new Date();
		String languageId = request.getLanguageId();
		long campaignId = request.getCampaignId();

		String strSQL = "insert into SubscriberProduct(subproductid,userid,username,"
				+ "createdate,modifieddate,subscriberid,productid,isdn,subscribertype,"
				+ "supplierstatus,registerdate,languageid,campaignid,unregisterdate,expirationdate,gracedate) "
				+ "values(sub_product_seq.nextval,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		VNMMessage result = CommandUtil.createVNMMessage(request);

		Connection connection = null;
		PreparedStatement stmt = null;
		try {
			connection = com.crm.kernel.sql.Database.getConnection();
			stmt = connection.prepareStatement(strSQL);
			stmt.setLong(1, userId);
			stmt.setString(2, userName);
			stmt.setTimestamp(3, new Timestamp(createDate.getTime()));
			stmt.setTimestamp(4, new Timestamp(modifiedDate.getTime()));
			stmt.setString(5, subscriberId);
			stmt.setLong(6, productId);
			stmt.setString(7, isdn);
			stmt.setInt(8, subscriberType);
			stmt.setInt(9, supplierStatus);
			stmt.setTimestamp(10, new Timestamp(registerDate.getTime()));
			stmt.setString(11, languageId);
			stmt.setLong(12, campaignId);
			stmt.setTimestamp(13, new Timestamp(unregisterDate.getTime()));
			stmt.setTimestamp(14, new Timestamp(expirationDate.getTime()));
			stmt.setTimestamp(15, new Timestamp(graceDate.getTime()));
			stmt.executeUpdate();
		} catch (Exception ex) {
			result.setCause(Constants.ERROR);
			result.setDescription(ex.getMessage());
			instance.logMonitor(ex.getMessage());
			throw ex;
		} finally {
			Database.closeObject(stmt);
			Database.closeObject(connection);
		}
		return result;
	}

	public CommandMessage recover(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;
		try {
			// Change function to recover money when error
			SubscriberProductImpl.withdraw(request.getUserId(),
					request.getUserName(), request.getSubscriberId(),
					request.getIsdn(), "LOYALTY", -1 * request.getQuantity());
			result.setCause(Constants.SUCCESS);
		} catch (Exception e) {
			result.setCause("error-withdraw-loyalty");
			processError(instance, provisioningCommand, result, e);
		}
		return result;
	}

	// 2012-09-19 MinhDT ADD_START: add for product VB600 and VB220
	public CommandMessage confirmRegister(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;

		String responseCode = "";
		try {
			IDDServiceImpl.confirmRegister(result.getIsdn(),
					result.getProductId());
			responseCode = Constants.SUCCESS;
			result.setCause(responseCode);
			result.setResponse(responseCode);
			result.setStatus(Constants.ORDER_STATUS_APPROVED);
		} catch (Exception error) {
			processError(instance, provisioningCommand, result, error);
		}

		return result;
	}

	public CommandMessage registerIDD(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		try {
			boolean includeCurrentDay = request.getParameters().getBoolean(
					"includeCurrentDay");

			if (request.getActionType().equals(Constants.ACTION_REGISTER)
					|| request.getActionType().equals(Constants.ACTION_UPGRADE))
				includeCurrentDay = false;

			SubscriberProduct subProduct = SubscriberProductImpl.register(
					request.getUserId(), request.getUserName(),
					request.getSubscriberId(), request.getIsdn(),
					request.getSubscriberType(), request.getProductId(),
					request.getCampaignId(), request.getLanguageId(),
					includeCurrentDay, request.getMerchantId(), 0, 0);

			if (request.getActionType().equals(
					Constants.ACTION_SUPPLIER_REACTIVE)
					|| request.getParameters()
							.getProperty("PropertiesRenew", "false")
							.equals("true")) {
				IDDServiceImpl.updateProperties(request.getIsdn(),
						request.getProductId(), "renew");
				IDDServiceImpl.removeExtendIDDBuffet(request.getIsdn(),
						request.getProductId());

				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				Calendar cal = Calendar.getInstance();
				cal.add(cal.HOUR_OF_DAY, 14);

				request.setCause("renew.success");
				request.setResponseValue(ResponseUtil.SERVICE_START_DATE, cal
						.getTime().getHours());
				request.setResponseValue(ResponseUtil.SERVICE_ACTIVE_DATE,
						sdf.format(cal.getTime()));

				ProductRoute orderRoute = ProductFactory.getCache()
						.getProductRoute(request.getRouteId());
				boolean allowSendSMS = Boolean.parseBoolean(orderRoute
						.getParameter("AllowSendSMS", "false"));

				if (allowSendSMS) {
					CommandMessage object = request.clone();
					object.setCause("renew.success");
					object.setChannel(Constants.CHANNEL_SMS);

					ResponseUtil.notifyOwner(instance, orderRoute, object);
				}
			}

			IDDServiceImpl.removeConfirm(request.getIsdn(),
					request.getProductId());
		} catch (Exception error) {
			processError(instance, provisioningCommand, request, error);
		}

		return request;
	}

	public CommandMessage unregisterIDD(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		try {
			SubscriberProductImpl.unregister(request.getUserId(),
					request.getUserName(), request.getSubProductId(),
					request.getProductId());

			IDDServiceImpl.removeConfirm(request.getIsdn(),
					request.getProductId());

			IDDServiceImpl.removeExtendIDDBuffet(request.getIsdn(),
					request.getProductId());

			ProductRoute orderRoute = ProductFactory.getCache()
					.getProductRoute(request.getRouteId());
			boolean allowSendSMS = Boolean.parseBoolean(orderRoute
					.getParameter("AllowSendSMS", "false"));

			if (allowSendSMS) {
				CommandMessage object = request.clone();
				object.setChannel(Constants.CHANNEL_SMS);

				ResponseUtil.notifyOwner(instance, orderRoute, object);
			}
		} catch (Exception error) {
			processError(instance, provisioningCommand, request, error);
		}

		return request;
	}

	public CommandMessage registerMGM(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		ProductEntry product = ProductFactory.getCache().getProduct(
				request.getProductId());

		String introducer = request.getIsdn();

		String responseCode = "";

		try {
			String referral = request.getKeyword().toUpperCase();

			referral = referral.substring(referral.indexOf(" "),
					referral.length()).trim();

			// Check country code
			if (!referral.equals("")
					&& referral.startsWith(Constants.DOMESTIC_CODE)) {
				referral = referral.substring(Constants.DOMESTIC_CODE.length());
				referral = Constants.COUNTRY_CODE + referral;
			}

			// Check max referral
			int intNumOfCc = MGMServiceImpl.getNumOfCC(introducer);
			String CCGroupName = product.getParameter("CCGroupName", "CG");
			CCGroupName = CCGroupName + (intNumOfCc + 1);
			int AlcoTime = Integer.valueOf(product.getParameter("AlcoTime",
					"34"));

			Calendar serviceStart = Calendar.getInstance();
			Calendar serviceEnd = Calendar.getInstance();
			serviceEnd.add(Calendar.DATE, AlcoTime);

			boolean checkAddDaily = Boolean.parseBoolean(request
					.getParameters().getProperty("AddDaily", "true"));
			int addDaily = 1;

			if (!checkAddDaily) {
				addDaily = 2;
				AlcoTime = Integer.valueOf(product.getParameter("AlcoTime_1M",
						"30"));
			}

			// Insert into table.
			if (MGMServiceImpl.insertNewMGM(introducer, referral, introducer
					+ "_" + CCGroupName, serviceStart, serviceEnd, AlcoTime,
					addDaily)) {
				if (!checkAddDaily) {
					responseCode = Constants.SUCCESS + "_1M";
				} else {
					responseCode = Constants.SUCCESS;
				}
				request.setCause(responseCode);
				request.setShipTo(referral);
				request.setResponseValue(ResponseUtil.REFERAL, referral);
				request.setResponseValue(ResponseUtil.SERVICE_EXPIRE_DATE,
						new SimpleDateFormat("dd/MM/yyyy").format(serviceEnd
								.getTime()));
			} else {
				throw new AppException("insert-fail");
			}
		} catch (Exception e) {
			processError(instance, provisioningCommand, request, e);
		}

		return request;
	}

	public CommandMessage updateDailyMGM(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		String responseCode = "";
		String isdn = request.getIsdn();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			if (MGMServiceImpl.updateAddBalance(isdn, sdf.format(new Date()))) {
				responseCode = Constants.SUCCESS;
				request.setCause(responseCode);
				request.setStatus(Constants.SERVICE_STATUS_APPROVED);
			} else {
				throw new AppException("update-fail");
			}
		} catch (Exception e) {
			processError(instance, provisioningCommand, request, e);
		}

		return request;
	}

	public CommandMessage doNothing(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		return request;
	}

	public CommandMessage registerTest(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		return request;
	}

	public CommandMessage unregisterTest(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		return request;
	}

	/**
	 * Sets request and log, auto-generate sessionId
	 * 
	 * @param instance
	 * @param request
	 * @param requestString
	 * @return
	 * @throws Exception
	 */
	public long setRequest(CommandInstance instance, CommandMessage request,
			String requestString) throws Exception {
		long sessionId = 0;
		try {
			if (request.getOrderId() != Constants.DEFAULT_ID)
				sessionId = request.getOrderId();
			sessionId = GeneratorSeq.getNextSeq();
		} catch (Exception e) {
		}

		setRequest(instance, request, requestString, sessionId);

		return sessionId;
	}

	/**
	 * Sets request and log, use existing sessionId
	 * 
	 * @param instance
	 * @param request
	 * @param requestString
	 * @param sessionId
	 * @throws Exception
	 */
	public void setRequest(CommandInstance instance, CommandMessage request,
			String requestString, long sessionId) throws Exception {

		requestString = "ID=" + sessionId + ": " + requestString;

		instance.debugMonitor("SEND: " + requestString);
		// request.setRequest(requestString);
		request.setRequestTime(new Date());
	}

	/**
	 * Sets response and log
	 * 
	 * @param instance
	 * @param request
	 * @param responseString
	 * @param sessionId
	 * @throws Exception
	 */
	public void setResponse(CommandInstance instance, CommandMessage request,
			String responseString, long sessionId) throws Exception {
		request.setResponseTime(new Date());

		String costTime = CommandUtil.calculateCostTime(
				request.getRequestTime(), request.getResponseTime());

		responseString = "ID=" + sessionId + ": " + responseString
				+ ": costTime=" + costTime;
		instance.debugMonitor("RECEIVE: " + responseString);
		// request.setResponse(responseString);
	}

	public CommandMessage nextCommand(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {

		return request;
	}

	// DuyMB add GO services 20140402
	public CommandMessage registerGoService(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;
		long score = 0;
		long lastQuestionId = 0;
		long numberofQuestion = 0;
		int isFirstTime = 0;

		try {
			ProductEntry product = ProductFactory.getCache().getProduct(
					request.getProductId());

			if (!product.isSubscription()) {
				throw new AppException(Constants.ERROR_INVALID_REQUEST);
			}

			boolean includeCurrentDay = result.getParameters().getBoolean(
					"includeCurrentDay");

			String strArr = SubscriberProductImpl.getQuestion(
					request.getProductId(), "question", request.getIsdn());

			// get randome for questionID
			// lastQuestionId =
			// SubscriberProductImpl.randomizeQuestion(request.getProductId());

			lastQuestionId = Long.parseLong(strArr.split(";")[0]);

			result.setNextQuestion(strArr.split(";")[1]);

			result.getParameters().setLong("QuestionId", lastQuestionId);

			// Truong hop co them 1 content nua tra ve khi dang ky ngoai MT
			// thong bao va noi dung cau hoi
			String firstContent = SubscriberProductImpl.getFirstContent(
					request.getProductId(), "content");

			result.getParameters().setString("firstcontent", firstContent);

			// Score
			if (!result.getParameters().getString("BonusPoint").equals("")) {
				score = Long.valueOf(result.getParameters().getString(
						"BonusPoint"));
			}
			if (result.getParameters().getString("FreeForFirstTime")
					.equals("true")) {
				isFirstTime = 1;
			}
			if (request.getActionType().equals(Constants.ACTION_SUBSCRIPTION)) {
				if (request.isFullOfCharge()) {
					numberofQuestion = 0;
					score = Long.valueOf(result.getParameters().getString(
							"BonusPoint"));
				} else {
					numberofQuestion = Long.valueOf(product.getParameter(
							"SmsFree", "5")) - request.getQuantity();
					score = Long.valueOf(product.getParameter("BaseBonusPoint",
							"50"));
				}
				Calendar sendDate = Calendar.getInstance();
				sendDate.set(Calendar.HOUR_OF_DAY, Integer.valueOf(product
						.getParameter("Sms.SendHours", "8")));
				sendDate.set(Calendar.MINUTE, Integer.valueOf(product
						.getParameter("Sms.SendMinutes", "0")));
				sendDate.set(Calendar.SECOND, Integer.valueOf(product
						.getParameter("Sms.SendSecond", "0")));

				SubscriberProductImpl.insertSms(result.getIsdn(),
						result.getProductId(), result.getNextQuestion(),
						lastQuestionId, result.getServiceAddress(),
						sendDate.getTime());
				result.setNextQuestion("");
			}
			if (request.getParameters().getInteger("UseredQuestion", 0) > 0) {
				numberofQuestion = request.getParameters().getInteger(
						"UseredQuestion");
			}

			SubscriberProductImpl.registerGoService(result.getUserId(),
					result.getUserName(), result.getSubscriberId(),
					result.getIsdn(), result.getSubscriberType(),
					result.getProductId(), result.getCampaignId(),
					result.getLanguageId(), includeCurrentDay, score,
					lastQuestionId, numberofQuestion, isFirstTime,
					result.getMerchantId(), product.getOpId());
		} catch (Exception error) {
			processError(instance, provisioningCommand, request, error);
		}

		return result;
	}

	public CommandMessage subscriptionGoService(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;

		return result;
	}

	public CommandMessage unRegisterGoService(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;
		try {
			SubscriberProductImpl.unregisterGoService(request.getUserId(),
					request.getUserName(), request.getSubProductId(),
					request.getProductId());
		} catch (Exception error) {
			processError(instance, provisioningCommand, request, error);
		}
		return result;
	}

	public CommandMessage confirmGoService(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;
		int score = request.getParameters().getInt("Score");
		long questionId = request.getParameters().getLong("QuestionId");
		long numberofquestion = request.getParameters().getLong(
				"NumerOfQuestion");
		String correctanswer = SubscriberProductImpl.getAnswer(questionId,
				request.getProductId());
		String answer = "";
		// sms.params.count
		String[] split = request.getKeyword().split("_");
		answer = split[1];

		if (answer.equals(correctanswer)) {
			score = score
					+ (int) request.getParameters().getLong("RewardPoint")
					* (int) request.getParameters().getLong("BetRate");
			result.setCause(Constants.CORRECT_ANSWER);
			result.setResponse(Constants.SUCCESS);
			result.setDescription("Confirm success");
		} else {
			result.setCause(Constants.WRONG_ANSWER);
			result.setResponse(Constants.SUCCESS);
			result.setDescription("Confirm success");
		}
		SubscriberProductImpl.insertQuestionLog(request.getIsdn(), questionId);

		String strArr = SubscriberProductImpl.getQuestion(
				request.getProductId(), "question", request.getIsdn());

		questionId = Long.parseLong(strArr.split(";")[0]);

		String question = strArr.split(";")[1];

		// update lai questionId
		result.getParameters().setLong("QuestionId", questionId);

		SubscriberProductImpl.updateScoreGoService(request.getIsdn(),
				request.getProductId(), score, questionId, numberofquestion);
		result.setNextQuestion(question);
		result.getParameters().setString("NumOfCode", String.valueOf(score));
		result.setStatus(Constants.SERVICE_STATUS_APPROVED);
		return result;
	}

	public CommandMessage getFunStory(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;

		try {
			ProductEntry product = ProductFactory.getCache().getProduct(
					request.getProductId());

			String productCode = request.getRequestValue("productCode", "");

			if ("".equals(productCode)) {
				productCode = product.getParameter("MTProductCode", "");
			}

			FunStoryServiceImpl.sendFunStory(result.getIsdn(), result
					.getServiceAddress(), productCode, product.getParameters()
					.getInteger("TotalMT", 1), product.getParameter(
					"DelayTime", "5/(24*60*60)"), result.getProductId());
		} catch (Exception error) {
			processError(instance, provisioningCommand, request, error);
		}

		return result;
	}

	public CommandMessage CheckService(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;
		String content = "";
		try {
			ProductEntry product = ProductFactory.getCache().getProduct(
					request.getProductId());

			String productCode = request.getRequestValue("productCode", "");

			if ("".equals(productCode)) {
				productCode = product.getParameter("MTProductCode", "");
			}
			String templateBody = "~AAA~ tren dau so ~XXX~: ~ServiceDetail~. Phi dich vu: ~Fee~dong/~Unit~. Quy khach dang ky/gia han dich vu vao ngay ~DATE~ qua SMS.";
			String templateBlast = "De huy dich vu, vui long soan: HUY 9999 gui ~XXX~. Tran trong cam on!";
			String templateHeader = "Quy khach dang su dung dich vu ";

			SubscriberProduct[] listOfActiveService = SubscriberProductImpl
					.getListActive(1, request.getIsdn()).toArray(
							new SubscriberProduct[] {});
			SubscriberProduct[] listOfBarrService = SubscriberProductImpl
					.getBarring(request.getIsdn());
			if (listOfActiveService.length == 0
					&& listOfBarrService.length == 0) {
				content = "Quy khach khong su dung dich vu nao tren dau so nay. Tran trong cam on";
			} else {
				for (int i = 0; i < listOfActiveService.length; i++) {
					product = ProductFactory.getCache().getProduct(
							listOfActiveService[i].getProductId());
					if (product.getParameter("ActiveCheckService", "false")
							.equals("true")) {
						String temp = templateBody.replace("~AAA~",
								product.getAlias());
						temp = temp.replace("~XXX~",
								request.getServiceAddress());
						temp = temp.replace("~ServiceDetail~",
								product.getTitle());

						temp = temp.replace("~Fee~", new DecimalFormat("##")
								.format(product.getPrice()));
						temp = temp.replace(
								"~Unit~",
								String.valueOf(product.getSubscriptionPeriod())
										+ convertToVietnamese(product
												.getSubscriptionUnit()));
						temp = temp.replace("~DATE~", new SimpleDateFormat(
								"dd/mm/yyyy").format(listOfActiveService[i]
								.getRegisterDate()));
						content = content + temp;
					}
				}
				for (int i = 0; i < listOfBarrService.length; i++) {
					product = ProductFactory.getCache().getProduct(
							listOfActiveService[i].getProductId());
					if (product.getParameter("ActiveCheckService", "false")
							.equals("true")) {
						String temp = templateBody.replace("~AAA~",
								product.getAlias());
						temp = temp.replace("~XXX~",
								product.getServiceAddress());
						temp = temp.replace("~ServiceDetail~",
								product.getTitle());
						temp = temp.replace("~Fee~", new DecimalFormat("##")
								.format(product.getPrice()));
						temp = temp.replace(
								"~Unit~",
								String.valueOf(product.getSubscriptionPeriod())
										+ convertToVietnamese(product
												.getSubscriptionUnit()));
						temp = temp.replace("~DATE~", new SimpleDateFormat(
								"dd/mm/yyyy").format(listOfBarrService[i]
								.getRegisterDate()));
						content = content + temp;
					}
				}
				if (content.equals("")) {
					content = "Quy khach khong su dung dich vu nao tren dau so nay. Tran trong cam on";
				} else {
					content = templateHeader
							+ content
							+ templateBlast.replace("~XXX~",
									request.getServiceAddress());
				}
			}
		} catch (Exception error) {
			error.printStackTrace();
			content = "Loi he thong. Lien he 789 de duoc ho tro. Xin cam on!";
		}
		CommandUtil.sendSMS(null, request, request.getServiceAddress(), "",
				content);
		return result;
	}

	private String convertToVietnamese(String unit) {
		String convert = "";
		if (unit.toUpperCase().equals("DAY")
				|| unit.toUpperCase().equals("DAYS")) {
			convert = "Ngay";
		} else if (unit.toUpperCase().equals("MONTH")
				|| unit.toUpperCase().equals("MONTHLY")) {
			convert = "Thang";
		} else if (unit.toUpperCase().equals("WEEK")
				|| unit.toUpperCase().equals("WEEKLY")) {
			convert = "Tuan";
		}
		return convert;

	}

	public CommandMessage guidanceService(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;
		return result;
	}

	public CommandMessage getNumberOfCode(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;
		try {
			result.getParameters().setString(
					"NumOfCode",
					String.valueOf(SubscriberProductImpl.getNumberOfCode(
							request.getIsdn(), request.getProductId())));
		} catch (Exception error) {
			processError(instance, provisioningCommand, request, error);
		}
		return result;
	}

	// public CommandMessage sendMOToCP(CommandInstance instance,
	// ProvisioningCommand provisioningCommand, CommandMessage request)
	// throws Exception {
	//
	// CommandMessage result = request;
	// MerchantEntry merchant = MerchantFactory.getCache().getMerchant(
	// request.getMerchantId());
	//
	// ProductRoute productRoute = ProductFactory.getCache().getProductRoute(
	// request.getRouteId());
	// ProductEntry product = ProductFactory.getCache().getProduct(
	// request.getProductId());
	//
	// String productCode = product.getAlias();
	// String cmdCode = "";
	// String msgBody = "";
	//
	// if (product.getOpId() == 4) {
	// cmdCode = productRoute.getKeyword().replace("%", StringPool.BLANK)
	// .replace(" ", StringPool.BLANK);
	// msgBody = request.getKeyword().replace(" ", StringPool.URL_SPACE);
	// }
	//
	// if (product.getOpId() != 4) {
	// productCode = product.getAlias();
	// cmdCode = productRoute.getParameters()
	// .getString("mastercp.code", "")
	// .replace("%", StringPool.BLANK)
	// .replace(" ", StringPool.BLANK);
	// msgBody = productRoute.getParameters()
	// .getString("mastercp.msg", "")
	// .replace(" ", StringPool.URL_SPACE);
	// }
	//
	// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	//
	// Connection connection = null;
	// try {
	// StringBuffer cpUrl = new StringBuffer();
	// cpUrl.append(product.getHost());
	// cpUrl.append("?username=");
	// cpUrl.append(merchant.getUsername());
	// cpUrl.append("&password=");
	// cpUrl.append(merchant.getPassword());
	// cpUrl.append("&dest=");
	// cpUrl.append(request.getServiceAddress());
	// cpUrl.append("&isdn=");
	// cpUrl.append(request.getIsdn());
	// cpUrl.append("&reqid=");
	// cpUrl.append(request.getOrderId());
	// cpUrl.append("&requestDate=");
	// cpUrl.append(dateFormat.format(request.getOrderDate()));
	// cpUrl.append("&productCode=");
	// cpUrl.append(productCode);
	// cpUrl.append("&cmdcode=");
	// // cpUrl.append(productRoute.getKeyword().replace("%",
	// // StringPool.BLANK).replace(" ", StringPool.BLANK));
	// cpUrl.append(cmdCode);
	// cpUrl.append("&msgbody=");
	// // cpUrl.append(request.getKeyword().replace(" ",
	// // StringPool.URL_SPACE));
	// cpUrl.append(msgBody);
	// cpUrl.append("&opid=");
	// cpUrl.append(product.getOpId());
	//
	// connection = Database.getConnection();
	// // SubscriberOrderImpl.updateDesc(connection, request.getOrderId(),
	// // cpUrl.toString(), request.getOrderDate());
	// result.getParameters().setString("cpurl", cpUrl.toString());
	// long sessionId = setRequest(instance, request, cpUrl.toString());
	// String response = "";
	// if (instance.getDebugMode().equals("depend")) {
	// response = "202";
	// long simulationExecuteTime = ((ProvisioningThread) instance
	// .getDispatcher()).simulationTime;
	// Thread.sleep(simulationExecuteTime);
	// } else {
	// if (result.getOpId() == 2
	// && result.getActionType().equals(
	// Constants.ACTION_REGISTER)) {
	// long simulationExecuteTime = ((ProvisioningThread) instance
	// .getDispatcher()).simulationTime;
	// if (simulationExecuteTime != 0) {
	// Thread.sleep(simulationExecuteTime);
	// }
	//
	// response = HttpRequest.callURL(cpUrl.toString()).trim();
	// } else {
	// response = HttpRequest.callURL(cpUrl.toString()).trim();
	// }
	//
	// }
	//
	// if (response.equalsIgnoreCase("200")) {
	// result.setCause(Constants.SUCCESS);
	// result.setResponse(response);
	// } else if (response.equalsIgnoreCase("202")) {
	// result.setCause(Constants.SUCCESS);
	// result.setResponse(response);
	// } else {
	// result.setCause(Constants.ERROR);
	// setResponse(instance, request, result.getCause()
	// + ", cpResponse=" + response, sessionId);
	// throw new AppException(Constants.ERROR);
	// }
	// setResponse(instance, request, result.getCause() + ", cpResponse="
	// + response, sessionId);
	//
	// } catch (Exception e) {
	// processError(instance, provisioningCommand, request, e);
	// } finally {
	// Database.closeObject(connection);
	// }
	//
	// return result;
	// }

	public CommandMessage sendMOToCP(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		CommandMessage result = request;

		MerchantEntry merchant = MerchantFactory.getCache().getMerchant(
				request.getMerchantId());

		ProductRoute productRoute = ProductFactory.getCache().getProductRoute(
				request.getRouteId());
		ProductEntry product = ProductFactory.getCache().getProduct(
				request.getProductId());

		String productCode = product.getAlias();
		String cmdCode = "";
		String msgBody = "";

		if (product.getOpId() == 4) {
			cmdCode = productRoute.getKeyword().replace("%", StringPool.BLANK)
					.replace(" ", StringPool.BLANK);
			msgBody = request.getKeyword().replace(" ", StringPool.URL_SPACE);
		}

		if (product.getOpId() != 4) {
			productCode = product.getAlias();
			cmdCode = productRoute.getParameters()
					.getString("mastercp.code", "")
					.replace("%", StringPool.BLANK)
					.replace(" ", StringPool.BLANK);
			msgBody = productRoute.getParameters()
					.getString("mastercp.msg", "")
					.replace(" ", StringPool.URL_SPACE);
		}

		// insert mo queue
		SubscriberProductImpl.insertMOCPQueue(result.getOrderId(),
				result.getOrderDate(), merchant.getUsername(),
				merchant.getPassword(), result.getServiceAddress(),
				result.getIsdn(), productCode, cmdCode, msgBody,
				result.getOpId(), product.getHost());

		return result;
	}
}
