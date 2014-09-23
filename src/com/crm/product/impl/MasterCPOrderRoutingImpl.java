/**
 * 
 */
package com.crm.product.impl;

import java.util.Date;

import com.crm.kernel.message.Constants;
import com.crm.merchant.cache.MerchantEntry;
import com.crm.merchant.cache.MerchantFactory;
import com.crm.product.cache.ProductEntry;
import com.crm.product.cache.ProductFactory;
import com.crm.product.cache.ProductRoute;
import com.crm.provisioning.message.CommandMessage;
import com.crm.provisioning.thread.OrderRoutingInstance;
import com.crm.subscriber.bean.SubscriberProduct;
import com.crm.subscriber.impl.SubscriberProductImpl;
import com.crm.util.DateUtil;
import com.fss.util.AppException;

/**
 * @author HungDT
 * 
 */
public class MasterCPOrderRoutingImpl extends VNMOrderRoutingImpl
{
	public CommandMessage parser(OrderRoutingInstance instance,
			ProductRoute orderRoute, CommandMessage order) throws Exception
	{
		Exception error = null;

		ProductEntry product = null;

		SubscriberProduct subscriberProduct = null;

		MerchantEntry merchant = null;

		try
		{
			// check SMS syntax
			Date startTime = new Date();
			Date endTime = new Date();

			if (order.getChannel().equals("SMS"))
			{
				smsParser(instance, orderRoute, order);
			}

			// check duplicate request
			if (orderRoute.getDuplicateScan() > 0)
			{
				startTime = new Date();
				checkDuplicate(instance, orderRoute, order);

				endTime = new Date();
				instance.debugMonitor("Check duplicate(" + order.getIsdn() + ") cost time: " + (endTime
						.getTime() - startTime.getTime()) + "ms");
			}

			// check product in available list
			product = ProductFactory.getCache().getProduct(order.getProductId());

			merchant = MerchantFactory.getCache().getMerchant(product.getMerchantId());

			// get current subscriber product

			// Doi voi masterCP, khi thue bao ton tai tren he thong, update va
			// cho dang ki moi

			if (!product.isSubscription())
			{

			}
			else if (order.getSubProductId() == Constants.DEFAULT_ID)
			{
				subscriberProduct = SubscriberProductImpl.getActive(order
						.getIsdn(), order.getProductId());

			}
			else
			{
				subscriberProduct = SubscriberProductImpl.getProduct(order
						.getSubProductId());
			}


			// String contentType = order.getResponseValue("contentType", "2");
			//
			// order.setContentType(Integer.parseInt(contentType));

			// check action type
			checkActionType(instance, orderRoute, product, order, subscriberProduct);

			// // set content mode
			// if (order.getContentType() == 2 &&
			// order.getActionType().equals("delivery"))
			// {
			// order.setContentCode(Constants.CONTENT_CODE_MOBILE_TERMINATED_SMS);
			// }
			// else if (order.getContentType() == 2 &&
			// order.getActionType().equals("register"))
			// {
			// order.setContentCode(Constants.CONTENT_CODE_MOBILE_ORIGINATED_SMS);
			// }

			// set merchartId
			order.setMerchantId(product.getMerchantId());
			order.setOpId(product.getOpId());

			if (order.getSubscriberType() == Constants.UNKNOW_SUB_TYPE)
			{

				order.setSubscriberType(1);
			}
			Date currentDate = new Date();

			if ((order.getStatus() == Constants.ORDER_STATUS_DENIED) && order
					.getCause().equals(Constants.ERROR_NOT_ENOUGH_MONEY))
			{

				if (order.getActionType().equals(Constants.ACTION_TOPUP))
				{
					if (order.getRequestValue("first-action-type", "")
							.equals(Constants.ACTION_SUBSCRIPTION))
					{
						order.setActionType(Constants.ACTION_SUBSCRIPTION);
					}
				}

				if (order.getActionType().equals(Constants.ACTION_SUBSCRIPTION))
				{
					if (subscriberProduct.getExpirationDate()
							.before(currentDate))
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
				// checkSubscriberType(instance, product, order);
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

		if ((error != null) && !(error instanceof AppException))
		{
			throw error;
		}

		return order;
	}

	public void checkActionType(OrderRoutingInstance instance,
			ProductRoute orderRoute, ProductEntry product,
			CommandMessage order, SubscriberProduct subscriberProduct)
			throws Exception
	{
		Date now = new Date();

		try
		{
			if (subscriberProduct != null)
			{
				int remainDays = 0;
				if (subscriberProduct.getExpirationDate() != null)
				{
					remainDays = DateUtil.getDateDiff(now, subscriberProduct
							.getExpirationDate());
				}
				if (remainDays < 0)
				{
					remainDays = 0;
				}

				order.setResponseValue("service.activeDays", remainDays);
				if (subscriberProduct.getExpirationDate() != null)
				{
					order.setResponseValue("service.activeDate", subscriberProduct
							.getExpirationDate());
				}
			}

			if (subscriberProduct != null)
			{
				order.setSubProductId(subscriberProduct.getSubProductId());
			}

			String actionType = order.getActionType();

			// truong hop dang ky bang sms
			if (actionType.equals(Constants.ACTION_REGISTER) &&
					(subscriberProduct != null))
			{

				SubscriberProductImpl.unregister(order.getUserId(), order.getUserName(), subscriberProduct.getSubProductId(),
						order.getProductId());
				subscriberProduct = null;

			}
			// truong hop vms
			if (actionType.equals("vms-subrequest") && (subscriberProduct != null))
			{

				SubscriberProductImpl.unregister(order.getUserId(), order.getUserName(), subscriberProduct.getSubProductId(),
						order.getProductId());
				subscriberProduct = null;
			}
			// truong hop vina
			if (actionType.equals("vina-subrequest") && (subscriberProduct != null))
			{
				SubscriberProductImpl.unregister(order.getUserId(), order.getUserName(), subscriberProduct.getSubProductId(),
						order.getProductId());
				subscriberProduct = null;
			}

			if (product.isSubscription())
			{
				if ((subscriberProduct == null) || subscriberProduct.isCancel())
				{
					if (actionType.equals(Constants.ACTION_SUBSCRIPTION))
					{
						throw new AppException(Constants.ERROR_SUBSCRIPTION_NOT_FOUND);
					}
					else if (actionType.equals(Constants.ACTION_UNREGISTER))
					{
						// throw new AppException(
						// Constants.ERROR_UNREGISTERED);
						actionType = Constants.ERROR_UNREGISTERED;
					}
					else if (actionType.equals(Constants.ACTION_CANCEL))
					{
						throw new AppException(Constants.ERROR_SUBSCRIPTION_NOT_FOUND);
					}
					else if (actionType.equals(Constants.ACTION_TOPUP))
					{
						actionType = Constants.ACTION_REGISTER;
					}
				}
				else if (subscriberProduct != null)
				{
					if (orderRoute.isTopupEnable() && subscriberProduct
							.isBarring())
					{
						actionType = Constants.ACTION_TOPUP;
					}
					else if (actionType.equals(Constants.ACTION_SUBSCRIPTION))
					{
						if (DateUtil.compareDate(subscriberProduct
								.getExpirationDate(), now) >= 0)
						{
							throw new AppException(Constants.ERROR_REGISTERED);
						}
					}
				}
			}

			// get associate product
			if (actionType.equals(Constants.ACTION_REGISTER) || actionType
					.equals(Constants.ACTION_UPGRADE))
			{
				checkBlacklist(instance, product, order);

				checkUpgrade(instance, product, order);

				if (order.getAssociateProductId() != Constants.DEFAULT_ID)
				{
					actionType = Constants.ACTION_UPGRADE;
				}
			}

			order.setActionType(actionType);
		}
		catch (Exception e)
		{
			throw e;
		}
	}
}
