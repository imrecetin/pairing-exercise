package io.billie.merchants.resource

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.billie.merchants.resource.model.response.OrderResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class MerchantResourceIT {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@Autowired
	private lateinit var objectMapper: ObjectMapper

	@Test
	fun `should return a list of orders for a merchant`() {
		val merchantId = "111"

		val result = mockMvc.perform(get("/merchants/$merchantId/orders"))
			.andExpect(status().isOk)
			.andReturn()

		val response = objectMapper.readValue(result.response.contentAsString, object : TypeReference<List<OrderResponse>>() {})
		Assertions.assertEquals(1, response.size)
		Assertions.assertEquals(merchantId, response[0].merchantId)
		Assertions.assertEquals(3, response[0].items.size)
	}

	@Test
	fun `should handle no orders found for a merchant`() {
		val merchantId = "1111"

		mockMvc.perform(get("/merchants/$merchantId/orders"))
			.andExpect(status().isNoContent)
	}

	@Test
	fun `should return order for a merchant with specified transaction`() {
		val merchantId = "111"
		val foundOrder = mockMvc.perform(get("/merchants/$merchantId/orders"))
			.andExpect(status().isOk)
			.andReturn()

		val orderResponse = objectMapper.readValue(foundOrder.response.contentAsString, object : TypeReference<List<OrderResponse>>() {})

		val transactionId = orderResponse.get(0).transactionId.toString();
		val result = mockMvc.perform(get("/merchants/$merchantId/orders/$transactionId"))
			.andExpect(status().isOk)
			.andReturn()

		val response = objectMapper.readValue(result.response.contentAsString, OrderResponse::class.java)
		Assertions.assertEquals(merchantId, response.merchantId)
		Assertions.assertEquals(3, response.items.size)
	}

	@Test
	fun `should handle no orders found for a merchant with specified transaction`() {
		val merchantId = "111"
		val transactionId = UUID.randomUUID();

		mockMvc.perform(get("/merchants/$merchantId/orders/$transactionId"))
			.andExpect(status().isNoContent)
	}

	// Add more test cases to cover other scenarios and exceptions

	// ...

}