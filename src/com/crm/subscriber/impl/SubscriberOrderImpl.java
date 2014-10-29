package com.crm.subscriber.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.crm.kernel.message.Constants;
import com.crm.question.bean.ContentQuestion;
import com.crm.subscriber.bean.SubscriberOrder;
import com.crm.kernel.sql.Database;
import com.crm.util.DateUtil;
import com.fss.util.AppException;

public class SubscriberOrderImpl {
	/**
	 * TODO performance Test
	 * 
	 */
	// private static long sleepTime = 1000L;

	public static SubscriberOrder getOrder(ResultSet rsOrder) throws Exception {
		SubscriberOrder result = new SubscriberOrder();

		try {
			result.setUserId(rsOrder.getLong("userId"));
			result.setUserName(Database.getString(rsOrder, "userName"));

			result.setSubscriberId(rsOrder.getLong("subscriberId"));
			result.setSubProductId(rsOrder.getLong("subProductId"));
			result.setProductId(rsOrder.getLong("productId"));

			result.setSubscriberType(rsOrder.getInt("subscriberType"));
			result.setIsdn(Database.getString(rsOrder, "isdn"));
			result.setShipTo(Database.getString(rsOrder, "shippingTo"));

			result.setOrderDate(rsOrder.getTime("orderDate"));
			result.setOrderId(rsOrder.getLong("orderId"));
			result.setOrderNo(rsOrder.getString("orderNo"));
			result.setCycleDate(rsOrder.getTimestamp("cycleDate"));

			result.setStatus(rsOrder.getInt("status"));
		} catch (Exception e) {
			throw e;
		}

		return result;
	}

