package g6shenpcare.controller.admin;

import g6shenpcare.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/report")
public class AdminReportController {

    @Autowired private MedicalRecordService medicalService;

    @GetMapping("/daily")
    public String showDailyReport(Model model) {
        // Admin xem Doctor hôm nay làm ăn thế nào
        model.addAttribute("reports", medicalService.getDailyStats());
        return "admin/daily_report"; // Tạo file HTML hiển thị bảng này
    }
}