package cl.oyl.pedidos.service;

import cl.oyl.pedidos.client.ProductClient;
import cl.oyl.pedidos.dto.OrderItemRequest;
import cl.oyl.pedidos.dto.OrderRequest;
import cl.oyl.pedidos.dto.ProductInfo;
import cl.oyl.pedidos.exception.BusinessException;
import cl.oyl.pedidos.exception.ResourceNotFoundException;
import cl.oyl.pedidos.model.Order;
import cl.oyl.pedidos.model.OrderItem;
import cl.oyl.pedidos.model.OrderStatus;
import cl.oyl.pedidos.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Lógica de pedidos: valida stock, calcula totales y descuenta inventario en ms-productos. */
@Service
public class OrderService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Santiago"));

    private final OrderRepository repository;
    private final ProductClient productClient;
    private final Clock clock;

    public OrderService(OrderRepository repository, ProductClient productClient, Clock clock) {
        this.repository = repository;
        this.productClient = productClient;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Order> findAll(String customerEmail) {
        if (customerEmail != null && !customerEmail.isBlank()) {
            return repository.findByCustomerEmailIgnoreCaseOrderByTimestampDesc(customerEmail.trim());
        }
        return repository.findAllByOrderByTimestampDesc();
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido " + id + " no encontrado"));
    }

    @Transactional
    public Order create(OrderRequest request) {
        Order order = new Order();
        Instant now = clock.instant();
        order.setTimestamp(now.toEpochMilli());
        order.setDateFormatted(DATE_FORMAT.format(now));
        order.setOrderNumber(newOrderNumber(now));
        order.setCustomerName(request.customerName().trim());
        order.setCustomerEmail(request.customerEmail().trim().toLowerCase());
        order.setCustomerAddress(request.customerAddress().trim());
        order.setDeliveryZone(request.deliveryZone().trim());
        order.setPaymentMethod(request.paymentMethod().trim());
        order.setDeliveryFee(request.deliveryFee());
        order.setLatitude(request.latitude());
        order.setLongitude(request.longitude());

        // 1) Validar todo el stock antes de descontar nada.
        for (OrderItemRequest itemRequest : request.items()) {
            ProductInfo product = productClient.getProduct(itemRequest.productId());
            if (product.stock() == null || product.stock() < itemRequest.quantity()) {
                throw new BusinessException("Stock insuficiente para " + product.name());
            }
            order.addItem(new OrderItem(product.id(), product.name(), product.price(), itemRequest.quantity()));
        }

        // 2) Descontar stock en ms-productos.
        for (OrderItem item : order.getItems()) {
            productClient.decreaseStock(item.getProductId(), item.getQuantity());
        }

        double subtotal = order.getItems().stream().mapToDouble(OrderItem::getSubtotal).sum();
        order.setSubtotal(subtotal);
        order.setTotal(subtotal + request.deliveryFee());
        return repository.save(order);
    }

    @Transactional
    public Order updateStatus(Long id, OrderStatus status) {
        Order order = findById(id);
        if (order.getStatus() == OrderStatus.ENTREGADO || order.getStatus() == OrderStatus.CANCELADO) {
            throw new BusinessException("El pedido " + order.getOrderNumber() + " ya está " + order.getStatus());
        }
        order.setStatus(status);
        return repository.save(order);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Pedido " + id + " no encontrado");
        }
        repository.deleteById(id);
    }

    private String newOrderNumber(Instant now) {
        int year = now.atZone(ZoneId.of("America/Santiago")).getYear();
        String number;
        do {
            number = "ORD-" + year + "-" + ThreadLocalRandom.current().nextInt(1000, 10000);
        } while (repository.existsByOrderNumber(number));
        return number;
    }
}
