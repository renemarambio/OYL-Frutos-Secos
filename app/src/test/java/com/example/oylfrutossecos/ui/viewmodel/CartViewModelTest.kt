package com.example.oylfrutossecos.ui.viewmodel

import com.example.oylfrutossecos.data.model.DeliveryZone
import com.example.oylfrutossecos.data.model.Product
import com.example.oylfrutossecos.data.repository.CartRepository
import com.example.oylfrutossecos.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CartViewModel

    private val sampleProduct = Product(
        id = 1L,
        name = "Nuez Mariposa",
        category = "Frutos Secos",
        price = 10000.0,
        stock = 25,
        unit = "kg",
        description = "Nuez blanca",
        ingredients = "Nuez",
        origin = "Chile",
        barcode = "780998810001"
    )

    @Before
    fun setUp() {
        CartRepository.getInstance().clearCart()
        viewModel = CartViewModel()
    }

    @Test
    fun addToCart_agregaProductoAlCarrito() = runTest {
        backgroundScope.launch { viewModel.subtotal.collect {} }
        backgroundScope.launch { viewModel.totalItemsCount.collect {} }

        viewModel.addToCart(sampleProduct, 2)
        advanceUntilIdle()

        assertEquals(1, viewModel.cartItems.value.size)
        assertEquals(2, viewModel.cartItems.value.first().quantity)
        assertEquals(20000.0, viewModel.subtotal.value, 0.001)
        assertEquals(2, viewModel.totalItemsCount.value)
    }

    @Test
    fun updateQuantity_modificaCantidadDeItem() = runTest {
        backgroundScope.launch { viewModel.subtotal.collect {} }

        viewModel.addToCart(sampleProduct, 1)
        advanceUntilIdle()

        viewModel.updateQuantity(1L, 4)
        advanceUntilIdle()

        assertEquals(4, viewModel.cartItems.value.first().quantity)
        assertEquals(40000.0, viewModel.subtotal.value, 0.001)
    }

    @Test
    fun removeFromCart_eliminaItemDelCarrito() = runTest {
        viewModel.addToCart(sampleProduct, 1)
        advanceUntilIdle()
        assertEquals(1, viewModel.cartItems.value.size)

        viewModel.removeFromCart(1L)
        advanceUntilIdle()

        assertTrue(viewModel.cartItems.value.isEmpty())
    }

    @Test
    fun clearCart_vaciaCarrito() = runTest {
        viewModel.addToCart(sampleProduct, 3)
        advanceUntilIdle()

        viewModel.clearCart()
        advanceUntilIdle()

        assertTrue(viewModel.cartItems.value.isEmpty())
    }

    @Test
    fun selectDeliveryZone_cambiaZona() = runTest {
        backgroundScope.launch { viewModel.deliveryFee.collect {} }

        val customZone = DeliveryZone(
            id = "custom",
            name = "Zona Oriente",
            description = "Las Condes",
            fee = 3500.0,
            estimatedTime = "2 horas"
        )
        viewModel.selectDeliveryZone(customZone)
        advanceUntilIdle()

        assertEquals("Zona Oriente", viewModel.selectedDeliveryZone.value.name)
        assertEquals(3500.0, viewModel.deliveryFee.value, 0.001)
    }

    @Test
    fun setDeliveryLocationFromMap_calculaDistanciaYTarifa() = runTest {
        // Coordenadas cercanas a Casa Matriz
        viewModel.setDeliveryLocationFromMap(
            lat = -33.43000,
            lng = -70.62000,
            description = "Punto de prueba"
        )
        advanceUntilIdle()

        assertEquals("Punto de prueba", viewModel.selectedAddressDescription.value)
        assertTrue(viewModel.distanceKm.value > 0)
        assertNotNull(viewModel.selectedDeliveryZone.value)
    }

    @Test
    fun formatPrice_formateaMonedaChilena() {
        val formatted = viewModel.formatPrice(12500.0)
        assertEquals("$12.500", formatted)
    }
}
