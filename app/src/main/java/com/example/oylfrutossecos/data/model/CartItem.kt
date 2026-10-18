package com.example.oylfrutossecos.data.model

data class CartItem(
    val product: Product,
    val quantity: Int = 1
) {
    val subtotal: Double
        get() = product.price * quantity

    val formattedSubtotal: String
        get() = "$${String.format("%,d", subtotal.toLong()).replace(',', '.')}"
}
