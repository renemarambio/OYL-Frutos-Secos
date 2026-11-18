package cl.oyl.productos.dto;

import jakarta.validation.constraints.*;

/** Datos que envía la app para crear o editar un producto (validados con Bean Validation). */
public record ProductRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String name,

        @NotBlank(message = "La categoría es obligatoria")
        String category,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor a 0")
        Double price,

        @NotNull(message = "El stock es obligatorio")
        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock,

        String unit,
        String description,
        String ingredients,
        String origin,

        @Pattern(regexp = "^$|^[0-9]{8,14}$", message = "El código de barras debe tener entre 8 y 14 dígitos")
        String barcode,

        String imageUrl,
        Boolean isFavorite
) {}
