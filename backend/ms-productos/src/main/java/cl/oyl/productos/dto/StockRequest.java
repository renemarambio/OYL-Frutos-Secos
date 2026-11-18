package cl.oyl.productos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Nuevo stock absoluto de un producto (lo usa el administrador en Inventario). */
public record StockRequest(
        @NotNull(message = "El stock es obligatorio")
        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock
) {}
