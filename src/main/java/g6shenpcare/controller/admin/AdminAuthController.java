package g6shenpcare.controller.admin;

import g6shenpcare.dto.AdminRegisterForm;
import g6shenpcare.entity.StaffProfile;
import g6shenpcare.entity.UserAccount;
import g6shenpcare.repository.StaffProfileRepository;
import g6shenpcare.repository.UserAccountRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    private final UserAccountRepository userRepo;
    private final StaffProfileRepository staffRepo;
    private final PasswordEncoder passwordEncoder;

    public AdminAuthController(UserAccountRepository userRepo,
                               StaffProfileRepository staffRepo,
                               PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.staffRepo = staffRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // --- LOGIN ---
    @GetMapping("/login")
    public String showLogin() {
        return "admin/login";
    }

    // --- ĐĂNG KÝ ADMIN ĐẦU TIÊN (HỆ THỐNG CHỈ CHO PHÉP 1 ADMIN TỰ REG) ---
    @GetMapping("/register")
    public String showRegister(Model model, RedirectAttributes ra) {
        if (userRepo.countByRoleIgnoreCase("ADMIN") > 0) {
            ra.addFlashAttribute("errorMessage", "Hệ thống đã có Admin. Vui lòng đăng nhập.");
            return "redirect:/admin/login";
        }
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new AdminRegisterForm());
        }
        return "admin/register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("form") AdminRegisterForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes ra,
                                 Model model) {

        // Check kép an toàn
        if (userRepo.countByRoleIgnoreCase("ADMIN") > 0) {
            return "redirect:/admin/login";
        }

        if (bindingResult.hasErrors()) return "admin/register";

        if (userRepo.existsByUsernameIgnoreCase(form.getUsername())) {
            model.addAttribute("usernameError", "Tên đăng nhập đã tồn tại.");
            return "admin/register";
        }

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("passwordError", "Mật khẩu không khớp.");
            return "admin/register";
        }

        // Tạo Admin Account
        UserAccount admin = new UserAccount();
        admin.setUsername(form.getUsername().trim());
        admin.setFullName(form.getFullName().trim());
        admin.setEmail(form.getEmail());
        admin.setPhone(form.getPhone());
        admin.setRole("ADMIN");
        admin.setActive(true);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        admin.setPasswordHash(passwordEncoder.encode(form.getPassword()));

        UserAccount savedAdmin = userRepo.save(admin);

        // Tạo Profile cho Admin (Admin cũng là nhân viên cấp cao)
        StaffProfile profile = new StaffProfile();
        profile.setStaffId(savedAdmin.getUserId());
        profile.setStaffCode("ADM001");
        profile.setPosition("Quản trị viên hệ thống");
        profile.setAnnualLeaveQuota(12);
        
        staffRepo.save(profile);

        ra.addFlashAttribute("registerSuccess", "Khởi tạo Admin thành công. Hãy đăng nhập.");
        return "redirect:/admin/login";
    }
}