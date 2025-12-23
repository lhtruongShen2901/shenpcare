package g6shenpcare.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "OrderItems")
public class OrderItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderItemId")
    private Long orderItemId;

    // --- QUAN HỆ VỚI ORDER (ĐƠN TỔNG) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId", nullable = false)
    private Order order;

    // --- QUAN HỆ VỚI PRODUCT (THUỐC) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

// Sửa "PriceAtPurchase" thành "UnitPrice"
    // ...
    @Column(name = "PriceAtPurchase", nullable = false, precision = 18, scale = 2)
    private BigDecimal priceAtPurchase;
// ...

    // --- CONSTRUCTORS ---
    public OrderItems() {
    }

    public OrderItems(Order order, Product product, Integer quantity, BigDecimal priceAtPurchase) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }

    // --- GETTERS & SETTERS ---
    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }
}
