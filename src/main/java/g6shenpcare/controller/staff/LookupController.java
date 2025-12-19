    package g6shenpcare.controller.staff;


    import g6shenpcare.entity.UserAccount;
    import g6shenpcare.models.dto.BookingDetailDTO;
    import g6shenpcare.models.dto.LookupResultDTO;
    import g6shenpcare.models.dto.OrderDetailDTO;
    import g6shenpcare.models.dto.TicketDetailDTO;
    import g6shenpcare.repository.UserAccountRepository;
    import g6shenpcare.service.LookupService;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;

    @Controller
    @RequestMapping("/support/lookup")
    public class LookupController {


        private final LookupService lookupService;

        private final UserAccountRepository userRepository;


        public LookupController(LookupService lookupService, UserAccountRepository userRepository) {
            this.lookupService = lookupService;
            this.userRepository = userRepository;
        }


        @GetMapping
        public String showLookupPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
            UserAccount staff = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));


                model.addAttribute("staff", staff);
            return "staff/look-up";
        }


        @GetMapping("/search")
        public String search(
                @RequestParam("searchType") String searchType,
                @RequestParam("keyword") String keyword,
                Authentication auth,
                Model model,
                RedirectAttributes redirectAttributes) {

            if (keyword == null || keyword.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng nhập từ khóa tìm kiếm");
                return "redirect:/staff/look-up";
            }

            try {
                LookupResultDTO result = lookupService.search(searchType, keyword.trim());

                if (auth != null) {
                    model.addAttribute("staffName", auth.getName());
                }

                model.addAttribute("customer", result.getCustomer());
                model.addAttribute("pets", result.getPets());
//                model.addAttribute("medicalRecords", result.getMedicalRecords());
                model.addAttribute("bookings", result.getBookings());
                model.addAttribute("orders", result.getOrders());
                model.addAttribute("supportTickets", result.getSupportTickets());

                // Add search info
                model.addAttribute("searchType", searchType);
                model.addAttribute("keyword", keyword);

                return "staff/look-up";

            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", STR."Có lỗi xảy ra: \{e.getMessage()}");
                return "redirect:/support/lookup";
            }
        }

//        @GetMapping("/pet/{petId}/medical-history")
//        public String viewMedicalHistory(
//                @PathVariable Integer petId,
//                Authentication auth,
//                Model model,
//                RedirectAttributes redirectAttributes) {
//
//            try {
//                PetMedicalHistoryDTO history = lookupService.getPetMedicalHistory(petId);
//
//                if (auth != null) {
//                    model.addAttribute("staffName", auth.getName());
//                }
//
//                model.addAttribute("pet", history.getPet());
//                model.addAttribute("customer", history.getCustomer());
//                model.addAttribute("medicalRecords", history.getMedicalRecords());
//
//                return "staff/pet-medical-history";
//
//            } catch (Exception e) {
//                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin: " + e.getMessage());
//                return "redirect:/support/lookup";
//            }
//        }

        @GetMapping("/booking/{bookingId}")
        public String viewBookingDetail(
                @PathVariable Integer bookingId,
                Authentication auth,
                Model model,
                RedirectAttributes redirectAttributes) {

            try {
                BookingDetailDTO detail = lookupService.getBookingDetail(bookingId);

                if (auth != null) {
                    model.addAttribute("staffName", auth.getName());
                }

                model.addAttribute("booking", detail.getBooking());
                model.addAttribute("customer", detail.getCustomer());
                model.addAttribute("pet", detail.getPet());
//                model.addAttribute("medicalRecord", detail.getMedicalRecord());

                return "staff/booking-detail";

            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin: " + e.getMessage());
                return "redirect:/support/lookup";
            }
        }

        @GetMapping("/order/{orderId}")
        public String viewOrderDetail(
                @PathVariable Integer orderId,
                Authentication auth,
                Model model,
                RedirectAttributes redirectAttributes) {

            try {
                OrderDetailDTO detail = lookupService.getOrderDetail(Long.valueOf(orderId));

                if (auth != null) {
                    model.addAttribute("staffName", auth.getName());
                }

                model.addAttribute("order", detail.getOrder());
                model.addAttribute("customer", detail.getCustomer());
//                model.addAttribute("orderItems", detail.getOrderItems());

                return "staff/order-detail";

            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", STR."Không tìm thấy thông tin: \{e.getMessage()}");
                return "redirect:/support/lookup";
            }
        }

        @GetMapping("/ticket/{ticketId}")
        public String viewTicketDetail(
                @PathVariable Integer ticketId,
                Authentication auth,
                Model model,
                RedirectAttributes redirectAttributes) {

            try {
                TicketDetailDTO detail = lookupService.getTicketDetail(Long.valueOf(ticketId));

                if (auth != null) {
                    model.addAttribute("staffName", auth.getName());
                }

                model.addAttribute("ticket", detail.getTicket());
                model.addAttribute("customer", detail.getCustomer());
//                model.addAttribute("messages", detail.getMessages());

                return "staff/ticket-detail";

            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", STR."Không tìm thấy thông tin: \{e.getMessage()}");
                return "redirect:/support/lookup";
            }
        }


    }
