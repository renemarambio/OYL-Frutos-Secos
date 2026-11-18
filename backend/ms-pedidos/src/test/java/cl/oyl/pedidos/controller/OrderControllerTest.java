package cl.oyl.pedidos.controller;

import cl.oyl.pedidos.exception.BusinessException;
import cl.oyl.pedidos.exception.ServiceUnavailableException;
import cl.oyl.pedidos.model.Order;
import cl.oyl.pedidos.service.OrderService;
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

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired private MockMvc mvc;
    @MockitoBean private OrderService service;

    private static final String VALID_BODY = """
            {"customerName":"Ana","customerEmail":"ana@mail.cl","customerAddress":"Calle 1",
             "deliveryZone":"Providencia","deliveryFee":2500,"paymentMethod":"Tarjeta",
             "items":[{"productId":1,"quantity":2}]}""";

    @Test
    void list_porCliente_devuelve200() throws Exception {
        Order order = new Order();
        order.setOrderNumber("ORD-2026-1234");
        when(service.findAll("ana@mail.cl")).thenReturn(List.of(order));
        mvc.perform(get("/api/orders").param("customerEmail", "ana@mail.cl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-2026-1234"));
    }

    @Test
    void create_valido_devuelve201() throws Exception {
        Order order = new Order();
        order.setId(7L);
        when(service.create(any())).thenReturn(order);
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void create_sinProductos_devuelve400() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("[{\"productId\":1,\"quantity\":2}]", "[]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.items").exists());
    }

    @Test
    void create_stockInsuficiente_devuelve409() throws Exception {
        when(service.create(any())).thenThrow(new BusinessException("Stock insuficiente para Nueces"));
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void create_productosCaido_devuelve503() throws Exception {
        when(service.create(any())).thenThrow(new ServiceUnavailableException("El servicio de productos no está disponible"));
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void updateStatus_valido_devuelve200() throws Exception {
        when(service.updateStatus(any(), any())).thenReturn(new Order());
        mvc.perform(patch("/api/orders/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EN_CAMINO\"}"))
                .andExpect(status().isOk());
    }
}
