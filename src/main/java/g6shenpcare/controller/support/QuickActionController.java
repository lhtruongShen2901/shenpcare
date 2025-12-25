package g6shenpcare.controller.support;


import g6shenpcare.entity.UserAccount;
import g6shenpcare.models.dto.*;
import g6shenpcare.repository.ServiceCategoryRepository;
import g6shenpcare.repository.ServicesRepository;
import g6shenpcare.repository.UserAccountRepository;
import g6shenpcare.service.QuickActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/support/api/quick-actions")
@RequiredArgsConstructor
public class QuickActionController {

    private final QuickActionService quickActionService;

    private final ServiceCategoryRepository serviceCategoryRepository;

    private final UserAccountRepository userRepository;


    private final ServicesRepository servicesRepository;



    @GetMapping("/pricing/service-categories")
    @ResponseBody
    public List<ServiceCategoryDTO> getServiceCategories() {
        return serviceCategoryRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(c -> new ServiceCategoryDTO(
                        c.getServiceCategoryId(),
                        c.getCategoryType(),
                        c.getName()
                ))
                .toList();
    }


    @GetMapping("/pricing")
    public ResponseEntity<List<ServicePricingDTO>> getServicePricing(
            @RequestParam(required = false) Integer petId) {

        List<ServicePricingDTO> pricing = quickActionService.getServicePricingForCustomer(petId);
        return ResponseEntity.ok(pricing);
    }

    @PostMapping("/pricing/send-to-chat")
    public ResponseEntity<MessageDTO> sendPricingToChat(
            @RequestParam Long sessionId,
            @RequestParam Integer serviceId,
            @RequestParam(required = false) Integer petId) {

        MessageDTO message = quickActionService.sendPricingQuoteToChat(sessionId, serviceId, petId);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/booking/available-slots")
    public ResponseEntity<List<TimeSlotDTO>> getAvailableSlots(
            @RequestParam Integer serviceId,
            @RequestParam LocalDate date,
            @RequestParam(required = false) Integer staffId) {

        List<TimeSlotDTO> slots = quickActionService.getAvailableSlots(serviceId, date, staffId);
        return ResponseEntity.ok(slots);
    }

    @PostMapping("/booking/create")
    public ResponseEntity<BookingDTO> createQuickBooking(@RequestBody QuickBookingRequest request) {
        BookingDTO booking = quickActionService.createQuickBooking(request);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDTO>> getCustomerOrders(@RequestParam Integer customerId) {
        List<OrderDTO> orders = quickActionService.getCustomerOrders(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/schedule")
    public ResponseEntity<Map<String, List<ScheduleSlotDTO>>> getStaffSchedule(
            @RequestParam LocalDate date,
            @RequestParam(required = false) String staffType) {

        Map<String, List<ScheduleSlotDTO>> schedule = quickActionService.getStaffScheduleByDate(date, staffType);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/pets")
    public ResponseEntity<List<PetDetailDTO>> getCustomerPets(@RequestParam Integer customerId) {
        List<PetDetailDTO> pets = quickActionService.getCustomerPetsWithHistory(customerId);
        return ResponseEntity.ok(pets);
    }


    @PostMapping("/notes")
    public ResponseEntity<CustomerNoteDTO> addCustomerNote(@AuthenticationPrincipal UserDetails userDetails, @RequestBody CustomerNoteRequest request) {
        UserAccount staff = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        CustomerNoteDTO note = quickActionService.addNote(request,staff);
        return ResponseEntity.ok(note);
    }

    @GetMapping("/notes")
    public ResponseEntity<List<CustomerNoteDTO>> getCustomerNotes(@RequestParam Integer customerId) {
        List<CustomerNoteDTO> notes = quickActionService.getCustomerNotes(customerId);
        return ResponseEntity.ok(notes);
    }


    @GetMapping("/staff-roles")
    public ResponseEntity<List<String>> getStaffRoles() {
        return ResponseEntity.ok(userRepository.findDistinctActiveRoles());
    }


    @GetMapping("/all")
    public ResponseEntity<List<BookingServiceDTO>> getAllBookingServices() {
        return ResponseEntity.ok(
                servicesRepository.findAllActiveForBooking()
        );
    }

}