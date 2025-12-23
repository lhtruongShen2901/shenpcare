package g6shenpcare.controller.client;

import g6shenpcare.dto.BookingRequestDTO;
import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.entity.Pets;
import g6shenpcare.repository.PetRepository;
import g6shenpcare.repository.ServicesRepository;
import g6shenpcare.service.BookingService;
import g6shenpcare.service.ClientService;
import g6shenpcare.service.EmailService; // [NEW] Import EmailService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/booking")
public class ClientBookingController {

    @Autowired private BookingService bookingService;
    @Autowired private ServicesRepository servicesRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private ClientService clientService;
    
    @Autowired // Tiêm EmailService
    private EmailService emailService;

    @GetMapping
    public String showBookingForm(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        CustomerProfile customer = clientService.getProfileByUsername(principal.getName());

        BookingRequestDTO bookingDTO = new BookingRequestDTO();
        bookingDTO.setCustomerName(customer.getFullName());
        bookingDTO.setCustomerPhone(customer.getPhone());
        bookingDTO.setCustomerEmail(customer.getEmail());
        bookingDTO.setCustomerId(Long.valueOf(customer.getCustomerId()));
        bookingDTO.setCustomerAddress(customer.getAddressLine());

        model.addAttribute("booking", bookingDTO);
        model.addAttribute("myPets", clientService.getPetsByCustomer(customer));
        model.addAttribute("services", servicesRepository.findAll());

        return "client/booking";
    }

    @PostMapping("/submit")
    public String submitBooking(@ModelAttribute BookingRequestDTO dto,
                                Principal principal,
                                RedirectAttributes ra) {
        try {
            if (principal == null) return "redirect:/login";

            CustomerProfile customer = clientService.getProfileByUsername(principal.getName());
            dto.setCustomerId(Long.valueOf(customer.getCustomerId()));

            boolean hasBooking = false;
            // Biến lưu thời gian để gửi mail (lấy từ vòng lặp cuối hoặc dto gốc)
            String bookingTimeInfo = dto.getBookingDate() + " " + dto.getTimeSlot();

            // TRƯỜNG HỢP 1: Chọn Pet có sẵn
            if (dto.getSelectedPetIds() != null && !dto.getSelectedPetIds().isEmpty()) {
                for (Long petId : dto.getSelectedPetIds()) {
                    BookingRequestDTO petBooking = copyBookingInfo(dto);
                    petBooking.setPetId(petId);
                    bookingService.createClientBooking(petBooking);
                }
                hasBooking = true;
            }

            // TRƯỜNG HỢP 2: Tạo Pet mới
            if (dto.getPetName() != null && !dto.getPetName().trim().isEmpty()) {
                Pets newPet = new Pets();
                newPet.setCustomerId(customer.getCustomerId());
                newPet.setOwnerId(customer.getUserId());
                newPet.setName(dto.getPetName());
                newPet.setSpecies(dto.getPetSpecies());
                newPet.setBreed(dto.getPetBreed());
                if (dto.getPetAge() != null) {
                    newPet.setBirthDate(LocalDate.now().minusYears(dto.getPetAge()));
                }
                newPet.setWeight(dto.getPetWeight());
                newPet.setPetCode("P-BOOK-" + System.currentTimeMillis());
                newPet.setActive(true);
                newPet.setCreatedAt(LocalDateTime.now());
                
                Pets savedPet = petRepository.save(newPet);

                BookingRequestDTO newPetBooking = copyBookingInfo(dto);
                newPetBooking.setPetId(Long.valueOf(savedPet.getPetId()));
                bookingService.createClientBooking(newPetBooking);
                hasBooking = true;
            }

            // TRƯỜNG HỢP 3: Không chọn Pet
            if (!hasBooking) {
                BookingRequestDTO noPetBooking = copyBookingInfo(dto);
                noPetBooking.setPetId(null);
                bookingService.createClientBooking(noPetBooking);
            }

            // [NEW] Gửi Email Xác Nhận (Chạy ngầm)
            try {
                if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                    emailService.sendBookingConfirmation(
                        customer.getEmail(),
                        customer.getFullName(),
                        bookingTimeInfo,
                        "Dịch vụ tại ShenPCare" // Có thể query tên dịch vụ nếu cần
                    );
                }
            } catch (Exception ex) {
                System.err.println("Không gửi được mail xác nhận: " + ex.getMessage());
            }

            ra.addFlashAttribute("message", "Gửi yêu cầu thành công! Chúng tôi sẽ liên hệ sớm.");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/booking";
    }

    private BookingRequestDTO copyBookingInfo(BookingRequestDTO original) {
        BookingRequestDTO copy = new BookingRequestDTO();
        copy.setCustomerId(original.getCustomerId());
        copy.setServiceId(original.getServiceId());
        copy.setBookingDate(original.getBookingDate());
        copy.setTimeSlot(original.getTimeSlot());
        copy.setNotes(original.getNotes());
        copy.setIsUrgent(original.getIsUrgent());
        return copy;
    }
}