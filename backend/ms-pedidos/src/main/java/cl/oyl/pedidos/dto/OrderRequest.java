package cl.oyl.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/** Datos del pedido que envía la app al confirmar la compra. */
public record OrderRequest(
        @NotBlank(message = "El nombre del cliente es obligatorio") String customerName,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido") String customerEmail,
        @NotBlank(message = "La dirección es obligatoria") String customerAddress,
        @NotBlank(message = "La zona de despacho es obligatoria") String deliveryZone,
        @NotNull(message = "El costo de envío es obligatorio")
        @PositiveOrZero(message = "El costo de envío no puede ser negativo") Double deliveryFee,
        @NotBlank(message = "El método de pago es obligatorio") String paymentMethod,
        Double latitude,
        Double longitude,
        @NotEmpty(message = "El pedido debe tener al menos un producto")
        List<@Valid OrderItemRequest> items
) {}
