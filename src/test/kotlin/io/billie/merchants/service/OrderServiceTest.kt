import io.billie.merchants.data.ItemRepository
import io.billie.merchants.data.OrderRepository
import io.billie.merchants.resource.model.ItemStatus
import io.billie.merchants.resource.model.OrderStatus
import io.billie.merchants.resource.model.request.ItemRequest
import io.billie.merchants.resource.model.request.OrderRequest
import io.billie.merchants.resource.model.response.ItemResponse
import io.billie.merchants.resource.model.response.OrderResponse
import io.billie.merchants.service.InvoiceService
import io.billie.merchants.service.OrderService
import io.billie.merchants.service.PaymentService
import io.billie.merchants.service.exception.item.ItemAlreadyShipped
import io.billie.merchants.service.exception.order.OrderAlreadyExisting
import io.billie.merchants.service.exception.order.OrderedItemNotFound
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@ExtendWith(MockitoExtension::class)
class OrderServiceTest {

	@Mock
	private lateinit var dbOrder: OrderRepository

	@Mock
	private lateinit var dbItem: ItemRepository

	@Mock
	private lateinit var paymentService: PaymentService

	@Mock
	private lateinit var invoiceService: InvoiceService

	@InjectMocks
	private lateinit var orderService: OrderService

	@Test
	fun `should return all orders for a merchant`() {
		val merchantId = "111"
		val transactionId = UUID.randomUUID()
		val orderResponseList =
			listOf(sampleOrderResponse(merchantId, "ORD12345678", transactionId), sampleOrderResponse(merchantId, "ORD123456789", transactionId))

		`when`(dbOrder.ordersBy(merchantId)).thenReturn(orderResponseList)

		val result = orderService.ordersBy(merchantId)

		assertEquals(orderResponseList.size, result.size, "Expected and actual list sizes should match")
	}

	@Test
	fun `should find order by merchant and transaction id`() {
		val merchantId = "111"
		val transactionId = UUID.randomUUID()
		val orderResponse = sampleOrderResponse(merchantId, "ORD12345678", transactionId)

		`when`(dbOrder.orderByTransactionId(merchantId, transactionId.toString())).thenReturn(orderResponse)

		val result = orderService.orderBy(merchantId, transactionId.toString())

		assertTrue(result.isPresent)
		assertTrue(result.get().transactionId == transactionId)
	}

	@Test
	fun `should throw OrderAlreadyExisting exception when placing an existing order`() {
		val merchantId = "111"
		val merchantOrderId = "ORD12345678"
		val transactionId = UUID.randomUUID()
		val orderRequest = sampleOrderRequest(merchantOrderId)
		val existingOrder = sampleOrderResponse(merchantId, merchantOrderId, transactionId)

		`when`(dbOrder.orderByMerchantOrderId(merchantId, orderRequest.orderId)).thenReturn(existingOrder)

		assertThrows<OrderAlreadyExisting> {
			orderService.takePlaceOrder(merchantId, orderRequest)
		}
	}

	@Test
	fun `takePlaceOrder should create items successfully`() {
		val merchantId = "111"
		val merchantOrderId = "44444"
		val orderRequest = sampleOrderRequest(merchantOrderId)
		val transactionUUID = UUID.randomUUID()

		`when`(dbOrder.orderByMerchantOrderId(merchantId, orderRequest.orderId)).thenReturn(null)
		`when`(dbOrder.create(merchantId, orderRequest)).thenReturn(transactionUUID)

		orderService.takePlaceOrder(merchantId, orderRequest)

		verify(dbOrder).orderByMerchantOrderId(merchantId, orderRequest.orderId)
		verify(dbOrder).create(merchantId, orderRequest)
		orderRequest.items.forEach { item ->
			verify(dbItem).create(transactionUUID, item)
		}
		verify(dbOrder).updateStatus(merchantId, transactionUUID.toString(), OrderStatus.SHIPPING)
	}

	@Test
	fun `completeShipment should throw OrderedItemNotFound exception`() {
		val transactionUUID = UUID.randomUUID()
		val merchantId = "111"
		val itemId = "I123"

		`when`(dbItem.retrieveItemBy(transactionUUID.toString(), itemId)).thenReturn(null)

		assertThrows<OrderedItemNotFound> {
			orderService.completeShipment(merchantId, transactionUUID.toString(), itemId)
		}
	}

	@Test
	fun `completeShipment should throw ItemAlreadyShipped exception`() {
		val transactionUUID = UUID.randomUUID()
		val merchantId = "111"
		val itemId = "I123"
		val itemResponse = ItemResponse(
			id = UUID.randomUUID(),
			transactionId = transactionUUID,
			itemId = 123456L,
			price = BigDecimal("29.99"),
			status = ItemStatus.SHIPPED
		)

		`when`(dbItem.retrieveItemBy(transactionUUID.toString(), itemId)).thenReturn(itemResponse)

		assertThrows<ItemAlreadyShipped> {
			orderService.completeShipment(merchantId, transactionUUID.toString(), itemId)
		}
	}

	@Test
	fun `completeShipment should complete shipment successfully`() {
		val transactionUUID = UUID.randomUUID()
		val transactionId = transactionUUID.toString()
		val merchantId = "111"
		val itemId = 123L
		val itemResponse = ItemResponse(
			id = UUID.randomUUID(),
			transactionId = transactionUUID,
			itemId = itemId,
			price = BigDecimal("29.99"),
			status = ItemStatus.ORDERED
		)
		val orderResponse = sampleOrderResponse(merchantId, "ORD12345678", transactionUUID)

		`when`(dbItem.retrieveItemBy(transactionId, itemId.toString())).thenReturn(itemResponse)
		`when`(dbOrder.orderByTransactionId(merchantId, transactionId)).thenReturn(orderResponse)

		orderService.completeShipment(merchantId, transactionId, itemId.toString())

		verify(dbItem).updateStatus(transactionId, itemId.toString(), ItemStatus.SHIPPED)
		verify(paymentService).makePaymentToMerchant(merchantId, transactionId, itemId.toString())
		verify(dbOrder).updateStatus(merchantId, transactionId, OrderStatus.INVOICED_TO_CUSTOMER)
		verify(invoiceService).claimInvoiceToBuyer(merchantId, transactionId)
	}

	// Add more test cases to cover other scenarios and exceptions

	// ...

	private fun sampleOrderResponse(merchantId: String, merchantOrderId: String, transactionId: UUID): OrderResponse {
		return OrderResponse(
			transactionId,
			merchantId,
			merchantOrderId,
			"B789012",
			OrderStatus.SHIPPING,
			listOf(
				ItemResponse(UUID.randomUUID(), transactionId, 123456L, BigDecimal("29.99"), ItemStatus.SHIPPED),
				ItemResponse(UUID.randomUUID(), transactionId, 123457L, BigDecimal("39.99"), ItemStatus.SHIPPED),
			),
			LocalDateTime.parse("10/11/2023 14:30:00", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
		)
	}

	private fun sampleOrderRequest(merchantOrderId: String): OrderRequest {
		return OrderRequest(
			merchantOrderId,
			"B789012",
			listOf(ItemRequest(1, BigDecimal(25.99)), ItemRequest(2, BigDecimal(49.99))),
			LocalDateTime.parse("10/11/2023 14:30:00", java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
		)
	}
}