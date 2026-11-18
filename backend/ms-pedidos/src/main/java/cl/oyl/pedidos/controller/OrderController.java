package cl.oyl.pedidos.controller;

import cl.oyl.pedidos.dto.OrderRequest;
import cl.oyl.pedidos.dto.StatusRequest;
import cl.oyl.pedidos.model.Order;
import cl.oyl.pedidos.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints REST de pedidos. */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    /** GET /api/orders (admin: todas las ventas)  o  /api/orders?customerEmail=x (cliente: "Mis pedidos") */
    @GetMapping
    public List<Order> list(@RequestParam(required = false) String customerEmail) {
        return service.findAll(customerEmail);
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Order> create(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PatchMapping("/{id}/status")
    public Order updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return service.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
