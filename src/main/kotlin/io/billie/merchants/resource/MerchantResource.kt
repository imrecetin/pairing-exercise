package io.billie.merchants.resource

import io.billie.merchants.resource.model.request.OrderRequest
import io.billie.merchants.resource.model.response.OrderResponse
import io.billie.merchants.service.OrderService
import io.billie.merchants.service.exception.item.AllItemsAlreadyShipped
import io.billie.merchants.service.exception.item.ItemAlreadyShipped
import io.billie.merchants.service.exception.order.OrderAlreadyExisting
import io.billie.merchants.service.exception.order.OrderNotFound
import io.billie.merchants.service.exception.order.OrderedItemNotFound
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.CollectionUtils
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import javax.validation.Valid

@RestController
@RequestMapping("merchants")
class MerchantResource(val order: OrderService) {

	@GetMapping("/{merchantId}/orders")
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "200",
				description = "All orders with specified merchant",
				content = [
					(Content(
						mediaType = "application/json",
						array = (ArraySchema(schema = Schema(implementation = OrderResponse::class)))
					))]
			),
			ApiResponse(
				responseCode = "204",
				description = "No any orders with specified merchant",
				content = [Content()]
			)
		]
	)
	fun orders(@PathVariable("merchantId") merchantId: String): List<OrderResponse> {
		val foundOrders = order.allOrders(merchantId)
		if (CollectionUtils.isEmpty(foundOrders)) {
			throw ResponseStatusException(
				HttpStatus.NO_CONTENT,
				"No order found for $merchantId"
			)
		}
		return foundOrders
	}

	@GetMapping("/{merchantId}/orders/{transactionId}")
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "200",
				description = "Order with specified merchant and transaction",
				content = [
					(Content(
						mediaType = "application/json",
						array = (ArraySchema(schema = Schema(implementation = OrderResponse::class), minItems = 1, maxItems = 1))
					))]
			),
			ApiResponse(
				responseCode = "204",
				description = "No any order with specified merchant and transaction",
				content = [Content()]
			)
		]
	)
	fun orderBy(@PathVariable("merchantId") merchantId: String, @PathVariable("transactionId") transactionId: String): OrderResponse {
		val foundOrderResponse = order.findOrderBy(merchantId, transactionId)
		if (!foundOrderResponse.isPresent) {
			throw ResponseStatusException(
				HttpStatus.NO_CONTENT,
				"No order found for $merchantId and $transactionId"
			)
		}
		return foundOrderResponse.get()
	}

	//TODO Idempotency with unique Order ID provided by merchant??
	@PutMapping("/{merchantId}/orders")
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "200",
				description = "Accepted the new order",
				content = [
					Content(
						mediaType = "application/json",
						array = (ArraySchema(schema = Schema(implementation = String::class)))
					)
				]
			),
			ApiResponse(
				responseCode = "422",
				description = "Order has already been created with specified transaction",
				content = [Content()]
			)
		]
	)
	fun createOrder(@PathVariable("merchantId") merchantId: String, @Valid @RequestBody orderRequest: OrderRequest): ResponseEntity<String> {
		return try {
			val transactionId = order.takePlaceOrder(merchantId, orderRequest)
			ResponseEntity.ok("Order has been created with transaction: $transactionId")
		} catch (e: OrderAlreadyExisting) {
			ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body("Order has already been created with transaction: ${e.transactionId}")
		}
	}

	//TODO Idempotency with unique Order ID provided by merchant??
	@PatchMapping("/{merchantId}/orders/{transactionId}/items/{itemId}:completeShipment")
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "200",
				description = "Accepted the new order",
				content = [
					Content(
						mediaType = "application/json",
						array = (ArraySchema(schema = Schema(implementation = String::class)))
					)
				]
			),
			ApiResponse(
				responseCode = "204",
				description = "Item not found with specified transaction",
				content = [Content()]
			),
			ApiResponse(
				responseCode = "422",
				description = "Item with specified transaction have already been shipped",
				content = [Content()]
			)
		]
	)
	fun completeShipmentByItem(
		@PathVariable("merchantId") merchantId: String,
		@PathVariable("transactionId") transactionId: String,
		@PathVariable("itemId") itemId: String
	): ResponseEntity<String> {
		return try {
			order.completeShipment(merchantId, transactionId, itemId)
			return ResponseEntity.ok(itemId)
		} catch (e: OrderedItemNotFound) {
			ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found for orderId ${e.orderId}")
		} catch (e: ItemAlreadyShipped) {
			ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body("Item with orderId ${e.orderId} have already been shipped")
		}
	}

	//TODO Idempotency with unique Order ID provided by merchant??
	@ApiResponses(
		value = [
			ApiResponse(
				responseCode = "200",
				description = "Accepted the new order",
				content = [
					Content(
						mediaType = "application/json",
						array = (ArraySchema(schema = Schema(implementation = String::class)))
					)
				]
			),
			ApiResponse(
				responseCode = "204",
				description = "Order not found specified transaction",
				content = [Content()]
			),
			ApiResponse(
				responseCode = "422",
				description = "All items with specified transactionId have already been shipped",
				content = [Content()]
			)
		]
	)
	@PatchMapping("/{merchantId}/orders/{transactionId}/items:completeShipment")
	fun completeShipment(
		@PathVariable("merchantId") merchantId: String,
		@PathVariable("transactionId") transactionId: String
	): ResponseEntity<String> {
		return try {
			order.completeShipment(merchantId, transactionId)
			return ResponseEntity.ok(transactionId)
		} catch (e: OrderNotFound) {
			ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found for transactionId: ${e.transactionId}")
		} catch (e: AllItemsAlreadyShipped) {
			ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body("All items with transactionId ${e.transactionId} have already been shipped")
		}
	}

}