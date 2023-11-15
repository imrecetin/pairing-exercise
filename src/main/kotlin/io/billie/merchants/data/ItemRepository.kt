package io.billie.merchants.data

import io.billie.merchants.resource.model.ItemStatus
import io.billie.merchants.resource.model.request.ItemRequest
import io.billie.merchants.resource.model.response.ItemResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ItemRepository {

	@Autowired
	lateinit var jdbcTemplate: JdbcTemplate

	fun retrieveItemBy(transactionId: UUID): List<ItemResponse> {
		val itemSql = "SELECT * FROM organisations_schema.items WHERE transaction_id = ?"
		return jdbcTemplate.query(itemSql, arrayOf(transactionId)) { rs, _ ->
			ItemResponse(
				id = rs.getObject("id", UUID::class.java),
				transactionId = rs.getObject("transaction_id", UUID::class.java),
				itemId = rs.getLong("item_id"),
				price = rs.getBigDecimal("price"),
				status = ItemStatus.valueOf(rs.getString("status"))
			)
		}
	}

	fun retrieveItemBy(transactionId: String, itemId: String): ItemResponse? {
		val itemSql = "SELECT * FROM organisations_schema.items WHERE transaction_id = ? and item_id = ?"
		return jdbcTemplate.query(itemSql, arrayOf(transactionId, itemId)) { rs, _ ->
			ItemResponse(
				id = rs.getObject("id", UUID::class.java),
				transactionId = rs.getObject("transaction_id", UUID::class.java),
				itemId = rs.getLong("item_id"),
				price = rs.getBigDecimal("price"),
				status = ItemStatus.valueOf(rs.getString("status"))
			)
		}.firstOrNull()
	}

	fun create(transactionId: UUID, itemRequest: ItemRequest): UUID {
		val keyHolder: KeyHolder = GeneratedKeyHolder()
		jdbcTemplate.update(
			{ connection ->
				val ps = connection.prepareStatement(
					"INSERT INTO organisations_schema.items (" +
							"transaction_id, " +
							"item_id, " +
							"price, " +
							"status" +
							") VALUES (?, ?, ?, ?)",
					arrayOf("id")
				)
				ps.setObject(1, transactionId)
				ps.setLong(2, itemRequest.itemId)
				ps.setBigDecimal(3, itemRequest.price)
				ps.setString(4, ItemStatus.ORDERED.name)
				ps
			}, keyHolder
		)
		return keyHolder.getKeyAs(UUID::class.java)!!
	}

	fun updateStatus(transactionId: String, itemId: String, status: ItemStatus): UUID {
		val keyHolder: KeyHolder = GeneratedKeyHolder()
		jdbcTemplate.update(
			{ connection ->
				val ps = connection.prepareStatement(
					"UPDATE organisations_schema.items " +
							"SET status = ?" +
							"WHERE item_id = ? AND transaction_id = ?",
					arrayOf("id")
				)
				ps.setString(1, status.name)
				ps.setString(2, itemId)
				ps.setObject(3, transactionId)
				ps
			},
			keyHolder
		)
		return keyHolder.getKeyAs(UUID::class.java)!!
	}
}