	public static SubscriberOrder getOrder(Connection connection, long orderId,
			Date orderDate) throws Exception {
		PreparedStatement stmtOrder = null;
		ResultSet rsOrder = null;

		SubscriberOrder result = null;

		try {
			String SQL = "Select * From SubscriberOrder Where orderId = ? and orderDate >= trunc(?) and orderDate < (trunc(?) + 1) ";

			stmtOrder = connection.prepareStatement(SQL);
			stmtOrder.setLong(1, orderId);
			stmtOrder.setTimestamp(2, DateUtil.getTimestampSQL(orderDate));
			stmtOrder.setTimestamp(3, DateUtil.getTimestampSQL(orderDate));

			rsOrder = stmtOrder.executeQuery();

			if (rsOrder.next()) {
				result = getOrder(rsOrder);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(rsOrder);
			Database.closeObject(stmtOrder);
		}

		return result;
	}

	public static int getPendingOrder(Connection connection, String isdn,
			long productId, Date orderDate, int duplicateScan) throws Exception {
		// SubscriberOrder order = null;

		PreparedStatement stmtOrder = null;
		ResultSet rsOrder = null;

		if (duplicateScan <= 0) {
			return 0;
		}
		int count = 0;
		try {
			/**
			 * Change to select count(*)
			 */
			String SQL = "Select count(*) total "
					+ "   From 	SubscriberOrder "
					+ "   Where	isdn = ? and productId = ? and status = ? "
					+ "      	and orderDate >= (sysdate - ? / 86400) and orderDate <= sysdate ";

			stmtOrder = connection.prepareStatement(SQL);

			stmtOrder.setString(1, isdn);
			stmtOrder.setLong(2, productId);
			stmtOrder.setInt(3, Constants.ORDER_STATUS_PENDING);

			// stmtOrder.setInt(4, delta);
			stmtOrder.setInt(4, duplicateScan);

			rsOrder = stmtOrder.executeQuery();

			if (rsOrder.next()) {
				// order = getOrder(rsOrder);
				count = rsOrder.getInt("total");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(rsOrder);
			Database.closeObject(stmtOrder);
		}

		return count;
	}

	public static boolean isDuplicatedOrder(Connection connection, String isdn,
			long productId, Date orderDate, int duplicateScan) throws Exception {
		if (duplicateScan <= 0) {
			return false;
		} else {
			int count = getPendingOrder(connection, isdn, productId, orderDate,
					duplicateScan);

			return (count > 1);
		}
	}

	public static boolean isDuplicatedOrder(String isdn, long productId,
			Date orderDate, int duplicateScan) throws Exception {
		Connection connection = null;

		try {
			/**
			 * TODO: performance test
			 */
			// Thread.sleep(sleepTime);
			// return false;

			if (duplicateScan <= 0) {
				return false;
			}

			connection = Database.getConnection();

			return isDuplicatedOrder(connection, isdn, productId, orderDate,
					duplicateScan);
		} finally {
			Database.closeObject(connection);
		}
	}

	public static int getRegisteredOrder(Connection connection, String isdn,
			long productId, Date orderDate) throws Exception {
		PreparedStatement stmtOrder = null;
		ResultSet rsOrder = null;

		int total = 0;

		try {
			String SQL = "Select count(*) total "
					+ "   From 	SubscriberOrder "
					+ "   Where	isdn = ? and productId = ? and status = ? and orderType in (?, ?, ?, ?, ?, ?) "
					+ "      	and orderDate >= trunc(?) and orderDate < (trunc(?) + 1) ";

			// String SQL = "Select count(*) total "
			// + "   From 	SubscriberOrder "
			// +
			// "   Where	isdn = ? and productId = ? and status = ? and orderType in (?, ?, ?) "
			// +
			// "      	and orderDate >= trunc(to_date(?, 'DD/MM/SYYYY HH24:MI:SS')) "
			// +
			// " 		and orderDate < (trunc(to_date(?, 'DD/MM/SYYYY HH24:MI:SS')) + 1) ";

			stmtOrder = connection.prepareStatement(SQL);

			// SimpleDateFormat sdf = new
			// SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			stmtOrder.setString(1, isdn);
			stmtOrder.setLong(2, productId);
			stmtOrder.setInt(3, Constants.ORDER_STATUS_APPROVED);
			stmtOrder.setString(4, Constants.ACTION_REGISTER);
			stmtOrder.setString(5, Constants.ACTION_TOPUP);
			stmtOrder.setString(6, Constants.ACTION_UPGRADE);
			stmtOrder.setString(7, Constants.ACTION_AUTORENEW);
			stmtOrder.setString(8, Constants.ACTION_ADVERTISING);
			stmtOrder.setString(9, Constants.ACTION_REGISTER_AFTER_18H);
			// stmtOrder.setString(7, sdf.format(orderDate));
			// stmtOrder.setString(8, sdf.format(orderDate));
			stmtOrder.setTimestamp(10, DateUtil.getTimestampSQL(orderDate));
			stmtOrder.setTimestamp(11, DateUtil.getTimestampSQL(orderDate));

			rsOrder = stmtOrder.executeQuery();

			if (rsOrder.next()) {
				total = rsOrder.getInt("total");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(rsOrder);
			Database.closeObject(stmtOrder);
		}

		return total;
	}

	public static int getRegisteredOrder(String isdn, long productId,
			Date orderDate) throws Exception {
		Connection connection = null;

		try {
			/**
			 * TODO: For performance test
			 */
			// Thread.sleep(sleepTime);
			// return 0;
			connection = Database.getConnection();

			return getRegisteredOrder(connection, isdn, productId, orderDate);
		} finally {
			Database.closeObject(connection);
		}
	}

	public static SubscriberOrder createOrder(Connection connection,
			long userId, String userName, Date orderDate, String orderType,
			long subscriberId, String isdn, int subscriberType,
			long subProductId, long productId, double price, int quantity,
			double discount, double amount, double score, String cause,
			int status, String channel, String serviceAddr, long merchantId,
			long opId, long orderNo, String content, long agentId,
			String telcoServiceId, int motype) throws Exception {
		SubscriberOrder order = null;

		PreparedStatement stmtOrder = null;

		try {
			Date now = new Date();
			Date cycleDate = SubscriberEntryImpl.getCycleDate(now);

			long orderId = Database.getSequence(connection, "order_seq");

			/**
			 * Edited by NamTA 2013/04/23
			 */
			String SQL = "Insert into SubscriberOrder "
					+ "		(orderId, userId, userName, createDate, modifiedDate, orderType, orderDate, cycleDate "
					+ "		, subscriberId, subProductId, productId, isdn, subscriberType "
					+ "		, offerPrice, price, quantity, discount, amount, score, status, cause, channel,serviceaddr,merchantId,telcoid, orderNo, shippingto, agentId, exportstatus,TELCOSSERVICEID, DELIVERYCOUNTER, motype) "
					+ " Values " + "		(?, ?, ?, sysDate, sysDate, ?, ?, ? "
					+ "		, ?, ?, ?, ?, ? "
					+ "		, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,?,1,?,0,?) ";

			stmtOrder = connection.prepareStatement(SQL);

			stmtOrder.setLong(1, orderId);
			stmtOrder.setLong(2, userId);
			stmtOrder.setString(3, userName);

			// stmtOrder.setTime(4, DateUtil.getTimeSQL(orderDate));
			stmtOrder.setString(4, orderType);
			stmtOrder.setTime(5, DateUtil.getTimeSQL(orderDate));
			stmtOrder.setTimestamp(6, DateUtil.getTimestampSQL(cycleDate));

			stmtOrder.setLong(7, subscriberId);
			stmtOrder.setLong(8, subProductId);
			stmtOrder.setLong(9, productId);
			stmtOrder.setString(10, isdn);
			stmtOrder.setInt(11, subscriberType);

			stmtOrder.setDouble(12, 0);
			stmtOrder.setDouble(13, price);
			stmtOrder.setDouble(14, quantity);
			stmtOrder.setDouble(15, discount);
			stmtOrder.setDouble(16, amount);
			stmtOrder.setDouble(17, score);

			stmtOrder.setInt(18, status);
			stmtOrder.setString(19, cause);
			stmtOrder.setString(20, channel);
			stmtOrder.setString(21, serviceAddr);
			stmtOrder.setLong(22, merchantId);
			stmtOrder.setLong(23, opId);
			stmtOrder.setLong(24, orderNo);
			stmtOrder.setString(25, content);
			stmtOrder.setLong(26, agentId);
			stmtOrder.setString(27, telcoServiceId);
			stmtOrder.setInt(28, motype);

			stmtOrder.execute();

			// bind order
			order = new SubscriberOrder();

			order.setUserId(userId);
			order.setUserName(userName);

			order.setSubscriberId(subscriberId);
			order.setSubProductId(subProductId);
			order.setProductId(productId);

			order.setSubscriberType(subscriberType);
			order.setIsdn(isdn);
			// order.setShipTo(shipTo);

			order.setOrderDate(now);
			order.setOrderId(orderId);
			// order.setOrderNo(orderNo);
			order.setCycleDate(cycleDate);

			order.setStatus(status);
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtOrder);
		}

		return order;
	}

	public static SubscriberOrder createOrder(long userId, String userName,
			Date orderDate, String orderType, long subscriberId, String isdn,
			int subscriberType, long subProductId, long productId,
			double price, int quantity, double discount, double amount,
			double score, String cause, int status, String serviceAddr,
			long merchantId, int opId, int orderNo, String content,
			long agentId, String telcoServiceId, int motype) throws Exception {
		return createOrder(userId, userName, orderDate, orderType,
				subscriberId, isdn, subscriberType, subProductId, productId,
				price, quantity, discount, amount, score, cause, status, "",
				serviceAddr, merchantId, opId, orderNo, content, agentId,
				telcoServiceId, motype);

	}

	public static SubscriberOrder createOrder(long userId, String userName,
			Date orderDate, String orderType, long subscriberId, String isdn,
			int subscriberType, long subProductId, long productId,
			double price, int quantity, double discount, double amount,
			double score, String cause, int status, String channel,
			String serviceAddr, long merchantId, long opId, long orderNo,
			String content, long agentId, String telcoServiceId, int motype)
			throws Exception {
		Connection connection = null;
		long startTime = System.currentTimeMillis();
		try {
			connection = Database.getConnection();

			return createOrder(connection, userId, userName, orderDate,
					orderType, subscriberId, isdn, subscriberType,
					subProductId, productId, price, quantity, discount, amount,
					score, cause, status, channel, serviceAddr, merchantId,
					opId, orderNo, content, agentId, telcoServiceId, motype);
		} finally {
			Database.closeObject(connection);
			long endTime = System.currentTimeMillis();
			// if ((endTime - startTime) > Database.getDatabaseHigh())
			// {
			// Database.processAlarmDB(startTime, endTime, orderDate, productId,
			// isdn, "Insert Order");
			// }
		}
	}

	public static void validateIsdn(String isdn, String table) throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();

			String sql = "select count(*) total from " + table
					+ " where isdn = ?";

			PreparedStatement stm = connection.prepareStatement(sql);

			stm.setString(1, isdn);

			ResultSet rs = stm.executeQuery();

			if (rs.next()) {
				if (rs.getInt("total") > 0)
					return;
			}

			throw new AppException(Constants.ERROR_INVALID_OWNER);
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
		}
	}

