package g6shenpcare.service;


import g6shenpcare.entity.*;
import g6shenpcare.models.dto.*;
import g6shenpcare.models.entity.*;
import g6shenpcare.models.mapper.MessageMapper;
import g6shenpcare.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuickActionService {



    private final SimpMessagingTemplate messagingTemplate;


    private final MessageMapper messageMapper;


    private final ServicesRepository servicesRepository;
    private final ServicePricingMatrixRepository pricingMatrixRepository;
    private final PetRepository petRepository;
    private final ChatSessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final UserAccountRepository userRepository;
    private final BookingRepository bookingRepository;
    private final StaffWorkingScheduleRepository scheduleRepository;
    private final CustomerProfileRepository customerRepository;
    private final StaffProfileRepository staffRepository;
    private final  OrderRepository orderRepository;
    private final PetMedicalRecordRepository medicalRecordRepository;
    private final CustomerNoteRepository noteRepository;



    public   List<OrderDTO> getCustomerOrders(Integer customerId) {
        List<Order> orders = orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);

        return orders.stream()
                .map(o -> OrderDTO.builder()
                        .orderId(o.getOrderId())
                        .orderDate(o.getOrderDate())
                        .status(o.getStatus())
                        .totalAmount(o.getTotalAmount())
                        .itemCount(o.getItems() != null ? o.getItems().size() : 0)
                        .shippingAddress(o.getShippingAddress())
                        .build())
                .collect(Collectors.toList());
    }

    public List<PetDetailDTO> getCustomerPets(Integer customerId) {
        List<Pets> pets = petRepository.findByCustomerIdAndActiveTrue(customerId);

        return pets.stream()
                .map(this::mapToPetDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thú cưng của khách hàng kèm history (cho modal detail)
     */
    public List<PetDetailDTO> getCustomerPetsWithHistory(Integer customerId) {
        List<Pets> pets = petRepository.findByCustomerIdAndActiveTrue(customerId);

        return pets.stream()
                .map(pet -> {
                    PetDetailDTO dto = mapToPetDetailDTO(pet);

                    // Add extra history info
                    List<Booking> recentBookings = bookingRepository
                            .findTop5ByPetIdOrderByBookingDateDesc(pet.getPetId());

                    dto.setRecentBookingCount(recentBookings.size());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy lịch sử dịch vụ của thú cưng
     */
    public PetHistoryDTO getPetServiceHistory(Integer petId) {
        Pets pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        // Service history
        List<Booking> bookings = bookingRepository.findByPetIdOrderByBookingDateDesc(petId);
        List<ServiceHistoryDTO> serviceHistory = bookings.stream()
                .map(b -> ServiceHistoryDTO.builder()
                        .bookingId(b.getBookingId())
                        .bookingDate(b.getBookingDate())
                        .serviceName(b.getService().getName())
                        .status(b.getStatus())
                        .amount(b.getTotalAmount())
                        .notes(b.getNotes())
                        .build())
                .collect(Collectors.toList());

        // Medical history
        List<PetMedicalRecord> records = medicalRecordRepository
                .findByPet_PetIdOrderByVisitDateDesc(petId);
        List<MedicalRecordDTO> medicalHistory = records.stream()
                .map(r -> MedicalRecordDTO.builder()
                        .recordId(r.getRecordId())
                        .visitDate(r.getVisitDate())
                        .doctorName(r.getDoctor() != null ?
                                r.getDoctor().getUser().getFullName() : null)
                        .diagnosis(r.getDiagnosis())
                        .treatment(r.getTreatment())
                        .weight(r.getWeightKg() != null ? r.getWeightKg().floatValue() : null)
                        .temperature(r.getTemperature() != null ?
                                r.getTemperature().floatValue() : null)
                        .build())
                .collect(Collectors.toList());

        return PetHistoryDTO.builder()
                .pet(mapToPetDetailDTO(pet))
                .serviceHistory(serviceHistory)
                .medicalHistory(medicalHistory)
                .build();
    }

    /**
     * Map to DTO
     */
    private PetDetailDTO mapToPetDetailDTO(Pets pet) {
        // Count total visits
        int totalVisits = bookingRepository.countByPetIdAndStatusIn(
                pet.getPetId(),
                Arrays.asList("COMPLETED", "CONFIRMED","IN_PROGRESS")
        );

        // Get last visit
        LocalDate lastVisit = bookingRepository
                .findTopByPetIdOrderByBookingDateDesc(pet.getPetId())
                .map(Booking::getBookingDate)
                .orElse(null);

        return PetDetailDTO.builder()
                .petId(pet.getPetId())
                .name(pet.getName())
                .species(pet.getSpecies())
                .breed(pet.getBreed())
                .gender(pet.getGender())
                .birthDate(pet.getBirthDate())
                .weightKg(pet.getWeightKg())
                .color(pet.getColor())
                .coatLength(pet.getCoatLength())
                .isSterilized(pet.isSterilized())
                .notes(pet.getNotes())
                .totalVisits(totalVisits)
                .lastVisit(lastVisit)
                .build();
    }

    public List<ServicePricingDTO> getServicePricingForCustomer(Integer petId) {
        List<Services> activeServices = servicesRepository.findByActiveTrueOrderBySortOrder();

        Pets pet = null;
        if (petId != null) {
            pet = petRepository.findById(petId).orElse(null);
        }

        List<ServicePricingDTO> result = new ArrayList<>();

        for (Services service : activeServices) {
            ServicePricingDTO dto = buildServicePricingDTO(service, pet);
            result.add(dto);
        }

        return result;
    }

    /**
     * Build DTO giá dịch vụ
     */
    private ServicePricingDTO buildServicePricingDTO(Services service, Pets pet) {
        ServicePricingDTO.ServicePricingDTOBuilder builder = ServicePricingDTO.builder()
                .serviceId(service.getServiceId())
                .serviceName(service.getName())
                .category(service.getServiceCategory() != null ?
                        service.getServiceCategory().getCategoryType() : "")
                .durationMinutes(service.getDurationMinutes())
                .description(service.getDescription())
                .priceModel(service.getPriceModel())
                .discountPercent(service.getDiscountPercent());

        // Nếu là FIXED price
        if ("FIXED".equals(service.getPriceModel())) {
            BigDecimal basePrice = service.getFixedPrice();
            BigDecimal discountedPrice = calculateDiscountedPrice(
                    basePrice,
                    service.getDiscountPercent()
            );

            builder.basePrice(basePrice)
                    .discountedPrice(discountedPrice);
        }
        // Nếu là WEIGHT_BASED
        else if ("WEIGHT_BASED".equals(service.getPriceModel())) {
            List<ServicePricingMatrix> pricingList = pricingMatrixRepository
                    .findByServiceIdOrderByMinWeightAsc(service.getServiceId());

            List<PriceRangeDTO> ranges = pricingList.stream()
                    .map(p -> PriceRangeDTO.builder()
                            .minWeight(p.getMinWeight())
                            .maxWeight(p.getMaxWeight())
                            .coatLength(p.getCoatLength())
                            .price(p.getPrice())
                            .build())
                    .collect(Collectors.toList());

            builder.priceRanges(ranges);

            // Nếu có thông tin pet, tính giá cụ thể
            if (pet != null) {
                BigDecimal specificPrice = calculatePriceForPet(service, pet);
                builder.basePrice(specificPrice)
                        .discountedPrice(calculateDiscountedPrice(
                                specificPrice,
                                service.getDiscountPercent()
                        ));
            }
        }

        return builder.build();
    }

    /**
     * Tính giá cho thú cưng cụ thể
     */
    private BigDecimal calculatePriceForPet(Services service, Pets pet) {
        if (pet.getWeightKg() == null) {
            return BigDecimal.ZERO;
        }

       ServicePricingMatrix pricing = pricingMatrixRepository
                .findApplicablePricing(
                        service.getServiceId(),
                        pet.getSpecies(),
                        pet.getCoatLength(),
                        pet.getWeightKg()
                )
                .stream()
                .findFirst()
                .orElse(null);

        return pricing != null ? pricing.getPrice() : BigDecimal.ZERO;
    }

    /**
     * Tính giá sau discount
     */

    /**
     * Gửi báo giá vào chat
     */
    @Transactional
    public MessageDTO sendPricingQuoteToChat(Long sessionId, Integer serviceId, Integer petId) {

        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Services service = servicesRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        Pets pet = petId != null
                ? petRepository.findById(petId).orElse(null)
                : null;

        // 1️⃣ Build message text
        StringBuilder messageText = new StringBuilder();
        messageText.append("📋 BÁO GIÁ DỊCH VỤ\n\n");
        messageText.append("🔹 Dịch vụ: ").append(service.getName()).append("\n");
        messageText.append("⏱️ Thời gian: ").append(service.getDurationMinutes()).append(" phút\n");

        if (pet != null) {
            messageText.append("🐾 Thú cưng: ").append(pet.getName()).append("\n");
        }

        if ("FIXED".equals(service.getPriceModel())) {
            BigDecimal price = service.getFixedPrice();
            if (service.getDiscountPercent() != null && service.getDiscountPercent() > 0) {
                BigDecimal discountedPrice =
                        calculateDiscountedPrice(price, service.getDiscountPercent());
                messageText.append("💰 Giá: ")
                        .append(formatMoney(discountedPrice))
                        .append(" (Giảm ")
                        .append(service.getDiscountPercent())
                        .append("%)\n");
                messageText.append("~~")
                        .append(formatMoney(price))
                        .append("~~\n");
            } else {
                messageText.append("💰 Giá: ")
                        .append(formatMoney(price))
                        .append("\n");
            }
        } else if ("WEIGHT_BASED".equals(service.getPriceModel())) {
            if (pet != null && pet.getWeightKg() != null) {
                BigDecimal price = calculatePriceForPet(service, pet);
                BigDecimal finalPrice =
                        calculateDiscountedPrice(price, service.getDiscountPercent());
                messageText.append("💰 Giá: ")
                        .append(formatMoney(finalPrice))
                        .append("\n");
                messageText.append("(Dựa trên cân nặng: ")
                        .append(pet.getWeightKg())
                        .append("kg)\n");
            } else {
                messageText.append("💰 Giá theo cân nặng thú cưng\n");
            }
        }

        messageText.append("\n✨ Vui lòng liên hệ để đặt lịch!");

        // 2️⃣ Tạo Message
        Message message = new Message();
        message.setSession(session);
        message.setSender(session.getSupportStaff()); // staff gửi
        message.setMessageText(messageText.toString());
        message.setSentAt(LocalDateTime.now());
        message.setIsRead(false);

        Message saved = messageRepository.save(message);

        // 3️⃣ Map DTO
        MessageDTO dto = messageMapper.toDto(saved);

        // 4️⃣ 🔥 GỬI REALTIME CHO CẢ STAFF & CUSTOMER
        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId + "/messages",
                dto
        );

        // (tuỳ chọn) notify staff list
        if (session.getSupportStaff() != null) {
            Integer staffId = session.getSupportStaff().getUserId();
            messagingTemplate.convertAndSend(
                    "/topic/staff/" + staffId + "/session-messages",
                    dto
            );
        }

        return dto;
    }


    /**
     * Lấy tất cả dịch vụ active
     */
    public List<ServicePricingDTO> getAllActiveServices() {
        return getServicePricingForCustomer( null);
    }


    private MessageDTO mapToMessageDTO(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .messageText(message.getMessageText())
                .sentAt(message.getSentAt())
                .session(new SessionDTO(message.getSession().getId()))
                .sender(new UserDTO(message.getSender().getUserId(),
                        message.getSender().getUsername(),
                        message.getSender().getFullName(),
                        message.getSender().getRole()))
                .build();
    }


    public List<TimeSlotDTO> getAvailableSlots(Integer serviceId, LocalDate date, Integer staffId) {
        Services service = servicesRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday

        // Lấy lịch làm việc
        List<StaffWorkingSchedule> schedules;
        if (staffId != null) {
            schedules = scheduleRepository.findByStaffIdAndDayOfWeekAndActiveTrue(
                    staffId, dayOfWeek);
        } else {
            schedules = scheduleRepository.findByDayOfWeekAndActiveTrue(dayOfWeek);
        }

        List<TimeSlotDTO> slots = new ArrayList<>();

        for (StaffWorkingSchedule schedule : schedules) {
            // Tạo các slot theo duration của service
            LocalTime currentTime = schedule.getStartTime();
            LocalTime endTime = schedule.getEndTime();
            int slotDuration = service.getDurationMinutes();

            while (currentTime.plusMinutes(slotDuration).isBefore(endTime) ||
                    currentTime.plusMinutes(slotDuration).equals(endTime)) {

                LocalTime slotEndTime = currentTime.plusMinutes(slotDuration);

                // Đếm số booking hiện tại trong slot này
                int currentBookings = countBookingsInSlot(
                        date,
                        currentTime,
                        slotEndTime,
                        schedule.getStaffId()
                );

                int maxBookings = schedule.getMaxDailyBookings() != null ?
                        schedule.getMaxDailyBookings() : 5;

                TimeSlotDTO slot = TimeSlotDTO.builder()
                        .startTime(currentTime)
                        .endTime(slotEndTime)
                        .staffId(schedule.getStaffId())
                        .staffName(schedule.getStaff() != null ?
                                schedule.getStaff().getFullName() : null)
                        .currentBookings(currentBookings)
                        .maxBookings(maxBookings)
                        .available(currentBookings < maxBookings)
                        .build();

                slots.add(slot);

                currentTime = slotEndTime;
            }
        }

        return slots.stream()
                .sorted(Comparator.comparing(TimeSlotDTO::getStartTime))
                .collect(Collectors.toList());
    }

    /**
     * Đếm số booking trong slot
     */
    private int countBookingsInSlot(LocalDate date, LocalTime startTime,
                                    LocalTime endTime, Integer staffId) {
        LocalDateTime slotStart = LocalDateTime.of(date, startTime);
        LocalDateTime slotEnd = LocalDateTime.of(date, endTime);

        return bookingRepository.countByBookingDateAndTimeRange(
                date, slotStart, slotEnd, staffId);
    }

    /**
     * Tạo booking nhanh
     */
    public BookingDTO createQuickBooking(QuickBookingRequest request) {
        // Validate
        CustomerProfile customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Pets pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        Services service = servicesRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        // Tính giá
        BigDecimal totalAmount = calculateBookingAmount(service, pet);

        // Tạo booking
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setPet(pet);
        booking.setService(service);
        booking.setBookingDate(request.getBookingDate());

        LocalDateTime startDateTime = LocalDateTime.of(
                request.getBookingDate(),
                request.getStartTime()
        );
        LocalDateTime endDateTime = startDateTime.plusMinutes(service.getDurationMinutes());

        booking.setStartTime(startDateTime);
        booking.setEndTime(endDateTime);
        booking.setStatus("PENDING_CONFIRMATION");
        booking.setPaymentStatus("PENDING");
        booking.setTotalAmount(totalAmount);
        booking.setNotes(request.getNotes());
        booking.setCreatedAt(LocalDateTime.now());

        if (request.getStaffId() != null) {
            UserAccount staff = userRepository.findById(request.getStaffId())
                    .orElse(null);
            booking.setStaff(staff);
        }

        booking = bookingRepository.save(booking);

        // Gửi thông báo vào chat nếu có
        if (request.getSessionId() != null) {
            sendBookingConfirmationToChat(request.getSessionId(), booking);
        }

        return mapToBookingDTO(booking);
    }

    /**
     * Tính tiền booking
     */
    private BigDecimal calculateBookingAmount(Services service, Pets pet) {
        if ("FIXED".equals(service.getPriceModel())) {
            BigDecimal price = service.getFixedPrice();
            return calculateDiscountedPrice(price, service.getDiscountPercent());
        } else if ("WEIGHT_BASED".equals(service.getPriceModel())) {
            // Logic tính theo cân nặng - tương tự ServiceService
            return BigDecimal.valueOf(100000); // Placeholder
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateDiscountedPrice(BigDecimal basePrice, Integer discountPercent) {
        if (discountPercent == null || discountPercent == 0) {
            return basePrice;
        }

        BigDecimal discountAmount = basePrice
                .multiply(BigDecimal.valueOf(discountPercent))
                .divide(BigDecimal.valueOf(100));

        return basePrice.subtract(discountAmount);
    }


    /**
     * Gửi xác nhận booking vào chat
     */
    private void sendBookingConfirmationToChat(Long sessionId, Booking booking) {
        ChatSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null) return;

        String messageText = String.format(
                "✅ ĐẶT LỊCH THÀNH CÔNG\n\n" +
                        "🔖 Mã booking: #%d\n" +
                        "🐾 Thú cưng: %s\n" +
                        "🔹 Dịch vụ: %s\n" +
                        "📅 Ngày: %s\n" +
                        "⏰ Giờ: %s\n" +
                        "💰 Tổng tiền: %s\n\n" +
                        "Chúng tôi sẽ xác nhận lại với bạn sớm nhất!",
                booking.getBookingId(),
                booking.getPet().getName(),
                booking.getService().getName(),
                booking.getBookingDate(),
                booking.getStartTime().toLocalTime(),
                formatMoney(booking.getTotalAmount())
        );

        // 1️⃣ Tạo message
        Message message = new Message();
        message.setSession(session);
        message.setSender(session.getSupportStaff()); // staff gửi
        message.setMessageText(messageText);
        message.setSentAt(LocalDateTime.now());
        message.setIsRead(false);

        Message saved = messageRepository.save(message);

        // 2️⃣ Map DTO
        MessageDTO dto = messageMapper.toDto(saved);

        // 3️⃣ 🔥 GỬI REALTIME CHO CHAT SESSION
        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId + "/messages",
                dto
        );

        // 4️⃣ (Tuỳ chọn) notify staff list
        if (session.getSupportStaff() != null) {
            Integer staffId = session.getSupportStaff().getUserId();
            messagingTemplate.convertAndSend(
                    "/topic/staff/" + staffId + "/session-messages",
                    dto
            );
        }
    }


    /**
     * Map to DTO
     */
    private BookingDTO mapToBookingDTO(Booking booking) {
        return BookingDTO.builder()
                .bookingId(booking.getBookingId())
                .customerId(booking.getCustomer().getCustomerId())
                .customerName(booking.getCustomer().getFullName())
                .petId(booking.getPet().getPetId())
                .petName(booking.getPet().getName())
                .serviceId(booking.getService().getServiceId())
                .serviceName(booking.getService().getName())
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime() != null ?
                        booking.getStartTime().toLocalTime() : null)
                .endTime(booking.getEndTime() != null ?
                        booking.getEndTime().toLocalTime() : null)
                .status(booking.getStatus())
                .assignedStaffId(booking.getAssignedStaffId() != null ?
                        booking.getStaff().getUserId() : null)
                .assignedStaffName(booking.getStaff().getFullName() != null ?
                        booking.getStaff().getFullName() : null)
                .totalAmount(booking.getTotalAmount())
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                .build();
    }
    private String formatMoney(BigDecimal amount) {
        return String.format("%,.0f₫", amount);
    }



    /**
     * Chi tiết đơn hàng
     */
//    public OrderDetailDTO getOrderDetail(Integer orderId) {
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
//
//        List<OrderItemDTO> itemDTOs = items.stream()
//                .map(item -> OrderItemDTO.builder()
//                        .productId(item.getProduct().getProductId())
//                        .productName(item.getProduct().getName())
//                        .quantity(item.getQuantity())
//                        .unitPrice(item.getUnitPrice())
//                        .lineTotal(item.getLineTotal())
//                        .build())
//                .collect(Collectors.toList());
//
//        return OrderDetailDTO.builder()
//                .orderId(order.getOrderId())
//                .orderDate(order.getOrderDate())
//                .status(order.getStatus())
//                .totalAmount(order.getTotalAmount())
//                .shippingAddress(order.getShippingAddress())
//                .items(itemDTOs)
//                .notes(order.getNotes())
//                .build();
//    }




    /**
     * Lấy lịch làm việc theo ngày
     */
    public Map<String, List<ScheduleSlotDTO>> getStaffScheduleByDate(
            LocalDate date, String staffType) {

        int dayOfWeek = date.getDayOfWeek().getValue();

        // Lấy tất cả staff schedule cho ngày này
        List<StaffWorkingSchedule> schedules;
        if (staffType != null && !staffType.isEmpty()) {
            schedules = scheduleRepository.findByDayOfWeekAndStaffTypeAndIsActiveTrue(
                    dayOfWeek, staffType);
        } else {
            schedules = scheduleRepository.findByDayOfWeekAndActiveTrue(dayOfWeek);
        }

        // Group by staff
        Map<String, List<ScheduleSlotDTO>> result = new LinkedHashMap<>();

        for (StaffWorkingSchedule schedule : schedules) {
            String staffName = schedule.getStaff() != null ?
                    schedule.getStaff().getFullName() : "Unknown";

            List<ScheduleSlotDTO> slots = createSlotsForSchedule(schedule, date);

            result.computeIfAbsent(staffName, k -> new ArrayList<>()).addAll(slots);
        }

        return result;
    }

    /**
     * Tạo các slot cho schedule
     */
    private List<ScheduleSlotDTO> createSlotsForSchedule(
            StaffWorkingSchedule schedule, LocalDate date) {

        List<ScheduleSlotDTO> slots = new ArrayList<>();

        // Chia thành các slot 30 phút
        LocalTime currentTime = schedule.getStartTime();
        LocalTime endTime = schedule.getEndTime();
        int slotDuration = 30; // minutes

        while (currentTime.plusMinutes(slotDuration).isBefore(endTime) ||
                currentTime.plusMinutes(slotDuration).equals(endTime)) {

            LocalTime slotEndTime = currentTime.plusMinutes(slotDuration);

            // Lấy bookings trong slot này
            List<Booking> bookingsInSlot = getBookingsInSlot(
                    schedule.getStaffId(),
                    date,
                    currentTime,
                    slotEndTime
            );

            List<BookingSlotDTO> bookingDTOs = bookingsInSlot.stream()
                    .map(b -> BookingSlotDTO.builder()
                            .bookingId(b.getBookingId())
                            .customerName(b.getCustomer().getFullName())
                            .serviceName(b.getService().getName())
                            .petName(b.getPet().getName())
                            .startTime(b.getStartTime().toLocalTime())
                            .endTime(b.getEndTime().toLocalTime())
                            .build())
                    .collect(Collectors.toList());

            int maxBookings = schedule.getMaxDailyBookings() != null ?
                    schedule.getMaxDailyBookings() : 5;

            ScheduleSlotDTO slot = ScheduleSlotDTO.builder()
                    .staffId(schedule.getStaffId())
                    .staffName(schedule.getStaff() != null ?
                            schedule.getStaff().getFullName() : null)
                    .staffType(schedule.getStaff() != null ?
                            schedule.getStaff().getRole(): null)
                    .startTime(currentTime)
                    .endTime(slotEndTime)
                    .currentBookings(bookingsInSlot.size())
                    .maxBookings(maxBookings)
                    .available(bookingsInSlot.size() < maxBookings)
                    .bookings(bookingDTOs)
                    .build();

            slots.add(slot);
            currentTime = slotEndTime;
        }

        return slots;
    }

    /**
     * Lấy bookings trong slot
     */
    private List<Booking> getBookingsInSlot(Integer staffId, LocalDate date,
                                            LocalTime startTime, LocalTime endTime) {
        LocalDateTime slotStart = LocalDateTime.of(date, startTime);
        LocalDateTime slotEnd = LocalDateTime.of(date, endTime);

        return bookingRepository.findByStaffAndDateTimeRange(
                staffId, date, slotStart, slotEnd);
    }



    /**
     * Thêm ghi chú
     */
    public CustomerNoteDTO addNote(CustomerNoteRequest request, UserAccount currentUser) {
        CustomerProfile customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Giả sử có entity CustomerNote
        CustomerNote note = new CustomerNote();
        note.setCustomer(customer);
        note.setNoteText(request.getNoteText());
        note.setNoteType(request.getNoteType());

         note.setCreatedBy(currentUser);
        note.setCreatedAt(LocalDateTime.now());

        note = noteRepository.save(note);

        return mapToNoteDTO(note);
    }

    /**
     * Lấy ghi chú của khách hàng
     */
    public List<CustomerNoteDTO> getCustomerNotes(Integer customerId) {
        List<CustomerNote> notes = noteRepository
                .findByCustomer_CustomerIdOrderByCreatedAtDesc(customerId);

        return notes.stream()
                .map(this::mapToNoteDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map to DTO
     */
    private CustomerNoteDTO mapToNoteDTO(CustomerNote note) {
        return CustomerNoteDTO.builder()
                .noteId(note.getNoteId())
                .customerId(note.getCustomer().getCustomerId())
                .noteText(note.getNoteText())
                .noteType(note.getNoteType())
                .createdBy(note.getCreatedBy() != null ?
                        note.getCreatedBy().getFullName() : "System")
                .createdAt(note.getCreatedAt())
                .build();
    }


}
