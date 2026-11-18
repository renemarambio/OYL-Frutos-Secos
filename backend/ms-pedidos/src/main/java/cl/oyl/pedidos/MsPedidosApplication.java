package cl.oyl.pedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de pedidos de OYL Frutos Secos.
 * Registra las compras de los clientes y consulta/descuenta stock en ms-productos.
 */
@SpringBootApplication
public class MsPedidosApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsPedidosApplication.class, args);
    }
}
