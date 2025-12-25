    package g6shenpcare.controller.support;


    import g6shenpcare.entity.CustomerProfile;
    import g6shenpcare.entity.Pets;
    import g6shenpcare.entity.UserAccount;
    import g6shenpcare.models.dto.*;
    import g6shenpcare.models.entity.PetMedicalRecord;
    import g6shenpcare.models.entity.Prescription;
    import g6shenpcare.models.entity.PrescriptionItem;
    import g6shenpcare.repository.CustomerProfileRepository;
    import g6shenpcare.repository.PetMedicalRecordRepository;
    import g6shenpcare.repository.PetRepository;
    import g6shenpcare.repository.UserAccountRepository;
    import g6shenpcare.service.LookupService;
    import org.springframework.http.ResponseEntity;
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

    import java.util.Collections;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.stream.Collectors;

    @Controller
    @RequestMapping("/support/lookup")
    public class LookupController {


        private final LookupService lookupService;

        private final UserAccountRepository userRepository;

        private final PetRepository petsRepository;
        private final PetMedicalRecordRepository medicalRecordRepository;
        private final CustomerProfileRepository customerProfileRepository;


        public LookupController(LookupService lookupService, UserAccountRepository userRepository, PetRepository petsRepository, PetMedicalRecordRepository medicalRecordRepository, CustomerProfileRepository customerProfileRepository) {
            this.lookupService = lookupService;
            this.userRepository = userRepository;
            this.petsRepository = petsRepository;
            this.medicalRecordRepository = medicalRecordRepository;
            this.customerProfileRepository = customerProfileRepository;
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
                @AuthenticationPrincipal UserDetails userDetails,
                Model model,
                RedirectAttributes redirectAttributes) {

            if (keyword == null || keyword.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng nhập từ khóa tìm kiếm");
                return "redirect:/support/lookup";
            }

            try {
                LookupResultDTO result = lookupService.search(searchType, keyword.trim());


                UserAccount staff = userRepository.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                model.addAttribute("staff", staff);


                model.addAttribute("customer", result.getCustomer());
                model.addAttribute("pets", result.getPets());
                model.addAttribute("medicalRecords", result.getMedicalRecords());
                model.addAttribute("bookings", result.getBookings());
                model.addAttribute("orders", result.getOrders());
                model.addAttribute("supportTickets", result.getSupportTickets());
                model.addAttribute("searchType", searchType);
                model.addAttribute("keyword", keyword);

                return "staff/look-up";

            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
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
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin: " + e.getMessage());
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
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin: " + e.getMessage()
                );
                return "redirect:/support/lookup";
            }
        }


        @GetMapping("/pet/{petId}/medical-history")
        public ResponseEntity<?> getMedicalHistory(@PathVariable Integer petId) {
            try {
                Pets pet = petsRepository.findById(petId)
                        .orElseThrow(() -> new RuntimeException("Pet not found"));

                CustomerProfile customer = customerProfileRepository.findById(pet.getCustomerId())
                        .orElse(null);

                List<PetMedicalRecord> records = medicalRecordRepository.findByPet_PetIdOrderByVisitDateDesc(petId);

                Map<String, Object> response = new HashMap<>();
                response.put("petName", pet.getName());
                response.put("petInfo", buildPetInfo(pet));
                response.put("customerInfo", customer != null ? buildCustomerInfo(customer) : null);
                response.put("records", records.stream()
                        .map(this::buildRecordDto)
                        .collect(Collectors.toList()));

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", e.getMessage());
                return ResponseEntity.status(500).body(error);
            }
        }

        private Map<String, Object> buildPetInfo(Pets pet) {
            Map<String, Object> info = new HashMap<>();
            info.put("petId", pet.getPetId());
            info.put("name", pet.getName());
            info.put("species", pet.getSpecies());
            info.put("breed", pet.getBreed());
            info.put("gender", pet.getGender());
            info.put("birthDate", pet.getBirthDate() != null ? pet.getBirthDate().toString() : null);
            info.put("color", pet.getColor());
            info.put("weightKg", pet.getWeightKg());
            return info;
        }

        private Map<String, Object> buildCustomerInfo(CustomerProfile customer) {
            Map<String, Object> info = new HashMap<>();
            info.put("customerId", customer.getCustomerId());
            info.put("fullName", customer.getFullName());
            info.put("phone", customer.getPhone());
            info.put("email", customer.getEmail());
            return info;
        }

        private Map<String, Object> buildRecordDto(PetMedicalRecord record) {
            Map<String, Object> dto = new HashMap<>();
            dto.put("recordId", record.getRecordId());
            dto.put("visitDate", record.getVisitDate().toString());
            dto.put("doctorName", record.getDoctor() != null ? record.getDoctor().getUser().getFullName() : "Unknown");
            dto.put("examType", record.getExamType());
            dto.put("symptoms", record.getSymptoms());
            dto.put("physicalExamFindings", record.getPhysicalExamFindings());
            dto.put("diagnosis", record.getDiagnosis());
            dto.put("finalDiagnosis", record.getFinalDiagnosis());
            dto.put("treatment", record.getTreatment());
            dto.put("weightKg", record.getWeightKg());
            dto.put("temperature", record.getTemperature());
            dto.put("heartRate", record.getHeartRate());
            dto.put("mucousMembrane", record.getMucousMembrane());
            dto.put("notes", record.getNotes());
            dto.put("nextVisitDate", record.getNextVisitDate() != null ? record.getNextVisitDate().toString() : null);

            // Handle prescriptions
            if (record.getPrescriptions() != null) {
                dto.put("prescriptions", record.getPrescriptions().stream()
                        .map(this::buildPrescriptionDto)
                        .collect(Collectors.toList()));
            } else {
                dto.put("prescriptions", Collections.emptyList());
            }

            return dto;
        }

        private Map<String, Object> buildPrescriptionDto(Prescription prescription) {
            Map<String, Object> dto = new HashMap<>();
            dto.put("prescriptionId", prescription.getPrescriptionId());
            dto.put("doctorName", prescription.getDoctor() != null ?
                    prescription.getDoctor().getUser().getFullName() : "Unknown");
            dto.put("notes", prescription.getNotes());
            dto.put("createdAt", prescription.getCreatedAt().toString());

            // Map prescription items
            if (prescription.getItems() != null) {
                dto.put("items", prescription.getItems().stream()
                        .map(this::buildPrescriptionItemDto)
                        .collect(Collectors.toList()));
            } else {
                dto.put("items", Collections.emptyList());
            }

            return dto;
        }

        private Map<String, Object> buildPrescriptionItemDto(PrescriptionItem item) {
            Map<String, Object> dto = new HashMap<>();
            dto.put("medicineName", item.getMedicineName());
            dto.put("dosage", item.getDosage());
            dto.put("frequency", item.getFrequency());
            dto.put("duration", item.getDurationDays() != null ?
                    item.getDurationDays() + " ngày" : null);
            dto.put("instructions", item.getInstruction() != null ?
                    item.getInstruction() : item.getNotes());
            return dto;
        }

    }