	public static double getChargingMoney(String isdn, String table,
			Date startDate) throws Exception {
		double money = 0;
		Connection connection = null;

		try {
			connection = Database.getConnection();

			String sql = "select FACE_VALUE from " + table
					+ " where mdn = ? and recharge_date > ?";

			PreparedStatement stm = connection.prepareStatement(sql);

			stm.setString(1, isdn);
			stm.setTimestamp(2, DateUtil.getTimestampSQL(startDate));

			ResultSet rs = stm.executeQuery();

			while (rs.next()) {
				money += rs.getDouble("FACE_VALUE");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
		}

		return money;
	}

	public static void updateDeliveryStatus(long orderId, String status,
			String sId) throws Exception {
		PreparedStatement stmtOrder = null;
		Connection connection = null;
		try {

			String sql = "Update SubscriberOrder set delivery_status = ?, TELCOSSERVICEID = ?, MODIFIEDDATE = sysdate, EXPORTSTATUS = 1 where orderId = ?";
			connection = Database.getConnection();
			stmtOrder = connection.prepareStatement(sql);
			stmtOrder.setString(1, status);
			stmtOrder.setString(2, sId);
			stmtOrder.setLong(3, orderId);

			stmtOrder.executeUpdate();

		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtOrder);
			Database.closeObject(connection);
		}
	}

