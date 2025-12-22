package g6shenpcare.controller.client;

import g6shenpcare.entity.Booking; // Đảm bảo đã import Booking
import g6shenpcare.repository.BookingRepository; // Cần thêm import này
import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.entity.Pets;
import g6shenpcare.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/my-account")
public class ClientController {

    @Autowired
    private ClientService clientService;

    // Helper để check login
    private boolean isAuthenticated(Principal principal) {
        return principal != null;
    }
    @Autowired
    private BookingRepository bookingRepo;

    // ================== 1. PROFILE (HỒ SƠ) ==================
    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        if (!isAuthenticated(principal)) {
            return "redirect:/login";
        }

        // Service tự động tìm hoặc tạo profile nếu chưa có -> Không lo null
        CustomerProfile profile = clientService.getProfileByUsername(principal.getName());
        model.addAttribute("profile", profile);

        return "client/dashboard/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute CustomerProfile formInput,
            Principal principal,
            RedirectAttributes ra) {
        if (!isAuthenticated(principal)) {
            return "redirect:/login";
        }

        CustomerProfile currentProfile = clientService.getProfileByUsername(principal.getName());

        // Gọi Service để update (Logic nằm bên Service cho gọn)
        clientService.updateProfile(currentProfile, formInput);

        ra.addFlashAttribute("message", "Cập nhật hồ sơ thành công!");
        return "redirect:/my-account/profile";
    }

    // ================== 2. PETS (THÚ CƯNG) ==================
    @GetMapping("/pets")
    public String showPets(Model model, Principal principal) {
        if (!isAuthenticated(principal)) {
            return "redirect:/login";
        }

        CustomerProfile profile = clientService.getProfileByUsername(principal.getName());

        model.addAttribute("pets", clientService.getPetsByCustomer(profile));
        model.addAttribute("newPet", new Pets()); // Form thêm mới

        return "client/dashboard/pets";
    }

    @PostMapping("/pets/add")
    public String addPet(@ModelAttribute("newPet") Pets pet,
            @RequestParam("avatarFile") MultipartFile file,
            Principal principal,
            RedirectAttributes ra) {
        if (!isAuthenticated(principal)) {
            return "redirect:/login";
        }

        try {
            CustomerProfile profile = clientService.getProfileByUsername(principal.getName());

            // Logic thêm pet chuẩn (đã fix lỗi lưu ảo) nằm trong Service
            clientService.addNewPet(profile, pet, file);

            ra.addFlashAttribute("message", "Thêm thú cưng thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/my-account/pets";
    }

    // ================== 3. HISTORY (LỊCH SỬ) ==================
    @GetMapping("/history")
    public String showHistory(Model model, Principal principal) {
        if (!isAuthenticated(principal)) {
            return "redirect:/login";
        }

        CustomerProfile profile = clientService.getProfileByUsername(principal.getName());

        model.addAttribute("bookings", clientService.getBookingHistory(profile));

        return "client/dashboard/history";
    }

    // === THÊM ĐOẠN CODE NÀY VÀO CUỐI ===
    @PostMapping("/booking/cancel")
    public String cancelBooking(@RequestParam("bookingId") Integer bookingId,
            Principal principal,
            RedirectAttributes ra) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            // 1. Tìm booking
            Booking booking = bookingRepo.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Đơn đặt lịch không tồn tại"));

            // 2. Bảo mật: Kiểm tra xem Booking này có đúng là của khách hàng đang đăng nhập không?
            // (Tránh trường hợp hacker đoán ID để hủy đơn của người khác)
            CustomerProfile profile = clientService.getProfileByUsername(principal.getName());
            if (!booking.getCustomerId().equals(profile.getCustomerId())) {
                ra.addFlashAttribute("error", "Bạn không có quyền hủy đơn này!");
                return "redirect:/my-account/history";
            }

            // 3. Kiểm tra trạng thái hợp lệ
            if ("COMPLETED".equals(booking.getStatus()) || "IN_PROGRESS".equals(booking.getStatus())) {
                ra.addFlashAttribute("error", "Không thể hủy đơn đã hoàn thành hoặc đang thực hiện!");
                return "redirect:/my-account/history";
            }

            // 4. Thực hiện hủy
            booking.setStatus("CANCELLED");
            bookingRepo.save(booking);

            ra.addFlashAttribute("message", "Đã hủy lịch hẹn thành công.");

        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/my-account/history";
    }
}
