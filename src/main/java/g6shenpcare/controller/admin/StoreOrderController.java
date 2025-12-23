package g6shenpcare.controller.admin;

import g6shenpcare.entity.Order;
import g6shenpcare.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class StoreOrderController {

    @Autowired
    private OrderRepository orderRepo;

    // Xem danh sách đơn hàng
    @GetMapping
    public String listOrders(Model model, Principal principal) {
        String username = (principal != null) ? principal.getName() : "admin";
        model.addAttribute("currentUser", username);
        model.addAttribute("activeMenu", "orders"); // Để active menu sidebar

        // Lấy tất cả đơn hàng, mới nhất lên đầu
        List<Order> orders = orderRepo.findAll(Sort.by(Sort.Direction.DESC, "orderDate"));
        model.addAttribute("orders", orders);

        return "admin/order-list";
    }

    // Xử lý đơn hàng (Duyệt / Hủy)
    @PostMapping("/update-status")
    public String updateStatus(@RequestParam("orderId") Long orderId,
            @RequestParam("status") String status,
            RedirectAttributes ra) {
        try {
            Order order = orderRepo.findById(orderId).orElseThrow();

            // Logic: Nếu đang là PENDING mà chuyển sang CONFIRMED -> Giữ nguyên (vì đã trừ kho lúc khách mua)
            // Nếu HỦY đơn (CANCELLED) -> Cần cộng lại kho (Logic nâng cao, tạm thời chỉ đổi status)
            order.setStatus(status);
            orderRepo.save(order);

            ra.addFlashAttribute("message", "Đã cập nhật đơn hàng #" + orderId + " sang " + status);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }
    // THÊM API NÀY VÀO

    @PostMapping("/confirm-payment")
    public String confirmOrderPayment(@RequestParam("orderId") Long orderId, RedirectAttributes ra) {
        try {
            Order order = orderRepo.findById(orderId).orElseThrow();
            // Giả sử trong Entity Order bạn chưa có field paymentStatus, 
            // ta tạm dùng status chung hoặc bạn cần thêm field paymentStatus vào Entity Order.
            // Ở đây mình giả định luồng: CONFIRMED -> COMPLETED (nghĩa là đã giao và thu tiền)

            order.setStatus("COMPLETED");
            orderRepo.save(order);

            ra.addFlashAttribute("message", "Đã xác nhận hoàn thành đơn hàng #" + orderId);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }
}
