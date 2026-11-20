package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.model.CartItem
import com.example.oylfrutossecos.data.model.Product
import com.example.oylfrutossecos.data.remote.api.OrderApiService
import com.example.oylfrutossecos.data.remote.dto.OrderDto
import com.example.oylfrutossecos.data.remote.dto.OrderItemDto
import com.example.oylfrutossecos.data.remote.dto.OrderRequestDto
import com.example.oylfrutossecos.data.remote.dto.StatusRequestDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class SalesRepositoryTest {

    private val api: OrderApiService = mockk()
    private lateinit var repository: SalesRepository

    private val sampleProduct = Product(
        id = 1L,
        name = "Nuez Mariposa",
        category = "Frutos Secos",
        price = 12000.0,
        stock = 25,
        unit = "kg",
        description = "Nuez blanca",
        ingredients = "Nuez",
        origin = "Chile",
        barcode = "780998810001"
    )

    private val sampleOrderDto = OrderDto(
        id = 100L,
        orderNumber = "ORD-2026-0001",
        timestamp = 1700000000000L,
        dateFormatted = "18/11/2026 15:00",
        customerName = "Rene Marambio",
        customerEmail = "rene@oyl.cl",
        customerAddress = "Av. Providencia 1240",
        deliveryZone = "Santiago Centro & Providencia",
        paymentMethod = "Tarjeta Débito/Crédito",
        items = listOf(
            OrderItemDto(
                id = 10L,
                saleId = 100L,
                productId = 1L,
                productName = "Nuez Mariposa",
                unitPrice = 12000.0,
                quantity = 2,
                subtotal = 24000.0
            )
        ),
        subtotal = 24000.0,
        deliveryFee = 2500.0,
        total = 26500.0,
        status = "PENDIENTE",
        latitude = -33.42628,
        longitude = -70.61254
    )

    @Before
    fun setUp() {
        repository = SalesRepository(api = api)
    }

    @Test
    fun getAllSales_exitoRemoto_retornaListaDeVentas() = runTest {
        coEvery { api.getOrders() } returns Response.success(listOf(sampleOrderDto))

        val result = repository.getAllSales()

        assertEquals(1, result.size)
        assertEquals("ORD-2026-0001", result[0].orderNumber)
        assertEquals("Pendiente", result[0].status)
        assertNull(repository.lastError)
    }

    @Test
    fun getAllSales_errorApi_retornaListaVaciaYGuardaError() = runTest {
        val errorBody = "{\"status\":500,\"message\":\"Error servidor\"}"
            .toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { api.getOrders() } returns Response.error(500, errorBody)

        val result = repository.getAllSales()

        assertTrue(result.isEmpty())
        assertNotNull(repository.lastError)
    }

    @Test
    fun getSalesByCustomer_filtraPorEmail_enviaEmailEnMinusculas() = runTest {
        coEvery { api.getOrders(customerEmail = "rene@oyl.cl") } returns Response.success(listOf(sampleOrderDto))

        val result = repository.getSalesByCustomer("  RENE@OYL.CL  ")

        assertEquals(1, result.size)
        assertEquals("rene@oyl.cl", result[0].customerEmail)
        coVerify(exactly = 1) { api.getOrders(customerEmail = "rene@oyl.cl") }
    }

    @Test
    fun getSalesByCustomer_emailVacio_retornaVacioSinLlamarApi() = runTest {
        val result = repository.getSalesByCustomer("")

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { api.getOrders(any()) }
    }

    @Test
    fun getSaleById_exito_retornaVenta() = runTest {
        coEvery { api.getOrderById(100L) } returns Response.success(sampleOrderDto)

        val result = repository.getSaleById(100L)

        assertNotNull(result)
        assertEquals(100L, result?.id)
    }

    @Test
    fun getSaleById_noEncontrado_retornaNull() = runTest {
        val errorBody = "{\"status\":404}".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { api.getOrderById(999L) } returns Response.error(404, errorBody)

        val result = repository.getSaleById(999L)

        assertNull(result)
    }

    @Test
    fun createSale_armaElOrderRequestDtoCorrecto_yVaciaCarrito() = runTest {
        val requestSlot = slot<OrderRequestDto>()
        coEvery { api.createOrder(capture(requestSlot)) } returns Response.success(sampleOrderDto)

        val cartItems = listOf(CartItem(product = sampleProduct, quantity = 2))

        val result = repository.createSale(
            cartItems = cartItems,
            customerName = "Rene Marambio",
            customerEmail = "rene@oyl.cl",
            customerAddress = "Av. Providencia 1240",
            deliveryZone = "Santiago Centro & Providencia",
            deliveryFee = 2500.0,
            paymentMethod = "Tarjeta Débito/Crédito",
            latitude = -33.42628,
            longitude = -70.61254
        )

        assertTrue(result.isSuccess)
        val createdSale = result.getOrNull()
        assertEquals("ORD-2026-0001", createdSale?.orderNumber)

        val capturedDto = requestSlot.captured
        assertEquals("Rene Marambio", capturedDto.customerName)
        assertEquals("rene@oyl.cl", capturedDto.customerEmail)
        assertEquals(1, capturedDto.items.size)
        assertEquals(1L, capturedDto.items[0].productId)
        assertEquals(2, capturedDto.items[0].quantity)
    }

    @Test
    fun createSale_carritoVacio_retornaFailure() = runTest {
        val result = repository.createSale(
            cartItems = emptyList(),
            customerName = "Rene",
            customerEmail = "rene@oyl.cl",
            customerAddress = "Direccion",
            deliveryZone = "Zona",
            deliveryFee = 0.0,
            paymentMethod = "Efectivo"
        )

        assertTrue(result.isFailure)
        assertEquals("El carrito de compras está vacío", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { api.createOrder(any()) }
    }

    @Test
    fun createSale_error409StockInsuficiente_retornaFailureConMensaje() = runTest {
        val errorBody = "{\"status\":409,\"message\":\"Stock insuficiente para el producto Nuez Mariposa\"}"
            .toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { api.createOrder(any()) } returns Response.error(409, errorBody)

        val cartItems = listOf(CartItem(product = sampleProduct, quantity = 50))

        val result = repository.createSale(
            cartItems = cartItems,
            customerName = "Rene",
            customerEmail = "rene@oyl.cl",
            customerAddress = "Direccion",
            deliveryZone = "Zona",
            deliveryFee = 0.0,
            paymentMethod = "Efectivo"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Stock insuficiente") == true)
    }

    @Test
    fun updateStatus_mapeaEstadoLegibleAEnumBackend() = runTest {
        val statusSlot = slot<StatusRequestDto>()
        val updatedDto = sampleOrderDto.copy(status = "EN_CAMINO")
        coEvery { api.updateOrderStatus(100L, capture(statusSlot)) } returns Response.success(updatedDto)

        val result = repository.updateStatus(100L, "En camino")

        assertTrue(result.isSuccess)
        assertEquals("EN_CAMINO", statusSlot.captured.status)
        assertEquals("En camino", result.getOrNull()?.status)
    }

    @Test
    fun updateStatus_error404_retornaFailure() = runTest {
        val errorBody = "{\"status\":404,\"message\":\"Pedido no encontrado\"}"
            .toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { api.updateOrderStatus(999L, any()) } returns Response.error(404, errorBody)

        val result = repository.updateStatus(999L, "Entregado")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Pedido no encontrado") == true)
    }

    @Test
    fun deleteSale_exito_retornaTrue() = runTest {
        coEvery { api.deleteOrder(100L) } returns Response.success(Unit)

        val result = repository.deleteSale(100L)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
        coVerify(exactly = 1) { api.deleteOrder(100L) }
    }

    @Test
    fun deleteSale_error500_retornaFailure() = runTest {
        val errorBody = "{\"status\":500,\"message\":\"Error al eliminar\"}"
            .toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { api.deleteOrder(100L) } returns Response.error(500, errorBody)

        val result = repository.deleteSale(100L)

        assertTrue(result.isFailure)
    }
}
