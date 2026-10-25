package com.example.oylfrutossecos.data.model

data class Sale(
    val id: Long = 0,
    val orderNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val dateFormatted: String,
    val customerName: String,
    val customerEmail: String,
    val customerAddress: String,
    val deliveryZone: String,
    val paymentMethod: String,
    val items: List<SaleItem> = emptyList(),
    val subtotal: Double,
    val deliveryFee: Double,
    val total: Double,
    val status: String = "Completado",
    val latitude: Double = -33.42628,
    val longitude: Double = -70.61254
) {
    val formattedTotal: String
        get() = "$${String.format("%,d", total.toLong()).replace(',', '.')}"

    val formattedSubtotal: String
        get() = "$${String.format("%,d", subtotal.toLong()).replace(',', '.')}"

    val formattedDeliveryFee: String
        get() = "$${String.format("%,d", deliveryFee.toLong()).replace(',', '.')}"
}

data class SaleItem(
    val id: Long = 0,
    val saleId: Long = 0,
    val productId: Long,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int,
    val subtotal: Double
) {
    val formattedSubtotal: String
        get() = "$${String.format("%,d", subtotal.toLong()).replace(',', '.')}"
}
