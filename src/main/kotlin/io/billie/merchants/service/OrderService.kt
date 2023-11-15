package io.billie.merchants.service

import io.billie.merchants.data.ItemRepository
import io.billie.merchants.data.OrderRepository
import io.billie.merchants.resource.model.ItemStatus
import io.billie.merchants.resource.model.OrderStatus
import io.billie.merchants.resource.model.request.OrderRequest
import io.billie.merchants.resource.model.response.OrderResponse
import io.billie.merchants.service.exception.item.AllItemsAlreadyShipped
import io.billie.merchants.service.exception.item.ItemAlreadyShipped
import io.billie.merchants.service.exception.order.OrderAlreadyExisting
import io.billie.merchants.service.exception.order.OrderNotFound
import io.billie.merchants.service.exception.order.OrderedItemNotFound
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class OrderService(val dbOrder: OrderRepository, val dbItem: ItemRepository, val paymentService: PaymentService, val invoiceService: InvoiceService) {

	@Transactional(readOnly = true)
	fun ordersBy(merchantId: String): List<OrderResponse> {
		return dbOrder.ordersBy(merchantId)
	}

	@Transactional(readOnly = true)
	fun orderBy(merchantId: String, transactionId: String): Optional<OrderResponse> {
		val order = dbOrder.orderByTransactionId(merchantId, transactionId) ?: return Optional.empty()
		return Optional.of(order)
	}

	@Transactional
	fun takePlaceOrder(merchantId: String, orderRequest: OrderRequest): String {
		val order = dbOrder.orderByMerchantOrderId(merchantId, orderRequest.orderId)
		if (order != null)
			throw OrderAlreadyExisting(order.transactionId)
		val transactionUUID = dbOrder.create(merchantId, orderRequest)
		val itemRequests = orderRequest.items
		itemRequests.forEach { itemRequest ->
			dbItem.create(transactionUUID, itemRequest)
		}
		dbOrder.updateStatus(merchantId, transactionUUID.toString(), OrderStatus.SHIPPING)
		return transactionUUID.toString()
	}

	@Transactional
	fun completeShipment(merchantId: String, transactionId: String, itemId: String) {
		val item = dbItem.retrieveItemBy(transactionId, itemId) ?: throw OrderedItemNotFound(itemId)
		if (ItemStatus.ORDERED != item.status)
			throw ItemAlreadyShipped(itemId)
		dbItem.updateStatus(transactionId, itemId, ItemStatus.SHIPPED)
		paymentService.makePaymentToMerchant(merchantId, transactionId, itemId)
		val order = dbOrder.orderByTransactionId(merchantId, transactionId)
		if (order?.items?.count { it.status == ItemStatus.ORDERED } == 0) {
			dbOrder.updateStatus(merchantId, transactionId, OrderStatus.INVOICED_TO_CUSTOMER)
			invoiceService.claimInvoiceToBuyer(merchantId, transactionId)
		}
	}

	@Transactional
	fun completeShipment(merchantId: String, transactionId: String) {
		val order = dbOrder.orderByTransactionId(merchantId, transactionId) ?: throw OrderNotFound(transactionId)
		var shippedItemCount = 0
		order.items.filter { it.status == ItemStatus.ORDERED }.forEach { item ->
			val itemId = item.itemId.toString()
			dbItem.updateStatus(transactionId, itemId, ItemStatus.SHIPPED)
			paymentService.makePaymentToMerchant(merchantId, transactionId, itemId)
			shippedItemCount++
		}
		if (shippedItemCount > 0)
			throw AllItemsAlreadyShipped(transactionId)
		dbOrder.updateStatus(merchantId, transactionId, OrderStatus.INVOICED_TO_CUSTOMER)
		invoiceService.claimInvoiceToBuyer(merchantId, transactionId)
	}

}