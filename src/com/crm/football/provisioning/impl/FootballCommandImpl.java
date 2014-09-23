package com.crm.football.provisioning.impl;

import java.util.ArrayList;
import java.util.List;

import com.crm.football.bean.FootballInfo;
import com.crm.football.sql.impl.FootballImpl;
import com.crm.kernel.message.Constants;
import com.crm.product.cache.ProductEntry;
import com.crm.product.cache.ProductFactory;
import com.crm.provisioning.cache.ProvisioningCommand;
import com.crm.provisioning.impl.CommandImpl;
import com.crm.provisioning.message.CommandMessage;
import com.crm.provisioning.thread.CommandInstance;
import com.fss.util.AppException;

public class FootballCommandImpl extends CommandImpl
{
	public CommandMessage registerFootball(CommandInstance instance, ProvisioningCommand provisioningCommand,
			CommandMessage request)
			throws Exception
	{
		CommandMessage result = request;

//		long keyId = 0;
//		String code = "";
//		ProductEntry product = ProductFactory.getCache().getProduct(result.getProductId());
//
//		try
//		{
//			keyId = Long.parseLong(result.getRequestValue("football.id", "0"));
//			code = result.getRequestValue("football.code", "");
//
//			long subsId = FootballImpl.register(result.getIsdn(), keyId, result.getServiceAddress(),
//					result.getProductId(), true, result.getKeyword());
//
//			if (subsId == 0)
//			{
//				throw new AppException(Constants.ERROR);
//			}
//
//			result.setRequestValue("football.subsId", subsId);
//
//			FootballImpl.addQueueWait(subsId, result.getIsdn(), result.getProductId(),
//						result.getServiceAddress(), keyId, true);
//
//			result.setCause(Constants.SUCCESS);
//		}
//		catch (Exception e)
//		{
//			processError(instance, provisioningCommand, result, e);
//		}
//
//		instance.debugMonitor("REGISTER - ADD QUEUE " + product.getAlias() + ":" + result.getIsdn() + "," + keyId
//				+ "," + code + ", " + result.getCause());
		return result;
	}

	public CommandMessage registerFootballWC(CommandInstance instance, ProvisioningCommand provisioningCommand,
			CommandMessage request)
			throws Exception
	{
		CommandMessage result = request;
//		ProductEntry product = ProductFactory.getCache().getProduct(result.getProductId());
//
//		try
//		{
//			List<FootballInfo> infos = FootballImpl.getAllTeam();
//			long subsId = 0;
//			long keyId = FootballImpl.getFirstMatchTeam();
//			for (FootballInfo info : infos)
//			{
//				long id = FootballImpl.register(result.getIsdn(), info.getId(), result.getServiceAddress(),
//						result.getProductId(), true, info.getCode());
//
//				if (keyId == info.getId())
//					subsId = id;
//
//				instance.debugMonitor("REGISTER" + product.getAlias() + ":" + result.getIsdn() + "," + info.getId()
//						+ "," + info.getCode());
//			}
//
//			FootballImpl.addQueueWait(subsId, result.getIsdn(), result.getProductId(),
//						result.getServiceAddress(), keyId, true);
//
//			result.setCause(Constants.SUCCESS);
//			instance.debugMonitor("ADD QUEUE" + product.getAlias() + ":" + result.getIsdn() + "," + keyId);
//		}
//		catch (Exception e)
//		{
//			processError(instance, provisioningCommand, result, e);
//		}
		return result;
	}

	public CommandMessage registerFootballwoAddQueue(CommandInstance instance, ProvisioningCommand provisioningCommand,
			CommandMessage request)
			throws Exception
	{
		CommandMessage result = request;

//		long keyId = 0;
//		String code = "";
//		ProductEntry product = ProductFactory.getCache().getProduct(result.getProductId());
//
//		try
//		{
//			keyId = Long.parseLong(result.getRequestValue("football.id", "0"));
//			code = result.getRequestValue("football.code", "");
//
//			long subsId = FootballImpl.register(result.getIsdn(), keyId, result.getServiceAddress(),
//					result.getProductId(), false, result.getKeyword());
//
//			if (subsId == 0)
//			{
//				throw new AppException(Constants.ERROR);
//			}
//
//			result.setRequestValue("football.subsId", subsId);
//
//			result.setCause(Constants.SUCCESS);
//			result.setStatus(Constants.ORDER_STATUS_PENDING);
//		}
//		catch (Exception e)
//		{
//			processError(instance, provisioningCommand, result, e);
//		}
//
//		instance.debugMonitor("REGISTE:" + product.getAlias() + ":" + result.getIsdn() + "," + keyId
//				+ "," + code + ", " + result.getCause());
		return result;
	}

