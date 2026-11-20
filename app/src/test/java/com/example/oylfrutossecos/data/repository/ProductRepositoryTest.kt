package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.local.ProductLocalDataSource
import com.example.oylfrutossecos.data.model.Product
import com.example.oylfrutossecos.data.remote.api.ProductApiService
import com.example.oylfrutossecos.data.remote.dto.CategoryDto
import com.example.oylfrutossecos.data.remote.dto.ProductDto
import com.example.oylfrutossecos.data.remote.dto.StockRequestDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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

class ProductRepositoryTest {

    private val api: ProductApiService = mockk()
    private val local: ProductLocalDataSource = mockk(relaxed = true)
    private lateinit var repository: ProductRepository

    private val sampleProductDto = ProductDto(
        id = 1L,
        name = "Nuez Mariposa",
        category = "Frutos Secos",
        price = 12000.0,
        stock = 25,
        unit = "kg",
        description = "Nuez partida blanca",
        ingredients = "100% Nuez",
        origin = "Chile",
        barcode = "780998810001",
        imageUrl = "",
        isFavorite = false
    )

    private val sampleProduct = Product(
        id = 1L,
        name = "Nuez Mariposa",
        category = "Frutos Secos",
        price = 12000.0,
        stock = 25,
        unit = "kg",
        description = "Nuez partida blanca",
        ingredients = "100% Nuez",
        origin = "Chile",
        barcode = "780998810001"
    )

    @Before
    fun setUp() {
        repository = ProductRepository(api = api, local = local)
    }

    @Test
    fun getAllProducts_exitoRemoto_guardaEnCacheYDevuelveLista() = runTest {
        coEvery { api.getProducts() } returns Response.success(listOf(sampleProductDto))

        val result = repository.getAllProducts()

        assertEquals(1, result.size)
        assertEquals("Nuez Mariposa", result[0].name)
        assertFalse(repository.isOffline)
        assertNull(repository.lastError)
        coVerify(exactly = 1) { local.replaceCache(any()) }
    }

    @Test
    fun getAllProducts_falloDeRed_retornaCacheLocalYAvisaOffline() = runTest {
        coEvery { api.getProducts() } throws RuntimeException("Error de conexión con ms-productos")
        coEvery { local.getAllProducts() } returns listOf(sampleProduct)

        val result = repository.getAllProducts()

        assertEquals(1, result.size)
        assertEquals("Nuez Mariposa", result[0].name)
        assertTrue(repository.isOffline)
        assertNotNull(repository.lastError)
        coVerify(exactly = 1) { local.getAllProducts() }
    }

    @Test
    fun getAllProducts_error500_retornaCacheLocal() = runTest {
        val errorBody = "{\"status\":500,\"message\":\"Error interno\"}"
            .toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { api.getProducts() } returns Response.error(500, errorBody)
        coEvery { local.getAllProducts() } returns listOf(sampleProduct)

        val result = repository.getAllProducts()

        assertEquals(1, result.size)
        assertTrue(repository.isOffline)
    }

    @Test
    fun getProductById_exitoRemoto_guardaEnCache() = runTest {
        coEvery { api.getProductById(1L) } returns Response.success(sampleProductDto)

        val result = repository.getProductById(1L)

        assertNotNull(result)
        assertEquals(1L, result?.id)
        assertFalse(repository.isOffline)
        coVerify(exactly = 1) { local.saveProduct(any()) }
    }

    @Test
    fun getProductById_404_retornaNull() = runTest {
        val errorBody = "{\"status\":404,\"message\":\"Producto no encontrado\"}"
            .toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { api.getProductById(99L) } returns Response.error(404, errorBody)

        val result = repository.getProductById(99L)

        assertNull(result)
        assertFalse(repository.isOffline)
    }

    @Test
    fun getProductById_errorRed_retornaDeCache() = runTest {
        coEvery { api.getProductById(1L) } throws RuntimeException("Timeout")
        coEvery { local.getProductById(1L) } returns sampleProduct

        val result = repository.getProductById(1L)

        assertNotNull(result)
        assertEquals(1L, result?.id)
        assertTrue(repository.isOffline)
    }

