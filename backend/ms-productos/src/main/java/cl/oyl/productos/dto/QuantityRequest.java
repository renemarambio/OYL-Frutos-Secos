package cl.oyl.productos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Cantidad a descontar del stock (la usa ms-pedidos al registrar una venta). */
public record QuantityRequest(
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a 0")
        Integer quantity
) {}
