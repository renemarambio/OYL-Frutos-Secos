package com.example.oylfrutossecos.ui.viewmodel

import android.app.Application
import com.example.oylfrutossecos.data.model.CartItem
import com.example.oylfrutossecos.data.model.Product
import com.example.oylfrutossecos.data.model.Sale
import com.example.oylfrutossecos.data.model.SaleItem
import com.example.oylfrutossecos.data.repository.SalesRepository
import com.example.oylfrutossecos.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SalesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val application: Application = mockk(relaxed = true)
    private val repository: SalesRepository = mockk(relaxed = true)

    private val sampleSale = Sale(
        id = 1L,
        orderNumber = "ORD-001",
        timestamp = 1700000000000L,
        dateFormatted = "18/11/2026",
        customerName = "Rene Marambio",
        customerEmail = "rene@oyl.cl",
        customerAddress = "Av. Providencia 1240",
        deliveryZone = "Santiago Centro",
        paymentMethod = "Tarjeta",
        items = listOf(
            SaleItem(id = 1, saleId = 1, productId = 10, productName = "Nuez", unitPrice = 10000.0, quantity = 1, subtotal = 10000.0)
        ),
        subtotal = 10000.0,
        deliveryFee = 2500.0,
        total = 12500.0,
        status = "Pendiente"
    )

    private val sampleProduct = Product(
        id = 10L,
        name = "Nuez",
        category = "Frutos Secos",
        price = 10000.0,
        stock = 20,
        unit = "kg",
        description = "Nuez blanca",
        ingredients = "Nuez",
        origin = "Chile",
        barcode = "780998810010"
    )

    @Before
    fun setUp() {
        every { repository.lastError } returns null
        coEvery { repository.getAllSales() } returns listOf(sampleSale)
        coEvery { repository.getSalesByCustomer(any()) } returns listOf(sampleSale)
    }

    @Test
    fun loadSales_exito_cargaLista() = runTest {
        val viewModel = SalesViewModel(application, repository)
        advanceUntilIdle()

        assertEquals(1, viewModel.sales.value.size)
        assertEquals("ORD-001", viewModel.sales.value.first().orderNumber)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun loadSales_error_actualizaErrorMessage() = runTest {
        every { repository.lastError } returns "Error al conectar"
        coEvery { repository.getAllSales() } returns emptyList()

        val viewModel = SalesViewModel(application, repository)
        advanceUntilIdle()

        assertTrue(viewModel.sales.value.isEmpty())
        assertEquals("Error al conectar", viewModel.errorMessage.value)
    }

    @Test
    fun loadCustomerOrders_exito_cargaListaCliente() = runTest {
        val viewModel = SalesViewModel(application, repository)
        advanceUntilIdle()

        viewModel.loadCustomerOrders("rene@oyl.cl")
        advanceUntilIdle()

        assertEquals(1, viewModel.customerOrders.value.size)
        assertEquals("rene@oyl.cl", viewModel.customerOrders.value.first().customerEmail)
    }

    @Test
    fun selectSale_actualizaSelectedSale() = runTest {
        val viewModel = SalesViewModel(application, repository)
        advanceUntilIdle()

        viewModel.selectSale(sampleSale)
        assertEquals(sampleSale, viewModel.selectedSale.value)

        viewModel.selectSale(null)
        assertNull(viewModel.selectedSale.value)
    }

    @Test
    fun onCheckoutFieldChanged_actualizaCamposYLimpiaErrores() = runTest {
        val viewModel = SalesViewModel(application, repository)
        advanceUntilIdle()

        viewModel.onCheckoutFieldChanged(
            name = "Rene Marambio",
            email = "rene@oyl.cl",
            phone = "+56 9 1234 5678",
            address = "Av. Providencia 1240, Providencia"
        )

        val form = viewModel.checkoutForm.value
        assertEquals("Rene Marambio", form.customerName)
        assertEquals("rene@oyl.cl", form.customerEmail)
        assertEquals("+56 9 1234 5678", form.customerPhone)
        assertNull(form.nameError)
        assertNull(form.emailError)
    }

    @Test
    fun confirmOrder_camposInvalidos_marcaErroresSinLlamarRepo() = runTest {
        val viewModel = SalesViewModel(application, repository)
        advanceUntilIdle()

        viewModel.onCheckoutFieldChanged(name = "", email = "invalido", phone = "123", address = "")
        var exito = false
        var error: String? = null
        viewModel.confirmOrder(
            cartItems = listOf(CartItem(product = sampleProduct, quantity = 1)),
            deliveryFee = 2500.0,
            onSuccess = { exito = true },
            onError = { error = it }
        )

        assertFalse(exito)
        assertNotNull(viewModel.checkoutForm.value.nameError)
        assertNotNull(viewModel.checkoutForm.value.emailError)
        assertNotNull(viewModel.checkoutForm.value.phoneError)
        assertNotNull(viewModel.checkoutForm.value.addressError)
        coVerify(exactly = 0) { repository.createSale(any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun confirmOrder_exito_vaciaFormularioYNotificaExito() = runTest {
        coEvery {
            repository.createSale(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns Result.success(sampleSale)

        val viewModel = SalesViewModel(application, repository)
        advanceUntilIdle()

        viewModel.onCheckoutFieldChanged(
            name = "Rene Marambio",
            email = "rene@oyl.cl",
            phone = "+56 9 1234 5678",
            address = "Av. Providencia 1240, Providencia"
        )

        var createdSale: Sale? = null
        viewModel.confirmOrder(
            cartItems = listOf(CartItem(product = sampleProduct, quantity = 1)),
            deliveryFee = 2500.0,
            onSuccess = { createdSale = it },
            onError = {}
        )
        advanceUntilIdle()

        assertNotNull(createdSale)
        assertEquals("ORD-001", createdSale?.orderNumber)
        assertFalse(viewModel.checkoutForm.value.isProcessing)
    }

    @Test
    fun confirmOrder_errorStockInsuficiente_muestraError() = runTest {
        coEvery {
            repository.createSale(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException("Stock insuficiente"))

        val viewModel = SalesViewModel(application, repository)
        advanceUntilIdle()

        viewModel.onCheckoutFieldChanged(
            name = "Rene Marambio",
            email = "rene@oyl.cl",
            phone = "+56 9 1234 5678",
            address = "Av. Providencia 1240, Providencia"
        )

        var errorMsg: String? = null
        viewModel.confirmOrder(
            cartItems = listOf(CartItem(product = sampleProduct, quantity = 1)),
            deliveryFee = 2500.0,
            onSuccess = {},
            onError = { errorMsg = it }
        )
        advanceUntilIdle()

        assertEquals("Stock insuficiente", errorMsg)
        assertEquals("Stock insuficiente", viewModel.errorMessage.value)
        assertFalse(viewModel.checkoutForm.value.isProcessing)
    }

    @Test
    fun updateOrderStatus_exito_actualizaVentaEnListas() = runTest {
        val updated = sampleSale.copy(status = "En camino")
        coEvery { repository.updateStatus(1L, "En camino") } returns Result.success(updated)

        val viewModel = SalesViewModel(application, repository)
        advanceUntilIdle()

        viewModel.selectSale(sampleSale)

        var ok = false
        viewModel.updateOrderStatus(1L, "En camino") { success, _ -> ok = success }
        advanceUntilIdle()

        assertTrue(ok)
        assertEquals("En camino", viewModel.sales.value.first().status)
        assertEquals("En camino", viewModel.selectedSale.value?.status)
    }

    @Test
    fun updateOrderStatus_fallo_ejecutaCallbackConError() = runTest {
        coEvery { repository.updateStatus(1L, any()) } returns Result.failure(RuntimeException("Error"))

        val viewModel = SalesViewModel(application, repository)
        advanceUntilIdle()

        var error: String? = null
        viewModel.updateOrderStatus(1L, "Cancelado") { success, err -> error = err }
        advanceUntilIdle()

        assertEquals("Error", error)
    }

    @Test
    fun deleteOrder_exito_eliminaVentaDeListas() = runTest {
        coEvery { repository.deleteSale(1L) } returns Result.success(true)

        val viewModel = SalesViewModel(application, repository)
        advanceUntilIdle()

        viewModel.selectSale(sampleSale)

        var ok = false
        viewModel.deleteOrder(1L) { success, _ -> ok = success }
        advanceUntilIdle()

        assertTrue(ok)
        assertTrue(viewModel.sales.value.isEmpty())
        assertNull(viewModel.selectedSale.value)
    }

    @Test
    fun deleteOrder_fallo_ejecutaCallbackConError() = runTest {
        coEvery { repository.deleteSale(1L) } returns Result.failure(RuntimeException("No se pudo eliminar"))

        val viewModel = SalesViewModel(application, repository)
        advanceUntilIdle()

        var error: String? = null
        viewModel.deleteOrder(1L) { _, err -> error = err }
        advanceUntilIdle()

        assertEquals("No se pudo eliminar", error)
    }

    @Test
    fun clearErrorMessage_limpiaError() = runTest {
        val viewModel = SalesViewModel(application, repository)
        advanceUntilIdle()

        viewModel.clearErrorMessage()
        assertNull(viewModel.errorMessage.value)
    }
}
