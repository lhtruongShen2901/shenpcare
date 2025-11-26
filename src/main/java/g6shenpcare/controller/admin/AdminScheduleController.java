package g6shenpcare.controller.admin;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminScheduleController {

    @GetMapping("/schedule")
    public String workingSchedule(Model model, Principal principal) {
        String username = (principal != null) ? principal.getName() : "admin";

        model.addAttribute("currentUser", username);
        model.addAttribute("clinicName", "ShenPCare Clinic");

        model.addAttribute("pageTitle", "Working schedule & Slot");
        model.addAttribute("activeMenu", "schedule");

        // Sau này: truyền cấu hình lịch + slot
        return "admin/schedule";
    }
}
