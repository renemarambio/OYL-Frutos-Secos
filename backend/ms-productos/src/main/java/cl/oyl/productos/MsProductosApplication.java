package cl.oyl.productos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de productos de OYL Frutos Secos.
 * Expone el catálogo y el inventario (CRUD + stock) en /api/products.
 */
@SpringBootApplication
public class MsProductosApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsProductosApplication.class, args);
    }
}
