package cl.oyl.pedidos.dto;

import cl.oyl.pedidos.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull(message = "El estado es obligatorio") OrderStatus status) {}
