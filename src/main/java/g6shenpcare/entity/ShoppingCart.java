package g6shenpcare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ShoppingCarts")
public class ShoppingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CartId")
    private Integer cartId;

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "LastUpdated")
    private LocalDateTime lastUpdated;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShoppingCartItems> items = new ArrayList<>();

    // Getters & Setters
    public Integer getCartId() { return cartId; }
    public void setCartId(Integer cartId) { this.cartId = cartId; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public List<ShoppingCartItems> getItems() { return items; }
    public void setItems(List<ShoppingCartItems> items) { this.items = items; }
}