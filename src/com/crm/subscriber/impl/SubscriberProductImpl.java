package com.crm.subscriber.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.crm.marketing.cache.CampaignEntry;
import com.crm.marketing.cache.CampaignFactory;
import com.crm.product.cache.ProductEntry;
import com.crm.product.cache.ProductFactory;
import com.crm.kernel.message.Constants;
import com.crm.subscriber.bean.SubscriberGoService;
import com.crm.subscriber.bean.SubscriberProduct;
import com.crm.kernel.sql.Database;
import com.crm.util.DateUtil;
import com.fss.util.AppException;
import com.logica.smpp.Data;

/**
 * 
 * @author ?? <br>
 *         Modified by NamTA Modified Date: 07/06/2012
 * 
 */
public class SubscriberProductImpl {
	public final static String CONDITION_ACTIVE = " (supplierStatus = "
			+ Constants.SUPPLIER_ACTIVE_STATUS + ") ";

	public final static String CONDITION_BARRING = " (supplierStatus = "
			+ Constants.SUPPLIER_BARRING_STATUS + ") ";

	public final static String CONDITION_TERMINATED = " (supplierStatus = "
			+ Constants.SUPPLIER_CANCEL_STATUS + ") ";

	public final static String CONDITION_UNTERMINATED = " (supplierStatus != "
			+ Constants.SUPPLIER_CANCEL_STATUS + ") ";

	private static long DEFAULT_ID = 0;

	/**
	 * TODO: Performance test
	 */
	// private static long sleepTime = 1000L;

