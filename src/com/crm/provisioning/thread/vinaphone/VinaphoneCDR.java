/**
 * 
 */
package com.crm.provisioning.thread.vinaphone;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.crm.provisioning.thread.CDRImpl;

/**
 * @author hungdt
 * 
 */
public class VinaphoneCDR {
	private int streamNo = 0;
	private String msgId = "";
	private String cpName = "";
	private String spId = "";
	private String serviceId = "";
	private String serviceName = "";
	private String productId = "";
	private String contentName = "";
	private String subContentName = "";
	private String subContentType = "";
	private String playType = "";
	// 0.Wap - 2.SMS - 3.MMS - 99999999. package
	private int serviceType = 0;
	/*
	 * Access channel: 0: WAP 1: WEB 2: SMS 3: USSD 4: IVR 100: SP website (API)
	 */
	private int accessChannel = 0;

	private String time_stamp = "";
	private String pkgSpId = "";
	private String pkgServiceId = "";
	private String pkgProductId = "";
	private String contentId = "";
	private String msisdn = "";
	private int originalfee = 0;
	private int fee = 0;

	// 0 postpaid , 1 prepaid
	private int payType = 0;

	private String reason = "";

	// 2 REG, 3 RENEWAL
	private int chargeType = 0;

	// 0 Thanh cong, #0 That bai
	private int chargeResult = 0;
	private int recNum = 0;

	private String productCode = "";

	/*
	 * Streamno|Msgid|Cpname|Spid|Serviceid|Servicename|productid|Contentname|
	 * Subcontenttype
	 * |Playtype|Accesschannel|Time_stamp|PkgSpid|Pkgserviceid|Pkgproductid
	 * |Contentid
	 * |Msisdn|Originalfee|Fee|Paytype|Reason|chargetype|Chargeresult|Recnum
	 */
	public static VinaphoneCDR createCDRFromFileString(String cdrContent) {
		if (cdrContent == null) {
			return null;
		}

		String[] contentElements = cdrContent.split("\\|");
		VinaphoneCDR cdr = new VinaphoneCDR();

		try {
			cdr.setStreamNo(Integer.parseInt(contentElements[0])); //streamno
			cdr.setMsgId(contentElements[1]); //MSGID
			cdr.setCpName(contentElements[2]); //cpname
			cdr.setSpId(contentElements[3]);// spID
			cdr.setServiceId(contentElements[4]); //serviceID
			cdr.setServiceName(contentElements[5]); //serviceName
			cdr.setProductId(contentElements[6]); //PRODUCTID_TELCO
			cdr.setContentName(contentElements[7]); // contentName
			cdr.setSubContentName(contentElements[8]); // subcontentName
			cdr.setPlayType(contentElements[9]); //playtype
			cdr.setAccessChannel(Integer.parseInt(contentElements[10])); //ACCESSCHANNEL
			cdr.setTime_stamp(contentElements[11]); //TIMESTAMP
			cdr.setPkgSpId(contentElements[12]); //PKGSPID
			cdr.setPkgServiceId(contentElements[13]); //PKGSERVICEID
			cdr.setPkgProductId(contentElements[14]); //PKGPRODUCTID
			cdr.setContentId(contentElements[15]); //contentId
			cdr.setMsisdn(contentElements[16]); //ISDN
			cdr.setOriginalfee(Integer.parseInt(contentElements[17]));//prediscountfee
			cdr.setFee(Integer.parseInt(contentElements[18])); //cost
			cdr.setPayType(Integer.parseInt(contentElements[19]));//subtype
			cdr.setReason(contentElements[20]);//reason
			cdr.setChargeType(Integer.parseInt(contentElements[21]));//chargeMode
			cdr.setChargeResult(Integer.parseInt(contentElements[22]));//CHARGERESULT
			//cdr.setRecNum(Integer.parseInt(contentElements[23]));//recnum
			cdr.setProductCode(CDRImpl.getProductId(contentElements[4]));//PRODUCTID
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return cdr;

	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public int getStreamNo() {
		return streamNo;
	}

	public void setStreamNo(int streamNo) {
		this.streamNo = streamNo;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getCpName() {
		return cpName;
	}

	public void setCpName(String cpName) {
		this.cpName = cpName;
	}

	public String getSpId() {
		return spId;
	}

	public void setSpId(String spId) {
		this.spId = spId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getContentName() {
		return contentName;
	}

	public void setContentName(String contentName) {
		this.contentName = contentName;
	}

	public String getSubContentName() {
		return subContentName;
	}

	public void setSubContentName(String subContentName) {
		this.subContentName = subContentName;
	}

	public String getSubContentType() {
		return subContentType;
	}

	public void setSubContentType(String subContentType) {
		this.subContentType = subContentType;
	}

	public String getPlayType() {
		return playType;
	}

	public void setPlayType(String playType) {
		this.playType = playType;
	}

	public int getServiceType() {
		return serviceType;
	}

	public void setServiceType(int serviceType) {
		this.serviceType = serviceType;
	}

	public int getAccessChannel() {
		return accessChannel;
	}

	public void setAccessChannel(int accessChannel) {
		this.accessChannel = accessChannel;
	}

	public String getTime_stamp() {
		return time_stamp;
	}

	public void setTime_stamp(String time_stamp) {
		this.time_stamp = time_stamp;
	}

	public String getPkgSpId() {
		return pkgSpId;
	}

	public void setPkgSpId(String pkgSpId) {
		this.pkgSpId = pkgSpId;
	}

	public String getPkgServiceId() {
		return pkgServiceId;
	}

	public void setPkgServiceId(String pkgServiceId) {
		this.pkgServiceId = pkgServiceId;
	}

	public String getPkgProductId() {
		return pkgProductId;
	}

	public void setPkgProductId(String pkgProductId) {
		this.pkgProductId = pkgProductId;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public int getOriginalfee() {
		return originalfee;
	}

	public void setOriginalfee(int originalfee) {
		this.originalfee = originalfee;
	}

	public int getFee() {
		return fee;
	}

	public void setFee(int fee) {
		this.fee = fee;
	}

	public int getPayType() {
		return payType;
	}

	public void setPayType(int payType) {
		this.payType = payType;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public int getChargeType() {
		return chargeType;
	}

	public void setChargeType(int chargeType) {
		this.chargeType = chargeType;
	}

	public int getChargeResult() {
		return chargeResult;
	}

	public void setChargeResult(int chargeResult) {
		this.chargeResult = chargeResult;
	}

	public int getRecNum() {
		return recNum;
	}

	public void setRecNum(int recNum) {
		this.recNum = recNum;
	}

	public String toString() {
		Class<? extends VinaphoneCDR> type = this.getClass();
		Method[] methods = type.getMethods();
		String returnString = "";
		for (int i = 0; i < methods.length; i++) {
			if (!methods[i].getName().startsWith("get")) {
				continue;
			}
			String member = "";
			try {
				member = methods[i].getName().substring(3) + "=";
				Object value = methods[i].invoke(this, new Object[] {});
				if (value instanceof Date || value instanceof Calendar) {
					member += (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"))
							.format(value);
				} else {
					member += value.toString();
				}
				member += " | ";
			} catch (Exception e) {
				member = "";
			}
			returnString += member;
		}
		return returnString.trim();
	}
}
