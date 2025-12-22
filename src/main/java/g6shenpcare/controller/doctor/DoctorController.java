package g6shenpcare.controller.doctor;

import g6shenpcare.dto.ExamSubmissionDTO;
import g6shenpcare.entity.Booking;
import g6shenpcare.entity.UserAccount;
import g6shenpcare.repository.BookingRepository;
import g6shenpcare.repository.ProductRepository;
import g6shenpcare.repository.UserAccountRepository;
import g6shenpcare.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired private MedicalRecordService medicalService;
    @Autowired private ProductRepository productRepo;
    @Autowired private BookingRepository bookingRepo;
    @Autowired private UserAccountRepository userRepo;

    // Helper: Lấy thông tin Bác sĩ đang đăng nhập
    private UserAccount getLoggedInDoctor(Principal principal) {
        if (principal == null) return null;
        return userRepo.findByUsername(principal.getName()).orElse(null);
    }

    // =================================================================
    // 1. DASHBOARD - DANH SÁCH BỆNH NHÂN CẦN KHÁM
    // =================================================================
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        UserAccount doctor = getLoggedInDoctor(principal);
        if (doctor == null) return "redirect:/login";

        // Logic: Lấy các Booking ĐÃ ĐƯỢC PHÂN CÔNG cho bác sĩ này
        // Và trạng thái là CONFIRMED (Đã đến/Đang chờ) hoặc IN_PROGRESS (Đang khám)
        // AdminBookingController khi check-in khách sẽ chuyển status sang CONFIRMED
        List<Booking> myQueue = bookingRepo.findByAssignedStaffIdAndStatusIn(
                doctor.getUserId(), 
                List.of("CONFIRMED", "IN_PROGRESS")
        );

        model.addAttribute("queue", myQueue);
        model.addAttribute("doctorName", doctor.getFullName());
        model.addAttribute("waitingCount", myQueue.size());

        return "doctor/dashboard";
    }

    // =================================================================
    // 2. MÀN HÌNH KHÁM BỆNH (EXAM FORM)
    // =================================================================
    @GetMapping("/exam/{bookingId}")
    public String showExamForm(@PathVariable Integer bookingId, 
                               Model model, 
                               Principal principal,
                               RedirectAttributes ra) {
        
        UserAccount doctor = getLoggedInDoctor(principal);
        Booking booking = bookingRepo.findById(bookingId).orElseThrow();

        // Validate: Có đúng là bác sĩ được phân công không?
        if (booking.getAssignedStaffId() != null && 
            !booking.getAssignedStaffId().equals(doctor.getUserId())) {
            ra.addFlashAttribute("error", "Bạn không được phân công ca khám này!");
            return "redirect:/doctor/dashboard";
        }

        // Đổi trạng thái sang IN_PROGRESS (Đang khám) nếu mới mở
        if ("CONFIRMED".equals(booking.getStatus())) {
            booking.setStatus("IN_PROGRESS");
            bookingRepo.save(booking);
        }

        // Đẩy dữ liệu sang View
        model.addAttribute("booking", booking);
        model.addAttribute("pet", booking.getPet());
        model.addAttribute("customer", booking.getCustomer());
        
        // Lấy danh sách thuốc (Active) để hiện trong Dropdown kê đơn
        model.addAttribute("productList", productRepo.findByIsActiveTrue());

        return "doctor/exam_form";
    }

    // =================================================================
    // 3. LƯU BỆNH ÁN & KẾT THÚC
    // =================================================================
    @PostMapping("/exam/save")
    public String saveExam(@ModelAttribute ExamSubmissionDTO dto, 
                           Principal principal,
                           RedirectAttributes ra) {
        try {
            UserAccount doctor = getLoggedInDoctor(principal);
            
            // Gọi Service để lưu bệnh án + Trừ kho thuốc
            medicalService.saveExamination(dto, Long.valueOf(doctor.getUserId()));

            // Cập nhật trạng thái Booking -> COMPLETED
            // Lúc này Admin sẽ thấy để thu tiền
            Booking booking = bookingRepo.findById(Integer.parseInt(dto.getBookingId())).orElseThrow();
            booking.setStatus("COMPLETED");
            booking.setPaymentStatus("PENDING_PAYMENT"); // Chờ thanh toán tại quầy
            bookingRepo.save(booking);

            ra.addFlashAttribute("message", "Đã hoàn thành ca khám cho bé " + booking.getPet().getName());
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi khi lưu bệnh án: " + e.getMessage());
            return "redirect:/doctor/exam/" + dto.getBookingId();
        }
        return "redirect:/doctor/dashboard";
    }
}