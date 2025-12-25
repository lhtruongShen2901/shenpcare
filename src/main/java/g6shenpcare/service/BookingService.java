package g6shenpcare.service;

import g6shenpcare.dto.BookingConfirmForm;
import g6shenpcare.dto.BookingMonitorDTO; // [MỚI] Đảm bảo đã tạo class này
import g6shenpcare.entity.Booking;
import g6shenpcare.entity.DailyServiceLimit;
import g6shenpcare.entity.Services;
import g6shenpcare.models.dto.BookingHistoryDTO;
import g6shenpcare.repository.BookingRepository;
import g6shenpcare.repository.DailyServiceLimitRepository;
import g6shenpcare.repository.ServicesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    // 1.1 Tạo Booking mới (Từ phía Khách hàng)
    @Transactional
    public void createClientBooking(Booking booking) {
        // Kiểm tra dịch vụ 
        Services service = servicesRepo.findById(booking.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Dịch vụ không tồn tại."));

        // Thiết lập trạng thái mặc định
        booking.setStatus("PENDING_CONFIRMATION");
        booking.setPaymentStatus("UNPAID");

        // Set tạm giờ là 00:00 nếu chưa có giờ cụ thể
        if (booking.getBookingDate() != null) {
            booking.setStartTime(booking.getBookingDate().atStartOfDay());
        }

        // Fallback: Tránh lỗi Null nếu Controller chưa gửi ID (Dự phòng)
        if (booking.getCustomerId() == null) {
            booking.setCustomerId(1);
        }
        if (booking.getPetId() == null) {
            booking.setPetId(1);
        }

        // Lưu giá dự kiến
        if (service.getFixedPrice() != null) {
            booking.setTotalAmount(service.getFixedPrice());
        } else {
            booking.setTotalAmount(BigDecimal.ZERO);
        }

        bookingRepo.save(booking);
    }

    // 1.2 Xác nhận & Phân công (Từ phía Support Staff)
    @Transactional
    public void confirmBooking(BookingConfirmForm form, Integer supportStaffId) {
        Booking booking = bookingRepo.findById(form.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + form.getBookingId()));

        Services service = servicesRepo.findById(booking.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Dịch vụ không tồn tại"));

        int duration = service.getDurationMinutes();

        // Cập nhật thời gian chính thức
        if (form.getStartTime() != null) {
            // Gộp Ngày (của Booking) + Giờ (từ Form xác nhận)
            booking.setStartTime(booking.getBookingDate().atTime(form.getStartTime()));
            // Tính giờ kết thúc dựa trên thời lượng dịch vụ
            booking.setEndTime(booking.getBookingDate().atTime(form.getStartTime().plusMinutes(duration)));
        }

        booking.setAssignedStaffId(form.getAssignedStaffId());
        // booking.setConfirmedBySupportId(supportStaffId); // Bỏ comment nếu có field này
        booking.setStatus("CONFIRMED");

        bookingRepo.save(booking);
    }

    // =================================================================
    // 2. DỮ LIỆU CHO DASHBOARD MONITOR (DTO & FILTER)
    // =================================================================
    /**
     * Lấy dữ liệu hiển thị lên bảng Giám Sát, có hỗ trợ lọc theo loài (DOG/CAT)
     */
    // Thêm tham số keyword vào hàm này
    public List<BookingMonitorDTO> getBookingMonitorData(LocalDate date, String speciesFilter, String keyword) {
        List<Booking> bookings;

        // 1. Nếu có từ khóa tìm kiếm -> Gọi hàm search
        if (keyword != null && !keyword.trim().isEmpty()) {
            bookings = bookingRepo.searchBookings(date, keyword);
        } else {
            // Không tìm kiếm -> Lấy hết theo ngày
            bookings = bookingRepo.findByBookingDateOrderByStartTimeAsc(date);
        }

        // 2. Logic lọc loài & Convert DTO (Giữ nguyên)
        return bookings.stream()
                .filter(b -> filterBySpecies(b, speciesFilter))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Logic lọc: Nếu filter là ALL thì lấy hết, nếu không thì so sánh Species của Pet
    private boolean filterBySpecies(Booking b, String speciesFilter) {
        if (speciesFilter == null || speciesFilter.equals("ALL")) {
            return true;
        }
        // Nếu booking không có Pet (lỗi dữ liệu cũ) thì có thể chọn bỏ qua hoặc lấy luôn
        if (b.getPet() == null) {
            return false;
        }

        return b.getPet().getSpecies().equalsIgnoreCase(speciesFilter);
    }

// Helper: Chuyển đổi Entity Booking -> DTO hiển thị (Bản Final: An toàn + Đầy đủ)
    private BookingMonitorDTO convertToDTO(Booking b) {
        BookingMonitorDTO dto = new BookingMonitorDTO();

        // 1. Map ID & Trạng thái cơ bản (Luôn có dữ liệu)
        dto.setBookingId(b.getBookingId());
        dto.setStatus(b.getStatus() != null ? b.getStatus() : "UNKNOWN");

        // 2. Xử lý Thời gian (Tránh null cho StartTime để Dashboard sắp xếp được)
        dto.setBookingDate(b.getBookingDate());
        dto.setStartTime(b.getStartTime() != null ? b.getStartTime() : b.getBookingDate().atStartOfDay());
        dto.setEndTime(b.getEndTime());

        // 3. Xử lý Ghi chú & Cờ Khẩn cấp
        String notes = b.getNotes();
        if (notes != null && !notes.trim().isEmpty()) {
            dto.setServiceNote(notes);
            // Kiểm tra "khẩn cấp" không phân biệt hoa thường
            dto.setUrgent(notes.toLowerCase().contains("khẩn cấp"));
        } else {
            dto.setServiceNote("");
            dto.setUrgent(false);
        }

        // 4. Dịch vụ (Kiểm tra null nếu dịch vụ bị xóa khỏi DB)
        if (b.getService() != null) {
            String serviceName = b.getService().getName();
            dto.setServiceName((serviceName != null) ? serviceName : "Dịch vụ lỗi tên");

            // [QUAN TRỌNG] Lấy loại dịch vụ (SPA/VET) để nút "Điều chỉnh" biết gọi Doctor hay Groomer
            dto.setServiceType(b.getService().getServiceType());
        } else {
            dto.setServiceName("Dịch vụ không tồn tại"); // Fallback
            dto.setServiceType("UNKNOWN");
        }

        // 5. Khách hàng (QUAN TRỌNG NHẤT - NGUYÊN NHÂN LỖI TRẮNG TRANG)
        if (b.getCustomer() != null) {
            String fullName = b.getCustomer().getFullName();

            // Nếu Tên = Null hoặc Rỗng hoặc Chỉ có khoảng trắng
            if (fullName == null || fullName.trim().isEmpty()) {
                // Tự động gán tên giả định bằng ID khách hàng
                dto.setCustomerName("Khách #" + b.getCustomerId());
            } else {
                dto.setCustomerName(fullName.trim());
            }

            // Xử lý SĐT
            String phone = b.getCustomer().getPhone();
            dto.setCustomerPhone((phone != null && !phone.isEmpty()) ? phone : "---");
        } else {
            // Trường hợp dữ liệu Booking mồ côi (không có khách hàng)
            dto.setCustomerName("Khách vãng lai");
            dto.setCustomerPhone("---");
        }

        // 6. Thú cưng
        if (b.getPet() != null) {
            String petName = b.getPet().getName();
            dto.setPetName((petName != null && !petName.trim().isEmpty()) ? petName : "Pet không tên");

            String species = b.getPet().getSpecies();
            dto.setPetSpecies((species != null) ? species : "---");

            String breed = b.getPet().getBreed();
            dto.setPetBreed((breed != null) ? breed : "");
        } else {
            dto.setPetName("Chưa có Pet");
            dto.setPetSpecies("---");
            dto.setPetBreed("");
        }

        // 7. Nhân viên
        if (b.getStaff() != null) {
            String staffName = b.getStaff().getFullName();
            dto.setStaffName((staffName != null && !staffName.trim().isEmpty()) ? staffName : "Staff #" + b.getAssignedStaffId());
        } else {
            dto.setStaffName("Chưa gán");
        }

        return dto;
    }
    // =================================================================
    // 3. QUẢN LÝ SỨC CHỨA (QUOTA & CAPACITY)
    // =================================================================

    // Lấy giới hạn tối đa (Max Quota) của 1 loại dịch vụ trong ngày
    public int getMaxQuota(LocalDate date, String serviceType) {
        return limitRepo.findByApplyDateAndServiceType(date, serviceType)
                .map(DailyServiceLimit::getMaxQuota)
                .orElseGet(() -> limitRepo.findByServiceTypeAndApplyDateIsNull(serviceType)
                .map(DailyServiceLimit::getMaxQuota)
                .orElse(20)); // Mặc định 20 nếu chưa cấu hình
    }

    // Đếm số lượng đơn hiện tại
    public long getCurrentCount(LocalDate date, String serviceType) {
        return bookingRepo.countByDateAndServiceType(date, serviceType);
    }

    // Kiểm tra xem ngày đó đã quá tải chưa (trả về true/false)
    public boolean isDateOverloaded(LocalDate date, String serviceType) {
        return getCurrentCount(date, serviceType) >= getMaxQuota(date, serviceType);
    }
    // =================================================================
    // 4. CHỨC NĂNG CẬP NHẬT / DỜI LỊCH (RESCHEDULE)
    // =================================================================

    @Transactional
    public void updateBookingDetails(Integer bookingId, LocalDate newDate, LocalTime newTime, Integer newStaffId) {
        // 1. Tìm đơn hàng cần sửa
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + bookingId));

        // 2. Lấy thông tin dịch vụ để tính lại giờ kết thúc (Duration)
        // Cần query lại serviceRepo để đảm bảo lấy đúng thời lượng
        Services service = servicesRepo.findById(booking.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Dịch vụ gốc không tồn tại (ID: " + booking.getServiceId() + ")"));

        int duration = service.getDurationMinutes();

        // 3. Cập nhật Ngày & Giờ
        booking.setBookingDate(newDate); // Cập nhật ngày hiển thị

        // Gộp [Ngày Mới] + [Giờ Mới] = [StartTime Mới]
        booking.setStartTime(newDate.atTime(newTime));

        // Tính lại EndTime = StartTime + Thời lượng dịch vụ
        booking.setEndTime(booking.getStartTime().plusMinutes(duration));

        // 4. Cập nhật Nhân viên mới
        booking.setAssignedStaffId(newStaffId);

        // Lưu xuống Database
        // (@PreUpdate trong Entity sẽ tự động cập nhật cột UpdatedAt)
        bookingRepo.save(booking);
    }



    @Transactional
    public List<BookingHistoryDTO> getBookingHistoryByCustomer(Integer customerId) {

        return bookingRepo
                .findByCustomer_CustomerIdOrderByBookingDateDesc(customerId)
                .stream()
                .map(this::toHistoryDTO)
                .toList();
    }

    private BookingHistoryDTO toHistoryDTO(Booking b) {
        return new BookingHistoryDTO(
                b.getBookingId(),
                b.getBookingDate(),
                b.getStatus(),
                b.getTotalAmount(),
                b.getService() != null ? b.getService().getName() : null,
                b.getPet() != null ? b.getPet().getName() : null
        );
    }
}
