package com.crm.provisioning.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import com.crm.kernel.sql.Database;
import com.crm.provisioning.thread.vinaphone.VinaphoneCDR;
import com.crm.util.DateUtil;

public class CDRImpl {
	public static void insertCDR(CDR[] cdrs) throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();
			insertCDR(connection, cdrs);

		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
		}
	}

	public static void insertCDR(Connection connection, CDR[] cdrs)
			throws Exception {
		PreparedStatement stmtCDR = null;

		try {
			int count = 0;

			String SQL = "insert into TELCOCDR(id, streamno, createdate, timestamp, chargeresult, isdn, "
					+ "spid, serviceid, productid_telco, productid, chargemode, begintime, endtime, "
					+ "subtype, cost, b_isdn, status, telcoID, THIRDPARTY,times,uplinkvolume,downlinkvolume,preDiscountFee,spBenifitRate"
					+ ",sPBenifitFee,apn,ggsnId,pKgSpId,pKgServiceId,pKgProductId,serviceCategory,contentProvision,orderDate)"
					+ " VALUES(CDR_SEQ.nextVal,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1,1,?,?,?,?,?,?,?,?,?,?,?,?,?,?,sysdate)";

			stmtCDR = connection.prepareStatement(SQL);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			for (CDR cdr : cdrs) {
				if (!cdr.getMsIsdn().equals("") || cdr.getMsIsdn() != null
						|| cdr.getTimeStamp().length() >= 14) {
					stmtCDR.setString(1, cdr.getStreamNo());
					stmtCDR.setTimestamp(2, DateUtil.getTimestampSQL(sdf
							.parse(cdr.getTimeStamp())));
					stmtCDR.setString(3, cdr.getTimeStamp());
					stmtCDR.setString(4, cdr.getChargeResult());
					stmtCDR.setString(5, cdr.getMsIsdn());
					stmtCDR.setString(6, cdr.getSpID());
					stmtCDR.setString(7, cdr.getSpID() + cdr.getServiceID());
					stmtCDR.setString(8, cdr.getProductID_telco());
					stmtCDR.setString(9, cdr.getProductId());
					stmtCDR.setString(10, cdr.getChargeMode());
					stmtCDR.setString(11, cdr.getBeginTime());
					stmtCDR.setString(12, cdr.getEndTime());
					stmtCDR.setString(13, cdr.getPayType());
					stmtCDR.setString(14, cdr.getCost());
					stmtCDR.setString(15, cdr.getB_Isdn());
					stmtCDR.setString(16, cdr.getThirdParty());
					stmtCDR.setInt(17, cdr.getTimes());
					stmtCDR.setInt(18, cdr.getUplinkVolume());
					stmtCDR.setInt(19, cdr.getDownlinkVolume());
					stmtCDR.setInt(20, cdr.getPreDiscountFee());
					stmtCDR.setInt(21, cdr.getSpBenifitRate());
					stmtCDR.setInt(22, cdr.getsPBenifitFee());
					stmtCDR.setString(23, cdr.getApn());
					stmtCDR.setString(24, cdr.getGgsnId());
					stmtCDR.setString(25, cdr.getpKgSpId());
					stmtCDR.setString(26, cdr.getpKgServiceId());
					stmtCDR.setString(27, cdr.getpKgProductId());
					stmtCDR.setString(28, cdr.getServiceCategory());
					stmtCDR.setString(29, cdr.getContentProvision());

					stmtCDR.addBatch();
					count++;

					if (count >= 50) {
						stmtCDR.executeBatch();
						count = 0;
					}

				} else {
					System.out.println("LOI ROI " + cdr.getStreamNo());
				}
			}
			if (count > 0) {
				stmtCDR.executeBatch();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtCDR);
		}

	}

	public static void insertCDRVinaphone(VinaphoneCDR[] cdrs) throws Exception {
		Connection connection = null;
		PreparedStatement stmtCDR = null;
		try {
			int count = 0;

			String sql = "insert into TELCOCDR(id, streamno, createdate, orderdate, msgId, cpname, spId, serviceId, "
					+ "servicename, productId_telco, contentName, subcontentName, playtype, accessChannel, timestamp, "
					+ "pkgspid, pkgserviceid, pkgproductid, contentid, isdn, prediscountfee, cost, subtype, reason, "
					+ "chargeMode, chargeResult, recnum, productId, telcoId) "
					+ "values "
					+ "(CDR_SEQ.nextVal,?,?,sysdate,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			connection = Database.getConnection();
			stmtCDR = connection.prepareStatement(sql);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			for (VinaphoneCDR cdr : cdrs) {
				if (!cdr.getMsisdn().equals("") || cdr.getMsisdn() != null
						|| cdr.getTime_stamp().length() >= 14) {
					stmtCDR.setInt(1, cdr.getStreamNo());
					stmtCDR.setTimestamp(2, DateUtil.getTimestampSQL(sdf
							.parse(cdr.getTime_stamp())));
					stmtCDR.setString(3, cdr.getMsgId());
					stmtCDR.setString(4, cdr.getCpName());
					stmtCDR.setString(5, cdr.getSpId());
					stmtCDR.setString(6, cdr.getServiceId());
					stmtCDR.setString(7, cdr.getServiceName());
					stmtCDR.setString(8, cdr.getProductId());
					stmtCDR.setString(9, cdr.getContentName());
					stmtCDR.setString(10, cdr.getContentName());
					stmtCDR.setString(11, cdr.getPlayType());
					stmtCDR.setInt(12, cdr.getAccessChannel());
					stmtCDR.setString(13, cdr.getTime_stamp());
					stmtCDR.setString(14, cdr.getPkgSpId());
					stmtCDR.setString(15, cdr.getPkgServiceId());
					stmtCDR.setString(16, cdr.getPkgProductId());
					stmtCDR.setString(17, cdr.getContentId());
					stmtCDR.setString(18, cdr.getMsisdn());
					stmtCDR.setInt(19, cdr.getOriginalfee());
					stmtCDR.setInt(20, cdr.getFee());
					stmtCDR.setInt(21, cdr.getPayType());
					stmtCDR.setString(22, cdr.getReason());
					stmtCDR.setInt(23, cdr.getChargeType());
					stmtCDR.setInt(24, cdr.getChargeResult());
					stmtCDR.setInt(25, cdr.getRecNum());
					stmtCDR.setString(26, cdr.getProductCode());
					stmtCDR.setInt(27, 2);// telcoID VNP = 2;

					stmtCDR.addBatch();
					count++;

					if (count >= 50) {
						stmtCDR.executeBatch();
						count = 0;

					}
				}

			}

			if (count > 0) {
				stmtCDR.executeBatch();
			}

		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtCDR);
			Database.closeObject(connection);
		}
	}

	public static String getProductId(String serviceId) throws Exception {
		String productId = "0";

		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			connection = Database.getConnection();
			String sql = "select alias_ from productEntry where productId = (select productId from productConfig where properties like ?)";
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, "%" + serviceId + "%");
			rs = stmt.executeQuery();
			if (rs.next()) {
				productId = rs.getString("alias_");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
			Database.closeObject(stmt);
			Database.closeObject(rs);
		}

		return productId;
	}

	public static void updateExportStatus(int status, long orderId)
			throws Exception {
		PreparedStatement stmtOrder = null;

		Connection connection = null;
		try {
			connection = Database.getConnection();

			String sql = "Update telcocdr Set status = ? Where id = ? ";

			stmtOrder = connection.prepareStatement(sql);

			stmtOrder.setInt(1, status);
			stmtOrder.setLong(2, orderId);

			stmtOrder.execute();
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtOrder);
			Database.closeObject(connection);
		}
	}

}