    @Test
    fun getProductByBarcode_exitoRemoto_guardaEnCache() = runTest {
        coEvery { api.getProductByBarcode("780998810001") } returns Response.success(sampleProductDto)

        val result = repository.getProductByBarcode("780998810001")

        assertNotNull(result)
        assertEquals("780998810001", result?.barcode)
        coVerify(exactly = 1) { local.saveProduct(any()) }
    }

    @Test
    fun getProductByBarcode_404_retornaNull() = runTest {
        val errorBody = "{\"status\":404}".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { api.getProductByBarcode("9999") } returns Response.error(404, errorBody)

        val result = repository.getProductByBarcode("9999")

        assertNull(result)
    }

    @Test
    fun getProductByBarcode_errorRed_retornaDeCache() = runTest {
        coEvery { api.getProductByBarcode("780998810001") } throws RuntimeException("Sin red")
        coEvery { local.getProductByBarcode("780998810001") } returns sampleProduct

        val result = repository.getProductByBarcode("780998810001")

        assertNotNull(result)
        assertTrue(repository.isOffline)
    }

    @Test
    fun getProductByCode_conBarcodeExistente() = runTest {
        coEvery { api.getProductByBarcode("780998810001") } returns Response.success(sampleProductDto)

        val result = repository.getProductByCode("780998810001")

        assertNotNull(result)
        assertEquals(1L, result?.id)
    }

    @Test
    fun getProductByCode_conIdNumerico() = runTest {
        val errorBody = "{\"status\":404}".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { api.getProductByBarcode("1") } returns Response.error(404, errorBody)
        coEvery { api.getProductById(1L) } returns Response.success(sampleProductDto)

        val result = repository.getProductByCode("1")

        assertNotNull(result)
        assertEquals(1L, result?.id)
    }

    @Test
    fun searchProducts_exitoRemoto() = runTest {
        coEvery { api.getProducts(query = "nuez") } returns Response.success(listOf(sampleProductDto))

        val result = repository.searchProducts("nuez")

        assertEquals(1, result.size)
        assertEquals("Nuez Mariposa", result[0].name)
        assertFalse(repository.isOffline)
    }

    @Test
    fun searchProducts_queryVacio_retornaTodos() = runTest {
        coEvery { api.getProducts() } returns Response.success(listOf(sampleProductDto))

        val result = repository.searchProducts("")

        assertEquals(1, result.size)
    }

    @Test
    fun searchProducts_falloRed_buscaEnLocal() = runTest {
        coEvery { api.getProducts(query = "almendra") } throws RuntimeException("Fallo red")
        coEvery { local.searchProducts("almendra") } returns listOf(sampleProduct)

        val result = repository.searchProducts("almendra")

        assertEquals(1, result.size)
        assertTrue(repository.isOffline)
    }

    @Test
    fun getProductsByCategory_todos_retornaTodos() = runTest {
        coEvery { api.getProducts() } returns Response.success(listOf(sampleProductDto))

        val result = repository.getProductsByCategory("Todos")

        assertEquals(1, result.size)
    }

    @Test
    fun getProductsByCategory_categoriaEspecifica_exitoRemoto() = runTest {
        coEvery { api.getProducts(category = "Frutos Secos") } returns Response.success(listOf(sampleProductDto))

        val result = repository.getProductsByCategory("Frutos Secos")

        assertEquals(1, result.size)
        assertFalse(repository.isOffline)
    }

    @Test
    fun getProductsByCategory_falloRed_buscaEnLocal() = runTest {
        coEvery { api.getProducts(category = "Semillas") } throws RuntimeException("Error")
        coEvery { local.getProductsByCategory("Semillas") } returns listOf(sampleProduct)

        val result = repository.getProductsByCategory("Semillas")

        assertEquals(1, result.size)
        assertTrue(repository.isOffline)
    }

    @Test
    fun getCategories_exitoRemoto() = runTest {
        val categoryDtos = listOf(
            CategoryDto(name = "Frutos Secos", productCount = 5)
        )
        coEvery { api.getCategories() } returns Response.success(categoryDtos)

        val result = repository.getCategories()

        assertTrue(result.isNotEmpty())
        val fs = result.find { it.name == "Frutos Secos" }
        assertEquals(5, fs?.productCount)
        assertFalse(repository.isOffline)
    }

    @Test
    fun getCategories_falloRed_cuentaEnLocal() = runTest {
        coEvery { api.getCategories() } throws RuntimeException("Sin red")
        coEvery { local.getAllProducts() } returns listOf(sampleProduct)

        val result = repository.getCategories()

        assertTrue(result.isNotEmpty())
        assertTrue(repository.isOffline)
    }

