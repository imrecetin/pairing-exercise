package io.billie.merchants.resource.model.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.billie.merchants.resource.model.OrderStatus
import java.time.LocalDateTime
import java.util.*

data class OrderResponse(
	@JsonProperty("transaction_id")
	val transactionId: UUID,

	@JsonProperty("merchant_id")
	val merchantId: String,

	@JsonProperty("merchant_order_id")
	val merchantOrderId: String,

	@JsonProperty("buyer_id")
	val buyerId: String,

	@JsonProperty("status")
	val status: OrderStatus,

	@JsonProperty("items")
	val items: List<ItemResponse>,

	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	@JsonProperty("order_created_at")
	val orderCreatedAt: LocalDateTime
)