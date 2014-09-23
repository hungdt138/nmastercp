/**
 * 
 */
package com.crm.provisioning.impl.nmschargingapi;

import com.crm.provisioning.cache.ProvisioningCommand;
import com.crm.provisioning.impl.CommandImpl;
import com.crm.provisioning.message.CommandMessage;
import com.crm.provisioning.message.VNMMessage;
import com.crm.provisioning.thread.CommandInstance;

/**
 * @author hungdt
 *
 */
public class NMSCharingImpl extends CommandImpl
{
	public CommandMessage debitAmount(CommandInstance instance,
			ProvisioningCommand provisioningCommand, CommandMessage request)
			throws Exception {
		return request;
	}
}
