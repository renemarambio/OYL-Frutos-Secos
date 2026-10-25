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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CartViewModel : ViewModel() {

    companion object {
        // Coordenadas fijas de Casa Matriz OYL (Providencia #1820, Santiago)
        const val STORE_LAT = -33.42628
        const val STORE_LNG = -70.61254
        const val STORE_NAME = "Casa Matriz OYL Frutos Secos (Providencia #1820)"
    }

    private val cartRepository = CartRepository.getInstance()

    val cartItems: StateFlow<List<CartItem>> = cartRepository.cartItems

    private val _deliveryZones = MutableStateFlow(MockDataProvider.deliveryZones)
    val deliveryZones: StateFlow<List<DeliveryZone>> = _deliveryZones.asStateFlow()

    private val _selectedDeliveryZone = MutableStateFlow(MockDataProvider.deliveryZones.first())
    val selectedDeliveryZone: StateFlow<DeliveryZone> = _selectedDeliveryZone.asStateFlow()

    // Ubicación GPS elegida por el cliente en el mapa OpenStreetMap
    private val _selectedLat = MutableStateFlow(STORE_LAT)
    val selectedLat: StateFlow<Double> = _selectedLat.asStateFlow()

    private val _selectedLng = MutableStateFlow(STORE_LNG)
    val selectedLng: StateFlow<Double> = _selectedLng.asStateFlow()

    private val _selectedAddressDescription = MutableStateFlow("Providencia #1820 (Punto Matriz)")
    val selectedAddressDescription: StateFlow<String> = _selectedAddressDescription.asStateFlow()

    private val _distanceKm = MutableStateFlow(0.0)
    val distanceKm: StateFlow<Double> = _distanceKm.asStateFlow()

    // Subtotal
    val subtotal: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Costo de despacho dinámico según mapa o zona
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

    /**
     * Calcula la distancia geodésica a la tienda y fija el costo de despacho dinámicamente
     * cuando el cliente toca el mapa OpenStreetMap.
     */
    fun setDeliveryLocationFromMap(lat: Double, lng: Double, description: String? = null) {
        _selectedLat.value = lat
        _selectedLng.value = lng

        val dist = calculateDistanceKm(STORE_LAT, STORE_LNG, lat, lng)
        _distanceKm.value = dist

        val calculatedFee = calculateFeeByDistance(dist)
        val zoneName = when {
            dist <= 3.0 -> "Radio Corto (< 3 km)"
            dist <= 8.0 -> "Santiago Urbano (3 - 8 km)"
            dist <= 15.0 -> "Santiago Extendido (8 - 15 km)"
            else -> "Región Metropolitana (> 15 km)"
        }

        val formattedKm = String.format("%.1f", dist)
        val desc = description ?: "Punto de entrega en mapa ($formattedKm km de tienda)"
        _selectedAddressDescription.value = desc

        _selectedDeliveryZone.value = DeliveryZone(
            id = "zone-osm-custom",
            name = "$zoneName ($formattedKm km)",
            description = "Despacho a $formattedKm km de Providencia #1820",
            fee = calculatedFee,
            estimatedTime = if (dist <= 8.0) "2 a 4 horas" else "Mismo día (4 a 6 horas)"
        )
    }

    private fun calculateFeeByDistance(distanceKm: Double): Double {
        return when {
            distanceKm <= 1.0 -> 1200.0
            distanceKm <= 4.0 -> 2200.0
            distanceKm <= 8.0 -> 3200.0
            distanceKm <= 15.0 -> 4200.0
            else -> (4500.0 + (distanceKm - 15.0) * 200.0).coerceAtMost(8500.0)
        }
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radio de la Tierra en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun formatPrice(amount: Double): String {
        return "$${String.format("%,d", amount.toLong()).replace(',', '.')}"
    }
}
