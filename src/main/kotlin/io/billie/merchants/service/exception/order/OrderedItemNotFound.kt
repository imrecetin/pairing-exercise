package io.billie.merchants.service.exception.order

class OrderedItemNotFound(val orderId: String) : RuntimeException()