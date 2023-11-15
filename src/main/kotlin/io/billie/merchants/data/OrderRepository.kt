package io.billie.merchants.data

import io.billie.merchants.resource.model.OrderStatus
import io.billie.merchants.resource.model.request.OrderRequest
import io.billie.merchants.resource.model.response.OrderResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.*

@Repository
class OrderRepository(val dbItem: ItemRepository) {

	@Autowired
	lateinit var jdbcTemplate: JdbcTemplate

	fun ordersBy(merchantId: String): List<OrderResponse> {
		val sql = "SELECT * FROM organisations_schema.orders WHERE merchant_id = ? "
		return jdbcTemplate.query(sql, arrayOf(merchantId)) { rs, _ ->
			populateOrderResponse(rs)
		}
	}

	fun orderByTransactionId(merchantId: String, transactionId: String): OrderResponse? {
		val sql = "SELECT * FROM organisations_schema.orders WHERE merchant_id = ? AND transaction_id = ?"
		return jdbcTemplate.query(sql, arrayOf(merchantId, UUID.fromString(transactionId))) { rs, _ ->
			populateOrderResponse(rs)
		}.firstOrNull()
	}

	fun orderByMerchantOrderId(merchantId: String, merchantOrderId: String): OrderResponse? {
		val sql = "SELECT * FROM organisations_schema.orders WHERE merchant_id = ? AND merchant_order_id = ?"
		return jdbcTemplate.query(sql, arrayOf(merchantId, merchantOrderId)) { rs, _ ->
			populateOrderResponse(rs)
		}.firstOrNull()
	}

	fun create(merchantId: String, orderRequest: OrderRequest): UUID {
		val keyHolder: KeyHolder = GeneratedKeyHolder()
		jdbcTemplate.update(
			{ connection ->
				val ps = connection.prepareStatement(
					"INSERT INTO organisations_schema.orders (" +
							"merchant_id, " +
							"merchant_order_id, " +
							"buyer_id, " +
							"status, " +
							"order_created_at" +
							") VALUES (?, ?, ?, ?, ?)",
					arrayOf("id")
				)
				ps.setString(1, merchantId)
				ps.setString(2, orderRequest.orderId)
				ps.setString(3, orderRequest.buyerId)
				ps.setString(4, OrderStatus.ORDERED.name)
				ps.setTimestamp(5, Timestamp.valueOf(orderRequest.orderCreatedAt))
				ps
			}, keyHolder
		)
		return keyHolder.getKeyAs(UUID::class.java)!!
	}

	fun updateStatus(merchantId: String, transactionId: String, status: OrderStatus): UUID {
		val keyHolder: KeyHolder = GeneratedKeyHolder()
		jdbcTemplate.update(
			{ connection ->
				val ps = connection.prepareStatement(
					"UPDATE organisations_schema.orders " +
							"SET status = ?" +
							"WHERE merchant_id = ? AND transaction_id = ?",
					arrayOf("id")
				)
				ps.setString(1, status.name)
				ps.setString(2, merchantId)
				ps.setObject(3, transactionId)
				ps
			},
			keyHolder
		)
		return keyHolder.getKeyAs(UUID::class.java)!!
	}

	private fun populateOrderResponse(rs: ResultSet) = OrderResponse(
		transactionId = rs.getObject("transaction_id", UUID::class.java),
		merchantId = rs.getString("merchant_id"),
		merchantOrderId = rs.getString("merchant_order_id"),
		buyerId = rs.getString("buyer_id"),
		status = OrderStatus.valueOf(rs.getString("status")),
		orderCreatedAt = rs.getTimestamp("order_created_at").toLocalDateTime(),
		items = dbItem.retrieveItemBy(rs.getObject("transaction_id", UUID::class.java))
	)
}