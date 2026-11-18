package cl.oyl.productos.repository;

import cl.oyl.productos.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Acceso a datos con Spring Data JPA: las consultas se generan por el nombre del método. */
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryIgnoreCaseOrderByNameAsc(String category);

    List<Product> findByNameContainingIgnoreCaseOrderByNameAsc(String query);

    Optional<Product> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);
}
