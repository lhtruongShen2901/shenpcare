package g6shenpcare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootRedirectController {
    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/admin/login";
    }
}
