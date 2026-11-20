package com.example.oylfrutossecos.data.remote.mapper

import com.example.oylfrutossecos.data.model.Product
import com.example.oylfrutossecos.data.model.Sale
import com.example.oylfrutossecos.data.model.SaleItem
import com.example.oylfrutossecos.data.remote.dto.CategoryDto
import com.example.oylfrutossecos.data.remote.dto.OrderDto
import com.example.oylfrutossecos.data.remote.dto.OrderItemDto
import com.example.oylfrutossecos.data.remote.dto.ProductDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {

    @Test
    fun mapBackendStatusToReadable_mapeaCorrectamenteTodosLosCasos() {
        assertEquals("Pendiente", mapBackendStatusToReadable("PENDIENTE"))
        assertEquals("Preparando", mapBackendStatusToReadable("PREPARANDO"))
        assertEquals("En camino", mapBackendStatusToReadable("EN_CAMINO"))
        assertEquals("Entregado", mapBackendStatusToReadable("ENTREGADO"))
        assertEquals("Cancelado", mapBackendStatusToReadable("CANCELADO"))
        assertEquals("Entregado", mapBackendStatusToReadable("COMPLETADO"))
        assertEquals("Pendiente", mapBackendStatusToReadable(null))
        assertEquals("OTRO_ESTADO", mapBackendStatusToReadable("OTRO_ESTADO"))
    }

    @Test
    fun mapReadableStatusToBackend_mapeaCorrectamenteTodosLosCasos() {
        assertEquals("PENDIENTE", mapReadableStatusToBackend("pendiente"))
        assertEquals("PREPARANDO", mapReadableStatusToBackend("preparando"))
        assertEquals("EN_CAMINO", mapReadableStatusToBackend("en camino"))
        assertEquals("ENTREGADO", mapReadableStatusToBackend("entregado"))
        assertEquals("ENTREGADO", mapReadableStatusToBackend("completado"))
        assertEquals("CANCELADO", mapReadableStatusToBackend("cancelado"))
        assertEquals("PENDIENTE", mapReadableStatusToBackend(null))
        assertEquals("EN_ESPERA", mapReadableStatusToBackend("en espera"))
    }

    @Test
    fun productDto_toDomain_convierteCampos() {
        val dto = ProductDto(
            id = 5L,
            name = "Nuez",
            category = "Frutos Secos",
            price = 9990.0,
            stock = 10,
            unit = "",
            description = "Desc",
            ingredients = "Ingr",
            origin = "",
            barcode = "780123",
            imageUrl = "url",
            isFavorite = true
        )

        val domain = dto.toDomain()

        assertEquals(5L, domain.id)
        assertEquals("Nuez", domain.name)
        assertEquals("kg", domain.unit)
        assertEquals("Chile", domain.origin)
        assertTrue(domain.isFavorite)
    }

    @Test
    fun product_toDto_y_toRequestDto_convierteCampos() {
        val prod = Product(
            id = 5L,
            name = "Nuez",
            category = "Frutos Secos",
            price = 9990.0,
            stock = 10,
            unit = "kg",
            description = "Desc",
            ingredients = "Ingr",
            origin = "Chile",
            barcode = "780123",
            imageUrl = "url",
            isFavorite = false
        )

        val dto = prod.toDto()
        assertEquals(5L, dto.id)
        assertEquals("Nuez", dto.name)

        val reqDto = prod.toRequestDto()
        assertEquals("Nuez", reqDto.name)
        assertEquals(9990.0, reqDto.price, 0.001)
    }

    @Test
    fun categoryDto_toDomain_convierteCampos() {
        val dto = CategoryDto(
            name = "Frutos Secos",
            productCount = 12
        )

        val cat = dto.toDomain()
        assertEquals("frutos-secos", cat.id)
        assertEquals("Frutos Secos", cat.name)
        assertEquals(12, cat.productCount)
    }

    @Test
    fun orderItemDto_toDomain_y_viceversa() {
        val dto = OrderItemDto(
            id = 1L,
            saleId = 10L,
            productId = 2L,
            productName = "Almendra",
            unitPrice = 5000.0,
            quantity = 2,
            subtotal = 10000.0
        )

        val item = dto.toDomain()
        assertEquals(1L, item.id)
        assertEquals(10L, item.saleId)
        assertEquals("Almendra", item.productName)

        val backDto = item.toDto()
        assertEquals(10000.0, backDto.subtotal, 0.001)

        val reqDto = item.toOrderItemRequestDto()
        assertEquals(2L, reqDto.productId)
        assertEquals(2, reqDto.quantity)
    }

    @Test
    fun orderDto_toDomain_y_sale_toOrderRequestDto() {
        val orderDto = OrderDto(
            id = 10L,
            orderNumber = "ORD-001",
            timestamp = 1000L,
            dateFormatted = "18/11/2026",
            customerName = "Rene",
            customerEmail = "rene@oyl.cl",
            customerAddress = "Providencia 1240",
            deliveryZone = "Santiago",
            paymentMethod = "Tarjeta",
            items = listOf(
                OrderItemDto(
                    id = 1L,
                    productId = 2L,
                    productName = "Almendra",
                    unitPrice = 5000.0,
                    quantity = 2,
                    subtotal = 10000.0,
                    saleId = 10L
                )
            ),
            subtotal = 10000.0,
            deliveryFee = 2000.0,
            total = 12000.0,
            status = "PENDIENTE",
            latitude = -33.42,
            longitude = -70.61
        )

        val sale = orderDto.toDomain()
        assertEquals(10L, sale.id)
        assertEquals("Pendiente", sale.status)
        assertEquals(1, sale.items.size)

        val req = sale.toOrderRequestDto()
        assertEquals("Rene", req.customerName)
        assertEquals(1, req.items.size)
        assertEquals(2L, req.items.first().productId)
    }
}
