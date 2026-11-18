package cl.oyl.pedidos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/** Línea de un pedido: producto, precio al momento de la compra y cantidad. */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 120)
    private String productName;

    @Column(nullable = false)
    private Double unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double subtotal;

    public OrderItem() {}

    public OrderItem(Long productId, String productName, Double unitPrice, Integer quantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = unitPrice * quantity;
    }

    /** Se expone como "saleId" para que calce con el modelo SaleItem de la app. */
    public Long getSaleId() { return order == null ? null : order.getId(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
}
