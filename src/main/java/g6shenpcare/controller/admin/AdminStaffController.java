package g6shenpcare.controller.admin;

import g6shenpcare.dto.UserForm;
import g6shenpcare.entity.UserAccount;
import g6shenpcare.repository.StaffProfileRepository;
import g6shenpcare.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/admin/staff") // Đổi đường dẫn cơ sở thành /admin/staff
public class AdminStaffController {

    private final UserService userService;
    private final StaffProfileRepository staffProfileRepository;

    public AdminStaffController(UserService userService,
                                StaffProfileRepository staffProfileRepository) {
        this.userService = userService;
        this.staffProfileRepository = staffProfileRepository;
    }

    // ==== Helper ====
    private List<String> getStaffRoles() {
        // Chỉ liệt kê các Role thuộc về nhân viên
        return Arrays.asList("ADMIN", "DOCTOR", "GROOMER", "SUPPORT", "STORE", "ACCOUNTANT");
    }

    private void addCommonHeader(Model model, Principal principal) {
        String username = (principal != null) ? principal.getName() : "admin";
        model.addAttribute("currentUser", username);
        model.addAttribute("clinicName", "ShenPCare Clinic");
        model.addAttribute("activeMenu", "staff"); // Đặt activeMenu là 'staff' để sáng menu bên trái
    }

    // ================== LIST STAFF ==================
    @GetMapping
    public String listStaff(
            @RequestParam(name = "role", required = false, defaultValue = "ALL") String roleFilter,
            @RequestParam(name = "status", required = false, defaultValue = "ALL") String statusFilter,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            Model model,
            Principal principal
    ) {
        // Gọi hàm searchStaff chuyên biệt trong Service
        List<UserAccount> staffList = userService.searchStaff(roleFilter, statusFilter, keyword);

        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Quản lý Nhân sự");
        
        // Dropdown data
        List<String> roles = new LinkedList<>();
        roles.add("ALL");
        roles.addAll(getStaffRoles());
        
        model.addAttribute("roles", roles);
        model.addAttribute("statuses", Arrays.asList("ALL", "ACTIVE", "LOCKED"));
        
        // Filter selection state
        model.addAttribute("selectedRole", roleFilter);
        model.addAttribute("selectedStatus", statusFilter);
        model.addAttribute("keyword", keyword);

        // Data table
        model.addAttribute("users", staffList);
        model.addAttribute("totalUsers", staffList.size());
        model.addAttribute("displayedCount", staffList.size());

        // Tái sử dụng view users.html (Bạn cần sửa lại link trong html này sau)
        return "admin/staff-list"; 
    }

    // ================== CREATE FORM ==================
    @GetMapping("/new")
    public String showCreateForm(Model model, Principal principal) {
        UserForm form = new UserForm();
        form.setStatus("ACTIVE");

        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Thêm Nhân viên mới");
        model.addAttribute("userForm", form);
        model.addAttribute("roles", getStaffRoles());
        model.addAttribute("statuses", Arrays.asList("ACTIVE", "LOCKED"));
        model.addAttribute("isEdit", false);

        // Tái sử dụng form user-detail.html
        return "admin/staff-detail";
    }

    // ================== EDIT FORM ==================
    @GetMapping("/{id}")
    public String showEditForm(@PathVariable("id") Long userId,
                               Model model,
                               Principal principal) {
        UserAccount user = userService.getUserById(userId);

        UserForm form = new UserForm();
        form.setUserId(user.getUserId());
        form.setUsername(user.getUsername());
        form.setFullName(user.getFullName());
        form.setEmail(user.getEmail());
        form.setPhone(user.getPhone());
        form.setRole(user.getRole());
        form.setStatus(user.isActive() ? "ACTIVE" : "LOCKED");

        // Load thông tin chuyên môn từ StaffProfile
        staffProfileRepository.findById(userId).ifPresent(sp -> {
            form.setHireDate(sp.getHireDate());
            form.setSpecialization(sp.getSpecialization());
            form.setLicenseNumber(sp.getLicenseNumber());
        });

        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Chỉnh sửa Nhân viên");
        model.addAttribute("userForm", form);
        model.addAttribute("roles", getStaffRoles());
        model.addAttribute("statuses", Arrays.asList("ACTIVE", "LOCKED"));
        model.addAttribute("isEdit", true);

        return "admin/staff-detail";
    }

    // ================== SAVE ==================
    @PostMapping("/save")
    public String saveStaff(@ModelAttribute("userForm") UserForm form,
                            RedirectAttributes redirectAttributes) {
        
        // Gọi Service (đã có Transaction & Logic sinh StaffCode tự động)
        userService.saveUser(form);

        redirectAttributes.addFlashAttribute("message", 
            (form.getUserId() != null) ? "Đã cập nhật nhân viên." : "Đã thêm nhân viên mới.");

        // Redirect về danh sách staff
        return "redirect:/admin/staff";
    }

    // ================== TOGGLE STATUS ==================
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") Long userId,
                               RedirectAttributes redirectAttributes) {
        userService.toggleUserStatus(userId);
        redirectAttributes.addFlashAttribute("message", "Đã cập nhật trạng thái tài khoản.");
        return "redirect:/admin/staff";
    }
}