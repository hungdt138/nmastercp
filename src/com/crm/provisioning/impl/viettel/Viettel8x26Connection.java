/**
 * 
 */
package com.crm.provisioning.impl.viettel;

import org.tempuri.WebServiceStub;
import org.tempuri.WebServiceStub.InsertMT;
import org.tempuri.WebServiceStub.InsertMTResponse;

import com.crm.provisioning.cache.ProvisioningConnection;
import com.crm.util.AppProperties;

/**
 * @author Hung
 * 
 */
public class Viettel8x26Connection extends ProvisioningConnection
{

	public String	op	= "";

	public void setParameters(AppProperties parameters) throws Exception
	{
		super.setParameters(parameters);
		op = getParameters().getString("operator", "vte26");

	}

	public boolean openConnection() throws Exception
	{
		return super.openConnection();
	}

	public String sendSMS(long moId, String srcNum,
			String dstNum, String cmdCode, String msgTitle, int msgType,
			String msgContent, int seqNum, long seqId, int isCDR) throws Exception
	{
		String result = "";

		InsertMT ins = null;
		try
		{
			ins = new InsertMT();
			ins.setCmdCode(cmdCode);
			ins.setDstNum(dstNum);
			ins.setIsCdr(isCDR);
			ins.setMoID((int) moId);
			ins.setMsgContent(msgContent);
			ins.setMsgTitle(msgTitle);
			ins.setMsgType(msgType);
			ins.setOperator(op);
			ins.setPassword(getPassword());
			ins.setSeqID((int) seqId);
			ins.setSeqNum(seqNum);
			ins.setSrcNum(srcNum);
			ins.setUserName(getUserName());

			WebServiceStub stub = new WebServiceStub(getHost());

			InsertMTResponse resp = stub.InsertMT(ins);

			result = resp.getInsertMTResult();

		}
		catch (Exception e)
		{
			throw e;
		}

		return result;
	}

	// public static void main(String[] args) throws Exception
	// {
	// Viettel8x26Connection connection = new Viettel8x26Connection();
	// String test = connection
	// .sendSMS(
	// 1,
	// "8626",
	// "84967289990",
	// "SS",
	// "",
	// 1,
	// "Chuc mung!Ban la nguoi co so diem cao nhat trong vong choi nay va da chien thang iPod sanh dieu.Chung toi se lien lac voi ban de nhan giai thuong",
	// 1, 0, 0);
	// }
}
