package cl.oyl.pedidos.service;

import cl.oyl.pedidos.client.ProductClient;
import cl.oyl.pedidos.dto.OrderItemRequest;
import cl.oyl.pedidos.dto.OrderRequest;
import cl.oyl.pedidos.dto.ProductInfo;
import cl.oyl.pedidos.exception.BusinessException;
import cl.oyl.pedidos.exception.ResourceNotFoundException;
import cl.oyl.pedidos.model.Order;
import cl.oyl.pedidos.model.OrderStatus;
import cl.oyl.pedidos.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del servicio de pedidos.
 * Se mockean el repositorio (base de datos) y el ProductClient (llamadas HTTP a ms-productos).
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository repository;
    @Mock private ProductClient productClient;

    private OrderService service;

    @BeforeEach
    void setUp() {
        Clock fixed = Clock.fixed(Instant.parse("2026-11-20T15:30:00Z"), ZoneId.of("UTC"));
        service = new OrderService(repository, productClient, fixed);
    }

    private OrderRequest request(int quantity) {
        return new OrderRequest("Ana Pérez", "ANA@mail.cl ", "Av. Siempre Viva 123", "Providencia",
                2500.0, "Tarjeta", -33.4, -70.6, List.of(new OrderItemRequest(1L, quantity)));
    }

    @Test
    void create_calculaTotalesYDescuentaStock() {
        when(productClient.getProduct(1L)).thenReturn(new ProductInfo(1L, "Nueces", 9900.0, 10, "kg"));
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order order = service.create(request(2));

        assertThat(order.getSubtotal()).isEqualTo(19800.0);
        assertThat(order.getTotal()).isEqualTo(22300.0);
        assertThat(order.getCustomerEmail()).isEqualTo("ana@mail.cl");
        assertThat(order.getDateFormatted()).isEqualTo("20/11/2026 12:30");
        assertThat(order.getOrderNumber()).startsWith("ORD-2026-");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDIENTE);
        assertThat(order.getItems()).singleElement()
                .satisfies(i -> assertThat(i.getProductName()).isEqualTo("Nueces"));
        verify(productClient).decreaseStock(1L, 2);
    }

    @Test
    void create_stockInsuficiente_noDescuentaNiGuarda() {
        when(productClient.getProduct(1L)).thenReturn(new ProductInfo(1L, "Nueces", 9900.0, 1, "kg"));

        assertThatThrownBy(() -> service.create(request(5)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Stock insuficiente");
        verify(productClient, never()).decreaseStock(anyLong(), anyInt());
        verify(repository, never()).save(any());
    }

    @Test
    void findAll_conCorreo_filtraPorCliente() {
        when(repository.findByCustomerEmailIgnoreCaseOrderByTimestampDesc("ana@mail.cl")).thenReturn(List.of());
        assertThat(service.findAll(" ana@mail.cl ")).isEmpty();
        verify(repository, never()).findAllByOrderByTimestampDesc();
    }

    @Test
    void findAll_sinCorreo_devuelveTodas() {
        when(repository.findAllByOrderByTimestampDesc()).thenReturn(List.of(new Order()));
        assertThat(service.findAll(null)).hasSize(1);
    }

    @Test
    void findById_inexistente_lanza404() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(9L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_pedidoPendiente_cambiaEstado() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDIENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(order));
        when(repository.save(order)).thenReturn(order);
        assertThat(service.updateStatus(1L, OrderStatus.EN_CAMINO).getStatus()).isEqualTo(OrderStatus.EN_CAMINO);
    }

    @Test
    void updateStatus_pedidoEntregado_lanza409() {
        Order order = new Order();
        order.setStatus(OrderStatus.ENTREGADO);
        when(repository.findById(1L)).thenReturn(Optional.of(order));
        assertThatThrownBy(() -> service.updateStatus(1L, OrderStatus.CANCELADO))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void delete_inexistente_lanza404() {
        when(repository.existsById(3L)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(3L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existente_elimina() {
        when(repository.existsById(3L)).thenReturn(true);
        service.delete(3L);
        verify(repository).deleteById(3L);
    }
}
