package cl.oyl.pedidos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Producto y cantidad que el cliente quiere comprar. El precio lo obtiene el backend (no se confía en la app). */
public record OrderItemRequest(
        @NotNull(message = "El producto es obligatorio") Long productId,
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a 0") Integer quantity
) {}
