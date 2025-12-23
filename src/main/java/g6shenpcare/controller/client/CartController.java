package g6shenpcare.controller.client;

import g6shenpcare.entity.*;
import g6shenpcare.repository.OrderRepository;
import g6shenpcare.service.CartService;
import g6shenpcare.service.ClientService;
import g6shenpcare.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired private CartService cartService;
    @Autowired private OrderRepository orderRepo;
    @Autowired private ClientService clientService;
    @Autowired private EmailService emailService;

    @GetMapping
    public String viewCart(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        
        ShoppingCart cart = cartService.getOrCreateCart(principal.getName());
        model.addAttribute("cart", cart);
        model.addAttribute("totalAmount", cartService.calculateTotal(cart));
        
        // Lấy thông tin user để điền form checkout
        CustomerProfile profile = clientService.getProfileByUsername(principal.getName());
        model.addAttribute("userAddress", profile.getAddressLine());
        model.addAttribute("userPhone", profile.getPhone());
        
        return "client/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Integer productId, 
                            @RequestParam Integer quantity, 
                            Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";
        try {
            cartService.addToCart(principal.getName(), productId, quantity);
            ra.addFlashAttribute("message", "Đã thêm vào giỏ hàng!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        // Redirect lại trang thuốc để mua tiếp
        return "redirect:/pharmacy";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Integer productId, Principal principal) {
        cartService.removeFromCart(principal.getName(), productId);
        return "redirect:/cart";
    }

    // CHECKOUT TỪ GIỎ HÀNG
    @PostMapping("/checkout")
    public String checkout(@RequestParam String address, 
                           @RequestParam String phone,
                           @RequestParam(required = false) String notes,
                           Principal principal, RedirectAttributes ra) {
        try {
            ShoppingCart cart = cartService.getOrCreateCart(principal.getName());
            if (cart.getItems().isEmpty()) {
                ra.addFlashAttribute("error", "Giỏ hàng trống!");
                return "redirect:/cart";
            }

            CustomerProfile profile = clientService.getProfileByUsername(principal.getName());

            // 1. Tạo Order
            Order order = new Order();
            order.setCustomerId(Long.valueOf(profile.getCustomerId()));
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("PENDING");
            order.setPaymentMethod("CASH"); // Mặc định là COD, nếu thanh toán MoMo sẽ update sau
            order.setShippingAddress(address + " (SĐT: " + phone + ")");
            order.setNotes(notes);
            order.setTotalAmount(cartService.calculateTotal(cart));
            
            // 2. Chuyển CartItems sang OrderItems
            for (ShoppingCartItems cartItem : cart.getItems()) {
                OrderItems orderItem = new OrderItems();
                orderItem.setOrder(order);
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPriceAtPurchase(cartItem.getUnitPrice()); // Lưu giá lúc mua
                order.addOrderItem(orderItem);
            }
            
            Order savedOrder = orderRepo.save(order);
            
            // 3. Xóa giỏ hàng
            cartService.clearCart(cart);

            // 4. Gửi email
            try {
                if (profile.getEmail() != null) {
                    emailService.sendOrderConfirmation(profile.getEmail(), profile.getFullName(), savedOrder.getOrderId(), String.format("%,.0f đ", savedOrder.getTotalAmount()));
                }
            } catch (Exception ex) {}

            ra.addFlashAttribute("message", "Đặt hàng thành công! Mã đơn: #" + savedOrder.getOrderId());
            // Chuyển hướng sang trang chọn thanh toán để khách trả MoMo nếu muốn
            return "redirect:/my-account/history"; 

        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi thanh toán: " + e.getMessage());
            return "redirect:/cart";
        }
    }
}