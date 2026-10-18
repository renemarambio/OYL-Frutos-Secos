package com.example.oylfrutossecos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oylfrutossecos.data.local.MockDataProvider
import com.example.oylfrutossecos.data.model.CartItem
import com.example.oylfrutossecos.data.model.DeliveryZone
import com.example.oylfrutossecos.data.model.Product
import com.example.oylfrutossecos.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CartViewModel : ViewModel() {

    private val cartRepository = CartRepository.getInstance()

    val cartItems: StateFlow<List<CartItem>> = cartRepository.cartItems

    private val _deliveryZones = MutableStateFlow(MockDataProvider.deliveryZones)
    val deliveryZones: StateFlow<List<DeliveryZone>> = _deliveryZones.asStateFlow()

    private val _selectedDeliveryZone = MutableStateFlow(MockDataProvider.deliveryZones.first())
    val selectedDeliveryZone: StateFlow<DeliveryZone> = _selectedDeliveryZone.asStateFlow()

    val subtotal: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val deliveryFee: StateFlow<Double> = selectedDeliveryZone.map { zone ->
        zone.fee
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MockDataProvider.deliveryZones.first().fee)

    val total: StateFlow<Double> = combine(subtotal, deliveryFee) { sub, fee ->
        if (sub > 0) sub + fee else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalItemsCount: StateFlow<Int> = cartItems.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addToCart(product: Product, quantity: Int = 1) {
        cartRepository.addToCart(product, quantity)
    }

    fun updateQuantity(productId: Long, newQuantity: Int) {
        cartRepository.updateQuantity(productId, newQuantity)
    }

    fun removeFromCart(productId: Long) {
        cartRepository.removeFromCart(productId)
    }

    fun clearCart() {
        cartRepository.clearCart()
    }

    fun selectDeliveryZone(zone: DeliveryZone) {
        _selectedDeliveryZone.value = zone
    }

    fun formatPrice(amount: Double): String {
        return "$${String.format("%,d", amount.toLong()).replace(',', '.')}"
    }
}
