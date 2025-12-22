package g6shenpcare.controller.client;
//client
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        // Lấy thông tin người dùng đang đăng nhập (nếu có)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            model.addAttribute("username", auth.getName());
            model.addAttribute("isLoggedIn", true);
            // Kiểm tra role để hiển thị menu phù hợp
            boolean isCustomer = auth.getAuthorities().stream()
                                     .anyMatch(r -> r.getAuthority().equals("ROLE_CUSTOMER"));
            model.addAttribute("isCustomer", isCustomer);
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        return "client/home"; // Trả về file templates/client/home.html
    }
}