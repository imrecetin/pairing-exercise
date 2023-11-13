package io.billie.merchants.resource

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class MerchantResourceIT {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@Autowired
	private lateinit var objectMapper: ObjectMapper

	@Test
	fun `should return a list of orders for a merchant`() {
		val merchantId = "testMerchantId"

		mockMvc.perform(get("/merchants/$merchantId/orders"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$").isArray)
			.andExpect(jsonPath("$[0].propertyName").value("expectedValue"))
		// Add more assertions based on expected response structure
	}

	@Test
	fun `should handle no orders found for a merchant`() {
		val merchantId = "nonExistentMerchantId"

		mockMvc.perform(get("/merchants/$merchantId/orders"))
			.andExpect(status().isNoContent)
	}

}