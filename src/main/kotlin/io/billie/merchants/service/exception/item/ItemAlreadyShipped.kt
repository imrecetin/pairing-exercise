package io.billie.merchants.service.exception.item

class ItemAlreadyShipped(val orderId: String) : RuntimeException()