package com.example.oylfrutossecos.data.model

data class Product(
    val id: Long = 0,
    val name: String,
    val category: String,
    val price: Double,
    val stock: Int,
    val unit: String = "kg",
    val description: String = "",
    val ingredients: String = "",
    val origin: String = "Chile",
    val barcode: String = "",
    val imageUrl: String = "",
    val isFavorite: Boolean = false
) {
    val formattedPrice: String
        get() = "$${String.format("%,d", price.toLong()).replace(',', '.')}"

    val isLowStock: Boolean
        get() = stock <= 10
}
