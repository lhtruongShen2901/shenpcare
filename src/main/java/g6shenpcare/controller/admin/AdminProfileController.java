package g6shenpcare.controller.admin;

import g6shenpcare.entity.UserAccount;
import g6shenpcare.repository.UserAccountRepository;
import g6shenpcare.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/profile")
public class AdminProfileController {

    private final UserService userService;
    private final UserAccountRepository userRepo;

    public AdminProfileController(UserService userService, UserAccountRepository userRepo) {
        this.userService = userService;
        this.userRepo = userRepo;
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Authentication auth,
                                 RedirectAttributes ra) {
        
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Mật khẩu nhập lại không khớp!");
            // Quay lại trang dashboard hoặc staff list tùy ngữ cảnh
            return "redirect:/admin/staff"; 
        }

        String currentUsername = auth.getName();
        UserAccount currentUser = userRepo.findByUsername(currentUsername).orElseThrow();

        userService.changePassword(currentUser.getUserId(), newPassword);
        
        ra.addFlashAttribute("message", "Đổi mật khẩu thành công. Vui lòng đăng nhập lại.");
        return "redirect:/admin/staff"; 
    }
}