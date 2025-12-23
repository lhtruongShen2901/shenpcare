package g6shenpcare.service;

import g6shenpcare.entity.*;
import g6shenpcare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CartService {

    @Autowired private ShoppingCartRepository cartRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private ClientService clientService;

    // Lấy giỏ hàng của khách (Nếu chưa có thì tạo mới)
    public ShoppingCart getOrCreateCart(String username) {
        CustomerProfile profile = clientService.getProfileByUsername(username);
        return cartRepo.findByCustomerId(profile.getCustomerId())
                .orElseGet(() -> {
                    ShoppingCart newCart = new ShoppingCart();
                    newCart.setCustomerId(profile.getCustomerId());
                    newCart.setCreatedAt(LocalDateTime.now());
                    newCart.setLastUpdated(LocalDateTime.now());
                    return cartRepo.save(newCart);
                });
    }

    // Thêm sản phẩm vào giỏ
    @Transactional
    public void addToCart(String username, Integer productId, Integer quantity) {
        ShoppingCart cart = getOrCreateCart(username);
        Product product = productRepo.findById(productId).orElseThrow();

        // Kiểm tra xem sp đã có trong giỏ chưa
        ShoppingCartItems existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(productId))
                .findFirst().orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            ShoppingCartItems newItem = new ShoppingCartItems();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setUnitPrice(product.getRetailPrice());
            cart.getItems().add(newItem);
        }
        cart.setLastUpdated(LocalDateTime.now());
        cartRepo.save(cart);
    }

    // Xóa sản phẩm khỏi giỏ
    @Transactional
    public void removeFromCart(String username, Integer productId) {
        ShoppingCart cart = getOrCreateCart(username);
        cart.getItems().removeIf(item -> item.getProduct().getProductId().equals(productId));
        cartRepo.save(cart);
    }
    
    // Clear giỏ sau khi mua
    @Transactional
    public void clearCart(ShoppingCart cart) {
        cart.getItems().clear();
        cartRepo.save(cart);
    }
    
    // Tính tổng tiền giỏ hàng
    public BigDecimal calculateTotal(ShoppingCart cart) {
        return cart.getItems().stream()
                .map(ShoppingCartItems::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}