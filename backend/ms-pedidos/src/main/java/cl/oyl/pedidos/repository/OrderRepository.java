package cl.oyl.pedidos.repository;

import cl.oyl.pedidos.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOrderByTimestampDesc();

    List<Order> findByCustomerEmailIgnoreCaseOrderByTimestampDesc(String customerEmail);

    boolean existsByOrderNumber(String orderNumber);
}
