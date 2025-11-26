package g6shenpcare.controller.admin;

import g6shenpcare.dto.AdminRegisterForm;
import g6shenpcare.entity.UserAccount;
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
    private final PasswordEncoder passwordEncoder;

    public AdminAuthController(UserAccountRepository userRepo,
                               PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // ================== LOGIN ==================

    @GetMapping("/login")
    public String showLogin() {
        // View: src/main/resources/templates/admin/login.html
        // param.error, param.logout, registerSuccess... xử lý ở Thymeleaf
        return "admin/login";
    }

    // ================== REGISTER ADMIN (GET) ==================

    @GetMapping("/register")
    public String showRegister(Model model,
                               RedirectAttributes redirectAttributes) {

        long adminCount = userRepo.countByRoleIgnoreCase("ADMIN");

        // Nếu đã có ít nhất 1 ADMIN thì không cho đăng ký nữa
        if (adminCount > 0) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Hệ thống đã có tài khoản Admin. Vui lòng đăng nhập."
            );
            return "redirect:/admin/login";
        }

        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new AdminRegisterForm());
        }

        // View: src/main/resources/templates/admin/register.html
        return "admin/register";
    }

    // ================== REGISTER ADMIN (POST) ==================

    @PostMapping("/register")
    public String handleRegister(
            @Valid @ModelAttribute("form") AdminRegisterForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Kiểm tra lại lần nữa: nếu đã có ADMIN thì chặn
        long adminCount = userRepo.countByRoleIgnoreCase("ADMIN");
        if (adminCount > 0) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Hệ thống đã có tài khoản Admin. Vui lòng đăng nhập."
            );
            return "redirect:/admin/login";
        }

        // Lỗi validate annotation (@NotBlank, @Size, @Email, ...)
        if (bindingResult.hasErrors()) {
            model.addAttribute("form", form);
            return "admin/register";
        }

        // Kiểm tra trùng username (không phân biệt hoa thường)
        if (userRepo.existsByUsernameIgnoreCase(form.getUsername())) {
            model.addAttribute("form", form);
            model.addAttribute("usernameError", "Tên đăng nhập đã tồn tại.");
            return "admin/register";
        }

        // Kiểm tra mật khẩu và confirm mật khẩu
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("form", form);
            model.addAttribute("passwordError", "Mật khẩu nhập lại không khớp.");
            return "admin/register";
        }

        // Tạo user ADMIN
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

        userRepo.save(admin);

        // Gửi message sang trang login
        redirectAttributes.addFlashAttribute(
                "registerSuccess",
                "Tạo tài khoản Admin thành công. Vui lòng đăng nhập."
        );

        return "redirect:/admin/login";
    }
}
