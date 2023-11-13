package io.billie.merchants.service

import io.billie.merchants.data.ItemRepository
import io.billie.merchants.resource.model.ItemStatus
import org.springframework.stereotype.Service

@Service
class PaymentService(val dbItem: ItemRepository) {
	fun makePaymentToMerchant(merchantId: String, transactionId: String, itemId: String) {
		dbItem.updateStatus(transactionId, itemId, ItemStatus.PAID_TO_MERCHANT)
		println("Making payment to Merchant: $merchantId, Transaction: $transactionId, for Item: $itemId")
	}
}