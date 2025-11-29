package g6shenpcare.controller.admin;

import g6shenpcare.entity.UserAccount;
import g6shenpcare.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/customers") // Đường dẫn riêng cho khách hàng
public class AdminCustomerController {

    private final UserService userService;

    public AdminCustomerController(UserService userService) {
        this.userService = userService;
    }

    private void addCommonHeader(Model model, Principal principal) {
        String username = (principal != null) ? principal.getName() : "admin";
        model.addAttribute("currentUser", username);
        model.addAttribute("clinicName", "ShenPCare Clinic");
        model.addAttribute("activeMenu", "customers"); // Menu active là 'customers'
    }

    @GetMapping
    public String listCustomers(
            @RequestParam(name = "status", required = false, defaultValue = "ALL") String statusFilter,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            Model model,
            Principal principal
    ) {
        // Gọi hàm searchCustomers chuyên biệt (Chỉ lấy Role = CUSTOMER)
        List<UserAccount> customers = userService.searchCustomers(statusFilter, keyword);

        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Quản lý Khách hàng");

        model.addAttribute("statuses", Arrays.asList("ALL", "ACTIVE", "LOCKED"));
        model.addAttribute("selectedStatus", statusFilter);
        model.addAttribute("keyword", keyword);

        // Data table
        model.addAttribute("users", customers);
        model.addAttribute("totalUsers", customers.size());
        model.addAttribute("displayedCount", customers.size());
        
        // Lưu ý: Khách hàng KHÔNG có Role filter vì họ mặc định là CUSTOMER
        // Bạn có thể tạo file admin/customers.html riêng nếu muốn bỏ cột Role
        return "admin/customer-list"; 
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleCustomerStatus(@PathVariable("id") Long userId,
                                       RedirectAttributes redirectAttributes) {
        userService.toggleUserStatus(userId);
        redirectAttributes.addFlashAttribute("message", "Đã cập nhật trạng thái khách hàng.");
        return "redirect:/admin/customers";
    }
}