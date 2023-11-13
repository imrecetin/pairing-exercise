import io.billie.merchants.data.ItemRepository
import io.billie.merchants.data.OrderRepository
import io.billie.merchants.resource.model.request.OrderRequest
import io.billie.merchants.resource.model.response.OrderResponse
import io.billie.merchants.service.InvoiceService
import io.billie.merchants.service.OrderService
import io.billie.merchants.service.PaymentService
import io.billie.merchants.service.exception.order.OrderAlreadyExisting
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
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
		val orderResponseList = listOf(OrderResponse(/* fill in with sample data */))

		`when`(dbOrder.orderBy(merchantId)).thenReturn(orderResponseList)

		val result = orderService.allOrders(merchantId)

		// Add assertions based on the expected behavior
	}

	@Test
	fun `should find order by merchant and transaction id`() {
		val merchantId = "111"
		val transactionId = "11111"
		val orderResponse = OrderResponse(/* fill in with sample data */)

		`when`(dbOrder.orderByTransactionId(merchantId, transactionId)).thenReturn(orderResponse)

		val result = orderService.findOrderBy(merchantId, transactionId)

		// Add assertions based on the expected behavior
	}

	@Test
	fun `should throw OrderAlreadyExisting exception when placing an existing order`() {
		val merchantId = "111"
		val orderRequest = OrderRequest(/* fill in with sample data */)
		val existingOrder = OrderResponse(/* fill in with sample data */)

		`when`(dbOrder.orderByMerchantOrderId(merchantId, orderRequest.orderId)).thenReturn(existingOrder)

		assertThrows<OrderAlreadyExisting> {
			orderService.takePlaceOrder(merchantId, orderRequest)
		}
	}

	// Add more test cases to cover other scenarios and exceptions

	// ...

}