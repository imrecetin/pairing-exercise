package io.billie.merchants.service.exception.order

class OrderNotFound(val transactionId: String) : RuntimeException()