package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.model.CartItem
import com.example.oylfrutossecos.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CartRepository private constructor() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    fun addToCart(product: Product, quantity: Int = 1) {
        _cartItems.update { currentList ->
            val existingIndex = currentList.indexOfFirst { it.product.id == product.id }
            if (existingIndex >= 0) {
                currentList.mapIndexed { index, item ->
                    if (index == existingIndex) {
                        val newQty = (item.quantity + quantity).coerceAtMost(product.stock)
                        item.copy(quantity = newQty)
                    } else item
                }
            } else {
                currentList + CartItem(product = product, quantity = quantity.coerceAtMost(product.stock))
            }
        }
    }

    fun updateQuantity(productId: Long, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
            return
        }
        _cartItems.update { currentList ->
            currentList.map { item ->
                if (item.product.id == productId) {
                    val validQty = newQuantity.coerceAtMost(item.product.stock)
                    item.copy(quantity = validQty)
                } else item
            }
        }
    }

    fun removeFromCart(productId: Long) {
        _cartItems.update { currentList ->
            currentList.filterNot { it.product.id == productId }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun getSubtotal(): Double {
        return _cartItems.value.sumOf { it.subtotal }
    }

    fun getTaxAmount(): Double {
        // IVA 19% en Chile ya incluido o calculado para desglose informativo
        return getSubtotal() * 0.19
    }

    fun getTotalItemsCount(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }

    companion object {
        @Volatile
        private var INSTANCE: CartRepository? = null

        fun getInstance(): CartRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = CartRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}
