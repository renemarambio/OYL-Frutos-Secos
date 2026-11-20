package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CartRepositoryTest {

    private lateinit var repository: CartRepository

    private val sampleProduct = Product(
        id = 1L,
        name = "Nuez Mariposa",
        category = "Frutos Secos",
        price = 10000.0,
        stock = 10,
        unit = "kg",
        description = "Nuez blanca",
        ingredients = "Nuez",
        origin = "Chile",
        barcode = "780998810001"
    )

    @Before
    fun setUp() {
        repository = CartRepository.getInstance()
        repository.clearCart()
    }

    @Test
    fun addToCart_productoNuevo_agregaACarrito() {
        repository.addToCart(sampleProduct, 2)

        val items = repository.cartItems.value
        assertEquals(1, items.size)
        assertEquals(2, items.first().quantity)
        assertEquals(20000.0, repository.getSubtotal(), 0.001)
        assertEquals(2, repository.getTotalItemsCount())
        assertEquals(3800.0, repository.getTaxAmount(), 0.001)
    }

    @Test
    fun addToCart_productoExistente_sumaCantidadSinSuperarStock() {
        repository.addToCart(sampleProduct, 6)
        repository.addToCart(sampleProduct, 8) // stock es 10

        val items = repository.cartItems.value
        assertEquals(1, items.size)
        assertEquals(10, items.first().quantity)
    }

    @Test
    fun updateQuantity_menorOIgualACero_eliminaItem() {
        repository.addToCart(sampleProduct, 2)
        assertEquals(1, repository.cartItems.value.size)

        repository.updateQuantity(1L, 0)
        assertTrue(repository.cartItems.value.isEmpty())
    }

    @Test
    fun updateQuantity_valida_actualizaCantidadSinSuperarStock() {
        repository.addToCart(sampleProduct, 2)
        repository.updateQuantity(1L, 20) // stock es 10

        assertEquals(10, repository.cartItems.value.first().quantity)
    }

    @Test
    fun removeFromCart_eliminaItem() {
        repository.addToCart(sampleProduct, 2)
        repository.removeFromCart(1L)

        assertTrue(repository.cartItems.value.isEmpty())
    }

    @Test
    fun clearCart_vaciaTodosLosItems() {
        repository.addToCart(sampleProduct, 2)
        repository.clearCart()

        assertTrue(repository.cartItems.value.isEmpty())
        assertEquals(0.0, repository.getSubtotal(), 0.001)
    }
}
