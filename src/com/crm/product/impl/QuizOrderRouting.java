/**
 * 
 */
package com.crm.product.impl;

import java.util.Date;
import java.util.List;

import com.crm.kernel.message.Constants;
import com.crm.merchant.cache.MerchantEntry;
import com.crm.merchant.cache.MerchantFactory;
import com.crm.product.cache.ProductEntry;
import com.crm.product.cache.ProductFactory;
import com.crm.product.cache.ProductPrice;
import com.crm.product.cache.ProductRoute;
import com.crm.provisioning.message.CommandMessage;
import com.crm.provisioning.message.VNMMessage;
import com.crm.provisioning.thread.OrderRoutingInstance;
import com.crm.provisioning.util.CommandUtil;
import com.crm.question.bean.ContentQuestion;
import com.crm.subscriber.bean.SubscriberGoService;
import com.crm.subscriber.bean.SubscriberProduct;
import com.crm.subscriber.impl.SubscriberEntryImpl;
import com.crm.subscriber.impl.SubscriberOrderImpl;
import com.crm.subscriber.impl.SubscriberProductImpl;
import com.crm.util.AppProperties;
import com.crm.util.DateUtil;
import com.crm.util.GeneratorSeq;
import com.fss.util.AppException;

/**
 * @author HungDT
 * 
 */
public class QuizOrderRouting extends OrderRoutingImpl
{

	public void checkActionType(OrderRoutingInstance instance, ProductRoute orderRoute, ProductEntry product,
			CommandMessage order, SubscriberProduct subscriberProduct) throws Exception
	{
		if (subscriberProduct != null)
		{
			if (order.getActionType().equals(Constants.ACTION_REGISTER) || order.getActionType().equals(Constants.ACTION_SUBSCRIPTION))
			{
				// Date now = new Date();
				if (subscriberProduct != null)
				{
					// truong hop dang ky lai, gui thong bao va gui cau hoi hien
					// tai cua khach hang
					SubscriberGoService subscriberGoService = SubscriberProductImpl.getGoServiceData(order.getIsdn(), product.getProductId());

					String question = SubscriberProductImpl.getQuestion(order.getProductId(), "question", subscriberGoService.getLastQuestionId());

					order.setNextQuestion(question);
					order.getParameters().setString("NumOfCode", String.valueOf(subscriberGoService.getScore()));

					throw new AppException(Constants.ERROR_REGISTERED);
				}
			}
			order.setSubProductId(subscriberProduct.getSubProductId());
		}

		// Mien phi cho lan dang ky dau tien.
		if (order.getActionType().equals(Constants.ACTION_REGISTER)
				&& SubscriberProductImpl.isFirstTimeRegister(order.getIsdn(), product.getProductId()))
		{
			order.getParameters().setString("FreeForFirstTime", "true");
			order.getParameters().setString("BonusPoint", "0");
		}
		else if (order.getActionType().equals(Constants.ACTION_REGISTER) &&
				SubscriberProductImpl.isSecondTimeInDay(order.getIsdn(), product.getProductId()))
		{
			order.getParameters().setString("BonusPoint", "50");
			order.getParameters().setString("UseredQuestion", "0");
		}

		else if (order.getActionType().equals(Constants.ACTION_REGISTER) &&
				SubscriberProductImpl.isUnregisterInDay(order.getIsdn(), product.getProductId()) &&
				subscriberProduct == null)
		{
			order.setPaid(true);
			order.getParameters().setString("UseredQuestion", "5");

		}
		else if (order.getActionType().equals(Constants.ACTION_UNREGISTER) && subscriberProduct == null)
		{
			throw new AppException(Constants.ERROR_UNREGISTERED);
		}

	}