	public static Date calculateExpirationDate(Date startDate,
			String subscriptionType, int period, int quantity) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);

		// Duy MB update 13:43 14/06/2013
		// if (period <= 0)
		// {
		// return null;
		// }

		if (subscriptionType.equalsIgnoreCase("monthly")
				|| subscriptionType.equalsIgnoreCase("month")) {
			calendar.add(Calendar.DATE, 30 * period * quantity);
		} else if (subscriptionType.equalsIgnoreCase("weekly")
				|| subscriptionType.equalsIgnoreCase("week")) {
			calendar.add(Calendar.DATE, 7 * period * quantity);
		} else if (subscriptionType.equalsIgnoreCase("daily")
				|| subscriptionType.equalsIgnoreCase("day")) {
			calendar.add(Calendar.DATE, 1 * period * quantity);
		}

		// calendar.add(Calendar.DATE, -1);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);

		return calendar.getTime();
	}

	public static Date calculateGraceDate(Date startDate, String graceDateUnit,
			int graceDatePeriod) throws Exception {
		Date graceDate = null;
		if (graceDatePeriod > 0) {
			graceDate = DateUtil.addDate(startDate, graceDateUnit,
					graceDatePeriod);
		}

		return graceDate;
	}

	public static SubscriberProduct getProduct(ResultSet rsProduct)
			throws Exception {
		SubscriberProduct result = new SubscriberProduct();

		try {
			result.setUserId(rsProduct.getLong("userId"));
			result.setUserName(Database.getString(rsProduct, "userName"));

			result.setSubscriberId(rsProduct.getLong("subscriberId"));
			result.setSubProductId(rsProduct.getLong("subProductId"));
			result.setProductId(rsProduct.getLong("productId"));

			result.setSubscriberType(rsProduct.getInt("subscriberType"));
			result.setIsdn(Database.getString(rsProduct, "isdn"));
			result.setLanguageId(Database.getString(rsProduct, "languageId"));

			result.setRegisterDate(rsProduct.getTimestamp("registerDate"));
			result.setUnregisterDate(rsProduct.getTimestamp("unregisterDate"));
			result.setTermDate(rsProduct.getTimestamp("termDate"));
			result.setExpirationDate(rsProduct.getTimestamp("expirationDate"));
			result.setGraceDate(rsProduct.getTimestamp("graceDate"));

			result.setBarringStatus(rsProduct.getInt("barringStatus"));
			result.setSupplierStatus(rsProduct.getInt("supplierStatus"));
		} catch (Exception e) {
			throw e;
		}

		return result;
	}

	/**
	 * Only use for searching on table subscriberorder to get information about
	 * service.
	 * 
	 * @param rsProduct
	 * @param temp
	 * @return
	 * @throws Exception
	 */
	public static SubscriberProduct getProduct(ResultSet rsProduct, long temp)
			throws Exception {
		SubscriberProduct result = new SubscriberProduct();

		try {
			result.setUserId(rsProduct.getLong("userId"));
			result.setUserName(Database.getString(rsProduct, "userName"));

			result.setSubscriberId(rsProduct.getLong("subscriberId"));
			result.setSubProductId(rsProduct.getLong("subProductId"));
			result.setProductId(rsProduct.getLong("productId"));

			result.setSubscriberType(rsProduct.getInt("subscriberType"));
			result.setIsdn(Database.getString(rsProduct, "isdn"));

			result.setRegisterDate(rsProduct.getTimestamp("createdate"));

		} catch (Exception e) {
			throw e;
		}

		return result;
	}

	public static SubscriberProduct getProduct(Connection connection,
			long subProductId) throws Exception {
		PreparedStatement stmtProduct = null;
		ResultSet rsProduct = null;

		SubscriberProduct result = null;

		try {
			String SQL = "Select * From SubscriberProduct Where subProductId = ?";

			stmtProduct = connection.prepareStatement(SQL);
			stmtProduct.setLong(1, subProductId);

			rsProduct = stmtProduct.executeQuery();

			if (rsProduct.next()) {
				result = getProduct(rsProduct);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(rsProduct);
			Database.closeObject(stmtProduct);
		}

		return result;
	}

	public static SubscriberProduct getProduct(long subProductId)
			throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();

			return getProduct(connection, subProductId);
		} finally {
			Database.closeObject(connection);
		}
	}

	/**
	 * Get all unterminated products of specific isdn<br>
	 * Create by NamTA<br>
	 * Create Date: 18/06/2013
	 * 
	 * @param isdn
	 * @return
	 * @throws Exception
	 */
	public static SubscriberProduct[] getUnterminated(String isdn)
			throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();

			return getUnterminated(connection, isdn);
		} finally {
			Database.closeObject(connection);
		}
	}

	/**
	 * Get all unterminated products of specific isdn<br>
	 * Create by NamTA<br>
	 * Create Date: 18/06/2013
	 * 
	 * @param connection
	 * @param isdn
	 * @return
	 * @throws Exception
	 */
	public static SubscriberProduct[] getUnterminated(Connection connection,
			String isdn) throws Exception {
		PreparedStatement stmtActive = null;
		ResultSet rsActive = null;

		List<SubscriberProduct> subProducts = new ArrayList<SubscriberProduct>();

		try {
			String SQL = "Select * " + "From SubscriberProduct "
					+ "Where isdn = ? and " + CONDITION_UNTERMINATED
					+ "Order by registerDate desc";

			stmtActive = connection.prepareStatement(SQL);
			stmtActive.setString(1, isdn);

			rsActive = stmtActive.executeQuery();

			while (rsActive.next()) {
				subProducts.add(getProduct(rsActive));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(rsActive);
			Database.closeObject(stmtActive);
		}

		return subProducts.toArray(new SubscriberProduct[] {});
	}

	/**
	 * Created by NamTA<br>
	 * Created Date: 07/06/2012
	 * 
	 * @param isdn
	 * @param productId
	 * @return
	 * @throws Exception
	 */
	public static SubscriberProduct getUnterminated(String isdn, long productId)
			throws Exception {
		Connection connection = null;

		try {
			/**
			 * TODO: performanceTest
			 */

			// Thread.sleep(sleepTime);
			// return null;

			connection = Database.getConnection();

			return getUnterminated(connection, isdn, productId);
		} finally {
			Database.closeObject(connection);
		}
	}

	/**
	 * Created by NamTA<br>
	 * Created Date: 07/06/2012
	 * 
	 * @param connection
	 * @param isdn
	 * @param productId
	 * @return
	 * @throws Exception
	 */
	public static SubscriberProduct getUnterminated(Connection connection,
			String isdn, long productId) throws Exception {
		PreparedStatement stmtActive = null;
		ResultSet rsActive = null;

		SubscriberProduct result = null;

		try {
			String SQL = "Select * " + "From SubscriberProduct "
					+ "Where isdn = ? and productId = ? and "
					+ CONDITION_UNTERMINATED + "Order by registerDate desc";

			stmtActive = connection.prepareStatement(SQL);
			stmtActive.setString(1, isdn);
			stmtActive.setLong(2, productId);

			rsActive = stmtActive.executeQuery();

			if (rsActive.next()) {
				result = getProduct(rsActive);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(rsActive);
			Database.closeObject(stmtActive);
		}

		return result;
	}

	/**
	 * Get all barring products of specific isdn<br>
	 * Created by NamTA<br>
	 * Created Date: 18/06/2013
	 * 
	 * @param isdn
	 * @return
	 * @throws Exception
	 */
	public static SubscriberProduct[] getBarring(String isdn) throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();

			return getBarring(connection, isdn);
		} finally {
			Database.closeObject(connection);
		}
	}

	/**
	 * Get all barring products of specific isdn<br>
	 * Created by NamTA<br>
	 * Created Date: 18/06/2013
	 * 
	 * @param connection
	 * @param isdn
	 * @return
	 * @throws Exception
	 */
	public static SubscriberProduct[] getBarring(Connection connection,
			String isdn) throws Exception {
		PreparedStatement stmtBarring = null;
		ResultSet rsBarring = null;

		List<SubscriberProduct> subProducts = new ArrayList<SubscriberProduct>();
		try {
			String SQL = "Select * " + "From SubscriberProduct "
					+ "Where isdn = ? and " + CONDITION_BARRING
					+ "Order by registerDate desc";

			stmtBarring = connection.prepareStatement(SQL);
			stmtBarring.setString(1, isdn);

			rsBarring = stmtBarring.executeQuery();

			while (rsBarring.next()) {
				subProducts.add(getProduct(rsBarring));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(rsBarring);
			Database.closeObject(stmtBarring);
		}

		return subProducts.toArray(new SubscriberProduct[] {});
	}

	/**
	 * Created by NamTA<br>
	 * Created Date: 07/06/2012
	 * 
	 * @param connection
	 * @param isdn
	 * @param productId
	 * @return
	 * @throws Exception
	 */
	public static SubscriberProduct getBarring(Connection connection,
			String isdn, long productId) throws Exception {
		PreparedStatement stmtActive = null;
		ResultSet rsActive = null;

		SubscriberProduct result = null;

		try {
			String SQL = "Select * " + "From SubscriberProduct "
					+ "Where isdn = ? and productId = ? and "
					+ CONDITION_BARRING + "Order by registerDate desc";

			stmtActive = connection.prepareStatement(SQL);
			stmtActive.setString(1, isdn);
			stmtActive.setLong(2, productId);

			rsActive = stmtActive.executeQuery();

			if (rsActive.next()) {
				result = getProduct(rsActive);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(rsActive);
			Database.closeObject(stmtActive);
		}

		return result;
	}

	/**
	 * Created by NamTA<br>
	 * Created Date: 07/06/2012
	 * 
	 * @param isdn
	 * @param productId
	 * @return
	 * @throws Exception
	 */
	public static SubscriberProduct getBarring(String isdn, long productId)
			throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();

			return getBarring(connection, isdn, productId);
		} finally {
			Database.closeObject(connection);
		}
	}

	public static SubscriberProduct getActive(Connection connection,
			String isdn, long productId) throws Exception {
		PreparedStatement stmtActive = null;
		ResultSet rsActive = null;

		SubscriberProduct result = null;

		try {
			String SQL = "Select * " + "From SubscriberProduct "
					+ "Where isdn = ? and productId = ? and "
					+ CONDITION_ACTIVE + "Order by registerDate desc";

			stmtActive = connection.prepareStatement(SQL);
			stmtActive.setString(1, isdn);
			stmtActive.setLong(2, productId);

			rsActive = stmtActive.executeQuery();

			if (rsActive.next()) {
				result = getProduct(rsActive);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(rsActive);
			Database.closeObject(stmtActive);
		}

		return result;
	}

	public static SubscriberProduct getActive(String isdn, long productId)
			throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();

			return getActive(connection, isdn, productId);
		} finally {
			Database.closeObject(connection);
		}
	}

	public static SubscriberProduct getActive(Connection connection,
			long subscriberId, long productId) throws Exception {
		PreparedStatement stmtActive = null;
		ResultSet rsActive = null;

		SubscriberProduct result = null;

		try {
			String SQL = "Select /* + SUBS_SUBPCRIBERID_INX2 */ * "
					+ "From SubscriberProduct "
					+ "Where subscriberId = ? and productId = ? and "
					+ CONDITION_ACTIVE + "Order by registerDate desc";

			stmtActive = connection.prepareStatement(SQL);
			stmtActive.setLong(1, subscriberId);
			stmtActive.setLong(2, productId);

			rsActive = stmtActive.executeQuery();

			if (rsActive.next()) {
				result = getProduct(rsActive);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(rsActive);
			Database.closeObject(stmtActive);
		}

		return result;
	}

	public static SubscriberProduct getActive(long subscriberId, long productId)
			throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();

			return getActive(connection, subscriberId, productId);
		} finally {
			Database.closeObject(connection);
		}
	}

	public static SubscriberProduct getActive(Connection connection,
			long subscriberId, String isdn, long productId) throws Exception {
		if (subscriberId != DEFAULT_ID) {
			return getActive(connection, subscriberId, productId);
		} else {
			return getActive(connection, isdn, productId);
		}
	}

	public static SubscriberProduct getActiveX(String isdn, long productId,
			Date orderDate) throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();

			return getActiveX(connection, isdn, productId, orderDate);
		} finally {
			Database.closeObject(connection);
		}
	}

	public static SubscriberProduct getActiveX(Connection connection,
			String isdn, long productId, Date orderDate) throws Exception {
		PreparedStatement stmtActive = null;
		ResultSet rsActive = null;

		SubscriberProduct result = null;

		try {
			String SQL = "Select * "
					+ "From SubscriberOrder "
					+ "Where isdn = ? and productId = ? and status = ? "
					+ "and ordertype in (?,?,?,?) "
					+ "and orderDate >= trunc(sysdate) and orderDate < (trunc(sysdate) + 1)"
					+ "Order by createdate desc";

			stmtActive = connection.prepareStatement(SQL);
			stmtActive.setString(1, isdn);
			stmtActive.setLong(2, productId);
			stmtActive.setLong(3, Constants.ORDER_STATUS_APPROVED);
			// Duymb Add 20130627
			stmtActive.setString(4, Constants.ACTION_REGISTER);
			stmtActive.setString(5, Constants.ACTION_ADVERTISING);
			stmtActive.setString(6, Constants.ACTION_AUTORENEW);
			stmtActive.setString(7, Constants.ACTION_SUBSCRIPTION);
			// Duymb Add end 20130627
			rsActive = stmtActive.executeQuery();

			if (rsActive.next()) {
				result = getProduct(rsActive, 1);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(rsActive);
			Database.closeObject(stmtActive);
		}

		return result;

	}

	public static List<SubscriberProduct> getListActive(long subscriberId,
			String isdn) throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();
			return getActive(connection, subscriberId, isdn);
		} finally {
			Database.closeObject(connection);
		}
	}

	public static List<SubscriberProduct> getActive(Connection connection,
			long subscriberId, String isdn) throws Exception {
		PreparedStatement stmtActive = null;
		ResultSet rsActive = null;

		ArrayList<SubscriberProduct> result = new ArrayList<SubscriberProduct>();

		try {
			if (subscriberId != Constants.DEFAULT_ID) {
				String SQL = "Select * From SubscriberProduct Where isdn = ? and "
						+ CONDITION_ACTIVE;

				stmtActive = connection.prepareStatement(SQL);
				stmtActive.setString(1, isdn);
			} else {
				String SQL = "Select * From SubscriberProduct Where subscriberId = ? and "
						+ CONDITION_ACTIVE;

				stmtActive = connection.prepareStatement(SQL);
				stmtActive.setLong(1, subscriberId);
			}

			rsActive = stmtActive.executeQuery();

			while (rsActive.next()) {
				result.add(getProduct(rsActive));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(rsActive);
			Database.closeObject(stmtActive);
		}

		return result;
	}

	/**
	 * Edited by NamTA<br>
	 * Modified Date: 17/05/2012
	 * 
	 * @param connection
	 * @param userId
	 * @param userName
	 * @param subscriberId
	 * @param isdn
	 * @param subscriberType
	 * @param productId
	 * @param campaignId
	 * @param languageId
	 * @param includeCurrentDay
	 * @return
	 * @throws Exception
	 */
	public static SubscriberProduct register(Connection connection,
			long userId, String userName, long subscriberId, String isdn,
			int subscriberType, long productId, long campaignId,
			String languageId, boolean includeCurrentDay, long merchantId,
			int opId, int motype) throws Exception {
		SubscriberProduct subscriberProduct = null;

		PreparedStatement stmtRegister = null;

		try {
			Date now = new Date();

			ProductEntry product = ProductFactory.getCache().getProduct(
					productId);

			// calculate term of use date
			Date termDate = null;

			if (product.getTermPeriod() > 0) {
				termDate = DateUtil.addDate(now, product.getTermUnit(),
						product.getTermPeriod());
			}

			// calculate expire date
			Date expirationDate = null;
			Date graceDate = null;

			if (product.isSubscription()) {
				int quantity = 1;

				int expirationPeriod = product.getSubscriptionPeriod();

				String expirationUnit = product.getSubscriptionUnit();

				if (campaignId != DEFAULT_ID) {
					CampaignEntry campaign = CampaignFactory.getCache()
							.getCampaign(campaignId);

					if ((campaign != null)) {
						expirationPeriod = campaign.getSchedulePeriod();
						expirationUnit = campaign.getScheduleUnit();
					}
				}

				expirationDate = calculateExpirationDate(now, expirationUnit,
						expirationPeriod, quantity);

				/**
				 * remove 1 day if expiration time includes current day
				 */
				if (includeCurrentDay) {
					Calendar expiration = Calendar.getInstance();
					expiration.setTime(expirationDate);
					expiration.add(Calendar.DATE, -1);

					expirationDate = expiration.getTime();
				}
				graceDate = calculateGraceDate(expirationDate,
						product.getGraceUnit(), product.getGracePeriod());
			}

			// check product are registered or not
			subscriberProduct = getActive(connection, subscriberId, isdn,
					productId);

			if (subscriberProduct == null) {

				// register product for subscriber
				String sql = "Insert into SubscriberProduct "
						+ "     (subProductId, userId, userName, createDate, modifiedDate "
						+ "     , subscriberId, isdn, subscriberType, productId, languageId "
						+ "     , registerDate, termDate, expirationDate, graceDate, barringStatus, supplierStatus, CampaignId, merchantId, telcoId, motype) "
						+ "Values " + "     (?, ?, ?, sysDate, sysDate "
						+ "     , ?, ?, ?, ?, ? "
						+ "     , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

				stmtRegister = connection.prepareStatement(sql);

				long subProductId = Database.getSequence(connection,
						"sub_product_seq");

				int barringStatus = Constants.USER_ACTIVE_STATUS;
				int supplierStatus = Constants.SUPPLIER_ACTIVE_STATUS;

				stmtRegister.setLong(1, subProductId);
				stmtRegister.setLong(2, userId);
				stmtRegister.setString(3, userName);

				stmtRegister.setLong(4, subscriberId);
				stmtRegister.setString(5, isdn);
				stmtRegister.setInt(6, subscriberType);
				stmtRegister.setLong(7, productId);
				stmtRegister.setString(8, languageId);

				stmtRegister.setTimestamp(9, DateUtil.getTimestampSQL(now));
				stmtRegister.setTimestamp(10,
						DateUtil.getTimestampSQL(termDate));
				stmtRegister.setTimestamp(11,
						DateUtil.getTimestampSQL(expirationDate));
				stmtRegister.setTimestamp(12,
						DateUtil.getTimestampSQL(graceDate));

				stmtRegister.setInt(13, barringStatus);
				stmtRegister.setInt(14, supplierStatus);
				stmtRegister.setLong(15, campaignId);
				stmtRegister.setLong(16, merchantId);
				stmtRegister.setInt(17, opId);
				stmtRegister.setInt(18, motype);

				stmtRegister.execute();

				// bind return
				// bind order
				subscriberProduct = new SubscriberProduct();

				subscriberProduct.setUserId(userId);
				subscriberProduct.setUserName(userName);

				subscriberProduct.setSubscriberId(subscriberId);
				subscriberProduct.setSubProductId(subProductId);
				subscriberProduct.setProductId(productId);

				subscriberProduct.setSubscriberType(subscriberType);
				subscriberProduct.setIsdn(isdn);

				subscriberProduct.setRegisterDate(now);
				subscriberProduct.setTermDate(termDate);
				subscriberProduct.setExpirationDate(expirationDate);
				subscriberProduct.setGraceDate(graceDate);

				subscriberProduct.setBarringStatus(barringStatus);
				subscriberProduct.setSupplierStatus(supplierStatus);

				if (product.isAuditEnable()) {
					SubscriberActivateImpl.addActivate(connection, userId,
							userName, subscriberId, isdn, subProductId,
							subscriberProduct.getRegisterDate(),
							subscriberProduct.getBarringStatus(),
							subscriberProduct.getSupplierStatus(), "");
				}
			}

		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtRegister);
		}

		return subscriberProduct;
	}

	/**
	 * Edited by NamTA<br>
	 * Modified Date: 17/05/2012
	 * 
	 * @param userId
	 * @param userName
	 * @param subscriberId
	 * @param isdn
	 * @param subscriberType
	 * @param productId
	 * @param campaignId
	 * @param languageId
	 * @param includeCurrentDay
	 * @return
	 * @throws Exception
	 */
	public static SubscriberProduct register(long userId, String userName,
			long subscriberId, String isdn, int subscriberType, long productId,
			long campaignId, String languageId, boolean includeCurrentDay,
			long merchantId, int Opid, int moType) throws Exception {
		Connection connection = null;

		try {
			/**
			 * TODO; PerformanceTest
			 */

			// Thread.sleep(sleepTime);
			// SubscriberProduct subPro = new SubscriberProduct();
			// subPro.setExpirationDate(new Date());
			//
			// return subPro;

			connection = Database.getConnection();

			return register(connection, userId, userName, subscriberId, isdn,
					subscriberType, productId, campaignId, languageId,
					includeCurrentDay, merchantId, Opid, moType);
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
		}
	}

	public static void unregister(Connection connection, long userId,
			String userName, long subProductId, long productId)
			throws Exception {
		SubscriberProduct subscriberProduct = null;

		PreparedStatement stmtSubscription = null;

		try {
			String sql = "Update SubscriberProduct "
					+ "   Set 	userId = ?, userName = ?, modifiedDate = sysDate "
					+ "   		, unregisterDate = sysDate, barringStatus = ?, supplierStatus = ? "
					+ "	  Where subProductId = ? and " + CONDITION_UNTERMINATED;

			stmtSubscription = connection.prepareStatement(sql);

			stmtSubscription.setLong(1, userId);
			stmtSubscription.setString(2, userName);
			stmtSubscription.setInt(3, Constants.USER_CANCEL_STATUS);
			stmtSubscription.setInt(4, Constants.SUPPLIER_CANCEL_STATUS);
			stmtSubscription.setLong(5, subProductId);

			stmtSubscription.execute();

			if (stmtSubscription.getUpdateCount() == 0) {
				throw new AppException(Constants.ERROR_UNREGISTERED);
			}

			ProductEntry product = ProductFactory.getCache().getProduct(
					productId);

			if (product.isAuditEnable()) {
				subscriberProduct = getProduct(connection, subProductId);

				SubscriberActivateImpl.unregister(connection, userId, userName,
						subscriberProduct.getSubscriberId(),
						subscriberProduct.getIsdn(),
						subscriberProduct.getProductId(),
						subscriberProduct.getUnregisterDate(), "");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtSubscription);
		}
	}

	public static void unregister(long userId, String userName,
			long subProductId, long productId) throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();

			unregister(connection, userId, userName, subProductId, productId);
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
		}
	}

	public static void barringBySupplier(Connection connection, long userId,
			String userName, long subProductId) throws Exception {
		SubscriberProduct subscriberProduct = null;

		PreparedStatement stmtSubscription = null;

		try {
			subscriberProduct = getProduct(connection, subProductId);

			if (subscriberProduct == null) {
				throw new AppException(Constants.ERROR_UNREGISTERED);
			} else if (subscriberProduct.getSupplierStatus() == Constants.SUPPLIER_BARRING_STATUS) {
				return;
			}

			ProductEntry product = ProductFactory.getCache().getProduct(
					subscriberProduct.getProductId());

			String sql = "Update SubscriberProduct "
					+ "     Set    userId = ?, userName = ?, modifiedDate = sysDate, supplierStatus = ? "
					+ "     Where  subProductId = ? and unregisterDate is null ";

			stmtSubscription = connection.prepareStatement(sql);

			stmtSubscription.setLong(1, userId);
			stmtSubscription.setString(2, userName);
			stmtSubscription.setInt(3, Constants.SUPPLIER_BARRING_STATUS);
			stmtSubscription.setLong(4, subProductId);

			stmtSubscription.execute();

			subscriberProduct
					.setSupplierStatus(Constants.SUPPLIER_BARRING_STATUS);

			if (product.isAuditEnable()) {
				SubscriberActivateImpl.updateActivate(connection, userId,
						userName, subscriberProduct.getSubscriberId(),
						subscriberProduct.getIsdn(),
						subscriberProduct.getProductId(), new Date(),
						subscriberProduct.getBarringStatus(),
						subscriberProduct.getSupplierStatus(), "");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtSubscription);
		}
	}

	public static void barringBySupplier(long userId, String userName,
			long subProductId) throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();

			barringBySupplier(connection, userId, userName, subProductId);
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
		}
	}

	public static SubscriberProduct subscription(Connection connection,
			long userId, String userName, long subProductId,
			boolean fullOfCharge, int quantity) throws Exception {
		SubscriberProduct subscriberProduct = null;

		PreparedStatement stmtSubscription = null;

		try {
			subscriberProduct = getProduct(connection, subProductId);

			if (subscriberProduct == null) {
				throw new AppException(Constants.ERROR_UNREGISTERED);
			}

			ProductEntry product = ProductFactory.getCache().getProduct(
					subscriberProduct.getProductId());

			// if (!product.isSubscription())
			// {
			// throw new AppException(Constants.ERROR_SUBSCRIPTION_NOT_FOUND);
			// }

			String subscriptionUnit = fullOfCharge ? product
					.getSubscriptionUnit() : "daily";
			int subscriptionPeriod = fullOfCharge ? product
					.getSubscriptionPeriod() : quantity;

			int gracePeriod = product.getGracePeriod();
			String graceUnit = product.getGraceUnit();

			// extend subscription date
			Date now = new Date();
			Calendar expiration = Calendar.getInstance();
			expiration.setTime(subscriberProduct.getExpirationDate());
			if (expiration.getTime().getTime() < now.getTime()) {
				expiration.setTime(now);
				expiration.add(Calendar.DATE, -1);
			}

			Date expirationDate = DateUtil.addDate(expiration.getTime(),
					subscriptionUnit, subscriptionPeriod);
			Date graceDate = DateUtil.addDate(expirationDate, graceUnit,
					gracePeriod);

			// prepare SQL
			String SQL = "Update SubscriberProduct "
					+ "   Set userId = ?, userName = ?, modifiedDate = sysDate "
					+ "       , supplierStatus = ?, expirationDate = ?, graceDate = ? "
					+ "   Where subProductId = ? and unregisterDate is null ";

			stmtSubscription = connection.prepareStatement(SQL);

			stmtSubscription.setLong(1, userId);
			stmtSubscription.setString(2, userName);
			stmtSubscription.setInt(3, Constants.SUPPLIER_ACTIVE_STATUS);
			stmtSubscription.setTimestamp(4,
					DateUtil.getTimestampSQL(expirationDate));
			stmtSubscription.setTimestamp(5,
					DateUtil.getTimestampSQL(graceDate));
			stmtSubscription.setLong(6, subProductId);

			stmtSubscription.execute();

			if (stmtSubscription.getUpdateCount() == 0) {
				throw new AppException(Constants.ERROR_UNREGISTERED);
			}

			if (product.isAuditEnable()
					&& (subscriberProduct.getSupplierStatus() != Constants.SUPPLIER_ACTIVE_STATUS)) {
				SubscriberActivateImpl.updateActivate(connection, userId,
						userName, subscriberProduct.getSubscriberId(),
						subscriberProduct.getIsdn(),
						subscriberProduct.getProductId(), new Date(),
						subscriberProduct.getBarringStatus(),
						subscriberProduct.getSupplierStatus(), "");
			}

			subscriberProduct.setExpirationDate(expirationDate);
			subscriberProduct.setGraceDate(graceDate);
			subscriberProduct
					.setSupplierStatus(Constants.SUPPLIER_ACTIVE_STATUS);
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtSubscription);
		}

		return subscriberProduct;
	}

	public static SubscriberProduct subscription(long userId, String userName,
			long subProductId, boolean fullOfCharge, int quantity)
			throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();

			return subscription(connection, userId, userName, subProductId,
					fullOfCharge, quantity);
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
		}
	}

	public static void changeLanguage(Connection connection, long userId,
			String userName, long subProductId, String languageId)
			throws Exception {
		PreparedStatement stmtProduct = null;

		try {
			// prepare SQL
			String SQL = "Update 	SubscriberProduct "
					+ "   Set 		userId = ?, userName = ?, modifiedDate = sysDate, languageId = ? "
					+ "   Where 	subProductId = ? and unregisterDate is null ";

			stmtProduct = connection.prepareStatement(SQL);

			// update
			stmtProduct.setLong(1, userId);
			stmtProduct.setString(2, userName);
			stmtProduct.setString(3, languageId);
			stmtProduct.setLong(4, subProductId);

			if (stmtProduct.executeUpdate() == 0) {
				throw new AppException("unregistered");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtProduct);
		}
	}

	public static void changeLanguage(long userId, String userName,
			long subProductId, String languageId) throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();

			changeLanguage(connection, userId, userName, subProductId,
					languageId);
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
		}
	}

	public static void unbarringBySupplier(Connection connection, long userId,
			String userName, long subProductId) throws Exception {
		SubscriberProduct subscriberProduct = null;

		PreparedStatement stmtSubscription = null;

		try {
			subscriberProduct = getProduct(connection, subProductId);

			if (subscriberProduct == null) {
				throw new AppException(Constants.ERROR_UNREGISTERED);
			} else if (subscriberProduct.getSupplierStatus() == Constants.SUPPLIER_ACTIVE_STATUS) {
				return;
			}

			ProductEntry product = ProductFactory.getCache().getProduct(
					subscriberProduct.getProductId());

			String sql = "Update SubscriberProduct "
					+ "     Set    userId = ?, userName = ?, modifiedDate = sysDate, supplierStatus = ? "
					+ "     Where  subProductId = ? and unregisterDate is null ";

			stmtSubscription = connection.prepareStatement(sql);

			stmtSubscription.setLong(1, userId);
			stmtSubscription.setString(2, userName);
			stmtSubscription.setInt(3, Constants.SUPPLIER_ACTIVE_STATUS);
			stmtSubscription.setLong(4, subProductId);

			stmtSubscription.execute();

			subscriberProduct
					.setSupplierStatus(Constants.SUPPLIER_ACTIVE_STATUS);

			if (product.isAuditEnable()) {
				SubscriberActivateImpl.updateActivate(connection, userId,
						userName, subscriberProduct.getSubscriberId(),
						subscriberProduct.getIsdn(),
						subscriberProduct.getProductId(), new Date(),
						subscriberProduct.getBarringStatus(),
						subscriberProduct.getSupplierStatus(), "");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtSubscription);
		}
	}

	public static void unbarringBySupplier(long userId, String userName,
			long subProductId) throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();

			unbarringBySupplier(connection, userId, userName, subProductId);
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
		}
	}

	/**
	 * 
	 * Created by NamTA <br>
	 * Created Date: 16/05/2012
	 * 
	 * @param connection
	 * @param userId
	 * @param userName
	 * @param subProductId
	 * @return
	 * @throws Exception
	 */
	public static SubscriberProduct extendExpirationDate(Connection connection,
			long userId, String userName, long subProductId, long campaignId,
			boolean includeCurrentDay) throws Exception {
		SubscriberProduct subscriberProduct = null;

		PreparedStatement stmtSubscription = null;

		try {
			subscriberProduct = getProduct(connection, subProductId);

			if (subscriberProduct == null) {
				throw new AppException(Constants.ERROR_UNREGISTERED);
			}

			ProductEntry product = ProductFactory.getCache().getProduct(
					subscriberProduct.getProductId());

			Date now = new Date();
			Date expirationDate = subscriberProduct.getExpirationDate();
			Date graceDate = null;
			if (expirationDate.before(now))
				expirationDate = now;

			if (product.isSubscription()) {
				int quantity = 1;

				int expirationPeriod = product.getSubscriptionPeriod();

				String expirationUnit = product.getSubscriptionUnit();

				if (campaignId != DEFAULT_ID) {
					CampaignEntry campaign = CampaignFactory.getCache()
							.getCampaign(campaignId);

					if (campaign != null) {
						expirationPeriod = campaign.getSchedulePeriod();
						expirationUnit = campaign.getScheduleUnit();
					}
				}

				expirationDate = calculateExpirationDate(expirationDate,
						expirationUnit, expirationPeriod, quantity);

				if (includeCurrentDay) {
					/**
					 * remove 1 day if expiration time includes current day
					 */
					if (includeCurrentDay) {
						Calendar expiration = Calendar.getInstance();
						expiration.setTime(expirationDate);
						expiration.add(Calendar.DATE, -1);

						expirationDate = expiration.getTime();
					}
				}

				graceDate = DateUtil.addDate(expirationDate,
						product.getGraceUnit(), product.getGracePeriod());
			}

			String sql = "Update SubscriberProduct "
					+ "     Set    userId = ?, userName = ?, modifiedDate = sysDate, supplierStatus = ?, "
					+ "     expirationDate = ?, graceDate = ? "
					+ "     Where  subProductId = ? and unregisterDate is null ";

			stmtSubscription = connection.prepareStatement(sql);

			stmtSubscription.setLong(1, userId);
			stmtSubscription.setString(2, userName);
			stmtSubscription.setInt(3, Constants.SUPPLIER_ACTIVE_STATUS);
			stmtSubscription.setTimestamp(4,
					DateUtil.getTimestampSQL(expirationDate));
			stmtSubscription.setTimestamp(5,
					DateUtil.getTimestampSQL(graceDate));
			stmtSubscription.setLong(6, subProductId);

			stmtSubscription.execute();

			subscriberProduct
					.setSupplierStatus(Constants.SUPPLIER_ACTIVE_STATUS);
			subscriberProduct.setExpirationDate(expirationDate);
			subscriberProduct.setGraceDate(graceDate);

			if (product.isAuditEnable()) {
				SubscriberActivateImpl.updateActivate(connection, userId,
						userName, subscriberProduct.getSubscriberId(),
						subscriberProduct.getIsdn(),
						subscriberProduct.getProductId(), new Date(),
						subscriberProduct.getBarringStatus(),
						subscriberProduct.getSupplierStatus(), "");
			}

			return subscriberProduct;
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtSubscription);
		}
	}

	/**
	 * 
	 * Created by NamTA<br>
	 * Created Date: 16/05/2012
	 * 
	 * @param userId
	 * @param userName
	 * @param subProductId
	 * @return
	 * @throws Exception
	 */
	public static SubscriberProduct extendExpirationDate(long userId,
			String userName, long subProductId, long campaignId,
			boolean includeCurrentDay) throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();

			return extendExpirationDate(connection, userId, userName,
					subProductId, campaignId, includeCurrentDay);
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
		}
	}

	public static String getMemberList(Connection connection, String isdn,
			long productId, boolean includeSuspended) throws Exception {
		String phoneBookList = "";
		PreparedStatement stmtSubscription = null;

		ResultSet resultSet = null;

		try {
			String sql = "";
			if (includeSuspended) {
				sql = "Select * from subscribergroup "
						+ "Where  isdn = ? and productid = ? "
						+ " and unregisterdate is null order by createdate";
			} else {
				sql = "Select * from subscribergroup "
						+ "Where  isdn = ? and productid = ? and status = ? "
						+ " and unregisterdate is null order by createdate";
			}

			stmtSubscription = connection.prepareStatement(sql);

			stmtSubscription.setString(1, isdn);
			stmtSubscription.setLong(2, productId);
			if (!includeSuspended) {
				stmtSubscription.setInt(3, Constants.SUPPLIER_ACTIVE_STATUS);
			}
			resultSet = stmtSubscription.executeQuery();

			if (resultSet.next()) {
				phoneBookList = com.fss.util.StringUtil.nvl(
						resultSet.getString("REFERALISDN"), "");
			}

			while (resultSet.next()) {
				phoneBookList = phoneBookList
						+ ","
						+ com.fss.util.StringUtil.nvl(
								resultSet.getString("REFERALISDN"), "");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtSubscription);
		}
		return phoneBookList;
	}

	public static String getMemberList(String isdn, String userName,
			long productId, boolean includeSuspended) throws Exception {
		Connection connection = null;
		String result;
		try {
			connection = Database.getConnection();

			result = getMemberList(connection, isdn, productId,
					includeSuspended);
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
		}
		return result;
	}

	public static boolean withdraw(long userId, String userName,
			long subscriberId, String isdn, String balanceType, double amount)
			throws Exception {
		Connection connection = Database.getConnection();
		connection.setAutoCommit(false);

		PreparedStatement stmtBalance = null;

		try {
			String SQL = "Update SubscriberBalance "
					+ "Set userId = ?, userName = ?, modifiedDate = sysDate, balanceAmount = nvl(balanceAmount, 0) - ? "
					+ "Where subscriberId = ? and balanceType = ? and nvl(balanceAmount, 0) >= ? ";

			stmtBalance = connection.prepareStatement(SQL);

			stmtBalance.setLong(1, userId);
			stmtBalance.setString(2, userName);
			stmtBalance.setDouble(3, amount);
			stmtBalance.setString(4, isdn);
			stmtBalance.setString(5, balanceType);
			stmtBalance.setDouble(6, amount);

			stmtBalance.execute();

			if (stmtBalance.getUpdateCount() == 0) {
				throw new AppException("not-enough-money");
			}

			connection.commit();
		} catch (Exception e) {
			Database.rollback(connection);

			throw e;
		} finally {
			Database.closeObject(stmtBalance);
			Database.closeObject(connection);
		}

		return true;
	}

	/**
	 * Create by Do Tien Hung
	 * 
	 * @param connection
	 * @param userId
	 * @param userName
	 * @param subscriberId
	 * @param isdn
	 * @param subscriberType
	 * @param productId
	 * @param campaignId
	 * @param languageId
	 * @param includeCurrentDay
	 * @return
	 * @throws Exception
	 */

	public static SubscriberProduct registerProductBypassExisted(
			Connection connection, long userId, String userName,
			long subscriberId, String isdn, int subscriberType, long productId,
			long campaignId, String languageId, boolean includeCurrentDay)
			throws Exception {
		SubscriberProduct subscriberProduct = null;

		PreparedStatement stmtRegister = null;

		try {
			Date now = new Date();

			ProductEntry product = ProductFactory.getCache().getProduct(
					productId);

			// calculate term of use date
			Date termDate = null;

			if (product.getTermPeriod() > 0) {
				termDate = DateUtil.addDate(now, product.getTermUnit(),
						product.getTermPeriod());
			}

			// calculate expire date
			Date expirationDate = null;
			Date graceDate = null;

			if (product.isSubscription()) {
				int quantity = 1;

				int expirationPeriod = product.getSubscriptionPeriod();

				String expirationUnit = product.getSubscriptionUnit();

				if (campaignId != DEFAULT_ID) {
					CampaignEntry campaign = CampaignFactory.getCache()
							.getCampaign(campaignId);

					if (campaign != null) {
						expirationPeriod = campaign.getSchedulePeriod();
						expirationUnit = campaign.getScheduleUnit();
					}
				}

				expirationDate = calculateExpirationDate(now, expirationUnit,
						expirationPeriod, quantity);

				/**
				 * remove 1 day if expiration time includes current day
				 */
				if (includeCurrentDay) {
					Calendar expiration = Calendar.getInstance();
					expiration.setTime(expirationDate);
					expiration.add(Calendar.DATE, -1);

					expirationDate = expiration.getTime();
				}

				graceDate = DateUtil.addDate(expirationDate,
						product.getGraceUnit(), product.getGracePeriod());
			}

			// check product are registered or not
			String[] listProductId = product.getParameter("listProductId", "")
					.split(",");
			for (int i = 0; i < listProductId.length; i++) {
				subscriberProduct = getActive(connection, subscriberId, isdn,
						Long.parseLong(listProductId[i]));
				if (subscriberProduct != null) {
					break;
				}
			}

			if (subscriberProduct != null) {
				long subProductId = subscriberProduct.getSubProductId();
				unregister(connection, userId, userName, subProductId,
						productId);
			}

			// register product for subscriber
			String sql = "Insert into SubscriberProduct "
					+ "     (subProductId, userId, userName, createDate, modifiedDate "
					+ "     , subscriberId, isdn, subscriberType, productId, languageId "
					+ "     , registerDate, termDate, expirationDate, graceDate, barringStatus, supplierStatus) "
					+ "Values " + "     (?, ?, ?, sysDate, sysDate "
					+ "     , ?, ?, ?, ?, ? " + "     , ?, ?, ?, ?, ?, ?)";

			stmtRegister = connection.prepareStatement(sql);

			long subProductId = Database.getSequence(connection,
					"sub_product_seq");

			int barringStatus = Constants.USER_ACTIVE_STATUS;
			int supplierStatus = Constants.SUPPLIER_ACTIVE_STATUS;

			stmtRegister.setLong(1, subProductId);
			stmtRegister.setLong(2, userId);
			stmtRegister.setString(3, userName);

			stmtRegister.setLong(4, subscriberId);
			stmtRegister.setString(5, isdn);
			stmtRegister.setInt(6, subscriberType);
			stmtRegister.setLong(7, productId);
			stmtRegister.setString(8, languageId);

			stmtRegister.setTimestamp(9, DateUtil.getTimestampSQL(now));
			stmtRegister.setTimestamp(10, DateUtil.getTimestampSQL(termDate));
			stmtRegister.setTimestamp(11,
					DateUtil.getTimestampSQL(expirationDate));
			stmtRegister.setTimestamp(12, DateUtil.getTimestampSQL(graceDate));

			stmtRegister.setInt(13, barringStatus);
			stmtRegister.setInt(14, supplierStatus);

			stmtRegister.execute();

			// bind return
			// bind order
			subscriberProduct = new SubscriberProduct();

			subscriberProduct.setUserId(userId);
			subscriberProduct.setUserName(userName);

			subscriberProduct.setSubscriberId(subscriberId);
			subscriberProduct.setSubProductId(subProductId);
			subscriberProduct.setProductId(productId);

			subscriberProduct.setSubscriberType(subscriberType);
			subscriberProduct.setIsdn(isdn);

			subscriberProduct.setRegisterDate(now);
			subscriberProduct.setTermDate(termDate);
			subscriberProduct.setExpirationDate(expirationDate);
			subscriberProduct.setGraceDate(graceDate);

			subscriberProduct.setBarringStatus(barringStatus);
			subscriberProduct.setSupplierStatus(supplierStatus);

			if (product.isAuditEnable()) {
				SubscriberActivateImpl.addActivate(connection, userId,
						userName, subscriberId, isdn, subProductId,
						subscriberProduct.getRegisterDate(),
						subscriberProduct.getBarringStatus(),
						subscriberProduct.getSupplierStatus(), "");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtRegister);
		}

		return subscriberProduct;
	}

	public static SubscriberProduct registerProductBypassExisted(long userId,
			String userName, long subscriberId, String isdn,
			int subscriberType, long productId, long campaignId,
			String languageId, boolean includeCurrentDay) throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();

			return registerProductBypassExisted(connection, userId, userName,
					subscriberId, isdn, subscriberType, productId, campaignId,
					languageId, includeCurrentDay);
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
		}
	}

	public static void setSubscriptionComplete(long subProductId)
			throws Exception {
		Connection connection = null;
		try {
			connection = Database.getConnection();
			setSubscriptionComplete(connection, subProductId);
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
		}
	}

	public static void setSubscriptionComplete(Connection connection,
			long subProductId) throws Exception {
		PreparedStatement stmtSubscription = null;
		try {
			String sql = "Update SubscriberProduct Set subscriptionStatus = 0 Where subProductId = ?";
			stmtSubscription = connection.prepareStatement(sql);

			stmtSubscription.setLong(1, subProductId);

			stmtSubscription.executeUpdate();
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtSubscription);
		}
	}

	public static boolean checkFirstTime(String isdn, long productId,
			Connection connection) {
		PreparedStatement stmtFirstTime = null;
		ResultSet rs = null;
		boolean isFirstTime = true;
		try {
			String sql = "select * from subscriberproduct where isdn =? and productid=? ";
			stmtFirstTime = connection.prepareStatement(sql);

			stmtFirstTime.setString(1, isdn);
			stmtFirstTime.setLong(2, productId);

			rs = stmtFirstTime.executeQuery();
			while (rs.next()) {
				isFirstTime = false;
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Database.closeObject(rs);
			Database.closeObject(stmtFirstTime);
		}
		return isFirstTime;
	}

	public static boolean checkFirstTime(String isdn, long productId)
			throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();

			return checkFirstTime(isdn, productId, connection);
		} finally {
			Database.closeObject(connection);
		}
	}

	public static boolean isFirstTimeRegister(String isdn, long productId)
			throws Exception {
		Connection connection = null;
		try {
			connection = Database.getConnection();

			return checkFirstTime(isdn, productId, connection);
		} finally {
			Database.closeObject(connection);
		}
	}

	public static boolean checkSecondTimeInDay(String isdn, long productid,
			Connection connection) {
		PreparedStatement stmtSQL = null;
		ResultSet rs = null;
		boolean isSecond = false;
		try {
			String sql = "select * from subscriberproduct "
					+ "where isdn = ? and productid = ? and "
					+ "registerdate > trunc(sysdate) and registerdate < trunc(sysdate + 1) order by registerdate desc";
			stmtSQL = connection.prepareStatement(sql);
			stmtSQL.setString(1, isdn);
			stmtSQL.setLong(2, productid);
			rs = stmtSQL.executeQuery();
			while (rs.next()) {
				if (rs.getInt("isfirsttime") == 1) {
					isSecond = true;
				}
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Database.closeObject(rs);
			Database.closeObject(stmtSQL);
		}
		return isSecond;
	}

	public static boolean isSecondTimeInDay(String isdn, long productId)
			throws Exception {
		Connection connection = null;
		try {
			connection = Database.getConnection();

			return checkSecondTimeInDay(isdn, productId, connection);
		} finally {
			Database.closeObject(connection);
		}
	}

	public static SubscriberGoService getGoServiceData(String isdn,
			long productId, Connection connection) {
		PreparedStatement stmtCountAnswer = null;
		ResultSet rs = null;
		SubscriberGoService subscriberGoService = null;
		try {
			String sql = "select * from subscriberproduct where isdn = ? and productid= ? and supplierstatus = ?";
			stmtCountAnswer = connection.prepareStatement(sql);
			stmtCountAnswer.setString(1, isdn);
			stmtCountAnswer.setLong(2, productId);
			stmtCountAnswer.setInt(3, Constants.SUPPLIER_ACTIVE_STATUS);

			rs = stmtCountAnswer.executeQuery();
			while (rs.next()) {
				SubscriberGoService temp = new SubscriberGoService();
				temp.setNumOfQuestion(rs.getInt("NUMOFQUESTION"));
				temp.setIsdn(isdn);
				temp.setScore(rs.getLong("Score"));
				temp.setLastQuestionId(rs.getLong("LASTQUESTIONID"));
				temp.setListQuestion(rs.getString("QUESTIONLIST"));
				subscriberGoService = temp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Database.closeObject(rs);
			Database.closeObject(stmtCountAnswer);
		}
		return subscriberGoService;
	}

	public static SubscriberGoService getGoServiceData(String isdn,
			long productId) throws Exception {
		Connection connection = null;
		try {
			connection = Database.getConnection();
			return getGoServiceData(isdn, productId, connection);
		} finally {
			Database.closeObject(connection);
		}
	}

	public static void registerGoService(long userId, String userName,
			long subscriberId, String isdn, int subscriberType, long productId,
			long campaignId, String languageId, boolean includeCurrentDay,
			long score, long lastQuestionId, long numberofQuestion,
			int isFirstTime, long merchantId, int opId) throws Exception {
		Connection connection = null;

		try {

			connection = Database.getConnection();

			registerGoService(connection, userId, userName, subscriberId, isdn,
					subscriberType, productId, campaignId, languageId,
					includeCurrentDay, score, lastQuestionId, numberofQuestion,
					isFirstTime, merchantId, opId);
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
		}
	}

	public static void registerGoService(Connection connection, long userId,
			String userName, long subscriberId, String isdn,
			int subscriberType, long productId, long campaignId,
			String languageId, boolean includeCurrentDay, long score,
			long lastQuestionId, long numberofQuestion, int isFirstTime,
			long merchantId, int opId) throws Exception {
		SubscriberProduct subscriberProduct = null;

		PreparedStatement stmtRegister = null;

		try {
			Date now = new Date();

			ProductEntry product = ProductFactory.getCache().getProduct(
					productId);

			// calculate term of use date
			Date termDate = null;

			if (product.getTermPeriod() > 0) {
				termDate = DateUtil.addDate(now, product.getTermUnit(),
						product.getTermPeriod());
			}

			// calculate expire date
			Date expirationDate = null;
			Date graceDate = null;

			if (product.isSubscription()) {
				int quantity = 1;

				int expirationPeriod = product.getSubscriptionPeriod();

				String expirationUnit = product.getSubscriptionUnit();
				expirationDate = calculateExpirationDate(now, expirationUnit,
						expirationPeriod, quantity);

				if (includeCurrentDay) {
					Calendar expiration = Calendar.getInstance();
					expiration.setTime(expirationDate);
					expiration.add(Calendar.DATE, -1);

					expirationDate = expiration.getTime();
				}
				graceDate = calculateGraceDate(expirationDate,
						product.getGraceUnit(), product.getGracePeriod());
			}

			// check product are registered or not
			subscriberProduct = getUnterminated(isdn, productId);

			if (subscriberProduct != null) {
				SubscriberProductImpl.unregisterGoService(userId, userName,
						subscriberProduct.getSubProductId(),
						subscriberProduct.getProductId());
			}

			// register product for subscriber
			String sql = "Insert into SubscriberProduct "
					+ "     (subProductId, userId, userName, createDate, modifiedDate "
					+ "     , subscriberId, isdn, subscriberType, productId, languageId "
					+ "     , registerDate, termDate, expirationDate, graceDate, barringStatus, supplierStatus, CampaignId"
					+ "	  , score, LASTQUESTIONID,NUMOFQUESTION,ISFIRSTTIME, merchantId, telcoId) "
					+ "Values " + "     (?, ?, ?, sysDate, sysDate "
					+ "     , ?, ?, ?, ?, ? " + "     , ?, ?, ?, ?, ?, ?, ?"
					+ "	  , ?, ?, ?, ?, ?, ?)";

			stmtRegister = connection.prepareStatement(sql);

			long subProductId = Database.getSequence(connection,
					"sub_product_seq");

			int barringStatus = Constants.USER_ACTIVE_STATUS;
			int supplierStatus = Constants.SUPPLIER_ACTIVE_STATUS;

			stmtRegister.setLong(1, subProductId);
			stmtRegister.setLong(2, userId);
			stmtRegister.setString(3, userName);

			stmtRegister.setLong(4, subscriberId);
			stmtRegister.setString(5, isdn);
			stmtRegister.setInt(6, subscriberType);
			stmtRegister.setLong(7, productId);
			stmtRegister.setString(8, languageId);

			stmtRegister.setTimestamp(9, DateUtil.getTimestampSQL(now));
			stmtRegister.setTimestamp(10, DateUtil.getTimestampSQL(termDate));
			stmtRegister.setTimestamp(11,
					DateUtil.getTimestampSQL(expirationDate));
			stmtRegister.setTimestamp(12, DateUtil.getTimestampSQL(graceDate));

			stmtRegister.setInt(13, barringStatus);
			stmtRegister.setInt(14, supplierStatus);
			stmtRegister.setLong(15, campaignId);
			// DuyMB add 20140402
			stmtRegister.setLong(16, score);
			stmtRegister.setLong(17, lastQuestionId);
			stmtRegister.setLong(18, numberofQuestion);
			stmtRegister.setLong(19, isFirstTime);
			stmtRegister.setLong(20, merchantId);
			stmtRegister.setLong(21, opId);

			stmtRegister.execute();
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtRegister);
		}
	}

	public static void unregisterGoService(long userId, String userName,
			long subProductId, long productId) throws Exception {
		Connection connection = null;

		try {
			connection = Database.getConnection();

			unregisterGoService(connection, userId, userName, subProductId,
					productId);
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
		}
	}

	public static void unregisterGoService(Connection connection, long userId,
			String userName, long subProductId, long productId)
			throws Exception {
		// SubscriberProduct subscriberProduct = null;

		PreparedStatement stmtSubscription = null;

		try {
			String sql = "Update SubscriberProduct "
					+ "   Set 	userId = ?, userName = ?, modifiedDate = sysDate "
					+ "   		, unregisterDate = sysDate, barringStatus = ?, supplierStatus = ? "
					+ "	  Where subProductId = ? and " + CONDITION_UNTERMINATED;

			stmtSubscription = connection.prepareStatement(sql);

			stmtSubscription.setLong(1, userId);
			stmtSubscription.setString(2, userName);
			stmtSubscription.setInt(3, Constants.USER_CANCEL_STATUS);
			stmtSubscription.setInt(4, Constants.SUPPLIER_CANCEL_STATUS);
			stmtSubscription.setLong(5, subProductId);

			stmtSubscription.execute();

			if (stmtSubscription.getUpdateCount() == 0) {
				throw new AppException(Constants.ERROR_UNREGISTERED);
			}

			// ProductEntry product =
			// ProductFactory.getCache().getProduct(productId);
			//
			// if (product.isAuditEnable())
			// {
			// subscriberProduct = getProduct(connection, subProductId);
			//
			// SubscriberActivateImpl.unregister(
			// connection, userId, userName, subscriberProduct.getSubscriberId()
			// , subscriberProduct.getIsdn(), subscriberProduct.getProductId(),
			// subscriberProduct.getUnregisterDate(),
			// "");
			// }
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtSubscription);
		}
	}

	public static String getQuestion(long productid, String type, String isdn)
			throws Exception {
		Connection connection = null;
		try {
			connection = Database.getConnection();
			return getQuestion(productid, type, isdn, connection);
		} finally {
			Database.closeObject(connection);
		}
	}

	public static String getQuestion(long productid, String type, String isdn,
			Connection connection) throws Exception {
		String strQuestion = "";
		PreparedStatement stmtQuest = null;
		ResultSet rs = null;
		try {
			String sql = "select * from (select * from questiontable where  productid = ? and type = ? and questionid not in"
					+ " (select questionid from questionLog where isdn = ?) order by questionid asc)  where rownum = 1";
			stmtQuest = connection.prepareStatement(sql);

			stmtQuest.setLong(1, productid);
			stmtQuest.setString(2, type);
			stmtQuest.setString(3, isdn);
			rs = stmtQuest.executeQuery();
			while (rs.next()) {
				strQuestion = rs.getString("questionid") + ";"
						+ rs.getString("question");
			}
		} finally {
			Database.closeObject(rs);
			Database.closeObject(stmtQuest);
		}
		return strQuestion;
	}

	public static String getFirstContent(long productid, String type)
			throws Exception {
		String strQuestion = "";
		PreparedStatement stmtQuest = null;
		ResultSet rs = null;
		Connection connection = null;
		try {
			connection = Database.getConnection();
			String sql = "select * from questiontable where type = ? and productid = ?";
			stmtQuest = connection.prepareStatement(sql);

			stmtQuest.setString(1, type);
			stmtQuest.setLong(2, productid);

			rs = stmtQuest.executeQuery();
			while (rs.next()) {
				strQuestion = rs.getString("question");
			}
		} finally {
			Database.closeObject(rs);
			Database.closeObject(stmtQuest);
			Database.closeObject(connection);
		}
		return strQuestion;
	}

	public static String getQuestion(long productid, String type,
			long questionId) throws Exception {
		String strQuestion = "";
		PreparedStatement stmtQuest = null;
		ResultSet rs = null;
		Connection connection = null;
		try {
			connection = Database.getConnection();
			String sql = "select * from questiontable where type = ? and productid = ? and questionId = ?";
			stmtQuest = connection.prepareStatement(sql);

			stmtQuest.setString(1, type);
			stmtQuest.setLong(2, productid);
			stmtQuest.setLong(3, questionId);
			rs = stmtQuest.executeQuery();
			while (rs.next()) {
				strQuestion = rs.getString("question");
			}
		} finally {
			Database.closeObject(rs);
			Database.closeObject(stmtQuest);
			Database.closeObject(connection);
		}
		return strQuestion;
	}

	public static String getAnswer(long questionId, long productid)
			throws Exception {
		Connection connection = null;
		try {
			connection = Database.getConnection();
			return getAnswer(questionId, productid, connection);
		} finally {
			Database.closeObject(connection);
		}
	}

	public static String getAnswer(long questionId, long productid,
			Connection connection) throws Exception {
		String answer = "";
		PreparedStatement stmtAnswer = null;
		ResultSet rs = null;
		try {
			String sql = "select * from questiontable where questionid= ? and productid = ?";

			stmtAnswer = connection.prepareStatement(sql);
			stmtAnswer.setLong(1, questionId);
			stmtAnswer.setLong(2, productid);

			rs = stmtAnswer.executeQuery();
			while (rs.next()) {
				answer = rs.getString("answer");
			}
		} finally {
			Database.closeObject(rs);
			Database.closeObject(stmtAnswer);
		}
		return answer;
	}

	public static void updateScoreGoService(String isdn, long productid,
			long score, long lastQuestionId, long numberofquestion) {
		Connection connection = null;
		try {
			connection = Database.getConnection();
			updateScoreGoService(isdn, productid, score, lastQuestionId,
					numberofquestion, connection);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Database.closeObject(connection);
		}

	}

	public static void updateScoreGoService(String isdn, long productid,
			long score, long lastQuestionId, long numberofquestion,
			Connection connection) throws Exception {
		PreparedStatement stmtUpdateScore = null;
		try {
			String sql = "update subscriberproduct set score = ? , lastquestionid = ?, numofquestion = ?, quantity = 0"
					+ " where isdn =? and productid =? and supplierstatus = 1";
			stmtUpdateScore = connection.prepareStatement(sql);
			stmtUpdateScore.setLong(1, score);
			stmtUpdateScore.setLong(2, lastQuestionId);
			stmtUpdateScore.setLong(3, numberofquestion);
			stmtUpdateScore.setString(4, isdn);
			stmtUpdateScore.setLong(5, productid);

			int updateRecord = stmtUpdateScore.executeUpdate();
			System.out.print(updateRecord);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Database.closeObject(stmtUpdateScore);
		}
	}

	public static int randomizeQuestion(long productid) {
		int totalQuestion = 0;
		Connection connection = null;
		PreparedStatement stmtQuestList = null;
		ResultSet rs = null;
		try {
			connection = Database.getConnection();

			String sql = " select count(*) as total from questiontable where productid = ? and type = 'question'";
			stmtQuestList = connection.prepareStatement(sql);
			stmtQuestList.setLong(1, productid);
			rs = stmtQuestList.executeQuery();

			while (rs.next()) {
				totalQuestion = rs.getInt("total");
			}
		} catch (Exception e) {

		} finally {
			Database.closeObject(rs);
			Database.closeObject(stmtQuestList);
			Database.closeObject(connection);
		}
		Random randomGenerator = new Random();
		return randomGenerator.nextInt(totalQuestion) + 1;
	}

	public static int getTotalQuestion(long productId, Connection connection) {
		PreparedStatement stmtQuest = null;
		ResultSet rs = null;
		int total = 99999;
		try {
			String sqlTotalQuestion = "select count(*) total from questiontable where productid = ? and type = 'question'";

			stmtQuest = connection.prepareStatement(sqlTotalQuestion);
			stmtQuest.setLong(1, productId);

			rs = stmtQuest.executeQuery();
			while (rs.next()) {
				total = rs.getInt("total");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Database.closeObject(rs);
			Database.closeObject(stmtQuest);
		}
		return total;
	}

	public static int getTotalQuestion(long productId) throws Exception {
		Connection connection = null;
		try {
			connection = Database.getConnection();
			return getTotalQuestion(productId, connection);
		} finally {
			Database.closeObject(connection);
		}
	}

	public static boolean isUnregisterInDay(String isdn, long productid)
			throws Exception {
		Connection connection = null;
		try {
			connection = Database.getConnection();
			return isUnregisterInDay(isdn, productid, connection);
		} finally {
			Database.closeObject(connection);
		}
	}

	public static boolean isUnregisterInDay(String isdn, long productid,
			Connection connection) throws Exception {
		PreparedStatement stmtUnreg = null;
		ResultSet rs = null;
		boolean isUnregister = false;
		try {
			String sql = "select * from subscriberproduct "
					+ " where isdn= ? and productid = ? and supplierstatus = ? and registerdate > trunc(sysdate)";
			stmtUnreg = connection.prepareStatement(sql);
			stmtUnreg.setString(1, isdn);
			stmtUnreg.setLong(2, productid);
			stmtUnreg.setLong(3, Constants.SUPPLIER_CANCEL_STATUS);
			rs = stmtUnreg.executeQuery();
			while (rs.next()) {
				isUnregister = true;
				break;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(rs);
			Database.closeObject(stmtUnreg);
		}
		return isUnregister;
	}

	public static void insertSms(String isdn, long productid,
			Connection connection, String content, long questionid,
			String serviceAddress, Date sendDate) throws Exception {
		PreparedStatement stmtInsert = null;
		try {
			String sql = " insert into send_sms_go (id,isdn,productid,content,questionid,senddate,sendflag,serviceaddress) "
					+ " values (?,?,?,?,?,?,?,?)";
			stmtInsert = connection.prepareStatement(sql);
			stmtInsert.setLong(1,
					Database.getSequence(connection, "send_sms_go_seq"));
			stmtInsert.setString(2, isdn);
			stmtInsert.setLong(3, productid);
			stmtInsert.setString(4, content);
			stmtInsert.setLong(5, questionid);
			stmtInsert.setTimestamp(6, DateUtil.getTimestampSQL(sendDate));
			stmtInsert.setLong(7, 0);
			stmtInsert.setString(8, serviceAddress);
			stmtInsert.execute();
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtInsert);
		}
	}

	public static void insertSms(String isdn, long productid, String content,
			long questionid, String serviceAddress, Date sendDate)
			throws Exception {
		Connection connection = null;
		try {
			connection = Database.getConnection();

			insertSms(isdn, productid, connection, content, questionid,
					serviceAddress, sendDate);
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
		}
	}

	public static boolean isSendQuestion(String isdn, long productid)
			throws Exception {
		Connection connection = null;
		try {
			connection = Database.getConnection();

			return isSendQuestion(isdn, productid, connection);
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(connection);
		}
	}

	public static boolean isSendQuestion(String isdn, long productid,
			Connection connection) throws Exception {
		boolean result = true;
		String strSQL = "Select count(*) as total from send_sms_vt_daily where isdn = ? and productid =? and nvl(sendflag,0) = 0 and senddate > trunc(sysdate)";
		PreparedStatement stmtSelect = null;
		ResultSet rs = null;
		try {

			stmtSelect = connection.prepareStatement(strSQL);
			stmtSelect.setString(1, isdn);
			stmtSelect.setLong(2, productid);

			rs = stmtSelect.executeQuery();
			int total = 0;
			while (rs.next()) {
				total = rs.getInt("total");
			}
			if (total > 0) {
				result = false;
			}
		} catch (Exception ex) {
			throw ex;
		} finally {
			Database.closeObject(rs);
			Database.closeObject(stmtSelect);
		}

		return result;
	}

	public static int getNumberOfCode(String isdn, long productid)
			throws Exception {
		Connection connection = null;
		try {
			connection = Database.getConnection();
			return getNumberOfCode(isdn, productid, connection);
		} catch (Exception ex) {
			throw ex;
		} finally {
			Database.closeObject(connection);
		}
	}

	public static int getNumberOfCode(String isdn, long productid,
			Connection connection) {
		PreparedStatement stmtGetNumberOfCode = null;
		ResultSet rs = null;
		int total = 0;
		try {
			String strSQL = "select count(*) as total from coderewardgo where isdn =? and productid=?";
			stmtGetNumberOfCode = connection.prepareStatement(strSQL);
			stmtGetNumberOfCode.setString(1, isdn);
			stmtGetNumberOfCode.setLong(2, productid);
			rs = stmtGetNumberOfCode.executeQuery();
			while (rs.next()) {
				total = rs.getInt("total");
			}
		} catch (Exception ex) {

		} finally {
			Database.closeObject(rs);
			Database.closeObject(stmtGetNumberOfCode);
		}
		return total;
	}

	/*
	 * 
	 * Cap nhan so luong gui tin hang ngay
	 */

	public static void updateQuantity(long userId, String userName,
			long subscriberId, int quantity) throws Exception {
		int quantityTotal = 0;
		Connection connection = null;
		PreparedStatement scoreStmt = null;
		ResultSet rsScrore = null;
		try {
			connection = Database.getConnection();
			// get total score
			String sqlGet = "select quantity from subscriberProduct where subProductId = ?";
			scoreStmt = connection.prepareStatement(sqlGet);
			scoreStmt.setLong(1, subscriberId);
			rsScrore = scoreStmt.executeQuery();
			if (rsScrore.next()) {
				quantityTotal = rsScrore.getInt("quantity");
			}

			quantityTotal = quantityTotal + quantity;

			// update score
			String sqlUpdate = "update SubscriberProduct set userId = ?, userName = ?, modifiedDate = sysdate, quantity = ?, numOfQuestion = 0 where subProductId = ?";

			scoreStmt = connection.prepareStatement(sqlUpdate);
			scoreStmt.setLong(1, userId);
			scoreStmt.setString(2, userName);
			scoreStmt.setInt(3, quantityTotal);
			scoreStmt.setLong(4, subscriberId);

			scoreStmt.executeUpdate();

		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(scoreStmt);
			Database.closeObject(connection);
			Database.closeObject(rsScrore);
		}
	}

	/**
	 * Reset so quantity khi tra loi cau hoi
	 */

	public static void updateQuantityToZero(long userId, String userName,
			long subscriberId) throws Exception {
		Connection connection = null;
		PreparedStatement scoreStmt = null;
		ResultSet rsScrore = null;
		try {
			connection = Database.getConnection();

			// update score
			String sqlUpdate = "update SubscriberProduct set userId = ?, userName = ?, modifiedDate = sysdate, quantity = ? where subProductId = ?";

			scoreStmt = connection.prepareStatement(sqlUpdate);
			scoreStmt.setLong(1, userId);
			scoreStmt.setString(2, userName);
			scoreStmt.setInt(3, 0);
			scoreStmt.setLong(4, subscriberId);

			scoreStmt.executeUpdate();

		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(scoreStmt);
			Database.closeObject(connection);
			Database.closeObject(rsScrore);
		}
	}

	public static void insertQuestionLog(String isdn, long questionId)
			throws Exception {
		PreparedStatement stmtInsert = null;
		Connection connection = null;
		try {
			connection = Database.getConnection();
			String sql = " insert into QuestionLog (Id, questionId, isdn, orderDate, isconfirm) values (?,?,?,sysdate,0)";
			stmtInsert = connection.prepareStatement(sql);

			long id = Database.getSequence(connection, "QUESTIONLOG_SEQ");

			stmtInsert.setLong(1, id);
			stmtInsert.setLong(2, questionId);
			stmtInsert.setString(3, isdn);

			stmtInsert.execute();
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtInsert);
			Database.closeObject(connection);
		}
	}

	public static void insertMOCPQueue(long orderId, Date requestDate,
			String username, String password, String serviceAddress,
			String isdn, String productCode, String cmdCode, String msgBody,
			long opId, String cpUrl) throws Exception {
		PreparedStatement stmtInsert = null;
		Connection connection = null;
		try {
			connection = Database.getConnection();
			String sql = " insert into CPQUEUE (ORDERID, REQUESTDATE, USERNAME, PASSWORD, SERVICEADDRESS, ISDN, "
					+ "PRODUCTCODE, CMDCODE, MSGBODY, OPID, CPURL, RETRY, STATUS) values (?,?,?,?,?,?,?,?,?,?,?,0,1)";
			stmtInsert = connection.prepareStatement(sql);

			stmtInsert.setLong(1, orderId);
			stmtInsert.setTimestamp(2, DateUtil.getTimestampSQL(requestDate));
			stmtInsert.setString(3, username);
			stmtInsert.setString(4, password);
			stmtInsert.setString(5, serviceAddress);
			stmtInsert.setString(6, isdn);
			stmtInsert.setString(7, productCode);
			stmtInsert.setString(8, cmdCode);
			stmtInsert.setString(9, msgBody);
			stmtInsert.setLong(10, opId);
			stmtInsert.setString(11, cpUrl);

			stmtInsert.execute();
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtInsert);
			Database.closeObject(connection);
		}
	}

	public static void updateStatusMoQueue(long orderId, int status, int retry)
			throws Exception {
		PreparedStatement stmtInsert = null;
		Connection connection = null;
		try {
			connection = Database.getConnection();
			String sql = " update CPQUEUE set status = ?, retry = ? where orderId = ?";
			stmtInsert = connection.prepareStatement(sql);

			stmtInsert.setInt(1, 1);

			if (status == 0) {
				retry = retry + 1;
			} else if (status == 1) {
				retry = 0;
			}
			stmtInsert.setInt(2, retry);
			stmtInsert.setLong(3, orderId);

			stmtInsert.execute();
		} catch (Exception e) {
			throw e;
		} finally {
			Database.closeObject(stmtInsert);
			Database.closeObject(connection);
		}
	}
	
	

}
