package g6shenpcare.service;


import g6shenpcare.entity.*;
import g6shenpcare.models.dto.GroomerPetInfoDTO;
import g6shenpcare.models.dto.GroomerScheduleDTO;
import g6shenpcare.models.dto.GroomingProgressRequest;
import g6shenpcare.models.dto.PreviousVisitDTO;
import g6shenpcare.models.entity.GroomingBoardingProgress;
import g6shenpcare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroomerService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private GroomingBoardingProgressRepository progressRepository;


    @Autowired
    private StaffProfileRepository staffProfileRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private CustomerProfileRepository customerRepository;

    @Autowired
    private ServicesRepository serviceRepository;

    @Autowired
    private  GroomingBoardingProgressRepository groomingProgressRepository;

    @Autowired
    private  UserAccountRepository userRepository;


    public List<GroomerScheduleDTO> getGroomerSchedule(
            Integer staffId,
            LocalDate startDate,
            LocalDate endDate,
            String serviceType,
            String search) {

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Booking> bookings = bookingRepository.findByAssignedStaffIdAndDateRange(
                staffId, startDateTime, endDateTime
        );

        // Filter by service type if provided
        if (serviceType != null && !serviceType.isEmpty()) {
            bookings = bookings.stream()
                    .filter(b -> matchesServiceType(b.getService(), serviceType))
                    .collect(Collectors.toList());
        }

        // Filter by search term if provided
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            bookings = bookings.stream()
                    .filter(b ->
                            b.getCustomer().getFullName().toLowerCase().contains(searchLower) ||
                                    b.getPet().getName().toLowerCase().contains(searchLower) ||
                                    b.getService().getName().toLowerCase().contains(searchLower)
                    )
                    .collect(Collectors.toList());
        }

        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateBookingStatus(Integer bookingId, Integer staffId, String status, String notes) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        StaffProfile staffProfile = staffProfileRepository.findByUser_UserId(staffId);




        // Verify staff assignment
        if (!booking.getStaff().getUserId().equals(staffId)) {
            throw new RuntimeException("Unauthorized");
        }

        // Update booking status
        booking.setStatus(status);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // Create progress record
        GroomingBoardingProgress progress = new GroomingBoardingProgress();
        progress.setBooking(booking);
        progress.setStatus(status);
        progress.setUpdatedBy(staffProfile);
        progress.setUpdatedAt(LocalDateTime.now());
        progress.setNotes(notes);
        progressRepository.save(progress);
    }

    private boolean matchesServiceType(Services service, String serviceType) {
        if (serviceType == null || serviceType.isEmpty()) return true;

        String categoryType = service.getServiceCategory().getCategoryType(); //  SPA | BOARDING

        return serviceType.equalsIgnoreCase(categoryType);
    }


    private GroomerScheduleDTO convertToDTO(Booking booking) {
        GroomerScheduleDTO dto = new GroomerScheduleDTO();
        dto.setBookingId(booking.getBookingId());
        dto.setBookingDate(booking.getBookingDate());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setStatus(booking.getStatus());
        dto.setNotes(booking.getNotes());

        // Customer info
        dto.setCustomerName(booking.getCustomer().getFullName());
        dto.setCustomerPhone(booking.getCustomer().getPhone());

        // Pet info
        dto.setPetName(booking.getPet().getName());
        dto.setPetSpecies(booking.getPet().getSpecies());
        dto.setPetBreed(booking.getPet().getBreed());
        dto.setPetWeightKg(Double.valueOf(booking.getPet().getWeightKg()));

        // Service info
        dto.setServiceName(booking.getService().getName());
        dto.setServiceCategory(booking.getService().getServiceCategory().getName());
        dto.setDurationMinutes(booking.getService().getDurationMinutes());
        dto.setTotalAmount(booking.getTotalAmount());

        // Get latest progress
        List<GroomingBoardingProgress> progresses =
                progressRepository.findByBooking_BookingIdOrderByUpdatedAtDesc(booking.getBookingId());
        if (!progresses.isEmpty()) {
            dto.setLastProgressNotes(progresses.get(0).getNotes());
            dto.setLastProgressTime(progresses.get(0).getUpdatedAt());
        }

        return dto;
    }


    /**
     * Lấy thông tin đầy đủ về thú cưng và yêu cầu grooming
     */
    public GroomerPetInfoDTO getPetInfoForGrooming(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID: " + bookingId));

        Pets pet = petRepository.findById(booking.getPetId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin thú cưng"));

        CustomerProfile customer = customerRepository.findById(booking.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng"));

        Services service = serviceRepository.findById(booking.getServiceId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin dịch vụ"));

        // Tính tuổi thú cưng
        String petAge = calculatePetAge(pet.getDateOfBirth());

        // Lấy lịch sử grooming trước đây
        List<PreviousVisitDTO> previousVisits = getPreviousGroomingHistory(pet.getPetId(), bookingId);

        return GroomerPetInfoDTO.builder()
                .booking(booking)
                .pet(pet)
                .customer(customer)
                .service(service)
                .petAge(petAge)
                .previousVisits(previousVisits)
                .build();
    }

    /**
     * Kiểm tra xem booking có được assign cho groomer này không
     */
    public boolean isAssignedToGroomer(Integer bookingId, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));

        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        // Kiểm tra role
        if (!"GROOMER".equals(user.getRole())) {
            return false;
        }

        // Kiểm tra xem có được assign không
        return booking.getStaff().getUserId() != null &&
                booking.getStaff().getUserId().equals(user.getUserId());
    }



    /**
     * Lấy danh sách booking của groomer
     */
    public Page<Booking> getGroomerBookings(String username, String status, int page, int size) {
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());

        if (status != null && !status.isEmpty()) {
            return bookingRepository.findByAssignedStaffIdAndStatus(user.getUserId(), status, pageable);
        } else {
            return bookingRepository.findByAssignedStaffId(user.getUserId(), pageable);
        }
    }

    /**
     * Tính tuổi thú cưng
     */
    private String calculatePetAge(java.time.LocalDate birthDate) {
        if (birthDate == null) {
            return "Không rõ";
        }

        Period period = Period.between(birthDate, java.time.LocalDate.now());
        int years = period.getYears();
        int months = period.getMonths();

        if (years > 0) {
            if (months > 0) {
                return years + " tuổi " + months + " tháng";
            }
            return years + " tuổi";
        } else if (months > 0) {
            return months + " tháng tuổi";
        } else {
            return period.getDays() + " ngày tuổi";
        }
    }

    /**
     * Lấy lịch sử grooming trước đây của thú cưng
     */
    private List<PreviousVisitDTO> getPreviousGroomingHistory(Integer petId, Integer currentBookingId) {
        List<Booking> previousBookings = bookingRepository
                .findByPetIdAndBookingIdNotAndStatusOrderByStartTimeDesc(
                        petId, currentBookingId, "COMPLETED");

        return previousBookings.stream()
                .limit(5) // Chỉ lấy 5 lần gần nhất
                .map(booking -> {
                    Services service = serviceRepository.findById(booking.getServiceId())
                            .orElse(null);

                    return PreviousVisitDTO.builder()
                            .bookingId(booking.getBookingId())
                            .startTime(booking.getStartTime())
                            .serviceName(service != null ? service.getName() : "N/A")
                            .status(booking.getStatus())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi checklist sang tiếng Việt
     */
    private String[] convertChecklistToVietnamese(String[] checklist) {
        String[] result = new String[checklist.length];
        for (int i = 0; i < checklist.length; i++) {
            switch (checklist[i]) {
                case "bath":
                    result[i] = "Tắm và làm sạch";
                    break;
                case "dry":
                    result[i] = "Sấy khô lông";
                    break;
                case "trim":
                    result[i] = "Cắt tỉa lông";
                    break;
                case "nails":
                    result[i] = "Cắt móng";
                    break;
                case "ears":
                    result[i] = "Vệ sinh tai";
                    break;
                case "teeth":
                    result[i] = "Vệ sinh răng miệng";
                    break;
                case "glands":
                    result[i] = "Nặn tuyến hôi";
                    break;
                default:
                    result[i] = checklist[i];
            }
        }
        return result;
    }



    /**
     * Lấy lịch sử cập nhật tiến độ của booking
     */
    public List<GroomingBoardingProgress> getProgressHistory(Integer bookingId) {
        return groomingProgressRepository.findByBooking_BookingIdOrderByUpdatedAtDesc(bookingId);
    }

    /**
     * Cập nhật tiến độ grooming
     */
    @Transactional
    public void updateGroomingProgress(Integer bookingId, GroomingProgressRequest request, String staffUsername) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        UserAccount staff = userRepository.findByUsername(staffUsername)
                .orElseThrow(() -> new RuntimeException("Staff không tồn tại"));

        StaffProfile staffProfile = staff.getStaffProfile();
        if (staffProfile == null) {
            throw new RuntimeException("Staff profile không tồn tại");
        }

        // Tạo bản ghi progress mới
        GroomingBoardingProgress progress = GroomingBoardingProgress.builder()
                .booking(booking)
                .status("IN_PROGRESS")
                .notes(request.getNotes())
                .updatedAt(LocalDateTime.now())
                .updatedBy(staffProfile)
                .build();

        groomingProgressRepository.save(progress);

        // Cập nhật status booking nếu chưa IN_PROGRESS
        if (!"IN_PROGRESS".equals(booking.getStatus())) {
            booking.setStatus("IN_PROGRESS");
            bookingRepository.save(booking);
        }
    }

    /**
     * Hoàn thành grooming
     */
    @Transactional
    public void completeGrooming(Integer bookingId, GroomingProgressRequest request, String staffUsername) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        UserAccount staff = userRepository.findByUsername(staffUsername)
                .orElseThrow(() -> new RuntimeException("Staff không tồn tại"));

        StaffProfile staffProfile = staff.getStaffProfile();
        if (staffProfile == null) {
            throw new RuntimeException("Staff profile không tồn tại");
        }

        // Tạo bản ghi progress hoàn thành
        GroomingBoardingProgress progress = GroomingBoardingProgress.builder()
                .booking(booking)
                .status("COMPLETED")
                .notes(request.getNotes())
                .updatedAt(LocalDateTime.now())
                .updatedBy(staffProfile)
                .build();

        groomingProgressRepository.save(progress);

        // Cập nhật status booking
        booking.setStatus("COMPLETED");
        bookingRepository.save(booking);
    }

}