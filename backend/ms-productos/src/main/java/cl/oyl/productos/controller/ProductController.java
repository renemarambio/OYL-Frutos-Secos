package cl.oyl.productos.controller;

import cl.oyl.productos.dto.CategorySummary;
import cl.oyl.productos.dto.ProductRequest;
import cl.oyl.productos.dto.QuantityRequest;
import cl.oyl.productos.dto.StockRequest;
import cl.oyl.productos.model.Product;
import cl.oyl.productos.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints REST del catálogo e inventario. */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    /** GET /api/products?category=Semillas  o  /api/products?q=nuez */
    @GetMapping
    public List<Product> list(@RequestParam(required = false) String category,
                              @RequestParam(required = false) String q) {
        return service.findAll(category, q);
    }

    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/barcode/{barcode}")
    public Product getByBarcode(@PathVariable String barcode) {
        return service.findByBarcode(barcode);
    }

    @GetMapping("/categories")
    public List<CategorySummary> categories() {
        return service.categories();
    }

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    public Product updateStock(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        return service.updateStock(id, request.stock());
    }

    /** Lo llama ms-pedidos al confirmar una compra. */
    @PostMapping("/{id}/decrease-stock")
    public Product decreaseStock(@PathVariable Long id, @Valid @RequestBody QuantityRequest request) {
        return service.decreaseStock(id, request.quantity());
    }
}
