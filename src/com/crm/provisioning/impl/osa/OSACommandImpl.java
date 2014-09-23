/**
 * 
 */
package com.crm.provisioning.impl.osa;

import org.apache.axis.AxisFault;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.crm.kernel.message.Constants;
import com.crm.product.cache.ProductEntry;
import com.crm.product.cache.ProductFactory;
import com.crm.provisioning.cache.CommandEntry;
import com.crm.provisioning.cache.ProvisioningCommand;
import com.crm.provisioning.cache.ProvisioningFactory;
import com.crm.provisioning.impl.CommandImpl;
import com.crm.provisioning.message.VNMMessage;
import com.crm.provisioning.message.CommandMessage;
import com.crm.provisioning.thread.CommandInstance;
import com.crm.provisioning.util.CommandUtil;

import com.fss.util.AppException;

/**
 * @author ThangPV
 * 
 */
public class OSACommandImpl extends CommandImpl
{
	@Override
	public String getErrorCode(CommandInstance instance, CommandMessage request, Exception error)
	{
		String errorCode = Constants.ERROR;
		String errorDescription = "";
		
		if (!(error instanceof AxisFault) && !(error instanceof AppException))
		{
			instance.debugMonitor(error);
			return errorCode;
		}

		AxisFault axisError = (AxisFault) error;

		if (axisError.getFaultDetails() != null)
		{
			if (axisError.getFaultDetails().length > 0)
			{
				Element e = axisError.getFaultDetails()[0];
				String name = e.getLocalName();
				errorDescription = name;

				try
				{
					NodeList nodeList = e.getElementsByTagName("ExtraInformation");
					if (nodeList != null)
					{
						if (nodeList.getLength() > 0)
						{
							Element extraInfo = (Element) nodeList.item(0);
							if (extraInfo.getChildNodes().getLength() > 0)
							{
								errorDescription += ": " + extraInfo.getChildNodes().item(0).getNodeValue();
							}
						}
					}
				}
				catch (Exception ex)
				{
				}

				if (name == null)
				{
					errorCode = "osa-server-error";
					errorDescription = "OSA Server Error";
				}
				else if (name.equals("P_INVALID_USER"))
				{
					errorCode = "invalid-user";
				}
				else if (name.equals("P_INVALID_ACCOUNT"))
				{
					errorCode = "invalid-account";
				}
				else if (name.equals("P_INVALID_AMOUNT"))
				{
					errorCode = "invalid-amount";
				}
				else if (name.equals("P_INVALID_CURRENCY"))
				{
					errorCode = "invalid-currency";
				}
				else if (name.equals("P_INVALID_SESSION_ID"))
				{
					errorCode = "invalid-session";
				}
				else if (name.equals("P_INVALID_REQUEST_NUMBER"))
				{
					errorCode = "invalid-request-number";
				}
			}
		}

		request.setDescription(errorDescription);

		return errorCode;
	}

	public void creditAmount(
			CommandInstance instance, ProvisioningCommand provisioningCommand, CommandMessage request, String description)
			throws Exception
	{
		try
		{
			CommandEntry command = ProvisioningFactory.getCache().getCommand(request.getCommandId());

			ProductEntry product = ProductFactory.getCache().getProduct(request.getProductId());

			command.setMaxRetry(1);

			int amount = CommandUtil.getAmount(request.getAmount());

			if (amount < 0)
			{
				throw new AppException("invalid-amount");
			}
			else if (amount == 0)
			{
				instance.debugMonitor("Amount = 0, Do not credit amount.");
				throw new AppException("invalid-amount");
			}
			else
			{

				String descKey = "osa.credit.description." + request.getActionType();

				description = product.getParameter(descKey, "");
				if (description.equals(""))
				{
					description = product.getParameter("osa.credit.description", "");
				}
				if (description.equals(""))
				{
					description = "CP=" + request.getDescription().replaceAll("\\|", "");
				}

				description = description.replace("<ALIAS>", product.getIndexKey());

				if (instance.getDebugMode().equals("depend"))
				{
					simulation(instance, provisioningCommand, request);
				}
				else if (amount > 0)
				{
					request = OSAConnection.charging(instance, request, description, false);
				}

				request.getParameters().setBoolean("isPaid", (request.getStatus() == Constants.ORDER_STATUS_DENIED));
			}
		}
		catch (Exception e)
		{
			processError(instance, provisioningCommand, request, e);
		}
	}
	
