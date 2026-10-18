package com.example.oylfrutossecos.data.model

data class DeliveryZone(
    val id: String,
    val name: String,
    val fee: Double,
    val estimatedTime: String,
    val description: String,
    val status: String = "Disponible"
) {
    val formattedFee: String
        get() = if (fee == 0.0) "GRATIS" else "$${String.format("%,d", fee.toLong()).replace(',', '.')}"
}
