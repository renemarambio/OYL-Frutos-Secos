package cl.oyl.pedidos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Parte de la respuesta de ms-productos que necesita este servicio. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductInfo(Long id, String name, Double price, Integer stock, String unit) {}