    @Test
    fun saveProduct_create_exito() = runTest {
        val newProduct = sampleProduct.copy(id = 0L)
        coEvery { api.createProduct(any()) } returns Response.success(sampleProductDto)

        val result = repository.saveProduct(newProduct)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        coVerify(exactly = 1) { local.saveProduct(any()) }
    }

    @Test
    fun saveProduct_update_exito() = runTest {
        coEvery { api.updateProduct(1L, any()) } returns Response.success(sampleProductDto)

        val result = repository.saveProduct(sampleProduct)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        coVerify(exactly = 1) { local.saveProduct(any()) }
    }

    @Test
    fun saveProduct_error409_retornaFailure() = runTest {
        val errorBody = "{\"status\":409,\"message\":\"Código de barras ya registrado\"}"
            .toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { api.createProduct(any()) } returns Response.error(409, errorBody)

        val result = repository.saveProduct(sampleProduct.copy(id = 0L))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Código de barras ya registrado") == true)
    }

    @Test
    fun saveProduct_offline_retornaFailure() = runTest {
        coEvery { api.getProducts() } throws RuntimeException("Sin conexión")
        repository.getAllProducts() // fuerzo estado offline
        assertTrue(repository.isOffline)

        val result = repository.saveProduct(sampleProduct)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Sin conexión con el servidor") == true)
    }

    @Test
    fun deleteProduct_exito() = runTest {
        coEvery { api.deleteProduct(1L) } returns Response.success(Unit)

        val result = repository.deleteProduct(1L)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
        coVerify(exactly = 1) { local.deleteProduct(1L) }
    }

    @Test
    fun deleteProduct_error404_retornaFailure() = runTest {
        val errorBody = "{\"status\":404,\"message\":\"Producto no existe\"}"
            .toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { api.deleteProduct(99L) } returns Response.error(404, errorBody)

        val result = repository.deleteProduct(99L)

        assertTrue(result.isFailure)
    }

    @Test
    fun deleteProduct_offline_retornaFailure() = runTest {
        coEvery { api.getProducts() } throws RuntimeException("Sin conexión")
        repository.getAllProducts()
        assertTrue(repository.isOffline)

        val result = repository.deleteProduct(1L)

        assertTrue(result.isFailure)
    }

    @Test
    fun updateStock_exito() = runTest {
        coEvery { api.updateStock(1L, match { it.stock == 30 }) } returns Response.success(sampleProductDto.copy(stock = 30))

        val result = repository.updateStock(1L, 30)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
        coVerify(exactly = 1) { local.saveProduct(any()) }
    }

    @Test
    fun updateStock_error409_retornaFailure() = runTest {
        val errorBody = "{\"status\":409,\"message\":\"Stock no puede ser negativo\"}"
            .toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { api.updateStock(1L, any()) } returns Response.error(409, errorBody)

        val result = repository.updateStock(1L, -5)

        assertTrue(result.isFailure)
    }

    @Test
    fun updateStock_offline_retornaFailure() = runTest {
        coEvery { api.getProducts() } throws RuntimeException("Sin conexión")
        repository.getAllProducts()
        assertTrue(repository.isOffline)

        val result = repository.updateStock(1L, 10)

        assertTrue(result.isFailure)
    }

    @Test
    fun updatePrice_exito() = runTest {
        coEvery { api.getProductById(1L) } returns Response.success(sampleProductDto)
        coEvery { api.updateProduct(1L, any()) } returns Response.success(sampleProductDto.copy(price = 15000.0))

        val result = repository.updatePrice(1L, 15000.0)

        assertTrue(result.isSuccess)
    }

    @Test
    fun updatePrice_productoNoEncontrado_retornaFailure() = runTest {
        val errorBody = "{\"status\":404}".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { api.getProductById(99L) } returns Response.error(404, errorBody)

        val result = repository.updatePrice(99L, 15000.0)

        assertTrue(result.isFailure)
        assertEquals("Producto no encontrado", result.exceptionOrNull()?.message)
    }

    @Test
    fun resetDefaults_llamaALocal() = runTest {
        repository.resetDefaults()
        coVerify(exactly = 1) { local.resetDefaults() }
    }
}
