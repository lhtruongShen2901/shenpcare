package g6shenpcare.controller.admin;

import g6shenpcare.dto.RevenueDashboardDTO;
import g6shenpcare.repository.BookingRepository;
import g6shenpcare.repository.MedicalRecordRepository;
import g6shenpcare.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/report")
public class AdminReportController {

    @Autowired private MedicalRecordRepository medicalRepo;
    @Autowired private BookingRepository bookingRepo;
    @Autowired private OrderRepository orderRepo;

    @GetMapping("/daily")
    public String showDailyReport(Model model) {
        LocalDate today = LocalDate.now();
        RevenueDashboardDTO dashboard = new RevenueDashboardDTO();

        // 1. Lấy doanh thu Bán thuốc Online (Order)
        BigDecimal onlineRev = orderRepo.calculateDailyOnlineRevenue(today);
        dashboard.setTotalOnlineRevenue(onlineRev);

        // 2. Lấy doanh thu Dịch vụ Spa (Booking loại SPA)
        BigDecimal spaRev = bookingRepo.calculateDailyServiceRevenue(today, "SPA");
        dashboard.setTotalSpaRevenue(spaRev);

        // 3. Lấy doanh thu Phòng khám (Dịch vụ Khám + Thuốc kê đơn)
        // a. Tiền công khám (Booking loại CLINIC)
        BigDecimal clinicServiceRev = bookingRepo.calculateDailyServiceRevenue(today, "CLINIC");
        
        // b. Tiền thuốc kê đơn (Lấy từ list DailyReportDTO cũ của bạn)
        // Lưu ý: Query cũ của bạn trong MedicalRecordRepository tính tổng tiền thuốc theo từng bác sĩ
        // Chúng ta cần sum lại.
        var doctorStats = medicalRepo.getDailyReportByDate(today);
        BigDecimal prescribedMedicineRev = doctorStats.stream()
                .map(d -> d.getEstimatedRevenue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        dashboard.setTotalClinicRevenue(clinicServiceRev.add(prescribedMedicineRev));
        
        // 4. Tổng cộng tất cả
        dashboard.setTotalRevenueToday(
                onlineRev.add(spaRev).add(dashboard.getTotalClinicRevenue())
        );

        // 5. Set chi tiết hiệu suất bác sĩ (để vẽ bảng)
        dashboard.setDoctorPerformance(doctorStats);

        model.addAttribute("data", dashboard);
        return "admin/daily_report";
    }
}