package g6shenpcare.repository;

import g6shenpcare.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    long countByStatusIgnoreCase(String status);


    boolean existsByPetId(Integer petId);

    // Tìm theo ngày (Logic cũ, không sắp xếp) - Giữ lại để tránh lỗi code cũ
    List<Booking> findByBookingDate(LocalDate bookingDate);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.assignedStaffId = :staffId "
            + "AND b.status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS')")
    long countActiveBookingsByStaff(@Param("staffId") Integer staffId);

    @Modifying
    @Query("UPDATE Booking b SET b.assignedStaffId = :newStaffId "
            + "WHERE b.assignedStaffId = :oldStaffId "
            + "AND b.status IN ('PENDING', 'CONFIRMED')")
    void reassignBookings(@Param("oldStaffId") Integer oldStaffId,
            @Param("newStaffId") Integer newStaffId);

    @Query("SELECT b FROM Booking b "
            + "LEFT JOIN b.customer c "
            + "LEFT JOIN b.pet p "
            + "WHERE b.bookingDate = :date "
            + "AND (:keyword IS NULL OR :keyword = '' OR "
            + "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "c.phone LIKE CONCAT('%', :keyword, '%') OR "
            + "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Booking> searchBookings(@Param("date") LocalDate date, @Param("keyword") String keyword);
    // =========================================================
    // 2. CÁC HÀM BỔ SUNG MỚI (CHO MONITOR, QUOTA, DOCTOR)
    // =========================================================

    // [MỚI] Lấy danh sách theo ngày và SẮP XẾP THEO GIỜ (Sáng -> Chiều)
    // Hàm này dùng cho Dashboard để hiển thị timeline chuẩn xác
    List<Booking> findByBookingDateOrderByStartTimeAsc(LocalDate bookingDate);

    // [CẬP NHẬT] Đếm số lượng Booking theo ngày và loại dịch vụ (Để tính % Quota)
    // Lưu ý: Đã cập nhật cú pháp JOIN "b.service" để tương thích với Entity mới có @ManyToOne
    @Query("SELECT COUNT(b) FROM Booking b "
            + "JOIN b.service s "
            + // Dùng relationship b.service thay vì join thủ công ID
            "WHERE b.bookingDate = :date "
            + "AND s.serviceType = :serviceType "
            + "AND b.status <> 'CANCELLED'")
    long countByDateAndServiceType(@Param("date") LocalDate date,
            @Param("serviceType") String serviceType);

    // [MỚI] Tìm booking theo nhân viên trong ngày (Dùng cho Doctor Dashboard sau này)
    List<Booking> findByAssignedStaffIdAndBookingDate(Integer staffId, LocalDate bookingDate);


    List<Booking> findByCustomerIdOrderByStartTimeDesc(Integer customerId);
    List<Booking> findByPetIdOrderByStartTimeDesc(Integer petId);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.assignedStaffId = :staffId " +
            "AND b.startTime >= :startDateTime " +
            "AND b.startTime <= :endDateTime " +
            "ORDER BY b.startTime ASC")
    List<Booking> findByAssignedStaffIdAndDateRange(
            @Param("staffId") Integer staffId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );


    Page<Booking> findByAssignedStaffId(Integer staffId, Pageable pageable);

    Page<Booking> findByAssignedStaffIdAndStatus(Integer staffId, String status, Pageable pageable);

    List<Booking> findByPetIdAndBookingIdNotAndStatusOrderByStartTimeDesc(
            Integer petId, Integer excludeBookingId, String status);


    List<Booking> findByCustomer_CustomerIdOrderByBookingDateDesc(Integer customerId);


    List<Booking> findTop5ByPetIdOrderByBookingDateDesc(Integer petId);


    List<Booking> findByCustomerIdOrderByBookingDateDesc(Integer customerId);

    List<Booking> findByPetIdOrderByBookingDateDesc(Integer petId);

    Optional<Booking> findTopByPetIdOrderByBookingDateDesc(Integer petId);

    int countByPetIdAndStatusIn(Integer petId, List<String> statuses);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingDate = :date " +
            "AND b.startTime >= :startTime AND b.startTime < :endTime " +
            "AND b.staff.userId = :staffId " +
            "AND b.status NOT IN ('CANCELLED')")
    int countByBookingDateAndTimeRange(
            @Param("date") LocalDate date,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("staffId") Integer staffId
    );

    @Query("SELECT b FROM Booking b WHERE b.staff.userId = :staffId " +
            "AND b.bookingDate = :date " +
            "AND b.startTime >= :startTime AND b.startTime < :endTime " +
            "AND b.status NOT IN ('CANCELLED')")
    List<Booking> findByStaffAndDateTimeRange(
            @Param("staffId") Integer staffId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}