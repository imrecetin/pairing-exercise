package io.billie.merchants.service

import io.billie.merchants.data.OrderRepository
import io.billie.merchants.resource.model.OrderStatus
import org.springframework.stereotype.Service

@Service
class InvoiceService(val dbOrder: OrderRepository) {
	fun claimInvoiceToBuyer(merchantId: String, transactionId: String) {
		dbOrder.updateStatus(merchantId, transactionId, OrderStatus.PAID)
		println("Preparing and claiming invoice Merchant: $merchantId, Transaction: $transactionId")
	}
}