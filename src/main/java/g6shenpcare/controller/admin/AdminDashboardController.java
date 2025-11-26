package g6shenpcare.controller.admin;

import g6shenpcare.repository.BookingRepository;
import g6shenpcare.repository.CustomerProfileRepository;
import g6shenpcare.repository.OrderRepository;
import g6shenpcare.repository.PetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final CustomerProfileRepository customerRepo;
    private final PetRepository petRepo;
    private final BookingRepository bookingRepo;
    private final OrderRepository orderRepo;

    public AdminDashboardController(CustomerProfileRepository customerRepo,
                                    PetRepository petRepo,
                                    BookingRepository bookingRepo,
                                    OrderRepository orderRepo) {
        this.customerRepo = customerRepo;
        this.petRepo = petRepo;
        this.bookingRepo = bookingRepo;
        this.orderRepo = orderRepo;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {

        long totalCustomers  = customerRepo.countByIsActiveTrue();
        long totalPets       = petRepo.countByIsActiveTrue();
        long pendingBookings = bookingRepo.countByStatusIgnoreCase("PENDING");
        long pendingOrders   = orderRepo.countByStatusIgnoreCase("PENDING");

        String username = (principal != null) ? principal.getName() : "admin";

        model.addAttribute("currentUser", username);
        model.addAttribute("clinicName", "ShenPCare Clinic");
        model.addAttribute("pageTitle", "Dashboard Overview");
        model.addAttribute("activeMenu", "dashboard");

        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalPets", totalPets);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("pendingOrders", pendingOrders);

        return "admin/dashboard";
    }
}
