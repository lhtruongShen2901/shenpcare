package g6shenpcare.controller.client;

import g6shenpcare.dto.CustomerRegisterForm;
import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.entity.UserAccount;
import g6shenpcare.repository.CustomerProfileRepository;
import g6shenpcare.repository.UserAccountRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;

@Controller
public class ClientAuthController {

    private final UserAccountRepository userRepo;
    private final CustomerProfileRepository profileRepo;
    private final PasswordEncoder passwordEncoder;

    public ClientAuthController(UserAccountRepository userRepo, 
                                CustomerProfileRepository profileRepo,
                                PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // Trang đăng nhập (Dùng chung giao diện hoặc tách riêng tùy bạn)
    @GetMapping("/login")
    public String showLoginPage() {
        return "client/login"; 
    }

    // Form đăng ký KHÁCH HÀNG
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CustomerRegisterForm());
        }
        return "client/register"; 
    }

    // Xử lý đăng ký
    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("form") CustomerRegisterForm form,
                                  BindingResult result, Model model) {
        
        // 1. Validate Form
        if (result.hasErrors()) return "client/register";

        // 2. Validate Logic
        if (userRepo.existsByUsernameIgnoreCase(form.getUsername())) {
            model.addAttribute("usernameError", "Tên đăng nhập đã tồn tại");
            return "client/register";
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
             model.addAttribute("passwordError", "Mật khẩu không khớp");
             return "client/register";
        }

        // 3. Tạo User Account
        UserAccount user = new UserAccount();
        user.setUsername(form.getUsername());
        user.setFullName(form.getFullName());
        user.setEmail(form.getEmail());
        user.setPhone(form.getPhone());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        
        user.setRole("CUSTOMER"); // Luôn là Customer
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        UserAccount savedUser = userRepo.save(user);

        // 4. Tạo Customer Profile (Quan trọng để lưu địa chỉ, lịch sử khám sau này)
        CustomerProfile profile = new CustomerProfile();
        profile.setUserId(savedUser.getUserId());
        profile.setFullName(savedUser.getFullName());
        profile.setEmail(savedUser.getEmail());
        profile.setPhone(savedUser.getPhone());
        profile.setIsActive(true);
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        
        profileRepo.save(profile);

        return "redirect:/login?registerSuccess=true";
    }
}