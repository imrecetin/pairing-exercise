package io.billie.merchants.service

import org.springframework.stereotype.Service

@Service
class InvoiceService {
	fun claimInvoiceToBuyer(merchantId: String, transactionId: String) {
		println("Preparing and claiming invoice Merchant: $merchantId, Transaction: $transactionId")
	}
}