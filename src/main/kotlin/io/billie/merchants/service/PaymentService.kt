package io.billie.merchants.service

import org.springframework.stereotype.Service

@Service
class PaymentService {
	fun makePaymentToMerchant(merchantId: String, transactionId: String, itemId: String) {
		println("Making payment to Merchant: $merchantId, Transaction: $transactionId, for Item: $itemId")
	}
}