package com.example.oylfrutossecos.data.remote.mapper

import com.example.oylfrutossecos.data.model.Category
import com.example.oylfrutossecos.data.model.Product
import com.example.oylfrutossecos.data.model.Sale
import com.example.oylfrutossecos.data.model.SaleItem
import com.example.oylfrutossecos.data.remote.dto.CategoryDto
import com.example.oylfrutossecos.data.remote.dto.OrderDto
import com.example.oylfrutossecos.data.remote.dto.OrderItemDto
import com.example.oylfrutossecos.data.remote.dto.OrderItemRequestDto
import com.example.oylfrutossecos.data.remote.dto.OrderRequestDto
import com.example.oylfrutossecos.data.remote.dto.ProductDto
import com.example.oylfrutossecos.data.remote.dto.ProductRequestDto

// ---- Conversión de Estados ----

fun mapBackendStatusToReadable(status: String?): String {
    return when (status?.trim()?.uppercase()) {
        "PENDIENTE" -> "Pendiente"
        "PREPARANDO" -> "Preparando"
        "EN_CAMINO" -> "En camino"
        "ENTREGADO" -> "Entregado"
        "CANCELADO" -> "Cancelado"
        "COMPLETADO" -> "Entregado"
        null -> "Pendiente"
        else -> status
    }
}

fun mapReadableStatusToBackend(status: String?): String {
    return when (status?.trim()?.lowercase()) {
        "pendiente" -> "PENDIENTE"
        "preparando" -> "PREPARANDO"
        "en camino" -> "EN_CAMINO"
        "entregado", "completado" -> "ENTREGADO"
        "cancelado" -> "CANCELADO"
        null -> "PENDIENTE"
        else -> status.uppercase().replace(' ', '_')
    }
}

// ---- Mappers de Producto ----

fun ProductDto.toDomain(): Product {
    return Product(
        id = id,
        name = name,
        category = category,
        price = price,
        stock = stock,
        unit = unit.ifBlank { "kg" },
        description = description,
        ingredients = ingredients,
        origin = origin.ifBlank { "Chile" },
        barcode = barcode,
        imageUrl = imageUrl,
        isFavorite = isFavorite
    )
}

fun Product.toDto(): ProductDto {
    return ProductDto(
        id = id,
        name = name,
        category = category,
        price = price,
        stock = stock,
        unit = unit,
        description = description,
        ingredients = ingredients,
        origin = origin,
        barcode = barcode,
        imageUrl = imageUrl,
        isFavorite = isFavorite
    )
}

fun Product.toRequestDto(): ProductRequestDto {
    return ProductRequestDto(
        name = name,
        category = category,
        price = price,
        stock = stock,
        unit = unit,
        description = description,
        ingredients = ingredients,
        origin = origin,
        barcode = barcode,
        imageUrl = imageUrl,
        isFavorite = isFavorite
    )
}

// ---- Mappers de Categoría ----

fun CategoryDto.toDomain(): Category {
    return Category(
        id = name.lowercase().replace(" ", "-"),
        name = name,
        description = "",
        iconName = "",
        productCount = productCount.toInt()
    )
}

// ---- Mappers de Pedidos / Ventas ----

fun OrderItemDto.toDomain(): SaleItem {
    return SaleItem(
        id = id,
        saleId = saleId,
        productId = productId,
        productName = productName,
        unitPrice = unitPrice,
        quantity = quantity,
        subtotal = subtotal
    )
}

fun SaleItem.toDto(): OrderItemDto {
    return OrderItemDto(
        id = id,
        saleId = saleId,
        productId = productId,
        productName = productName,
        unitPrice = unitPrice,
        quantity = quantity,
        subtotal = subtotal
    )
}

fun SaleItem.toOrderItemRequestDto(): OrderItemRequestDto {
    return OrderItemRequestDto(
        productId = productId,
        quantity = quantity
    )
}

fun OrderDto.toDomain(): Sale {
    return Sale(
        id = id,
        orderNumber = orderNumber,
        timestamp = timestamp,
        dateFormatted = dateFormatted,
        customerName = customerName,
        customerEmail = customerEmail,
        customerAddress = customerAddress,
        deliveryZone = deliveryZone,
        paymentMethod = paymentMethod,
        items = items.map { it.toDomain() },
        subtotal = subtotal,
        deliveryFee = deliveryFee,
        total = total,
        status = mapBackendStatusToReadable(status),
        latitude = latitude ?: -33.42628,
        longitude = longitude ?: -70.61254
    )
}

fun Sale.toOrderRequestDto(): OrderRequestDto {
    return OrderRequestDto(
        customerName = customerName,
        customerEmail = customerEmail,
        customerAddress = customerAddress,
        deliveryZone = deliveryZone,
        deliveryFee = deliveryFee,
        paymentMethod = paymentMethod,
        latitude = latitude,
        longitude = longitude,
        items = items.map { it.toOrderItemRequestDto() }
    )
}
