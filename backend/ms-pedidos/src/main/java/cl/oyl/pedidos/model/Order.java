package cl.oyl.pedidos.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/** Entidad JPA: un pedido (tabla "orders") con sus líneas (relación 1:N). */
@Entity(name = "PurchaseOrder")
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Column(nullable = false)
    private Long timestamp;

    @Column(nullable = false, length = 30)
    private String dateFormatted;

    @Column(nullable = false, length = 120)
    private String customerName;

    @Column(nullable = false, length = 120)
    private String customerEmail;

    @Column(nullable = false, length = 250)
    private String customerAddress;

    @Column(nullable = false, length = 80)
    private String deliveryZone;

    @Column(nullable = false, length = 40)
    private String paymentMethod;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Double subtotal;

    @Column(nullable = false)
    private Double deliveryFee;

    @Column(nullable = false)
    private Double total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDIENTE;

    private Double latitude;
    private Double longitude;

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    public String getDateFormatted() { return dateFormatted; }
    public void setDateFormatted(String dateFormatted) { this.dateFormatted = dateFormatted; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    public String getDeliveryZone() { return deliveryZone; }
    public void setDeliveryZone(String deliveryZone) { this.deliveryZone = deliveryZone; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
