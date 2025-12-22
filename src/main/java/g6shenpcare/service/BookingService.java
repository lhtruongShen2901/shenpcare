package g6shenpcare.service;

import g6shenpcare.dto.BookingConfirmForm;
import g6shenpcare.dto.BookingMonitorDTO;
import g6shenpcare.dto.BookingRequestDTO;
import g6shenpcare.entity.Booking;
import g6shenpcare.entity.DailyServiceLimit;
import g6shenpcare.entity.Services;
import g6shenpcare.repository.BookingRepository;
import g6shenpcare.repository.DailyServiceLimitRepository;
import g6shenpcare.repository.ServicesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepo;
    private final ServicesRepository servicesRepo;
    private final DailyServiceLimitRepository limitRepo;

    public BookingService(BookingRepository bookingRepo,
                          ServicesRepository servicesRepo,
                          DailyServiceLimitRepository limitRepo) {
        this.bookingRepo = bookingRepo;
        this.servicesRepo = servicesRepo;
        this.limitRepo = limitRepo;
    }

    // =================================================================
    // 1. TẠO & XỬ LÝ BOOKING (CORE FLOW)
    // =================================================================
    
    // 1.1 Tạo Booking mới (Từ phía Khách hàng - Sử dụng DTO)
    @Transactional
    public void createClientBooking(BookingRequestDTO dto) {
        Booking booking = new Booking();

        // 1. Thông tin cơ bản
        if (dto.getCustomerId() != null) {
            booking.setCustomerId(dto.getCustomerId().intValue());
        }
        
        // [QUAN TRỌNG] Kiểm tra null cho PetId (Cho phép đặt lịch không Pet)
        if (dto.getPetId() != null) {
            booking.setPetId(dto.getPetId().intValue());
        } else {
            booking.setPetId(null); 
        }

        booking.setServiceId(dto.getServiceId());
        booking.setBookingDate(dto.getBookingDate());
        booking.setNotes(dto.getNotes()); 
        booking.setIsUrgent(dto.getIsUrgent()); 

        // 2. Xử lý Thời gian
        if (dto.getTimeSlot() != null && !dto.getTimeSlot().isEmpty()) {
            try {
                LocalTime time = LocalTime.parse(dto.getTimeSlot());
                LocalDateTime startDateTime = LocalDateTime.of(dto.getBookingDate(), time);
                booking.setStartTime(startDateTime);
            } catch (Exception e) {
                booking.setStartTime(dto.getBookingDate().atStartOfDay());
            }
        } else {
            booking.setStartTime(dto.getBookingDate().atStartOfDay());
        }

        // 3. Lấy thông tin Dịch vụ & Tính toán
        Services service = servicesRepo.findById(dto.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Dịch vụ không tồn tại."));

        if (booking.getStartTime() != null) {
            int duration = (service.getDurationMinutes() != null) ? service.getDurationMinutes() : 60;
            booking.setEndTime(booking.getStartTime().plusMinutes(duration));
        }

        if (service.getFixedPrice() != null) {
            booking.setTotalAmount(service.getFixedPrice());
        } else {
            booking.setTotalAmount(BigDecimal.ZERO);
        }

        booking.setStatus("PENDING");
        booking.setPaymentStatus("UNPAID");
        booking.setCreatedAt(LocalDateTime.now());

        bookingRepo.save(booking);
    }

    // 1.2 Xác nhận & Phân công
    @Transactional
    public void confirmBooking(BookingConfirmForm form, Integer supportStaffId) {
        Booking booking = bookingRepo.findById(form.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + form.getBookingId()));

        Services service = servicesRepo.findById(booking.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Dịch vụ không tồn tại"));

        int duration = (service.getDurationMinutes() != null) ? service.getDurationMinutes() : 60;

        if (form.getConfirmTime() != null) {
            booking.setStartTime(booking.getBookingDate().atTime(form.getConfirmTime()));
            booking.setEndTime(booking.getBookingDate().atTime(form.getConfirmTime().plusMinutes(duration)));
        }

        booking.setAssignedStaffId(form.getAssignedStaffId());
        booking.setStatus("CONFIRMED");

        bookingRepo.save(booking);
    }

    // =================================================================
    // 2. DỮ LIỆU CHO DASHBOARD MONITOR
    // =================================================================
    
    public List<BookingMonitorDTO> getBookingMonitorData(LocalDate date, String speciesFilter, String keyword) {
        List<Booking> bookings;
        if (keyword != null && !keyword.trim().isEmpty()) {
            bookings = bookingRepo.searchBookings(date, keyword);
        } else {
            bookings = bookingRepo.findByBookingDateOrderByStartTimeAsc(date);
        }

        return bookings.stream()
                .filter(b -> filterBySpecies(b, speciesFilter))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private boolean filterBySpecies(Booking b, String speciesFilter) {
        if (speciesFilter == null || speciesFilter.equals("ALL")) return true;
        if (b.getPet() == null) return false;
        return b.getPet().getSpecies().equalsIgnoreCase(speciesFilter);
    }

    private BookingMonitorDTO convertToDTO(Booking b) {
        BookingMonitorDTO dto = new BookingMonitorDTO();
        dto.setBookingId(b.getBookingId());
        dto.setStatus(b.getStatus() != null ? b.getStatus() : "UNKNOWN");
        dto.setBookingDate(b.getBookingDate());
        dto.setStartTime(b.getStartTime() != null ? b.getStartTime() : b.getBookingDate().atStartOfDay());
        dto.setEndTime(b.getEndTime());

        String notes = b.getNotes();
        if (notes != null && !notes.trim().isEmpty()) {
            dto.setServiceNote(notes);
            dto.setUrgent(notes.toLowerCase().contains("khẩn cấp"));
        } else {
            dto.setServiceNote("");
            dto.setUrgent(false);
        }

        if (b.getService() != null) {
            dto.setServiceName(b.getService().getName());
            dto.setServiceType(b.getService().getServiceType());
        } else {
            dto.setServiceName("Dịch vụ lỗi");
            dto.setServiceType("UNKNOWN");
        }

        if (b.getCustomer() != null) {
            String fullName = b.getCustomer().getFullName();
            dto.setCustomerName((fullName != null && !fullName.trim().isEmpty()) ? fullName : "Khách #" + b.getCustomerId());
            dto.setCustomerPhone(b.getCustomer().getPhone());
        } else {
            dto.setCustomerName("Khách vãng lai");
            dto.setCustomerPhone("---");
        }

        if (b.getPet() != null) {
            dto.setPetName(b.getPet().getName());
            dto.setPetSpecies(b.getPet().getSpecies());
            dto.setPetBreed(b.getPet().getBreed());
        } else {
            dto.setPetName("Chưa có Pet");
            dto.setPetSpecies("---");
        }

        if (b.getStaff() != null) {
            dto.setStaffName(b.getStaff().getFullName());
        } else {
            dto.setStaffName("Chưa gán");
        }

        return dto;
    }

    // =================================================================
    // 3. QUẢN LÝ SỨC CHỨA
    // =================================================================

    public int getMaxQuota(LocalDate date, String serviceType) {
        return limitRepo.findByApplyDateAndServiceType(date, serviceType)
                .map(DailyServiceLimit::getMaxQuota)
                .orElseGet(() -> limitRepo.findByServiceTypeAndApplyDateIsNull(serviceType)
                .map(DailyServiceLimit::getMaxQuota)
                .orElse(20));
    }

    public long getCurrentCount(LocalDate date, String serviceType) {
        return bookingRepo.countByDateAndServiceType(date, serviceType);
    }

    public boolean isDateOverloaded(LocalDate date, String serviceType) {
        return getCurrentCount(date, serviceType) >= getMaxQuota(date, serviceType);
    }

    // =================================================================
    // 4. CHỨC NĂNG CẬP NHẬT / DỜI LỊCH
    // =================================================================

    @Transactional
    public void updateBookingDetails(Integer bookingId, LocalDate newDate, LocalTime newTime, Integer newStaffId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + bookingId));

        Services service = servicesRepo.findById(booking.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Dịch vụ gốc không tồn tại"));

        int duration = (service.getDurationMinutes() != null) ? service.getDurationMinutes() : 60;

        booking.setBookingDate(newDate);
        booking.setStartTime(newDate.atTime(newTime));
        booking.setEndTime(booking.getStartTime().plusMinutes(duration));
        booking.setAssignedStaffId(newStaffId);

        bookingRepo.save(booking);
    }
}