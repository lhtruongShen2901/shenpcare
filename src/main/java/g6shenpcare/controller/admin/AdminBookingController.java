package g6shenpcare.controller.admin;

import g6shenpcare.entity.Booking;
import g6shenpcare.entity.UserAccount;
import g6shenpcare.repository.BookingRepository;
import g6shenpcare.service.BookingService;
import g6shenpcare.service.UserService;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/bookings") // Số nhiều cho chuẩn REST
public class AdminBookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final BookingRepository bookingRepo;

    public AdminBookingController(BookingService bookingService, UserService userService, BookingRepository bookingRepo) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.bookingRepo = bookingRepo;
    }

    private void addCommonAttributes(Model model, Principal principal, String activeMenu) {
        String username = (principal != null) ? principal.getName() : "admin";
        model.addAttribute("currentUser", username);
        model.addAttribute("activeMenu", activeMenu);
    }

    // --- 1. TRANG DANH SÁCH BOOKING (QUẢN LÝ TẬP TRUNG) ---
    @GetMapping("/list")
    public String listBookings(
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "status", required = false, defaultValue = "ALL") String status,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            Model model,
            Principal principal) {

        // 1. Lấy dữ liệu thô (Sắp xếp mới nhất)
        List<Booking> bookings = bookingRepo.findAll(Sort.by(Sort.Direction.DESC, "bookingDate", "startTime"));

        // 2. Logic Lọc dữ liệu (Filtering)
        if (date != null) {
            bookings = bookings.stream().filter(b -> b.getBookingDate().equals(date)).collect(Collectors.toList());
        }
        if (!"ALL".equals(status)) {
            bookings = bookings.stream().filter(b -> b.getStatus().equalsIgnoreCase(status)).collect(Collectors.toList());
        }
        if (!keyword.isEmpty()) {
            String k = keyword.toLowerCase();
            bookings = bookings.stream().filter(b -> 
                (b.getCustomer() != null && b.getCustomer().getFullName().toLowerCase().contains(k)) ||
                (b.getCustomer() != null && b.getCustomer().getPhone().contains(k)) ||
                (" #" + b.getBookingId()).contains(k)
            ).collect(Collectors.toList());
        }

        // 3. Lấy danh sách Nhân viên (Bác sĩ/Groomer) -> Để hiện trong Modal gán việc
        List<UserAccount> staffList = userService.searchStaff("ALL", "ACTIVE", "");

        addCommonAttributes(model, principal, "bookings");
        model.addAttribute("pageTitle", "Quản lý Lịch hẹn");
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("staffList", staffList);
        
        // Truyền lại params bộ lọc
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);

        return "admin/booking-list";
    }

    // --- 2. API CẬP NHẬT ĐẦY ĐỦ (DÙNG CHO MODAL) ---
    @PostMapping("/update")
    public String updateBookingFull(
            @RequestParam("bookingId") Integer bookingId,
            @RequestParam("newDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate,
            @RequestParam("newTime") LocalTime newTime,
            @RequestParam(value = "newStaffId", required = false) Integer newStaffId,
            @RequestParam("newStatus") String newStatus,
            RedirectAttributes ra) {
        try {
            // 1. Cập nhật thông tin (Ngày, Giờ, Nhân viên)
            bookingService.updateBookingDetails(bookingId, newDate, newTime, newStaffId);
            
            // 2. Cập nhật trạng thái
            Booking booking = bookingRepo.findById(bookingId).orElseThrow();
            if (!booking.getStatus().equals(newStatus)) {
                booking.setStatus(newStatus);
                // Nếu hoàn thành -> set Payment PAID (Tùy chỉnh theo nghiệp vụ)
                if ("COMPLETED".equals(newStatus)) {
                    booking.setPaymentStatus("PAID");
                }
                bookingRepo.save(booking);
            }

            ra.addFlashAttribute("message", "Cập nhật đơn hàng #" + bookingId + " thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/bookings/list";
    }
}