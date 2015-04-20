package com.crm.provisioning.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;

import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueSession;

import com.crm.kernel.message.Constants;
import com.crm.kernel.queue.QueueFactory;
import com.crm.kernel.sql.Database;
import com.crm.product.cache.ProductFactory;
import com.crm.product.cache.ProductRoute;
import com.crm.provisioning.message.CommandMessage;
import com.crm.subscriber.impl.SubscriberOrderImpl;
import com.crm.thread.DispatcherInstance;
import com.crm.thread.util.ThreadUtil;
import com.crm.util.DateUtil;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright:
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author: HungPQ
 * @version 1.0
 */

public class OrderResponseInstance extends DispatcherInstance {
	private Connection connection = null;
	private PreparedStatement stmtUpdateOrder = null;
	private PreparedStatement stmtUpdateSubscription = null;

	private QueueSession session = null;
	private MessageProducer producer = null;

	public OrderResponseInstance() throws Exception {
		super();
	}

	public OrderResponseThread getDispatcher() {
		return (OrderResponseThread) dispatcher;
	}

	// //////////////////////////////////////////////////////
	// process session
	// Author : ThangPV
	// Created Date : 16/09/2004
	// //////////////////////////////////////////////////////
	public void beforeProcessSession() throws Exception {
		super.beforeProcessSession();

		if (QueueFactory.queueServerEnable && dispatcher.queueDispatcherEnable) {
			Queue sendQueue = QueueFactory.getQueue(dispatcher.queueName);

			session = dispatcher.getQueueSession();
			producer = QueueFactory.createProducer(session, sendQueue,
					getDispatcher().timeout);
		}

		connection = Database.getConnection();
		connection.setAutoCommit(false);

		String SQL = "Update SubscriberOrder "
				+ " Set	modifiedDate = sysdate, channel = ?, orderType = ? "
				+ "		, subscriberId = ?, subProductId = ?, productId = ? "
				+ "		, isdn = ?, subscriberType = ?, price = ?, quantity = ? "
				+ "		, discount = ?, amount = ?, score = ? "
				+ " 	, status = ?, cause = ?, description = ?, delivery_status = ?, cpurlrequest = ?, questionId = ?"
				+ "Where createDate >= trunc(?) and createDate < (trunc(?) + 1) and orderId = ? ";

		stmtUpdateOrder = connection.prepareStatement(SQL);

		SQL = "Update SubscriberProduct Set subscriptionStatus = 0 Where subProductId = ?";

		stmtUpdateSubscription = connection.prepareStatement(SQL);
	}

	// //////////////////////////////////////////////////////
	// process session
	// Author : ThangPV
	// Created Date : 16/09/2004
	// //////////////////////////////////////////////////////
	public void afterProcessSession() throws Exception {
		QueueFactory.closeQueue(producer);
		QueueFactory.closeQueue(session);

		Database.closeObject(stmtUpdateOrder);
		Database.closeObject(stmtUpdateSubscription);
		Database.closeObject(connection);

		super.afterProcessSession();
	}

