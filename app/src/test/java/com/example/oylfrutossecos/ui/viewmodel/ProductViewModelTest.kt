package com.example.oylfrutossecos.ui.viewmodel

import android.app.Application
import com.example.oylfrutossecos.data.model.Category
import com.example.oylfrutossecos.data.model.Product
import com.example.oylfrutossecos.data.repository.ProductRepository
import com.example.oylfrutossecos.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
class ProductViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val application: Application = mockk(relaxed = true)
    private val repository: ProductRepository = mockk(relaxed = true)

    private val sampleProduct = Product(
        id = 1L,
        name = "Almendra Non Pareil",
        category = "Frutos Secos",
        price = 14500.0,
        stock = 30,
        unit = "kg",
        description = "Almendra tostada",
        ingredients = "Almendra",
        origin = "Chile",
        barcode = "780998810002"
    )

    private val sampleCategories = listOf(
        Category(id = "frutos-secos", name = "Frutos Secos", description = "", iconName = "", productCount = 5)
    )

    @Before
    fun setUp() {
        every { repository.isOffline } returns false
        every { repository.lastError } returns null
        coEvery { repository.getAllProducts() } returns listOf(sampleProduct)
        coEvery { repository.getCategories() } returns sampleCategories
    }

    @Test
    fun loadData_exito_cargaProductosYCategorias() = runTest {
        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        assertEquals(1, viewModel.products.value.size)
        assertEquals(1, viewModel.categories.value.size)
        assertFalse(viewModel.isLoading.value)
        assertFalse(viewModel.isOffline.value)
    }

    @Test
    fun loadData_offline_actualizaEstadoIsOffline() = runTest {
        every { repository.isOffline } returns true

        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        assertTrue(viewModel.isOffline.value)
        assertNotNull(viewModel.offlineMessage.value)
    }

    @Test
    fun onSearchQueryChanged_filtraProductosCorrectamente() = runTest {
        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Almendra")
        advanceUntilIdle()

        assertEquals("Almendra", viewModel.searchQuery.value)
        assertEquals(1, viewModel.products.value.size)

        viewModel.onSearchQueryChanged("Inexistente")
        advanceUntilIdle()
        assertEquals(0, viewModel.products.value.size)
    }

    @Test
    fun onCategorySelected_filtraPorCategoria() = runTest {
        coEvery { repository.getProductsByCategory("Semillas") } returns emptyList()

        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        viewModel.onCategorySelected("Semillas")
        advanceUntilIdle()

        assertEquals("Semillas", viewModel.selectedCategory.value)
        assertEquals(0, viewModel.products.value.size)
    }

    @Test
    fun loadProductById_encontrado_actualizaFormulario() = runTest {
        coEvery { repository.getProductById(1L) } returns sampleProduct

        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        viewModel.loadProductById(1L)
        advanceUntilIdle()

        assertEquals(sampleProduct, viewModel.selectedProduct.value)
        assertEquals("Almendra Non Pareil", viewModel.formState.value.name)
        assertEquals("14500", viewModel.formState.value.price)
    }

    @Test
    fun loadProductById_noEncontrado_reseteaFormulario() = runTest {
        coEvery { repository.getProductById(99L) } returns null

        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        viewModel.loadProductById(99L)
        advanceUntilIdle()

        assertNull(viewModel.selectedProduct.value)
        assertEquals("", viewModel.formState.value.name)
    }

    @Test
    fun onFormFieldChanged_actualizaCamposYLimpiaErrores() = runTest {
        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        viewModel.onFormFieldChanged(name = "Castaña de Cajú", price = "18000", stock = "15")

        assertEquals("Castaña de Cajú", viewModel.formState.value.name)
        assertEquals("18000", viewModel.formState.value.price)
        assertEquals("15", viewModel.formState.value.stock)
        assertNull(viewModel.formState.value.nameError)
    }

    @Test
    fun saveProduct_camposInvalidos_marcaErroresSinLlamarRepositorio() = runTest {
        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        viewModel.onFormFieldChanged(name = "", price = "-10", stock = "abc")
        var successCalled = false
        viewModel.saveProduct(onSuccess = { successCalled = true })

        assertFalse(successCalled)
        assertNotNull(viewModel.formState.value.nameError)
        assertNotNull(viewModel.formState.value.priceError)
        assertNotNull(viewModel.formState.value.stockError)
        coVerify(exactly = 0) { repository.saveProduct(any()) }
    }

    @Test
    fun saveProduct_modoOffline_muestraErrorSinLlamarRepositorio() = runTest {
        every { repository.isOffline } returns true
        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        viewModel.onFormFieldChanged(name = "Nuez", price = "10000", stock = "10")
        var errorMsg: String? = null
        viewModel.saveProduct(onSuccess = {}, onError = { errorMsg = it })

        assertNotNull(errorMsg)
        assertTrue(errorMsg?.contains("Sin conexión") == true)
        coVerify(exactly = 0) { repository.saveProduct(any()) }
    }

    @Test
    fun saveProduct_valido_llamaRepositorioYEjecutaCallbackExito() = runTest {
        coEvery { repository.saveProduct(any()) } returns Result.success(10L)

        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        viewModel.onFormFieldChanged(
            name = "Pistachos Tostados",
            category = "Frutos Secos",
            price = "15990",
            stock = "20",
            unit = "kg",
            barcode = "780123456789"
        )

        var savedId: Long? = null
        viewModel.saveProduct(onSuccess = { savedId = it })
        advanceUntilIdle()

        assertEquals(10L, savedId)
        assertFalse(viewModel.formState.value.isSaving)
        coVerify(atLeast = 1) { repository.saveProduct(any()) }
    }

    @Test
    fun saveProduct_precioConPuntosDeMiles_convierteAEnteroCorrecto() = runTest {
        val capturedProduct = slot<Product>()
        coEvery { repository.saveProduct(capture(capturedProduct)) } returns Result.success(99L)

        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        viewModel.onFormFieldChanged(
            name = "Nueces Selección",
            category = "Frutos Secos",
            price = "12.000",
            stock = "50",
            unit = "kg",
            barcode = "780999999999"
        )

        var savedId: Long? = null
        viewModel.saveProduct(onSuccess = { savedId = it })
        advanceUntilIdle()

        assertEquals(99L, savedId)
        assertTrue(capturedProduct.isCaptured)
        assertEquals(12000.0, capturedProduct.captured.price, 0.001)
    }

    @Test
    fun saveProduct_falloEnServidor_muestraMensajeError() = runTest {
        coEvery { repository.saveProduct(any()) } returns Result.failure(RuntimeException("Error en base de datos"))

        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        viewModel.onFormFieldChanged(
            name = "Pistachos Tostados",
            price = "15990",
            stock = "20"
        )

        var receivedError: String? = null
        viewModel.saveProduct(onSuccess = {}, onError = { receivedError = it })
        advanceUntilIdle()

        assertEquals("Error en base de datos", receivedError)
        assertEquals("Error en base de datos", viewModel.errorMessage.value)
    }

    @Test
    fun deleteProduct_modoOffline_muestraError() = runTest {
        every { repository.isOffline } returns true
        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        var errorMsg: String? = null
        viewModel.deleteProduct(1L, onSuccess = {}, onError = { errorMsg = it })

        assertNotNull(errorMsg)
        assertTrue(errorMsg?.contains("Sin conexión") == true)
        coVerify(exactly = 0) { repository.deleteProduct(any()) }
    }

    @Test
    fun deleteProduct_exito_recargaDatosYEjecutaCallback() = runTest {
        coEvery { repository.deleteProduct(1L) } returns Result.success(true)

        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        var success = false
        viewModel.deleteProduct(1L, onSuccess = { success = true })
        advanceUntilIdle()

        assertTrue(success)
        coVerify(exactly = 1) { repository.deleteProduct(1L) }
    }

    @Test
    fun deleteProduct_fallo_muestraMensajeError() = runTest {
        coEvery { repository.deleteProduct(1L) } returns Result.failure(RuntimeException("No se puede eliminar"))

        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        var errorMsg: String? = null
        viewModel.deleteProduct(1L, onSuccess = {}, onError = { errorMsg = it })
        advanceUntilIdle()

        assertEquals("No se puede eliminar", errorMsg)
        assertEquals("No se puede eliminar", viewModel.errorMessage.value)
    }

    @Test
    fun adjustStock_exito_actualizaStock() = runTest {
        coEvery { repository.updateStock(1L, 35) } returns Result.success(true)

        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        var resultSuccess = false
        viewModel.adjustStock(1L, 5) { resultSuccess = it }
        advanceUntilIdle()

        assertTrue(resultSuccess)
        assertEquals(35, viewModel.products.value.first().stock)
    }

    @Test
    fun updateStockDirect_exito() = runTest {
        coEvery { repository.updateStock(1L, 50) } returns Result.success(true)

        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        var resultSuccess = false
        viewModel.updateStockDirect(1L, 50) { resultSuccess = it }
        advanceUntilIdle()

        assertTrue(resultSuccess)
        assertEquals(50, viewModel.products.value.first().stock)
    }

    @Test
    fun updatePriceDirect_exito() = runTest {
        coEvery { repository.updatePrice(1L, 16000.0) } returns Result.success(true)

        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        var resultSuccess = false
        viewModel.updatePriceDirect(1L, 16000.0) { resultSuccess = it }
        advanceUntilIdle()

        assertTrue(resultSuccess)
        assertEquals(16000.0, viewModel.products.value.first().price, 0.001)
    }

    @Test
    fun findProductByCode_retornaProducto() = runTest {
        coEvery { repository.getProductByCode("780998810002") } returns sampleProduct

        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        var found: Product? = null
        viewModel.findProductByCode("780998810002") { found = it }
        advanceUntilIdle()

        assertEquals(sampleProduct, found)
    }

    @Test
    fun resetToDefaults_ejecutaCallback() = runTest {
        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        var completed = false
        viewModel.resetToDefaults { completed = true }
        advanceUntilIdle()

        assertTrue(completed)
        coVerify(exactly = 1) { repository.resetDefaults() }
    }

    @Test
    fun clearErrorMessage_limpiaError() = runTest {
        val viewModel = ProductViewModel(application, repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()

        viewModel.clearErrorMessage()
        assertNull(viewModel.errorMessage.value)
    }
}
