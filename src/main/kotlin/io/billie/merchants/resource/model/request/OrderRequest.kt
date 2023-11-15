package io.billie.merchants.resource.model.request

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank

data class OrderRequest(
	@field:NotBlank
	@JsonProperty("order_id")
	val orderId: String,

	@field:NotBlank
	@JsonProperty("buyer_id")
	val buyerId: String,

	@field:NotBlank
	@JsonProperty("items")
	val items: List<ItemRequest>,

	@field:NotBlank
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	@JsonProperty("order_created_at")
	val orderCreatedAt: LocalDateTime,
)