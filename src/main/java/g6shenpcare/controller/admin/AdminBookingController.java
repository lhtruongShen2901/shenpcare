package g6shenpcare.controller.admin;

import g6shenpcare.dto.BookingConfirmForm;
import g6shenpcare.dto.BookingMonitorDTO;
import g6shenpcare.entity.UserAccount;
import g6shenpcare.service.BookingService;
import g6shenpcare.service.ScheduleService;
import g6shenpcare.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/booking")
public class AdminBookingController {

    private final BookingService bookingService;
    private final ScheduleService scheduleService;
    private final UserService userService;

    public AdminBookingController(BookingService bookingService,
                                  ScheduleService scheduleService,
                                  UserService userService) {
        this.bookingService = bookingService;
        this.scheduleService = scheduleService;
        this.userService = userService;
    }

    // =======================================================
    // 1. MONITOR DASHBOARD (Hàm duy nhất, không trùng lặp)
    // =======================================================
    @GetMapping("/monitor")
    public String monitorPage(Model model,
                              @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              @RequestParam(value = "species", required = false, defaultValue = "ALL") String species,
                              @RequestParam(value = "keyword", required = false) String keyword) { // [ĐÃ SỬA: Đặt tham số đúng vị trí]

        if (date == null) {
            date = LocalDate.now();
        }

        // 1. Lấy danh sách DTO từ Service (Đã bao gồm logic lọc & tìm kiếm)
        List<BookingMonitorDTO> bookings = bookingService.getBookingMonitorData(date, species, keyword);

        // 2. Tính toán Sức chứa (Giả lập Spa & Grooming)
        // Trong thực tế bạn có thể gọi bookingService.getMaxQuota(...)
        int totalSlots = 20;
        long usedSlots = bookingService.getCurrentCount(date, "SPA"); // VD lấy SPA
        int progress = (int) ((double) usedSlots / totalSlots * 100);

        // 3. Truyền data sang View
        model.addAttribute("bookings", bookings);
        model.addAttribute("currentDate", date);
        model.addAttribute("speciesFilter", species);
        model.addAttribute("keyword", keyword); // Giữ lại keyword trong ô tìm kiếm

        model.addAttribute("spaProgress", progress);
        model.addAttribute("spaUsed", usedSlots);
        model.addAttribute("spaTotal", totalSlots);

        return "admin/quota-monitor";
    }

    // =======================================================
    // 2. CÁC API KHÁC
    // =======================================================
    @PostMapping("/confirm")
    public String confirmBooking(@ModelAttribute BookingConfirmForm form,
                                 Principal principal,
                                 RedirectAttributes ra) {
        try {
            UserAccount currentUser = userService.getUserByUsername(principal.getName());
            bookingService.confirmBooking(form, currentUser.getUserId());
            ra.addFlashAttribute("message", "Đã xác nhận đơn thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/booking/monitor";
    }

    @GetMapping("/api/staff-on-duty")
    @ResponseBody
    public List<Map<String, Object>> getStaffOnDuty(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                    @RequestParam("role") String role) {
        
        return scheduleService.getSchedulesByDateRange(date, date).stream()
                .filter(s -> s.getStaff() != null && s.getStaff().getRole().equalsIgnoreCase(role))
                .map(s -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("staffId", s.getStaffId());
                    m.put("fullName", s.getStaff().getFullName());
                    m.put("shift", s.getStartTime() + " - " + s.getEndTime());
                    return m;
                })
                .collect(Collectors.toList());
    }
    // Trong AdminBookingController.java

    // 1. API Cập nhật thông tin Booking (Đổi giờ, Đổi ngày, Đổi nhân viên)
    @PostMapping("/update")
    public String updateBooking(@RequestParam("bookingId") Integer bookingId,
                                @RequestParam("newDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate,
                                @RequestParam("newTime") LocalTime newTime,
                                @RequestParam("newStaffId") Integer newStaffId,
                                RedirectAttributes ra) {
        try {
            // Gọi Service xử lý (Bạn cần thêm hàm này vào Service)
            bookingService.updateBookingDetails(bookingId, newDate, newTime, newStaffId);
            ra.addFlashAttribute("message", "Đã cập nhật thông tin đơn hàng #" + bookingId);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/booking/monitor?date=" + newDate; // Quay lại đúng ngày vừa chuyển
    }

    // 2. API Cập nhật giới hạn (Quota) - Demo nhanh
    @PostMapping("/update-quota")
    public String updateQuota(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              @RequestParam("newLimit") Integer newLimit,
                              RedirectAttributes ra) {
        // Lưu vào DB (Cần tạo hàm trong Service, tạm thời log ra console)
        System.out.println("Cập nhật giới hạn ngày " + date + " thành: " + newLimit);
        // service.updateDailyLimit(date, "SPA", newLimit); 
        ra.addFlashAttribute("message", "Đã cập nhật giới hạn phục vụ!");
        return "redirect:/admin/booking/monitor?date=" + date;
    }
}