	public static void updateDesc(Connection connection, long orderId,
			String desc, Date orderDate) throws Exception {
		PreparedStatement stmtOrder = null;

		try {
			String sql = "Update SubscriberOrder "
					+ "   Set cpurlrequest = ? "
					+ "Where orderDate >= trunc(?) and orderDate < (trunc(?) + 1) and orderId = ? ";

			stmtOrder = connection.prepareStatement(sql);

			stmtOrder.setString(1, desc);
			stmtOrder.setTimestamp(2, DateUtil.getTimestampSQL(orderDate));
			stmtOrder.setTimestamp(3, DateUtil.getTimestampSQL(orderDate));
			stmtOrder.setLong(4, orderId);

			stmtOrder.execute();
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtOrder);
		}
	}

	public static void updateExportStatus(int status, long orderId)
			throws Exception {
		PreparedStatement stmtOrder = null;

		Connection connection = null;
		try {
			connection = Database.getConnection();

			String sql = "Update SubscriberOrder Set exportStatus = ? Where orderId = ? ";

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

	/**
	 * Cáº­p nháº­t ID cáº©u há»�i
	 * 
	 * @param orderId
	 * @param questionId
	 * @throws Exception
	 */
	public static void updateQuestionId(long orderId, long questionId)
			throws Exception {
		PreparedStatement stmtOrder = null;
		Connection connection = null;
		try {

			String sql = "Update SubscriberOrder set questionId = ?, MODIFIEDDATE = sysdate where orderId = ?";
			connection = Database.getConnection();
			stmtOrder = connection.prepareStatement(sql);
			stmtOrder.setLong(1, questionId);
			stmtOrder.setLong(2, orderId);

			stmtOrder.executeUpdate();

		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtOrder);
			Database.closeObject(connection);
		}
	}

	public static List<ContentQuestion> getListQuestionIdFromOrder(String isdn,
			long productId) throws Exception {
		ContentQuestion content = null;
		PreparedStatement stmtOrder = null;
		ResultSet rsOrder = null;
		Connection connection = null;
		List<ContentQuestion> lst = new ArrayList<ContentQuestion>();
		try {
			String sql = "select A.* from productContent A, SubscriberOrder B where A.contentId = B.questionId and B.isdn = ? and (B.ordertype = ? "
					+ "or B.ordertype = ?) and B.productId = ? and B.questionId is not null and B.questionId <> 0"
					+ " order by B.orderDate desc";
			connection = Database.getConnection();
			stmtOrder = connection.prepareStatement(sql);
			stmtOrder.setString(1, isdn);
			stmtOrder.setString(2, Constants.ACTION_REGISTER);
			stmtOrder.setString(3, Constants.ACTION_ANSWER);
			stmtOrder.setLong(4, productId);
			rsOrder = stmtOrder.executeQuery();

			while (rsOrder.next()) {
				content = new ContentQuestion();
				content.setContentId(rsOrder.getLong("contentId"));
				content.setContent(rsOrder.getString("content"));
				content.setAnswer(rsOrder.getString("answer"));
				lst.add(content);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtOrder);
			Database.closeObject(connection);
			Database.closeObject(rsOrder);
		}

		return lst;
	}

	// check 15th MO
	public static List<ContentQuestion> getListQuestionIdFromOrder(String isdn)
			throws Exception {
		ContentQuestion content = null;
		PreparedStatement stmtOrder = null;
		ResultSet rsOrder = null;
		Connection connection = null;
		List<ContentQuestion> lst = new ArrayList<ContentQuestion>();
		try {
			String sql = "select A.* from productContent A, SubscriberOrder B where A.contentId = B.questionId and B.isdn = ? and (B.ordertype = ? "
					+ "or B.ordertype = ?) and B.questionId is not null and B.questionId <> 0 and B.createDate >= trunc(sysdate)"
					+ "order by B.orderDate desc";
			connection = Database.getConnection();
			stmtOrder = connection.prepareStatement(sql);
			stmtOrder.setString(1, isdn);
			stmtOrder.setString(2, Constants.ACTION_REGISTER);
			stmtOrder.setString(3, Constants.ACTION_ANSWER);

			rsOrder = stmtOrder.executeQuery();

			while (rsOrder.next()) {
				content = new ContentQuestion();
				content.setContentId(rsOrder.getLong("contentId"));
				content.setContent(rsOrder.getString("content"));
				content.setAnswer(rsOrder.getString("answer"));
				lst.add(content);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtOrder);
			Database.closeObject(connection);
			Database.closeObject(rsOrder);
		}

		return lst;
	}

	public static int getTotalQuestion(String isdn, long productId)
			throws Exception {
		int count = 0;
		PreparedStatement stmtOrder = null;
		ResultSet rsOrder = null;
		Connection connection = null;
		try {
			String sql = "select count(*) from SubscriberOrder where isdn = ? and (ordertype = ? or ordertype = ?) and productId = ? and status = 0 and orderDate = trunc(sysdate)";
			connection = Database.getConnection();
			stmtOrder = connection.prepareStatement(sql);
			stmtOrder.setString(1, isdn);
			stmtOrder.setString(2, Constants.ACTION_ANSWER);
			stmtOrder.setString(3, Constants.ACTION_REGISTER);
			stmtOrder.setLong(4, productId);

			rsOrder = stmtOrder.executeQuery();

			if (rsOrder.next()) {
				count = rsOrder.getInt(1);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtOrder);
			Database.closeObject(connection);
			Database.closeObject(rsOrder);
		}

		return count;
	}

	public static long getQuestionId(String isdn, long productId)
			throws Exception {
		long qId = 0;
		PreparedStatement stmtOrder = null;
		ResultSet rsOrder = null;
		Connection connection = null;
		try {
			String sql = "select * from (select * from SubscriberOrder where isdn = ? and (ordertype = ? or ordertype = ?)"
					+ " and productId = ? and questionId is not null and questionId <> 0 order by MODIFIEDDATE desc) where rownum = 1";
			connection = Database.getConnection();
			stmtOrder = connection.prepareStatement(sql);
			stmtOrder.setString(1, isdn);
			stmtOrder.setString(2, Constants.ACTION_ANSWER);
			stmtOrder.setString(3, Constants.ACTION_REGISTER);
			stmtOrder.setLong(4, productId);
			rsOrder = stmtOrder.executeQuery();

			while (rsOrder.next()) {
				qId = rsOrder.getLong("questionId");

			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtOrder);
			Database.closeObject(connection);
			Database.closeObject(rsOrder);
		}

		return qId;
	}

	public static void updateMTCouter(long orderId) throws Exception {
		PreparedStatement stmtOrder = null;
		Connection connection = null;
		try {

			String sql = "Update SubscriberOrder set DELIVERYCOUNTER = DELIVERYCOUNTER + 1, MODIFIEDDATE = sysdate where orderId = ?";
			connection = Database.getConnection();
			stmtOrder = connection.prepareStatement(sql);
			// stmtOrder.setInt(1, deliveryCouter);
			stmtOrder.setLong(1, orderId);

			stmtOrder.executeUpdate();

		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtOrder);
			Database.closeObject(connection);
		}
	}

	public static void insertOTPQueue(String isdn, long productId)
			throws Exception {
		PreparedStatement stmtOrder = null;
		Connection connection = null;
		try {
			String sql = "insert into otpqueue(isdn, productId) values (?,?)";
			connection = Database.getConnection();
			stmtOrder = connection.prepareStatement(sql);
			stmtOrder.setString(1, isdn);
			stmtOrder.setLong(2, productId);

			stmtOrder.execute();
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtOrder);
			Database.closeObject(connection);
		}
	}

	public static boolean checkOTP(String isdn, long productId)
			throws Exception {
		boolean check = false;
		PreparedStatement stmtOrder = null;
		Connection connection = null;
		ResultSet rs = null;
		try {
			String sql = "select * from otpqueue where isdn = ? and productId = ?";
			connection = Database.getConnection();
			stmtOrder = connection.prepareStatement(sql);
			stmtOrder.setString(1, isdn);
			stmtOrder.setLong(2, productId);

			rs = stmtOrder.executeQuery();
			if (rs.next()) {
				check = true;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtOrder);
			Database.closeObject(connection);
			Database.closeObject(rs);
		}

		return check;
	}

	public static void deleteOTPQueue(String isdn, long productId)
			throws Exception {
		PreparedStatement stmtOrder = null;
		Connection connection = null;
		try {
			String sql = "delete otpqueue where isdn = ? and productId = ?";
			connection = Database.getConnection();
			stmtOrder = connection.prepareStatement(sql);
			stmtOrder.setString(1, isdn);
			stmtOrder.setLong(2, productId);

			stmtOrder.execute();
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtOrder);
			Database.closeObject(connection);
		}
	}

}
