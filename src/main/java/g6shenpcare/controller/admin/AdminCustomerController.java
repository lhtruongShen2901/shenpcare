package g6shenpcare.controller.admin;

import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.entity.UserAccount;
import g6shenpcare.repository.CustomerProfileRepository;
import g6shenpcare.service.PetService;
import g6shenpcare.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    private final UserService userService;
    private final PetService petService;
    private final CustomerProfileRepository customerProfileRepo;

    // Inject thêm PetService và CustomerProfileRepository để lấy dữ liệu cho trang chi tiết
    public AdminCustomerController(UserService userService,
            PetService petService,
            CustomerProfileRepository customerProfileRepo) {
        this.userService = userService;
        this.petService = petService;
        this.customerProfileRepo = customerProfileRepo;
    }

    private void addCommonHeader(Model model, Principal principal) {
        String username = (principal != null) ? principal.getName() : "admin";
        model.addAttribute("currentUser", username);
        model.addAttribute("clinicName", "ShenPCare Clinic");
        model.addAttribute("activeMenu", "customers");
    }

    // 1. DANH SÁCH KHÁCH HÀNG
    @GetMapping
    public String listCustomers(
            @RequestParam(name = "status", required = false, defaultValue = "ALL") String statusFilter,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            Model model,
            Principal principal
    ) {
        List<UserAccount> customers = userService.searchCustomers(statusFilter, keyword);

        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Quản lý Khách hàng");

        model.addAttribute("statuses", Arrays.asList("ALL", "ACTIVE", "LOCKED"));
        model.addAttribute("selectedStatus", statusFilter);
        model.addAttribute("keyword", keyword);

        model.addAttribute("users", customers);
        model.addAttribute("totalUsers", customers.size());
        model.addAttribute("displayedCount", customers.size());

        return "admin/customer-list";
    }

    // 2. CHI TIẾT KHÁCH HÀNG (CUSTOMER 360) & DANH SÁCH THÚ CƯNG
    @GetMapping("/{id}")
    public String customerDetail(@PathVariable("id") Integer userId, Model model, Principal principal) {
        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Hồ sơ Khách hàng (Customer 360)");

        // A. Lấy thông tin tài khoản (UserAccount)
        UserAccount user = userService.getUserById(userId);
        model.addAttribute("user", user);

        // B. Lấy thông tin hồ sơ chi tiết (CustomerProfile)
        // Nếu chưa có profile (do lỗi data cũ), tạo đối tượng rỗng để tránh lỗi null trên view
        CustomerProfile profile = customerProfileRepo.findById(userId).orElse(new CustomerProfile());
        model.addAttribute("profile", profile);

        // C. Lấy danh sách Thú cưng (Pets)
        model.addAttribute("pets", petService.getPetsByCustomer(userId));

        // Trả về view chi tiết (File này chúng ta sẽ tạo ở bước kế tiếp)
        return "admin/customer-detail";
    }

// 3. KHÓA / MỞ KHÓA TÀI KHOẢN KHÁCH HÀNG
    @PostMapping("/{id}/toggle-status")
    public String toggleCustomerStatus(@PathVariable("id") Integer userId,
            Principal principal, // <--- THÊM THAM SỐ NÀY
            RedirectAttributes redirectAttributes) {

        // Gọi Service với 2 tham số (để check xem có tự khóa mình không - dù Admin ít khi là Customer)
        userService.toggleUserStatus(userId, principal.getName());

        redirectAttributes.addFlashAttribute("message", "Đã cập nhật trạng thái khách hàng.");
        return "redirect:/admin/customers";
    }
}
