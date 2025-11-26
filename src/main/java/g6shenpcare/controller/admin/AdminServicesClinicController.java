package g6shenpcare.controller.admin;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminServicesClinicController {

    @GetMapping("/services")
    public String servicesClinic(Model model, Principal principal) {
        String username = (principal != null) ? principal.getName() : "admin";

        model.addAttribute("currentUser", username);
        model.addAttribute("clinicName", "ShenPCare Clinic");

        model.addAttribute("pageTitle", "Services & Clinic");
        model.addAttribute("activeMenu", "services");

        // Sau này sẽ truyền list services, clinic branches...
        return "admin/services";
    }
}
