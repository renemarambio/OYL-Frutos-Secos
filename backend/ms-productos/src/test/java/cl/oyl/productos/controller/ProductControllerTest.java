package cl.oyl.productos.controller;

import cl.oyl.productos.exception.ResourceNotFoundException;
import cl.oyl.productos.model.Product;
import cl.oyl.productos.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Pruebas de la capa web: verifica rutas, códigos HTTP y validaciones. */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProductService service;

    private Product sample() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Nueces Mariposa");
        p.setCategory("Frutos Secos");
        p.setPrice(9900.0);
        p.setStock(10);
        return p;
    }

    @Test
    void list_devuelve200ConProductos() throws Exception {
        when(service.findAll(null, null)).thenReturn(List.of(sample()));
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Nueces Mariposa"));
    }

    @Test
    void get_inexistente_devuelve404() throws Exception {
        when(service.findById(99L)).thenThrow(new ResourceNotFoundException("Producto 99 no encontrado"));
        mvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Producto 99 no encontrado"));
    }

    @Test
    void create_valido_devuelve201() throws Exception {
        when(service.create(any())).thenReturn(sample());
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nueces\",\"category\":\"Frutos Secos\",\"price\":9900,\"stock\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_precioNegativo_devuelve400ConDetalle() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nueces\",\"category\":\"Frutos Secos\",\"price\":-1,\"stock\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void delete_devuelve204() throws Exception {
        mvc.perform(delete("/api/products/1")).andExpect(status().isNoContent());
    }
}