	public void creditAmount(
			CommandInstance instance, ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception
	{
		try
		{
			CommandEntry command = ProvisioningFactory.getCache().getCommand(request.getCommandId());

			ProductEntry product = ProductFactory.getCache().getProduct(request.getProductId());

			command.setMaxRetry(1);

			int amount = CommandUtil.getAmount(request.getAmount());

			if (amount < 0)
			{
				throw new AppException("invalid-amount");
			}
			else if (amount == 0)
			{
				instance.debugMonitor("Amount = 0, Do not credit amount.");
				throw new AppException("invalid-amount");
			}
			else
			{
				String description = "";
				
				if (request.getActionType().equals("charge-core-balance") || request.getActionType().equals("online-prepaid-u"))
				{
					description = "CP=" + request.getDescription().replaceAll("\\|", "");
				}
				
				if (description.equals(""))
				{
					description = product.getParameter("osa.credit.description", "");
				}
				if (description.equals(""))
				{
					description = "Charging Gateway " + product.getAlias();
//					if (request.getActionType().equals(Constants.ACTION_SUBSCRIPTION))
//					{
//						description = "Cancel-Vas-MonthlyBilling: <ALIAS> fee";
//					}
//					else if (request.getActionType().equals("charge-core-balance"))
//					{
//						description = request.getParameters().getString("description", "");
//					}
//					else
//					{
//						description = "Cancel-Reserve <ALIAS> fee";
//					}
				}

				description = description.replace("<ALIAS>", product.getIndexKey());

				if (instance.getDebugMode().equals("depend"))
				{
					simulation(instance, provisioningCommand, request);
				}
				else if (amount > 0)
				{
					request = OSAConnection.charging(instance, request, description, false);
				}

				request.getParameters().setBoolean("isPaid", (request.getStatus() == Constants.ORDER_STATUS_DENIED));
			}
		}
		catch (Exception e)
		{
			processError(instance, provisioningCommand, request, e);
		}
	}

	public VNMMessage cancelDebit(
			CommandInstance instance, ProvisioningCommand provisioningCommand, CommandMessage request) throws Exception
	{
		VNMMessage result = CommandUtil.createVNMMessage(request);

		try
		{
			result.setRequest("cancelDebit");
			String description = "cancelDetbit";

			if (result.isPaid())
			{
				creditAmount(instance, provisioningCommand, result, description);
			}
		}
		catch (Exception e)
		{
			processError(instance, provisioningCommand, request, e);
		}

		return result;
	}

	public VNMMessage directDebit(CommandInstance instance, ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception
	{
		VNMMessage result = CommandUtil.createVNMMessage(request);

		if (instance.getDebugMode().equals("depend"))
		{
			simulation(instance, provisioningCommand, result);
		}
		else
		{
			try
			{
				CommandEntry command = ProvisioningFactory.getCache().getCommand(result.getCommandId());
				ProductEntry product = ProductFactory.getCache().getProduct(result.getProductId());

				command.setMaxRetry(1);

				int amount = CommandUtil.getAmount(result.getAmount());
				if (amount < 0)
				{
					throw new AppException("invalid-amount");
				}
				else if (amount == 0)
				{
					instance.debugMonitor("Amount = 0, Do not debit amount.");
					throw new AppException("invalid-amount");
				}
				else
				{
					String descKey = "osa.debit.description." + result.getActionType();

					String description = product.getParameter(descKey, "");
					if (description.equals(""))
					{
						description = product.getParameter("osa.debit.description", "");
					}
					if (description.equals(""))
					{
						description = "CP=" + request.getDescription().replaceAll("\\|", "");
//						if (result.getActionType().equals(Constants.ACTION_SUBSCRIPTION))
//						{
//
//							description = "Vas-MonthlyBilling: <ALIAS> fee";
//						}
//						else if (result.getActionType().equals("charge-core-balance") || result.getActionType().equals("offline-prepaid-d"))
//						{
//							description = result.getParameters().getString("description", "");
//						}
//						else
//						{
//							description = "Reserve <ALIAS> fee";
//						}
					}

					description = description.replace("<ALIAS>", product.getIndexKey());

					if (amount > 0)
					{
						request = OSAConnection.charging(instance, request, description, true);
					}

					result.setPaid(result.getStatus() != Constants.ORDER_STATUS_DENIED);
				}
			}
			catch (Exception e)
			{
				processError(instance, provisioningCommand, result, e);
			}
		}
		
		return result;
	}



	public VNMMessage subscription(
			CommandInstance instance, ProvisioningCommand provisioningCommand, CommandMessage request) throws Exception
	{
		VNMMessage result = CommandUtil.createVNMMessage(request);

		try
		{
			CommandEntry command = ProvisioningFactory.getCache().getCommand(result.getCommandId());

			ProductEntry product = ProductFactory.getCache().getProduct(result.getProductId());

			command.setMaxRetry(1);

			int amount = CommandUtil.getAmount(result.getAmount());

			if (amount < 0)
			{
				throw new AppException("invalid-amount");
			}
			else if (amount == 0)
			{
				throw new AppException("invalid-amount");
			}
			else
			{
				String descKey = "osa.debit.description." + result.getActionType();

				String description = product.getParameter(descKey, "");
				if (description.equals(""))
				{
					description = product.getParameter("osa.debit.description", "");
				}
				if (description.equals(""))
				{
					if (result.getActionType().equals(Constants.ACTION_SUBSCRIPTION))
					{
						description = "Vas-MonthlyBilling: <ALIAS> fee";
					}
					else
					{
						description = "Reserve <ALIAS> fee";
					}
				}

				description = description.replace("<ALIAS>", product.getIndexKey());

				if (instance.getDebugMode().equals("depend"))
				{
					simulation(instance, provisioningCommand, result);
				}
				else if (amount > 0)
				{
					request = OSAConnection.charging(instance, request, description, true);
				}

				result.setPaid(result.getStatus() != Constants.ORDER_STATUS_DENIED);
			}
		}
		catch (Exception e)
		{
			processError(instance, provisioningCommand, result, e);
		}

		return result;
	}
	
	public VNMMessage cgwDebit(CommandInstance instance, ProvisioningCommand provisioningCommand, CommandMessage request)
	throws Exception
	{
		VNMMessage result = CommandUtil.createVNMMessage(request);

		try
		{
			String prefix = "balance.charging.core";
			double amount = result.getParameters().getDouble(prefix + ".amount", 0);
			if(amount < 0)
			{ 
				amount = -amount;
			}
			
			result.setAmount(amount);
			
			String type = result.getParameters().getString(prefix + ".type");
			String description = "CP=" + result.getDescription().replaceAll("\\|", "");
			
			if(type.equals("add"))
			{
				creditAmount(instance, provisioningCommand, request, description);
			}
			else if (type.equals("deduct"))
			{
				result = directDebit(instance, provisioningCommand, result);
			}
			else
			{
				double avaibleAmount = result.getAvailableBalance();
				double _temp = 0;
				
				if(avaibleAmount < amount)
				{
					_temp = amount - avaibleAmount;
					result.setAmount(_temp);
					
					creditAmount(instance, provisioningCommand, request, description);
				}
				else if (avaibleAmount > amount)
				{
					_temp = avaibleAmount - amount;
					result.setAmount(_temp);
					
					directDebit(instance, provisioningCommand, result);
				}
				else
				{
					_temp = avaibleAmount;
				}
				
			}
			
			result.setPaid(true);
		}
		catch (Exception e)
		{
			processError(instance, provisioningCommand, result, e);
		}
		
		return result;
	}
	
	public VNMMessage cgwSubmodifyDebit(
			CommandInstance instance, ProvisioningCommand provisioningCommand, CommandMessage request) throws Exception
	{
		VNMMessage result = CommandUtil.createVNMMessage(request);

		try
		{
			
			double amount = 0;
			String _tempAmount = "";
			String[] balances = result.getSubmodifyBalance().split(",");
			String[] subAmount = result.getSubmodifyAmount().split(",");
			
			for(int i= 0; i< balances.length; i++)
			{
				if(balances[i].toLowerCase().equals("core"))
				{
					_tempAmount = subAmount[i];
					if(_tempAmount.startsWith("+") && _tempAmount.startsWith("-"))
					{
					amount = Double.parseDouble(subAmount[i].substring(1));
					}
					else
						
					{
						amount = Double.parseDouble(subAmount[i]);
					}
				}
			}
			
			if(amount < 0)
			{
				amount = -amount;
			}
			
			result.setAmount(amount);
			
			
			String description = "CP=" + result.getDescription();
			if(_tempAmount.startsWith("+"))
			{
				creditAmount(instance, provisioningCommand, request, description);
			}
			else if (_tempAmount.startsWith("-"))
			{
				result = directDebit(instance, provisioningCommand, result);
			}
			else
			{
				double avaibleAmount = result.getAvailableBalance();
				double _temp = 0;
				
				if(avaibleAmount < amount)
				{
					_temp = amount - avaibleAmount;
					result.setAmount(_temp);
					
					creditAmount(instance, provisioningCommand, request, description);
				}
				else if (avaibleAmount > amount)
				{
					_temp = avaibleAmount - amount;
					result.setAmount(_temp);
					
					directDebit(instance, provisioningCommand, result);
				}
				else
				{
					_temp = avaibleAmount;
				}
				
			}
		}
		catch (Exception e)
		{
			processError(instance, provisioningCommand, result, e);
		}

		
		return result;
	}
}
