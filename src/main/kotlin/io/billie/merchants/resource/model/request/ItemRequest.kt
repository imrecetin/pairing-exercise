package io.billie.merchants.resource.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import javax.validation.constraints.NotBlank

data class ItemRequest(
	@field:NotBlank
	@JsonProperty("item_id")
	val itemId: Long,

	@field:NotBlank
	@JsonProperty("price")
	val price: BigDecimal,
)
