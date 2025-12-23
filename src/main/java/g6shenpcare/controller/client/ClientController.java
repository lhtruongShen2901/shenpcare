package g6shenpcare.controller.client;

import g6shenpcare.entity.Booking;
import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.entity.Order;
import g6shenpcare.entity.Pets;
import g6shenpcare.repository.BookingRepository;
import g6shenpcare.repository.OrderRepository;
import g6shenpcare.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/my-account")
public class ClientController {

    @Autowired
    private ClientService clientService;
    @Autowired
    private BookingRepository bookingRepo;
    @Autowired
    private OrderRepository orderRepo;

    // Helper check login
    private boolean isAuthenticated(Principal principal) {
        return principal != null;
    }

    // ================== 1. PROFILE (HỒ SƠ) ==================
    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        if (!isAuthenticated(principal)) {
            return "redirect:/login";
        }
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
        clientService.updateProfile(currentProfile, formInput);
        ra.addFlashAttribute("message", "Cập nhật hồ sơ thành công!");
        return "redirect:/my-account/profile";
    }

    // ================== 2. PETS (THÚ CƯNG) - ĐÃ NÂNG CẤP ==================
    @GetMapping("/pets")
    public String showPets(Model model, Principal principal) {
        if (!isAuthenticated(principal)) {
            return "redirect:/login";
        }
        CustomerProfile profile = clientService.getProfileByUsername(principal.getName());
        model.addAttribute("pets", clientService.getPetsByCustomer(profile));
        return "client/dashboard/pets"; // File HTML mới
    }

    // [CẬP NHẬT] Dùng chung 1 hàm Save cho cả Thêm mới và Sửa
    // HTML form action: /my-account/pets/save
    @PostMapping("/pets/save")
    public String savePet(@ModelAttribute Pets pet,
            @RequestParam("avatarFile") MultipartFile file,
            Principal principal,
            RedirectAttributes ra) {
        if (!isAuthenticated(principal)) {
            return "redirect:/login";
        }

        try {
            // Hàm này bên Service sẽ tự kiểm tra:
            // - Nếu pet.getPetId() == null -> Tạo mới
            // - Nếu có ID -> Cập nhật thông tin
            clientService.savePetForUser(pet, file, principal.getName());

            String msg = (pet.getPetId() == null) ? "Thêm thú cưng thành công!" : "Cập nhật thông tin thành công!";
            ra.addFlashAttribute("message", msg);
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/my-account/pets";
    }

    // [MỚI] Hàm xóa thú cưng
    // HTML form action: /my-account/pets/delete
    @PostMapping("/pets/delete")
    public String deletePet(@RequestParam("petId") Integer petId,
            Principal principal,
            RedirectAttributes ra) {
        if (!isAuthenticated(principal)) {
            return "redirect:/login";
        }

        try {
            // Cần check xem Pet này có thuộc về User đang đăng nhập không (Bảo mật)
            // Logic này nên nằm trong Service
            clientService.deletePet(petId);
            ra.addFlashAttribute("message", "Đã xóa hồ sơ thú cưng.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa (Có thể do đang có lịch hẹn): " + e.getMessage());
        }
        return "redirect:/my-account/pets";
    }

    // ================== 3. HISTORY (LỊCH SỬ) ==================
    @GetMapping("/history")
    public String showHistory(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        CustomerProfile profile = clientService.getProfileByUsername(principal.getName());

        // 1. Lấy Booking
        model.addAttribute("bookings", clientService.getBookingHistory(profile));

        // 2. Lấy Orders
        List<Order> orders = orderRepo.findByCustomerIdOrderByOrderDateDesc(Long.valueOf(profile.getCustomerId()));
        model.addAttribute("orders", orders);

        return "client/dashboard/history";
    }

    @PostMapping("/booking/cancel")
    public String cancelBooking(@RequestParam("bookingId") Integer bookingId,
            Principal principal,
            RedirectAttributes ra) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            Booking booking = bookingRepo.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Đơn đặt lịch không tồn tại"));

            CustomerProfile profile = clientService.getProfileByUsername(principal.getName());
            if (!booking.getCustomerId().equals(profile.getCustomerId())) {
                ra.addFlashAttribute("error", "Bạn không có quyền hủy đơn này!");
                return "redirect:/my-account/history";
            }

            if ("COMPLETED".equals(booking.getStatus()) || "IN_PROGRESS".equals(booking.getStatus())) {
                ra.addFlashAttribute("error", "Không thể hủy đơn đã hoàn thành hoặc đang thực hiện!");
                return "redirect:/my-account/history";
            }

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
