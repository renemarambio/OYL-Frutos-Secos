package com.example.oylfrutossecos.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Representa el producto que devuelve ms-productos. */
data class ProductDto(
    val id: Long = 0,
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val unit: String = "kg",
    val description: String = "",
    val ingredients: String = "",
    val origin: String = "Chile",
    val barcode: String = "",
    val imageUrl: String = "",
    val isFavorite: Boolean = false
)

/** Datos que envía la app para crear o editar un producto en ms-productos. */
data class ProductRequestDto(
    val name: String,
    val category: String,
    val price: Double,
    val stock: Int,
    val unit: String? = "kg",
    val description: String? = "",
    val ingredients: String? = "",
    val origin: String? = "Chile",
    val barcode: String? = "",
    val imageUrl: String? = "",
    val isFavorite: Boolean? = false
)

/** Categoría devuelta por GET /api/products/categories. */
data class CategoryDto(
    @SerializedName("name", alternate = ["category"])
    val name: String = "",
    @SerializedName("productCount", alternate = ["count"])
    val productCount: Long = 0L
)

/** Pedido devuelto por ms-pedidos. */
data class OrderDto(
    val id: Long = 0,
    val orderNumber: String = "",
    val timestamp: Long = 0L,
    val dateFormatted: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val customerAddress: String = "",
    val deliveryZone: String = "",
    val paymentMethod: String = "",
    val items: List<OrderItemDto> = emptyList(),
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val total: Double = 0.0,
    val status: String = "PENDIENTE",
    val latitude: Double? = null,
    val longitude: Double? = null
)

/** Ítem dentro de un pedido devuelto por ms-pedidos. */
data class OrderItemDto(
    val id: Long = 0,
    val productId: Long = 0,
    val productName: String = "",
    val unitPrice: Double = 0.0,
    val quantity: Int = 0,
    val subtotal: Double = 0.0,
    val saleId: Long = 0
)

/** Solicitud para crear un pedido en ms-pedidos. */
data class OrderRequestDto(
    val customerName: String,
    val customerEmail: String,
    val customerAddress: String,
    val deliveryZone: String,
    val deliveryFee: Double,
    val paymentMethod: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val items: List<OrderItemRequestDto>
)

/** Producto y cantidad dentro de una solicitud de pedido. */
data class OrderItemRequestDto(
    val productId: Long,
    val quantity: Int
)

/** Solicitud para cambiar el estado de un pedido en ms-pedidos. */
data class StatusRequestDto(
    val status: String
)

/** Solicitud para cambiar el stock de un producto en ms-productos. */
data class StockRequestDto(
    val stock: Int
)

/** Respuesta de mindicador.cl para dolar o uf. */
data class IndicatorDto(
    val version: String? = null,
    val autor: String? = null,
    val codigo: String? = null,
    val nombre: String? = null,
    @SerializedName("unidad_medida")
    val unidadMedida: String? = null,
    val serie: List<IndicatorSerieDto>? = null
) {
    val valorActual: Double?
        get() = serie?.firstOrNull()?.valor

    val fechaActual: String?
        get() = serie?.firstOrNull()?.fecha
}

/** Punto de serie histórica de mindicador.cl. */
data class IndicatorSerieDto(
    val fecha: String = "",
    val valor: Double = 0.0
)

/** Cuerpo de error estándar retornado por los microservicios Spring Boot. */
data class ApiErrorResponse(
    val timestamp: String? = null,
    val status: Int? = null,
    val message: String? = null,
    val errors: Map<String, String>? = null
)
