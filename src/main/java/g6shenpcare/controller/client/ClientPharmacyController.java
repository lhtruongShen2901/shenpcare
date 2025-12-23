package g6shenpcare.controller.client;

import g6shenpcare.entity.*;
import g6shenpcare.repository.*;
import g6shenpcare.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pharmacy")
public class ClientPharmacyController {

    @Autowired private ProductRepository productRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private OrderItemsRepository orderItemsRepo;
    @Autowired private ClientService clientService;

    // 1. HIỂN THỊ DANH SÁCH THUỐC (GIỮ NGUYÊN)
    @GetMapping
    public String showPharmacy(Model model, Principal principal) {
        List<Product> medicines = productRepo.findByIsMedicineTrueAndIsActiveTrue();
        model.addAttribute("medicines", medicines);
        
        // Lấy thông tin user để điền form mua nhanh (nếu có)
        loadUserInfo(model, principal);
        
        return "client/pharmacy";
    }

    // [MỚI] 2. XEM CHI TIẾT SẢN PHẨM
    @GetMapping("/product/{id}")
    public String viewProductDetail(@PathVariable("id") Integer id, Model model, Principal principal) {
        // A. Lấy thông tin sản phẩm
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại: " + id));
        model.addAttribute("p", product);

        // B. Lấy sản phẩm liên quan (Cùng danh mục, khác ID hiện tại)
        List<Product> relatedProducts = productRepo.findByIsMedicineTrueAndIsActiveTrue().stream()
                .filter(pr -> pr.getCategory() != null && pr.getCategory().equals(product.getCategory()))
                .filter(pr -> !pr.getProductId().equals(id))
                .limit(4) // Lấy tối đa 4 sản phẩm
                .collect(Collectors.toList());
        model.addAttribute("relatedProducts", relatedProducts);

        // C. Lấy thông tin User (Để điền vào Modal Mua Ngay tại trang chi tiết)
        loadUserInfo(model, principal);

        return "client/product-detail"; // File HTML mới
    }

    // 3. XỬ LÝ MUA HÀNG (GIỮ NGUYÊN LOGIC CŨ)
    @PostMapping("/buy")
    public String buyProduct(@RequestParam("productId") Integer productId,
                             @RequestParam("quantity") Integer quantity,
                             @RequestParam("address") String address,
                             @RequestParam("phone") String phone,
                             @RequestParam(value = "notes", required = false) String notes,
                             Principal principal,
                             RedirectAttributes ra) {
        
        if (principal == null) return "redirect:/login";

        try {
            CustomerProfile profile = clientService.getProfileByUsername(principal.getName());
            Product product = productRepo.findById(productId).orElseThrow();

            // A. Kiểm tra tồn kho
            if (product.getStockQuantity() < quantity) {
                ra.addFlashAttribute("error", "Sản phẩm '" + product.getName() + "' chỉ còn " + product.getStockQuantity() + " sản phẩm.");
                return "redirect:/pharmacy/product/" + productId; // Trả về trang chi tiết nếu lỗi
            }

            // B. Tạo Đơn hàng Tổng (Order)
            Order order = new Order();
            order.setCustomerId(Long.valueOf(profile.getCustomerId()));
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("PENDING"); 
            order.setShippingAddress(address + " (SĐT: " + phone + ")");
            order.setNotes(notes);
            
            BigDecimal itemPrice = product.getRetailPrice();
            BigDecimal total = itemPrice.multiply(BigDecimal.valueOf(quantity));
            order.setTotalAmount(total);

            Order savedOrder = orderRepo.save(order);

            // C. Tạo Chi tiết đơn
            OrderItems item = new OrderItems();
            item.setOrder(savedOrder);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setPriceAtPurchase(itemPrice); 
            
            orderItemsRepo.save(item);

            // D. Trừ kho
            product.setStockQuantity(product.getStockQuantity() - quantity);
            productRepo.save(product);

            ra.addFlashAttribute("message", "Đặt hàng thành công! Mã đơn: #" + savedOrder.getOrderId());

        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi đặt hàng: " + e.getMessage());
        }

        return "redirect:/my-account/history"; 
    }

    // Helper: Hàm dùng chung để lấy thông tin user
    private void loadUserInfo(Model model, Principal principal) {
        if (principal != null) {
            try {
                CustomerProfile profile = clientService.getProfileByUsername(principal.getName());
                model.addAttribute("userAddress", profile.getAddressLine());
                model.addAttribute("userPhone", profile.getPhone());
            } catch (Exception e) {
                // Bỏ qua lỗi profile
            }
        }
    }
}