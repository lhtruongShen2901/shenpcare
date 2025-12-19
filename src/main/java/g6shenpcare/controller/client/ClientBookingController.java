package g6shenpcare.controller.client;

import g6shenpcare.entity.Booking;
import g6shenpcare.entity.CustomerProfile; // [MỚI]
import g6shenpcare.entity.Pets;
import g6shenpcare.entity.UserAccount;
import g6shenpcare.repository.CustomerProfileRepository; // [MỚI]
import g6shenpcare.repository.PetRepository;
import g6shenpcare.repository.ServicesRepository;
import g6shenpcare.service.BookingService;
import g6shenpcare.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.ArrayList; // Để xử lý trường hợp khách chưa có Profile

@Controller
@RequestMapping("/booking")
public class ClientBookingController {

    private final BookingService bookingService;
    private final ServicesRepository servicesRepository;
    private final UserService userService;
    private final PetRepository petRepository;
    private final CustomerProfileRepository customerProfileRepository; // [Inject thêm cái này]

    public ClientBookingController(BookingService bookingService, 
                                   ServicesRepository servicesRepository,
                                   UserService userService,
                                   PetRepository petRepository,
                                   CustomerProfileRepository customerProfileRepository) {
        this.bookingService = bookingService;
        this.servicesRepository = servicesRepository;
        this.userService = userService;
        this.petRepository = petRepository;
        this.customerProfileRepository = customerProfileRepository;
    }

    @GetMapping
    public String showBookingForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login"; 
        }

        // 1. Lấy User từ Login
        UserAccount user = userService.getUserByUsername(principal.getName());
        
        // 2. [LOGIC MỚI] Từ User -> Tìm CustomerProfile (Thông tin khách hàng)
        CustomerProfile customer = customerProfileRepository.findByUserId(user.getUserId())
                .orElse(null);

        // Chuẩn bị danh sách Pet
        List<Pets> myPets = new ArrayList<>();
        Integer customerId = null;

        if (customer != null) {
            customerId = customer.getCustomerId();
            // 3. Nếu là Khách hàng đã có hồ sơ -> Lấy danh sách Pet của họ
            myPets = petRepository.findByCustomerId(customerId);
        } else {
            // Trường hợp: User mới tạo tài khoản, chưa cập nhật hồ sơ (Chưa có CustomerId)
            // -> Có thể redirect họ sang trang cập nhật hồ sơ hoặc để trống
        }

        model.addAttribute("myPets", myPets);
        model.addAttribute("services", servicesRepository.findByServiceTypeNot("COMBO"));
        
        Booking booking = new Booking();
        // Gán CustomerId vào form (nếu có)
        if (customerId != null) {
            booking.setCustomerId(customerId);
        }
        
        model.addAttribute("booking", booking);
        return "client/booking-form";
    }

    @PostMapping("/submit")
    public String submitBooking(@ModelAttribute Booking booking, RedirectAttributes ra) {
        try {
            // Kiểm tra nếu khách chưa có CustomerId (chưa có profile)
            if (booking.getCustomerId() == null) {
                ra.addFlashAttribute("error", "Vui lòng cập nhật hồ sơ cá nhân trước khi đặt lịch!");
                return "redirect:/profile"; // Ví dụ đường dẫn
            }

            bookingService.createClientBooking(booking);
            ra.addFlashAttribute("message", "Gửi yêu cầu thành công! Nhân viên sẽ gọi xác nhận sớm.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/booking";
    }
}