	public CommandMessage upgradeFootball(CommandInstance instance, ProvisioningCommand provisioningCommand,
			CommandMessage request)
			throws Exception
	{
		CommandMessage result = request;

//		long keyId = 0;
//		String code = "";
//		ProductEntry product = ProductFactory.getCache().getProduct(result.getProductId());
//
//		String debugString = "";
//		try
//		{
//			keyId = Long.parseLong(result.getRequestValue("football.id", "0"));
//			code = result.getRequestValue("football.code", "");
//
//			if (result.getAssociateProductId() != Constants.DEFAULT_ID)
//			{
//				FootballImpl.unregister(result.getIsdn(), result.getAssociateProductId(), keyId);
//
//				debugString = "UNREGISTER: " + result.getAssociateProductId() + "," + keyId + " - ";
//			}
//
//			long subsId = FootballImpl.register(result.getIsdn(), keyId, result.getServiceAddress(),
//					result.getProductId(), true, result.getKeyword());
//
//			if (subsId == 0)
//			{
//				throw new AppException(Constants.ERROR);
//			}
//
//			result.setRequestValue("football.subsId", subsId);
//
//			FootballImpl.addQueueWait(subsId, result.getIsdn(), result.getProductId(),
//						result.getServiceAddress(), keyId, true);
//
//			result.setCause(Constants.SUCCESS);
//
//			debugString += "REGISTER & ADD QUEUE " + product.getAlias() + ":" + result.getIsdn() + "," + keyId
//					+ "," + code;
//		}
//		catch (Exception e)
//		{
//			processError(instance, provisioningCommand, result, e);
//		}
//
//		instance.debugMonitor(debugString + ", " + result.getCause());
		return result;
	}

	public CommandMessage unregisterFootball(CommandInstance instance, ProvisioningCommand provisioningCommand,
			CommandMessage request)
			throws Exception
	{
		CommandMessage result = request;
//		ProductEntry product = ProductFactory.getCache().getProduct(result.getProductId());
//		try
//		{
//			FootballImpl.unregister(result.getIsdn(), result.getProductId());
//			
//			FootballImpl.removeQueueWait(result.getIsdn(), result.getProductId());
//
//			result.setCause(Constants.SUCCESS);
//		}
//		catch (Exception e)
//		{
//			processError(instance, provisioningCommand, result, e);
//		}
//
//		instance.debugMonitor("UNREGISTER - " + product.getAlias() + ":" + result.getIsdn() + "," + result.getCause());
		return result;
	}

	public CommandMessage renewFootball(CommandInstance instance, ProvisioningCommand provisioningCommand,
			CommandMessage request)
			throws Exception
	{
		CommandMessage result = request;
//		ProductEntry product = ProductFactory.getCache().getProduct(result.getProductId());
//		long keyId = 0;
//		try
//		{
//			keyId = Long.parseLong(result.getRequestValue("football.id", "0"));
//			long subsId = Long.parseLong(result.getRequestValue("football.subsId", "0"));
//
//			FootballImpl.renew(subsId);
//
//			FootballImpl.addQueueWait(subsId, result.getIsdn(), result.getProductId(),
//					result.getServiceAddress(), keyId, false);
//
//			result.setCause(Constants.SUCCESS);
//		}
//		catch (Exception e)
//		{
//			processError(instance, provisioningCommand, result, e);
//		}
//
//		instance.debugMonitor("RENEW - " + product.getAlias() + ":" + result.getIsdn() + "," + keyId
//				+ "," + result.getCause());
		return result;
	}

}

