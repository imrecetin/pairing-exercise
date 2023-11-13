package io.billie.merchants.service.exception.item

class AllItemsAlreadyShipped(val transactionId: String) : RuntimeException()