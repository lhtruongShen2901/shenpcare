package g6shenpcare.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ShoppingCartItems")
public class ShoppingCartItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CartItemId")
    private Integer cartItemId;

    @ManyToOne
    @JoinColumn(name = "CartId", nullable = false)
    private ShoppingCart cart;

    @ManyToOne
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "UnitPrice", nullable = false)
    private BigDecimal unitPrice;

    // Getters & Setters
    public Integer getCartItemId() { return cartItemId; }
    public void setCartItemId(Integer cartItemId) { this.cartItemId = cartItemId; }
    public ShoppingCart getCart() { return cart; }
    public void setCart(ShoppingCart cart) { this.cart = cart; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    // Helper tính tổng tiền item
    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}