	// //////////////////////////////////////////////////////
	// process session
	// Author : ThangPV
	// Created Date : 16/09/2004
	// //////////////////////////////////////////////////////
	public int processMessage(CommandMessage message) throws Exception {
		boolean updated = false;
		boolean missing = false;

		long startTime = System.currentTimeMillis();
		long sqlCostTime = 0;
		long startSendToQueue = 0;
		long endSendToQueue = 0;
		long startUpdate = 0;
		long endUpdate = 0;
		long startCreateMessage = 0;
		long endCreateMessage = 0;
		ProductRoute orderRoute = null;
		try {
			orderRoute = ProductFactory.getCache().getProductRoute(
					message.getRouteId());

			if (((orderRoute != null) && orderRoute.isCreateOrder())
					|| (message.getSubProductId() != Constants.DEFAULT_ID)) {
				sqlCostTime = System.currentTimeMillis() - startTime;

				if (sqlCostTime > 100) {
					logMonitor("take long time when database connection : "
							+ sqlCostTime);
				}
			}

			String correlationId = message.getCorrelationID();

			// if (message.getChannel().equals(Constants.CHANNEL_SMS))
			// {
			// return Constants.BIND_ACTION_SUCCESS;
			// }
			// else if ((orderRoute == null) || !orderRoute.isSynchronous())
			// {
			// return Constants.BIND_ACTION_SUCCESS;
			// }
			// else if (correlationId.equals(""))
			// {
			// return Constants.BIND_ACTION_NONE;
			// }

			Object waitingRequest = QueueFactory.callbackListerner
					.get(correlationId);

			if (waitingRequest != null) {
				// Using in simulater case.
				synchronized (waitingRequest) {
					QueueFactory.callbackOrder.put(correlationId, message);

					try {
						waitingRequest.notifyAll();
					} catch (Exception e) {
						logMonitor(e);
					}
				}
			} else if (dispatcher.queueDispatcherEnable) {
				try {
					startCreateMessage = System.currentTimeMillis();

					Message response = QueueFactory.createObjectMessage(
							session, message);
					endCreateMessage = System.currentTimeMillis();
					startSendToQueue = System.currentTimeMillis();
					if (!isTimeOut(message.getOrderDate().getTime(),
							System.currentTimeMillis(), message.getTimeout())) {
						if (message.getOpId() == 2 || message.getOpId() == 1) {

							if (message.getActionType().equals(
									Constants.ACTION_REGISTER)) {
								// khong response lai queue
							} else if (message.getActionType().equals(
									Constants.ACTION_UNREGISTER)) {
								// khong response lai queue
							} else if (message.getActionType().equals(
									"unregistered")) {
								// khong response lai queue
							} else if (message.getActionType().equals(
									"registered")) {
								// khong response lai queue
							} else if (message.getActionType()
									.equals("send-mt")) {
								//khong response lai queue
							} else {
								// debugMonitor("Response lai MT, ActionType " +
								// message.getActionType());
								producer.send(response);
							}

						} else {
							producer.send(response);
						}

					}
					endSendToQueue = System.currentTimeMillis();
				} catch (Exception e) {
					QueueFactory.attachLocal(QueueFactory.ORDER_RESPONSE_QUEUE,
							message);

					logMonitor(e);
				}
			} else {
				missing = true;
			}

			if ((orderRoute != null) && orderRoute.isCreateOrder()) {
				startUpdate = System.currentTimeMillis();
				stmtUpdateOrder.setString(1, message.getChannel());
				stmtUpdateOrder.setString(2, message.getActionType());
				stmtUpdateOrder.setLong(3, message.getSubscriberId());
				stmtUpdateOrder.setLong(4, message.getSubProductId());
				stmtUpdateOrder.setLong(5, message.getProductId());
				stmtUpdateOrder.setString(6, message.getIsdn());
				stmtUpdateOrder.setInt(7, message.getSubscriberType());
				stmtUpdateOrder.setDouble(8, message.getPrice());
				stmtUpdateOrder.setDouble(9, message.getQuantity());
				stmtUpdateOrder.setDouble(10, message.getDiscount());
				stmtUpdateOrder.setDouble(11, message.getAmount());
				stmtUpdateOrder.setDouble(12, message.getScore());
				stmtUpdateOrder.setInt(13, message.getStatus());
				stmtUpdateOrder.setString(14, message.getCause());
				stmtUpdateOrder.setString(15, message.getDescription());
				stmtUpdateOrder.setString(16, message.getIdentifier());
				stmtUpdateOrder.setString(17, message.getParameters()
						.getString("cpurl", ""));
				stmtUpdateOrder.setLong(18,
						message.getParameters().getLong("QuestionId", 0));

				stmtUpdateOrder.setTimestamp(19, DateUtil
						.getTimestampSQL(Calendar.getInstance().getTime()));
				stmtUpdateOrder.setTimestamp(20, DateUtil
						.getTimestampSQL(Calendar.getInstance().getTime()));
				stmtUpdateOrder.setLong(21, message.getOrderId());

				stmtUpdateOrder.execute();

				updated = true;
			}

			if (message.getSubProductId() != Constants.DEFAULT_ID) {
				stmtUpdateSubscription.setLong(1, message.getSubProductId());
				stmtUpdateSubscription.execute();

				updated = true;
			}

			if (updated) {
				connection.commit();
				endUpdate = System.currentTimeMillis();
			}
		} catch (Exception e) {
			Database.rollback(connection);

			throw e;
		} finally {
			long costTime = System.currentTimeMillis() - startTime;
			boolean highCost = ((sqlCostTime > 100) || ((costTime - sqlCostTime) > 100));

			if (highCost || dispatcher.displayDebug) {
				StringBuilder sbLog = new StringBuilder();

				sbLog.append("orderId = ");
				sbLog.append(message.getOrderId());
				sbLog.append(", orderDate = ");
				sbLog.append(ThreadUtil.logTimestamp(message.getOrderDate()));
				sbLog.append(", isdn = ");
				sbLog.append(message.getIsdn());
				sbLog.append(", status = ");
				sbLog.append(message.getStatus());
				sbLog.append(", cause = ");
				sbLog.append(message.getCause());
				sbLog.append(", description = ");
				sbLog.append(message.getDescription());
				sbLog.append(", response date: ");
				sbLog.append(ThreadUtil.logTimestamp(message.getResponseTime()));
				sbLog.append(", order cost: ");
				sbLog.append(System.currentTimeMillis()
						- message.getOrderDate().getTime());
				sbLog.append(", update cost: ");
				sbLog.append(endUpdate - startUpdate);
				sbLog.append(", Queue cost: ");
				sbLog.append(endSendToQueue - startSendToQueue);
				sbLog.append(", Create cost: ");
				sbLog.append(endCreateMessage - startCreateMessage);

				if (missing) {
					sbLog.append(", warning: missing destination");
				}

				if (highCost) {
					logMonitor(sbLog.toString());
				} else {
					debugMonitor(sbLog.toString());
				}
			}
		}

		return Constants.BIND_ACTION_SUCCESS;
	}

	public boolean isTimeOut(long startTime, long endTime, long timeout) {
		return (endTime - startTime) > timeout;
	}
}
