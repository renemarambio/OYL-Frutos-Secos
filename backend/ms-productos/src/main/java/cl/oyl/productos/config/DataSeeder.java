package cl.oyl.productos.config;

import cl.oyl.productos.model.Product;
import cl.oyl.productos.repository.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/** Carga el catálogo inicial (seed-products.json) solo si la tabla está vacía. */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final ProductRepository repository;

    public DataSeeder(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() > 0) return;
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try (InputStream in = new ClassPathResource("seed-products.json").getInputStream()) {
            List<Product> products = mapper.readValue(in, new TypeReference<>() {});
            products.forEach(p -> p.setId(null));
            repository.saveAll(products);
            log.info("Catálogo inicial cargado: {} productos", products.size());
        }
    }
}
