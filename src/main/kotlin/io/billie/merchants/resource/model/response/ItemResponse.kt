package io.billie.merchants.resource.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.billie.merchants.resource.model.ItemStatus
import java.math.BigDecimal
import java.util.*

data class ItemResponse(
	@JsonProperty("id")
	val id: UUID,

	@JsonProperty("transaction_id")
	val transactionId: UUID,

	@JsonProperty("item_id")
	val itemId: Long,

	@JsonProperty("price")
	val price: BigDecimal,

	@JsonProperty("status")
	val status: ItemStatus,
)