	public CommandMessage parser(OrderRoutingInstance instance,
			ProductRoute orderRoute, CommandMessage order) throws Exception
	{
		Exception error = null;

		ProductEntry product = null;

		SubscriberProduct subscriberProduct = null;

		MerchantEntry merchant = null;

		SubscriberGoService subscriberGoService = null;
		try
		{
			// check SMS syntax
			Date startTime = new Date();
			Date endTime = new Date();

			/**
			 * Should check for both SMS & web.
			 */
			smsParser(instance, orderRoute, order);

			// check duplicate request
			if (orderRoute.getDuplicateScan() > 0)
			{
				startTime = new Date();
				checkDuplicate(instance, orderRoute, order);

				endTime = new Date();
				instance.debugMonitor("Check duplicate(" + order.getIsdn() + ") cost time: "
						+ (endTime.getTime() - startTime.getTime()) + "ms");
			}

			// if (orderRoute.getMaxRegisterDaily() > 0)
			// {
			// startTime = new Date();
			// checkMaxRegister(instance, orderRoute, order);
			//
			// endTime = new Date();
			// instance.debugMonitor("Check maxregisterdaily(" + order.getIsdn()
			// + ")  cost time: "
			// + (endTime.getTime() - startTime.getTime()) + "ms");
			// }
			// check promotion
			// if (orderRoute.isCheckPromotion())
			// {
			// checkPromotion(instance, orderRoute, order);
			// }

			// check product in available list
			product = ProductFactory.getCache().getProduct(order.getProductId());

			merchant = MerchantFactory.getCache().getMerchant(product.getMerchantId());

			order.getParameters().setBoolean("includeCurrentDay", product.getParameters().getBoolean("includeCurrentDay", true));

			// get current subscriber product
			if (!product.isSubscription())
			{

			}
			else if (order.getSubProductId() == Constants.DEFAULT_ID)
			{
				/**
				 * Edited: replaced getActive by getUnterminated (for barring
				 * subscription case)
				 */
				subscriberProduct = SubscriberProductImpl.getActive(
						order.getIsdn(), order.getProductId());
			}
			else
			{
				subscriberProduct = SubscriberProductImpl.getProduct(order
						.getSubProductId());
			}

			String cmdCode = product.getParameters().getString("commandcode");
			order.getParameters().setString("commandcode", cmdCode);
			order.getParameters().setBoolean("isViettel_nonsub", true);
			order.getParameters().setString("vt8x26.commandcode", product.getParameters().getString("vt8x26.commandcode"));

			if (order.getParameters().getString("responseQueue", "").equals(""))
			{
				order.getParameters().setString("responseQueue", "vt/OrderResponse");
			}

			// check action type
			checkActionType(instance, orderRoute, product, order, subscriberProduct);

			if (order.getActionType().equals("smsdaily"))
			{
				SubscriberProductImpl.updateQuantity(order.getUserId(), order
						.getUserName(), subscriberProduct.getSubProductId(), 1);
			}

			if (order.getActionType().equals(Constants.ACTION_CONFIRM) ||
					order.getActionType().equals(Constants.ACTION_BET))
			{
				subscriberGoService = SubscriberProductImpl.getGoServiceData(order.getIsdn(), product.getProductId());
				// Check valid question
				if (subscriberGoService == null)
				{
					throw new AppException(Constants.ERROR_INVALID_CONFIRM);
				}
				else if (subscriberGoService.getNumOfQuestion() >= Integer.valueOf(product.getParameter("MaxQuestion", "10")))
				{
					order.getParameters().setString("NumOfCode", String.valueOf(subscriberGoService.getScore()));
					throw new AppException(Constants.ERROR_OVER_CONFIRM);
				}
				else if (!SubscriberProductImpl.isSendQuestion(order.getIsdn(), product.getProductId()))
				{
					throw new AppException(Constants.ERROR_INVALID_CONFIRM);
				}
				else
				{
					// Set quanlity
					order.setQuantity(subscriberGoService.getNumOfQuestion() + 1);
					order.getParameters().setLong("QuestionId", subscriberGoService.getLastQuestionId());
					order.getParameters().setLong("Score", subscriberGoService.getScore());
					order.getParameters().setLong("NumerOfQuestion", subscriberGoService.getNumOfQuestion() + 1);
					if (order.getActionType().equals(Constants.ACTION_BET))
					{
						order.getParameters().setLong("BetRate", Long.valueOf(product.getParameter("BetRate", "1")));
					}
					else
					{
						order.getParameters().setLong("BetRate", 1);
					}
					order.getParameters().setLong("RewardPoint", Long.valueOf(product.getParameter("RewardPoint", "100")));
					order.getParameters().setInteger("iscdr", 1);
				}
			}

			// Set subscriber type
			if (order.getSubscriberType() == Constants.UNKNOW_SUB_TYPE)
			{
				order.setSubscriberType(1);
			}

			/**
			 * Check if sub type is supported or not
			 */
			if (order.getSubscriberType() == Constants.PREPAID_SUB_TYPE)
			{
				String unsupported = orderRoute.getParameter("unsupport.prepaid", "false");
				if (unsupported.trim().toLowerCase().equals("true"))
					throw new AppException(Constants.ERROR_UNSUPPORT_PREPAID);
			}
			else if (order.getSubscriberType() == Constants.POSTPAID_SUB_TYPE)
			{
				String unsupported = orderRoute.getParameter("unsupport.postpaid", "false");
				if (unsupported.trim().toLowerCase().equals("true"))
					throw new AppException(Constants.ERROR_UNSUPPORT_POSTPAID);
			}

			// DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date currentDate = new Date();

			if ((order.getStatus() == Constants.ORDER_STATUS_DENIED)
					&& order.getCause()
							.equals(Constants.ERROR_NOT_ENOUGH_MONEY))
			{
				if (order.getActionType().equals(Constants.ACTION_TOPUP))
				{
					if (order.getRequestValue("first-action-type", "").equals(Constants.ACTION_SUBSCRIPTION))
					{
						order.setActionType(Constants.ACTION_SUBSCRIPTION);
					}
				}
				// Duymb add Autorenew for MAXI 24
				if (order.getActionType().equals(Constants.ACTION_AUTORENEW))
				{
					order.setActionType(Constants.ACTION_UNRENEW);
					order.setCause("");
					order.setStatus(Constants.ORDER_STATUS_PENDING);
				}
				// DuyMB add end.
				if (order.getActionType().equals(Constants.ACTION_SUBSCRIPTION))
				{
					if (subscriberProduct
							.getExpirationDate().before(currentDate))
					{
						if (subscriberProduct.getSupplierStatus() == Constants.SUPPLIER_ACTIVE_STATUS)
						{
							order.setActionType(Constants.ACTION_SUPPLIER_DEACTIVE);

							order.setCause("");

							order.setStatus(Constants.ORDER_STATUS_PENDING);
						}
					}
				}
			}
			else
			{
				checkSubscriberType(instance, product, order);
			}

			if (order.getActionType().equals(Constants.ACTION_SUBSCRIPTION)
					|| order.getActionType().equals(Constants.ACTION_SUPPLIER_DEACTIVE))
			{
				if (subscriberProduct == null)
					throw new AppException(Constants.ERROR_SUBSCRIPTION_NOT_FOUND);

				boolean subscriptionNeverExpire = product.getParameters().getBoolean("subscription.neverExpire", false);

				/**
				 * <code>
				 * if subscription.neverExpire=true
				 * {
				 * 		case graceDate != null && graceDate < currentDate && actionType is de-active
				 * 			cancel subscription;
				 * 		case graceDate != null && graceDate < currentDate && is barring
				 * 			cancel subscription;
				 * 		case graceDate == null && actionType is de-active
				 * 			cancel subscription;
				 * }
				 * </code>
				 */
				if (!subscriptionNeverExpire && ((subscriberProduct.getGraceDate() == null
						&& order.getActionType().equals(Constants.ACTION_SUPPLIER_DEACTIVE))
						|| (subscriberProduct.getGraceDate() != null
						&& subscriberProduct.getGraceDate().before(currentDate)
						&& (order.getActionType().equals(Constants.ACTION_SUPPLIER_DEACTIVE)
						|| subscriberProduct.isBarring())
						)))
				{
					order.setActionType(Constants.ACTION_CANCEL);

					order.setCause("");

					order.setStatus(Constants.ORDER_STATUS_PENDING);
				}
				else if (subscriberProduct.isBarring() && !orderRoute.isTopupEnable()
						&& order.getActionType().equals(Constants.ACTION_SUBSCRIPTION))
				{
					order.setCause(Constants.ERROR_REGISTERED);

					order.setStatus(Constants.ORDER_STATUS_DENIED);
				}
			}

			if (order.getActionType().equals(Constants.ACTION_SUPPLIER_DEACTIVE)
					&& subscriberProduct.isBarring())
			{
				order.setCause(Constants.ERROR_SUBSCRIPTION_NOT_FOUND);
				order.setDescription("Is already suspended.");
				order.setStatus(Constants.ORDER_STATUS_DENIED);
			}

			if (order.getStatus() != Constants.ORDER_STATUS_DENIED)
			{
				validate(instance, orderRoute, order);
			}
		}
		catch (Exception e)
		{
			error = e;
		}

		if (error != null)
		{
			order.setStatus(Constants.ORDER_STATUS_DENIED);

			if (error instanceof AppException)
			{
				order.setCause(error.getMessage());
			}
			else
			{
				order.setDescription(error.getMessage());
			}
		}

		/**
		 * In case of subscription (or de-active) <br/>
		 * Unregister subscription if subscriber is Retired(S3) <br />
		 */
		if (order.getActionType().equals(Constants.ACTION_SUBSCRIPTION)
				|| order.getActionType().equals(Constants.ACTION_SUPPLIER_DEACTIVE))
		{
			String currentState = "";
			try
			{
				currentState = getSubscriberState(instance, orderRoute, product, order);
			}
			catch (Exception e)
			{

			}

			if (currentState.equals(Constants.BALANCE_STATE_RETIRED_S3))
			{
				order.setActionType(Constants.ACTION_CANCEL);
				order.setDescription(order.getCause());
				order.setCause("");
				/**
				 * unregister for all subtype = prepaid subtype
				 */
				order.setSubscriberType(Constants.PREPAID_SUB_TYPE);
				order.setStatus(Constants.ORDER_STATUS_PENDING);
			}
		}

		if ((error != null) && !(error instanceof AppException))
		{
			throw error;
		}

		return order;
	}
}
