package io.billie.merchants.service.exception.order

import java.util.*

class OrderAlreadyExisting(val transactionId: UUID) : RuntimeException()