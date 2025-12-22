package g6shenpcare.controller.admin;

import g6shenpcare.entity.Booking;
import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.entity.Pets;
import g6shenpcare.entity.UserAccount;
import g6shenpcare.repository.BookingRepository;
import g6shenpcare.repository.CustomerProfileRepository;
import g6shenpcare.repository.PetRepository;
import g6shenpcare.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    private final UserService userService;
    private final PetRepository petRepo;
    private final CustomerProfileRepository customerProfileRepo;
    private final BookingRepository bookingRepo;

    public AdminCustomerController(UserService userService,
            PetRepository petRepo,
            CustomerProfileRepository customerProfileRepo,
            BookingRepository bookingRepo) {
        this.userService = userService;
        this.petRepo = petRepo;
        this.customerProfileRepo = customerProfileRepo;
        this.bookingRepo = bookingRepo;
    }

    private void addCommonAttributes(Model model, Principal principal, String activeMenu) {
        String username = (principal != null) ? principal.getName() : "admin";
        model.addAttribute("currentUser", username);
        model.addAttribute("activeMenu", activeMenu);
    }

    // --- 1. DANH SÁCH KHÁCH HÀNG (CHỈ HIỂN THỊ TÀI KHOẢN) ---
    @GetMapping
    public String listCustomers(
            @RequestParam(name = "status", required = false, defaultValue = "ALL") String statusFilter,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            Model model,
            Principal principal
    ) {
        // Tìm kiếm user có role là CUSTOMER
        List<UserAccount> customers = userService.searchCustomers(statusFilter, keyword);

        addCommonAttributes(model, principal, "customers");
        model.addAttribute("pageTitle", "Quản lý Khách hàng");
        model.addAttribute("selectedStatus", statusFilter);
        model.addAttribute("keyword", keyword);
        model.addAttribute("users", customers);
        model.addAttribute("totalCount", customers.size());

        return "admin/customer-list";
    }

    // --- 2. CHI TIẾT KHÁCH HÀNG (CUSTOMER 360 & LỊCH SỬ) ---
    @GetMapping("/detail/{id}")
    public String customerDetail(@PathVariable("id") Integer userId, Model model, Principal principal) {
        addCommonAttributes(model, principal, "customers");
        model.addAttribute("pageTitle", "Hồ sơ Khách hàng");

        // A. Tài khoản
        UserAccount user = userService.getUserById(userId);
        model.addAttribute("user", user);

        // B. Hồ sơ chi tiết (Profile)
        CustomerProfile profile = customerProfileRepo.findByUserId(userId).orElse(new CustomerProfile());
        model.addAttribute("profile", profile);

        // C. Thú cưng & Lịch sử Booking
        List<Pets> pets = new ArrayList<>();
        List<Booking> bookings = new ArrayList<>();

        if (profile.getCustomerId() != null) {
            pets = petRepo.findByCustomerId(profile.getCustomerId());
            bookings = bookingRepo.findByCustomerIdOrderByCreatedAtDesc(profile.getCustomerId());
        }

        model.addAttribute("pets", pets);
        model.addAttribute("bookings", bookings);

        return "admin/customer-detail";
    }

    // --- 3. KHÓA / MỞ KHÓA TÀI KHOẢN ---
    @PostMapping("/{id}/toggle-status")
    public String toggleCustomerStatus(@PathVariable("id") Integer userId,
            Principal principal,
            RedirectAttributes ra) {
        userService.toggleUserStatus(userId, principal.getName());
        ra.addFlashAttribute("message", "Đã cập nhật trạng thái tài khoản thành công.");
        return "redirect:/admin/customers";
    }
}
