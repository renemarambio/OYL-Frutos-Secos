package cl.oyl.productos.service;

import cl.oyl.productos.dto.CategorySummary;
import cl.oyl.productos.dto.ProductRequest;
import cl.oyl.productos.exception.BusinessException;
import cl.oyl.productos.exception.ResourceNotFoundException;
import cl.oyl.productos.model.Product;
import cl.oyl.productos.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Lógica de negocio del catálogo e inventario. */
@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Product> findAll(String category, String query) {
        if (query != null && !query.isBlank()) {
            return repository.findByNameContainingIgnoreCaseOrderByNameAsc(query.trim());
        }
        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("Todos")) {
            return repository.findByCategoryIgnoreCaseOrderByNameAsc(category.trim());
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto " + id + " no encontrado"));
    }

    @Transactional(readOnly = true)
    public Product findByBarcode(String barcode) {
        return repository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un producto con código " + barcode));
    }

    @Transactional(readOnly = true)
    public List<CategorySummary> categories() {
        Map<String, Long> counts = new TreeMap<>();
        repository.findAll().forEach(p -> counts.merge(p.getCategory(), 1L, Long::sum));
        return counts.entrySet().stream()
                .map(e -> new CategorySummary(e.getKey(), e.getValue()))
                .toList();
    }

    @Transactional
    public Product create(ProductRequest request) {
        String barcode = blankToNull(request.barcode());
        if (barcode != null && repository.existsByBarcode(barcode)) {
            throw new BusinessException("Ya existe un producto con el código " + barcode);
        }
        Product product = new Product();
        apply(product, request);
        return repository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product product = findById(id);
        String barcode = blankToNull(request.barcode());
        if (barcode != null && repository.existsByBarcodeAndIdNot(barcode, id)) {
            throw new BusinessException("Ya existe otro producto con el código " + barcode);
        }
        apply(product, request);
        return repository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Producto " + id + " no encontrado");
        }
        repository.deleteById(id);
    }

    @Transactional
    public Product updateStock(Long id, int stock) {
        Product product = findById(id);
        product.setStock(stock);
        return repository.save(product);
    }

    /** Descuenta stock al vender. Lanza 409 si no alcanza. */
    @Transactional
    public Product decreaseStock(Long id, int quantity) {
        Product product = findById(id);
        if (product.getStock() < quantity) {
            throw new BusinessException("Stock insuficiente para " + product.getName()
                    + ": quedan " + product.getStock() + " " + product.getUnit());
        }
        product.setStock(product.getStock() - quantity);
        return repository.save(product);
    }

    private void apply(Product p, ProductRequest r) {
        p.setName(r.name().trim());
        p.setCategory(r.category().trim());
        p.setPrice(r.price());
        p.setStock(r.stock());
        p.setUnit(r.unit() == null || r.unit().isBlank() ? "kg" : r.unit().trim());
        p.setDescription(nullToEmpty(r.description()));
        p.setIngredients(nullToEmpty(r.ingredients()));
        p.setOrigin(r.origin() == null || r.origin().isBlank() ? "Chile" : r.origin().trim());
        p.setBarcode(blankToNull(r.barcode()));
        p.setImageUrl(nullToEmpty(r.imageUrl()));
        p.setIsFavorite(Boolean.TRUE.equals(r.isFavorite()));
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s.trim(); }

    private static String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
